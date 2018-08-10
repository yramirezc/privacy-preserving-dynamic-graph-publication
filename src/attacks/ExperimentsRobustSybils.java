package attacks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;

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
import util.GraphUtil;
import util.Statistics;

public class ExperimentsRobustSybils {
	
	public static int getEdgeNum(int vexnum , double density){
		return (int)(density*vexnum*(vexnum-1)/2);
	}
	
	public static void experimentRobustSybils(String [] args) throws NoSuchAlgorithmException, IOException {
		
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
					String fileNameOutOriginalWalkBased = "Exp1-Rob-Syb-Original-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutDistAnonymizationWalkBased = "Exp1-Rob-Syb-Dist-Anonymization-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutDistTransformationWalkBased = "Exp1-Rob-Syb-Dist-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNameOutAdjTransformationWalkBased = "Exp1-Rob-Syb-Adj-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;
					String fileNamesOutsRandomPerturbations = "Exp1-Rob-Syb-Random-Perturbation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerCount + "-AttackType-" + attackType + "-EditDist-" + editDist;					
					oneRunExperimentRobustSybils(vernum, edgenum, attackType, attackerCount, editDist, fileNameOutOriginalWalkBased, fileNameOutDistAnonymizationWalkBased, fileNameOutDistTransformationWalkBased, fileNameOutAdjTransformationWalkBased, fileNamesOutsRandomPerturbations);
				}
			}
		}
	}
	
	public static void oneRunExperimentRobustSybils(int n, int m, int attackType, int attackersCount, int maxEditDist, String fileNameOutOriginalWalkBased,
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

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		experimentRobustSybils(args);	
	}

}
