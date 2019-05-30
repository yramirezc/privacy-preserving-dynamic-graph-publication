package attacks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import anonymization.AdjacencyAnonymizer;
import anonymization.OddCycle;
import real.FacebookEgoNetwork;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;
import util.BarabasiAlbertGraphGenerator;
import util.GraphUtil;
import util.Statistics;
import util.WattsStrogatzGraphGenerator;

public class ExperimentsRobustSybils {
	
	//==================================================================================================================
	
	// On Erdos-Renyi random graphs
	
	public static void experimentRobustSybilsErdosRenyiRandomGraphs(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 200;
		
		//double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		int attackType = 0;   // Run the original walk-based attack by default 
		
		int attackerCounts[] = new int[]{1,4,8,16};
		
		//int editDistances[] = new int[]{1,2,5,10,20,50};
		int editDistances[] = new int[]{4};
		
		if (args.length == 5) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackType = Integer.parseInt(args[2]);
			attackerCounts = new int[1];
			attackerCounts[0] = Integer.parseInt(args[3]);
			editDistances = new int[1];
			editDistances[0] = Integer.parseInt(args[4]);
		}   // else the defaults defined above are taken
		
		//String expNamePrefix = "Exp3";
		String expNamePrefix = "Exp4"; 
		
		for (double density : densities) {
			for (int attackerCount : attackerCounts) { 
				for (int editDist : editDistances) {
					int edgenum = getEdgeNum(vernum, density);				
					String fileNameOutOriginalWalkBased = expNamePrefix + "-Rob-Syb-Original-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutDistAnonymizationWalkBased = expNamePrefix + "-Rob-Syb-Dist-Anonymization-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutDistTransformationWalkBased = expNamePrefix + "-Rob-Syb-Dist-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutAdjTransformationWalkBased = expNamePrefix + "-Rob-Syb-Adj-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNamesOutsRandomPerturbations = expNamePrefix + "-Rob-Syb-Random-Perturbation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;					
					oneRunExperimentRobustSybilsErdosRenyiRandomGraphs(vernum, edgenum, attackType, attackerCount, editDist, fileNameOutOriginalWalkBased, fileNameOutDistAnonymizationWalkBased, fileNameOutDistTransformationWalkBased, fileNameOutAdjTransformationWalkBased, fileNamesOutsRandomPerturbations);
				}
			}
		}
	}
	
	public static int getEdgeNum(int vexnum , double density) {
		return (int)(density*vexnum*(vexnum-1)/2);
	}
	
	public static void oneRunExperimentRobustSybilsErdosRenyiRandomGraphs(int n, int m, int attackType, int attackerCount, int maxEditDist, String fileNameOutOriginalWalkBased,
			String fileNameOutDistAnonymizationWalkBased, String fileNameOutDistTransformationWalkBased, String fileNameOutAdjTransformationWalkBased, 
			String fileNamePrefixesOutsRandomPerturbations) throws NoSuchAlgorithmException, IOException {
		
		if (attackerCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outDistAnonymizationWalkBased = new FileWriter(fileNameOutDistAnonymizationWalkBased+".DAT", true);
		Writer outDistTransformationWalkBased = new FileWriter(fileNameOutDistTransformationWalkBased+".DAT", true);
		Writer outAdjTransformationWalkBased = new FileWriter(fileNameOutAdjTransformationWalkBased+".DAT", true);
		
		//int percentages[] = new int[] {1, 5, 10, 15, 20, 25};
		int percentages[] = new int[] {1, 5, 10};
		String[] fileNamesOutsRandomPerturbations = new String[percentages.length];
		Writer[] outsRandomPerturbations = new Writer[percentages.length];
		for (int pct = 0; pct < percentages.length; pct++) {
			fileNamesOutsRandomPerturbations[pct] = fileNamePrefixesOutsRandomPerturbations + '-' + percentages[pct] + "-pct-Flips";
			outsRandomPerturbations[pct] = new FileWriter(fileNamesOutsRandomPerturbations[pct] + ".DAT", true);
		}
		
		final int startingVertex = attackerCount;
		
		int victimCount = attackerCount;
		if (victimCount == 0)
			victimCount = 1;
		
		if (attackerCount + victimCount > n)
			victimCount = n - attackerCount;
		
		SybilAttackSimulator attackSimulator = null;
		
		switch (attackType) {
		case 0:   // Original walk-based attack
			attackSimulator = new OriginalWalkBasedAttackSimulator();
			break;
		case 1:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true);
			break;
		case 2:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, true);
			break;
		case 3:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, false);
			break;
		case 4:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, false);
			break;
		case 5:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, true);
			break;
		case 6:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, true);
			break;			
		case 7:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, false);
			break;
		case 8:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, false);
			break;
		case 9:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, uniformly distributed fingerprints: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true, false);   
			break;
		default:
			break;
		} 
		
		if (attackSimulator != null) {
			
			int total = 100000;
			
			for (int i = 1; i <= total ; i++) {
				
				UndirectedGraph<String, DefaultEdge> walkBasedAttackedGraph = null;
				ConnectivityInspector<String, DefaultEdge> connInspector = null;
				
				do {
					
					VertexFactory<String> vertexFactory = new VertexFactory<String>() {
						int i = startingVertex;
						
						@Override
						public String createVertex() {
							int result = i;
							i++;
							return result+"";
						}
						
					};
					
					walkBasedAttackedGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
					RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);			
					generator.generateGraph(walkBasedAttackedGraph, vertexFactory, null);
					
					attackSimulator.simulateAttackerSubgraphCreation(walkBasedAttackedGraph, attackerCount, victimCount);
					
					connInspector = new ConnectivityInspector<>(walkBasedAttackedGraph);
					
				} while (!connInspector.isGraphConnected());
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalWalkBasedAttackedGraph = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);
				Statistics.printStatisticsRobustSybilsExp(i, outOriginalWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph, fileNameOutOriginalWalkBased, attackerCount, victimCount, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// Random perturbations
				
				for (int pct = 0; pct < percentages.length; pct++) {
					SimpleGraph<String, DefaultEdge> randomlyPerturbedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
					int flipCount = (int)(((double)(percentages[pct]) / 100d) * (double)(walkBasedAttackedGraph.vertexSet().size() * (walkBasedAttackedGraph.vertexSet().size() - 1) / 2));
					GraphUtil.flipRandomEdges(flipCount, randomlyPerturbedGraphWalkBased);
					
					FloydWarshallShortestPaths<String, DefaultEdge> floydRandomlyPerturtbedGraph = new FloydWarshallShortestPaths<>(randomlyPerturbedGraphWalkBased);			
					Statistics.printStatisticsRobustSybilsExp(i, outsRandomPerturbations[pct], randomlyPerturbedGraphWalkBased, floydRandomlyPerturtbedGraph, fileNamesOutsRandomPerturbations[pct], attackerCount, victimCount, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				}
				
				// Original (>1,>1)-anonymity method
				
				SimpleGraph<String, DefaultEdge> anonDistAnonymizedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				OddCycle.anonymizeGraph(anonDistAnonymizedGraphWalkBased, floydOriginalWalkBasedAttackedGraph, 3);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydDistAnonymizedGraph = new FloydWarshallShortestPaths<>(anonDistAnonymizedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outDistAnonymizationWalkBased, anonDistAnonymizedGraphWalkBased, floydDistAnonymizedGraph, fileNameOutDistAnonymizationWalkBased, attackerCount, victimCount, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// (>1,\Gamma_1)-anonymity
				
				SimpleGraph<String, DefaultEdge> anonDistTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				OddCycle.anonymousTransformation(anonDistTransformedGraphWalkBased, floydOriginalWalkBasedAttackedGraph);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydDistTransformedGraph = new FloydWarshallShortestPaths<>(anonDistTransformedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outDistTransformationWalkBased, anonDistTransformedGraphWalkBased, floydDistTransformedGraph, fileNameOutDistTransformationWalkBased, attackerCount, victimCount, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// (attackersCount,\Gamma_1)-adjacency anonymity
				
				SimpleGraph<String, DefaultEdge> anonAdjTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				//AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, 2);
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, attackerCount);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydAdjTransformedGraph = new FloydWarshallShortestPaths<>(anonAdjTransformedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outAdjTransformationWalkBased, anonAdjTransformedGraphWalkBased, floydAdjTransformedGraph, fileNameOutAdjTransformationWalkBased, attackerCount, victimCount, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			}
			
			outOriginalWalkBased.close();
			outDistAnonymizationWalkBased.close();
			outDistTransformationWalkBased.close();
			outAdjTransformationWalkBased.close();
			for (int ro = 0; ro < percentages.length; ro++)
				outsRandomPerturbations[ro].close();
		}
	}
	
	//==================================================================================================================
	
	// On Barabasi-Albert scale-free random graphs
	
	public static void experimentRobustSybilsBarabasiAlbertRandomGraphs(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 200;
		
		int m0 = 20;
		
		int[] mValues = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		
		int seedTypeId = 3;   // By default, the seed graph is an ER random graph with density 0.5
		
		int attackType = 0;   // Run the original walk-based attack by default
		
		int attackerCounts[] = new int[]{1,4,8,16};
		
		int editDistances[] = new int[]{4, 8};
		
		if (args.length == 7) {
			vernum = Integer.parseInt(args[0]);
			m0 = Integer.parseInt(args[1]);
			mValues = new int[1];
			mValues[0] = Integer.parseInt(args[2]);			
			seedTypeId = Integer.parseInt(args[3]);
			attackType = Integer.parseInt(args[4]);
			attackerCounts = new int[1];
			attackerCounts[0] = Integer.parseInt(args[5]);
			editDistances = new int[1];
			editDistances[0] = Integer.parseInt(args[6]);
		}   // else the defaults defined above are taken
		
		String expNamePrefix = "Exp4"; 
		
		for (int m : mValues) {
			for (int attackerCount : attackerCounts) {
				for (int editDist : editDistances) {				
					String fileNameOutOriginalWalkBased = expNamePrefix + "-Rob-Syb-Original-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutDistAnonymizationWalkBased = expNamePrefix + "-Rob-Syb-Dist-Anonymization-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutDistTransformationWalkBased = expNamePrefix + "-Rob-Syb-Dist-Transformation-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutAdjTransformationWalkBased = expNamePrefix + "-Rob-Syb-Adj-Transformation-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNamesOutsRandomPerturbations = expNamePrefix + "-Rob-Syb-Random-Perturbation-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;					
					oneRunExperimentRobustSybilsBarabasiAlbertRandomGraphs(vernum, m0, m, seedTypeId, attackType, attackerCount, editDist, fileNameOutOriginalWalkBased, fileNameOutDistAnonymizationWalkBased, fileNameOutDistTransformationWalkBased, fileNameOutAdjTransformationWalkBased, fileNamesOutsRandomPerturbations);
				}
			}
		}
	}
	
	public static void oneRunExperimentRobustSybilsBarabasiAlbertRandomGraphs(int n, int m0, int m, int seedTypeId, int attackType, int attackersCount, int maxEditDist, String fileNameOutOriginalWalkBased,
			String fileNameOutDistAnonymizationWalkBased, String fileNameOutDistTransformationWalkBased, String fileNameOutAdjTransformationWalkBased, 
			String fileNamePrefixesOutsRandomPerturbations) throws NoSuchAlgorithmException, IOException {
		
		if (attackersCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outDistAnonymizationWalkBased = new FileWriter(fileNameOutDistAnonymizationWalkBased+".DAT", true);
		Writer outDistTransformationWalkBased = new FileWriter(fileNameOutDistTransformationWalkBased+".DAT", true);
		Writer outAdjTransformationWalkBased = new FileWriter(fileNameOutAdjTransformationWalkBased+".DAT", true);
		
		int percentages[] = new int[] {1, 5, 10};
		String[] fileNamesOutsRandomPerturbations = new String[percentages.length];
		Writer[] outsRandomPerturbations = new Writer[percentages.length];
		for (int pct = 0; pct < percentages.length; pct++) {
			fileNamesOutsRandomPerturbations[pct] = fileNamePrefixesOutsRandomPerturbations + '-' + percentages[pct] + "-pct-Flips";
			outsRandomPerturbations[pct] = new FileWriter(fileNamesOutsRandomPerturbations[pct] + ".DAT", true);
		}
		
		int victimsCountWalkBased = attackersCount;
		if (victimsCountWalkBased == 0)
			victimsCountWalkBased = 1;
		
		if (attackersCount + victimsCountWalkBased > n)
			victimsCountWalkBased = n - attackersCount;
		
		SybilAttackSimulator attackSimulator = null;
		
		switch (attackType) {
		case 0:   // Original walk-based attack
			attackSimulator = new OriginalWalkBasedAttackSimulator();
			break;
		case 1:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true);
			break;
		case 2:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, true);
			break;
		case 3:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, false);
			break;
		case 4:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, false);
			break;
		case 5:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, true);
			break;
		case 6:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, true);
			break;			
		case 7:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, false);
			break;
		case 8:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, false);
			break;
		case 9:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, uniformly distributed fingerprints: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true, false);
			break;
		default:
			break;
		} 
		
		if (attackSimulator != null) {
			
			int total = 100000;
			
			for (int i = 1; i <= total ; i++) {
				
				UndirectedGraph<String, DefaultEdge> walkBasedAttackedGraph = null;
				ConnectivityInspector<String, DefaultEdge> connInspector = null;
				
				do {
					
					UndirectedGraph<String, DefaultEdge> origBAGraph = BarabasiAlbertGraphGenerator.newGraph(n, 0, m0, m, seedTypeId);   // Giving 0 as firstVertId because the shift&shuffle method called afterwards will shift the vertex ids 
					
					// This method is called to guarantee that in different runs the victims are different types of vertices 
					// (in terms of their degrees)
					walkBasedAttackedGraph = GraphUtil.shiftAndShuffleVertexIds(origBAGraph, attackersCount, origBAGraph.vertexSet());    
					
					attackSimulator.simulateAttackerSubgraphCreation(walkBasedAttackedGraph, attackersCount, victimsCountWalkBased);
					
					connInspector = new ConnectivityInspector<>(walkBasedAttackedGraph);
					
				} while (!connInspector.isGraphConnected());
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalWalkBasedAttackedGraph = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);
				Statistics.printStatisticsRobustSybilsExp(i, outOriginalWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph, fileNameOutOriginalWalkBased, attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// Random perturbations
				
				for (int pct = 0; pct < percentages.length; pct++) {
					SimpleGraph<String, DefaultEdge> randomlyPerturbedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
					int flipCount = (int)(((double)(percentages[pct]) / 100d) * (double)(walkBasedAttackedGraph.vertexSet().size() * (walkBasedAttackedGraph.vertexSet().size() - 1) / 2));
					GraphUtil.flipRandomEdges(flipCount, randomlyPerturbedGraphWalkBased);
					
					FloydWarshallShortestPaths<String, DefaultEdge> floydRandomlyPerturtbedGraph = new FloydWarshallShortestPaths<>(randomlyPerturbedGraphWalkBased);			
					Statistics.printStatisticsRobustSybilsExp(i, outsRandomPerturbations[pct], randomlyPerturbedGraphWalkBased, floydRandomlyPerturtbedGraph, fileNamesOutsRandomPerturbations[pct], attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				}
				
				// Original (>1,>1)-anonymity method
				
				SimpleGraph<String, DefaultEdge> anonDistAnonymizedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				OddCycle.anonymizeGraph(anonDistAnonymizedGraphWalkBased, floydOriginalWalkBasedAttackedGraph, 3);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydDistAnonymizedGraph = new FloydWarshallShortestPaths<>(anonDistAnonymizedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outDistAnonymizationWalkBased, anonDistAnonymizedGraphWalkBased, floydDistAnonymizedGraph, fileNameOutDistAnonymizationWalkBased, attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// (>1,\Gamma_1)-anonymity
				
				SimpleGraph<String, DefaultEdge> anonDistTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				OddCycle.anonymousTransformation(anonDistTransformedGraphWalkBased, floydOriginalWalkBasedAttackedGraph);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydDistTransformedGraph = new FloydWarshallShortestPaths<>(anonDistTransformedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outDistTransformationWalkBased, anonDistTransformedGraphWalkBased, floydDistTransformedGraph, fileNameOutDistTransformationWalkBased, attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// (attackersCount,\Gamma_1)-adjacency anonymity
				
				SimpleGraph<String, DefaultEdge> anonAdjTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				//AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, 2);
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, attackersCount);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydAdjTransformedGraph = new FloydWarshallShortestPaths<>(anonAdjTransformedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outAdjTransformationWalkBased, anonAdjTransformedGraphWalkBased, floydAdjTransformedGraph, fileNameOutAdjTransformationWalkBased, attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			}
			
			outOriginalWalkBased.close();
			outDistAnonymizationWalkBased.close();
			outDistTransformationWalkBased.close();
			outAdjTransformationWalkBased.close();
			for (int ro = 0; ro < percentages.length; ro++)
				outsRandomPerturbations[ro].close();
		}
	}
	
	//==================================================================================================================
	
	// On Watts-Strogatz small-world random graphs
	
	public static void experimentRobustSybilsWattsStrogatzRandomGraphs(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 200;
		
		double[] rhos = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		
		int[] ks = {20, 40, 60, 80, 100};   // Approximately inducing densities 0.1, 0.2, ... , 0.5
		
		int attackType = 0;   // Run the original walk-based attack by default
		
		int attackerCounts[] = new int[]{1,4,8,16};
		
		int editDistances[] = new int[]{4,8};
		
		if (args.length == 6) {
			vernum = Integer.parseInt(args[0]);
			ks = new int[1];
			ks[0] = Integer.parseInt(args[1]);
			rhos = new double[1];
			rhos[0] = Double.parseDouble(args[2]);
			attackType = Integer.parseInt(args[3]);
			attackerCounts = new int[1];
			attackerCounts[0] = Integer.parseInt(args[4]);
			editDistances = new int[1];
			editDistances[0] = Integer.parseInt(args[5]);
		}   // else the defaults defined above are taken
		
		String expNamePrefix = "Exp4"; 
		
		for (int k : ks) {
			for (double rho : rhos) {
				for (int attackerCount : attackerCounts) {
					for (int editDist : editDistances) {				
						String fileNameOutOriginalWalkBased = expNamePrefix + "-Rob-Syb-Original-V-" + vernum + "-K-" + k + "-Rho-" + rho + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
						String fileNameOutDistAnonymizationWalkBased = expNamePrefix + "-Rob-Syb-Dist-Anonymization-V-" + vernum + "-K-" + k + "-Rho-" + rho + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
						String fileNameOutDistTransformationWalkBased = expNamePrefix + "-Rob-Syb-Dist-Transformation-V-" + vernum + "-K-" + k + "-Rho-" + rho + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
						String fileNameOutAdjTransformationWalkBased = expNamePrefix + "-Rob-Syb-Adj-Transformation-V-" + vernum + "-K-" + k + "-Rho-" + rho + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
						String fileNamesOutsRandomPerturbations = expNamePrefix + "-Rob-Syb-Random-Perturbation-V-" + vernum + "-K-" + k + "-Rho-" + rho + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;					
						oneRunExperimentRobustSybilsWattsStrogatzRandomGraphs(vernum, k, rho, attackType, attackerCount, editDist, fileNameOutOriginalWalkBased, fileNameOutDistAnonymizationWalkBased, fileNameOutDistTransformationWalkBased, fileNameOutAdjTransformationWalkBased, fileNamesOutsRandomPerturbations);
					}
				}
			}
		}
	}
	
	public static void oneRunExperimentRobustSybilsWattsStrogatzRandomGraphs(int n, int k, double rho, int attackType, int attackersCount, int maxEditDist, String fileNameOutOriginalWalkBased,
			String fileNameOutDistAnonymizationWalkBased, String fileNameOutDistTransformationWalkBased, String fileNameOutAdjTransformationWalkBased, 
			String fileNamePrefixesOutsRandomPerturbations) throws NoSuchAlgorithmException, IOException {
		
		if (attackersCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outDistAnonymizationWalkBased = new FileWriter(fileNameOutDistAnonymizationWalkBased+".DAT", true);
		Writer outDistTransformationWalkBased = new FileWriter(fileNameOutDistTransformationWalkBased+".DAT", true);
		Writer outAdjTransformationWalkBased = new FileWriter(fileNameOutAdjTransformationWalkBased+".DAT", true);
		
		int percentages[] = new int[] {1, 5, 10};
		String[] fileNamesOutsRandomPerturbations = new String[percentages.length];
		Writer[] outsRandomPerturbations = new Writer[percentages.length];
		for (int pct = 0; pct < percentages.length; pct++) {
			fileNamesOutsRandomPerturbations[pct] = fileNamePrefixesOutsRandomPerturbations + '-' + percentages[pct] + "-pct-Flips";
			outsRandomPerturbations[pct] = new FileWriter(fileNamesOutsRandomPerturbations[pct] + ".DAT", true);
		}
		
		final int startingVertex = attackersCount;
		
		int victimsCountWalkBased = attackersCount;
		if (victimsCountWalkBased == 0)
			victimsCountWalkBased = 1;
		
		if (attackersCount + victimsCountWalkBased > n)
			victimsCountWalkBased = n - attackersCount;
		
		SybilAttackSimulator attackSimulator = null;
		
		switch (attackType) {
		case 0:   // Original walk-based attack
			attackSimulator = new OriginalWalkBasedAttackSimulator();
			break;
		case 1:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true);
			break;
		case 2:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, true);
			break;
		case 3:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, false);
			break;
		case 4:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, false);
			break;
		case 5:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, true);
			break;
		case 6:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, true);
			break;			
		case 7:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, false);
			break;
		case 8:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, false);
			break;
		case 9:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, uniformly distributed fingerprints: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true, false);
			break;
		default:
			break;
		} 
		
		if (attackSimulator != null) {
			
			int total = 100000;
			
			for (int i = 1; i <= total ; i++) {
				
				UndirectedGraph<String, DefaultEdge> walkBasedAttackedGraph = null;
				ConnectivityInspector<String, DefaultEdge> connInspector = null;
				
				do {
					
					walkBasedAttackedGraph = WattsStrogatzGraphGenerator.newGraph(n, startingVertex, k, rho);
					
					attackSimulator.simulateAttackerSubgraphCreation(walkBasedAttackedGraph, attackersCount, victimsCountWalkBased);
					
					connInspector = new ConnectivityInspector<>(walkBasedAttackedGraph);
					
				} while (!connInspector.isGraphConnected());
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalWalkBasedAttackedGraph = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);
				Statistics.printStatisticsRobustSybilsExp(i, outOriginalWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph, fileNameOutOriginalWalkBased, attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// Random perturbations
				
				for (int pct = 0; pct < percentages.length; pct++) {
					SimpleGraph<String, DefaultEdge> randomlyPerturbedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
					int flipCount = (int)(((double)(percentages[pct]) / 100d) * (double)(walkBasedAttackedGraph.vertexSet().size() * (walkBasedAttackedGraph.vertexSet().size() - 1) / 2));
					GraphUtil.flipRandomEdges(flipCount, randomlyPerturbedGraphWalkBased);
					
					FloydWarshallShortestPaths<String, DefaultEdge> floydRandomlyPerturtbedGraph = new FloydWarshallShortestPaths<>(randomlyPerturbedGraphWalkBased);			
					Statistics.printStatisticsRobustSybilsExp(i, outsRandomPerturbations[pct], randomlyPerturbedGraphWalkBased, floydRandomlyPerturtbedGraph, fileNamesOutsRandomPerturbations[pct], attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				}
				
				// Original (>1,>1)-anonymity method
				
				SimpleGraph<String, DefaultEdge> anonDistAnonymizedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				OddCycle.anonymizeGraph(anonDistAnonymizedGraphWalkBased, floydOriginalWalkBasedAttackedGraph, 3);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydDistAnonymizedGraph = new FloydWarshallShortestPaths<>(anonDistAnonymizedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outDistAnonymizationWalkBased, anonDistAnonymizedGraphWalkBased, floydDistAnonymizedGraph, fileNameOutDistAnonymizationWalkBased, attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// (>1,\Gamma_1)-anonymity
				
				SimpleGraph<String, DefaultEdge> anonDistTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				OddCycle.anonymousTransformation(anonDistTransformedGraphWalkBased, floydOriginalWalkBasedAttackedGraph);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydDistTransformedGraph = new FloydWarshallShortestPaths<>(anonDistTransformedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outDistTransformationWalkBased, anonDistTransformedGraphWalkBased, floydDistTransformedGraph, fileNameOutDistTransformationWalkBased, attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// (attackersCount,\Gamma_1)-adjacency anonymity
				
				SimpleGraph<String, DefaultEdge> anonAdjTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				//AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, 2);
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, attackersCount);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydAdjTransformedGraph = new FloydWarshallShortestPaths<>(anonAdjTransformedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outAdjTransformationWalkBased, anonAdjTransformedGraphWalkBased, floydAdjTransformedGraph, fileNameOutAdjTransformationWalkBased, attackersCount, victimsCountWalkBased, attackSimulator, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			}
			
			outOriginalWalkBased.close();
			outDistAnonymizationWalkBased.close();
			outDistTransformationWalkBased.close();
			outAdjTransformationWalkBased.close();
			for (int ro = 0; ro < percentages.length; ro++)
				outsRandomPerturbations[ro].close();
		}
		
	}	
	
	//==================================================================================================================
	
	// On a real social graph (Facebook (full or an ego-net), Panzarasa or URV)
	
	public static void experimentRobustSybilsRealNetworks(String [] args) throws NoSuchAlgorithmException, IOException {
		if (args.length == 4) {
			
			int attackerCount = Integer.parseInt(args[1]);
			int attackType = Integer.parseInt(args[2]);
			int editDistance = Integer.parseInt(args[3]);
			
			if (args[0].equals("-facebook"))
				oneRunExperimentRobustSybilsRealNetworks("facebook", attackerCount, attackType, editDistance);
			else if (args[0].startsWith("-facebook-ego-net-"))
				oneRunExperimentRobustSybilsRealNetworks(args[0].substring(1), attackerCount, attackType, editDistance);
			else if (args[0].equals("-panzarasa"))
				oneRunExperimentRobustSybilsRealNetworks("panzarasa", attackerCount, attackType, editDistance);
			else if (args[0].equals("-urv"))
				oneRunExperimentRobustSybilsRealNetworks("urv", attackerCount, attackType, editDistance);
		}
	}
	
	static void oneRunExperimentRobustSybilsRealNetworks(String networkName, int attackerCount, int attackType, int maxEditDist) throws IOException {
		
		String expNamePrefix = "Exp4";
		
		String fileNameOutOriginal = expNamePrefix + "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-original";
		String fileNameOutDistAnonymized = expNamePrefix + "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-dist-anonymized";
		String fileNameOutDistTransformed = expNamePrefix + "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-dist-transformed";
		String fileNameOutAdjTransformedK2 = expNamePrefix + "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-adj-transformed-k-2";
		String fileNameOutAdjTransformedKAttCnt = expNamePrefix + "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-adj-transformed-k-" + attackerCount;
		
//		String [] fileNamesOutAdjTransformed = new String[7];
//		for (int k = 2; k < 9; k++)
//			fileNamesOutAdjTransformed[k-2] = networkName + "-walk-based-" + attackerCount + "-adj-transformed-k-" + k;
		
		Writer outOriginal = new FileWriter(fileNameOutOriginal + ".dat", true);
		Writer outDistAnonymized = new FileWriter(fileNameOutDistAnonymized + ".dat", true);
		Writer outDistTransformed = new FileWriter(fileNameOutDistTransformed + ".dat", true);
		Writer outAdjTransformedK2 = new FileWriter(fileNameOutAdjTransformedK2 + ".dat", true);
		Writer outAdjTransformedKAttCnt = new FileWriter(fileNameOutAdjTransformedKAttCnt + ".dat", true);
		
//		Writer [] outsAdjTransformed = new Writer[7];
//		for (int k = 2; k < 9; k++)
//			outsAdjTransformed[k-2] = new FileWriter(fileNamesOutAdjTransformed[k-2] + ".dat", true);
		
		int percentages[] = new int[] {1, 5, 10};
		String[] fileNamesOutsRandomPerturbations = new String[percentages.length];
		Writer[] outsRandomPerturbations = new Writer[percentages.length];
		for (int pct = 0; pct < percentages.length; pct++) {
			fileNamesOutsRandomPerturbations[pct] = expNamePrefix + "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-Random-Perturbation-" + percentages[pct] + "-pct-Flips";
			outsRandomPerturbations[pct] = new FileWriter(fileNamesOutsRandomPerturbations[pct] + ".dat", true);
		}
		
		UndirectedGraph<String, DefaultEdge> graph = null; 
				
		if (networkName.equals("facebook")) {
			graph = new FacebookGraph(DefaultEdge.class); 
		} else if (networkName.startsWith("facebook-ego-net-")) {
			graph = new FacebookEgoNetwork(DefaultEdge.class, networkName.substring(17));
		} else if (networkName.equals("panzarasa")) {
			graph = new PanzarasaGraph(DefaultEdge.class);
		} else if (networkName.equals("urv")) { 
			graph = new URVMailGraph(DefaultEdge.class);
		}
		
		ConnectivityInspector<String, DefaultEdge> connectivity = new ConnectivityInspector<>(graph);
		List<Set<String>> connComp = connectivity.connectedSets();
		
		Set<String> verticesToKeep = null;
		int maximum = 0;
		for (Set<String> comp : connComp) {
			if (comp.size() > maximum) {
				maximum = comp.size();
				verticesToKeep = new HashSet<String>(comp);
			}
		}
		
		int victimCount = attackerCount;
		
		SybilAttackSimulator attackSimulator = null;
		
		switch (attackType) {
		case 0:   // Original walk-based attack
			attackSimulator = new OriginalWalkBasedAttackSimulator();
			break;
		case 1:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true);
			break;
		case 2:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, true);
			break;
		case 3:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, false);
			break;
		case 4:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, false);
			break;
		case 5:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, true);
			break;
		case 6:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, true);
			break;			
		case 7:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, false);
			break;
		case 8:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, false);
			break;
		case 9:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, uniformly distributed fingerprints: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true, false);
			break;
		default:
			break;
		}
		
		for (int i = 0; i < 100000; i++) {	 // Although it is the same graph, every iteration shuffles its vertex set, so the attacker targets different victims 
		
			UndirectedGraph<String, DefaultEdge> attackedGraph = null;
			
			do {
				
				attackedGraph = GraphUtil.shiftAndShuffleVertexIds(graph, attackerCount, verticesToKeep);
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				if (!connectivity.isGraphConnected()) 
					throw new RuntimeException();
				
				victimCount = attackerCount;
				
				if (victimCount == 0)
					victimCount = 1;
				
				if (attackerCount + victimCount > attackedGraph.vertexSet().size())
					victimCount = attackedGraph.vertexSet().size() - attackerCount;
				
				attackSimulator.simulateAttackerSubgraphCreation(attackedGraph, attackerCount, victimCount);
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				
			} while (!connectivity.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(attackedGraph);
			Statistics.printStatisticsRobustSybilsExp(i, outOriginal, attackedGraph, floydOriginal, fileNameOutOriginal, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			// Random perturbations
			
			for (int pct = 0; pct < percentages.length; pct++) {
				SimpleGraph<String, DefaultEdge> randomlyPerturbedGraphWalkBased = GraphUtil.cloneGraph(attackedGraph);
				int flipCount = (int)(((double)(percentages[pct]) / 100d) * (double)(attackedGraph.vertexSet().size() * (attackedGraph.vertexSet().size() - 1) / 2));
				GraphUtil.flipRandomEdges(flipCount, randomlyPerturbedGraphWalkBased);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandomlyPerturtbedGraph = new FloydWarshallShortestPaths<>(randomlyPerturbedGraphWalkBased);			
				Statistics.printStatisticsRobustSybilsExp(i, outsRandomPerturbations[pct], randomlyPerturbedGraphWalkBased, floydRandomlyPerturtbedGraph, fileNamesOutsRandomPerturbations[pct], attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			}
			
			// (2,Gamma_{G,1})-adjacency anonymity
//			for (int k = 2; k < 9; k++) {
			SimpleGraph<String, DefaultEdge> graph2AdjTransformed = GraphUtil.cloneGraph(attackedGraph);
			AdjacencyAnonymizer.k1AdjAnonymousTransformation(graph2AdjTransformed, 2);
			FloydWarshallShortestPaths<String, DefaultEdge> floyd2AdjTransformed = new FloydWarshallShortestPaths<>(graph2AdjTransformed);
			Statistics.printStatisticsRobustSybilsExp(i, outAdjTransformedK2, graph2AdjTransformed, floyd2AdjTransformed, fileNameOutAdjTransformedK2, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
//			}
			
			// (attackerCount, Gamma_{G,1})-adjacency anonymity
//			for (int k = 2; k < 9; k++) {
			SimpleGraph<String, DefaultEdge> graphAttCntAdjTransformed = GraphUtil.cloneGraph(attackedGraph);
			AdjacencyAnonymizer.k1AdjAnonymousTransformation(graphAttCntAdjTransformed, attackerCount);
			FloydWarshallShortestPaths<String, DefaultEdge> floydAttCntAdjTransformed = new FloydWarshallShortestPaths<>(graphAttCntAdjTransformed);
			Statistics.printStatisticsRobustSybilsExp(i, outAdjTransformedKAttCnt, graphAttCntAdjTransformed, floydAttCntAdjTransformed, fileNameOutAdjTransformedKAttCnt, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
//			}
			
			// (2,Gamma_{G,1})-anonymity
			
			SimpleGraph<String, DefaultEdge> graphDistTransformed = GraphUtil.cloneGraph(attackedGraph);
			OddCycle.anonymousTransformation(graphDistTransformed, floydOriginal);
			FloydWarshallShortestPaths<String, DefaultEdge> floydDistTransformed = new FloydWarshallShortestPaths<>(graphDistTransformed);
			Statistics.printStatisticsRobustSybilsExp(i, outDistTransformed, graphDistTransformed, floydDistTransformed, fileNameOutDistTransformed, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			// (>1,>1)-anonymity
			
			SimpleGraph<String, DefaultEdge> graphDistAnonymized = GraphUtil.cloneGraph(attackedGraph);
			OddCycle.anonymizeGraph(graphDistAnonymized, floydOriginal, 3);
			FloydWarshallShortestPaths<String, DefaultEdge> floydDistAnonymized = new FloydWarshallShortestPaths<>(graphDistAnonymized);
			Statistics.printStatisticsRobustSybilsExp(i, outDistAnonymized, graphDistAnonymized, floydDistAnonymized, fileNameOutDistAnonymized, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
		}
		
		outOriginal.close();
		outDistAnonymized.close();
		outDistTransformed.close();
		outAdjTransformedK2.close();
		outAdjTransformedKAttCnt.close();
//		for (int k = 2; k < 9; k++)
//			outsAdjTransformed[k-2].close();
		for (int ro = 0; ro < percentages.length; ro++)
			outsRandomPerturbations[ro].close();
		
	}
	
	//==================================================================================================================
		
	// On a real social graph (Facebook, Panzarasa or URV)
	
	public static void experimentRobustSybilsRealNetworksOnePerturbation(String [] args) throws NoSuchAlgorithmException, IOException {
		if (args.length == 5) {
			
			int attackerCount = Integer.parseInt(args[1]);
			int attackType = Integer.parseInt(args[2]);
			int editDistance = Integer.parseInt(args[3]);
			int perturbationMethodId = Integer.parseInt(args[4]);
			
			if (args[0].equals("-facebook"))
				oneRunExperimentRobustSybilsRealNetworksOnePerturbation("facebook", attackerCount, attackType, editDistance, perturbationMethodId);
			else if (args[0].equals("-panzarasa"))
				oneRunExperimentRobustSybilsRealNetworksOnePerturbation("panzarasa", attackerCount, attackType, editDistance, perturbationMethodId);
			else if (args[0].equals("-urv"))
				oneRunExperimentRobustSybilsRealNetworksOnePerturbation("urv", attackerCount, attackType, editDistance, perturbationMethodId);
		}
	}
		
	static void oneRunExperimentRobustSybilsRealNetworksOnePerturbation(String networkName, int attackerCount, int attackType, int maxEditDist, int perturbationMethodId) throws IOException {
		
		String expNamePrefix = "Exp4";
		
		int victimCount = attackerCount;
		
		SybilAttackSimulator attackSimulator = null;
		
		switch (attackType) {
		case 0:   // Original walk-based attack
			attackSimulator = new OriginalWalkBasedAttackSimulator();
			break;
		case 1:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true);
			break;
		case 2:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, true);
			break;
		case 3:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, false);
			break;
		case 4:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, false, false);
			break;
		case 5:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, true);
			break;
		case 6:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: yes
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, true);
			break;			
		case 7:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, true, false);
			break;
		case 8:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: yes, approximate fingerprint matching: no, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, true, false, false);
			break;
		case 9:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, uniformly distributed fingerprints: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true, false);
			break;
		default:
			break;
		}
		
		UndirectedGraph<String, DefaultEdge> graph = null; 
		
		if (networkName.equals("facebook")) {
			graph = new FacebookGraph(DefaultEdge.class); 
		} else if (networkName.equals("panzarasa")) {
			graph = new PanzarasaGraph(DefaultEdge.class);
		} else if (networkName.equals("urv")) { 
			graph = new URVMailGraph(DefaultEdge.class);
		}
		
		ConnectivityInspector<String, DefaultEdge> connectivity = new ConnectivityInspector<>(graph);
		List<Set<String>> connComp = connectivity.connectedSets();
		
		Set<String> verticesToKeep = null;
		int maximum = 0;
		for (Set<String> comp : connComp) {
			if (comp.size() > maximum) {
				maximum = comp.size();
				verticesToKeep = new HashSet<String>(comp);
			}
		}
		
		String fileNameOutOriginal = expNamePrefix; 
		String fileNameOutPerturbed = expNamePrefix;
		
		switch (perturbationMethodId) {
		case 0:   // (k>1, ell>1)-anonymity
			fileNameOutOriginal += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-orig-4-dist-anonymized"; 
			fileNameOutPerturbed += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-dist-anonymized";
			break;
		case 1:   // (2,Gamma_{G,1})-anonymity			
			fileNameOutOriginal += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-orig-4-dist-transformed";
			fileNameOutPerturbed += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-dist-transformed";
			break;
		case 2:   // (2,Gamma_{G,1})-adjacency anonymity
			fileNameOutOriginal += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-orig-4-adj-transformed-k-2";
			fileNameOutPerturbed += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-adj-transformed-k-2";
			break;
		case 3:   // (attackerCount, Gamma_{G,1})-adjacency anonymity
			fileNameOutOriginal += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-orig-4-adj-transformed-k-" + attackerCount;
			fileNameOutPerturbed += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-adj-transformed-k-" + attackerCount;
			break;
		case 4:   // 1% random perturbation
			fileNameOutOriginal += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-orig-4-rand-pert-1-pct";
			fileNameOutPerturbed += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-rand-pert-1-pct";
			break;
		case 5:   // 5% random perturbation
			fileNameOutOriginal += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-orig-4-rand-pert-5-pct";
			fileNameOutPerturbed += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-rand-pert-5-pct";
			break;
		case 6:   // 10% random perturbation
			fileNameOutOriginal += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-orig-4-rand-pert-10-pct";
			fileNameOutPerturbed += "-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-rand-pert-10-pct";
			break;
		default:;
		}
		
		Writer outOriginal = new FileWriter(fileNameOutOriginal + ".dat", true);
		Writer outPerturbed = new FileWriter(fileNameOutPerturbed + ".dat", true);
		
		for (int i = 0; i < 100000; i++) {	 // Although it is the same graph, every iteration shuffles its vertex set, so the attacker targets different victims 
			
			UndirectedGraph<String, DefaultEdge> attackedGraph = null;
			
			do {
				
				attackedGraph = GraphUtil.shiftAndShuffleVertexIds(graph, attackerCount, verticesToKeep);
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				if (!connectivity.isGraphConnected()) 
					throw new RuntimeException();
				
				victimCount = attackerCount;
				
				if (victimCount == 0)
					victimCount = 1;
				
				if (attackerCount + victimCount > attackedGraph.vertexSet().size())
					victimCount = attackedGraph.vertexSet().size() - attackerCount;
				
				attackSimulator.simulateAttackerSubgraphCreation(attackedGraph, attackerCount, victimCount);
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				
			} while (!connectivity.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(attackedGraph);
			Statistics.printStatisticsRobustSybilsExp(i, outOriginal, attackedGraph, floydOriginal, fileNameOutOriginal, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			int flipCount = 0;
			SimpleGraph<String, DefaultEdge> perturbedGrap = GraphUtil.cloneGraph(attackedGraph);
			FloydWarshallShortestPaths<String, DefaultEdge> floydPerturtbedGraph = null;
			
			switch (perturbationMethodId) {
			case 0:   // (k>1, ell>1)-anonymity
				OddCycle.anonymizeGraph(perturbedGrap, floydOriginal, 3);
				floydPerturtbedGraph = new FloydWarshallShortestPaths<>(perturbedGrap);
				Statistics.printStatisticsRobustSybilsExp(i, outPerturbed, perturbedGrap, floydPerturtbedGraph, fileNameOutPerturbed, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
				break;
			case 1:   // (2,Gamma_{G,1})-anonymity
				OddCycle.anonymousTransformation(perturbedGrap, floydOriginal);
				floydPerturtbedGraph = new FloydWarshallShortestPaths<>(perturbedGrap);
				Statistics.printStatisticsRobustSybilsExp(i, outPerturbed, perturbedGrap, floydPerturtbedGraph, fileNameOutPerturbed, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
				break;
			case 2:   // (2,Gamma_{G,1})-adjacency anonymity
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(perturbedGrap, 2);
				floydPerturtbedGraph = new FloydWarshallShortestPaths<>(perturbedGrap);
				Statistics.printStatisticsRobustSybilsExp(i, outPerturbed, perturbedGrap, floydPerturtbedGraph, fileNameOutPerturbed, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
				break;
			case 3:   // (attackerCount, Gamma_{G,1})-adjacency anonymity
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(perturbedGrap, attackerCount);
				floydPerturtbedGraph = new FloydWarshallShortestPaths<>(perturbedGrap);
				Statistics.printStatisticsRobustSybilsExp(i, outPerturbed, perturbedGrap, floydPerturtbedGraph, fileNameOutPerturbed, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);	
				break;
			case 4:   // 1% random perturbation				
				flipCount = (int)(0.01 * (double)(attackedGraph.vertexSet().size() * (attackedGraph.vertexSet().size() - 1) / 2));
				GraphUtil.flipRandomEdges(flipCount, perturbedGrap);
				floydPerturtbedGraph = new FloydWarshallShortestPaths<>(perturbedGrap);			
				Statistics.printStatisticsRobustSybilsExp(i, outPerturbed, perturbedGrap, floydPerturtbedGraph, fileNameOutPerturbed, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);				
				break;
			case 5:   // 5% random perturbation
				flipCount = (int)(0.05 * (double)(attackedGraph.vertexSet().size() * (attackedGraph.vertexSet().size() - 1) / 2));
				GraphUtil.flipRandomEdges(flipCount, perturbedGrap);
				floydPerturtbedGraph = new FloydWarshallShortestPaths<>(perturbedGrap);			
				Statistics.printStatisticsRobustSybilsExp(i, outPerturbed, perturbedGrap, floydPerturtbedGraph, fileNameOutPerturbed, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);				
				break;
			case 6:   // 10% random perturbation
				flipCount = (int)(0.1 * (double)(attackedGraph.vertexSet().size() * (attackedGraph.vertexSet().size() - 1) / 2));
				GraphUtil.flipRandomEdges(flipCount, perturbedGrap);
				floydPerturtbedGraph = new FloydWarshallShortestPaths<>(perturbedGrap);			
				Statistics.printStatisticsRobustSybilsExp(i, outPerturbed, perturbedGrap, floydPerturtbedGraph, fileNameOutPerturbed, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
				break;
			default:;
			}
		}
		
		outOriginal.close();
		outPerturbed.close();
		
	}
	
	//==================================================================================================================

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		if (args.length == 4)
			experimentRobustSybilsRealNetworks(args);
		else if (args.length == 5)
			experimentRobustSybilsErdosRenyiRandomGraphs(args);
		else if (args.length == 6)
			experimentRobustSybilsWattsStrogatzRandomGraphs(args);
		else if (args.length == 7)
			experimentRobustSybilsBarabasiAlbertRandomGraphs(args);
	}

}
