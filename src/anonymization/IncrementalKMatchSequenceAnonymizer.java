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
import org.jgrapht.graph.SimpleGraph;

import util.BarabasiAlbertGraphGenerator;
import util.GraphUtil;
import util.WrapperMETIS;

public class IncrementalKMatchSequenceAnonymizer extends KMatchAnonymizer {
	
	/***
	 * Declarations
	 */
	
	protected boolean useMETIS;
	protected String metisExecName;
	protected String metisWorkDirName;
	protected static int metisFailureCount = 0;
	
	protected int commonK = 2;
	
	protected boolean firstSnapshotAnonymized = false;
	
	protected boolean optFoundImprov = false;
	
	/*** 
	 * optimizationType == 0: no optimzation
	 * optimizationType == 1: randomized local search
	 * else: simulated annealing
	 */ 
	protected int optimizationType;
	
	protected List<Set<String>> vertexSetsXGlobalVATColumn;
	
	/***
	 * Public interface
	 */
	
	public IncrementalKMatchSequenceAnonymizer(int k) {
		globalVAT = null;
		vertexSetsXGlobalVATColumn = null;
		commonK = k;
		firstSnapshotAnonymized = false;
		optimizationType = 0;   // No optimization
		useMETIS = false;
		optFoundImprov = false;
	}
	
	public IncrementalKMatchSequenceAnonymizer(int k, String eName, String wName) {
		globalVAT = null;
		vertexSetsXGlobalVATColumn = null;
		commonK = k;
		firstSnapshotAnonymized = false;
		optimizationType = 0;   // No optimization
		useMETIS = true;
		metisExecName = eName;
		metisWorkDirName = wName;
		optFoundImprov = false;
	}
	
	public IncrementalKMatchSequenceAnonymizer(int k, int optType) {
		globalVAT = null;
		vertexSetsXGlobalVATColumn = null;
		commonK = k;
		firstSnapshotAnonymized = false;
		optimizationType = optType;
		useMETIS = false;
		optFoundImprov = false;
	}
	
	public IncrementalKMatchSequenceAnonymizer(int k, int optType, String eName, String wName) {
		globalVAT = null;
		vertexSetsXGlobalVATColumn = null;
		commonK = k;
		firstSnapshotAnonymized = false;
		optimizationType = optType;
		useMETIS = true;
		metisExecName = eName;
		metisWorkDirName = wName;
		optFoundImprov = false;
	}
	
	public void restart(int k) {
		commonK = k;
		globalVAT = null;
		vertexSetsXGlobalVATColumn = null;
		firstSnapshotAnonymized = false;
		optFoundImprov = false;
	}

	@Override
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, boolean randomize, String uniqueIdFileName) {
		if (firstSnapshotAnonymized)
			anonymizeNewSnapshot(graph, randomize, uniqueIdFileName);
		else
			anonymizeFirstSnapshot(graph, randomize, uniqueIdFileName);
	}
	
	@Override
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize, String uniqueIdFileName) {
		if (k == commonK) 
			anonymizeGraph(graph, k, randomize, uniqueIdFileName);
		else 
			throw new RuntimeException("Forcing anonymization for k = " + k + ", anonymizer created for k = " + commonK);
	}
	
	@Override
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize) {
		anonymizeGraph(graph, k, randomize, "DefaultNameServiceFileIncrementalKMatchAnonymizerUsingMETIS");
	}
	
	@Override
	public void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, boolean randomize) {
		anonymizeGraph(graph, randomize, "DefaultNameServiceFileIncrementalKMatchAnonymizerUsingMETIS");
	}
	
	public boolean optimizationFoundImprovement() {
		return optFoundImprov;
	}
	
	/***
	 * Methods for anonymizing the first snapshot (first ever or first after a call of restart())
	 */
	
	protected void anonymizeFirstSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomize, String uniqueIdFileName) {
		// The static version adds dummy vertices, the dynamic one will not
		
		optFoundImprov = false;
		
		SecureRandom random = new SecureRandom();
		
		if (commonK < graph.vertexSet().size()) {
			
			// If the number of vertices is not a multiple of commonK, leave graph.vertexSet().size() % commonK random vertices pending
			
			int pendingVertCount = graph.vertexSet().size() % commonK;
			if (pendingVertCount > 0) {
				List<String> vertList = new ArrayList<>(graph.vertexSet());
				for (int i = 0; i < pendingVertCount; i++) {
					int pvId = random.nextInt(vertList.size());
					graph.removeVertex(vertList.get(pvId));
					vertList.remove(pvId);
				}
			}
			
			// Perform anonymization on trimmed graph
			
			performAnonymizationFirstSnapshot(graph, randomize, uniqueIdFileName);
		}
		else {
			// Return a null graph (0 vertices)
			Set<String> tmpVertSet = new TreeSet<>(graph.vertexSet());
			graph.removeAllVertices(tmpVertSet);
		}
		
		firstSnapshotAnonymized = true;
	}
	
	protected void performAnonymizationFirstSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomized, String uniqueIdFileName) {
		
		Map<String, Set<String>> partition = null;
		
		boolean doRoundRobinPartition = !useMETIS;
		
		if (useMETIS) {
			try {
				WrapperMETIS metisHandler = new WrapperMETIS(metisExecName, metisWorkDirName);
				partition = metisHandler.getPartitionVertSets(graph, commonK, uniqueIdFileName, false);	
			} catch (IOException | InterruptedException | RuntimeException e) {
				doRoundRobinPartition = true;
				metisFailureCount++;   // Just update, initialization and use up to the caller
			}
		}
		
		if (doRoundRobinPartition) { 			
			partition = new TreeMap<>();
			for (int i = 0; i < commonK; i++)
				partition.put(i + "", new TreeSet<String>());
			List<String> sortedVertList = GraphUtil.degreeSortedVertexList(graph, null, false);
			for (int i = 0; i < sortedVertList.size(); i++)
				partition.get((i % commonK) + "").add(sortedVertList.get(i));
		}
		
		// Perform the anonymization
		initializeVAT(graph, partition);
		if (optimizationType == 1)
			optimizeGlobalVATRandLocalSearch(graph, 10, 1000, 500);
		else if (optimizationType == 2)
			optimizeGlobalVATSimulatedAnnealing(graph, 10000, 5000, 5000, 100, 0.005d, 0.000001d, 0.95d, 500);
		alignBlocks(graph, globalVAT);
		if (randomized)
			randomlyUniformizeCrossingEdges(graph);
		else
			copyCrossingEdges(graph);   // Original approach by Zou et al.	
	}
	
	protected void initializeVAT(UndirectedGraph<String, DefaultEdge> graph, Map<String, Set<String>> partition) {
		
		globalVAT = new TreeMap<>();
		Set<String> vertsInVAT = new TreeSet<>();
		SecureRandom random = new SecureRandom();   // Will be used to randomize all equally optimal decisions
		
		// All elements of the partition must have the same number of vertices, but METIS does not guarantee that 
		// Some reallocations may be needed
		
		boolean balancingNeeded = false;
		List<String> blocksMissingVertices = new ArrayList<>();
		List<String> blocksExcessVertices = new ArrayList<>();
		for (String pid : partition.keySet()) {
			if (partition.get(pid).size() < graph.vertexSet().size() / commonK) {
				blocksMissingVertices.add(pid);
				balancingNeeded = true;
			}
			else if (partition.get(pid).size() > graph.vertexSet().size() / commonK) {
				blocksExcessVertices.add(pid);
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
			if (partition.get(randBlockMissingVerts).size() == graph.vertexSet().size() / commonK)
				blocksMissingVertices.remove(indRandBlockMissingVerts);
		}
		
		// Rebuilding blocks-as-subgraphs structure used in KMatchAnonymizerUsingMETIS 
		// and keeping the rest of the implementation as it was in there
		
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
		
		// First row of VAT
		
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
				globalVAT.put(rowKey, newRowVAT);
				
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
				globalVAT.put(rowKey, newRowVAT);
			}
		}
		
		// Remaining rows of VAT
		
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
					globalVAT.put(rowKey, newRowVAT);
				}
			}
			
		}
		
		// Initialize vertexSetsXGlobalVATColumn
		
		vertexSetsXGlobalVATColumn = new ArrayList<>();
		for (int i = 0; i < commonK; i++) 
			vertexSetsXGlobalVATColumn.add(new TreeSet<String>());
		for (String rowKey : globalVAT.keySet())
			for (int i = 0; i < commonK; i++) {
				String vCell = globalVAT.get(rowKey).get(i);
				vertexSetsXGlobalVATColumn.get(i).add(vCell);
			}
		
	}
	
	protected void optimizeGlobalVATRandLocalSearch(UndirectedGraph<String, DefaultEdge> graph, int roundCount, int maxItersXRound, int maxItersNoImprov) {
		
		Map<String, List<String>> globallyOptimalNewVAT = cloneVATRowSet(globalVAT);
		int costGloballyOptimalSolution = fullVATCost(graph, globallyOptimalNewVAT);
				
		for (int round = 0; round < roundCount; round++) {
			
			// Determine initial solution for this round
			
			Map<String, List<String>> currentVAT = (round == 0)? 
					cloneVATRowSet(globalVAT)              // Use globalVAT as the initial solution for the first round  
					: randomlyModifiedVATRowSet(globalVAT, 3);   // Start every other round with a shuffled version of globalVAT  
			int currentCost = fullVATCost(graph, currentVAT);
			if (currentCost < costGloballyOptimalSolution) {
				globallyOptimalNewVAT = cloneVATRowSet(currentVAT);
				costGloballyOptimalSolution = currentCost;
				optFoundImprov = true;
			}
			
			// Run local search
			
			int nonImprovIterCount = 0;
			
			for (int iter = 0; iter < maxItersXRound; iter++) {
				
				// Randomly swap a pair of elements in the new VAT rows
				Map<String, List<String>> newVAT = randomlyModifiedVATRowSet(currentVAT, 1);
				
				// Evaluate swap and keep if better
				int newCost = fullVATCost(graph, newVAT);
				if (newCost < currentCost) {
					currentCost = newCost;
					nonImprovIterCount = 0;
					currentVAT = newVAT;
					if (newCost < costGloballyOptimalSolution) {
						costGloballyOptimalSolution = newCost;
						globallyOptimalNewVAT = newVAT;
						optFoundImprov = true;
					}
				}
				else if (++nonImprovIterCount >= maxItersNoImprov)
					break;
			}
			
		}
		
		// Update globalVAT 
		
		globalVAT = cloneVATRowSet(globallyOptimalNewVAT);
		
		// Update vertexSetsXGlobalVATColumn
		
		vertexSetsXGlobalVATColumn = new ArrayList<>();
		for (int i = 0; i < commonK; i++) 
			vertexSetsXGlobalVATColumn.add(new TreeSet<String>());
		for (String rowKey : globalVAT.keySet())
			for (int i = 0; i < commonK; i++) {
				String vCell = globalVAT.get(rowKey).get(i);
				vertexSetsXGlobalVATColumn.get(i).add(vCell);
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
	
	protected void optimizeGlobalVATSimulatedAnnealing(UndirectedGraph<String, DefaultEdge> graph, 
			int maxItersTotal, int maxItersWithoutChange, 
			int maxWorseSolRejections, int maxAcceptableWorseSolXTempLevel,  
			double startTemp, double minTemp, double startTempDescentRate, int itersXDescentRate) {
		
		Map<String, List<String>> globallyOptimalNewVATRows = cloneVATRowSet(globalVAT);
		
		Map<String, List<String>> currentVAT = cloneVATRowSet(globalVAT);
		double denominatorEnergyForm = (double)((commonK - 1) * graph.edgeSet().size());
		double energyCurrentSolution = (double)fullVATCost(graph, globalVAT) / denominatorEnergyForm;
		double energyGloballyOptimalSolution = energyCurrentSolution;
		
		double temperature = startTemp;
		double tempDescentRate = startTempDescentRate; 
		int iterCount = 0;
		int itersWithoutChangeCount = 0;
		int acceptedWorseSolutions = 0;
		int rejectedWorseSolutions = 0;
		
		SecureRandom random = new SecureRandom();
				
		do {
			
			// Get a new candidate solution 
			// Perform a local change with prob. 0.95, or an exploratory change with prob. 0.05
			
			Map<String, List<String>> newVAT = null;
			if (random.nextDouble() < 0.95d)
				newVAT = randomlyModifiedVATRowSet(currentVAT, 1);
			else
				newVAT = randomlyModifiedVATRowSet(currentVAT, 3);
			
			// Evaluate new candidate solution and keep it:
			// a) with prob. 1 if it is better
			// b) with prob. 0.005 if it is equally costly 
			// c) with prob. exp(-delta E / T) if it is worse
			
			double energyNewSolution = (double)fullVATCost(graph, newVAT) / denominatorEnergyForm;
			
			if (energyNewSolution < energyCurrentSolution) {
				currentVAT = newVAT;
				energyCurrentSolution = energyNewSolution;
				itersWithoutChangeCount = 0;
				if (energyNewSolution < energyGloballyOptimalSolution) {
					globallyOptimalNewVATRows = newVAT;
					energyGloballyOptimalSolution = energyNewSolution;
					optFoundImprov = true;
				}
			}
			else if (energyNewSolution == energyCurrentSolution) {
				if (random.nextDouble() < 0.05d) {   // Take equal energy solution with prob. 0.05
					currentVAT = newVAT;
					itersWithoutChangeCount = 0;
				}
				else
					itersWithoutChangeCount++;
			} 
			else {  // energyNewSolution > energyCurrentSolution				
				// Accept a worse solution with prob. exp(-delta E / T)
				double prob = Math.exp((double)(energyCurrentSolution - energyNewSolution) / temperature);
				if (random.nextDouble() < prob) {
					currentVAT = newVAT;
					energyCurrentSolution = energyNewSolution;
					acceptedWorseSolutions++;
					itersWithoutChangeCount = 0;
				}
				else {
					rejectedWorseSolutions++;
					itersWithoutChangeCount++;
				}
			}
			
			iterCount++;
			
			// Update annealing parameters
			
			if (acceptedWorseSolutions >= maxAcceptableWorseSolXTempLevel) {
				temperature *= tempDescentRate;
				acceptedWorseSolutions = 0;
				rejectedWorseSolutions = 0;
			}
			
			if (iterCount % itersXDescentRate == itersXDescentRate - 1)
				tempDescentRate *= 0.9d;
			
		}
		while (iterCount < maxItersTotal && temperature >= minTemp && itersWithoutChangeCount < maxItersWithoutChange && (rejectedWorseSolutions < maxWorseSolRejections || acceptedWorseSolutions > 0));
		// The process stops when temperature is sufficiently low, a sufficient number of iterations are performed without changing
		// system energy, or up to maxWorstSolRejections worse solutions are rejected without accepting none for some temperature
		
		// Update globalVAT 
		
		globalVAT = cloneVATRowSet(globallyOptimalNewVATRows);
		
		// Update vertexSetsXGlobalVATColumn
		
		vertexSetsXGlobalVATColumn = new ArrayList<>();
		for (int i = 0; i < commonK; i++) 
			vertexSetsXGlobalVATColumn.add(new TreeSet<String>());
		for (String rowKey : globalVAT.keySet())
			for (int i = 0; i < commonK; i++) {
				String vCell = globalVAT.get(rowKey).get(i);
				vertexSetsXGlobalVATColumn.get(i).add(vCell);
			}
	}
	
	/***
	 * 
	 * @param currentVATRowSet:
	 * @param changeType: 0 --> swap two elements in the same row
	 *                    1 --> swap any two elements 
	 *                    2 --> perform a number of type 0 modifications ranging from minChangeCount to 3 * minChangeCount,
	 *                          where minChangeCount is one fifth of the number of vertices in the set of new VAT rows
	 *                    else: perform a number of type 1 modifications ranging from minChangeCount to 3 * minChangeCount,
	 *                          where minChangeCount is one fifth of the number of vertices in the set of new VAT rows
	 */
	
	protected Map<String, List<String>> randomlyModifiedVATRowSet(Map<String, List<String>> currentVATRowSet, int changeType) {
		
		Map<String, List<String>> newVATRowSet = cloneVATRowSet(currentVATRowSet);
		
		SecureRandom random = new SecureRandom();
		
		int changeCount = 1;
		int effectiveChangeType = changeType;
		if (changeType == 2) {
			int minChangeCount = (currentVATRowSet.size() * commonK) / 5;
			changeCount = minChangeCount + random.nextInt(2 * minChangeCount + 1);
			effectiveChangeType = 0;
		}
		else if (changeType >= 3) {
			int minChangeCount = (currentVATRowSet.size() * commonK) / 5;
			changeCount = minChangeCount + random.nextInt(2 * minChangeCount + 1);
			effectiveChangeType = 1;
		}
		
		for (int ch = 0; ch < changeCount; ch++) {
			
			// Randomly select elements to swap
			List<String> vatKeys = new ArrayList<>(newVATRowSet.keySet());
			String swapKey1 = vatKeys.get(random.nextInt(vatKeys.size()));
			int swapOrd1 = random.nextInt(newVATRowSet.get(swapKey1).size());
			String swapKey2 = swapKey1;   // Initialization required, using swapKey1  
			int swapOrd2;
			if (effectiveChangeType == 0) {   // A type 0 change will swap two elements on the same row
				swapOrd2 = random.nextInt(newVATRowSet.get(swapKey2).size());
				while (swapOrd2 == swapOrd1)
					swapOrd2 = random.nextInt(newVATRowSet.get(swapKey2).size());
			}
			else {  // effectiveChangeType == 1, swap two elements on different rows 
				while (swapKey2.equals(swapKey1))
					swapKey2 = vatKeys.get(random.nextInt(vatKeys.size()));
				swapOrd2 = random.nextInt(newVATRowSet.get(swapKey2).size());
			}
			
			// Perform the swap
			String tmp = newVATRowSet.get(swapKey1).get(swapOrd1);
			newVATRowSet.get(swapKey1).set(swapOrd1, newVATRowSet.get(swapKey2).get(swapOrd2));
			newVATRowSet.get(swapKey2).set(swapOrd2, tmp);
			
			// Update row keys if needed
			if (swapOrd1 == 0 && swapOrd2 != 0) {
				List<String> row1 = newVATRowSet.get(swapKey1);
				newVATRowSet.remove(swapKey1);
				newVATRowSet.put(row1.get(0), row1);
			}
			else if (swapOrd1 != 0 && swapOrd2 == 0) {
				List<String> row2 = newVATRowSet.get(swapKey2);
				newVATRowSet.remove(swapKey2);
				newVATRowSet.put(row2.get(0), row2);
			}
			else if (swapOrd1 == 0 && swapOrd2 == 0) {
				List<String> row1 = newVATRowSet.get(swapKey1);
				List<String> row2 = newVATRowSet.get(swapKey2);
				newVATRowSet.remove(swapKey1);
				newVATRowSet.remove(swapKey2);
				newVATRowSet.put(row1.get(0), row1);
				newVATRowSet.put(row2.get(0), row2);
			}
			
		}
		
		return newVATRowSet;
		
	}
		
	protected int fullVATCost(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> fullVAT) {
		
		// First, simulate alignBlocks to account for the editions that it would perform 
		List<String> vatKeys = new ArrayList<>(fullVAT.keySet());
		int costBlockAlignment = 0;
		for (int i = 0; i < vatKeys.size() - 1; i++)
			for (int j = i + 1; j < vatKeys.size(); j++) {
				// Check if the edge exists in some block and doesn't exist in some other
				boolean edgeFound = false, nonEdgeFound = false;
				for (int c = 0; (!edgeFound || !nonEdgeFound) && c < commonK; c++) {
					if (graph.containsVertex(fullVAT.get(vatKeys.get(i)).get(c)) && graph.containsVertex(fullVAT.get(vatKeys.get(j)).get(c)) 
						&& graph.containsEdge(fullVAT.get(vatKeys.get(i)).get(c), fullVAT.get(vatKeys.get(j)).get(c)))
						edgeFound = true;
					if (!graph.containsVertex(fullVAT.get(vatKeys.get(i)).get(c)) || !graph.containsVertex(fullVAT.get(vatKeys.get(j)).get(c)) 
						|| !graph.containsEdge(fullVAT.get(vatKeys.get(i)).get(c), fullVAT.get(vatKeys.get(j)).get(c)))
						nonEdgeFound = true;
				}
				if (edgeFound && nonEdgeFound) {
					for (int c = 0; c < commonK; c++)
						if (!graph.containsVertex(fullVAT.get(vatKeys.get(i)).get(c))
							|| !graph.containsVertex(fullVAT.get(vatKeys.get(j)).get(c))
							|| !graph.containsEdge(fullVAT.get(vatKeys.get(i)).get(c), fullVAT.get(vatKeys.get(j)).get(c)))
							costBlockAlignment++;
				}
			}
		
		// Then, compute the contribution of crossing edge copy to the overall number of editions
		int costCrossingEdges = 0;
		
		List<Set<String>> vertexSetsXBlock = new ArrayList<>();
		for (int i = 0; i < commonK; i++) 
			vertexSetsXBlock.add(new TreeSet<String>());
		for (String rowKey : fullVAT.keySet())
			for (int i = 0; i < commonK; i++) {
				String vCell = fullVAT.get(rowKey).get(i);
				vertexSetsXBlock.get(i).add(vCell);
			}
		int sumCrossEdges = 0;
		for (int i = 0; i < commonK; i++) {
			for (String v : vertexSetsXBlock.get(i)) {
				Set<String> neighbours = new TreeSet<>(Graphs.neighborListOf(graph, v));
				neighbours.removeAll(vertexSetsXBlock.get(i));
				sumCrossEdges += neighbours.size();
			}
		}
		costCrossingEdges = ((commonK - 1) * sumCrossEdges) / 2;
		
		return costBlockAlignment + costCrossingEdges;
	}
	
	/***
	 * Methods for incrementally anonymizing a non-first snapshot (not the first ever and not the first after a call of restart())
	 */
	
	protected void anonymizeNewSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomize, String uniqueIdFileName) {
		
		if (firstSnapshotAnonymized) {
			
			optFoundImprov = false;
			
			SecureRandom random = new SecureRandom();
			
			// Note: vertices not included in previous snapshots that no longer exist in this snapshot
			// will never be represented in the private sequence
			
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
			
			for (String key : vatRowsToRemove) {
				List<String> rowToRemove = globalVAT.get(key);
				for (int i = 0; i < commonK; i++)
					vertexSetsXGlobalVATColumn.get(i).remove(rowToRemove.get(i));
				globalVAT.remove(key);
			}
			
			// If the number of untabulated vertices is not a multiple of commonK, 
			// leave untabulatedVertices.size() % commonK random vertices pending
			int pendingVertCount = untabulatedVertices.size() % commonK;
			if (pendingVertCount > 0) {
				List<String> vertList = new ArrayList<>(untabulatedVertices);
				for (int i = 0; i < pendingVertCount; i++) {
					int pvId = random.nextInt(vertList.size());
					graph.removeVertex(vertList.get(pvId));
					untabulatedVertices.remove(vertList.get(pvId));
					vertList.remove(pvId);
				}
			}
			
			// Perform anonymization
			if (optimizationType == 0)
				extendVAT(newVATRowsRoundRobin(graph, untabulatedVertices));
			else if (optimizationType == 1)
				extendVAT(optimizedVATRowsRandLocalSearch(graph, newVATRowsRoundRobin(graph, untabulatedVertices), 10, 1000, 500));
			else
				extendVAT(optimizedVATRowsSimulatedAnnealing(graph, newVATRowsRoundRobin(graph, untabulatedVertices), 10000, 5000, 5000, 100, 0.005d, 0.000001d, 0.95d, 500));
			alignBlocks(graph, globalVAT);
			if (randomize)
				randomlyUniformizeCrossingEdges(graph);
			else
				copyCrossingEdges(graph);
			 
		}
		else   // Just in case
			anonymizeFirstSnapshot(graph, randomize, uniqueIdFileName);
	}
	
	protected Map<String, List<String>> newVATRowsRoundRobin(UndirectedGraph<String, DefaultEdge> graph, Set<String> untabulatedVertices) {
		Map<String, List<String>> newVATRows = new TreeMap<>();
		List<String> degreeSortedUntabVerts = GraphUtil.degreeSortedVertexList(graph, untabulatedVertices, false);
		for (int i = 0; i < degreeSortedUntabVerts.size() / commonK; i++) {
			String newRowKey = degreeSortedUntabVerts.get(i * commonK);
			List<String> newRowEntries = new ArrayList<>();
			for (int j = 0; j < commonK; j++)
				newRowEntries.add(degreeSortedUntabVerts.get(i * commonK + j));
			newVATRows.put(newRowKey, newRowEntries);
		}
		return newVATRows;
	}
	
	protected void extendVAT(Map<String, List<String>> newVATRows) {
		for (String key : newVATRows.keySet()) {
			List<String> rowToAdd = newVATRows.get(key);
			for (int i = 0; i < commonK; i++)
				vertexSetsXGlobalVATColumn.get(i).add(rowToAdd.get(i));
			globalVAT.put(key, rowToAdd);
		}
	}
	
	protected Map<String, List<String>> optimizedVATRowsRandLocalSearch(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> initialVATRows, int roundCount, int maxItersXRound, int maxItersNoImprov) {
				
		Map<String, List<String>> globallyOptimalNewVATRows = cloneVATRowSet(initialVATRows);
		int costGloballyOptimalSolution = vatExtensionCost(graph, globallyOptimalNewVATRows);
		
		for (int round = 0; round < roundCount; round++) {
			
			// Determine initial solution for this round
			
			Map<String, List<String>> currentVATRows = (round == 0)? 
					cloneVATRowSet(initialVATRows)                    // Use initialVATRows as the initial solution for the first round  
					: randomlyModifiedVATRowSet(initialVATRows, 3);   // Start every other round with a shuffled version of initialVATRows  
			int currentCost = vatExtensionCost(graph, currentVATRows);
			if (currentCost < costGloballyOptimalSolution) {
				globallyOptimalNewVATRows = cloneVATRowSet(currentVATRows);
				costGloballyOptimalSolution = currentCost;
				optFoundImprov = true;
			}
			
			// Run local search
			
			int nonImprovIterCount = 0;
			
			for (int iter = 0; iter < maxItersXRound; iter++) {
				
				// Randomly swap a pair of elements in the new VAT rows
				Map<String, List<String>> newVATRows = randomlyModifiedVATRowSet(currentVATRows, 1); 
				
				// Evaluate swap and keep if better
				int newCost = vatExtensionCost(graph, newVATRows);
				if (newCost < currentCost) {
					currentCost = newCost;
					nonImprovIterCount = 0;
					currentVATRows = newVATRows;
					if (newCost < costGloballyOptimalSolution) {
						costGloballyOptimalSolution = newCost;
						globallyOptimalNewVATRows = newVATRows;
						optFoundImprov = true;
					}
				}
				else if (++nonImprovIterCount >= maxItersNoImprov)
					break;
			}
			
		}
		
		return globallyOptimalNewVATRows;
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
	
	protected Map<String, List<String>> optimizedVATRowsSimulatedAnnealing(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> currentVATRows, 
			int maxItersTotal, int maxItersWithoutChange, int maxWorseSolRejections, int maxAcceptableWorseSolXTempLevel,  
			double startTemp, double minTemp, double startTempDescentRate, int itersXDescentRate) {
		
		Map<String, List<String>> globallyOptimalNewVATRows = cloneVATRowSet(currentVATRows);
		
		double denominatorEnergyForm = (double)((commonK - 1) * graph.edgeSet().size());
		double energyCurrentSolution = (double)vatExtensionCost(graph, currentVATRows) / denominatorEnergyForm;
		double energyGloballyOptimalSolution = energyCurrentSolution;
		
		double temperature = startTemp;
		double tempDescentRate = startTempDescentRate; 
		int iterCount = 0;
		int itersWithoutChangeCount = 0;
		int acceptedWorseSolutions = 0;
		int rejectedWorseSolutions = 0;
		
		SecureRandom random = new SecureRandom();
				
		do {
			
			// Get a new candidate solution 
			// Perform a local change with prob. 0.95, or an exploratory change with prob. 0.05
			
			Map<String, List<String>> newVATRows = null;
			if (random.nextDouble() < 0.95d)
				newVATRows = randomlyModifiedVATRowSet(currentVATRows, 1);
			else
				newVATRows = randomlyModifiedVATRowSet(currentVATRows, 3);
			
			// Evaluate new candidate solution and keep it:
			// a) with prob. 1 if it is better
			// b) with prob. 0.005 if it is equally costly 
			// c) with prob. exp(-delta E / T) if it is worse
			
			double energyNewSolution = (double)vatExtensionCost(graph, newVATRows) / denominatorEnergyForm;
			
			if (energyNewSolution < energyCurrentSolution) {
				currentVATRows = newVATRows;
				energyCurrentSolution = energyNewSolution;
				itersWithoutChangeCount = 0;
				if (energyNewSolution < energyGloballyOptimalSolution) {
					globallyOptimalNewVATRows = newVATRows;
					energyGloballyOptimalSolution = energyNewSolution;
					optFoundImprov = true;
				}
			}
			else if (energyNewSolution == energyCurrentSolution) {
				if (random.nextDouble() < 0.05d) {   // Take equal energy solution with prob. 0.05
					currentVATRows = newVATRows;
					itersWithoutChangeCount = 0;
				}
				else
					itersWithoutChangeCount++;
			} 
			else {  // energyNewSolution > energyCurrentSolution				
				// Accept a worse solution with prob. exp(-delta E / T)
				double prob = Math.exp((double)(energyCurrentSolution - energyNewSolution) / temperature);
				if (random.nextDouble() < prob) {
					currentVATRows = newVATRows;
					energyCurrentSolution = energyNewSolution;
					acceptedWorseSolutions++;
					itersWithoutChangeCount = 0;
				}
				else {
					rejectedWorseSolutions++;
					itersWithoutChangeCount++;
				}
			}
			
			iterCount++;
			
			// Update annealing parameters
			
			if (acceptedWorseSolutions >= maxAcceptableWorseSolXTempLevel) {
				temperature *= tempDescentRate;
				acceptedWorseSolutions = 0;
				rejectedWorseSolutions = 0;
			}
			
			if (iterCount % itersXDescentRate == itersXDescentRate - 1)
				tempDescentRate *= 0.9d;
			
		}
		while (iterCount < maxItersTotal && temperature >= minTemp && itersWithoutChangeCount < maxItersWithoutChange && (rejectedWorseSolutions < maxWorseSolRejections || acceptedWorseSolutions > 0));
		// The process stops when temperature is sufficiently low, a sufficient number of iterations are performed without changing
		// system energy, or up to maxWorstSolRejections worse solutions are rejected without accepting none for some temperature
		
		return globallyOptimalNewVATRows;		
	}
		
	protected int vatExtensionCost(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> newVATRows) {
		
		// First, simulate alignBlocks, restricted to edges incident in vertices in newVATRows, 
		// to account for the editions that it would perform 
		
		List<String> newRowsKeys = new ArrayList<>(newVATRows.keySet());
		List<String> currentVATKeys = new ArrayList<>(globalVAT.keySet());
		
		int costBlockAlignment = 0;
		
		for (int i = 0; i < newRowsKeys.size(); i++) {
			
			// Edges among two vertices in newVATRows
			
			if (i < newRowsKeys.size() - 1) {
				for (int j = i + 1; j < newRowsKeys.size(); j++) {
					// Check if the edge exists in some block and doesn't exist in some other
					boolean edgeFound = false, nonEdgeFound = false;
					for (int c = 0; (!edgeFound || !nonEdgeFound) && c < commonK; c++) {
						if (graph.containsVertex(newVATRows.get(newRowsKeys.get(i)).get(c)) && graph.containsVertex(newVATRows.get(newRowsKeys.get(j)).get(c)) 
							&& graph.containsEdge(newVATRows.get(newRowsKeys.get(i)).get(c), newVATRows.get(newRowsKeys.get(j)).get(c)))
							edgeFound = true;
						if (!graph.containsVertex(newVATRows.get(newRowsKeys.get(i)).get(c)) || !graph.containsVertex(newVATRows.get(newRowsKeys.get(j)).get(c)) 
							|| !graph.containsEdge(newVATRows.get(newRowsKeys.get(i)).get(c), newVATRows.get(newRowsKeys.get(j)).get(c)))
							nonEdgeFound = true;
					}
					if (edgeFound && nonEdgeFound) {
						for (int c = 0; c < commonK; c++)
							if (!graph.containsVertex(newVATRows.get(newRowsKeys.get(i)).get(c))
								|| !graph.containsVertex(newVATRows.get(newRowsKeys.get(j)).get(c))
								|| !graph.containsEdge(newVATRows.get(newRowsKeys.get(i)).get(c), newVATRows.get(newRowsKeys.get(j)).get(c)))
								costBlockAlignment++;
					}
				}
			}
			
			// Edges between a vertex in newVATRows and a vertex in globalVAT
			
			for (int j = 0; j < currentVATKeys.size(); j++) {
				// Check if the edge exists in some block and doesn't exist in some other
				boolean edgeFound = false, nonEdgeFound = false;
				for (int c = 0; (!edgeFound || !nonEdgeFound) && c < commonK; c++) {
					if (graph.containsVertex(newVATRows.get(newRowsKeys.get(i)).get(c)) && graph.containsVertex(globalVAT.get(currentVATKeys.get(j)).get(c)) 
						&& graph.containsEdge(newVATRows.get(newRowsKeys.get(i)).get(c), globalVAT.get(currentVATKeys.get(j)).get(c)))
						edgeFound = true;
					if (!graph.containsVertex(newVATRows.get(newRowsKeys.get(i)).get(c)) || !graph.containsVertex(globalVAT.get(currentVATKeys.get(j)).get(c)) 
						|| !graph.containsEdge(newVATRows.get(newRowsKeys.get(i)).get(c), globalVAT.get(currentVATKeys.get(j)).get(c)))
						nonEdgeFound = true;
				}
				if (edgeFound && nonEdgeFound) {
					for (int c = 0; c < commonK; c++)
						if (!graph.containsVertex(newVATRows.get(newRowsKeys.get(i)).get(c))
							|| !graph.containsVertex(globalVAT.get(currentVATKeys.get(j)).get(c))
							|| !graph.containsEdge(newVATRows.get(newRowsKeys.get(i)).get(c), globalVAT.get(currentVATKeys.get(j)).get(c)))
							costBlockAlignment++;
				}
			}
		}
		
		// Then, compute the contribution of crossing edge copy to the overall number of editions
		
		int costCrossingEdges = 0;
			
		// Compute equivalent of vertexSetsXGlobalVATColumn for new VAT entries
		
		List<Set<String>> vertexSetsXColumNewVATRows = new ArrayList<>();
		for (int i = 0; i < commonK; i++)
			vertexSetsXColumNewVATRows.add(new TreeSet<String>());
		for (String rowKey : newVATRows.keySet())
			for (int i = 0; i < commonK; i++) {
				String vCell = newVATRows.get(rowKey).get(i);
				vertexSetsXColumNewVATRows.get(i).add(vCell);
			}
		
		// Compute cost of necessary edge copies
		
		int sumCrossEdges = 0;
		
		for (int i = 0; i < commonK; i++) {
			for (String v : vertexSetsXGlobalVATColumn.get(i)) {
				Set<String> neighbours = new TreeSet<>(Graphs.neighborListOf(graph, v));
				neighbours.removeAll(vertexSetsXGlobalVATColumn.get(i));
				neighbours.removeAll(vertexSetsXColumNewVATRows.get(i));
				sumCrossEdges += neighbours.size();
			}
		}
		
		costCrossingEdges = ((commonK - 1) * sumCrossEdges) / 2;
		
		return costBlockAlignment + costCrossingEdges;
	}
	
	/***
	 * 
	 * The purpose of this main method is to serve as support for debugging and testing
	 * 
	 */
	
	public static void main(String [] args) {
		
		if (args.length == 2) {
			
			metisFailureCount = 0;
			
			int[] ks = {2, 5, 8};
			
			Map<Integer, Map<Integer, Integer>> timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNoOptStriclyBetterThanSimAnnealingUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS = new TreeMap<>();
					
			Map<Integer, Map<Integer, Integer>> timesMETISNoOptWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNotMETISNoOptWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesMETISRandLocalSearchWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNotMETISRandLocalSearchWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesMETISSimAnnealingWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNotMETISSimAnnealingWasBest = new TreeMap<>();
			
			int[] snapshots = {1, 2, 3};
			for (int ss : snapshots) {
				
				timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				
				timesMETISNoOptWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesNotMETISNoOptWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesMETISRandLocalSearchWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesNotMETISRandLocalSearchWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesMETISSimAnnealingWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesNotMETISSimAnnealingWasBest.put(ss, new TreeMap<Integer, Integer>());
				
				for (int k : ks) {
					
					timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(ss).put(k, 0);
					timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(ss).put(k, 0);
					timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(ss).put(k, 0);
					timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(ss).put(k, 0);
					timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(ss).put(k, 0);
					timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(ss).put(k, 0);
					
					timesMETISNoOptWasBest.get(ss).put(k, 0);
					timesNotMETISNoOptWasBest.get(ss).put(k, 0);
					timesMETISRandLocalSearchWasBest.get(ss).put(k, 0);
					timesNotMETISRandLocalSearchWasBest.get(ss).put(k, 0);
					timesMETISSimAnnealingWasBest.get(ss).put(k, 0);
					timesNotMETISSimAnnealingWasBest.get(ss).put(k, 0);
				
				}
			}
			
			int maxIterCount = 100;
			
			for (int iter = 0; iter < maxIterCount; iter++) {
				
				System.out.println("Iteration # " + iter);
				
				// Create anonymizers for k \in ks
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersUsingMETISNoOptimization = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersNotUsingMETISNoOptimization = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersUsingMETISRandLocalSearch = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersNotUsingMETISRandLocalSearch = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersUsingMETISSimAnnealing = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersNotUsingMETISSimAnnealing = new TreeMap<>();
				for (int k : ks) {
					anonymizersUsingMETISNoOptimization.put(k, new IncrementalKMatchSequenceAnonymizer(k, 0, args[0], args[1]));
					anonymizersNotUsingMETISNoOptimization.put(k, new IncrementalKMatchSequenceAnonymizer(k, 0));
					anonymizersUsingMETISRandLocalSearch.put(k, new IncrementalKMatchSequenceAnonymizer(k, 1, args[0], args[1]));
					anonymizersNotUsingMETISRandLocalSearch.put(k, new IncrementalKMatchSequenceAnonymizer(k, 1));
					anonymizersUsingMETISSimAnnealing.put(k, new IncrementalKMatchSequenceAnonymizer(k, 2, args[0], args[1]));
					anonymizersNotUsingMETISSimAnnealing.put(k, new IncrementalKMatchSequenceAnonymizer(k, 2));
				}
				
				System.out.println("First snapshot");
				
				UndirectedGraph<String, DefaultEdge> graph = BarabasiAlbertGraphGenerator.newGraph(200, 0, 50, 5, 3);
					
				int origEdgeCount = graph.edgeSet().size(), origVertexCount = graph.vertexSet().size();
			
				// Apply the method with k \in ks
				for (int k : ks) {
					
					System.out.println("\tk = " + k);
					
					//System.out.println("\tCopying crossing edges:");
					
					UndirectedGraph<String, DefaultEdge> cloneMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					
					anonymizersUsingMETISNoOptimization.get(k).anonymizeGraph(cloneMETISNoOpt, false, "TesterMETISNoOpt");
					anonymizersNotUsingMETISNoOptimization.get(k).anonymizeGraph(cloneNotMETISNoOpt, false, "TesterNoMETISNoOpt");
					anonymizersUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneMETISRandLocalSearch, false, "TesterMETISRandLocalSearch");
					anonymizersNotUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneNotMETISRandLocalSearch, false, "TesterNoMETISRandLocalSearch");
					anonymizersUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneMETISSimAnnealing, false, "TesterMETISSimAnnealing");
					anonymizersNotUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneNotMETISSimAnnealing, false, "TesterNoMETISSimAnnealing");
					
					// Report effect of anonymization on the graph
					System.out.println("\t\tOriginal vertex count: " + origVertexCount);
					System.out.println("\t\tFinal vertex count using METIS, no optimization: " + cloneMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, no optimization: " + cloneNotMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, randomized local search: " + cloneMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, simulated annealing: " + cloneMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tOriginal edge count: " + origEdgeCount);
					System.out.println("\t\tFinal edge count using METIS, no optimization: " + cloneMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, no optimization: " + cloneNotMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, randomized local search: " + cloneMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, simulated annealing: " + cloneMETISSimAnnealing.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.edgeSet().size());
					int deltaEdgeMETISNoOpt = cloneMETISNoOpt.edgeSet().size() - origEdgeCount;
					int minDeltaEdge = deltaEdgeMETISNoOpt;
					System.out.println("\t\tDelta using METIS, no optimization: " + deltaEdgeMETISNoOpt + " (" + ((double)deltaEdgeMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISNoOpt = cloneNotMETISNoOpt.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISNoOpt < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISNoOpt;
					System.out.println("\t\tDelta not using METIS, no optimization: " + deltaEdgeNotMETISNoOpt + " (" + ((double)deltaEdgeNotMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISRandLocalSearch = cloneMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISRandLocalSearch;
					System.out.println("\t\tDelta using METIS, randomized local search: " + deltaEdgeMETISRandLocalSearch + " (" + ((double)deltaEdgeMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISRandLocalSearch = cloneNotMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISRandLocalSearch;
					System.out.println("\t\tDelta not using METIS, randomized local search: " + deltaEdgeNotMETISRandLocalSearch + " (" + ((double)deltaEdgeNotMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISSimAnnealing = cloneMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISSimAnnealing;
					System.out.println("\t\tDelta using METIS, simulated annealing: " + deltaEdgeMETISSimAnnealing + " (" + ((double)deltaEdgeMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISSimAnnealing = cloneNotMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISSimAnnealing;
					System.out.println("\t\tDelta not using METIS, simulated annealing: " + deltaEdgeNotMETISSimAnnealing + " (" + ((double)deltaEdgeNotMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					
					if (deltaEdgeMETISNoOpt == minDeltaEdge)
						timesMETISNoOptWasBest.get(1).put(k, timesMETISNoOptWasBest.get(1).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt == minDeltaEdge)
						timesNotMETISNoOptWasBest.get(1).put(k, timesNotMETISNoOptWasBest.get(1).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch == minDeltaEdge)
						timesMETISRandLocalSearchWasBest.get(1).put(k, timesMETISRandLocalSearchWasBest.get(1).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch == minDeltaEdge)
						timesNotMETISRandLocalSearchWasBest.get(1).put(k, timesNotMETISRandLocalSearchWasBest.get(1).get(k) + 1);
					if (deltaEdgeMETISSimAnnealing == minDeltaEdge)
						timesMETISSimAnnealingWasBest.get(1).put(k, timesMETISSimAnnealingWasBest.get(1).get(k) + 1);
					if (deltaEdgeNotMETISSimAnnealing == minDeltaEdge)
						timesNotMETISSimAnnealingWasBest.get(1).put(k, timesNotMETISSimAnnealingWasBest.get(1).get(k) + 1);
					
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(1).put(k, timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(1).put(k, timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(1).put(k, timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).put(k, timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch < deltaEdgeMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(1).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch < deltaEdgeNotMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k) + 1);
					
				}
				
				System.out.println("");
				
				System.out.println("Second snapshot");
				
				graph = BarabasiAlbertGraphGenerator.newGraph(225, 0, (SimpleGraph<String, DefaultEdge>)graph, 5);
				
				origEdgeCount = graph.edgeSet().size(); 
				origVertexCount = graph.vertexSet().size();
				
				// Apply the method with k \in ks
				for (int k : ks) {
					
					System.out.println("\tk = " + k);
					
					UndirectedGraph<String, DefaultEdge> cloneMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					
					anonymizersUsingMETISNoOptimization.get(k).anonymizeGraph(cloneMETISNoOpt, false, "TesterMETISNoOpt");
					anonymizersNotUsingMETISNoOptimization.get(k).anonymizeGraph(cloneNotMETISNoOpt, false, "TesterNoMETISNoOpt");
					anonymizersUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneMETISRandLocalSearch, false, "TesterMETISRandLocalSearch");
					anonymizersNotUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneNotMETISRandLocalSearch, false, "TesterNoMETISRandLocalSearch");
					anonymizersUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneMETISSimAnnealing, false, "TesterMETISSimAnnealing");
					anonymizersNotUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneNotMETISSimAnnealing, false, "TesterNoMETISSimAnnealing");
					
					// Report effect of anonymization on the graph
					System.out.println("\t\tOriginal vertex count: " + origVertexCount);
					System.out.println("\t\tFinal vertex count using METIS, no optimization: " + cloneMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, no optimization: " + cloneNotMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, randomized local search: " + cloneMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, simulated annealing: " + cloneMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tOriginal edge count: " + origEdgeCount);
					System.out.println("\t\tFinal edge count using METIS, no optimization: " + cloneMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, no optimization: " + cloneNotMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, randomized local search: " + cloneMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, simulated annealing: " + cloneMETISSimAnnealing.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.edgeSet().size());
					int deltaEdgeMETISNoOpt = cloneMETISNoOpt.edgeSet().size() - origEdgeCount;
					int minDeltaEdge = deltaEdgeMETISNoOpt;
					System.out.println("\t\tDelta using METIS, no optimization: " + deltaEdgeMETISNoOpt + " (" + ((double)deltaEdgeMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISNoOpt = cloneNotMETISNoOpt.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISNoOpt < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISNoOpt;
					System.out.println("\t\tDelta not using METIS, no optimization: " + deltaEdgeNotMETISNoOpt + " (" + ((double)deltaEdgeNotMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISRandLocalSearch = cloneMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISRandLocalSearch;
					System.out.println("\t\tDelta using METIS, randomized local search: " + deltaEdgeMETISRandLocalSearch + " (" + ((double)deltaEdgeMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISRandLocalSearch = cloneNotMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISRandLocalSearch;
					System.out.println("\t\tDelta not using METIS, randomized local search: " + deltaEdgeNotMETISRandLocalSearch + " (" + ((double)deltaEdgeNotMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISSimAnnealing = cloneMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISSimAnnealing;
					System.out.println("\t\tDelta using METIS, simulated annealing: " + deltaEdgeMETISSimAnnealing + " (" + ((double)deltaEdgeMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISSimAnnealing = cloneNotMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISSimAnnealing;
					System.out.println("\t\tDelta not using METIS, simulated annealing: " + deltaEdgeNotMETISSimAnnealing + " (" + ((double)deltaEdgeNotMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					
					if (deltaEdgeMETISNoOpt == minDeltaEdge)
						timesMETISNoOptWasBest.get(2).put(k, timesMETISNoOptWasBest.get(2).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt == minDeltaEdge)
						timesNotMETISNoOptWasBest.get(2).put(k, timesNotMETISNoOptWasBest.get(2).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch == minDeltaEdge)
						timesMETISRandLocalSearchWasBest.get(2).put(k, timesMETISRandLocalSearchWasBest.get(2).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch == minDeltaEdge)
						timesNotMETISRandLocalSearchWasBest.get(2).put(k, timesNotMETISRandLocalSearchWasBest.get(2).get(k) + 1);
					if (deltaEdgeMETISSimAnnealing == minDeltaEdge)
						timesMETISSimAnnealingWasBest.get(2).put(k, timesMETISSimAnnealingWasBest.get(2).get(k) + 1);
					if (deltaEdgeNotMETISSimAnnealing == minDeltaEdge)
						timesNotMETISSimAnnealingWasBest.get(2).put(k, timesNotMETISSimAnnealingWasBest.get(2).get(k) + 1);
					
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(2).put(k, timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(2).put(k, timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(2).put(k, timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).put(k, timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch < deltaEdgeMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(2).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch < deltaEdgeNotMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k) + 1);
					
				}
				
				System.out.println("");
							
				System.out.println("Third snapshot");
				
				graph = BarabasiAlbertGraphGenerator.newGraph(250, 0, (SimpleGraph<String, DefaultEdge>)graph, 5);
				
				origEdgeCount = graph.edgeSet().size(); 
				origVertexCount = graph.vertexSet().size();
				
				// Apply the method with k \in ks
				for (int k : ks) {
					
					System.out.println("\tk = " + k);
					
					UndirectedGraph<String, DefaultEdge> cloneMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					
					anonymizersUsingMETISNoOptimization.get(k).anonymizeGraph(cloneMETISNoOpt, false, "TesterMETISNoOpt");
					anonymizersNotUsingMETISNoOptimization.get(k).anonymizeGraph(cloneNotMETISNoOpt, false, "TesterNoMETISNoOpt");
					anonymizersUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneMETISRandLocalSearch, false, "TesterMETISRandLocalSearch");
					anonymizersNotUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneNotMETISRandLocalSearch, false, "TesterNoMETISRandLocalSearch");
					anonymizersUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneMETISSimAnnealing, false, "TesterMETISSimAnnealing");
					anonymizersNotUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneNotMETISSimAnnealing, false, "TesterNoMETISSimAnnealing");
					
					// Report effect of anonymization on the graph
					System.out.println("\t\tOriginal vertex count: " + origVertexCount);
					System.out.println("\t\tFinal vertex count using METIS, no optimization: " + cloneMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, no optimization: " + cloneNotMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, randomized local search: " + cloneMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, simulated annealing: " + cloneMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tOriginal edge count: " + origEdgeCount);
					System.out.println("\t\tFinal edge count using METIS, no optimization: " + cloneMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, no optimization: " + cloneNotMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, randomized local search: " + cloneMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, simulated annealing: " + cloneMETISSimAnnealing.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.edgeSet().size());
					int deltaEdgeMETISNoOpt = cloneMETISNoOpt.edgeSet().size() - origEdgeCount;
					int minDeltaEdge = deltaEdgeMETISNoOpt;
					System.out.println("\t\tDelta using METIS, no optimization: " + deltaEdgeMETISNoOpt + " (" + ((double)deltaEdgeMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISNoOpt = cloneNotMETISNoOpt.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISNoOpt < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISNoOpt;
					System.out.println("\t\tDelta not using METIS, no optimization: " + deltaEdgeNotMETISNoOpt + " (" + ((double)deltaEdgeNotMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISRandLocalSearch = cloneMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISRandLocalSearch;
					System.out.println("\t\tDelta using METIS, randomized local search: " + deltaEdgeMETISRandLocalSearch + " (" + ((double)deltaEdgeMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISRandLocalSearch = cloneNotMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISRandLocalSearch;
					System.out.println("\t\tDelta not using METIS, randomized local search: " + deltaEdgeNotMETISRandLocalSearch + " (" + ((double)deltaEdgeNotMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISSimAnnealing = cloneMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISSimAnnealing;
					System.out.println("\t\tDelta using METIS, simulated annealing: " + deltaEdgeMETISSimAnnealing + " (" + ((double)deltaEdgeMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISSimAnnealing = cloneNotMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISSimAnnealing;
					System.out.println("\t\tDelta not using METIS, simulated annealing: " + deltaEdgeNotMETISSimAnnealing + " (" + ((double)deltaEdgeNotMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					
					if (deltaEdgeMETISNoOpt == minDeltaEdge)
						timesMETISNoOptWasBest.get(3).put(k, timesMETISNoOptWasBest.get(3).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt == minDeltaEdge)
						timesNotMETISNoOptWasBest.get(3).put(k, timesNotMETISNoOptWasBest.get(3).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch == minDeltaEdge)
						timesMETISRandLocalSearchWasBest.get(3).put(k, timesMETISRandLocalSearchWasBest.get(3).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch == minDeltaEdge)
						timesNotMETISRandLocalSearchWasBest.get(3).put(k, timesNotMETISRandLocalSearchWasBest.get(3).get(k) + 1);
					if (deltaEdgeMETISSimAnnealing == minDeltaEdge)
						timesMETISSimAnnealingWasBest.get(3).put(k, timesMETISSimAnnealingWasBest.get(3).get(k) + 1);
					if (deltaEdgeNotMETISSimAnnealing == minDeltaEdge)
						timesNotMETISSimAnnealingWasBest.get(3).put(k, timesNotMETISSimAnnealingWasBest.get(3).get(k) + 1);
					
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(3).put(k, timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(3).put(k, timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(3).put(k, timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).put(k, timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch < deltaEdgeMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(3).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch < deltaEdgeNotMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k) + 1);
					
				}
				
				System.out.println("");
				System.out.println("Summary so far:");
				
				System.out.println("");
				System.out.println("\tNumber of times using METIS and applying no optimization was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISNoOptWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISNoOptWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISNoOptWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISNoOptWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISNoOptWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISNoOptWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times not using METIS and applying no optimization was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISNoOptWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISNoOptWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISNoOptWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISNoOptWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISNoOptWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISNoOptWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
								
				System.out.println("");
				System.out.println("\tNumber of times using METIS and applying randomized local search was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISRandLocalSearchWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISRandLocalSearchWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");				
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISRandLocalSearchWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISRandLocalSearchWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");				
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISRandLocalSearchWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISRandLocalSearchWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times not using METIS and applying randomized local search was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISRandLocalSearchWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISRandLocalSearchWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISRandLocalSearchWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISRandLocalSearchWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");				
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISRandLocalSearchWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISRandLocalSearchWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times using METIS and applying simulated annealing was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISSimAnnealingWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISSimAnnealingWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISSimAnnealingWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISSimAnnealingWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISSimAnnealingWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISSimAnnealingWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times not using METIS and applying simulated annealing was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISSimAnnealingWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISSimAnnealingWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISSimAnnealingWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISSimAnnealingWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISSimAnnealingWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISSimAnnealingWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
								
				System.out.println("");
				System.out.println("\tNumber of times using no optimization was strictly better than randomized local search:");
				System.out.println("\t\tUsing METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tNot using METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times using no optimization was strictly better than simulated annealing:");
				System.out.println("\t\tUsing METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");				
				System.out.println("\t\tNot using METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times using randomized local search was strictly better than simulated annealing:");
				System.out.println("\t\tUsing METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tNot using METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
			}
			
			System.out.println("METIS failed " + metisFailureCount + " times");
			System.out.println();
			
		}
	}
	
}
