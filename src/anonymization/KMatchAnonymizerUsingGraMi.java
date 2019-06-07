package anonymization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import net.vivin.GenericTreeNode;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;
import util.BarabasiAlbertGraphGenerator;
import util.FrequentSubgraphMinerSingleLargeGraph;
import util.GraphUtil;
import util.WrapperGraMiFSM;

/***
 * 
 * This class implements the method K-Match following the description in Zou et al.'s paper to the best possible extent.
 * The paper does not specify what algorithm to use for finding frequent subgraphs.
 * For convenience, we will wrap and use the existing GraMi searcher 
 *
 */

public class KMatchAnonymizerUsingGraMi {
	
	protected static Map<String, List<String>> globalVAT;
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize) {
		if (k < graph.vertexSet().size()) {
			globalVAT = new TreeMap<>();
			performAnonymization(graph, k, randomize);
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

	protected static void performAnonymization(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize) {
		
		UndirectedGraph<String, DefaultEdge> workingGraph = GraphUtil.cloneGraph(graph); 
		
		FrequentSubgraphMinerSingleLargeGraph subgraphMiner = new WrapperGraMiFSM();   
		
		List<List<UndirectedGraph<String, DefaultEdge>>> allGroups = new ArrayList<>();
		
		while (workingGraph.edgeSet().size() >= 0) {
			
			UndirectedGraph<String, DefaultEdge> freqSubgrTemplate = subgraphMiner.frequentSubgraphMaxEdgeCount(workingGraph, k);
			List<UndirectedGraph<String, DefaultEdge>> group = retrieveGroup(workingGraph, freqSubgrTemplate, k);
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
		
		if (randomize)
			randomlyUniformizeCrossingEdges(graph);
		else
			copyCrossingEdges(graph);   // Original approach by Zou et al.
	}
	
	protected static List<UndirectedGraph<String, DefaultEdge>> retrieveGroup(UndirectedGraph<String, DefaultEdge> workingGraph, UndirectedGraph<String, DefaultEdge> freqSubgrTemplate, int k) {
		
		SecureRandom random = new SecureRandom();   // Will be used to randomize all equally optimal decisions

		List<String[]> vertListsMatchingSubgraphs = getAllMatchingSubgraphs(workingGraph, freqSubgrTemplate);
		List<Set<String>> vertSetsMatchingSubgraphs = new ArrayList<>();
		for (int i = 0; i < vertListsMatchingSubgraphs.size(); i++) {
			Set<String> vset = new TreeSet<>();
			for (int j = 0; j < vertListsMatchingSubgraphs.get(i).length; j++)
				vset.add(vertListsMatchingSubgraphs.get(i)[j]);
			vertSetsMatchingSubgraphs.add(vset);
		}
		
		List<UndirectedGraph<String, DefaultEdge>> group = new ArrayList<>();
		
		while (vertSetsMatchingSubgraphs.size() > k) {
			// Remove one of the non-previously removed subgraphs having most vertices in common with the rest
			int maxCommonVertCount = -1;
			List<Integer> indsMaxCommonVertCount = null;
			for (int i = 0; i < vertSetsMatchingSubgraphs.size(); i++) {
				int inCommon = 0;
				for (int j = 0; j < vertSetsMatchingSubgraphs.size(); j++)
					if (j != i) {
						Set<String> vertsInCommon = new TreeSet<>(vertSetsMatchingSubgraphs.get(i));
						vertsInCommon.retainAll(vertSetsMatchingSubgraphs.get(j));
						inCommon += vertsInCommon.size();
					}
				if (inCommon > maxCommonVertCount) {
					maxCommonVertCount = inCommon;
					indsMaxCommonVertCount = new ArrayList<>();
					indsMaxCommonVertCount.add(i);
				}
				else if (inCommon == maxCommonVertCount)
					indsMaxCommonVertCount.add(i);
			}
			vertSetsMatchingSubgraphs.remove(indsMaxCommonVertCount.get(random.nextInt(indsMaxCommonVertCount.size())).intValue());
		}
		
		for (int i = 0; i < vertSetsMatchingSubgraphs.size(); i++)		
			group.add(GraphUtil.inducedSubgraph(workingGraph, vertSetsMatchingSubgraphs.get(i)));
		
		return null;
	}
	
	// Code adapted from Trujillo's implementation of sybil subgraph retrieval used in the original walk-based attack
	protected static List<String[]> getAllMatchingSubgraphs(UndirectedGraph<String, DefaultEdge> workingGraph, UndirectedGraph<String, DefaultEdge> freqSubgrTemplate) {
		
		Set<String> tempVertSet = new TreeSet<>(freqSubgrTemplate.vertexSet());
		
		// Get vertex list decrementally sorted by degree in freqSubgrTemplate, aiming to reduce the search space for BFS
		List<String> sortedTemplVertList = new ArrayList<>();
		while (tempVertSet.size() > 1) {
			int maxDeg = -1;
			String vertMaxDeg = "";
			for (String v : tempVertSet)
				if (freqSubgrTemplate.degreeOf(v) > maxDeg) {
					maxDeg = freqSubgrTemplate.degreeOf(v);
					vertMaxDeg = v;
				}
			sortedTemplVertList.add(vertMaxDeg);
			tempVertSet.remove(vertMaxDeg);
		}
		// Add last remaining element, if any
		if (tempVertSet.size() == 1)
			sortedTemplVertList.add(tempVertSet.iterator().next());
		
		// Trujillo's BFS
		GenericTreeNode<String> root = new GenericTreeNode<>("root");
		List<GenericTreeNode<String>> currentLevel = new LinkedList<>();
		List<GenericTreeNode<String>> nextLevel = new LinkedList<>();			
		for (int i = 0; i < sortedTemplVertList.size(); i++) {
			nextLevel = new LinkedList<>();
			for (String vertex : workingGraph.vertexSet()) {
				//int degree = graph.degreeOf(vertex);
				//if (degree == fingerprintDegrees[i]) {
				if (workingGraph.degreeOf(vertex) >= freqSubgrTemplate.degreeOf(sortedTemplVertList.get(i))) {   // In the original method, it was possible to check exact degree, here this is the best we can do
					if (i == 0) {
						GenericTreeNode<String> newChild = new GenericTreeNode<>(vertex);
						root.addChild(newChild);
						nextLevel.add(newChild);
					}
					else {
						for (GenericTreeNode<String> lastVertex : currentLevel) {
							boolean ok = true;
							GenericTreeNode<String> tmp = lastVertex;
							int pos = i - 1;
							while (!tmp.equals(root)) {
								if (tmp.getData().equals(vertex)) {
									ok = false;
									break;
								}
								//if (graph.containsEdge(vertex, tmp.getData()) && !fingerprintLinks[i][pos]) {
								if (workingGraph.containsEdge(vertex, tmp.getData()) && !freqSubgrTemplate.containsEdge(sortedTemplVertList.get(i) + "", sortedTemplVertList.get(pos) + "")) {
									ok = false;
									break;
								}
								//if (!graph.containsEdge(vertex, tmp.getData()) && fingerprintLinks[i][pos]) {
								if (!workingGraph.containsEdge(vertex, tmp.getData()) && freqSubgrTemplate.containsEdge(sortedTemplVertList.get(i) + "", sortedTemplVertList.get(pos) + "")) {
									ok = false;
									break;
								}
								pos--;
								tmp = tmp.getParent();
							}
							if (ok) {
								tmp = new GenericTreeNode<>(vertex);
								lastVertex.addChild(tmp);
								nextLevel.add(tmp);
							}
						}
					}
				}	
			}
			currentLevel = nextLevel;
		}
		
		return buildListOfCandidates(root, workingGraph, sortedTemplVertList.size(), sortedTemplVertList.size());
	}
	
	// Code adapted from Trujillo's implementation of sybil subgraph retrieval used in the original walk-based attack
	protected static List<String[]> buildListOfCandidates(GenericTreeNode<String> root, UndirectedGraph<String, DefaultEdge> graph, int pos, int size) {
		List<String[]> result = new LinkedList<>();
		if (pos < 0) 
			throw new RuntimeException();
		if (root.isALeaf()) {
			if (pos > 0) 
				return result;
			String[] candidates = new String[size];
			candidates[size - pos - 1] = root.getData();
			result.add(candidates);
			return result;
		}
		for (GenericTreeNode<String> child : root.getChildren()){
			List<String[]> subcandidates = buildListOfCandidates(child, graph, pos-1, size);
			if (!root.isRoot()) {
				for (String[] subcandidate : subcandidates) {
					subcandidate[size-pos-1] = root.getData();
				}
			}
			result.addAll(subcandidates);
		}
		return result;
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
	
	protected static void randomlyUniformizeCrossingEdges(UndirectedGraph<String, DefaultEdge> graph) {
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
		else if (args.length == 1 && args[0].equals("-urv"))
			graph = new URVMailGraph(DefaultEdge.class);
		else
			graph = BarabasiAlbertGraphGenerator.newGraph(50, 3, 10, 10, 1);
		
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
		
		int origEdgeCount = graph.edgeSet().size(), origVertexCount = graph.vertexSet().size();
		
		// Apply the method with k \in {2,...,10}
		for (int k = 2; k <= 10; k++) {
			
			UndirectedGraph<String, DefaultEdge> clone = GraphUtil.cloneGraph(graph);
			anonymizeGraph(clone, 2, false);
			
			// Report effect of anonymization on the graph
			System.out.println("k = " + k);
			System.out.println("\tOriginal vertex count: " + origVertexCount);
			System.out.println("\tFinal vertex count: " + clone.vertexSet().size());
			System.out.println("\tDelta: " + (clone.vertexSet().size() - origVertexCount) + " (" + ((double)(clone.vertexSet().size() - origVertexCount) * 100d / (double)origVertexCount) + "%)");
			System.out.println("\tOriginal edge count: " + origEdgeCount);
			System.out.println("\tFinal edge count: " + clone.edgeSet().size());
			System.out.println("\tDelta: " + (clone.edgeSet().size() - origEdgeCount) + " (" + ((double)(clone.edgeSet().size() - origEdgeCount) * 100d / (double)origEdgeCount) + "%)");
		}
	}

}
