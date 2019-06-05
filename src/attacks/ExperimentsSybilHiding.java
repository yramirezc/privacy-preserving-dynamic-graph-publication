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
import anonymization.KMatchAnonymizerUsingGraMi;
import anonymization.KMatchAnonymizerUsingMETIS;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;
import util.BarabasiAlbertGraphGenerator;
import util.GraphUtil;
import util.Statistics;
import util.WattsStrogatzGraphGenerator;
import utilities.GraphParameterBasedUtilitiesJGraphT;

public class ExperimentsSybilHiding {

	//==================================================================================================================
	
	// On Erdos-Renyi random graphs
	
	public static void experimentSybilHidingErdosRenyiRandomNetworks(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 200;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		int attackType = 0;   // Run the original walk-based attack by default 
		
		int attackerCounts[] = new int[]{1,2,4,8};
		
		int editDistances[] = new int[]{4,8};
		
		int kValues[] = new int[]{2,3,4,5,6,7,8,9,10};
		
		boolean useMETIS = true;
		
		boolean randomize = false;
		
		if (args.length == 8) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackType = Integer.parseInt(args[2]);
			attackerCounts = new int[1];
			attackerCounts[0] = Integer.parseInt(args[3]);
			editDistances = new int[1];
			editDistances[0] = Integer.parseInt(args[4]);
			kValues = new int[1];
			kValues[0] = Integer.parseInt(args[5]);
			if (args[6].toLowerCase().equals("-metis"))
				useMETIS = true;
			else if (args[6].toLowerCase().equals("-grami"))
				useMETIS = false;
			if (args[7].toLowerCase().equals("-random"))
				randomize = true;
			else if (args[7].toLowerCase().equals("-orig"))
				randomize = false;
		}   // else the defaults defined above are taken
		
		String expNamePrefix = "Exp1";
		String expNameSuffix = (useMETIS)? "-METIS" : "-GraMi";
		expNameSuffix += (randomize)? "-random" : "-orig";
		
		for (double density : densities) {
			for (int attackerCount : attackerCounts) {
				for (int editDist : editDistances) {
					for (int k : kValues) {
						
						int edgenum = getEdgeNum(vernum, density);				
						String fileNameOutOriginal = expNamePrefix + "-Syb-Hiding-Original-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;	
						String fileNameOutKGamma1AdjAnonymity = expNamePrefix + "-Syb-Hiding-" + k + "-Gamma1-Adj-Anon-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutKAutomorphism = expNamePrefix + "-Syb-Hiding-" + k + "-Auto-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutWeakKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-Weak-" + k + "-ell-Subgraph-Iso-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutStrongKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-Strong-" + k + "-ell-Subgraph-Iso-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutRandEquivKGamma1AdjAnonymity = expNamePrefix + "-Syb-Hiding-RandEquiv-" + k + "-Gamma1-Adj-Anon-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutRandEquivKAutomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-" + k + "-Auto-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutRandEquivWeakKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-Weak-" + k + "-ell-Subgraph-Iso-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutRandEquivStrongKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-Strong-" + k + "-ell-Subgraph-Iso-V-" + vernum + "-D-" + density + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
											
						oneRunExperimentSybilHidingErdosRenyiRandomGraphs(vernum, edgenum, attackType, attackerCount, k, editDist, useMETIS, randomize, fileNameOutOriginal, fileNameOutKGamma1AdjAnonymity, fileNameOutRandEquivKGamma1AdjAnonymity, fileNameOutKAutomorphism, fileNameOutRandEquivKAutomorphism, fileNameOutWeakKEllSubgraphIsomorphism, fileNameOutRandEquivWeakKEllSubgraphIsomorphism, fileNameOutStrongKEllSubgraphIsomorphism, fileNameOutRandEquivStrongKEllSubgraphIsomorphism);
					}
				}
			}
		}
	}
	
	public static int getEdgeNum(int vexnum , double density) {
		return (int)(density*vexnum*(vexnum-1)/2);
	}
	
	public static void oneRunExperimentSybilHidingErdosRenyiRandomGraphs(int n, int m, int attackType, int attackerCount, int k, int maxEditDist, boolean useMETIS, boolean randomize, String fileNameOutOriginal, 
			String fileNameOutKGamma1AdjAnonymity, String fileNameOutRandEquivKGamma1AdjAnonymity, String fileNameOutKAutomorphism, String fileNameOutRandEquivKAutomorphism,
			String fileNameOutWeakKEllSubgraphIsomorphism, String fileNameOutRandEquivWeakKEllSubgraphIsomorphism, String fileNameOutStrongKEllSubgraphIsomorphism, String fileNameOutRandEquivStrongKEllSubgraphIsomorphism) 
					throws NoSuchAlgorithmException, IOException {
		
		if (attackerCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginal = new FileWriter(fileNameOutOriginal+".DAT", true);		
		Writer outKGamma1AdjAnonymity = new FileWriter(fileNameOutKGamma1AdjAnonymity+".DAT", true);
		Writer outKAutomorphism = new FileWriter(fileNameOutKAutomorphism+".DAT", true);
		Writer outWeakKEllSubgraphIsomorphism = new FileWriter(fileNameOutWeakKEllSubgraphIsomorphism+".DAT", true);
		Writer outStrongKEllSubgraphIsomorphism = new FileWriter(fileNameOutStrongKEllSubgraphIsomorphism+".DAT", true);		
		Writer outRandEquivKGamma1AdjAnonymity = new FileWriter(fileNameOutRandEquivKGamma1AdjAnonymity+".DAT", true);
		Writer outRandEquivKAutomorphism = new FileWriter(fileNameOutRandEquivKAutomorphism+".DAT", true);
		Writer outRandEquivWeakKEllSubgraphIsomorphism = new FileWriter(fileNameOutRandEquivWeakKEllSubgraphIsomorphism+".DAT", true);
		Writer outRandEquivStrongKEllSubgraphIsomorphism = new FileWriter(fileNameOutRandEquivStrongKEllSubgraphIsomorphism+".DAT", true);
		
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
				
				UndirectedGraph<String, DefaultEdge> attackedGraph = null;
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
					
					attackedGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
					RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);			
					generator.generateGraph(attackedGraph, vertexFactory, null);
					
					attackSimulator.simulateAttackerSubgraphCreation(attackedGraph, attackerCount, victimCount);
					
					connInspector = new ConnectivityInspector<>(attackedGraph);
					
				} while (!connInspector.isGraphConnected());
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				Statistics.printStatisticsSybilHidingExp(i, outOriginal, attackedGraph, floydOriginalAttackedGraph, fileNameOutOriginal, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// k-automorphism
				
				SimpleGraph<String, DefaultEdge> anonKAutomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				SimpleGraph<String, DefaultEdge> pertRandEquivKAutomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonKAutomorphicGraph, k, randomize, fileNameOutKAutomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonKAutomorphicGraph, k, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydKAutomorphicGraph = new FloydWarshallShortestPaths<>(anonKAutomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outKAutomorphism, anonKAutomorphicGraph, floydKAutomorphicGraph, fileNameOutKAutomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				Set<String> addedVertices = GraphUtil.addedVertices(attackedGraph, anonKAutomorphicGraph);
				int countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonKAutomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivKAutomorphicGraph);
				int countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonKAutomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivKAutomorphicGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivKAutomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivKAutomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivKAutomorphism, pertRandEquivKAutomorphicGraph, floydRandEquivKAutomorphicGraph, fileNameOutRandEquivKAutomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// Weak (k, ell)-subgraph isomorphism
				
				SimpleGraph<String, DefaultEdge> anonWeakKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				SimpleGraph<String, DefaultEdge> pertRandEquivWeakKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonWeakKEllSubgraphIsomorphicGraph, (k + 1) * attackerCount + 1, randomize, fileNameOutWeakKEllSubgraphIsomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonWeakKEllSubgraphIsomorphicGraph, (k + 1) * attackerCount + 1, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydWeakKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(anonWeakKEllSubgraphIsomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outWeakKEllSubgraphIsomorphism, anonWeakKEllSubgraphIsomorphicGraph, floydWeakKEllSubgraphIsomorphicGraph, fileNameOutWeakKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				addedVertices = GraphUtil.addedVertices(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivWeakKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivWeakKEllSubgraphIsomorphism, pertRandEquivWeakKEllSubgraphIsomorphicGraph, floydRandEquivWeakKEllSubgraphIsomorphicGraph, fileNameOutRandEquivWeakKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// Strong (k, ell)-subgraph isomorphism
				
				SimpleGraph<String, DefaultEdge> anonStrongKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph); 
				SimpleGraph<String, DefaultEdge> pertRandEquivStrongKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonStrongKEllSubgraphIsomorphicGraph, (k - 1) * (attackerCount * attackerCount - attackerCount + 1) + 1, randomize, fileNameOutStrongKEllSubgraphIsomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonStrongKEllSubgraphIsomorphicGraph, (k - 1) * (attackerCount * attackerCount - attackerCount + 1) + 1, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydStrongKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(anonStrongKEllSubgraphIsomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outStrongKEllSubgraphIsomorphism, anonStrongKEllSubgraphIsomorphicGraph, floydStrongKEllSubgraphIsomorphicGraph, fileNameOutStrongKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
								
				addedVertices = GraphUtil.addedVertices(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivStrongKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivStrongKEllSubgraphIsomorphism, pertRandEquivStrongKEllSubgraphIsomorphicGraph, floydRandEquivStrongKEllSubgraphIsomorphicGraph, fileNameOutRandEquivStrongKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// (k, \Gamma_1)-adjacency anonymity
				
				SimpleGraph<String, DefaultEdge> anonKGamma1AdjAnonymousGraph = GraphUtil.cloneGraph(attackedGraph); 
				SimpleGraph<String, DefaultEdge> pertRandEquivKGamma1AdjAnonymousGraph = GraphUtil.cloneGraph(attackedGraph);
				
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonKGamma1AdjAnonymousGraph, k);
				FloydWarshallShortestPaths<String, DefaultEdge> floydKGamma1AdjAnonymousGraph = new FloydWarshallShortestPaths<>(anonKGamma1AdjAnonymousGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outKGamma1AdjAnonymity, anonKGamma1AdjAnonymousGraph, floydKGamma1AdjAnonymousGraph, fileNameOutKGamma1AdjAnonymity, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonKGamma1AdjAnonymousGraph);
				if (countEdgeAdditions > 0)
					GraphUtil.addRandomEdges(countEdgeAdditions, pertRandEquivKGamma1AdjAnonymousGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonKGamma1AdjAnonymousGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivKGamma1AdjAnonymousGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivKGamma1AdjAnonymousGraph = new FloydWarshallShortestPaths<>(pertRandEquivKGamma1AdjAnonymousGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivKGamma1AdjAnonymity, pertRandEquivKGamma1AdjAnonymousGraph, floydRandEquivKGamma1AdjAnonymousGraph, fileNameOutRandEquivKGamma1AdjAnonymity, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
			}
			
			outOriginal.close();
			outKGamma1AdjAnonymity.close();
			outKAutomorphism.close();
			outWeakKEllSubgraphIsomorphism.close();
			outStrongKEllSubgraphIsomorphism.close();
			outRandEquivKGamma1AdjAnonymity.close();
			outRandEquivKAutomorphism.close();
			outRandEquivWeakKEllSubgraphIsomorphism.close();
			outRandEquivStrongKEllSubgraphIsomorphism.close();
		}
	}
	
	//==================================================================================================================
	
	// On Barabasi-Albert scale-free random graphs
	
	public static void experimentSybilHidingBarabasiAlbertRandomGraphs(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 200;
		
		int m0 = 20;
		
		int[] mValues = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		
		int seedTypeId = 3;   // By default, the seed graph is an ER random graph with density 0.5
		
		int attackType = 0;   // Run the original walk-based attack by default
		
		int attackerCounts[] = new int[]{1,2,4,8};
		
		int editDistances[] = new int[]{4, 8};
		
		int kValues[] = new int[]{2,3,4,5,6,7,8,9,10};
		
		boolean useMETIS = true;
		
		boolean randomize = false;
		
		if (args.length == 10) {
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
			kValues = new int[1];
			kValues[0] = Integer.parseInt(args[7]);
			if (args[8].toLowerCase().equals("-metis"))
				useMETIS = true;
			else if (args[8].toLowerCase().equals("-grami"))
				useMETIS = false;
			if (args[9].toLowerCase().equals("-random"))
				randomize = true;
			else if (args[9].toLowerCase().equals("-orig"))
				randomize = false;
		}   // else the defaults defined above are taken
		
		String expNamePrefix = "Exp1";
		String expNameSuffix = (useMETIS)? "-METIS" : "-GraMi";
		expNameSuffix += (randomize)? "-random" : "-orig";
		
		for (int m : mValues) {
			for (int attackerCount : attackerCounts) {
				for (int editDist : editDistances)
					for (int k : kValues) {
						
						String fileNameOutOriginal = expNamePrefix + "-Syb-Hiding-Original-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutKGamma1AdjAnonymity = expNamePrefix + "-Syb-Hiding-" + k + "-Gamma1-Adj-Anon-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutKAutomorphism = expNamePrefix + "-Syb-Hiding-" + k + "-Auto-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;	
						String fileNameOutWeakKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-Weak-" + k + "-ell-Subgraph-Iso-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutStrongKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-Strong-" + k + "-ell-Subgraph-Iso-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;	
						String fileNameOutRandEquivKGamma1AdjAnonymity = expNamePrefix + "-Syb-Hiding-RandEquiv-" + k + "-Gamma1-Adj-Anon-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutRandEquivKAutomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-" + k + "-Auto-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;	
						String fileNameOutRandEquivWeakKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-Weak-" + k + "-ell-Subgraph-Iso-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
						String fileNameOutRandEquivStrongKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-Strong-" + k + "-ell-Subgraph-Iso-V-" + vernum + "-m0-" + m0 + "-m-" + m + "-Seed-"+ seedTypeId + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
																	
						oneRunExperimentSybilHidingBarabasiAlbertRandomGraphs(vernum, m0, m, seedTypeId, attackType, attackerCount, k, editDist, useMETIS, randomize, fileNameOutOriginal, fileNameOutKGamma1AdjAnonymity, fileNameOutRandEquivKGamma1AdjAnonymity, fileNameOutKAutomorphism, fileNameOutRandEquivKAutomorphism, fileNameOutWeakKEllSubgraphIsomorphism, fileNameOutRandEquivWeakKEllSubgraphIsomorphism, fileNameOutStrongKEllSubgraphIsomorphism, fileNameOutRandEquivStrongKEllSubgraphIsomorphism);
					}
			}
		}
	}	
	
	public static void oneRunExperimentSybilHidingBarabasiAlbertRandomGraphs(int n, int m0, int m, int seedTypeId, int attackType, int attackerCount, int k, int maxEditDist, boolean useMETIS, boolean randomize, String fileNameOutOriginal, 
			String fileNameOutKGamma1AdjAnonymity, String fileNameOutRandEquivKGamma1AdjAnonymity, String fileNameOutKAutomorphism, String fileNameOutRandEquivKAutomorphism,
			String fileNameOutWeakKEllSubgraphIsomorphism, String fileNameOutRandEquivWeakKEllSubgraphIsomorphism, String fileNameOutStrongKEllSubgraphIsomorphism, String fileNameOutRandEquivStrongKEllSubgraphIsomorphism) 
					throws NoSuchAlgorithmException, IOException {
		
		if (attackerCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginal = new FileWriter(fileNameOutOriginal+".DAT", true);		
		Writer outKGamma1AdjAnonymity = new FileWriter(fileNameOutKGamma1AdjAnonymity+".DAT", true);
		Writer outKAutomorphism = new FileWriter(fileNameOutKAutomorphism+".DAT", true);
		Writer outWeakKEllSubgraphIsomorphism = new FileWriter(fileNameOutWeakKEllSubgraphIsomorphism+".DAT", true);
		Writer outStrongKEllSubgraphIsomorphism = new FileWriter(fileNameOutStrongKEllSubgraphIsomorphism+".DAT", true);		
		Writer outRandEquivKGamma1AdjAnonymity = new FileWriter(fileNameOutRandEquivKGamma1AdjAnonymity+".DAT", true);
		Writer outRandEquivKAutomorphism = new FileWriter(fileNameOutRandEquivKAutomorphism+".DAT", true);
		Writer outRandEquivWeakKEllSubgraphIsomorphism = new FileWriter(fileNameOutRandEquivWeakKEllSubgraphIsomorphism+".DAT", true);
		Writer outRandEquivStrongKEllSubgraphIsomorphism = new FileWriter(fileNameOutRandEquivStrongKEllSubgraphIsomorphism+".DAT", true);
		
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
				
				UndirectedGraph<String, DefaultEdge> attackedGraph = null;
				ConnectivityInspector<String, DefaultEdge> connInspector = null;
				
				do {
					
					UndirectedGraph<String, DefaultEdge> origBAGraph = BarabasiAlbertGraphGenerator.newGraph(n, 0, m0, m, seedTypeId);   // Giving 0 as firstVertId because the shift&shuffle method called afterwards will shift the vertex ids 
					
					// This method is called to guarantee that in different runs the victims are different types of vertices 
					// (in terms of their degrees)
					attackedGraph = GraphUtil.shiftAndShuffleVertexIds(origBAGraph, attackerCount, origBAGraph.vertexSet());    
					
					attackSimulator.simulateAttackerSubgraphCreation(attackedGraph, attackerCount, victimCount);
					
					connInspector = new ConnectivityInspector<>(attackedGraph);
					
				} while (!connInspector.isGraphConnected());
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				Statistics.printStatisticsSybilHidingExp(i, outOriginal, attackedGraph, floydOriginalAttackedGraph, fileNameOutOriginal, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// k-automorphism
				
				SimpleGraph<String, DefaultEdge> anonKAutomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				SimpleGraph<String, DefaultEdge> pertRandEquivKAutomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonKAutomorphicGraph, k, randomize, fileNameOutKAutomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonKAutomorphicGraph, k, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydKAutomorphicGraph = new FloydWarshallShortestPaths<>(anonKAutomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outKAutomorphism, anonKAutomorphicGraph, floydKAutomorphicGraph, fileNameOutKAutomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				Set<String> addedVertices = GraphUtil.addedVertices(attackedGraph, anonKAutomorphicGraph);
				int countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonKAutomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivKAutomorphicGraph);
				int countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonKAutomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivKAutomorphicGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivKAutomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivKAutomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivKAutomorphism, pertRandEquivKAutomorphicGraph, floydRandEquivKAutomorphicGraph, fileNameOutRandEquivKAutomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// Weak (k, ell)-subgraph isomorphism
				
				SimpleGraph<String, DefaultEdge> anonWeakKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				SimpleGraph<String, DefaultEdge> pertRandEquivWeakKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonWeakKEllSubgraphIsomorphicGraph, (k + 1) * attackerCount + 1, randomize, fileNameOutWeakKEllSubgraphIsomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonWeakKEllSubgraphIsomorphicGraph, (k + 1) * attackerCount + 1, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydWeakKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(anonWeakKEllSubgraphIsomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outWeakKEllSubgraphIsomorphism, anonWeakKEllSubgraphIsomorphicGraph, floydWeakKEllSubgraphIsomorphicGraph, fileNameOutWeakKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				addedVertices = GraphUtil.addedVertices(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivWeakKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivWeakKEllSubgraphIsomorphism, pertRandEquivWeakKEllSubgraphIsomorphicGraph, floydRandEquivWeakKEllSubgraphIsomorphicGraph, fileNameOutRandEquivWeakKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// Strong (k, ell)-subgraph isomorphism
				
				SimpleGraph<String, DefaultEdge> anonStrongKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph); 
				SimpleGraph<String, DefaultEdge> pertRandEquivStrongKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonStrongKEllSubgraphIsomorphicGraph, (k - 1) * (attackerCount * attackerCount - attackerCount + 1) + 1, randomize, fileNameOutStrongKEllSubgraphIsomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonStrongKEllSubgraphIsomorphicGraph, (k - 1) * (attackerCount * attackerCount - attackerCount + 1) + 1, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydStrongKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(anonStrongKEllSubgraphIsomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outStrongKEllSubgraphIsomorphism, anonStrongKEllSubgraphIsomorphicGraph, floydStrongKEllSubgraphIsomorphicGraph, fileNameOutStrongKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				addedVertices = GraphUtil.addedVertices(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivStrongKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivStrongKEllSubgraphIsomorphism, pertRandEquivStrongKEllSubgraphIsomorphicGraph, floydRandEquivStrongKEllSubgraphIsomorphicGraph, fileNameOutRandEquivStrongKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// (k, \Gamma_1)-adjacency anonymity
				
				SimpleGraph<String, DefaultEdge> anonKGamma1AdjAnonymousGraph = GraphUtil.cloneGraph(attackedGraph); 
				SimpleGraph<String, DefaultEdge> pertRandEquivKGamma1AdjAnonymousGraph = GraphUtil.cloneGraph(attackedGraph);
				
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonKGamma1AdjAnonymousGraph, k);
				FloydWarshallShortestPaths<String, DefaultEdge> floydKGamma1AdjAnonymousGraph = new FloydWarshallShortestPaths<>(anonKGamma1AdjAnonymousGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outKGamma1AdjAnonymity, anonKGamma1AdjAnonymousGraph, floydKGamma1AdjAnonymousGraph, fileNameOutKGamma1AdjAnonymity, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonKGamma1AdjAnonymousGraph);
				if (countEdgeAdditions > 0)
					GraphUtil.addRandomEdges(countEdgeAdditions, pertRandEquivKGamma1AdjAnonymousGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonKGamma1AdjAnonymousGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivKGamma1AdjAnonymousGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivKGamma1AdjAnonymousGraph = new FloydWarshallShortestPaths<>(pertRandEquivKGamma1AdjAnonymousGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivKGamma1AdjAnonymity, pertRandEquivKGamma1AdjAnonymousGraph, floydRandEquivKGamma1AdjAnonymousGraph, fileNameOutRandEquivKGamma1AdjAnonymity, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
			}
			
			outOriginal.close();
			outKGamma1AdjAnonymity.close();
			outKAutomorphism.close();
			outWeakKEllSubgraphIsomorphism.close();
			outStrongKEllSubgraphIsomorphism.close();
			outRandEquivKGamma1AdjAnonymity.close();
			outRandEquivKAutomorphism.close();
			outRandEquivWeakKEllSubgraphIsomorphism.close();
			outRandEquivStrongKEllSubgraphIsomorphism.close();
		}
	}
	
	//==================================================================================================================
	
	// On Watts-Strogatz small-world random graphs
	
	public static void experimentSybilHidingWattsStrogatzRandomGraphs(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 200;
		
		double[] rhos = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		
		int[] wsModelParamKValues = {20, 40, 60, 80, 100};   // Approximately inducing densities 0.1, 0.2, ... , 0.5
		
		int attackType = 0;   // Run the original walk-based attack by default
		
		int attackerCounts[] = new int[]{1,2,4,8};
		
		int editDistances[] = new int[]{4,8};
		
		int kmatchMethodKValues[] = new int[]{2,3,4,5,6,7,8,9,10};
		
		boolean useMETIS = true;
		
		boolean randomize = false;
		
		if (args.length == 9) {
			vernum = Integer.parseInt(args[0]);
			wsModelParamKValues = new int[1];
			wsModelParamKValues[0] = Integer.parseInt(args[1]);
			rhos = new double[1];
			rhos[0] = Double.parseDouble(args[2]);
			attackType = Integer.parseInt(args[3]);
			attackerCounts = new int[1];
			attackerCounts[0] = Integer.parseInt(args[4]);
			editDistances = new int[1];
			editDistances[0] = Integer.parseInt(args[5]);			
			kmatchMethodKValues = new int[1];
			kmatchMethodKValues[0] = Integer.parseInt(args[6]);
			if (args[7].toLowerCase().equals("-metis"))
				useMETIS = true;
			else if (args[7].toLowerCase().equals("-grami"))
				useMETIS = false;
			if (args[8].toLowerCase().equals("-random"))
				randomize = true;
			else if (args[8].toLowerCase().equals("-orig"))
				randomize = false;
		}   // else the defaults defined above are taken
		
		String expNamePrefix = "Exp1";
		String expNameSuffix = (useMETIS)? "-METIS" : "-GraMi";
		expNameSuffix += (randomize)? "-random" : "-orig";
		
		for (int wsParamK : wsModelParamKValues) {
			for (double rho : rhos) {
				for (int attackerCount : attackerCounts) {
					for (int editDist : editDistances)
						for (int kmatchK : kmatchMethodKValues) {				
							
							String fileNameOutOriginalWalkBased = expNamePrefix + "-Syb-Hiding-Original-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-WalkBased-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;				
							String fileNameOutKGamma1AdjAnonymity = expNamePrefix + "-Syb-Hiding-" + kmatchK + "-Gamma1-Adj-Anon-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
							String fileNameOutKAutomorphism = expNamePrefix + "-Syb-Hiding-" + kmatchK + "-Auto-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
							String fileNameOutWeakKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-Weak-" + kmatchK + "-ell-Subgraph-Iso-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
							String fileNameOutStrongKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-Strong-" + kmatchK + "-ell-Subgraph-Iso-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
							String fileNameOutRandEquivKGamma1AdjAnonymity = expNamePrefix + "-Syb-Hiding-RandEquiv-" + kmatchK + "-Gamma1-Adj-Anon-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
							String fileNameOutRandEquivKAutomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-" + kmatchK + "-Auto-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
							String fileNameOutRandEquivWeakKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-Weak-" + kmatchK + "-ell-Subgraph-Iso-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
							String fileNameOutRandEquivStrongKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-RandEquiv-Strong-" + kmatchK + "-ell-Subgraph-Iso-V-" + vernum + "-K-" + wsParamK + "-Rho-" + rho + "-A-" + attackerCount + "-AttType-" + attackType + "-ED-" + editDist + expNameSuffix;
												
							oneRunExperimentSybilHidingWattsStrogatzRandomGraphs(vernum, wsParamK, rho, attackType, attackerCount, kmatchK, editDist, useMETIS, randomize, fileNameOutOriginalWalkBased, fileNameOutKGamma1AdjAnonymity, fileNameOutRandEquivKGamma1AdjAnonymity, fileNameOutKAutomorphism, fileNameOutRandEquivKAutomorphism, fileNameOutWeakKEllSubgraphIsomorphism, fileNameOutRandEquivWeakKEllSubgraphIsomorphism, fileNameOutStrongKEllSubgraphIsomorphism, fileNameOutRandEquivStrongKEllSubgraphIsomorphism);
					}
				}
			}
		}
	}
	
	public static void oneRunExperimentSybilHidingWattsStrogatzRandomGraphs(int n, int wsParamK, double rho, int attackType, int attackerCount, int kmatchK, int maxEditDist, boolean useMETIS, boolean randomize, String fileNameOutOriginal, 
			String fileNameOutKGamma1AdjAnonymity, String fileNameOutRandEquivKGamma1AdjAnonymity, String fileNameOutKAutomorphism, String fileNameOutRandEquivKAutomorphism,
			String fileNameOutWeakKEllSubgraphIsomorphism, String fileNameOutRandEquivWeakKEllSubgraphIsomorphism, String fileNameOutStrongKEllSubgraphIsomorphism, String fileNameOutRandEquivStrongKEllSubgraphIsomorphism) 
					throws NoSuchAlgorithmException, IOException {
		
		if (attackerCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginal = new FileWriter(fileNameOutOriginal+".DAT", true);		
		Writer outKGamma1AdjAnonymity = new FileWriter(fileNameOutKGamma1AdjAnonymity+".DAT", true);
		Writer outKAutomorphism = new FileWriter(fileNameOutKAutomorphism+".DAT", true);
		Writer outWeakKEllSubgraphIsomorphism = new FileWriter(fileNameOutWeakKEllSubgraphIsomorphism+".DAT", true);
		Writer outStrongKEllSubgraphIsomorphism = new FileWriter(fileNameOutStrongKEllSubgraphIsomorphism+".DAT", true);		
		Writer outRandEquivKGamma1AdjAnonymity = new FileWriter(fileNameOutRandEquivKGamma1AdjAnonymity+".DAT", true);
		Writer outRandEquivKAutomorphism = new FileWriter(fileNameOutRandEquivKAutomorphism+".DAT", true);
		Writer outRandEquivWeakKEllSubgraphIsomorphism = new FileWriter(fileNameOutRandEquivWeakKEllSubgraphIsomorphism+".DAT", true);
		Writer outRandEquivStrongKEllSubgraphIsomorphism = new FileWriter(fileNameOutRandEquivStrongKEllSubgraphIsomorphism+".DAT", true);
		
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
				
				UndirectedGraph<String, DefaultEdge> attackedGraph = null;
				ConnectivityInspector<String, DefaultEdge> connInspector = null;
				
				do {
					
					attackedGraph = WattsStrogatzGraphGenerator.newGraph(n, startingVertex, wsParamK, rho);    
					
					attackSimulator.simulateAttackerSubgraphCreation(attackedGraph, attackerCount, victimCount);
					
					connInspector = new ConnectivityInspector<>(attackedGraph);
					
				} while (!connInspector.isGraphConnected());
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				Statistics.printStatisticsSybilHidingExp(i, outOriginal, attackedGraph, floydOriginalAttackedGraph, fileNameOutOriginal, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// k-automorphism
				
				SimpleGraph<String, DefaultEdge> anonKAutomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				SimpleGraph<String, DefaultEdge> pertRandEquivKAutomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonKAutomorphicGraph, kmatchK, randomize, fileNameOutKAutomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonKAutomorphicGraph, kmatchK, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydKAutomorphicGraph = new FloydWarshallShortestPaths<>(anonKAutomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outKAutomorphism, anonKAutomorphicGraph, floydKAutomorphicGraph, fileNameOutKAutomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				Set<String> addedVertices = GraphUtil.addedVertices(attackedGraph, anonKAutomorphicGraph);
				int countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonKAutomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivKAutomorphicGraph);
				int countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonKAutomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivKAutomorphicGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivKAutomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivKAutomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivKAutomorphism, pertRandEquivKAutomorphicGraph, floydRandEquivKAutomorphicGraph, fileNameOutRandEquivKAutomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// Weak (k, ell)-subgraph isomorphism
				
				SimpleGraph<String, DefaultEdge> anonWeakKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				SimpleGraph<String, DefaultEdge> pertRandEquivWeakKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonWeakKEllSubgraphIsomorphicGraph, (kmatchK + 1) * attackerCount + 1, randomize, fileNameOutWeakKEllSubgraphIsomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonWeakKEllSubgraphIsomorphicGraph, (kmatchK + 1) * attackerCount + 1, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydWeakKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(anonWeakKEllSubgraphIsomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outWeakKEllSubgraphIsomorphism, anonWeakKEllSubgraphIsomorphicGraph, floydWeakKEllSubgraphIsomorphicGraph, fileNameOutWeakKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				addedVertices = GraphUtil.addedVertices(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonWeakKEllSubgraphIsomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivWeakKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivWeakKEllSubgraphIsomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivWeakKEllSubgraphIsomorphism, pertRandEquivWeakKEllSubgraphIsomorphicGraph, floydRandEquivWeakKEllSubgraphIsomorphicGraph, fileNameOutRandEquivWeakKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// Strong (k, ell)-subgraph isomorphism
				
				SimpleGraph<String, DefaultEdge> anonStrongKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph); 
				SimpleGraph<String, DefaultEdge> pertRandEquivStrongKEllSubgraphIsomorphicGraph = GraphUtil.cloneGraph(attackedGraph);
				
				if (useMETIS)
					KMatchAnonymizerUsingMETIS.anonymizeGraph(anonStrongKEllSubgraphIsomorphicGraph, (kmatchK - 1) * (attackerCount * attackerCount - attackerCount + 1) + 1, randomize, fileNameOutStrongKEllSubgraphIsomorphism);
				else   // use GraMi
					KMatchAnonymizerUsingGraMi.anonymizeGraph(anonStrongKEllSubgraphIsomorphicGraph, (kmatchK - 1) * (attackerCount * attackerCount - attackerCount + 1) + 1, randomize);
				FloydWarshallShortestPaths<String, DefaultEdge> floydStrongKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(anonStrongKEllSubgraphIsomorphicGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outStrongKEllSubgraphIsomorphism, anonStrongKEllSubgraphIsomorphicGraph, floydStrongKEllSubgraphIsomorphicGraph, fileNameOutStrongKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				addedVertices = GraphUtil.addedVertices(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				if (addedVertices.size() > 0 || countEdgeAdditions > 0)
					GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonStrongKEllSubgraphIsomorphicGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivStrongKEllSubgraphIsomorphicGraph = new FloydWarshallShortestPaths<>(pertRandEquivStrongKEllSubgraphIsomorphicGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivStrongKEllSubgraphIsomorphism, pertRandEquivStrongKEllSubgraphIsomorphicGraph, floydRandEquivStrongKEllSubgraphIsomorphicGraph, fileNameOutRandEquivStrongKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				// (k, \Gamma_1)-adjacency anonymity
				
				SimpleGraph<String, DefaultEdge> anonKGamma1AdjAnonymousGraph = GraphUtil.cloneGraph(attackedGraph); 
				SimpleGraph<String, DefaultEdge> pertRandEquivKGamma1AdjAnonymousGraph = GraphUtil.cloneGraph(attackedGraph);
				
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonKGamma1AdjAnonymousGraph, kmatchK);
				FloydWarshallShortestPaths<String, DefaultEdge> floydKGamma1AdjAnonymousGraph = new FloydWarshallShortestPaths<>(anonKGamma1AdjAnonymousGraph);			
				Statistics.printStatisticsSybilHidingExp(i, outKGamma1AdjAnonymity, anonKGamma1AdjAnonymousGraph, floydKGamma1AdjAnonymousGraph, fileNameOutKGamma1AdjAnonymity, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
				countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, anonKGamma1AdjAnonymousGraph);
				if (countEdgeAdditions > 0)
					GraphUtil.addRandomEdges(countEdgeAdditions, pertRandEquivKGamma1AdjAnonymousGraph);
				countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, anonKGamma1AdjAnonymousGraph);
				if (countEdgeRemovals > 0)
					GraphUtil.removeRandomEdges(countEdgeRemovals, pertRandEquivKGamma1AdjAnonymousGraph);
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivKGamma1AdjAnonymousGraph = new FloydWarshallShortestPaths<>(pertRandEquivKGamma1AdjAnonymousGraph);
				Statistics.printStatisticsSybilHidingExp(i, outRandEquivKGamma1AdjAnonymity, pertRandEquivKGamma1AdjAnonymousGraph, floydRandEquivKGamma1AdjAnonymousGraph, fileNameOutRandEquivKGamma1AdjAnonymity, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginalAttackedGraph);
				
			}
			
			outOriginal.close();
			outKGamma1AdjAnonymity.close();
			outKAutomorphism.close();
			outWeakKEllSubgraphIsomorphism.close();
			outStrongKEllSubgraphIsomorphism.close();
			outRandEquivKGamma1AdjAnonymity.close();
			outRandEquivKAutomorphism.close();
			outRandEquivWeakKEllSubgraphIsomorphism.close();
			outRandEquivStrongKEllSubgraphIsomorphism.close();
		}
	}
	
	//==================================================================================================================
	
	// On a real social graph (Facebook, Panzarasa or URV)
	
	public static void experimentSybilHidingRealNetworks(String [] args) throws NoSuchAlgorithmException, IOException {
		
		if (args.length == 7) {
			
			int attackerCount = Integer.parseInt(args[1]);
			int attackType = Integer.parseInt(args[2]);
			int editDistance = Integer.parseInt(args[3]);
			int k = Integer.parseInt(args[4]);
			boolean useMETIS = false;
			boolean randomize = false;
			if (args[5].toLowerCase().equals("-metis"))
				useMETIS = true;
			else if (args[5].toLowerCase().equals("-grami"))
				useMETIS = false;
			if (args[6].toLowerCase().equals("-random"))
				randomize = true;
			else if (args[6].toLowerCase().equals("-orig"))
				randomize = false;
			
			if (args[0].equals("-facebook"))
				oneRunExperimentSybilHidingRealNetworks("facebook", attackerCount, attackType, editDistance, k, useMETIS, randomize);
			else if (args[0].equals("-panzarasa"))
				oneRunExperimentSybilHidingRealNetworks("panzarasa", attackerCount, attackType, editDistance, k, useMETIS, randomize);
			else if (args[0].equals("-urv"))
				oneRunExperimentSybilHidingRealNetworks("urv", attackerCount, attackType, editDistance, k, useMETIS, randomize);
		}
	}
	
	static void oneRunExperimentSybilHidingRealNetworks(String networkName, int attackerCount, int attackType, int maxEditDist, int k, boolean useMETIS, boolean randomize) throws IOException {
		
		String expNamePrefix = "Exp1";
		String expNameSuffix = (useMETIS)? "-METIS" : "-GraMi";
		expNameSuffix += (randomize)? "-random" : "-orig";
		
		String fileNameOutOriginal = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-original" + expNameSuffix;		
		String fileNameOutKGamma1AdjAnonymity = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-" + k + "-gamma1-anon" + expNameSuffix;
		String fileNameOutKAutomorphism = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-" + k + "-auto" + expNameSuffix;
		String fileNameOutWeakKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-weak-" + k + "-ell-subgraph-iso" + expNameSuffix;
		String fileNameOutStrongKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-strong-" + k + "-ell-subgraph-iso" + expNameSuffix;
		String fileNameOutRandEquivKGamma1AdjAnonymity = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-rand-equiv-" + k + "-gamma1-anon" + expNameSuffix;
		String fileNameOutRandEquivKAutomorphism = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-rand-equiv-" + k + "-auto" + expNameSuffix;
		String fileNameOutRandEquivWeakKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-rand-equiv-weak-" + k + "-ell-subgraph-iso" + expNameSuffix;
		String fileNameOutRandEquivStrongKEllSubgraphIsomorphism = expNamePrefix + "-Syb-Hiding-" + networkName + "-AttType-" + attackType + "-AttCount-" + attackerCount + "-ED-" + maxEditDist + "-rand-equiv-strong-" + k + "-ell-subgraph-iso" + expNameSuffix;
		
		Writer outOriginal = new FileWriter(fileNameOutOriginal + ".dat", true);
		Writer outKGamma1AdjAnonymity = new FileWriter(fileNameOutKGamma1AdjAnonymity + ".dat", true);
		Writer outKAutomorphism = new FileWriter(fileNameOutKAutomorphism + ".dat", true);
		Writer outWeakKEllSubgraphIsomorphism = new FileWriter(fileNameOutWeakKEllSubgraphIsomorphism + ".dat", true);
		Writer outStrongKEllSubgraphIsomorphism = new FileWriter(fileNameOutStrongKEllSubgraphIsomorphism + ".dat", true);
		Writer outRandEquivKGamma1AdjAnonymity = new FileWriter(fileNameOutRandEquivKGamma1AdjAnonymity + ".dat", true);
		Writer outRandEquivKAutomorphism = new FileWriter(fileNameOutRandEquivKAutomorphism + ".dat", true);
		Writer outRandEquivWeakKEllSubgraphIsomorphism = new FileWriter(fileNameOutRandEquivWeakKEllSubgraphIsomorphism + ".dat", true);
		Writer outRandEquivStrongKEllSubgraphIsomorphism = new FileWriter(fileNameOutRandEquivStrongKEllSubgraphIsomorphism + ".dat", true);
				
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
		case 9:   // Robust sybil retrieval: yes (it is always yes), degree sequence optimization: no, uniformly distributed fingerprints: yes, approximate fingerprint matching: yes, error-correcting fingerprints: no
			attackSimulator = new RobustWalkBasedAttackSimulator(maxEditDist, false, true, true, false);
			break;
		default:
			break;
		}
		
		for (int i = 0; i < 1000; i++) {	 // Although it is the same graph, every iteration shuffles its vertex set, so the attacker targets different victims 
		
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
			Statistics.printStatisticsSybilHidingExp(i, outOriginal, attackedGraph, floydOriginal, fileNameOutOriginal, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
				
			// k-automorphism
			SimpleGraph<String, DefaultEdge> graphKAutomorphic = GraphUtil.cloneGraph(attackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandEquivKAutomorphic = GraphUtil.cloneGraph(attackedGraph);
			
			if (useMETIS)
				KMatchAnonymizerUsingMETIS.anonymizeGraph(graphKAutomorphic, k, randomize, fileNameOutKAutomorphism);
			else   // use GraMi
				KMatchAnonymizerUsingGraMi.anonymizeGraph(graphKAutomorphic, k, randomize);
			FloydWarshallShortestPaths<String, DefaultEdge> floydKAutomorphic = new FloydWarshallShortestPaths<>(graphKAutomorphic);
			Statistics.printStatisticsSybilHidingExp(i, outKAutomorphism, graphKAutomorphic, floydKAutomorphic, fileNameOutKAutomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			Set<String> addedVertices = GraphUtil.addedVertices(attackedGraph, graphKAutomorphic);
			int countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, graphKAutomorphic);
			if (addedVertices.size() > 0 || countEdgeAdditions > 0)
				GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, graphRandEquivKAutomorphic);
			int countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, graphKAutomorphic);
			if (countEdgeRemovals > 0)
				GraphUtil.removeRandomEdges(countEdgeRemovals, graphRandEquivKAutomorphic);
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivKAutomorphic = new FloydWarshallShortestPaths<>(graphRandEquivKAutomorphic);
			Statistics.printStatisticsSybilHidingExp(i, outRandEquivKAutomorphism, graphRandEquivKAutomorphic, floydRandEquivKAutomorphic, fileNameOutRandEquivKAutomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			// Weak (k, ell)-subgraph isomorphism
			
			SimpleGraph<String, DefaultEdge> graphWeakKEllSubgraphIsomorphic = GraphUtil.cloneGraph(attackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandEquivWeakKEllSubgraphIsomorphic = GraphUtil.cloneGraph(attackedGraph);
			
			if (useMETIS)
				KMatchAnonymizerUsingMETIS.anonymizeGraph(graphWeakKEllSubgraphIsomorphic, (k + 1) * attackerCount + 1, randomize, fileNameOutWeakKEllSubgraphIsomorphism);
			else   // use GraMi 
				KMatchAnonymizerUsingGraMi.anonymizeGraph(graphWeakKEllSubgraphIsomorphic, (k + 1) * attackerCount + 1, randomize);
			FloydWarshallShortestPaths<String, DefaultEdge> floydWeakKEllSubgraphIsomorphic = new FloydWarshallShortestPaths<>(graphWeakKEllSubgraphIsomorphic);
			Statistics.printStatisticsSybilHidingExp(i, outWeakKEllSubgraphIsomorphism, graphWeakKEllSubgraphIsomorphic, floydWeakKEllSubgraphIsomorphic, fileNameOutWeakKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			addedVertices = GraphUtil.addedVertices(attackedGraph, graphWeakKEllSubgraphIsomorphic);
			countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, graphWeakKEllSubgraphIsomorphic);
			if (addedVertices.size() > 0 || countEdgeAdditions > 0)
				GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, graphRandEquivWeakKEllSubgraphIsomorphic);
			countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, graphWeakKEllSubgraphIsomorphic);
			if (countEdgeRemovals > 0)
				GraphUtil.removeRandomEdges(countEdgeRemovals, graphRandEquivWeakKEllSubgraphIsomorphic);
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivWeakKEllSubgraphIsomorphic = new FloydWarshallShortestPaths<>(graphRandEquivWeakKEllSubgraphIsomorphic);
			Statistics.printStatisticsSybilHidingExp(i, outRandEquivWeakKEllSubgraphIsomorphism, graphRandEquivWeakKEllSubgraphIsomorphic, floydRandEquivWeakKEllSubgraphIsomorphic, fileNameOutRandEquivWeakKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			// Strong (k, ell)-subgraph isomorphism
			
			SimpleGraph<String, DefaultEdge> graphStrongKEllSubgraphIsomorphic = GraphUtil.cloneGraph(attackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandEquivStrongKEllSubgraphIsomorphic = GraphUtil.cloneGraph(attackedGraph);
			
			if (useMETIS)
				KMatchAnonymizerUsingMETIS.anonymizeGraph(graphStrongKEllSubgraphIsomorphic, (k - 1) * (attackerCount * attackerCount - attackerCount + 1) + 1, randomize, fileNameOutStrongKEllSubgraphIsomorphism);
			else   // use GraMi
				KMatchAnonymizerUsingGraMi.anonymizeGraph(graphStrongKEllSubgraphIsomorphic, (k - 1) * (attackerCount * attackerCount - attackerCount + 1) + 1, randomize);
			FloydWarshallShortestPaths<String, DefaultEdge> floydStrongKEllSubgraphIsomorphic = new FloydWarshallShortestPaths<>(graphStrongKEllSubgraphIsomorphic);
			Statistics.printStatisticsSybilHidingExp(i, outStrongKEllSubgraphIsomorphism, graphStrongKEllSubgraphIsomorphic, floydStrongKEllSubgraphIsomorphic, fileNameOutStrongKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			addedVertices = GraphUtil.addedVertices(attackedGraph, graphStrongKEllSubgraphIsomorphic);
			countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, graphStrongKEllSubgraphIsomorphic);
			if (addedVertices.size() > 0 || countEdgeAdditions > 0)
				GraphUtil.addVertsAndRandomEdges(addedVertices, countEdgeAdditions, graphRandEquivStrongKEllSubgraphIsomorphic);
			countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, graphStrongKEllSubgraphIsomorphic);
			if (countEdgeRemovals > 0)
				GraphUtil.removeRandomEdges(countEdgeRemovals, graphRandEquivStrongKEllSubgraphIsomorphic);
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivStrongKEllSubgraphIsomorphic = new FloydWarshallShortestPaths<>(graphRandEquivStrongKEllSubgraphIsomorphic);
			Statistics.printStatisticsSybilHidingExp(i, outRandEquivStrongKEllSubgraphIsomorphism, graphRandEquivStrongKEllSubgraphIsomorphic, floydRandEquivStrongKEllSubgraphIsomorphic, fileNameOutRandEquivStrongKEllSubgraphIsomorphism, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			// (k, \Gamma_1)-adjacency anonymity
			
			SimpleGraph<String, DefaultEdge> graphKGamma1AdjAnonymous = GraphUtil.cloneGraph(attackedGraph); 
			SimpleGraph<String, DefaultEdge> graphRandEquivKGamma1AdjAnonymous = GraphUtil.cloneGraph(attackedGraph);
			
			AdjacencyAnonymizer.k1AdjAnonymousTransformation(graphKGamma1AdjAnonymous, k);
			FloydWarshallShortestPaths<String, DefaultEdge> floydKGamma1AdjAnonymous = new FloydWarshallShortestPaths<>(graphKGamma1AdjAnonymous);			
			Statistics.printStatisticsSybilHidingExp(i, outKGamma1AdjAnonymity, graphKGamma1AdjAnonymous, floydKGamma1AdjAnonymous, fileNameOutKGamma1AdjAnonymity, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
			countEdgeAdditions = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(attackedGraph, graphKGamma1AdjAnonymous);
			if (countEdgeAdditions > 0)
				GraphUtil.addRandomEdges(countEdgeAdditions, graphRandEquivKGamma1AdjAnonymous);
			countEdgeRemovals = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(attackedGraph, graphKGamma1AdjAnonymous);
			if (countEdgeRemovals > 0)
				GraphUtil.removeRandomEdges(countEdgeRemovals, graphRandEquivKGamma1AdjAnonymous);
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandEquivKGamma1AdjAnonymous = new FloydWarshallShortestPaths<>(graphRandEquivKGamma1AdjAnonymous);
			Statistics.printStatisticsSybilHidingExp(i, outRandEquivKGamma1AdjAnonymity, graphRandEquivKGamma1AdjAnonymous, floydRandEquivKGamma1AdjAnonymous, fileNameOutRandEquivKGamma1AdjAnonymity, attackerCount, victimCount, attackSimulator, attackedGraph, floydOriginal);
			
		}
		
		outOriginal.close();
		outKGamma1AdjAnonymity.close();
		outKAutomorphism.close();
		outWeakKEllSubgraphIsomorphism.close();
		outStrongKEllSubgraphIsomorphism.close();
		outRandEquivKGamma1AdjAnonymity.close();
		outRandEquivKAutomorphism.close();
		outRandEquivWeakKEllSubgraphIsomorphism.close();
		outRandEquivStrongKEllSubgraphIsomorphism.close();
		
	}
	
	//==================================================================================================================

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		if (args.length == 7)
			experimentSybilHidingRealNetworks(args);
		else if (args.length == 8)
			experimentSybilHidingErdosRenyiRandomNetworks(args);
		else if (args.length == 9)
			experimentSybilHidingWattsStrogatzRandomGraphs(args);
		else if (args.length == 10)
			experimentSybilHidingBarabasiAlbertRandomGraphs(args);
		else 
			System.err.println("Argument list triggers no experiment");
	}

}
