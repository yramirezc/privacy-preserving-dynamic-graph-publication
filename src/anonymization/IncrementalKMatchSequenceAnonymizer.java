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

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import util.GraphUtil;
import util.WrapperMETIS;

public class IncrementalKMatchSequenceAnonymizer extends KMatchAnonymizer {
	
	/***
	 * Declarations
	 */
	
	protected static int timesExceptionOccurred = 0;
	
	protected int commonK = 2;
	
	protected boolean firstSnapshotAnonymized = false;
	protected Set<String> pendingVertexAdditions;
	
	/***
	 * Public interface
	 */
	
	public IncrementalKMatchSequenceAnonymizer() {
		globalVAT = null;
		commonK = 2;
		firstSnapshotAnonymized = false;
		pendingVertexAdditions = new TreeSet<>();
	}
	
	public IncrementalKMatchSequenceAnonymizer(int k) {
		globalVAT = null;
		commonK = k;
		firstSnapshotAnonymized = false;
		pendingVertexAdditions = new TreeSet<>();
	}
	
	public void restart() {
		globalVAT = null;
		firstSnapshotAnonymized = false;
		pendingVertexAdditions = new TreeSet<>();
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
	
	/***
	 * Methods for anonymizing the first snapshot (first ever or first after a call of restart())
	 */
	
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
			List<String> sortedVertList = GraphUtil.degreeSortedVertexList(graph, null, false);
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
	
	protected Map<String, List<String>> initializeVAT(UndirectedGraph<String, DefaultEdge> graph, Map<String, Set<String>> partition) {
		
		Map<String, List<String>> auxVAT = new TreeMap<>();
		Set<String> vertsInVAT = new TreeSet<>();
		SecureRandom random = new SecureRandom();   // Will be used to randomize all equally optimal decisions
		
		// All elements of the partition must have the same number of vertices
		// Some reallocations may be needed
		
		boolean balancingNeeded = false;
		List<String> blocksMissingVertices = new ArrayList<>();
		List<String> blocksExcessVertices = new ArrayList<>();
		for (String partId : partition.keySet()) {
			if (partition.get(partId).size() < graph.vertexSet().size() / commonK) {
				blocksMissingVertices.add(partId);
				balancingNeeded = true;
			}
			else if (partition.get(partId).size() > graph.vertexSet().size() / commonK) {
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
			if (partition.get(randBlockExcessVerts).size() == graph.vertexSet().size() / commonK) {
				blocksExcessVertices.remove(indRandBlockExcessVerts);
				if (blocksExcessVertices.size() == 0)   // By construction, when blocksExcessVertices becomes empty so does blocksMissingVertices 
					balancingNeeded = false;
			}
			
			int indRandBlockMissingVerts = random.nextInt(blocksMissingVertices.size());
			String randBlockMissingVerts = blocksMissingVertices.get(indRandBlockMissingVerts);
			partition.get(randBlockMissingVerts).add(vertToSwitch);
			if (partition.get(randBlockMissingVerts).size() == graph.vertexSet().size() / commonK) {
				blocksMissingVertices.remove(indRandBlockMissingVerts);
			}
		}
		
		// Rebuilding blocks-as-subgraphs structure used in KMatchAnonymizerUsingMETIS 
		// and keeping the rest of the implementation as it was
		
		List<UndirectedGraph<String, DefaultEdge>> group = new ArrayList<>();
		for (String pid : partition.keySet())
			group.add(GraphUtil.inducedSubgraph(graph, partition.get(pid)));
		
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
	
	/***
	 * Methods for incrementally anonymizing a non-first snapshot (not the first ever and not the first after a call of restart())
	 */
	
	protected void anonymizeNewSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomize, String uniqueIdFileName) {
		
		if (firstSnapshotAnonymized) {
			
			SecureRandom random = new SecureRandom();
			
			// Remove from pendingVertexAdditions vertices that no longer exist in this snapshot
			// These vertices will never be represented in the private sequence
			pendingVertexAdditions.retainAll(graph.vertexSet());
			
			// Remove VAT rows such that all of their elements no longer exist
			// Determine set of untabulated vertices
			
			Set<String> untabulatedVertices = new TreeSet<>(graph.vertexSet());
			Set<String> vatRowsToRemove = new TreeSet<>();
			
			for (String vatKey : globalVAT.keySet()) {
				
				boolean removeRow = true;
				for (String v : globalVAT.get(vatKey))
					if (graph.containsVertex(v)) {
						removeRow = false;
						break;
					}
				
				if (removeRow)
					vatRowsToRemove.add(vatKey);
				else {
					
					untabulatedVertices.removeAll(globalVAT.get(vatKey));
					
					// Tabulated vertices that no longer exist in graph will be re-inserted as degree-0 vertices
					for (String v : globalVAT.get(vatKey)) 
						if (!graph.containsVertex(v))
							graph.addVertex(v);
				}
			}
			
			for (String key : vatRowsToRemove)
				globalVAT.remove(key);
			
			// If the number of untabulated vertices is not a multiple of commonK, 
			// leave untabulatedVertices.size() % commonK random vertices pending
			int pendingVertCount = untabulatedVertices.size() % commonK;
			if (pendingVertCount > 0) {
				List<String> vertList = new ArrayList<>(untabulatedVertices);
				for (int i = 0; i < pendingVertCount; i++) {
					int pvId = random.nextInt(vertList.size());
					pendingVertexAdditions.add(vertList.get(pvId));
					graph.removeVertex(vertList.get(pvId));
					untabulatedVertices.remove(vertList.get(pvId));
					vertList.remove(pvId);
				}
			}
			
			// Perform anonymization
			updateVATRoundRobin(graph, untabulatedVertices);
			alignBlocks(graph, globalVAT);
			if (randomize)
				randomlyUniformizeCrossingEdges(graph);
			else
				copyCrossingEdges(graph);
			 
		}
		else   // Just in case
			anonymizeFirstSnapshot(graph, randomize, uniqueIdFileName);
	}
	
	protected void updateVATRoundRobin(UndirectedGraph<String, DefaultEdge> graph, Set<String> untabulatedVertices) {
		// Create new VAT entries by decrementally-degree-sorted round-robin
		List<String> degreeSortedUntabVerts = GraphUtil.degreeSortedVertexList(graph, untabulatedVertices, false);
		for (int i = 0; i < degreeSortedUntabVerts.size() / commonK; i++) {
			String newRowKey = degreeSortedUntabVerts.get(i * commonK);
			List<String> newRowEntries = new ArrayList<>();
			for (int j = 0; j < commonK; j++)
				newRowEntries.add(degreeSortedUntabVerts.get(i * commonK + j));
			globalVAT.put(newRowKey, newRowEntries);
		}
	}
	
	protected void updateVATRandLocalSearch(UndirectedGraph<String, DefaultEdge> graph, Set<String> untabulatedVertices, int maxItersTotal, int maxItersNoImprov) {
		
		// Initialize new VAT entries by decrementally-degree-sorted round-robin
		Map<String, List<String>> newVATRows = new TreeMap<>();
		List<String> degreeSortedUntabVerts = GraphUtil.degreeSortedVertexList(graph, untabulatedVertices, false);
		for (int i = 0; i < degreeSortedUntabVerts.size() / commonK; i++) {
			String newRowKey = degreeSortedUntabVerts.get(i * commonK);
			List<String> newRowEntries = new ArrayList<>();
			for (int j = 0; j < commonK; j++)
				newRowEntries.add(degreeSortedUntabVerts.get(i * commonK + j));
			newVATRows.put(newRowKey, newRowEntries);
		}
		
		// Modify new VAT entries to minimize number of crossing edges
		int nonImprovIterCount = 0;
		int currentCost = groupCost(graph, newVATRows, true);
		
		for (int i = 0; i < maxItersTotal; i++) {
			
			// Randomly swap a pair of elements in the new VAT rows
			Map<String, List<String>> modifNewVATRows = randomlyModifiedVATRowSet(newVATRows, true); 
			
			// Evaluate swap and keep if better
			int newCost = groupCost(graph, modifNewVATRows, true);
			if (newCost < currentCost) {
				currentCost = newCost;
				nonImprovIterCount = 0;
				newVATRows = modifNewVATRows;
			}
			else if (++nonImprovIterCount >= maxItersNoImprov)
				break;
		}
	}
	
	/***
	 * 
	 * @param graph
	 * @param untabulatedVertices
	 * @param maxItersTotal
	 * @param maxItersSameEnergy: maximum number iterations at the same energy level, after which the search can stop
	 * @param maxWorstSolRejections: maximum number of rejected worse solutions, after which the search can stop
	 * @param maxAcceptableWorstSolXTempLevel: maximum number of accepted worse solutions for each temperature, after which temperature must be reduced  
	 * @param startTemp
	 * @param minTemp: minimum temperature, below which the search can stop
	 * @param startTempDescentRate: initial temperature descent rate
	 * @param itersXDescentRate: the number of iterations after which the temperature descent rate is reduced so temperature descends faster as the process advances
	 */
	
	protected void updateVATSimulatedAnnealing(UndirectedGraph<String, DefaultEdge> graph, Set<String> untabulatedVertices, 
			int maxItersTotal, int maxItersSameEnergy, int maxWorstSolRejections, int maxAcceptableWorstSolXTempLevel,  
			double startTemp, double minTemp, double startTempDescentRate, int itersXDescentRate) {
		
		// Initialize new VAT entries by decrementally-degree-sorted round-robin
		Map<String, List<String>> newVATRows = new TreeMap<>();
		List<String> degreeSortedUntabVerts = GraphUtil.degreeSortedVertexList(graph, untabulatedVertices, false);
		for (int i = 0; i < degreeSortedUntabVerts.size() / commonK; i++) {
			String newRowKey = degreeSortedUntabVerts.get(i * commonK);
			List<String> newRowEntries = new ArrayList<>();
			for (int j = 0; j < commonK; j++)
				newRowEntries.add(degreeSortedUntabVerts.get(i * commonK + j));
			newVATRows.put(newRowKey, newRowEntries);
		}
		
		// Modify new VAT entries to minimize number of crossing edges
		
		double temperature = startTemp;
		double tempDescentRate = startTempDescentRate; 
		int iterCount = 0;
		int itersSameEenergyCount = 0;
		int acceptedWorstSolutions = 0;
		int rejectedWorstSolutions = 0;
		
		double denominatorEnergyForm = (double)upperBoundCost(graph, newVATRows);
		double energyCurrentSolution = (double)groupCost(graph, newVATRows, true) / denominatorEnergyForm;
		
		SecureRandom random = new SecureRandom();
				
		do {
			
			// Get a new candidate solution 
			// Perform a local change with prob. 0.95, or an exploratory change with prob. 0.05
			
			Map<String, List<String>> modifNewVATRows = null;
			if (random.nextDouble() < 0.95d)
				modifNewVATRows = randomlyModifiedVATRowSet(newVATRows, true);
			else
				modifNewVATRows = randomlyModifiedVATRowSet(newVATRows, false);
			
			// Evaluate new candidate solution and keep it:
			// a) with prob. 1 if it is better
			// b) with prob. 0.005 if it is equally costly 
			// c) with prob. exp(-delta E / T) if it is worse
			
			double energyNewSolution = (double)groupCost(graph, modifNewVATRows, true) / denominatorEnergyForm;
			
			if (energyNewSolution < energyCurrentSolution) {	
				newVATRows = modifNewVATRows;
				energyCurrentSolution = energyNewSolution;
				itersSameEenergyCount = 0;
			}
			else if (energyNewSolution == energyCurrentSolution) {   // This is in fact quite unlikely to happen for energy defined in terms of anonymization cost
				if (random.nextDouble() < 0.005d)   // Take equal energy solution with prob. 0.005
					newVATRows = modifNewVATRows;
				itersSameEenergyCount++;
			} 
			else {  // energyNewSolution > energyCurrentSolution				
				// Accept worse solution with prob. exp(-delta E / T)
				if (random.nextDouble() < Math.exp((double)(energyCurrentSolution - energyNewSolution) / temperature)) {
					newVATRows = modifNewVATRows;
					acceptedWorstSolutions++;
				}
				else 
					rejectedWorstSolutions++;
				itersSameEenergyCount = 0;
			}
			
			// Update annealing parameters
			
			if (acceptedWorstSolutions >= maxAcceptableWorstSolXTempLevel) {
				temperature *= tempDescentRate;
				acceptedWorstSolutions = 0;
				rejectedWorstSolutions = 0;
			}
			
			if (iterCount % itersXDescentRate == itersXDescentRate - 1)
				tempDescentRate -= tempDescentRate * 10.0d;
			
		}
		while (iterCount < maxItersTotal && temperature >= minTemp && itersSameEenergyCount < maxItersSameEnergy && (rejectedWorstSolutions < maxWorstSolRejections || acceptedWorstSolutions > 0));
		// The process stops when temperature is sufficiently low, a sufficient number of iterations are performed without changing
		// system energy, or up to maxWorstSolRejections worse solutions are rejected without accepting none for some temperature
	}
	
	protected Map<String, List<String>> randomlyModifiedVATRowSet(Map<String, List<String>> currentVATRowSet, boolean localChange) {
		
		Map<String, List<String>> modifNewVATRows = new TreeMap<>(currentVATRowSet);
		SecureRandom random = new SecureRandom();
		
		if (localChange) {
			
			List<String> newVATKeys = new ArrayList<>(currentVATRowSet.keySet());
			
			// Randomly select elements to swap
			String swapKey1 = newVATKeys.get(random.nextInt(newVATKeys.size()));
			int swapOrd1 = random.nextInt(currentVATRowSet.get(swapKey1).size());
			String swapKey2 = newVATKeys.get(random.nextInt(newVATKeys.size()));
			int swapOrd2 = random.nextInt(currentVATRowSet.get(swapKey2).size());
			while (swapKey1.equals(swapKey2) && swapOrd1 == swapOrd2) {
				swapKey1 = newVATKeys.get(random.nextInt(newVATKeys.size()));
				swapOrd1 = random.nextInt(currentVATRowSet.get(swapKey1).size());
				swapKey2 = newVATKeys.get(random.nextInt(newVATKeys.size()));
				swapOrd2 = random.nextInt(currentVATRowSet.get(swapKey2).size());
			}
			
			// Perform the swap 
			String tmp = modifNewVATRows.get(swapKey1).get(swapOrd1);
			modifNewVATRows.get(swapKey1).set(swapOrd1, modifNewVATRows.get(swapKey2).get(swapOrd2));
			modifNewVATRows.get(swapKey2).set(swapOrd2, tmp);
			
			// Update row keys if needed
			if (swapOrd1 == 0 && swapOrd2 != 0) {   
				modifNewVATRows.put(modifNewVATRows.get(swapKey1).get(0), modifNewVATRows.get(swapKey1));
				modifNewVATRows.remove(swapKey1);
			}
			else if (swapOrd1 != 0 && swapOrd2 == 0) {
				modifNewVATRows.put(modifNewVATRows.get(swapKey2).get(0), modifNewVATRows.get(swapKey2));
				modifNewVATRows.remove(swapKey2);
			}
			else {
				List<String> row1 = modifNewVATRows.get(swapKey1);
				List<String> row2 = modifNewVATRows.get(swapKey2);
				modifNewVATRows.remove(swapKey1);
				modifNewVATRows.remove(swapKey2);
				modifNewVATRows.put(row1.get(0), row1);
				modifNewVATRows.put(row2.get(0), row2);
			}
		}
		else {   // Exploratory (non-local) modifications
			
			// Perform a number of local modifications ranging from minChangeCount to 3 * minChangeCount,   
			// where minChangeCount is one fifth of the number of vertices in the set of new VAT rows
			int minChangeCount = (currentVATRowSet.size() * commonK) / 5;
			int changeCount = minChangeCount + random.nextInt(2 * minChangeCount + 1);
			for (int i = 0; i < changeCount; i++) 
				modifNewVATRows = randomlyModifiedVATRowSet(modifNewVATRows, true);
		}
		
		return modifNewVATRows;
	}
	
	// This is a loose, efficiently computable upper bound to be used as the normalizing factor 
	// for converting anonymization cost into energy in the simulated annealing VAT update method  
	protected int upperBoundCost(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> vatRowSet) {
		
		Set<String> vertsInVATRows = new TreeSet<>();
		for (String key : vatRowSet.keySet())
			for (String v : vatRowSet.get(key))
				vertsInVATRows.add(v);
		
		int upperBound = 0;
		for (String v : vertsInVATRows) 
			upperBound += (commonK - 1 ) * graph.degreeOf(v);
		upperBound /= 2;
		
		return upperBound;
	}
	
}
