package anonymization;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import util.GraphUtil;
import util.WrapperMETIS;

public class IncrementalKMatchSequenceAnonymizer extends KMatchAnonymizer {
	
	protected static int timesExceptionOccurred = 0;
	
	protected int commonK = 2;
	
	protected boolean firstSnapshotAnonymized = false;
	protected Set<String> pendingVertexAdditions;
	protected Map<String, Set<String>> pendingVertexRemovals;   // Vertices pending removal have their groups assigned  
	
	public IncrementalKMatchSequenceAnonymizer() {
		globalVAT = null;
		commonK = 2;
		firstSnapshotAnonymized = false;
		pendingVertexAdditions = new TreeSet<>();
		pendingVertexRemovals = new TreeMap<>();
	}
	
	public IncrementalKMatchSequenceAnonymizer(int k) {
		globalVAT = null;
		commonK = k;
		firstSnapshotAnonymized = false;
		pendingVertexAdditions = new TreeSet<>();
		pendingVertexRemovals = new TreeMap<>();
	}
	
	public void restart() {
		globalVAT = null;
		firstSnapshotAnonymized = false;
		pendingVertexAdditions = new TreeSet<>();
		pendingVertexRemovals = new TreeMap<>();
	}
	
	public void restart(int k) {
		commonK = k;
		restart();
	}

	@Override
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize, String uniqueIdFileName) {
		if (k == commonK) {
			if (firstSnapshotAnonymized)
				anonymizeNewSnapshot(graph, randomize, uniqueIdFileName);
			else
				anonymizeFirstSnapshot(graph, randomize, uniqueIdFileName);
		}
		else 
			throw new RuntimeException("Calling anonymization for k = " + k + ", " + commonK + " expected");
	}
	
	@Override
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize) {
		anonymizeGraph(graph, k, randomize, "DefaultNameServiceFileIncrementalKMatchAnonymizerUsingMETIS");
	}
	
	protected void anonymizeFirstSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomize, String uniqueIdFileName) {
		// The static version adds dummy vertices, the dynamic one will not
		
		SecureRandom random = new SecureRandom();
		
		if (commonK < graph.vertexSet().size()) {
			
			// If the number of vertices is not a multiple of commonK, leave graph.vertexSet().size() % commonK random vertices pending
			int pendingVertCount = graph.vertexSet().size() % commonK;
			if (pendingVertCount > 0) {
				List<String> vertList = new ArrayList<>(graph.vertexSet());
				for (int i = 0; i < pendingVertCount; i++) {
					int pvId = random.nextInt(vertList.size());
					pendingVertexAdditions.add(vertList.get(pvId));
					graph.removeVertex(vertList.get(pvId));
					vertList.remove(pvId);
				}
			}
			
			// Perform anonymization on trimmed graph
			globalVAT = new TreeMap<>();
			performAnonymizationFirstSnapshot(graph, randomize, uniqueIdFileName);
		}
		else {
			// Return a null graph, leave all current vertices pending 
			pendingVertexAdditions = new TreeSet<>(graph.vertexSet());
			graph.removeAllVertices(pendingVertexAdditions);
		}
		
		firstSnapshotAnonymized = true;
	}
	
	protected void anonymizeNewSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomize, String uniqueIdFileName) {
		if (firstSnapshotAnonymized) {
			
		}
		else 
			anonymizeFirstSnapshot(graph, randomize, uniqueIdFileName);
	}
	
	protected void performAnonymizationFirstSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomized, String uniqueIdFileName) {
		
		Map<String, Set<String>> partition = null;
		
		try {
			
			WrapperMETIS metisHandler = new WrapperMETIS();
			partition = metisHandler.getPartitionVertSets(graph, commonK, uniqueIdFileName, false);
			
		} catch (IOException | InterruptedException | RuntimeException e) {
			
			timesExceptionOccurred++;   // Just update, initialization and use up to the caller
			
			// Since some problem occurred in running METIS or handling its outputs, 
			// a naive partition is made by decrementally sorting vertices by degree 
			// and assigning each vertex to a partition using round-robin 			
			partition = new TreeMap<>();
			for (int i = 0; i < commonK; i++)
				partition.put((i + 1) + "", new TreeSet<String>());
			List<String> sortedVertList = GraphUtil.degreeSortedVertexList(graph, false);
			for (int i = 0; i < sortedVertList.size(); i++)
				partition.get((i % commonK) + "").add(sortedVertList.get(i));
		}
		
		// Perform the anonymization
		globalVAT = initializeVAT(graph, partition);
		alignBlocks(graph, globalVAT);
		if (randomized)
			randomlyUniformizeCrossingEdges(graph);
		else
			copyCrossingEdges(graph);   // Original approach by Zou et al.	
	}
	
	protected Map<String, List<String>> initializeVAT(UndirectedGraph<String, DefaultEdge> workingGraph, Map<String, Set<String>> partition) {
		
		Map<String, List<String>> auxVAT = new TreeMap<>();
		Set<String> vertsInVAT = new TreeSet<>();
		SecureRandom random = new SecureRandom();   // Will be used to randomize all equally optimal decisions
		
		// All elements of the partition must have the same number of vertices
		// Some reallocations may be needed
		
		boolean balancingNeeded = false;
		List<String> blocksMissingVertices = new ArrayList<>();
		List<String> blocksExcessVertices = new ArrayList<>();
		for (String partId : partition.keySet()) {
			if (partition.get(partId).size() < workingGraph.vertexSet().size() / commonK) {
				blocksMissingVertices.add(partId);
				balancingNeeded = true;
			}
			else if (partition.get(partId).size() > workingGraph.vertexSet().size() / commonK) {
				blocksExcessVertices.add(partId);
				balancingNeeded = true;
			}
		}
		
		while (balancingNeeded) {
			
			int indRandBlockExcessVerts = random.nextInt(blocksExcessVertices.size());
			String randBlockExcessVerts = blocksExcessVertices.get(indRandBlockExcessVerts); 
			List<String> exVerts = new ArrayList<>(partition.get(randBlockExcessVerts));
			String vertToSwitch = exVerts.get(random.nextInt(exVerts.size()));
			partition.get(randBlockExcessVerts).remove(vertToSwitch);
			if (partition.get(randBlockExcessVerts).size() == workingGraph.vertexSet().size() / commonK) {
				blocksExcessVertices.remove(indRandBlockExcessVerts);
				if (blocksExcessVertices.size() == 0)   // By construction, when blocksExcessVertices becomes empty so does blocksMissingVertices 
					balancingNeeded = false;
			}
			
			int indRandBlockMissingVerts = random.nextInt(blocksMissingVertices.size());
			String randBlockMissingVerts = blocksMissingVertices.get(indRandBlockMissingVerts);
			partition.get(randBlockMissingVerts).add(vertToSwitch);
			if (partition.get(randBlockMissingVerts).size() == workingGraph.vertexSet().size() / commonK) {
				blocksMissingVertices.remove(indRandBlockMissingVerts);
			}
		}
		
		// First approach, rebuild blocks-as-subgraphs structure used in KMatchAnonymizerUsingMETIS and keep the rest of the implementation as it was 
		List<UndirectedGraph<String, DefaultEdge>> group = new ArrayList<>();
		for (String pid : partition.keySet())
			group.add(GraphUtil.inducedSubgraph(workingGraph, partition.get(pid)));
		
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
}
