package anonymization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;
import util.BarabasiAlbertGraphGenerator;
import util.GraphUtil;
import util.WattsStrogatzGraphGenerator;

/***
 * 
 * This class implements an alternative K-Match, which follows the description in Zou et al.'s paper, 
 * except that it uses METIS for graph partitioning
 *
 */

public class KMatchAnonymizerUsingMETIS {
	
	protected static int timesExceptionOccurred = 0;
	
	protected Map<String, List<String>> globalVAT;
	
	public KMatchAnonymizerUsingMETIS() {
		globalVAT = null;
	}
	
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomized, String uniqueIdFileName) {
		if (k < graph.vertexSet().size()) {
			globalVAT = new TreeMap<>();
			performAnonymization(graph, k, randomized, uniqueIdFileName);
		}
		else {
			// Convert graph into a K_k
			int origVertCount = graph.vertexSet().size();
			int dummyIndex = GraphUtil.maxVertexId(graph) + 1;
			for (int i = 0; i < k - origVertCount; i++) {
				graph.addVertex("" + dummyIndex);
				dummyIndex++;
			}
			List<String> vertList = new ArrayList<>(graph.vertexSet());
			for (int i = 0; i < vertList.size() - 1; i++)
				for (int j = i + 1; j < vertList.size(); j++)
					if (!graph.containsEdge(vertList.get(i), vertList.get(j)))
						graph.addEdge(vertList.get(i), vertList.get(j));
		}
	}

	protected void performAnonymization(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomized, String uniqueIdFileName) {
		
		List<UndirectedGraph<String, DefaultEdge>> partitions = null;
		
		try {
			
			// Generate METIS input
			int startingVertId = GraphUtil.minVertexId(graph);
			int vertIdOffset = 1 - startingVertId;   // To make sure that vertex ids start at 1 as required by METIS
			if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
				generateMetisInput(graph, "C:\\cygwin64\\home\\yunior.ramirez\\metis-5.1.0\\graphs\\workingGraph-" + uniqueIdFileName + ".txt", false, vertIdOffset);
			else
				generateMetisInput(graph, "/home/users/yramirezcruz/metis-5.1.0/graphs/workingGraph-" + uniqueIdFileName + ".txt", false, vertIdOffset);
				
			// Run METIS
			Runtime rt = Runtime.getRuntime();
			String command = null;
			if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
				command = "C:\\cygwin64\\usr\\local\\bin\\gpmetis.exe C:\\cygwin64\\home\\yunior.ramirez\\metis-5.1.0\\graphs\\workingGraph-" + uniqueIdFileName + ".txt " + k;
			else
				command = "/home/users/yramirezcruz/bin/gpmetis /home/users/yramirezcruz/metis-5.1.0/graphs/workingGraph-" + uniqueIdFileName + ".txt " + k;
			Process proc = rt.exec(command);
			proc.waitFor();
			if (proc.exitValue() != 0)   // Unsuccessful execution 	
				throw new RuntimeException();
			
			// Load METIS output
			Map<String, Set<String>> vertsXPart = null;
			if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
				vertsXPart = loadMetisOutput("C:\\cygwin64\\home\\yunior.ramirez\\metis-5.1.0\\graphs\\workingGraph-" + uniqueIdFileName + ".txt.part." + k, startingVertId);
			else
				vertsXPart = loadMetisOutput("/home/users/yramirezcruz/metis-5.1.0/graphs/workingGraph-" + uniqueIdFileName + ".txt.part." + k, startingVertId);
			partitions = new ArrayList<>();
			for (String pid : vertsXPart.keySet())
				partitions.add(GraphUtil.inducedSubgraph(graph, vertsXPart.get(pid)));
			
		} catch (IOException | InterruptedException | RuntimeException e) {
			
			timesExceptionOccurred++;   // Just update, initialization and use up to the caller
			
			// Since some problem occurred in running METIS or handling its outputs, 
			// a naive partition is made by decrementally sorting vertices by degree 
			// and assigning each vertex to a partition using round-robin 
			List<Set<String>> vertsXPart = new ArrayList<>();
			for (int i = 0; i < k; i++)
				vertsXPart.add(new TreeSet<String>());
			List<String> sortedVertList = GraphUtil.degreeSortedVertexList(graph, false);
			for (int i = 0; i < sortedVertList.size(); i++)
				vertsXPart.get(i % k).add(sortedVertList.get(i));
			partitions = new ArrayList<>();
			for (int i = 0; i < k; i++)
				partitions.add(GraphUtil.inducedSubgraph(graph, vertsXPart.get(i)));
		}
		
		// Perform the anonymization
		globalVAT = getVAT(graph, partitions);
		alignBlocks(graph, globalVAT);
		if (randomized)
			randomlyUniformizeCrossingEdges(graph);
		else
			copyCrossingEdges(graph);   // Original approach by Zou et al.	
	}
	
	protected void generateMetisInput(UndirectedGraph<String, DefaultEdge> graph, String fileName, boolean weightedVertices, int vertIdOffset) throws IOException {
		final String NEW_LINE = System.getProperty("line.separator");
		int maxDeg = -1;
		if (weightedVertices) {
			for (String v : graph.vertexSet())
				if (graph.degreeOf(v) > maxDeg)
					maxDeg = graph.degreeOf(v);
			maxDeg++;   // So we get weight 1 for vertices of degree maxDeg, instead of 0
		}
		Writer metisInputWriter = new FileWriter(fileName, false);
		if (weightedVertices)
			metisInputWriter.append("" + graph.vertexSet().size() + " " + graph.edgeSet().size() + " 010 1" + NEW_LINE);
		else
			metisInputWriter.append("" + graph.vertexSet().size() + " " + graph.edgeSet().size() + NEW_LINE);
		for (String v : graph.vertexSet()) {
			String line = ""; 
			if (weightedVertices)
				line += (maxDeg - graph.degreeOf(v)) + " ";
			for (String w : Graphs.neighborListOf(graph, v))
				line += (Integer.parseInt(w) + vertIdOffset) + " ";
			metisInputWriter.append(line.trim() + NEW_LINE);
		}
		metisInputWriter.close();
	}
	
	protected Map<String, Set<String>> loadMetisOutput(String fileName, int startingVertId) throws IOException {
		Map<String, Set<String>> vertsXPart = new TreeMap<>();
		int vertId = startingVertId;
		BufferedReader metisOuputReader = new BufferedReader(new FileReader(new File(fileName)));
		for (String line = metisOuputReader.readLine(); line != null; line = metisOuputReader.readLine()) {
			if (!line.trim().equals("")) {
				if (vertsXPart.containsKey(line.trim()))
					vertsXPart.get(line.trim()).add(vertId+"");
				else {
					Set<String> newPart = new TreeSet<>();
					newPart.add(vertId+"");
					vertsXPart.put(line.trim(), newPart);
				}
				vertId++;
			}
		}
		metisOuputReader.close();
		return vertsXPart;
	}
	
	protected Map<String, List<String>> getVAT(UndirectedGraph<String, DefaultEdge> workingGraph, List<UndirectedGraph<String, DefaultEdge>> group) {
		
		Map<String, List<String>> auxVAT = new TreeMap<>();
		Set<String> vertsInVAT = new TreeSet<>();
		SecureRandom random = new SecureRandom();   // Will be used to randomize all equally optimal decisions
		
		// All blocks in group must have the same number of vertices, otherwise dummy vertices need to be added
		// Adding dummies at the beginning so the rest of the implementation may assume all blocks have the same number of vertices
		
		boolean dummiesNeeded = false;
		int maxBlockSize = -1;
		for (UndirectedGraph<String, DefaultEdge> block : group) {
			if (maxBlockSize > -1 && block.vertexSet().size() != maxBlockSize)
				dummiesNeeded = true;
			if (block.vertexSet().size() > maxBlockSize) 
				maxBlockSize = block.vertexSet().size();
		}
		
		if (dummiesNeeded) {
			
			int dummyIndex = GraphUtil.maxVertexId(workingGraph) + 1; 
			
			for (int i = 0; i < group.size(); i++) {
				if (group.get(i).vertexSet().size() < maxBlockSize) {
					int dummyCount = maxBlockSize - group.get(i).vertexSet().size();
					for (int j = 0; j < dummyCount; j++) {
						String dummyName = "" + dummyIndex;
						dummyIndex++;
						group.get(i).addVertex(dummyName);
					}			
				}
			}
		}
		
		// Add necessary entries in VAT
		
		// Find vertices having the same degree in their blocks
		Set<Integer> groupWideExistingDegrees = new TreeSet<>();
		if (group.size() > 0) {
			for (String v : group.get(0).vertexSet())
				groupWideExistingDegrees.add(group.get(0).degreeOf(v));
			for (int i = 1; i < group.size(); i++) {
				Set<Integer> degreesInBlock = new TreeSet<>();
				for (String v : group.get(i).vertexSet())
					degreesInBlock.add(group.get(i).degreeOf(v));
				groupWideExistingDegrees.retainAll(degreesInBlock);
			}
		}
				
		List<String> newVATKeys = new ArrayList<>();
		
		// First row of VAT for this group
		if (groupWideExistingDegrees.size() > 0) {   // A common max degree exists
			
			List<String> newRowVAT = new ArrayList<>();
			int maxDeg = Collections.max(groupWideExistingDegrees);
			
			if (group.size() > 0) {
				
				// Find vertices of degree maxDeg in first block
				List<String> possibleRowKeys = new ArrayList<>();
				for (String v : group.get(0).vertexSet())
					if (group.get(0).degreeOf(v) == maxDeg)
						possibleRowKeys.add(v);
								
				// Initialize new row entry in VAT with one of the vertices found
				String rowKey = possibleRowKeys.get(random.nextInt(possibleRowKeys.size()));
				newRowVAT.add(rowKey);
				newVATKeys.add(rowKey);
				vertsInVAT.add(rowKey);
				
				// Find a vertex of degree maxDeg in every other block and add it to first row of VAT
				for (int i = 1; i < group.size(); i++) {
					List<String> possibleNewEntries = new ArrayList<>();
					for (String v : group.get(i).vertexSet())
						if (group.get(i).degreeOf(v) == maxDeg) 
							possibleNewEntries.add(v);
					String newEntry = possibleNewEntries.get(random.nextInt(possibleNewEntries.size()));
					newRowVAT.add(newEntry);
					vertsInVAT.add(newEntry);
				}
				auxVAT.put(rowKey, newRowVAT);
				
			}
		}
		else {   // No common max degree exists
			
			List<String> newRowVAT = new ArrayList<>();
			
			if (group.size() > 0) {
				
				// Find highest-degree vertices in first block
				List<String> possibleRowKeys = new ArrayList<>();
				int maxDeg = -1;
				for (String v : group.get(0).vertexSet())
					if (group.get(0).degreeOf(v) > maxDeg) {
						possibleRowKeys = new ArrayList<>();
						possibleRowKeys.add(v);
						maxDeg = group.get(0).degreeOf(v); 
					}
					else if (group.get(0).degreeOf(v) == maxDeg) 
						possibleRowKeys.add(v);
				
				// Initialize new row entry in VAT with one of the vertices found
				String rowKey = possibleRowKeys.get(random.nextInt(possibleRowKeys.size()));
				newRowVAT.add(rowKey);
				newVATKeys.add(rowKey);
				vertsInVAT.add(rowKey);
				
				// Find a highest-degree vertex in every other block and add it to first row of VAT
				for (int i = 1; i < group.size(); i++) {
					List<String> possibleNewEntries = new ArrayList<>();
					maxDeg = -1;
					for (String v : group.get(i).vertexSet())
						if (group.get(i).degreeOf(v) > maxDeg) {
							possibleNewEntries = new ArrayList<>();
							possibleNewEntries.add(v);
							maxDeg = group.get(i).degreeOf(v);
						}
						else if (group.get(i).degreeOf(v) == maxDeg)
							possibleNewEntries.add(v);
					String newEntry = possibleNewEntries.get(random.nextInt(possibleNewEntries.size()));
					newRowVAT.add(newEntry);
					vertsInVAT.add(newEntry);
				}
				auxVAT.put(rowKey, newRowVAT);
			}
		}
		
		// Remaining rows of VAT for this group
		
		boolean untabulatedVerticesFound = true;
		
		while (untabulatedVerticesFound) {
			
			untabulatedVerticesFound = false;
			
			List<String> newRowVAT = new ArrayList<>();
			
			if (group.size() > 0) {
				
				// Find highest-degree non-in-VAT vertices in first block
				List<String> possibleRowKeys = new ArrayList<>();
				int maxDeg = -1;
				for (String v : group.get(0).vertexSet())
					if (!vertsInVAT.contains(v)) {
						untabulatedVerticesFound = true;
						if (group.get(0).degreeOf(v) > maxDeg) {
							possibleRowKeys = new ArrayList<>();
							possibleRowKeys.add(v);
							maxDeg = group.get(0).degreeOf(v);	
						}
						else if (group.get(0).degreeOf(v) == maxDeg)
							possibleRowKeys.add(v);
					}
				
				if (untabulatedVerticesFound) {
					// Initialize new row entry in VAT with one of the vertices found
					String rowKey = possibleRowKeys.get(random.nextInt(possibleRowKeys.size()));
					newRowVAT.add(rowKey);
					newVATKeys.add(rowKey);
					vertsInVAT.add(rowKey);
					
					// Find a highest-degree non-in-VAT vertex in every other block and add it to current row of VAT
					for (int i = 1; i < group.size(); i++) {
						List<String> possibleNewEntries = new ArrayList<>();
						maxDeg = -1;
						for (String v : group.get(i).vertexSet())
							if (!vertsInVAT.contains(v)) {
								untabulatedVerticesFound = true;
								if (group.get(i).degreeOf(v) > maxDeg) {
									possibleNewEntries = new ArrayList<>();
									possibleNewEntries.add(v);
									maxDeg = group.get(i).degreeOf(v);
								}
								else if (group.get(i).degreeOf(v) == maxDeg)
									possibleNewEntries.add(v);
							}
						// Unless some mistake was made in the dummy vertex inclusion, there must always be a vertex available here
						String newEntry = possibleNewEntries.get(random.nextInt(possibleNewEntries.size()));
						newRowVAT.add(newEntry);
						vertsInVAT.add(newEntry);
					}
					auxVAT.put(rowKey, newRowVAT);
				}
			}
			
		}
		
		return auxVAT;
	}
	
	protected void alignBlocks(UndirectedGraph<String, DefaultEdge> fullGraph, Map<String, List<String>> groupVAT) {
		
		List<String> vatKeys = new ArrayList<>(groupVAT.keySet());
		
		// First, add any dummy vertex in the VAT to fullGraph  
		for (int i = 0; i < vatKeys.size(); i++)
			for (int j = 0; j < groupVAT.get(vatKeys.get(i)).size(); j++)
				if (!fullGraph.containsVertex(groupVAT.get(vatKeys.get(i)).get(j))) 
					fullGraph.addVertex(groupVAT.get(vatKeys.get(i)).get(j));
		
		// Then, perform the alignment directly on fullGraph
		for (int i = 0; i < vatKeys.size() - 1; i++)
			for (int j = i + 1; j < vatKeys.size(); j++) {
				// Check if the edge exists in some block and doesn't exist in some other
				boolean edgeFound = false, nonEdgeFound = false;
				for (int k = 0; (!edgeFound || !nonEdgeFound) && k < groupVAT.get(vatKeys.get(i)).size(); k++) {
					if (fullGraph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)))
						edgeFound = true; 
					if (!fullGraph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)))
						nonEdgeFound = true;
				}
				
				// Add edges to align blocks
				if (edgeFound && nonEdgeFound) 
					for (int k = 0; k < groupVAT.get(vatKeys.get(i)).size(); k++) 
						fullGraph.addEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k));
			}
		
	}
	
	protected void copyCrossingEdges(UndirectedGraph<String, DefaultEdge> graph) {
		// When this method is called, all dummy vertices have already been added by one or several calls of alignBlocks
		List<String> vatKeys = new ArrayList<>(globalVAT.keySet());
		for (int i = 0; i < vatKeys.size(); i++)
			for (int j = 0; j < globalVAT.get(vatKeys.get(i)).size() - 1; j++)
				for (int p = i; p < vatKeys.size(); p++)
					for (int q = j + 1; q < globalVAT.get(vatKeys.get(p)).size(); q++) 
						if (graph.containsEdge(globalVAT.get(vatKeys.get(i)).get(j), globalVAT.get(vatKeys.get(p)).get(q))) {
							int len = globalVAT.get(vatKeys.get(i)).size();
							for (int offset = 1; offset < len; offset++) 
								graph.addEdge(globalVAT.get(vatKeys.get(i)).get((j + offset) % len), globalVAT.get(vatKeys.get(p)).get((q + offset) % len));
						}
	}
	
	protected void randomlyUniformizeCrossingEdges(UndirectedGraph<String, DefaultEdge> graph) {
		// When this method is called, all dummy vertices have already been added by one or several calls of alignBlocks
		SecureRandom random = new SecureRandom();
		List<String> vatKeys = new ArrayList<>(globalVAT.keySet());
		for (int i = 0; i < vatKeys.size(); i++)
			for (int j = 0; j < globalVAT.get(vatKeys.get(i)).size() - 1; j++)
				for (int p = i; p < vatKeys.size(); p++)
					for (int q = j + 1; q < globalVAT.get(vatKeys.get(p)).size(); q++) 
						if (graph.containsEdge(globalVAT.get(vatKeys.get(i)).get(j), globalVAT.get(vatKeys.get(p)).get(q))) {
							int len = globalVAT.get(vatKeys.get(i)).size();
							int countExistingCopies = 1;   // Counting current occurrence as a copy 
							for (int offset = 1; offset < len; offset++) 
								if (graph.containsEdge(globalVAT.get(vatKeys.get(i)).get((j + offset) % len), globalVAT.get(vatKeys.get(p)).get((q + offset) % len)))
									countExistingCopies++;
							// Decide whether to perform an edge copy, with probability countExistingCopies/len, or a removal, with probability (len-countExistingCopies)/len
							boolean performCopy = (random.nextInt(len) < countExistingCopies);
							if (performCopy)   // Uniformize by copying
								for (int offset = 1; offset < len; offset++)
									graph.addEdge(globalVAT.get(vatKeys.get(i)).get((j + offset) % len), globalVAT.get(vatKeys.get(p)).get((q + offset) % len));
							else   // Uniformize by removing
								for (int offset = 0; offset < len; offset++)   // Starts at offset 0 to first remove the current edge
									graph.removeEdge(globalVAT.get(vatKeys.get(i)).get((j + offset) % len), globalVAT.get(vatKeys.get(p)).get((q + offset) % len));
						}
	}
	
	protected int groupCost(UndirectedGraph<String, DefaultEdge> workingGraph, Map<String, List<String>> groupVAT, boolean countCrossingEdges) {
		
		// First, simulate alignBlocks to account for the editions that it would perform 
		List<String> vatKeys = new ArrayList<>(groupVAT.keySet());
		int edgeAdditions = 0;
		for (int i = 0; i < vatKeys.size() - 1; i++)
			for (int j = i + 1; j < vatKeys.size(); j++) {
				// Check if the edge exists in some block and doesn't exist in some other
				boolean edgeFound = false, nonEdgeFound = false;
				for (int k = 0; (!edgeFound || !nonEdgeFound) && k < groupVAT.get(vatKeys.get(i)).size(); k++) {
					if (workingGraph.containsVertex(groupVAT.get(vatKeys.get(i)).get(k)) && workingGraph.containsVertex(groupVAT.get(vatKeys.get(j)).get(k)) 
						&& workingGraph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)))
						edgeFound = true;
					if (!workingGraph.containsVertex(groupVAT.get(vatKeys.get(i)).get(k)) || !workingGraph.containsVertex(groupVAT.get(vatKeys.get(j)).get(k)) 
						|| !workingGraph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)))
						nonEdgeFound = true;
				}
				if (edgeFound && nonEdgeFound) {
					for (int k = 0; k < groupVAT.get(vatKeys.get(i)).size(); k++)
						if (!workingGraph.containsVertex(groupVAT.get(vatKeys.get(i)).get(k))
							|| !workingGraph.containsVertex(groupVAT.get(vatKeys.get(j)).get(k))
							|| !workingGraph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)))
							edgeAdditions++;
				}
			}
		
		// Then, compute the contribution of crossing edge copy to the overall number of editions
		int costCrossingEdges = 0;
		
		if (countCrossingEdges) {
			int blockCount = groupVAT.get(groupVAT.keySet().iterator().next()).size();
			List<Set<String>> vertexSetsXBlock = new ArrayList<>();
			for (int i = 0; i < blockCount; i++) 
				vertexSetsXBlock.add(new TreeSet<String>());
			for (String rowKey : groupVAT.keySet())
				for (int i = 0; i < blockCount; i++) {
					String vCell = groupVAT.get(rowKey).get(i);
					vertexSetsXBlock.get(i).add(vCell);
				}
			int sumCrossEdges = 0;
			for (int i = 0; i < blockCount; i++) {
				for (String v : vertexSetsXBlock.get(i)) {
					Set<String> neighbours = new TreeSet<>(Graphs.neighborListOf(workingGraph, v));
					neighbours.removeAll(vertexSetsXBlock.get(i));
					sumCrossEdges += neighbours.size();
				}
			}
			costCrossingEdges = ((blockCount - 1) * sumCrossEdges) / 2;
		}
		
		return edgeAdditions + costCrossingEdges;
	}
	
	public static void main(String [] args) {
		
		timesExceptionOccurred = 0;
		
		for (int iter = 0; iter < 1000; iter++) {
			
			System.out.println("Iteration # " + iter);
			
			UndirectedGraph<String, DefaultEdge> graph = null; 
			if (args.length == 1 && args[0].equals("-facebook"))
				graph = new FacebookGraph(DefaultEdge.class);
			else if (args.length == 1 && args[0].equals("-panzarasa"))
				graph = new PanzarasaGraph(DefaultEdge.class);
			else if (args.length == 1 && args[0].equals("-urv"))			
				graph = new URVMailGraph(DefaultEdge.class);
			else if ((new SecureRandom()).nextBoolean())
				graph = BarabasiAlbertGraphGenerator.newGraph(200, 0, 50, 5, 3);
			else
				graph = WattsStrogatzGraphGenerator.newGraph(200, 0, 30, 0.75);
			
			ConnectivityInspector<String, DefaultEdge> connectivity = new ConnectivityInspector<>(graph);
			List<Set<String>> connComp = connectivity.connectedSets();
			
			Set<String> vertsMainComponent = null;
			int maximum = 0;
			for (Set<String> comp : connComp) {
				if (comp.size() > maximum) {
					maximum = comp.size();
					vertsMainComponent = new HashSet<String>(comp);
				}
			}
			
			if (vertsMainComponent.size() < graph.vertexSet().size())
				graph = GraphUtil.inducedSubgraph(graph, vertsMainComponent);
			
			connectivity = new ConnectivityInspector<>(graph);
			if (!connectivity.isGraphConnected()) 
				throw new RuntimeException();
			
			int origEdgeCount = graph.edgeSet().size(), origVertexCount = graph.vertexSet().size();
		
			// Apply the method with k \in {2,...,10}
			for (int k = 2; k <= 10; k++) {
				
				System.out.println("k = " + k);
				
				System.out.println("\tCopying crossing edges:");
				
				UndirectedGraph<String, DefaultEdge> cloneCopyingVersion = GraphUtil.cloneGraph(graph);
				KMatchAnonymizerUsingMETIS anonymizer = new KMatchAnonymizerUsingMETIS();
				anonymizer.anonymizeGraph(cloneCopyingVersion, k, false, "TesterCopying");
				
				// Report effect of anonymization on the graph
				System.out.println("\t\tOriginal vertex count: " + origVertexCount);
				System.out.println("\t\tFinal vertex count: " + cloneCopyingVersion.vertexSet().size());
				System.out.println("\t\tDelta: " + (cloneCopyingVersion.vertexSet().size() - origVertexCount) + " (" + ((double)(cloneCopyingVersion.vertexSet().size() - origVertexCount) * 100d / (double)origVertexCount) + "%)");
				System.out.println("\t\tOriginal edge count: " + origEdgeCount);
				System.out.println("\t\tFinal edge count: " + cloneCopyingVersion.edgeSet().size());
				System.out.println("\t\tDelta: " + (cloneCopyingVersion.edgeSet().size() - origEdgeCount) + " (" + ((double)(cloneCopyingVersion.edgeSet().size() - origEdgeCount) * 100d / (double)origEdgeCount) + "%)");
				
				System.out.println("\tRandomizing crossing edges:");
				
				UndirectedGraph<String, DefaultEdge> cloneRandomizedVersion = GraphUtil.cloneGraph(graph);
				anonymizer.anonymizeGraph(cloneRandomizedVersion, k, true, "TesterRandomized");
				
				// Report effect of anonymization on the graph
				System.out.println("\t\tOriginal vertex count: " + origVertexCount);
				System.out.println("\t\tFinal vertex count: " + cloneRandomizedVersion.vertexSet().size());
				System.out.println("\t\tDelta: " + (cloneRandomizedVersion.vertexSet().size() - origVertexCount) + " (" + ((double)(cloneRandomizedVersion.vertexSet().size() - origVertexCount) * 100d / (double)origVertexCount) + "%)");
				System.out.println("\t\tOriginal edge count: " + origEdgeCount);
				System.out.println("\t\tFinal edge count: " + cloneRandomizedVersion.edgeSet().size());
				System.out.println("\t\tDelta: " + (cloneRandomizedVersion.edgeSet().size() - origEdgeCount) + " (" + ((double)(cloneRandomizedVersion.edgeSet().size() - origEdgeCount) * 100d / (double)origEdgeCount) + "%)");
				connectivity = new ConnectivityInspector<>(cloneRandomizedVersion);
				if (connectivity.isGraphConnected())
					System.out.println("\t\tAnonymized graph is connected: YES");
				else
					System.out.println("\t\tAnonymized graph is connected: NO");
			}
			
			System.out.println("");
		}
		
		System.out.println(timesExceptionOccurred + " exceptions occurred");
	}
	
}
