package anonymization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import util.FrequentSubgraphMinerSingleLargeGraph;
import util.WrapperGraMiFSM;
import util.TrivialFrequentSubgraphMinerSingleLargeGraph;
import util.GraphUtil;

/***
 * 
 * This class implements the method K-Match following the description in Zou et al.'s paper to the best possible extent.
 * The paper does not specify what algorithm to use for finding frequent subgraphs.
 * For convenience, we will wrap and use the existing GraMi searcher 
 *
 */

public class KMatchAnonymizerUsingGraMi {
	
	protected static Map<String, List<String>> globalVAT;
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k) {
		globalVAT = new TreeMap<>();
		partitionAlignAndAnonymize(graph, k);
	}

	protected static void partitionAlignAndAnonymize(UndirectedGraph<String, DefaultEdge> graph, int k) {
		
		UndirectedGraph<String, DefaultEdge> workingGraph = GraphUtil.cloneGraph(graph); 
		
		FrequentSubgraphMinerSingleLargeGraph subgraphMiner = new WrapperGraMiFSM();   
		
		List<List<UndirectedGraph<String, DefaultEdge>>> allGroups = new ArrayList<>();
		
		while (workingGraph.edgeSet().size() >= 0) {
			
			List<List<UndirectedGraph<String, DefaultEdge>>> frequentSubgraphs = subgraphMiner.frequentSubgraphs(workingGraph, k);
			
			int maxEdgeCount = -1;
			List<UndirectedGraph<String, DefaultEdge>> maxEdgeCountSubgraphs = null;
			for (List<UndirectedGraph<String, DefaultEdge>> fgs : frequentSubgraphs) {
				UndirectedGraph<String, DefaultEdge> frGr = fgs.iterator().next();   // Only necessary to take first element, as all of them are isomorphic
				if (frGr.edgeSet().size() > maxEdgeCount) {
					maxEdgeCount = frGr.edgeSet().size();
					maxEdgeCountSubgraphs = fgs;
				}
			}
			
			List<UndirectedGraph<String, DefaultEdge>> group = maxEdgeCountSubgraphs;
			List<UndirectedGraph<String, DefaultEdge>> extendedGroup = new ArrayList<>();
			Map<String, List<String>> vatGroup = getGroupVAT(workingGraph, group);
			Map<String, List<String>> vatExtendedGroup = new TreeMap<>();
			
			do {
				
				for (UndirectedGraph<String, DefaultEdge> block : group) {
					Set<String> vertsExtendedBlock = block.vertexSet();
					for (String v : block.vertexSet())
						vertsExtendedBlock.addAll(Graphs.neighborListOf(workingGraph, v));
					UndirectedGraph<String, DefaultEdge> extendedBlock = GraphUtil.inducedSubgraph(workingGraph, vertsExtendedBlock);
					extendedGroup.add(extendedBlock);
				}
				
				vatExtendedGroup = getGroupVAT(workingGraph, extendedGroup);
				
				if (groupCost(workingGraph, vatExtendedGroup, true) <= groupCost(workingGraph, vatGroup, true)) {
					group = extendedGroup;
					vatGroup = vatExtendedGroup;
					extendedGroup = new ArrayList<>();
				}
				else break;
				
			} while (true);
			
			// Align blocks in group
			allGroups.add(group);
			globalVAT.putAll(vatGroup);
			alignBlocks(graph, vatGroup);
			
			// Update workingGraph 
			
			Set<String> remainingVerts = new TreeSet<>(workingGraph.vertexSet());
			for (UndirectedGraph<String, DefaultEdge> block : group)
				remainingVerts.removeAll(block.vertexSet());
			workingGraph = GraphUtil.inducedSubgraph(graph, remainingVerts);
			
		}
		
		copyCrossingEdges(graph);
	}
	
	protected static Map<String, List<String>> getGroupVAT(UndirectedGraph<String, DefaultEdge> workingGraph, List<UndirectedGraph<String, DefaultEdge>> group) {
		
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
			
			int dummyIndex = -1;
			for (String v : workingGraph.vertexSet()) 
				if (Integer.parseInt(v) > dummyIndex)
					dummyIndex = Integer.parseInt(v); 
			dummyIndex++;
			
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
	
	protected static void alignBlocks(UndirectedGraph<String, DefaultEdge> fullGraph, Map<String, List<String>> groupVAT) {
				
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
	
	protected static void copyCrossingEdges(UndirectedGraph<String, DefaultEdge> graph) {
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
	
	protected static int groupCost(UndirectedGraph<String, DefaultEdge> workingGraph, Map<String, List<String>> groupVAT, boolean countCrossingEdges) {
		
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
