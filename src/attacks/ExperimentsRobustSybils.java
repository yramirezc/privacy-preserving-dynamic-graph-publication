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
import anonymization.DegreeAnonymityLiuTerzi;
import anonymization.OddCycle;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;
import util.GraphUtil;
import util.Statistics;

public class ExperimentsRobustSybils {
	
	//==================================================================================================================
	
	// On Erdos-Renyi random graphs
	
	public static void experimentRobustSybilsRandomNetworks(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 100;
		
		//double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		int attackType = 0;   // Run the original walk-based attack by default
		//int attackType = 3;
		
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
		
		for (double density : densities) {
			for (int attackerCount : attackerCounts) {
				for (int editDist : editDistances) {
					int edgenum = getEdgeNum(vernum, density);				
					String fileNameOutOriginalWalkBased = "Exp3-Rob-Syb-Original-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutDistAnonymizationWalkBased = "Exp3-Rob-Syb-Dist-Anonymization-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutDistTransformationWalkBased = "Exp3-Rob-Syb-Dist-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutAdjTransformationWalkBased = "Exp3-Rob-Syb-Adj-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNamesOutsRandomPerturbations = "Exp3-Rob-Syb-Random-Perturbation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;					
					oneRunExperimentRobustSybilsRandomNetworks(vernum, edgenum, attackType, attackerCount, editDist, fileNameOutOriginalWalkBased, fileNameOutDistAnonymizationWalkBased, fileNameOutDistTransformationWalkBased, fileNameOutAdjTransformationWalkBased, fileNamesOutsRandomPerturbations);
				}
			}
		}
	}
	
	public static int getEdgeNum(int vexnum , double density) {
		return (int)(density*vexnum*(vexnum-1)/2);
	}
	
	public static void oneRunExperimentRobustSybilsRandomNetworks(int n, int m, int attackType, int attackersCount, int maxEditDist, String fileNameOutOriginalWalkBased,
			String fileNameOutDistAnonymizationWalkBased, String fileNameOutDistTransformationWalkBased, String fileNameOutAdjTransformationWalkBased, 
			String fileNamePrefixesOutsRandomPerturbations) throws NoSuchAlgorithmException, IOException {
		
		if (attackersCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outDistAnonymizationWalkBased = new FileWriter(fileNameOutDistAnonymizationWalkBased+".DAT", true);
		Writer outDistTransformationWalkBased = new FileWriter(fileNameOutDistTransformationWalkBased+".DAT", true);
		Writer outAdjTransformationWalkBased = new FileWriter(fileNameOutAdjTransformationWalkBased+".DAT", true);
		
		int percentages[] = new int[] {1, 5, 10, 25, 50};
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
		default:
			break;
		} 
		
		if (attackSimulator != null) {
			
			//int total = 100000;
			int total = 10000;
			
			for (int i = 1; i <= total ; i++) {
				
				UndirectedGraph<String, DefaultEdge> walkBasedAttackedGraph = null;
				ConnectivityInspector<String, DefaultEdge> connInspector = null;
				
				do {
					
					VertexFactory<String> vertexFactory = new VertexFactory<String>(){
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
				
				// (2,\Gamma_1)-adjacency anonymity
				
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
	
	// On a real social graph (Facebook, Panzarasa or URV)
	
	public static void experimentRobustSybilsRealNetworks(String [] args) throws NoSuchAlgorithmException, IOException {
		if (args.length == 4) {
			
			int attackerCount = Integer.parseInt(args[1]);
			int attackType = Integer.parseInt(args[2]);
			int editDistance = Integer.parseInt(args[3]);
			
			if (args[0].equals("-facebook"))
				oneRunExperimentRobustSybilsRealNetworks("facebook", attackerCount, attackType, editDistance);
			else if (args[0].equals("-panzarasa"))
				oneRunExperimentRobustSybilsRealNetworks("panzarasa", attackerCount, attackType, editDistance);
			else if (args[0].equals("-urv"))
				oneRunExperimentRobustSybilsRealNetworks("urv", attackerCount, attackType, editDistance);
		}
	}
	
	static void oneRunExperimentRobustSybilsRealNetworks(String networkName, int attackerCount, int attackType, int maxEditDist) throws IOException {
		
		String fileNameOutOriginal = "Exp3-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-original";
		String fileNameOutDistAnonymized = "Exp3-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-dist-anonymized";
		String fileNameOutDistTransformed = "Exp3-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-dist-transformed";
		String fileNameOutAdjTransformedK2 = "Exp3-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-adj-transformed-k-2";
		String fileNameOutAdjTransformedKAttCnt = "Exp3-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-adj-transformed-k-" + attackerCount;
		
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
		
		int percentages[] = new int[] {5, 10, 25};
		String[] fileNamesOutsRandomPerturbations = new String[percentages.length];
		Writer[] outsRandomPerturbations = new Writer[percentages.length];
		for (int pct = 0; pct < percentages.length; pct++) {
			fileNamesOutsRandomPerturbations[pct] = "Exp3-Rob-Syb-" + networkName + "-AttackType-" + attackType + "-AttackerCount-" + attackerCount + "-EditDist-" + maxEditDist + "-Random-Perturbation-" + percentages[pct] + "-pct-Flips";
			outsRandomPerturbations[pct] = new FileWriter(fileNamesOutsRandomPerturbations[pct] + ".DAT", true);
		}
		
		UndirectedGraph<String, DefaultEdge> graph = null; 
				
		if (networkName.equals("facebook")) {
			graph = new FacebookGraph(DefaultEdge.class); 
		} else if (networkName.equals("panzarasa")) {
			graph = new PanzarasaGraph(DefaultEdge.class);
		} else {   // URV
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
		default:
			break;
		}
		
		for (int i = 0; i < 1000; i++) {	 // Although it is the same graph, every iteration shuffles its vertex set, so the attacker targets different victims 
		
			UndirectedGraph<String, DefaultEdge> attackedGraph = null;
			
			do {
				
				attackedGraph = GraphUtil.transformRealSocNetIntoOurFormat(graph, attackerCount, verticesToKeep);
				
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

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		if (args.length == 4)
			experimentRobustSybilsRealNetworks(args);
		else
			experimentRobustSybilsRandomNetworks(args);	
	}

}
