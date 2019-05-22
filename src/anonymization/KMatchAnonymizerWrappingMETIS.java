package anonymization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;
import util.GraphUtil;

/***
 * 
 * This class implements an alternative K-Match, which follows the description in Zou et al.'s paper, 
 * except that it uses METIS for graph partitioning
 *
 */

public class KMatchAnonymizerWrappingMETIS {
	
protected static Map<String, List<String>> globalVAT;
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k) {
		globalVAT = new TreeMap<>();
		partitionAlignAndAnonymize(graph, k);
	}

	protected static void partitionAlignAndAnonymize(UndirectedGraph<String, DefaultEdge> graph, int k) {
		
		try {
			
			// Generate METIS input
			if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
				GraphUtil.generateMetisInput(graph, "C:\\cygwin64\\home\\yunior.ramirez\\metis-5.1.0\\graphs\\workingGraph.txt", false);
			else
				GraphUtil.generateMetisInput(graph, "/home/yunior.ramirez/metis-5.1.0/graphs/workingGraph.txt", false);
			
			// Run METIS
			Runtime rt = Runtime.getRuntime();
			String command = null;
			if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
				command = "C:\\cygwin64\\usr\\local\\bin\\gpmetis C:\\cygwin64\\home\\yunior.ramirez\\metis-5.1.0\\graphs\\workingGraph.txt " + k;
			else
				command = "/usr/local/bin/gpmetis /home/yunior.ramirez/metis-5.1.0/graphs/workingGraph.txt " + k;
			Process proc = rt.exec(command);
			BufferedReader runTimeReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			runTimeReader.close();
			proc.waitFor();
			
			// Load METIS output
			BufferedReader metisOuputReader = new BufferedReader(new FileReader(new File("urv-email/urv-email.txt")));
			Map<String, Set<String>> vertsXPart = new TreeMap<>();
			int vertId = 1;   // generateMetisInput forced the ids to start at 1, because this is what is expected by METIS
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
			
			Set<UndirectedGraph<String, DefaultEdge>> partitions = new TreeSet<>();
			for (String pid : vertsXPart.keySet())
				partitions.add(GraphUtil.inducedSubgraph(graph, vertsXPart.get(pid)));
			
			// Perform the anonymization
			getVAT(graph, partitions);
			alignBlocks(graph, globalVAT);
			copyCrossingEdges(graph);
			
		} catch (IOException e) {
			// Do nothing, i.e. make no modifications to the graph
		} catch (InterruptedException e) {
			// Do nothing, i.e. make no modifications to the graph
		}
	}
	
	protected static void getVAT(UndirectedGraph<String, DefaultEdge> workingGraph, Set<UndirectedGraph<String, DefaultEdge>> group) {
		
		globalVAT = new TreeMap<>();
		Set<String> vertsInVAT = new TreeSet<>();
		
		// All blocks in group must have the same number of vertices, otherwise dummy vertices need to be added
		// Adding dummies at the beginning so the rest of the implementation may assume all blocks have the same number of vertices
		
		boolean dummiesNeeded = false;
		int maxBlockSize = -1;
		for (UndirectedGraph<String, DefaultEdge> block : group)
			if (block.vertexSet().size() > maxBlockSize) {
				if (maxBlockSize > -1)
					dummiesNeeded = true;
				maxBlockSize = block.vertexSet().size();
			}
		
		int dummyIndex = workingGraph.vertexSet().size();
		while (dummiesNeeded) {
			dummiesNeeded = false;
			for (UndirectedGraph<String, DefaultEdge> block : group)   // Breaking this cycle every time dummies are added because the dummy addition operation modifies the iterator
				if (block.vertexSet().size() < maxBlockSize) {
					dummiesNeeded = true;
					int dummyCount = maxBlockSize - block.vertexSet().size();
					for (int dm = 0; dm < dummyCount; dm++) {
						String dummyName = "" + dummyIndex;
						dummyIndex++;
						block.addVertex(dummyName);
					}
					break;   // Breaking this cycle every time dummies are added because the dummy addition operation modifies the iterator
				}
		}
		
		// Add necessary entries in VAT
		
		// Find vertices having the same degree in their blocks
		Set<Integer> groupWideExistingDegrees = new TreeSet<>();
		Iterator<UndirectedGraph<String, DefaultEdge>> groupIter = group.iterator();
		if (groupIter.hasNext()) {
			UndirectedGraph<String, DefaultEdge> block = groupIter.next();
			for (String v : block.vertexSet())
				groupWideExistingDegrees.add(block.degreeOf(v));
			while (groupIter.hasNext()) {
				block = groupIter.next();
				Set<Integer> degreesInBlock = new TreeSet<>();
				for (String v : block.vertexSet())
					degreesInBlock.add(block.degreeOf(v));
				groupWideExistingDegrees.retainAll(degreesInBlock);
			}		
		}
		
		List<String> newVATKeys = new ArrayList<>();
		
		// First row of VAT
		if (groupWideExistingDegrees.size() > 0) {
			List<String> newRowVAT = new ArrayList<>();
			int maxDeg = Collections.max(groupWideExistingDegrees);
			groupIter = group.iterator();
			if (groupIter.hasNext()) {
				// Find a vertex of degree maxDeg in first block
				UndirectedGraph<String, DefaultEdge> block = groupIter.next();
				String rowKey = null;
				for (String v : block.vertexSet())
					if (block.degreeOf(v) == maxDeg) {
						rowKey = v;
						break;
					}
				// Start VAT with vertex found
				newRowVAT.add(rowKey);
				newVATKeys.add(rowKey);
				vertsInVAT.add(rowKey);
				// Find a vertex of degree maxDeg in every other block and link to create first row of VAT
				while (groupIter.hasNext()) {
					block = groupIter.next();
					String newEntry = null;
					for (String v : block.vertexSet())
						if (block.degreeOf(v) == maxDeg) {
							newEntry = v;
							break;
						}
					newRowVAT.add(newEntry);
					vertsInVAT.add(newEntry);
				}
				globalVAT.put(rowKey, newRowVAT);
			}
		}
		else {
			List<String> newRowVAT = new ArrayList<>();
			groupIter = group.iterator();
			if (groupIter.hasNext()) {
				// Find highest degree vertex in first block
				UndirectedGraph<String, DefaultEdge> block = groupIter.next();
				String rowKey = null;
				int maxDeg = -1;
				for (String v : block.vertexSet())
					if (block.degreeOf(v) > maxDeg) {
						rowKey = v;
						maxDeg = block.degreeOf(v); 
					}
				// Start VAT with vertex found
				newRowVAT.add(rowKey);
				newVATKeys.add(rowKey);
				vertsInVAT.add(rowKey);
				// Find highest degree vertices in every other block and link to create first row of VAT
				while (groupIter.hasNext()) {
					block = groupIter.next();
					String newEntry = null;
					maxDeg = -1;
					for (String v : block.vertexSet())
						if (block.degreeOf(v) > maxDeg) {
							newEntry = v;
							maxDeg = block.degreeOf(v);
						}
					newRowVAT.add(newEntry);
					vertsInVAT.add(newEntry);
				}
				globalVAT.put(rowKey, newRowVAT);
			}
		}
		
		// Next rows of VAT
		
		boolean foundUntabulatedVertices = true;
		
		while (foundUntabulatedVertices) {
			
			foundUntabulatedVertices = false;
			
			List<String> newRowVAT = new ArrayList<>();
			groupIter = group.iterator();
			if (groupIter.hasNext()) {
				// Find highest degree non-in-VAT vertex in first block
				UndirectedGraph<String, DefaultEdge> block = groupIter.next();
				String rowKey = null;
				int maxDeg = -1;
				for (String v : block.vertexSet())
					if (!vertsInVAT.contains(v)) {
						foundUntabulatedVertices = true;
						if (block.degreeOf(v) > maxDeg) {
							rowKey = v;
							maxDeg = block.degreeOf(v);	
						}	 
					}
				// Start VAT with vertex found
				newRowVAT.add(rowKey);
				newVATKeys.add(rowKey);
				vertsInVAT.add(rowKey);
				// Find highest degree non-in-VAT vertices in every other block and link to create next row of VAT
				while (groupIter.hasNext()) {
					block = groupIter.next();
					String newEntry = null;
					maxDeg = -1;
					for (String v : block.vertexSet())
						if (!vertsInVAT.contains(v)) {
							foundUntabulatedVertices = true;
							if (block.degreeOf(v) > maxDeg) {
								newEntry = v;
								maxDeg = block.degreeOf(v);
							}
						}
					newRowVAT.add(newEntry);
					vertsInVAT.add(newEntry);
				}
				globalVAT.put(rowKey, newRowVAT);
			}
			
		}
		
	}
	
	protected static void alignBlocks(UndirectedGraph<String, DefaultEdge> fullGraph, Map<String, List<String>> groupVAT) {
				
		List<String> vatKeys = new ArrayList<>(groupVAT.keySet());
		
		for (int i = 0; i < vatKeys.size() - 1; i++)
			for (int j = i + 1; j < vatKeys.size(); j++) {
				// Check if the edge exists in some block
				boolean edgeMustBeCopied = false;
				for (int k = 0; !edgeMustBeCopied && k < groupVAT.get(vatKeys.get(i)).size(); k++)
					edgeMustBeCopied = edgeMustBeCopied 
						|| (fullGraph.containsVertex(groupVAT.get(vatKeys.get(i)).get(k)) 
							&& fullGraph.containsVertex(groupVAT.get(vatKeys.get(j)).get(k)) 
							&& fullGraph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)));
				
				if (edgeMustBeCopied) {
					for (int k = 0; k < groupVAT.get(vatKeys.get(i)).size(); k++) {
						if (!fullGraph.containsVertex(groupVAT.get(vatKeys.get(i)).get(k)))
							fullGraph.addVertex(groupVAT.get(vatKeys.get(i)).get(k));
						if (!fullGraph.containsVertex(groupVAT.get(vatKeys.get(j)).get(k)))
							fullGraph.addVertex(groupVAT.get(vatKeys.get(j)).get(k));
						fullGraph.addEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k));
					}
				}
			}
					
	}
	
	protected static void copyCrossingEdges(UndirectedGraph<String, DefaultEdge> graph) {
		List<String> vatKeys = new ArrayList<>(globalVAT.keySet());
		for (int i = 0; i < vatKeys.size() - 1; i++)
			for (int j = 0; j < globalVAT.get(vatKeys.get(i)).size() - 1; j++)
				for (int p = i; p < vatKeys.size(); p++)
					for (int q = j + 1; q < globalVAT.get(vatKeys.get(p)).size(); q++) 
						if (graph.containsEdge(globalVAT.get(vatKeys.get(i)).get(j), globalVAT.get(vatKeys.get(p)).get(q))) {
							int len = globalVAT.get(vatKeys.get(i)).size();
							for (int k = 1; k < len; k++)
								graph.addEdge(globalVAT.get(vatKeys.get(i)).get((j + k) % len), globalVAT.get(vatKeys.get(p)).get((q + k) % len));
						}
	}
	
	protected static int groupCost(UndirectedGraph<String, DefaultEdge> workingGraph, Map<String, List<String>> groupVAT) {
		
		List<String> vatKeys = new ArrayList<>(groupVAT.keySet());
		
		List<Integer> editDistances = new ArrayList<>();
		int blockCount = groupVAT.get(groupVAT.keySet().iterator().next()).size();
		for (int i = 0; i < blockCount; i++)
			editDistances.add(0);
		
		for (int i = 0; i < vatKeys.size() - 1; i++)
			for (int j = i + 1; j < vatKeys.size(); j++) {
				// Check if the edge exists in some block
				boolean edgeMustBeCopied = false;
				for (int k = 0; !edgeMustBeCopied && k < groupVAT.get(vatKeys.get(i)).size(); k++)
					edgeMustBeCopied = edgeMustBeCopied 
										|| (workingGraph.containsVertex(groupVAT.get(vatKeys.get(i)).get(k)) 
											&& workingGraph.containsVertex(groupVAT.get(vatKeys.get(j)).get(k)) 
											&& workingGraph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)));
				if (edgeMustBeCopied) {
					for (int k = 0; k < groupVAT.get(vatKeys.get(i)).size(); k++)
						if (!workingGraph.containsVertex(groupVAT.get(vatKeys.get(i)).get(k))
							|| !workingGraph.containsVertex(groupVAT.get(vatKeys.get(j)).get(k))
							|| !workingGraph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)))
							editDistances.set(k, editDistances.get(k) + 1);
				}
			}
		
		return Collections.min(editDistances);
	}
	
	public static void main(String [] args) {
		
		UndirectedGraph<String, DefaultEdge> graph = null; 
		if (args.length == 1 && args[0].equals("-facebook"))
			graph = new FacebookGraph(DefaultEdge.class);
		else if (args.length == 1 && args[0].equals("-panzarasa"))
			graph = new PanzarasaGraph(DefaultEdge.class);
		else
			graph = new URVMailGraph(DefaultEdge.class);
		
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
		
		graph = GraphUtil.shiftAndShuffleVertexIds(graph, 0, vertsMainComponent);
		
		connectivity = new ConnectivityInspector<>(graph);
		if (!connectivity.isGraphConnected()) 
			throw new RuntimeException();
		
		int origEdgeCount = graph.edgeSet().size();
		
		// Apply the method with k=2
		anonymizeGraph(graph, 2);
		
		// Report effect of anonymization on the graph
		System.out.println("Number of edge modifications: " + Math.abs(graph.edgeSet().size() - origEdgeCount));
		
	}
	
}
