package anonymization;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import util.BarabasiAlbertGraphGenerator;
import util.GraphUtil;
import util.WattsStrogatzGraphGenerator;
import util.WrapperMETIS;

/***
 * 
 * This class implements an alternative K-Match, which follows the description in Zou et al.'s paper, 
 * except that it uses METIS for graph partitioning
 *
 */

public class KMatchAnonymizerUsingMETIS extends KMatchAnonymizer {
	
	protected static int timesExceptionOccurred = 0;
	
	public KMatchAnonymizerUsingMETIS() {
		globalVAT = null;
	}
	
	@Override
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize, String uniqueIdFileName) {
		if (k < graph.vertexSet().size()) {
			globalVAT = new TreeMap<>();   
			performAnonymization(graph, k, randomize, uniqueIdFileName);
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
	
	@Override
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize) {
		anonymizeGraph(graph, k, randomize, "DefaultNameServiceFileKMatchAnonymizerUsingMETIS");
	}

	protected void performAnonymization(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize, String uniqueIdFileName) {
		
		List<UndirectedGraph<String, DefaultEdge>> partition = null;
		
		try {
			
			WrapperMETIS metisHandler = new WrapperMETIS();
			partition = metisHandler.getPartitionSubgraphs(graph, k, uniqueIdFileName, false);
			
		} catch (IOException | InterruptedException | RuntimeException e) {
			
			timesExceptionOccurred++;   // Just update, initialization and use up to the caller
			
			// Since some problem occurred in running METIS or handling its outputs, 
			// a naive partition is made by decrementally sorting vertices by degree 
			// and assigning each vertex to a partition using round-robin 
			List<Set<String>> vertsXPart = new ArrayList<>();
			for (int i = 0; i < k; i++)
				vertsXPart.add(new TreeSet<String>());
			List<String> sortedVertList = GraphUtil.degreeSortedVertexList(graph, null, false);
			for (int i = 0; i < sortedVertList.size(); i++)
				vertsXPart.get(i % k).add(sortedVertList.get(i));
			partition = new ArrayList<>();
			for (int i = 0; i < k; i++)
				partition.add(GraphUtil.inducedSubgraph(graph, vertsXPart.get(i)));
		}
		
		// Perform the anonymization
		globalVAT = getVAT(graph, partition);
		alignBlocks(graph, globalVAT);
		if (randomize)
			randomlyUniformizeCrossingEdges(graph);
		else
			copyCrossingEdges(graph);   // Original approach by Zou et al.	
	}
	
	protected Map<String, List<String>> getVAT(UndirectedGraph<String, DefaultEdge> graph, List<UndirectedGraph<String, DefaultEdge>> group) {
		
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
			
			int dummyIndex = GraphUtil.maxVertexId(graph) + 1; 
			
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
