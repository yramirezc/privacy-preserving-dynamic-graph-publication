package attacks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import anonymization.AdjacencyAnonymizer;
import anonymization.OddCycle;
import anonymization.ShortSingleCycle;
import anonymization.LargeSingleCycle;
import anonymization.BaseCycle;
import anonymization.ClusteringBasedAnonymizer;
import anonymization.DegreeAnonymityLiuTerzi;
import anonymization.FeasibilityTester;
import real.FacebookEgoNetwork;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;
import util.GraphUtil;
import util.Statistics;

public class AttackThreeMethod {
	
	public static int getEdgeNum(int vexnum , double density) {
		return (int)(density*vexnum*(vexnum-1)/2);
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		//mainExperiment1ExtendedVersion(args);
		//oneRunFacebookPanzURV(args);
		//computeStatsDensityBasedRandomGeneration();
		//main250(args);
		//main250Panzarasa(args);
		main250Facebook(args);
		//experimentsClusteringBasedAnonymizationWalkBasedAttack(args);
	}
	
	public static void main250(String[] args) throws NoSuchAlgorithmException, IOException {
		//System.setOut(new PrintStream("report.txt"));
		//int vernum = Integer.parseInt(args[0]);
		//int vernum = 250;
		int vernum = 50;
		double density = Double.parseDouble(args[0]);
		int attackers = Integer.parseInt(args[1]);	
		
		int edgenum = getEdgeNum(vernum,density);
		
		AttackThreeMethod agg = new AttackThreeMethod();

		//System.out.println("Now the density is "+ density+ " with "+ attackers+" attackers and edgenumber:"+edgenum);

//		Writer outOriginalWalkBased = new FileWriter("Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers+".DAT", true);
//		Writer outOriginalCutBased = new FileWriter("Original-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers+".DAT", true);
//		Writer outOddCycleWalkBased = new FileWriter("OddCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers+".DAT", true);
//		Writer outOddCycleCutBased = new FileWriter("OddCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers+".DAT", true);
//		Writer outOddCycleRandomWalkBased = new FileWriter("OddCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers+".DAT", true);
//		Writer outOddCycleRandomCutBased = new FileWriter("OddCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers+".DAT", true);
//		Writer outShortSingleCycleWalkBased = new FileWriter("ShortSingleCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers+".DAT", true);
//		Writer outShortSingleCycleCutBased = new FileWriter("ShortSingleCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers+".DAT", true);
//		Writer outShortSingleCycleRandomWalkBased = new FileWriter("ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers+".DAT", true);
//		Writer outShortSingleCycleRandomCutBased = new FileWriter("ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers+".DAT", true);
//		Writer outLargeSingleCycleWalkBased = new FileWriter("ShortSingleCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers+".DAT", true);
//		Writer outLargeSingleCycleCutBased = new FileWriter("ShortSingleCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers+".DAT", true);
//		Writer outLargeSingleCycleRandomWalkBased = new FileWriter("ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers+".DAT", true);
//		Writer outLargeSingleCycleRandomCutBased = new FileWriter("ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers+".DAT", true);
		//Writer outAllTriangles = new FileWriter("AllTriangles-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
		//Writer outAllTrianglesRandom = new FileWriter("AllTriangles-Random-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
				

		//agg.testingActiveAttacks(vernum, edgenum, attackers, outOriginal, outOddCycle, outOddCycleRandom, outShortSingleCycle, outShortSingleCycleRandom, outAllTriangles, outAllTrianglesRandom);
		//agg.testingActiveAttacks(vernum, edgenum, attackers, outOriginalWalkBased, outOriginalCutBased, outOddCycleWalkBased, outOddCycleCutBased, outOddCycleRandomWalkBased, outOddCycleRandomCutBased, outShortSingleCycleWalkBased, outShortSingleCycleCutBased, outShortSingleCycleRandomWalkBased, outShortSingleCycleRandomCutBased, outLargeSingleCycleWalkBased, outLargeSingleCycleCutBased, outLargeSingleCycleRandomWalkBased, outLargeSingleCycleRandomCutBased);
		
		String fileNameOutOriginalWalkBased = "Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers;
		String fileNameOutOriginalCutBased = "Original-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers;
		String fileNameOutOddCycleWalkBased = "OddCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers;
		String fileNameOutOddCycleCutBased = "OddCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers;
		String fileNameOutOddCycleRandomWalkBased = "OddCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers;
		String fileNameOutOddCycleRandomCutBased = "OddCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers;
		String fileNameOutShortSingleCycleWalkBased = "ShortSingleCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers;
		String fileNameOutShortSingleCycleCutBased = "ShortSingleCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers;
		String fileNameOutShortSingleCycleRandomWalkBased = "ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers;
		String fileNameOutShortSingleCycleRandomCutBased = "ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers;
		String fileNameOutLargeSingleCycleWalkBased = "LargeSingleCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers;
		String fileNameOutLargeSingleCycleCutBased = "LargeSingleCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers;
		String fileNameOutLargeSingleCycleRandomWalkBased = "LargeSingleCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackers;
		String fileNameOutLargeSingleCycleRandomCutBased = "LargeSingleCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackers;
		
		agg.testingActiveAttacks(vernum, edgenum, attackers, fileNameOutOriginalWalkBased, fileNameOutOriginalCutBased, fileNameOutOddCycleWalkBased, fileNameOutOddCycleCutBased, fileNameOutOddCycleRandomWalkBased, fileNameOutOddCycleRandomCutBased, fileNameOutShortSingleCycleWalkBased, fileNameOutShortSingleCycleCutBased, fileNameOutShortSingleCycleRandomWalkBased, fileNameOutShortSingleCycleRandomCutBased, fileNameOutLargeSingleCycleWalkBased, fileNameOutLargeSingleCycleCutBased, fileNameOutLargeSingleCycleRandomWalkBased, fileNameOutLargeSingleCycleRandomCutBased);
		
//		outOriginalWalkBased.close();
//		outOriginalCutBased.close();
//		outOddCycleWalkBased.close();
//		outOddCycleCutBased.close();
//		outOddCycleRandomWalkBased.close();
//		outOddCycleRandomCutBased.close();
//		outShortSingleCycleWalkBased.close();
//		outShortSingleCycleCutBased.close();
//		outShortSingleCycleRandomWalkBased.close();
//		outShortSingleCycleRandomCutBased.close();
//		outLargeSingleCycleWalkBased.close();
//		outLargeSingleCycleCutBased.close();
//		outLargeSingleCycleRandomWalkBased.close();
//		outLargeSingleCycleRandomCutBased.close();
		//outAllTriangles.close();
		//outAllTrianglesRandom.close();

		//System.out.println("========================================");
		
	}
	
	public static void main250Panzarasa(String[] args) throws NoSuchAlgorithmException, IOException {
		int attackers = Integer.parseInt(args[0]);	
		
		AttackThreeMethod agg = new AttackThreeMethod();

		Writer outOriginal = new FileWriter("panzarasa-"+attackers+".DAT", true);
		Writer outOddCycle = new FileWriter("panzarasa-anonymized-odd-cycle-"+attackers+".DAT", true);
		Writer outOddCycleRandom = new FileWriter("panzarasa-anonymized-randomly-eqv-odd-cycle-"+attackers+".DAT", true);
		Writer outShortSingleCycle = new FileWriter("panzarasa-anonymized-short-single-cycle-"+attackers+".DAT", true);
		Writer outShortSingleCycleRandom = new FileWriter("panzarasa-anonymized-randomly-eqv-short-single-cycle-"+attackers+".DAT", true);
		Writer outAllTriangles = new FileWriter("panzarasa-anonymized-all-triangles-"+attackers+".DAT", true);
		Writer outAllTrianglesRandom = new FileWriter("panzarasa-anonymized-randomly-eqv-all-triangles-"+attackers+".DAT", true);

		System.out.println("Building panzarasa graph");
		SimpleGraph<String, DefaultEdge> graph = new PanzarasaGraph(DefaultEdge.class);

		agg.testingWalkBasedAttack(graph, attackers, outOriginal, outOddCycle, outOddCycleRandom, outShortSingleCycle, outShortSingleCycleRandom, outAllTriangles, outAllTrianglesRandom);
		
		outOriginal.close();
		outOddCycle.close();
		outOddCycleRandom.close();
		outShortSingleCycle.close();
		outShortSingleCycleRandom.close();
		outAllTriangles.close();
		outAllTrianglesRandom.close();

		System.out.println("========================================");
		
	}

	public static void main250Facebook(String[] args) throws NoSuchAlgorithmException, IOException {
		int attackers = Integer.parseInt(args[0]);	
		
		AttackThreeMethod agg = new AttackThreeMethod();

		Writer outOriginal = new FileWriter("facebook-"+attackers+".DAT", true);
		Writer outOddCycle = new FileWriter("facebook-anonymized-odd-cycle-"+attackers+".DAT", true);
		Writer outOddCycleRandom = new FileWriter("facebook-anonymized-randomly-eqv-odd-cycle-"+attackers+".DAT", true);
		Writer outShortSingleCycle = new FileWriter("facebook-anonymized-short-single-cycle-"+attackers+".DAT", true);
		Writer outShortSingleCycleRandom = new FileWriter("facebook-anonymized-randomly-eqv-short-single-cycle-"+attackers+".DAT", true);
		Writer outAllTriangles = new FileWriter("facebook-anonymized-all-triangles-"+attackers+".DAT", true);
		Writer outAllTrianglesRandom = new FileWriter("facebook-anonymized-randomly-eqv-all-triangles-"+attackers+".DAT", true);

		System.out.println("Building facebook graph");
		SimpleGraph<String, DefaultEdge> graph = new FacebookGraph(DefaultEdge.class);

		agg.testingWalkBasedAttack(graph, attackers, outOriginal, outOddCycle, outOddCycleRandom, outShortSingleCycle, outShortSingleCycleRandom, outAllTriangles, outAllTrianglesRandom);
		
		outOriginal.close();
		outOddCycle.close();
		outOddCycleRandom.close();
		outShortSingleCycle.close();
		outShortSingleCycleRandom.close();
		outAllTriangles.close();
		outAllTrianglesRandom.close();

		System.out.println("========================================");
		
	}
	
	public static void oneRunFacebookPanzURV(String[] args) throws NoSuchAlgorithmException, IOException {
		
		int action = Integer.parseInt(args[1]);
		
		AttackThreeMethod agg = new AttackThreeMethod();
		
		if (args[0].equals("-facebook")) {
			SimpleGraph<String, DefaultEdge> graph = new FacebookGraph(DefaultEdge.class);
			agg.runOneMethodOneLargeNetwork(graph, "facebook", action);
		} else if (args[0].equals("-panzarasa")) {
			SimpleGraph<String, DefaultEdge> graph = new PanzarasaGraph(DefaultEdge.class);
			agg.runOneMethodOneLargeNetwork(graph, "panzarasa", action);
		} else if (args[0].equals("-urv")) {
			SimpleGraph<String, DefaultEdge> graph = new URVMailGraph(DefaultEdge.class);
			agg.runOneMethodOneLargeNetwork(graph, "urv", action);
		} else if (args[0].startsWith("-ego-")) {
			String egoNetworkId = args[0].substring(5, args[0].length());
			SimpleGraph<String, DefaultEdge> graph = new FacebookEgoNetwork(DefaultEdge.class, egoNetworkId);
			agg.runOneMethodOneLargeNetwork(graph, "ego-network-" + egoNetworkId, action);
		}
	}
	
	public static void oneRunAttackedFacebookPanzURV(String[] args) throws NoSuchAlgorithmException, IOException {
		
		int attackType = (args[1].equals("-walk"))? 0 : 1;
		int attackerCount = Integer.parseInt(args[2]);
		int action = Integer.parseInt(args[3]);
		
		AttackThreeMethod agg = new AttackThreeMethod();
		
		if (args[0].equals("-facebook")) {
			SimpleGraph<String, DefaultEdge> graph = new FacebookGraph(DefaultEdge.class);
			agg.runOneMethodOneLargeNetworkOneAttack(graph, "facebook", attackType, attackerCount, action);
		} else if (args[0].equals("-panzarasa")) {
			SimpleGraph<String, DefaultEdge> graph = new PanzarasaGraph(DefaultEdge.class);
			agg.runOneMethodOneLargeNetworkOneAttack(graph, "panzarasa", attackType, attackerCount, action);
		} else if (args[0].equals("-urv")) {
			SimpleGraph<String, DefaultEdge> graph = new URVMailGraph(DefaultEdge.class);
			agg.runOneMethodOneLargeNetworkOneAttack(graph, "urv", attackType, attackerCount, action);
		}
	}
	

	public static void mainExperiment1ExtendedVersion(String[] args) throws NoSuchAlgorithmException, IOException {
		//System.setOut(new PrintStream("report.txt"));
		
		//int vernum = 50;
		int vernum = 250;
		
		//double[] densities = new double[]{0.025, 0.05, 0.075, 0.1, 0.125, 0.15, 0.175, 0.2, 0.225, 0.25, 0.275, 0.3};
		//double[] densities = new double[]{0.5, 0.75, 0.1, 0.125, 0.15, 0.175, 0.2, 0.225, 0.25, 0.275, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		//double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		int attackerSizes[] = new int[]{1,2,4,8,16};
		
		if (args.length == 3) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackerSizes = new int[1];
			attackerSizes[0] = Integer.parseInt(args[2]);
		}   // else the defaults defined above are taken
		
		for (double density :densities){
			
			//System.out.println("Working out density = "+density);
			
			
			for (int attackerSize : attackerSizes){
				int edgenum = getEdgeNum(vernum,density);
				
				AttackThreeMethod agg = new AttackThreeMethod();
		
				//System.out.println("Now the density is "+ density+ " with "+ attackerSize+" attackers and edgenumber:"+edgenum);
		
//				Writer outOriginalWalkBased = new FileWriter("Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize+".DAT", true);
//				Writer outOriginalCutBased = new FileWriter("Original-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize+".DAT", true);
//				Writer outOddCycleWalkBased = new FileWriter("OddCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize+".DAT", true);
//				Writer outOddCycleCutBased = new FileWriter("OddCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize+".DAT", true);
//				Writer outOddCycleRandomWalkBased = new FileWriter("OddCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize+".DAT", true);
//				Writer outOddCycleRandomCutBased = new FileWriter("OddCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize+".DAT", true);
//				Writer outShortSingleCycleWalkBased = new FileWriter("ShortSingleCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize+".DAT", true);
//				Writer outShortSingleCycleCutBased = new FileWriter("ShortSingleCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize+".DAT", true);
//				Writer outShortSingleCycleRandomWalkBased = new FileWriter("ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize+".DAT", true);
//				Writer outShortSingleCycleRandomCutBased = new FileWriter("ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize+".DAT", true);
//				Writer outLargeSingleCycleWalkBased = new FileWriter("LargeSingleCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize+".DAT", true);
//				Writer outLargeSingleCycleCutBased = new FileWriter("LargeSingleCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize+".DAT", true);
//				Writer outLargeSingleCycleRandomWalkBased = new FileWriter("LargeSingleCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize+".DAT", true);
//				Writer outLargeSingleCycleRandomCutBased = new FileWriter("LargeSingleCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize+".DAT", true);
				//Writer outAllTriangles = new FileWriter("AllTriangles-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
				//Writer outAllTrianglesRandom = new FileWriter("AllTriangles-Random-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
		
				//agg.testingActiveAttacks(vernum, edgenum, attackers, outOriginalWalkBased, outOddCycleWalkBased, outOddCycleRandomWalkBased, outShortSingleCycleWalkBased, outShortSingleCycleRandomWalkBased, outAllTriangles, outAllTrianglesRandom);
				//agg.testingActiveAttacks(vernum, edgenum, attackerSize, outOriginalWalkBased, outOriginalCutBased, outOddCycleWalkBased, outOddCycleCutBased, outOddCycleRandomWalkBased, outOddCycleRandomCutBased, outShortSingleCycleWalkBased, outShortSingleCycleCutBased, outShortSingleCycleRandomWalkBased, outShortSingleCycleRandomCutBased, outLargeSingleCycleWalkBased, outLargeSingleCycleCutBased, outLargeSingleCycleRandomWalkBased, outLargeSingleCycleRandomCutBased);
				
				String fileNameOutOriginalWalkBased = "Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutOriginalCutBased = "Original-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutOddCycleWalkBased = "OddCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutOddCycleCutBased = "OddCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutOddCycleRandomWalkBased = "OddCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutOddCycleRandomCutBased = "OddCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutShortSingleCycleWalkBased = "ShortSingleCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutShortSingleCycleCutBased = "ShortSingleCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutShortSingleCycleRandomWalkBased = "ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutShortSingleCycleRandomCutBased = "ShortSingleCycle-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutLargeSingleCycleWalkBased = "LargeSingleCycle-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutLargeSingleCycleCutBased = "LargeSingleCycle-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutLargeSingleCycleRandomWalkBased = "LargeSingleCycle-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutLargeSingleCycleRandomCutBased = "LargeSingleCyclel-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				
				agg.testingActiveAttacks(vernum, edgenum, attackerSize, fileNameOutOriginalWalkBased, fileNameOutOriginalCutBased, fileNameOutOddCycleWalkBased, fileNameOutOddCycleCutBased, fileNameOutOddCycleRandomWalkBased, fileNameOutOddCycleRandomCutBased, fileNameOutShortSingleCycleWalkBased, fileNameOutShortSingleCycleCutBased, fileNameOutShortSingleCycleRandomWalkBased, fileNameOutShortSingleCycleRandomCutBased, fileNameOutLargeSingleCycleWalkBased, fileNameOutLargeSingleCycleCutBased, fileNameOutLargeSingleCycleRandomWalkBased, fileNameOutLargeSingleCycleRandomCutBased);
				
//				outOriginalWalkBased.close();
//				outOriginalCutBased.close();
//				outOddCycleWalkBased.close();
//				outOddCycleCutBased.close();
//				outOddCycleRandomWalkBased.close();
//				outOddCycleRandomCutBased.close();
//				outShortSingleCycleWalkBased.close();
//				outShortSingleCycleCutBased.close();
//				outShortSingleCycleRandomWalkBased.close();
//				outShortSingleCycleRandomCutBased.close();
//				outLargeSingleCycleWalkBased.close();
//				outLargeSingleCycleCutBased.close();
//				outLargeSingleCycleRandomWalkBased.close();
//				outLargeSingleCycleRandomCutBased.close();
				//outAllTriangles.close();
				//outAllTrianglesRandom.close();
				//System.out.println("========================================");
				
			}
		}
	}
	
	public static void experiment11AdjacencyAnonymity(String[] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 250;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		int attackerSizes[] = new int[]{1,2,4,8,16,32};
		
		if (args.length == 3) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackerSizes = new int[1];
			attackerSizes[0] = Integer.parseInt(args[2]);
		}   // else the defaults defined above are taken
		
		for (double density : densities){
			
			for (int attackerSize : attackerSizes) {
				
				int edgenum = getEdgeNum(vernum, density);
				
				AttackThreeMethod agg = new AttackThreeMethod();
						
				String fileNameOutOriginalWalkBased = "Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutOriginalCutBased = "Original-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutFewerEdgesWalkBased = "FewerEdges-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutFewerEdgesCutBased = "FewerEdges-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutFewerEdgesRandomWalkBased = "FewerEdges-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutFewerEdgesRandomCutBased = "FewerEdges-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutBestScoredEdgesWalkBased = "BestScoredEdges-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutBestScoredEdgesCutBased = "BestScoredEdges-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				String fileNameOutBestScoredEdgesRandomWalkBased = "BestScoredEdges-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerSize;
				String fileNameOutBestScoredEdgesRandomCutBased = "BestScoredEdges-Random-V-"+vernum+"-D-"+density+"-CutBased-A-"+attackerSize;
				
				agg.testingActiveAttacksAdj11(vernum, edgenum, attackerSize, fileNameOutOriginalWalkBased, fileNameOutOriginalCutBased, fileNameOutFewerEdgesWalkBased, fileNameOutFewerEdgesCutBased, fileNameOutFewerEdgesRandomWalkBased, fileNameOutFewerEdgesRandomCutBased, fileNameOutBestScoredEdgesWalkBased, fileNameOutBestScoredEdgesCutBased, fileNameOutBestScoredEdgesRandomWalkBased, fileNameOutBestScoredEdgesRandomCutBased);
				
			}
		}
	}
	
	//public void testingActiveAttacks(int n,int m, int attackersSize,
			//Writer outOriginal, Writer outOddCycle, Writer outOddCycleRandom,
			//Writer outShortSingleCycle, Writer outShortSingleCycleRandom,
			//Writer outAllTriangles, Writer outAllTrianglesRandom) throws NoSuchAlgorithmException, IOException{
	//public void testingActiveAttacks(int n,int m, int attackersSize,
			//Writer outOriginalWalkBased, Writer outOriginalCutBased,
			//Writer outOddCycleWalkBased, Writer outOddCycleCutBased,
			//Writer outOddCycleRandomWalkBased, Writer outOddCycleRandomCutBased,
			//Writer outShortSingleCycleWalkBased, Writer outShortSingleCycleCutBased,
			//Writer outShortSingleCycleRandomWalkBased, Writer outShortSingleCycleRandomCutBased,
			//Writer outLargeSingleCycleWalkBased, Writer outLargeSingleCycleCutBased,
			//Writer outLargeSingleCycleRandomWalkBased, Writer outLargeSingleCycleRandomCutBased) throws NoSuchAlgorithmException, IOException{
	
	
	// This is for the experiments where graphs are randomly generated given a density value
	public void testingActiveAttacks(int n,int m, int attackersSize,
			String fileNameOutOriginalWalkBased, String fileNameOutOriginalCutBased,
			String fileNameOutOddCycleWalkBased, String fileNameOutOddCycleCutBased,
			String fileNameOutOddCycleRandomWalkBased, String fileNameOutOddCycleRandomCutBased,
			String fileNameOutShortSingleCycleWalkBased, String fileNameOutShortSingleCycleCutBased,
			String fileNameOutShortSingleCycleRandomWalkBased, String fileNameOutShortSingleCycleRandomCutBased,
			String fileNameOutLargeSingleCycleWalkBased, String fileNameOutLargeSingleCycleCutBased,
			String fileNameOutLargeSingleCycleRandomWalkBased, String fileNameOutLargeSingleCycleRandomCutBased) throws NoSuchAlgorithmException, IOException{
		
		if (attackersSize > n) throw new IllegalArgumentException("The number of attacker cannot be higher " +
				"than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outOriginalCutBased = new FileWriter(fileNameOutOriginalCutBased+".DAT", true);
		Writer outOddCycleWalkBased = new FileWriter(fileNameOutOddCycleWalkBased+".DAT", true);
		Writer outOddCycleCutBased = new FileWriter(fileNameOutOddCycleCutBased+".DAT", true);
		Writer outOddCycleRandomWalkBased = new FileWriter(fileNameOutOddCycleRandomWalkBased+".DAT", true);
		Writer outOddCycleRandomCutBased = new FileWriter(fileNameOutOddCycleRandomCutBased+".DAT", true);
		Writer outShortSingleCycleWalkBased = new FileWriter(fileNameOutShortSingleCycleWalkBased+".DAT", true);
		Writer outShortSingleCycleCutBased = new FileWriter(fileNameOutShortSingleCycleCutBased+".DAT", true);
		Writer outShortSingleCycleRandomWalkBased = new FileWriter(fileNameOutShortSingleCycleRandomWalkBased+".DAT", true);
		Writer outShortSingleCycleRandomCutBased = new FileWriter(fileNameOutShortSingleCycleRandomCutBased+".DAT", true);
		Writer outLargeSingleCycleWalkBased = new FileWriter(fileNameOutLargeSingleCycleWalkBased+".DAT", true);
		Writer outLargeSingleCycleCutBased = new FileWriter(fileNameOutLargeSingleCycleCutBased+".DAT", true);
		Writer outLargeSingleCycleRandomWalkBased = new FileWriter(fileNameOutLargeSingleCycleRandomWalkBased+".DAT", true);
		Writer outLargeSingleCycleRandomCutBased = new FileWriter(fileNameOutLargeSingleCycleRandomCutBased+".DAT", true);

		/*Trujillo- Feb 3, 2016
		 * We will first define the number of targeted vertices
		 * We take the square of the number of attacker as suggested 
		 * in the original paper <-- Ramirez - Sep 8, 2016: seems to be an old comment*/
		//int victimsSize = attackersSize*attackersSize/2;
		//int victimsSize = 1;
		int victimsSizeWalkBased = attackersSize;
		if (victimsSizeWalkBased == 0)
			victimsSizeWalkBased = 1;
		
		int victimsSizeCutBased = (attackersSize - 3) / 3;
		if (victimsSizeCutBased == 0)
			victimsSizeCutBased = 1;
		
		/*Trujillo- Feb 3, 2016
		 * The degree of each attacking vertex should be between log n and 
		 * log n/2 <-- Ramirez - Sep 8, 2016: seems to be an old comment*/
		//int maxDegree = (int)(Math.log10(n)/Math.log10(2));   // <-- Ramirez - Sep 8, 2016: seems to be part of an old implementation
		
		/*Trujillo- Feb 3, 2016
		 * it may be the case that the number of victims is higher than the number of vertices
		 * In this case we restrict the number of victims to remaining vertices*/

		if (attackersSize + victimsSizeWalkBased > n)
			//victimsSizeWalkBased = m-attackersSize;   <-- Ramirez - Sep 9, 2016: fixed error (it appears that this situation never actually occurred in the paper experiments so it shouldn't have brought any consequences)
			victimsSizeWalkBased = n - attackersSize;
		
		if (attackersSize + victimsSizeCutBased > n)
			victimsSizeCutBased = n - attackersSize;
		
		SecureRandom random = new SecureRandom(); 
		/*Trujillo- Jan 21, 2016
		 * This loops will iterate over all graphs, which are by the 
		 * way going to be generated within this loop and then analyzed*/
		double timeDifAvg = 0;
		int total = 100000; 

		for (int i = 1; i <= total; i++){
			//System.out.println("This is the "+i+"th graph.");
			//System.out.println("===============================================");
			double timeIni = System.currentTimeMillis();
			UndirectedGraph<String, DefaultEdge> walkBasedAttackedGraph = null, cutBasedAttackedGraph = null;
			ConnectivityInspector<String, DefaultEdge> connectivityWalkBased = null, connectivityCutBased = null;
			do{
				//System.out.println("Looking for a connected random graph");
				/*Trujillo- Feb 3, 2016
				 * The structure of the vertex factory is as follows:
				 * - vertices from 0 to attackersSize-1 are attackers
				 * - vertices from attackersSize to attackersSize+victimsSize-1 are victims
				 * - the remaining vertices are just normal*/
				final int startingVertex = attackersSize;
				VertexFactory<String> vertexFactory = new VertexFactory<String>(){
					int i = startingVertex;
					@Override
					public String createVertex() {
						int result = i;
						i++;
						return result+"";
					}
					
				};
				
				/*Trujillo- Feb 3, 2016
				 * Next, we choose the degree of each attacking vertex*/
				/*int[] degrees = new int[attackersSize];
				int totalExternalDegrees = 0;
				for (int j = 0; j < degrees.length; j++) {
					degrees[j] = minDegree + random.nextInt(maxDegree-minDegree) + 1;
					totalExternalDegrees += degrees[j];
				} <-- Ramirez - Sep 8, 2016: seems to be part of an old implementation*/
				
				/*Trujillo- Feb 4, 2016
				 * We should also account for the number of internal edges, which is
				 * in average the x(x-1)/4 where x is the number of attackers node <-- Ramirez - Sep 8, 2016: seems to be part of an old implementation*/
				
				walkBasedAttackedGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

				RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);
				
				generator.generateGraph(walkBasedAttackedGraph, vertexFactory, null);

				/*Trujillo- Feb 12, 2016
				 * Once that graph has been created, we add the attacker vertices*/
				for (int j = 0; j < attackersSize; j++) {
					walkBasedAttackedGraph.addVertex(j+"");
				}
				
				/* Ramirez - Sep 8, 2016
				 * From this point on, the walk-based-attacked graph and the cut-based-attacked graph will not be the same 
				 * */
				
				cutBasedAttackedGraph = GraphUtil.cloneGraph(walkBasedAttackedGraph);   // Branch-out a clone of the current state of walkBasedAttackedGraph 
				
				/* Trujillo- Feb 3, 2016
				 * Next we add the edges between victims and to the attackers. This
				 * links should give a unique fingerprint to each victim
				 * Ramirez - Aug 8, 2016
				 * The following code is the original for the walk-based attack*/
				Hashtable<String, String> fingerprints = new Hashtable<>();
				for (int j = attackersSize; j < attackersSize + victimsSizeWalkBased; j++){
					//int degree = minDegree + random.nextInt(maxDegree-minDegree) + 1;    <-- Ramirez - Sep 8, 2016: seems to be part of an old implementation
					String fingerprint = null;
					do{
						fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, attackersSize)-1) + 1);
						//next we add zeros to the left
						while (fingerprint.length() < attackersSize)
							fingerprint = "0"+fingerprint;
					}while (fingerprints.containsKey(fingerprint));
					
					//System.out.println("Fingerprint found = "+fingerprint);
					fingerprints.put(fingerprint, fingerprint);
					
					//next we actually add the edges to the graph 
					for (int k = 0; k < fingerprint.length(); k++) {
						if (fingerprint.charAt(k) == '1'){
							//System.out.println("adding edge ("+j+","+(k+attackersSize-fingerprint.length())+")");
							walkBasedAttackedGraph.addEdge(j+"", (k+attackersSize-fingerprint.length())+"");
						}
					}
					
				}
				
				/*Trujillo- Feb 3, 2016
				 * We also create the path 0-1-2... formed by attacker nodes*/
				if (attackersSize > 1) {
					for (int k = 0; k < attackersSize-1; k++) {
						walkBasedAttackedGraph.addEdge(k+"", (k+1)+"");
					}
				} //   <-- Ramirez - Sep 8, 2016: had been commented out as a part of an old implementation (note the authors of the method indicate they do that)*/
				
				/*Trujillo- Feb 3, 2016
				 * And finally we add random edges between the attacker nodes
				 * Ramirez - Aug 8, 2016
				 * The following code is for the walk-based attack*/
				for (int k = 0; k < attackersSize - 2; k++) {
					for (int l = k + 2; l < attackersSize; l++) {
						if (random.nextBoolean() && !walkBasedAttackedGraph.containsEdge(k+"", l+"")) {
							walkBasedAttackedGraph.addEdge(k+"", l+"");
						}
					}						
				}
				
				/* Ramirez - Sep 8, 2016
				 * Adding edges between victims and attackers for the cut-based-attacked graph
				 * */
				
				HashMap<Integer, Integer> attackerVictimMap = new HashMap<>();
				for (int j = attackersSize; j < attackersSize + victimsSizeCutBased; j++) {
					int attackerId = random.nextInt(attackersSize);
					while (attackerVictimMap.containsKey(attackerId))
						attackerId = random.nextInt(attackersSize);
					attackerVictimMap.put(attackerId, j);
					cutBasedAttackedGraph.addEdge(attackerId+"", j+"");
				}
				
				/* Ramirez - Sep 8, 2016
				 * Randomly adding edges between attackers for the cut-based-attacked graph
				 * */
				
				for (int k = 0; k < attackersSize - 1; k++)
					for (int l = k + 1; l < attackersSize; l++)
						if (random.nextBoolean() && !cutBasedAttackedGraph.containsEdge(k+"", l+""))
							cutBasedAttackedGraph.addEdge(k+"", l+"");
				
				/* Trujillo- Feb 3, 2016
				 * After all of this, we still need to make sure that the graph is connected
				 * Ramirez - Sep 9, 2016
				 * Actually, now we have to make sure that both graphs are connected*/
				connectivityWalkBased = new ConnectivityInspector<>(walkBasedAttackedGraph);
				connectivityCutBased = new ConnectivityInspector<>(cutBasedAttackedGraph);
			} while(!connectivityWalkBased.isGraphConnected() || !connectivityCutBased.isGraphConnected());
			
			//System.out.println("For the walk-based attack, a connected random graph with "+walkBasedAttackedGraph.vertexSet().size()+" vertices" +
					//" and "+walkBasedAttackedGraph.edgeSet().size()+" edges was found, whith density = "+GraphUtil.computeDensity(walkBasedAttackedGraph));
			//System.out.println("For the cut-based attack, a connected random graph with "+cutBasedAttackedGraph.vertexSet().size()+" vertices" +
					//" and "+cutBasedAttackedGraph.edgeSet().size()+" edges was found, whith density = "+GraphUtil.computeDensity(cutBasedAttackedGraph));
			
			/*Trujillo- Jan 25, 2016
			 * Next, we compute (if we can) the value of k such that
			 * the graph is (k,1)-anonymous*/
			//int[][] matrix = GraphUtil.transformIntoAdjacencyMatrix(graph);   <-- Ramirez - Sep 8, 2016: seems to be part of an old implementation
			
			//System.out.println("Computing floyd for original");
			FloydWarshallShortestPaths<String, DefaultEdge> originalWalkBasedAttackedGraphFloyd = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);

			Statistics.printStatistics(i, outOriginalWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd, fileNameOutOriginalWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			GraphUtil.outputSNAPFormat(walkBasedAttackedGraph, fileNameOutOriginalWalkBased + "-" + i + ".txt");
			
			FloydWarshallShortestPaths<String, DefaultEdge> originalCutBasedAttackedGraphFloyd = new FloydWarshallShortestPaths<>(cutBasedAttackedGraph);

			Statistics.printStatistics(i, outOriginalCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd, fileNameOutOriginalCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			//System.out.println("The degree of the victim node at the orignal graph is "+graph.degreeOf("1"));
			//System.out.println("The degree of the attacker node at the orignal graph is "+graph.degreeOf("0"));
			
			/* Ramirez - Aug 10, 2016
			 * We will obtain all 3 anonymized versions (OddCycle, ShortSingleCycle and AllTriangles).
			 * For each one, a randomly modified graph will also be generated.
			 * Ramirez - Sep 8, 2016
			 * Actually, temporarily we will only work on OddCycle and ShortSingleCycle
			 * (and LargeSingleCycle)
			 * */
			
			//System.out.println("Cloning original");
			SimpleGraph<String, DefaultEdge> graphRandomEqvOddCycleWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEqvShortSingleCycleWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEqvLargeSingleCycleWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEqvOddCycleCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEqvShortSingleCycleCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEqvLargeSingleCycleCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph);
			//SimpleGraph<String, DefaultEdge> graphRandomEqvAllTriangles = GraphUtil.cloneGraph(walkBasedAttackedGraph);

			//System.out.println("Dealing with end-vertices for this graph adds " + addedEdgeNumDegreeOne + " edges.");
			
			/*Trujillo- Jan 26, 2016
			 * From now in we will use the new version of arc, which does not 
			 * contain end vertices*/
			
			// Anonymizing with OddCycle
			
			// On walk-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousOddCycleWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			
			//System.out.println("Anonymizing graph");
			//this method below anonymizes the graph using OddCycle 
			
			OddCycle.anonymizeGraph(graphAnonymousOddCycleWalkBased, originalWalkBasedAttackedGraphFloyd, 3);   // Currently in paper runs with 0
			
			int addedEdgesOddCycleWalkBased = graphAnonymousOddCycleWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesOddCycleWalkBased, graphRandomEqvOddCycleWalkBased);

			if (graphRandomEqvOddCycleWalkBased.edgeSet().size() != graphAnonymousOddCycleWalkBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvOddCycleWalkBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousOddCycleWalkBased.edgeSet().size());
			//System.out.println(addedEdges+" edges has been added in the graph using CPA.");
			
			//System.out.println("The degree of the victim node after applying CPA is "+graphTwo1Back.degreeOf("1"));
			//System.out.println("The degree of the attacker node node after applying CPA is "+graphTwo1Back.degreeOf("0"));
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphOddCycleFloydWalkBased = new FloydWarshallShortestPaths<>(graphAnonymousOddCycleWalkBased);
			
			Statistics.printStatistics(i, outOddCycleWalkBased, graphAnonymousOddCycleWalkBased, anonymousGraphOddCycleFloydWalkBased, fileNameOutOddCycleWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			//GraphUtil.outputSNAPFormat(graphAnonymousOddCycleWalkBased, fileNameOutOddCycleWalkBased + "-" + i + ".txt");
			
			//System.out.println("The degree of the victim node after applying CPA-random is "+graphTwo1BackRandom.degreeOf("1"));
			//System.out.println("The degree of the attacker node node after applying CPA-random is "+graphTwo1BackRandom.degreeOf("0"));
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvOddCycleFloydWalkBased = new FloydWarshallShortestPaths<>(graphRandomEqvOddCycleWalkBased);
			
			Statistics.printStatistics(i, outOddCycleRandomWalkBased, graphRandomEqvOddCycleWalkBased, randomGraphEqvOddCycleFloydWalkBased, fileNameOutOddCycleRandomWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			// On cut-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousOddCycleCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph); 
			
			OddCycle.anonymizeGraph(graphAnonymousOddCycleCutBased, originalCutBasedAttackedGraphFloyd, 3);   // Currently in paper runs with 0
			
			int addedEdgesOddCycleCutBased = graphAnonymousOddCycleCutBased.edgeSet().size() - cutBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesOddCycleCutBased, graphRandomEqvOddCycleCutBased);

			if (graphRandomEqvOddCycleCutBased.edgeSet().size() != graphAnonymousOddCycleCutBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvOddCycleCutBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousOddCycleCutBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphOddCycleFloydCutBased = new FloydWarshallShortestPaths<>(graphAnonymousOddCycleCutBased);
			
			Statistics.printStatistics(i, outOddCycleCutBased, graphAnonymousOddCycleCutBased, anonymousGraphOddCycleFloydCutBased, fileNameOutOddCycleCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvOddCycleFloydCutBased = new FloydWarshallShortestPaths<>(graphRandomEqvOddCycleCutBased);
			
			Statistics.printStatistics(i, outOddCycleRandomCutBased, graphRandomEqvOddCycleCutBased, randomGraphEqvOddCycleFloydCutBased, fileNameOutOddCycleRandomCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			/* Ramirez - Aug 10, 2016
			 * Anonymizing with ShortSingleCycle
			 **/
			
			// On walk-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousShortSingleCycleWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			ShortSingleCycle.anonymizeGraph(graphAnonymousShortSingleCycleWalkBased, originalWalkBasedAttackedGraphFloyd, 4);   // Currently in paper runs with 0
			
			int addedEdgesShortSingleCycleWalkBased = graphAnonymousShortSingleCycleWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesShortSingleCycleWalkBased, graphRandomEqvShortSingleCycleWalkBased);

			if (graphRandomEqvShortSingleCycleWalkBased.edgeSet().size() != graphAnonymousShortSingleCycleWalkBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvShortSingleCycleWalkBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousShortSingleCycleWalkBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphShortSingleCycleFloydWalkBased = new FloydWarshallShortestPaths<>(graphAnonymousShortSingleCycleWalkBased);
			
			Statistics.printStatistics(i, outShortSingleCycleWalkBased, graphAnonymousShortSingleCycleWalkBased, anonymousGraphShortSingleCycleFloydWalkBased, fileNameOutShortSingleCycleWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			//GraphUtil.outputSNAPFormat(graphAnonymousShortSingleCycleWalkBased, fileNameOutShortSingleCycleWalkBased + "-" + i + ".txt");
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvShortSingleCycleFloydWalkBased = new FloydWarshallShortestPaths<>(graphRandomEqvShortSingleCycleWalkBased);
			
			Statistics.printStatistics(i, outShortSingleCycleRandomWalkBased, graphRandomEqvShortSingleCycleWalkBased, randomGraphEqvShortSingleCycleFloydWalkBased, fileNameOutShortSingleCycleRandomWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
						
			// On cut-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousShortSingleCycleCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph); 
			
			ShortSingleCycle.anonymizeGraph(graphAnonymousShortSingleCycleCutBased, originalCutBasedAttackedGraphFloyd, 4);   // Currently in paper runs with 0
			
			int addedEdgesShortSingleCycleCutBased = graphAnonymousShortSingleCycleCutBased.edgeSet().size() - cutBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesShortSingleCycleCutBased, graphRandomEqvShortSingleCycleCutBased);

			if (graphRandomEqvShortSingleCycleCutBased.edgeSet().size() != graphAnonymousShortSingleCycleCutBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvShortSingleCycleCutBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousShortSingleCycleCutBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphShortSingleCycleFloydCutBased = new FloydWarshallShortestPaths<>(graphAnonymousShortSingleCycleCutBased);
			
			Statistics.printStatistics(i, outShortSingleCycleCutBased, graphAnonymousShortSingleCycleCutBased, anonymousGraphShortSingleCycleFloydCutBased, fileNameOutShortSingleCycleCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvShortSingleCycleFloydCutBased = new FloydWarshallShortestPaths<>(graphRandomEqvShortSingleCycleCutBased);
			
			Statistics.printStatistics(i, outShortSingleCycleRandomCutBased, graphRandomEqvShortSingleCycleCutBased, randomGraphEqvShortSingleCycleFloydCutBased, fileNameOutShortSingleCycleRandomCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			/* Ramirez - Sep 26, 2016
			 * Anonymizing with LargeSingleCycle
			 **/
			
			// On walk-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousLargeSingleCycleWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			LargeSingleCycle.anonymizeGraph(graphAnonymousLargeSingleCycleWalkBased, originalWalkBasedAttackedGraphFloyd, 5);   // Currently in paper runs with 0
			
			int addedEdgesLargeSingleCycleWalkBased = graphAnonymousLargeSingleCycleWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesLargeSingleCycleWalkBased, graphRandomEqvLargeSingleCycleWalkBased);

			if (graphRandomEqvLargeSingleCycleWalkBased.edgeSet().size() != graphAnonymousLargeSingleCycleWalkBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvLargeSingleCycleWalkBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousLargeSingleCycleWalkBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphLargeSingleCycleFloydWalkBased = new FloydWarshallShortestPaths<>(graphAnonymousLargeSingleCycleWalkBased);
			
			Statistics.printStatistics(i, outLargeSingleCycleWalkBased, graphAnonymousLargeSingleCycleWalkBased, anonymousGraphLargeSingleCycleFloydWalkBased, fileNameOutLargeSingleCycleWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			//GraphUtil.outputSNAPFormat(graphAnonymousLargeSingleCycleWalkBased, fileNameOutLargeSingleCycleWalkBased + "-" + i + ".txt");
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvLargeSingleCycleFloydWalkBased = new FloydWarshallShortestPaths<>(graphRandomEqvLargeSingleCycleWalkBased);
			
			Statistics.printStatistics(i, outLargeSingleCycleRandomWalkBased, graphRandomEqvLargeSingleCycleWalkBased, randomGraphEqvLargeSingleCycleFloydWalkBased, fileNameOutLargeSingleCycleRandomWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
						
			// On cut-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousLargeSingleCycleCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph); 
			
			LargeSingleCycle.anonymizeGraph(graphAnonymousLargeSingleCycleCutBased, originalCutBasedAttackedGraphFloyd, 5);   // Currently in paper runs with 0
			
			int addedEdgesLargeSingleCycleCutBased = graphAnonymousLargeSingleCycleCutBased.edgeSet().size() - cutBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesLargeSingleCycleCutBased, graphRandomEqvLargeSingleCycleCutBased);

			if (graphRandomEqvLargeSingleCycleCutBased.edgeSet().size() != graphAnonymousLargeSingleCycleCutBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvLargeSingleCycleCutBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousLargeSingleCycleCutBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphLargeSingleCycleFloydCutBased = new FloydWarshallShortestPaths<>(graphAnonymousLargeSingleCycleCutBased);
			
			Statistics.printStatistics(i, outLargeSingleCycleCutBased, graphAnonymousLargeSingleCycleCutBased, anonymousGraphLargeSingleCycleFloydCutBased, fileNameOutLargeSingleCycleCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvLargeSingleCycleFloydCutBased = new FloydWarshallShortestPaths<>(graphRandomEqvLargeSingleCycleCutBased);
			
			Statistics.printStatistics(i, outLargeSingleCycleRandomCutBased, graphRandomEqvLargeSingleCycleCutBased, randomGraphEqvLargeSingleCycleFloydCutBased, fileNameOutLargeSingleCycleRandomCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			/* Ramirez - Aug 10, 2016
			 * Anonymizing with AllTriangles
			 * Sep 8, 2016
			 * Temporarily commenting this out as this method will not be a part of the current experiments
			 **/
			
			/*
			SimpleGraph<String, DefaultEdge> graphAnonymousAllTriangles = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			AllTriangles.anonymizeGraph(graphAnonymousAllTriangles, originalWalkBasedAttackedGraphFloyd, 1);
			
			int addedEdgesAllTriangles = graphAnonymousAllTriangles.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesAllTriangles, graphRandomEqvAllTriangles);

			if (graphRandomEqvAllTriangles.edgeSet().size() != graphAnonymousAllTriangles.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvAllTriangles.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousAllTriangles.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphAllTrianglesFloyd = new FloydWarshallShortestPaths<>(graphAnonymousAllTriangles);
			
			Statistics.printStatistics(i, outAllTriangles, graphAnonymousAllTriangles, anonymousGraphAllTrianglesFloyd, "AllTriangles", attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvAllTrianglesFloyd = new FloydWarshallShortestPaths<>(graphRandomEqvAllTriangles);
			
			Statistics.printStatistics(i, outAllTrianglesRandom, graphRandomEqvAllTriangles, randomGraphEqvAllTrianglesFloyd, "AllTriangles-Random", attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			*/			
			
			// Round summary
			double timeEnd = System.currentTimeMillis();
			double timeDif = timeEnd - timeIni;
			timeDifAvg = ((i-1)*timeDifAvg + timeDif)/i;
			//double avgTakenTime = 2.7777 * timeDifAvg/Math.pow(10, 7);
			//System.out.println("This round took " + avgTakenTime+ " hours for density = "+GraphUtil.computeDensity(walkBasedAttackedGraph));
			//System.out.println("Remaining time = "+(avgTakenTime*(total-i))+" hours");
		}
		
		outOriginalWalkBased.close();
		outOriginalCutBased.close();
		outOddCycleWalkBased.close();
		outOddCycleCutBased.close();
		outOddCycleRandomWalkBased.close();
		outOddCycleRandomCutBased.close();
		outShortSingleCycleWalkBased.close();
		outShortSingleCycleCutBased.close();
		outShortSingleCycleRandomWalkBased.close();
		outShortSingleCycleRandomCutBased.close();
		outLargeSingleCycleWalkBased.close();
		outLargeSingleCycleCutBased.close();
		outLargeSingleCycleRandomWalkBased.close();
		outLargeSingleCycleRandomCutBased.close();
	}
	
	public void testingActiveAttacksAdj11(int n,int m, int attackersSize,
			String fileNameOutOriginalWalkBased, String fileNameOutOriginalCutBased,
			String fileNameOutFewerEdgesWalkBased, String fileNameOutFewerEdgesCutBased,
			String fileNameOutFewerEdgesRandomWalkBased, String fileNameOutFewerEdgesRandomCutBased,
			String fileNameOutBestScoredEdgesWalkBased, String fileNameOutBestScoredEdgesCutBased,
			String fileNameOutBestScoredEdgesRandomWalkBased, String fileNameOutBestScoredEdgesRandomCutBased) throws NoSuchAlgorithmException, IOException{
		
		if (attackersSize > n) throw new IllegalArgumentException("The number of attacker cannot be higher " +
				"than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outOriginalCutBased = new FileWriter(fileNameOutOriginalCutBased+".DAT", true);
		Writer outFewerEdgesWalkBased = new FileWriter(fileNameOutFewerEdgesWalkBased+".DAT", true);
		Writer outFewerEdgesCutBased = new FileWriter(fileNameOutFewerEdgesCutBased+".DAT", true);
		Writer outFewerEdgesRandomWalkBased = new FileWriter(fileNameOutFewerEdgesRandomWalkBased+".DAT", true);
		Writer outFewerEdgesRandomCutBased = new FileWriter(fileNameOutFewerEdgesRandomCutBased+".DAT", true);
		Writer outBestScoredEdgesWalkBased = new FileWriter(fileNameOutBestScoredEdgesWalkBased+".DAT", true);
		Writer outBestScoredEdgesCutBased = new FileWriter(fileNameOutBestScoredEdgesCutBased+".DAT", true);
		Writer outBestScoredEdgesRandomWalkBased = new FileWriter(fileNameOutBestScoredEdgesRandomWalkBased+".DAT", true);
		Writer outBestScoredEdgesRandomCutBased = new FileWriter(fileNameOutBestScoredEdgesRandomCutBased+".DAT", true);

		int victimsSizeWalkBased = attackersSize;
		if (victimsSizeWalkBased == 0)
			victimsSizeWalkBased = 1;
		
		int victimsSizeCutBased = (attackersSize - 3) / 3;
		if (victimsSizeCutBased == 0)
			victimsSizeCutBased = 1;
		
		if (attackersSize + victimsSizeWalkBased > n)
			victimsSizeWalkBased = n - attackersSize;
		
		if (attackersSize + victimsSizeCutBased > n)
			victimsSizeCutBased = n - attackersSize;
		
		SecureRandom random = new SecureRandom(); 
		
		int collectionSize = 100000; 

		for (int i = 1; i <= collectionSize; i++) {
			
			UndirectedGraph<String, DefaultEdge> walkBasedAttackedGraph = null, cutBasedAttackedGraph = null;
			ConnectivityInspector<String, DefaultEdge> connectivityWalkBased = null, connectivityCutBased = null;
			do {
				/*Trujillo- Feb 3, 2016
				 * The structure of the vertex factory is as follows:
				 * - vertices from 0 to attackersSize-1 are attackers
				 * - vertices from attackersSize to attackersSize+victimsSize-1 are victims
				 * - the remaining vertices are just normal*/
				final int startingVertex = attackersSize;
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

				for (int j = 0; j < attackersSize; j++) {
					walkBasedAttackedGraph.addVertex(j+"");
				}
				
				cutBasedAttackedGraph = GraphUtil.cloneGraph(walkBasedAttackedGraph);   // Branch-out a clone of the current state of walkBasedAttackedGraph 
				
				Hashtable<String, String> fingerprints = new Hashtable<>();
				for (int j = attackersSize; j < attackersSize + victimsSizeWalkBased; j++) {
					String fingerprint = null;
					do {
						fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, attackersSize)-1) + 1);
						while (fingerprint.length() < attackersSize)
							fingerprint = "0"+fingerprint;
					} while (fingerprints.containsKey(fingerprint));
					
					fingerprints.put(fingerprint, fingerprint);
					
					for (int k = 0; k < fingerprint.length(); k++) {
						if (fingerprint.charAt(k) == '1'){
							walkBasedAttackedGraph.addEdge(j+"", (k+attackersSize-fingerprint.length())+"");
						}
					}
					
				}
				
				if (attackersSize > 1) {
					for (int k = 0; k < attackersSize-1; k++) {
						walkBasedAttackedGraph.addEdge(k+"", (k+1)+"");
					}
				}
				
				for (int k = 0; k < attackersSize - 2; k++) {
					for (int l = k + 2; l < attackersSize; l++) {
						if (random.nextBoolean() && !walkBasedAttackedGraph.containsEdge(k+"", l+"")) {
							walkBasedAttackedGraph.addEdge(k+"", l+"");
						}
					}						
				}
				
				HashMap<Integer, Integer> attackerVictimMap = new HashMap<>();
				for (int j = attackersSize; j < attackersSize + victimsSizeCutBased; j++) {
					int attackerId = random.nextInt(attackersSize);
					while (attackerVictimMap.containsKey(attackerId))
						attackerId = random.nextInt(attackersSize);
					attackerVictimMap.put(attackerId, j);
					cutBasedAttackedGraph.addEdge(attackerId+"", j+"");
				}
				
				for (int k = 0; k < attackersSize - 1; k++)
					for (int l = k + 1; l < attackersSize; l++)
						if (random.nextBoolean() && !cutBasedAttackedGraph.containsEdge(k+"", l+""))
							cutBasedAttackedGraph.addEdge(k+"", l+"");
				
				connectivityWalkBased = new ConnectivityInspector<>(walkBasedAttackedGraph);
				connectivityCutBased = new ConnectivityInspector<>(cutBasedAttackedGraph);
			} while (!connectivityWalkBased.isGraphConnected() || !connectivityCutBased.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> originalWalkBasedAttackedGraphFloyd = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);

			Statistics.printStatistics(i, outOriginalWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd, fileNameOutOriginalWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> originalCutBasedAttackedGraphFloyd = new FloydWarshallShortestPaths<>(cutBasedAttackedGraph);

			Statistics.printStatistics(i, outOriginalCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd, fileNameOutOriginalCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			SimpleGraph<String, DefaultEdge> graphRandomEqvFewerEdgesWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEqvBestScoredEdgesWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEqvFewerEdgesCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEqvBestScoredEdgesCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph);

			// Anonymizing with FewerEdges
			
			// On walk-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousFewerEdgesWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			
			BaseCycle.getRidOfExtremeDegreeVertices(graphAnonymousFewerEdgesWalkBased, 0);
			
			int diffEdgesFewerEdgesWalkBased = graphAnonymousFewerEdgesWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			
			if (diffEdgesFewerEdgesWalkBased > 0)
				GraphUtil.addRandomEdges(diffEdgesFewerEdgesWalkBased, graphRandomEqvFewerEdgesWalkBased);
			else if (diffEdgesFewerEdgesWalkBased < 0)
				GraphUtil.removeRandomEdges(-diffEdgesFewerEdgesWalkBased, graphRandomEqvFewerEdgesWalkBased);

			if (graphRandomEqvFewerEdgesWalkBased.edgeSet().size() != graphAnonymousFewerEdgesWalkBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvFewerEdgesWalkBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousFewerEdgesWalkBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphFewerEdgesFloydWalkBased = new FloydWarshallShortestPaths<>(graphAnonymousFewerEdgesWalkBased);
			
			Statistics.printStatistics(i, outFewerEdgesWalkBased, graphAnonymousFewerEdgesWalkBased, anonymousGraphFewerEdgesFloydWalkBased, fileNameOutFewerEdgesWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvFewerEdgesFloydWalkBased = new FloydWarshallShortestPaths<>(graphRandomEqvFewerEdgesWalkBased);
			
			Statistics.printStatistics(i, outFewerEdgesRandomWalkBased, graphRandomEqvFewerEdgesWalkBased, randomGraphEqvFewerEdgesFloydWalkBased, fileNameOutFewerEdgesRandomWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			// On cut-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousFewerEdgesCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph); 
			
			BaseCycle.getRidOfExtremeDegreeVertices(graphAnonymousFewerEdgesCutBased, 0);
			
			int diffEdgesFewerEdgesCutBased = graphAnonymousFewerEdgesCutBased.edgeSet().size() - cutBasedAttackedGraph.edgeSet().size();
			
			if (diffEdgesFewerEdgesCutBased > 0)
				GraphUtil.addRandomEdges(diffEdgesFewerEdgesCutBased, graphRandomEqvFewerEdgesCutBased);
			else if (diffEdgesFewerEdgesCutBased < 0)
				GraphUtil.removeRandomEdges(-diffEdgesFewerEdgesCutBased, graphRandomEqvFewerEdgesCutBased);

			if (graphRandomEqvFewerEdgesCutBased.edgeSet().size() != graphAnonymousFewerEdgesCutBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvFewerEdgesCutBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousFewerEdgesCutBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphFewerEdgesFloydCutBased = new FloydWarshallShortestPaths<>(graphAnonymousFewerEdgesCutBased);
			
			Statistics.printStatistics(i, outFewerEdgesCutBased, graphAnonymousFewerEdgesCutBased, anonymousGraphFewerEdgesFloydCutBased, fileNameOutFewerEdgesCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvFewerEdgesFloydCutBased = new FloydWarshallShortestPaths<>(graphRandomEqvFewerEdgesCutBased);
			
			Statistics.printStatistics(i, outFewerEdgesRandomCutBased, graphRandomEqvFewerEdgesCutBased, randomGraphEqvFewerEdgesFloydCutBased, fileNameOutFewerEdgesRandomCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			// Anonymizing with BestScoredEdges
			
			// On walk-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousBestScoredEdgesWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			BaseCycle.getRidOfExtremeDegreeVertices(graphAnonymousBestScoredEdgesWalkBased, 1);
			
			int diffEdgesBestScoredEdgesWalkBased = graphAnonymousBestScoredEdgesWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			
			if (diffEdgesBestScoredEdgesWalkBased > 0)
				GraphUtil.addRandomEdges(diffEdgesBestScoredEdgesWalkBased, graphRandomEqvBestScoredEdgesWalkBased);
			else if (diffEdgesBestScoredEdgesWalkBased < 0)
				GraphUtil.removeRandomEdges(-diffEdgesBestScoredEdgesWalkBased, graphRandomEqvBestScoredEdgesWalkBased);

			if (graphRandomEqvBestScoredEdgesWalkBased.edgeSet().size() != graphAnonymousBestScoredEdgesWalkBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvBestScoredEdgesWalkBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousBestScoredEdgesWalkBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphBestScoredEdgesFloydWalkBased = new FloydWarshallShortestPaths<>(graphAnonymousBestScoredEdgesWalkBased);
			
			Statistics.printStatistics(i, outBestScoredEdgesWalkBased, graphAnonymousBestScoredEdgesWalkBased, anonymousGraphBestScoredEdgesFloydWalkBased, fileNameOutBestScoredEdgesWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvBestScoredEdgesFloydWalkBased = new FloydWarshallShortestPaths<>(graphRandomEqvBestScoredEdgesWalkBased);
			
			Statistics.printStatistics(i, outBestScoredEdgesRandomWalkBased, graphRandomEqvBestScoredEdgesWalkBased, randomGraphEqvBestScoredEdgesFloydWalkBased, fileNameOutBestScoredEdgesRandomWalkBased, attackersSize, victimsSizeWalkBased, walkBasedAttackedGraph, originalWalkBasedAttackedGraphFloyd);
						
			// On cut-based-attacked graph
			
			SimpleGraph<String, DefaultEdge> graphAnonymousBestScoredEdgesCutBased = GraphUtil.cloneGraph(cutBasedAttackedGraph); 
			
			BaseCycle.getRidOfExtremeDegreeVertices(graphAnonymousBestScoredEdgesCutBased, 1);
			
			int diffEdgesBestScoredEdgesCutBased = graphAnonymousBestScoredEdgesCutBased.edgeSet().size() - cutBasedAttackedGraph.edgeSet().size();
			
			if (diffEdgesBestScoredEdgesCutBased > 0)
				GraphUtil.addRandomEdges(diffEdgesBestScoredEdgesCutBased, graphRandomEqvBestScoredEdgesCutBased);
			else if (diffEdgesBestScoredEdgesCutBased < 0)
				GraphUtil.removeRandomEdges(-diffEdgesBestScoredEdgesCutBased, graphRandomEqvBestScoredEdgesCutBased);

			if (graphRandomEqvBestScoredEdgesCutBased.edgeSet().size() != graphAnonymousBestScoredEdgesCutBased.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandomEqvBestScoredEdgesCutBased.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymousBestScoredEdgesCutBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphBestScoredEdgesFloydCutBased = new FloydWarshallShortestPaths<>(graphAnonymousBestScoredEdgesCutBased);
			
			Statistics.printStatistics(i, outBestScoredEdgesCutBased, graphAnonymousBestScoredEdgesCutBased, anonymousGraphBestScoredEdgesFloydCutBased, fileNameOutBestScoredEdgesCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvBestScoredEdgesFloydCutBased = new FloydWarshallShortestPaths<>(graphRandomEqvBestScoredEdgesCutBased);
			
			Statistics.printStatistics(i, outBestScoredEdgesRandomCutBased, graphRandomEqvBestScoredEdgesCutBased, randomGraphEqvBestScoredEdgesFloydCutBased, fileNameOutBestScoredEdgesRandomCutBased, attackersSize, victimsSizeCutBased, cutBasedAttackedGraph, originalCutBasedAttackedGraphFloyd);
			
		}
		
		outOriginalWalkBased.close();
		outOriginalCutBased.close();
		outFewerEdgesWalkBased.close();
		outFewerEdgesCutBased.close();
		outFewerEdgesRandomWalkBased.close();
		outFewerEdgesRandomCutBased.close();
		outBestScoredEdgesWalkBased.close();
		outBestScoredEdgesCutBased.close();
		outBestScoredEdgesRandomWalkBased.close();
		outBestScoredEdgesRandomCutBased.close();
		
	}
	
	// This is for the experiments with a real social network
	public void runOneMethodOneLargeNetwork(UndirectedGraph<String, DefaultEdge> originalGraph,
			String socNetName, int action) throws NoSuchAlgorithmException, IOException{
		
		ConnectivityInspector<String, DefaultEdge> connectivity = new ConnectivityInspector<>(originalGraph);
		List<Set<String>> connComp = connectivity.connectedSets();
		
		Set<String> verticesToKeep = null;
		int maximum = 0;
		for (Set<String> comp : connComp){
			//System.out.println(comp.size()+"");
			if (comp.size() > maximum){
				maximum = comp.size();
				verticesToKeep = comp;
			}
		}
	
		UndirectedGraph<String, DefaultEdge> graph = GraphUtil.shiftAndShuffleVertexIds(originalGraph, 0, verticesToKeep); 
		
		connectivity = new ConnectivityInspector<>(graph);
		if (!connectivity.isGraphConnected()) throw new RuntimeException();
		
		int originalEdgeCount = graph.edgeSet().size();
		SimpleGraph<String, DefaultEdge> anonymousGraphRandom = null;
		FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalGraph = null;
		
		switch (action) {
		case 0:   // Do not anonymize
			GraphUtil.outputSNAPFormat(graph, socNetName + "-original.txt");
			break;
		case 1:   // Anonymize using OddCycle
			anonymousGraphRandom = GraphUtil.cloneGraph(graph);
			floydOriginalGraph = new FloydWarshallShortestPaths<>(graph);
			OddCycle.anonymizeGraph(graph, floydOriginalGraph, 3);   // Currently in paper runs with 0
			GraphUtil.outputSNAPFormat(graph, socNetName + "-anonymized-oddcycle.txt");
			GraphUtil.addRandomEdges(graph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
			GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + "-randomly-anonymized-equiv-oddcycle.txt");
			break;
		case 2:   // Anonymize using ShortSingleCycle
			anonymousGraphRandom = GraphUtil.cloneGraph(graph);
			floydOriginalGraph = new FloydWarshallShortestPaths<>(graph);
			ShortSingleCycle.anonymizeGraph(graph, floydOriginalGraph, 4);   // Currently in paper runs with 0
			GraphUtil.outputSNAPFormat(graph, socNetName + "-anonymized-shortestcycle.txt");
			GraphUtil.addRandomEdges(graph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
			GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + "-randomly-anonymized-equiv-shortestcycle.txt");
			break;
		case 3:   // Anonymize using LargeSingleCycle
			anonymousGraphRandom = GraphUtil.cloneGraph(graph);
			floydOriginalGraph = new FloydWarshallShortestPaths<>(graph);
			LargeSingleCycle.anonymizeGraph(graph, floydOriginalGraph, 5);   // Currently in paper runs with 0
			GraphUtil.outputSNAPFormat(graph, socNetName + "-anonymized-largestcycle.txt");
			GraphUtil.addRandomEdges(graph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
			GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + "-randomly-anonymized-equiv-largestcycle.txt");
			break;
		case 4:   // Anonymize using FewerEdges
			anonymousGraphRandom = GraphUtil.cloneGraph(graph);
			BaseCycle.getRidOfExtremeDegreeVertices(anonymousGraphRandom, 0);
			GraphUtil.outputSNAPFormat(graph, socNetName + "-anonymized-feweredges.txt");
			if (graph.edgeSet().size() - originalEdgeCount > 0)
				GraphUtil.addRandomEdges(graph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
			else if (graph.edgeSet().size() - originalEdgeCount < 0)
				GraphUtil.removeRandomEdges(originalEdgeCount - graph.edgeSet().size(), anonymousGraphRandom);
			GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + "-randomly-anonymized-equiv-feweredges.txt");
			break;
		case 5:   // Anonymize using FoafEdges
			anonymousGraphRandom = GraphUtil.cloneGraph(graph);
			BaseCycle.getRidOfExtremeDegreeVertices(anonymousGraphRandom, 1);
			GraphUtil.outputSNAPFormat(graph, socNetName + "-anonymized-foafedges.txt");
			if (graph.edgeSet().size() - originalEdgeCount > 0)
				GraphUtil.addRandomEdges(graph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
			else if (graph.edgeSet().size() - originalEdgeCount < 0)
				GraphUtil.removeRandomEdges(originalEdgeCount - graph.edgeSet().size(), anonymousGraphRandom);
			GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + "-randomly-anonymized-equiv-foafedges.txt");
			break;
		default:;
		}	
	}
	
	// This is for the experiments with a real social network
	public void runOneMethodOneLargeNetworkOneAttack(UndirectedGraph<String, DefaultEdge> originalGraph,
			String socNetName, int attackChoice, int attackerCount, int anonymizationChoice) throws NoSuchAlgorithmException, IOException{
		
		int victimCount;
		ConnectivityInspector<String, DefaultEdge> connectivity = new ConnectivityInspector<>(originalGraph);
		List<Set<String>> connComp = connectivity.connectedSets();
		SecureRandom random = new SecureRandom();
		String attackIdStr;
		if (attackChoice == 0)   // Walk-based attack
			attackIdStr = "-walk-based-" + attackerCount;
		else
			attackIdStr = "-cut-based-" + attackerCount;
		
		Set<String> verticesToKeep = null;
		int maximum = 0;
		for (Set<String> comp : connComp){
			//System.out.println(comp.size()+"");
			if (comp.size() > maximum){
				maximum = comp.size();
				verticesToKeep = new HashSet<String>(comp);
			}
		}
		
		//int iterCount = 1;
		int iterCount = 1000;
		
		for (int iter = 0; iter < iterCount; iter++) {
			
			String iterIdStr = "";
			if (iter <= 9)
				iterIdStr += "0";
			iterIdStr += iter;
			
			UndirectedGraph<String, DefaultEdge> attackedGraph = null;
			
			do {
				
				attackedGraph = GraphUtil.shiftAndShuffleVertexIds(originalGraph, attackerCount, verticesToKeep);
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				if (!connectivity.isGraphConnected()) throw new RuntimeException();
				
				// Attack graph
				if (attackChoice == 0) {   // Walk-based attack
					
					victimCount = attackerCount;
					
					if (victimCount == 0)
						victimCount = 1;
					
					if (attackerCount + victimCount > attackedGraph.vertexSet().size())
						victimCount = attackedGraph.vertexSet().size() - attackerCount;
					
					for (int j = 0; j < attackerCount; j++)
						attackedGraph.addVertex(j+"");
					
					Hashtable<String, String> fingerprints = new Hashtable<>();
					for (int j = attackerCount; j < attackerCount + victimCount; j++){
						String fingerprint = null;
						do {
							fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, attackerCount)-1) + 1);
							//next we add zeros to the left
							while (fingerprint.length() < attackerCount)
								fingerprint = "0"+fingerprint;
						} while (fingerprints.containsKey(fingerprint));
						
						fingerprints.put(fingerprint, fingerprint);
						
						for (int k = 0; k < fingerprint.length(); k++) {
							if (fingerprint.charAt(k) == '1'){
								//System.out.println("adding edge ("+j+","+(k+attackersSize-fingerprint.length())+")");
								attackedGraph.addEdge(j+"", (k + attackerCount - fingerprint.length())+"");
							}
						}
					}
					
					if (attackerCount > 1) {
						for (int k = 0; k < attackerCount - 1; k++) {
							attackedGraph.addEdge(k+"", (k+1)+"");
						}
					}				
					
					for (int k = 0; k < attackerCount - 2; k++) {
						for (int l = k + 2; l < attackerCount; l++) {
							if (random.nextBoolean() && !attackedGraph.containsEdge(k+"", l+"")) {
								attackedGraph.addEdge(k+"", l+"");
							}
						}
					}
				}
				else {   // Cut-based attack
					
					victimCount = (attackerCount - 3) / 3;
					
					if (victimCount == 0)
						victimCount = 1;
					
					if (attackerCount + victimCount > attackedGraph.vertexSet().size())
						victimCount = attackedGraph.vertexSet().size() - attackerCount;
					
					for (int j = 0; j < attackerCount; j++)
						attackedGraph.addVertex(j+"");
					
					HashMap<Integer, Integer> attackerVictimMap = new HashMap<>();
					for (int j = attackerCount; j < attackerCount + victimCount; j++) {
						int attackerId = random.nextInt(attackerCount);
						while (attackerVictimMap.containsKey(attackerId))
							attackerId = random.nextInt(attackerCount);
						attackerVictimMap.put(attackerId, j);
						attackedGraph.addEdge(attackerId+"", j+"");
					}
					
					for (int k = 0; k < attackerCount - 1; k++)
						for (int l = k + 1; l < attackerCount; l++)
							if (random.nextBoolean() && !attackedGraph.containsEdge(k+"", l+""))
								attackedGraph.addEdge(k+"", l+"");
				}
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				
			} while (!connectivity.isGraphConnected());
			
			int originalEdgeCount = attackedGraph.edgeSet().size();
			SimpleGraph<String, DefaultEdge> anonymousGraph = null;
			SimpleGraph<String, DefaultEdge> anonymousGraphRandom = null;
			FloydWarshallShortestPaths<String, DefaultEdge> floydAttackedGraph = null;
			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymizedGraph = null;
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomlyAnonymizedGraph = null;
			Writer out;
			
			switch (anonymizationChoice) {
			case 0:   // Do not anonymize
				GraphUtil.outputSNAPFormat(attackedGraph, socNetName + "-original-" + iterIdStr + ".txt");
				out = new FileWriter(socNetName + "-original-" + iterIdStr + "-stats.txt");
				floydAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				Statistics.printStatistics(0, out, attackedGraph, floydAttackedGraph, "original", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				break;
			case 1:   // Anonymize using OddCycle
				anonymousGraph = GraphUtil.cloneGraph(attackedGraph);
				anonymousGraphRandom = GraphUtil.cloneGraph(attackedGraph);
				floydAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				OddCycle.anonymizeGraph(anonymousGraph, floydAttackedGraph, 3);   // Runs with 0 never included in paper
				GraphUtil.outputSNAPFormat(anonymousGraph, socNetName + attackIdStr + "-anonymized-oddcycle-" + iterIdStr + ".txt");
				floydAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraph);
				out = new FileWriter(socNetName + attackIdStr + "-anonymized-oddcycle-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraph, floydAnonymizedGraph, attackIdStr + "-anonymized-oddcycle", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				GraphUtil.addRandomEdges(anonymousGraph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
				GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + attackIdStr + "-randomly-anonymized-equiv-oddcycle-" + iterIdStr + ".txt");
				floydRandomlyAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraphRandom);
				out = new FileWriter(socNetName + attackIdStr + "-randomly-anonymized-oddcycle-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraphRandom, floydRandomlyAnonymizedGraph, attackIdStr + "-randomly-anonymized-oddcycle", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				break;
			case 2:   // Anonymize using ShortSingleCycle
				anonymousGraph = GraphUtil.cloneGraph(attackedGraph);
				anonymousGraphRandom = GraphUtil.cloneGraph(attackedGraph);
				floydAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				ShortSingleCycle.anonymizeGraph(anonymousGraph, floydAttackedGraph, 4);   // Runs with 0 never included in paper
				GraphUtil.outputSNAPFormat(anonymousGraph, socNetName + attackIdStr + "-anonymized-shortestcycle-" + iterIdStr + ".txt");
				floydAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraph);
				out = new FileWriter(socNetName + attackIdStr + "-anonymized-shortestcycle-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraph, floydAnonymizedGraph, attackIdStr + "-anonymized-shortestcycle", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				GraphUtil.addRandomEdges(anonymousGraph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
				GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + attackIdStr + "-randomly-anonymized-equiv-shortestcycle-" + iterIdStr + ".txt");			
				floydRandomlyAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraphRandom);
				out = new FileWriter(socNetName + attackIdStr + "-randomly-anonymized-shortestcycle-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraphRandom, floydRandomlyAnonymizedGraph, attackIdStr + "-randomly-anonymized-shortestcycle", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				break;
			case 3:   // Anonymize using LargeSingleCycle
				anonymousGraph = GraphUtil.cloneGraph(attackedGraph);
				anonymousGraphRandom = GraphUtil.cloneGraph(attackedGraph);
				floydAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				LargeSingleCycle.anonymizeGraph(anonymousGraph, floydAttackedGraph, 5);   // Runs with 0 never included in paper
				GraphUtil.outputSNAPFormat(anonymousGraph, socNetName + attackIdStr + "-anonymized-largestcycle-" + iterIdStr + ".txt");			
				floydAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraph);
				out = new FileWriter(socNetName + attackIdStr + "-anonymized-largestcycle-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraph, floydAnonymizedGraph, attackIdStr + "-anonymized-largestcycle", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				GraphUtil.addRandomEdges(anonymousGraph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
				GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + attackIdStr + "-randomly-anonymized-equiv-largestcycle-" + iterIdStr + ".txt");			
				floydRandomlyAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraphRandom);
				out = new FileWriter(socNetName + attackIdStr + "-randomly-anonymized-largestcycle-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraphRandom, floydRandomlyAnonymizedGraph, attackIdStr + "-randomly-anonymized-largestcycle", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				break;
			case 4:   // Anonymize using FewerEdges
				floydAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				anonymousGraph = GraphUtil.cloneGraph(attackedGraph);
				anonymousGraphRandom = GraphUtil.cloneGraph(attackedGraph);
				BaseCycle.getRidOfExtremeDegreeVertices(anonymousGraph, 0);
				GraphUtil.outputSNAPFormat(anonymousGraph, socNetName + attackIdStr + "-anonymized-feweredges-" + iterIdStr + ".txt");
				floydAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraph);
				out = new FileWriter(socNetName + attackIdStr + "-anonymized-feweredges-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraph, floydAnonymizedGraph, attackIdStr + "-anonymized-feweredges", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				if (anonymousGraph.edgeSet().size() - originalEdgeCount > 0)
					GraphUtil.addRandomEdges(anonymousGraph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
				else if (anonymousGraph.edgeSet().size() - originalEdgeCount < 0)
					GraphUtil.removeRandomEdges(originalEdgeCount - anonymousGraph.edgeSet().size(), anonymousGraphRandom);
				GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + attackIdStr + "-randomly-anonymized-equiv-feweredges-" + iterIdStr + ".txt");			
				floydRandomlyAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraphRandom);
				out = new FileWriter(socNetName + attackIdStr + "-randomly-anonymized-feweredges-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraphRandom, floydRandomlyAnonymizedGraph, attackIdStr + "-randomly-anonymized-feweredges", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				break;
			case 5:   // Anonymize using FoafEdges
				floydAttackedGraph = new FloydWarshallShortestPaths<>(attackedGraph);
				anonymousGraph = GraphUtil.cloneGraph(attackedGraph);
				anonymousGraphRandom = GraphUtil.cloneGraph(attackedGraph);
				BaseCycle.getRidOfExtremeDegreeVertices(anonymousGraph, 1);
				GraphUtil.outputSNAPFormat(anonymousGraph, socNetName + attackIdStr + "-anonymized-foafedges-" + iterIdStr + ".txt");
				floydAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraph);
				out = new FileWriter(socNetName + attackIdStr + "-anonymized-foafedges-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraph, floydAnonymizedGraph, attackIdStr + "-anonymized-foafedges", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				if (anonymousGraph.edgeSet().size() - originalEdgeCount > 0)
					GraphUtil.addRandomEdges(anonymousGraph.edgeSet().size() - originalEdgeCount, anonymousGraphRandom);
				else if (anonymousGraph.edgeSet().size() - originalEdgeCount < 0)
					GraphUtil.removeRandomEdges(originalEdgeCount - anonymousGraph.edgeSet().size(), anonymousGraphRandom);
				GraphUtil.outputSNAPFormat(anonymousGraphRandom, socNetName + attackIdStr + "-randomly-anonymized-equiv-foafedges-" + iterIdStr + ".txt");			
				floydRandomlyAnonymizedGraph = new FloydWarshallShortestPaths<>(anonymousGraphRandom);
				out = new FileWriter(socNetName + attackIdStr + "-randomly-anonymized-foafedges-" + iterIdStr + "-stats.txt");
				Statistics.printStatistics(0, out, anonymousGraphRandom, floydRandomlyAnonymizedGraph, attackIdStr + "-randomly-anonymized-foafedges", attackerCount, victimCount, attackedGraph, floydAttackedGraph);
				out.close();
				break;
			default:;
			}	
		}
	}
	
	public void testingWalkBasedAttack(UndirectedGraph<String, DefaultEdge> originalGraph, int attackersSize,
			Writer outOriginal, Writer outOddCycle, Writer outOddCycleRandom,
			Writer outShortSingleCycle, Writer outShortSingleCycleRandom,
			Writer outAllTriangles, Writer outAllTrianglesRandom) throws NoSuchAlgorithmException, IOException{
		
		ConnectivityInspector<String, DefaultEdge> connectivity = new ConnectivityInspector<>(originalGraph);
		List<Set<String>> connComp = connectivity.connectedSets();
		System.out.println("The connected components are the following:");
		Set<String> verticesToKeep = null;
		int maximum = 0;
		for (Set<String> comp : connComp){
			System.out.println(comp.size()+"");
			if (comp.size() > maximum){
				maximum = comp.size();
				verticesToKeep = comp;
			}
		}
		double timeDifAvg = 0;

		int total = 10000;
		for (int i = 0; i < total; i++){
			System.out.println("This is the "+i+"th graph.");
			System.out.println("===============================================");
			double timeIni = System.currentTimeMillis();

			UndirectedGraph<String, DefaultEdge> graph = GraphUtil.shiftAndShuffleVertexIds(originalGraph, attackersSize, verticesToKeep);
			
			connectivity = new ConnectivityInspector<>(graph);
			if (!connectivity.isGraphConnected()) throw new RuntimeException();
			
			System.out.println("The graph is connected");
			
			int n = graph.vertexSet().size();
			
			if (attackersSize > n) throw new IllegalArgumentException("The number of attacker cannot be higher " +
					"than the number of vertices");
	
			/*Trujillo- Feb 3, 2016
			 * We will first define the number of targeted vertices
			 * We take the square of the number of attacker as suggested 
			 * in the original paper*/
			//int victimsSize = attackersSize*attackersSize/2;
			//int victimsSize = 1;
			int victimsSize = attackersSize;
			if (victimsSize == 0) victimsSize = 1;
			
			/*Trujillo- Feb 3, 2016
			 * The degree of each attacking vertex should be between log n and 
			 * log n/2*/
			// int maxDegree = (int)(Math.log10(n)/Math.log10(2));
			
			/*Trujillo- Feb 3, 2016
			 * it may be the case that the number of victims is higher than the number of vertices
			 * In this case we restrict the number of victims to remaining vertices*/
	
			if (attackersSize+victimsSize > n) victimsSize = graph.edgeSet().size()-attackersSize;
			
			SecureRandom random = new SecureRandom(); 
	
			/*Trujillo- Feb 12, 2016
			 * Once that graph has been created, we add the attacker vertices*/
			for (int j = 0; j < attackersSize; j++) {
				if (graph.containsVertex(j+"")) throw new RuntimeException();
				graph.addVertex(j+"");
			}
			
			/*Trujillo- Feb 3, 2016
			 * Next we add the edges between victims and to the attackers. This
			 * links should give a unique fingerprint to each victim*/
			Hashtable<String, String> fingerprints = new Hashtable<>();
			for (int j = attackersSize; j < attackersSize+victimsSize; j++){
				//int degree = minDegree + random.nextInt(maxDegree-minDegree) + 1;
				String fingerprint = null;
				do{
					fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, attackersSize)-1) + 1);
					//next we add zeros to the left
					while (fingerprint.length() < attackersSize)
						fingerprint = "0"+fingerprint;
				}while (fingerprints.containsKey(fingerprint));
				
				//System.out.println("Fingerprint found = "+fingerprint);
				fingerprints.put(fingerprint, fingerprint);
				
				//next we actually add the edges to the graph 
				for (int k = 0; k < fingerprint.length(); k++) {
					if (fingerprint.charAt(k) == '1'){
						//System.out.println("adding edge ("+j+","+(k+attackersSize-fingerprint.length())+")");
						graph.addEdge(j+"", (k+attackersSize-fingerprint.length())+"");
					}
				}
				
			}			
			/*Trujillo- Feb 3, 2016
			 * And finally we add random edges between the attacker nodes*/
			for (int k = 0; k < attackersSize; k++) {
				for (int l = k+1; l < attackersSize; l++) {
					if (random.nextBoolean()) {
						graph.addEdge(k+"", l+"");
					}
				}						
			}
	
			System.out.println("A connected random graph with "+graph.vertexSet().size()+" vertices" +
			 " and "+graph.edgeSet().size()+" edges was found, whith density = "+GraphUtil.computeDensity(graph));
			
			/*Trujillo- Jan 25, 2016
			 * Next, we compute (if we can) the value of k such that
			 * the graph is (k,1)-anonymous*/
			//int[][] matrix = GraphUtil.transformIntoAdjacencyMatrix(graph);
			
			System.out.println("Computing floyd for original");
			FloydWarshallShortestPaths<String, DefaultEdge> originalGraphFloyd = new FloydWarshallShortestPaths<>(graph);
			
			System.out.println("Diamter: " + GraphUtil.computeDiameter(graph, originalGraphFloyd));
			System.out.println("Radius: " + GraphUtil.computeRadius(graph, originalGraphFloyd));
	
//			Statistics.printStatistics(0, outOriginal, graph, originalGraphFloyd, "original",attackersSize, victimsSize, graph, originalGraphFloyd);
			
			//System.out.println("The degree of the victim node at the orignal graph is "+graph.degreeOf("1"));
			//System.out.println("The degree of the attacker node at the orignal graph is "+graph.degreeOf("0"));
			
//			System.out.println("Cloning original for random anonymizations");
//			SimpleGraph<String, DefaultEdge> graphRandomOddCycle = GraphUtil.cloneGraph(graph);
//			SimpleGraph<String, DefaultEdge> graphRandomShortSingleCycle = GraphUtil.cloneGraph(graph);
//			SimpleGraph<String, DefaultEdge> graphRandomAllTriangles = GraphUtil.cloneGraph(graph);
//	
//			// Existing OddCycle anonymization
//			System.out.println("Cloning original for OddCycle anonymization");
//			
//			SimpleGraph<String, DefaultEdge> graphAnonymousOddCycle = GraphUtil.cloneGraph(graph);
	
			System.out.println("Computing stats on graph");
			//this method below anonymize the graph
//			OddCycle.anonymizeGraph(graphAnonymousOddCycle, originalGraphFloyd, 1);
			FeasibilityTester.doCheckOnTransformations(graph, originalGraphFloyd);
			
//			int addedEdgesOddCycle = graphAnonymousOddCycle.edgeSet().size() - graph.edgeSet().size();
			
//			System.out.println("It has been added "+addedEdgesOddCycle+" edges for anonymization");
			
//			GraphUtil.addRandomEdges(addedEdgesOddCycle, graphRandomOddCycle);
	
//			if (graphRandomOddCycle.edgeSet().size() != graphAnonymousOddCycle.edgeSet().size())
//				throw new RuntimeException("The random graph has "+graphRandomOddCycle.edgeSet().size()+
//						", which is different to the graphAnonymous = "+graphAnonymousOddCycle.edgeSet().size());
			//System.out.println(addedEdges+" edges has been added in the graph using CPA.");
			
//			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphOddCycleFloyd = new FloydWarshallShortestPaths<>(graphAnonymousOddCycle);
			
//			Statistics.printStatistics(0, outOddCycle, graphAnonymousOddCycle, anonymousGraphOddCycleFloyd, "OddCycle", attackersSize, victimsSize, graph, originalGraphFloyd);
			
			//System.out.println("The degree of the victim node after applying CPA-random is "+graphTwo1BackRandom.degreeOf("1"));
			//System.out.println("The degree of the attacker node node after applying CPA-random is "+graphTwo1BackRandom.degreeOf("0"));
//			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvOddCycleFloyd = new FloydWarshallShortestPaths<>(graphRandomOddCycle);
			
//			Statistics.printStatistics(0, outOddCycleRandom, graphRandomOddCycle, randomGraphEqvOddCycleFloyd, "OddCycle-Random", attackersSize, victimsSize, graph, originalGraphFloyd);
			
			/* Ramirez - Aug 10, 2016
			 * ShortSingleCycle anonymization
			 * */
			
//			System.out.println("Cloning original for ShortSingleCycle anonymization");
//			
//			SimpleGraph<String, DefaultEdge> graphAnonymousShortSingleCycle = GraphUtil.cloneGraph(graph);
//	
//			System.out.println("Anonymizing graph");
//			
//			ShortSingleCycle.anonymizeGraph(graphAnonymousShortSingleCycle, originalGraphFloyd, 1);
//			
//			int addedEdgesShortSingleCycle = graphAnonymousShortSingleCycle.edgeSet().size() - graph.edgeSet().size();
//			
//			System.out.println("It has been added "+addedEdgesShortSingleCycle+" edges for anonymization");
//			
//			GraphUtil.addRandomEdges(addedEdgesShortSingleCycle, graphRandomShortSingleCycle);
//	
//			if (graphRandomShortSingleCycle.edgeSet().size() != graphAnonymousShortSingleCycle.edgeSet().size())
//				throw new RuntimeException("The random graph has "+graphRandomShortSingleCycle.edgeSet().size()+
//						", which is different to the graphAnonymous = "+graphAnonymousShortSingleCycle.edgeSet().size());
//						
//			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphShortSingleCycleFloyd = new FloydWarshallShortestPaths<>(graphAnonymousShortSingleCycle);
//			
//			Statistics.printStatistics(0, outShortSingleCycle, graphAnonymousShortSingleCycle, anonymousGraphShortSingleCycleFloyd, "ShortSingleCycle", attackersSize, victimsSize, graph, originalGraphFloyd);
//			
//			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvShortSingleCycleFloyd = new FloydWarshallShortestPaths<>(graphRandomShortSingleCycle);
//			
//			Statistics.printStatistics(0, outShortSingleCycleRandom, graphRandomShortSingleCycle, randomGraphEqvShortSingleCycleFloyd, "ShortSingleCycle-Random", attackersSize, victimsSize, graph, originalGraphFloyd);
//			
//			/* Ramirez - Aug 10, 2016
//			 * AllTriangles anonymization
//			 * */
//			
//			System.out.println("Cloning original for AllTriangles anonymization");
//			
//			SimpleGraph<String, DefaultEdge> graphAnonymousAllTriangles = GraphUtil.cloneGraph(graph);
//	
//			System.out.println("Anonymizing graph");
//			
//			AllTriangles.anonymizeGraph(graphAnonymousAllTriangles, originalGraphFloyd, 1);
//			
//			int addedEdgesAllTriangles = graphAnonymousAllTriangles.edgeSet().size() - graph.edgeSet().size();
//			
//			System.out.println("It has been added "+addedEdgesAllTriangles+" edges for anonymization");
//			
//			GraphUtil.addRandomEdges(addedEdgesAllTriangles, graphRandomAllTriangles);
//	
//			if (graphRandomAllTriangles.edgeSet().size() != graphAnonymousAllTriangles.edgeSet().size())
//				throw new RuntimeException("The random graph has "+graphRandomAllTriangles.edgeSet().size()+
//						", which is different to the graphAnonymous = "+graphAnonymousAllTriangles.edgeSet().size());
//						
//			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphAllTrianglesFloyd = new FloydWarshallShortestPaths<>(graphAnonymousAllTriangles);
//			
//			Statistics.printStatistics(0, outAllTriangles, graphAnonymousAllTriangles, anonymousGraphAllTrianglesFloyd, "AllTriangles", attackersSize, victimsSize, graph, originalGraphFloyd);
//			
//			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphEqvAllTrianglesFloyd = new FloydWarshallShortestPaths<>(graphRandomAllTriangles);
//			
//			Statistics.printStatistics(0, outAllTrianglesRandom, graphRandomAllTriangles, randomGraphEqvAllTrianglesFloyd, "AllTriangles-Random", attackersSize, victimsSize, graph, originalGraphFloyd);
			
			// Round summary
			double timeEnd = System.currentTimeMillis();
			double timeDif = timeEnd - timeIni;
			timeDifAvg = ((i-1)*timeDifAvg + timeDif)/i;
			double avgTakenTime = 2.7777 * timeDifAvg/Math.pow(10, 7);
			System.out.println("This round took " + avgTakenTime+ " hours for density = "+GraphUtil.computeDensity(graph));
			System.out.println("Remaining time = "+(avgTakenTime*(total-i))+" hours");
		}
	}
	
	// This is for computing stats when graphs are randomly generated given a density value
	public static void computeStatsDensityBasedRandomGeneration(String [] args) throws IOException {
		
		int vertCount = Integer.parseInt(args[0]);
		int [] attackersSizes = new int[]{1, 2, 4, 8, 16};
		double [] densities = new double[99];
		for (int dpct = 2; dpct <= 100; dpct++)
			densities[dpct-2] = (double)dpct/100d;
		
		for (int att = 0; att < attackersSizes.length; att++) {
			
			System.out.println(attackersSizes[att] + " attackers...");
			
			Writer outDiameterPerDensityWalkBased = new FileWriter("DiameterPerDensity-" + vertCount + "-WalkBased-" + attackersSizes[att] + ".txt", true);
			outDiameterPerDensityWalkBased.write("Density\tDiameter\n");
			Writer outDiameterPerDensityCutBased = new FileWriter("DiameterPerDensity-" + vertCount + "-CutBased-" + attackersSizes[att] + ".txt", true);
			outDiameterPerDensityCutBased.write("Density\tDiameter\n");
			Writer outDiffJIPerDensityWalkBased = new FileWriter("DiffJIPerDensity-" + vertCount + "-WalkBased-" + attackersSizes[att] + ".txt", true);
			outDiffJIPerDensityWalkBased.write("Density\tDiffJI\n");
			Writer outDiffJIPerDensityCutBased = new FileWriter("DiffJIPerDensity-" + vertCount + "-CutBased-" + attackersSizes[att] + ".txt", true);
			outDiffJIPerDensityCutBased.write("Density\tDiffJI\n");
			
			int victimsSizeWalkBased = attackersSizes[att];
			if (victimsSizeWalkBased == 0)
				victimsSizeWalkBased = 1;
			
			int victimsSizeCutBased = (attackersSizes[att] - 3) / 3;
			if (victimsSizeCutBased == 0)
				victimsSizeCutBased = 1;

			if (attackersSizes[att] + victimsSizeWalkBased > vertCount)
				victimsSizeWalkBased = vertCount - attackersSizes[att];
			
			if (attackersSizes[att] + victimsSizeCutBased > vertCount)
				victimsSizeCutBased = vertCount - attackersSizes[att];
			
			SecureRandom random = new SecureRandom(); 
			
			int total = 10000;
			
			for (int dens = 0; dens < densities.length; dens++) {
				
				int edgeCount = getEdgeNum(vertCount, densities[dens]);
				
				if (edgeCount > vertCount) {
					
					System.out.println("Density:" + densities[dens] + "...");
					
					double sumDiametersWalk = 0d, sumDiametersCut = 0d;
					double sumDiffsJIWalk = 0d, sumDiffsJICut = 0d;
					
					for (int i = 1; i <= total; i++) {
						
						UndirectedGraph<String, DefaultEdge> walkBasedAttackedGraph = null, cutBasedAttackedGraph = null;
						ConnectivityInspector<String, DefaultEdge> connectivityWalkBased = null, connectivityCutBased = null;
						do {
							final int startingVertex = attackersSizes[att];
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
		
							RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(vertCount, edgeCount);
							
							generator.generateGraph(walkBasedAttackedGraph, vertexFactory, null);
		
							for (int j = 0; j < attackersSizes[att]; j++) {
								walkBasedAttackedGraph.addVertex(j+"");
							}
							
							cutBasedAttackedGraph = GraphUtil.cloneGraph(walkBasedAttackedGraph);   // Branch-out a clone of the current state of walkBasedAttackedGraph 
							
							Hashtable<String, String> fingerprints = new Hashtable<>();
							for (int j = attackersSizes[att]; j < attackersSizes[att] + victimsSizeWalkBased; j++) {
								String fingerprint = null;
								do {
									fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, attackersSizes[att])-1) + 1);
									//next we add zeros to the left
									while (fingerprint.length() < attackersSizes[att])
										fingerprint = "0"+fingerprint;
								} while (fingerprints.containsKey(fingerprint));
								
								fingerprints.put(fingerprint, fingerprint);
								
								for (int k = 0; k < fingerprint.length(); k++) {
									if (fingerprint.charAt(k) == '1') {
										walkBasedAttackedGraph.addEdge(j+"", (k + attackersSizes[att] - fingerprint.length())+"");
									}
								}							
							}
							
							for (int k = 0; k < attackersSizes[att] - 1; k++) {
								for (int l = k + 1; l < attackersSizes[att]; l++) {
									if (random.nextBoolean()) {
										walkBasedAttackedGraph.addEdge(k+"", l+"");
									}
								}						
							}
							
							HashMap<Integer, Integer> attackerVictimMap = new HashMap<>();
							for (int j = attackersSizes[att]; j < attackersSizes[att] + victimsSizeCutBased; j++) {
								int attackerId = random.nextInt(attackersSizes[att]);
								while (attackerVictimMap.containsKey(attackerId))
									attackerId = random.nextInt(attackersSizes[att]);
								attackerVictimMap.put(attackerId, j);
								cutBasedAttackedGraph.addEdge(attackerId+"", j+"");
							}
							
							for (int k = 0; k < attackersSizes[att] - 1; k++)
								for (int l = k + 1; l < attackersSizes[att]; l++)
									if (random.nextBoolean())
										cutBasedAttackedGraph.addEdge(k+"", l+"");
							
							connectivityWalkBased = new ConnectivityInspector<>(walkBasedAttackedGraph);
							connectivityCutBased = new ConnectivityInspector<>(cutBasedAttackedGraph);
						} while(!connectivityWalkBased.isGraphConnected() || !connectivityCutBased.isGraphConnected());
						
						FloydWarshallShortestPaths<String, DefaultEdge> floydWalkBasedAttackedGraph = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);
						sumDiametersWalk += GraphUtil.computeDiameter(walkBasedAttackedGraph, floydWalkBasedAttackedGraph);
						sumDiffsJIWalk += FeasibilityTester.maxJminusI(walkBasedAttackedGraph, floydWalkBasedAttackedGraph);
						
						FloydWarshallShortestPaths<String, DefaultEdge> floydCutBasedAttackedGraph = new FloydWarshallShortestPaths<>(cutBasedAttackedGraph);
						sumDiametersCut += GraphUtil.computeDiameter(cutBasedAttackedGraph, floydCutBasedAttackedGraph);
						sumDiffsJICut += FeasibilityTester.maxJminusI(cutBasedAttackedGraph, floydCutBasedAttackedGraph);
						
					}
					outDiameterPerDensityWalkBased.write(densities[dens] + "\t" + sumDiametersWalk / (double)total + "\n");
					outDiameterPerDensityWalkBased.flush();
					outDiameterPerDensityCutBased.write(densities[dens] + "\t" + sumDiametersCut / (double)total + "\n");
					outDiameterPerDensityCutBased.flush();
					outDiffJIPerDensityWalkBased.write(densities[dens] + "\t" + sumDiffsJIWalk / (double)total + "\n");
					outDiffJIPerDensityWalkBased.flush();
					outDiffJIPerDensityCutBased.write(densities[dens] + "\t" + sumDiffsJICut / (double)total + "\n");
					outDiffJIPerDensityCutBased.flush();
				}
			}
			outDiameterPerDensityWalkBased.close();
			outDiameterPerDensityCutBased.close();
			outDiffJIPerDensityWalkBased.close();
			outDiffJIPerDensityCutBased.close();
		}
	}
	
	public static void experimentsClusteringBasedAnonymizationWalkBasedAttack(String[] args) throws IOException {
		
		int vernum = 256;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		if (args.length == 2) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
		}   // else the defaults defined above are taken
		
		for (double density : densities) {
			
			int edgenum = getEdgeNum(vernum, density);
			
			//for (int attackerCount = 1; attackerCount <= Math.log10(vernum)/Math.log10(2d); attackerCount++) {
			//for (int attackerCount = (int)(Math.log10(vernum)/Math.log10(2d)); attackerCount <= Math.log10(vernum)/Math.log10(2d); attackerCount++) {
			for (int attackerCount = 4; attackerCount <= 4; attackerCount++) {
				
				for (int victimCountMultiplier = 1; victimCountMultiplier <= attackerCount; victimCountMultiplier++) {
					AttackThreeMethod agg = new AttackThreeMethod();
					
					String fileNameOutOriginal = "Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					String fileNameOutAnonymized = "ClustAnonymized-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					String fileNameOutRandomEquiv = "ClustAnonymized-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					
					agg.oneClusteringBasedAnonymizationRunWalkBasedAttack(vernum, edgenum, attackerCount, attackerCount * victimCountMultiplier, 2, attackerCount, fileNameOutOriginal, fileNameOutAnonymized, fileNameOutRandomEquiv);
				}
			}
		}		
	}
	
	void oneClusteringBasedAnonymizationRunWalkBasedAttack(int n, int m, int attackerCount, int victimCount, int k, int ell, String fileNameOutOriginalWalkBased, String fileNameOutAnonymizedWalkBased, String fileNameOutRandomEquivWalkBased) throws IOException {
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outAnonymizedWalkBased = new FileWriter(fileNameOutAnonymizedWalkBased+".DAT", true);
		Writer outRandomEquivWalkBased = new FileWriter(fileNameOutRandomEquivWalkBased+".DAT", true);
		
		final int startingVertex = attackerCount;
		
		for (int i = 0; i < 100000; i++) {
			
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = startingVertex;
				@Override
				public String createVertex() {
					int result = i;
					i++;
					return result+"";
				}
				
			};
			
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);			
			generator.generateGraph(graph, vertexFactory, null);
			
			simulateWalkBasedAttack(graph, attackerCount, victimCount);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(graph);
			Statistics.printStatistics(i, outOriginalWalkBased, graph, floydOriginal, fileNameOutOriginalWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			SimpleGraph<String, DefaultEdge> graphAnonymous = GraphUtil.cloneGraph(graph);
			SimpleGraph<String, DefaultEdge> graphRandomEquiv = GraphUtil.cloneGraph(graph);
			
			ClusteringBasedAnonymizer anonymizer = new ClusteringBasedAnonymizer();
			anonymizer.enforcekEllAnonymity(graphAnonymous, k, ell);
			
			if (graphAnonymous.edgeSet().size() - graph.edgeSet().size() > 0)
				GraphUtil.addRandomEdges(graphAnonymous.edgeSet().size() - graph.edgeSet().size(), graphRandomEquiv);
			else if (graphAnonymous.edgeSet().size() - graph.edgeSet().size() < 0)
				GraphUtil.removeRandomEdges(graph.edgeSet().size() - graphAnonymous.edgeSet().size(), graphRandomEquiv);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymous = new FloydWarshallShortestPaths<>(graphAnonymous);
			Statistics.printStatistics(i, outAnonymizedWalkBased, graphAnonymous, floydAnonymous, fileNameOutAnonymizedWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomEquiv = new FloydWarshallShortestPaths<>(graphRandomEquiv);
			Statistics.printStatistics(i, outRandomEquivWalkBased, graphRandomEquiv, floydRandomEquiv, fileNameOutRandomEquivWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
		}
		
		outOriginalWalkBased.close();
		outAnonymizedWalkBased.close();
		outRandomEquivWalkBased.close();
		
	}
	
	//==================================================================================================================
	
	public static void experimentsK1AdjAnonymity(String[] args) throws IOException {
		
		if (args.length == 3) {
			int attackerCount = Integer.parseInt(args[1]);
			int k = Integer.parseInt(args[2]);
			if (args[0].equals("-facebook"))
				oneRunEnforceK1AdjAnonymityWalkBasedAttack("facebook", attackerCount, k);
			else if (args[0].equals("-panzarasa"))
				oneRunEnforceK1AdjAnonymityWalkBasedAttack("panzarasa", attackerCount, k);
			else if (args[0].equals("-urv"))
				oneRunEnforceK1AdjAnonymityWalkBasedAttack("urv", attackerCount, k);
		}
	}
	
	// On a real social graph (Facebook, Panzarasa or URV)
	static void oneRunEnforceK1AdjAnonymityWalkBasedAttack(String networkName, int attackerCount, int k) throws IOException {
		
		Writer outOriginalWalkBased = new FileWriter(networkName + "-walk-based-" + attackerCount + "-original.dat", true);
		Writer outAnonymizedWalkBased = new FileWriter(networkName + "-walk-based-" + attackerCount + "-anonymized-k-" + k + ".dat", true);
		Writer outRandomizedWalkBased = new FileWriter(networkName + "-walk-based-" + attackerCount + "-random-equiv-k-" + k + ".dat", true);
		
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
				
				simulateWalkBasedAttack(attackedGraph, attackerCount, victimCount);
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				
			} while (!connectivity.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(attackedGraph);
			Statistics.printStatisticsK1(i, outOriginalWalkBased, attackedGraph, floydOriginal, attackerCount, victimCount, k, attackedGraph, floydOriginal);
			
			//GraphUtil.outputSNAPFormat(attackedGraph, fileNameOutOriginalWalkBased+".txt");
			
			SimpleGraph<String, DefaultEdge> graphAnonymous = GraphUtil.cloneGraph(attackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEquiv = GraphUtil.cloneGraph(attackedGraph);
			
			AdjacencyAnonymizer.enforceK1AdjAnonymity(graphAnonymous, k);
			
			if (graphAnonymous.edgeSet().size() - attackedGraph.edgeSet().size() > 0)
				GraphUtil.addRandomEdges(graphAnonymous.edgeSet().size() - attackedGraph.edgeSet().size(), graphRandomEquiv);
			else if (graphAnonymous.edgeSet().size() - attackedGraph.edgeSet().size() < 0)
				GraphUtil.removeRandomEdges(attackedGraph.edgeSet().size() - graphAnonymous.edgeSet().size(), graphRandomEquiv);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymous = new FloydWarshallShortestPaths<>(graphAnonymous);
			Statistics.printStatisticsK1(i, outAnonymizedWalkBased, graphAnonymous, floydAnonymous, attackerCount, victimCount, k, attackedGraph, floydOriginal);
			
			//GraphUtil.outputSNAPFormat(graphAnonymous, fileNameOutAnonymizedWalkBased+".txt");
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomEquiv = new FloydWarshallShortestPaths<>(graphRandomEquiv);
			Statistics.printStatisticsK1(i, outRandomizedWalkBased, graphRandomEquiv, floydRandomEquiv, attackerCount, victimCount, k, attackedGraph, floydOriginal);
			
			//GraphUtil.outputSNAPFormat(graphRandomEquiv, fileNameOutRandomEquivWalkBased+".txt");
			
		}
		
		outOriginalWalkBased.close();
		outAnonymizedWalkBased.close();
		outRandomizedWalkBased.close();
		
	}
	
	// On a randomly generated graph
	void oneRunEnforceK1AdjAnonymityWalkBasedAttack(int n, int m, int attackerCount, int victimCount, int k, String fileNameOutOriginalWalkBased, String fileNameOutAnonymizedWalkBased, String fileNameOutRandomEquivWalkBased) throws IOException {
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outAnonymizedWalkBased = new FileWriter(fileNameOutAnonymizedWalkBased+".DAT", true);
		Writer outRandomEquivWalkBased = new FileWriter(fileNameOutRandomEquivWalkBased+".DAT", true);
		
		final int startingVertex = attackerCount;
		
		for (int i = 0; i < 100000; i++) {
			
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = startingVertex;
				@Override
				public String createVertex() {
					int result = i;
					i++;
					return result+"";
				}
				
			};
			
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);			
			generator.generateGraph(graph, vertexFactory, null);
			
			simulateWalkBasedAttack(graph, attackerCount, victimCount);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(graph);
			Statistics.printStatistics(i, outOriginalWalkBased, graph, floydOriginal, fileNameOutOriginalWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			GraphUtil.outputSNAPFormat(graph, fileNameOutOriginalWalkBased+".txt");
			
			SimpleGraph<String, DefaultEdge> graphAnonymous = GraphUtil.cloneGraph(graph);
			SimpleGraph<String, DefaultEdge> graphRandomEquiv = GraphUtil.cloneGraph(graph);
			
			AdjacencyAnonymizer.enforceK1AdjAnonymity(graphAnonymous, k);
			
			if (graphAnonymous.edgeSet().size() - graph.edgeSet().size() > 0)
				GraphUtil.addRandomEdges(graphAnonymous.edgeSet().size() - graph.edgeSet().size(), graphRandomEquiv);
			else if (graphAnonymous.edgeSet().size() - graph.edgeSet().size() < 0)
				GraphUtil.removeRandomEdges(graph.edgeSet().size() - graphAnonymous.edgeSet().size(), graphRandomEquiv);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymous = new FloydWarshallShortestPaths<>(graphAnonymous);
			Statistics.printStatistics(i, outAnonymizedWalkBased, graphAnonymous, floydAnonymous, fileNameOutAnonymizedWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			GraphUtil.outputSNAPFormat(graphAnonymous, fileNameOutAnonymizedWalkBased+".txt");
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomEquiv = new FloydWarshallShortestPaths<>(graphRandomEquiv);
			Statistics.printStatistics(i, outRandomEquivWalkBased, graphRandomEquiv, floydRandomEquiv, fileNameOutRandomEquivWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			GraphUtil.outputSNAPFormat(graphRandomEquiv, fileNameOutRandomEquivWalkBased+".txt");
			
		}
		
		outOriginalWalkBased.close();
		outAnonymizedWalkBased.close();
		outRandomEquivWalkBased.close();
		
	}
	
	//==================================================================================================================
	
	// For answering to KAIS reviewers
	
	public static void experimentKAISKRealNetworks(String[] args) throws IOException {
		
		if (args.length == 2) {
			int attackerCount = Integer.parseInt(args[1]);
			if (args[0].equals("-facebook"))
				oneRunKAISWalkBasedAttackedRealNetwork("facebook", attackerCount);
			else if (args[0].equals("-panzarasa"))
				oneRunKAISWalkBasedAttackedRealNetwork("panzarasa", attackerCount);
			else if (args[0].equals("-urv"))
				oneRunKAISWalkBasedAttackedRealNetwork("urv", attackerCount);
		}
	}
	
	// On a real social graph (Facebook, Panzarasa or URV)
	static void oneRunKAISWalkBasedAttackedRealNetwork(String networkName, int attackerCount) throws IOException {
		
		String fileNameOutOriginal = networkName + "-walk-based-" + attackerCount + "-original";
		String fileNameOutDistAnonymized = networkName + "-walk-based-" + attackerCount + "-dist-anonymized";
		String fileNameOutDistTransformed = networkName + "-walk-based-" + attackerCount + "-dist-transformed";
		String [] fileNamesOutAdjTransformed = new String[7];
		for (int k = 2; k < 9; k++)
			fileNamesOutAdjTransformed[k-2] = networkName + "-walk-based-" + attackerCount + "-adj-transformed-k-" + k;
		
		Writer outOriginal = new FileWriter(fileNameOutOriginal + ".dat", true);
		Writer outDistAnonymized = new FileWriter(fileNameOutDistAnonymized + ".dat", true);
		Writer outDistTransformed = new FileWriter(fileNameOutDistTransformed + ".dat", true);
		Writer [] outsAdjTransformed = new Writer[7];
		for (int k = 2; k < 9; k++)
			outsAdjTransformed[k-2] = new FileWriter(fileNamesOutAdjTransformed[k-2] + ".dat", true);
				
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
				
				simulateWalkBasedAttack(attackedGraph, attackerCount, victimCount);
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				
			} while (!connectivity.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(attackedGraph);
			Statistics.printStatistics(i, outOriginal, attackedGraph, floydOriginal, fileNameOutOriginal, attackerCount, victimCount, attackedGraph, floydOriginal);
			
			// (2,Gamma_{G,1})-adjacency anonymity
			for (int k = 2; k < 9; k++) {
				SimpleGraph<String, DefaultEdge> graphAdjTransformed = GraphUtil.cloneGraph(attackedGraph);
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(graphAdjTransformed, k);
				FloydWarshallShortestPaths<String, DefaultEdge> floydAdjTransformed = new FloydWarshallShortestPaths<>(graphAdjTransformed);
				Statistics.printStatistics(i, outsAdjTransformed[k-2], graphAdjTransformed, floydAdjTransformed, fileNamesOutAdjTransformed[k-2], attackerCount, victimCount, attackedGraph, floydOriginal);
			}
			
			// (2,Gamma_{G,1})-anonymity
			
			SimpleGraph<String, DefaultEdge> graphDistTransformed = GraphUtil.cloneGraph(attackedGraph);
			OddCycle.anonymousTransformation(graphDistTransformed, floydOriginal);
			FloydWarshallShortestPaths<String, DefaultEdge> floydDistTransformed = new FloydWarshallShortestPaths<>(graphDistTransformed);
			Statistics.printStatistics(i, outDistTransformed, graphDistTransformed, floydDistTransformed, fileNameOutDistTransformed, attackerCount, victimCount, attackedGraph, floydOriginal);
			
			// (>1,>1)-anonymity
			
			SimpleGraph<String, DefaultEdge> graphDistAnonymized = GraphUtil.cloneGraph(attackedGraph);
			OddCycle.anonymizeGraph(graphDistAnonymized, floydOriginal, 3);
			FloydWarshallShortestPaths<String, DefaultEdge> floydDistAnonymized = new FloydWarshallShortestPaths<>(graphDistAnonymized);
			Statistics.printStatistics(i, outDistAnonymized, graphDistAnonymized, floydDistAnonymized, fileNameOutDistAnonymized, attackerCount, victimCount, attackedGraph, floydOriginal);
		}
		
		outOriginal.close();
		outDistAnonymized.close();
		outDistTransformed.close();
		for (int k = 2; k < 9; k++)
			outsAdjTransformed[k-2].close();
	}
	
	//==================================================================================================================
	
	public static void experiments2EllAdjAnonymity(String[] args) throws IOException {
		
		int vernum = 256;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		int[] attackerCounts = new int[]{1, 2, 4, 8};
		
		int ell = (int)(Math.log10(vernum)/Math.log10(2d));
		
		if (args.length == 4) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackerCounts = new int[1];
			attackerCounts[0] = Integer.parseInt(args[2]);
			ell = Integer.parseInt(args[3]);
		}   // else the defaults defined above are taken
		
		for (double density : densities) {
			
			int edgenum = getEdgeNum(vernum, density);
			
			for (int attackerCount : attackerCounts) {
				
				//for (int victimCountMultiplier = 1; victimCountMultiplier <= attackerCount; victimCountMultiplier++) {
				for (int victimCountMultiplier = 1; victimCountMultiplier <= 1; victimCountMultiplier++) {
					AttackThreeMethod agg = new AttackThreeMethod();
					
					String fileNameOutOriginal = "Exp-2ell-Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					String fileNameOutAnonymized = "Exp-2ell-AdjAnonymized-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					String fileNameOutRandomEquiv = "Exp-2ell-AdjAnonymized-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					
					agg.oneRunEnforce2EllAdjAnonymityWalkBasedAttack(vernum, edgenum, attackerCount, attackerCount * victimCountMultiplier, ell, fileNameOutOriginal, fileNameOutAnonymized, fileNameOutRandomEquiv);
				}
			}
		}		
	}
	
	void oneRunEnforce2EllAdjAnonymityWalkBasedAttack(int n, int m, int attackerCount, int victimCount, int ell, String fileNameOutOriginalWalkBased, String fileNameOutAnonymizedWalkBased, String fileNameOutRandomEquivWalkBased) throws IOException {
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outAnonymizedWalkBased = new FileWriter(fileNameOutAnonymizedWalkBased+".DAT", true);
		Writer outRandomEquivWalkBased = new FileWriter(fileNameOutRandomEquivWalkBased+".DAT", true);
		
		final int startingVertex = attackerCount;
		
		//for (int i = 0; i < 100000; i++) {
		for (int i = 0; i < 5; i++) {
			
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = startingVertex;
				@Override
				public String createVertex() {
					int result = i;
					i++;
					return result+"";
				}
				
			};
			
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);			
			generator.generateGraph(graph, vertexFactory, null);
			
			simulateWalkBasedAttack(graph, attackerCount, victimCount);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(graph);
			Statistics.printStatistics(i, outOriginalWalkBased, graph, floydOriginal, fileNameOutOriginalWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			SimpleGraph<String, DefaultEdge> graphAnonymous = GraphUtil.cloneGraph(graph);
			SimpleGraph<String, DefaultEdge> graphRandomEquiv = GraphUtil.cloneGraph(graph);
			
			AdjacencyAnonymizer.enforce2EllAdjAnonymity(graphAnonymous, ell);
			
			if (graphAnonymous.edgeSet().size() - graph.edgeSet().size() > 0)
				GraphUtil.addRandomEdges(graphAnonymous.edgeSet().size() - graph.edgeSet().size(), graphRandomEquiv);
			else if (graphAnonymous.edgeSet().size() - graph.edgeSet().size() < 0)
				GraphUtil.removeRandomEdges(graph.edgeSet().size() - graphAnonymous.edgeSet().size(), graphRandomEquiv);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymous = new FloydWarshallShortestPaths<>(graphAnonymous);
			Statistics.printStatistics(i, outAnonymizedWalkBased, graphAnonymous, floydAnonymous, fileNameOutAnonymizedWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomEquiv = new FloydWarshallShortestPaths<>(graphRandomEquiv);
			Statistics.printStatistics(i, outRandomEquivWalkBased, graphRandomEquiv, floydRandomEquiv, fileNameOutRandomEquivWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
		}
		
		outOriginalWalkBased.close();
		outAnonymizedWalkBased.close();
		outRandomEquivWalkBased.close();
		
	}
	
	//==================================================================================================================
	
	public static void experimentsPerturbation(String[] args) throws IOException {
		
		if (args.length == 5) {
			int attackerCount = Integer.parseInt(args[1]);
			int k = Integer.parseInt(args[2]);
			int ell = Integer.parseInt(args[3]);
			int t = Integer.parseInt(args[4]);
			if (args[0].equals("-facebook"))
				oneRunEliminateOriginal1AntiresWalkBasedAttack("facebook", attackerCount, k, ell, t);
			else if (args[0].equals("-panzarasa"))
				oneRunEliminateOriginal1AntiresWalkBasedAttack("panzarasa", attackerCount, k, ell, t);
			else if (args[0].equals("-urv"))
				oneRunEliminateOriginal1AntiresWalkBasedAttack("urv", attackerCount, k, ell, t);
		}
		
		/*
		int vernum = 256;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		int[] attackerCounts = new int[]{1, 2, 4, 8};
		
		int ell = (int)(Math.log10(vernum)/Math.log10(2d));
		
		if (args.length == 4) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackerCounts = new int[1];
			attackerCounts[0] = Integer.parseInt(args[2]);
			ell = Integer.parseInt(args[3]);
		}   // else the defaults defined above are taken
		
		for (double density : densities) {
			
			int edgenum = getEdgeNum(vernum, density);
			
			for (int attackerCount : attackerCounts) {
				
				//for (int victimCountMultiplier = 1; victimCountMultiplier <= attackerCount; victimCountMultiplier++) {
				for (int victimCountMultiplier = 1; victimCountMultiplier <= 1; victimCountMultiplier++) {
										
					AttackThreeMethod agg = new AttackThreeMethod();
					
					String fileNameOutOriginal = "Exp-2ell-Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					String fileNameOutAnonymized = "Exp-2ell-Perturbed-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					String fileNameOutRandomEquiv = "Exp-2ell-Perturbed-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount;
					
					agg.oneRunPerturbationWalkBasedAttack(vernum, edgenum, attackerCount, attackerCount * victimCountMultiplier, ell, fileNameOutOriginal, fileNameOutAnonymized, fileNameOutRandomEquiv);					
				}
			}
		}//*/		
	}
	
	// On a real social graph (Facebook, Panzarasa or URV)
	static void oneRunEliminateOriginal1AntiresWalkBasedAttack(String networkName, int attackerCount, int k, int ell, int t) throws IOException {
		
		Writer outOriginalWalkBased = new FileWriter(networkName + "-walk-based-" + attackerCount + "-original.dat", true);
		Writer outAnonymizedWalkBased = new FileWriter(networkName + "-walk-based-" + attackerCount + "-anonymized-k-" + k + "-ell-" + ell + "-t-" + t + ".dat", true);
		Writer outRandomizedWalkBased = new FileWriter(networkName + "-walk-based-" + attackerCount + "-random-equiv-k-" + k + "-ell-" + ell + "-t-" + t + ".dat", true);
		
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
				
				simulateWalkBasedAttack(attackedGraph, attackerCount, victimCount);
				
				connectivity = new ConnectivityInspector<>(attackedGraph);
				
			} while (!connectivity.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(attackedGraph);
			Statistics.printStatisticsK1(i, outOriginalWalkBased, attackedGraph, floydOriginal, attackerCount, victimCount, ell, attackedGraph, floydOriginal);
			
			//GraphUtil.outputSNAPFormat(attackedGraph, fileNameOutOriginalWalkBased+".txt");
			
			SimpleGraph<String, DefaultEdge> graphAnonymous = GraphUtil.cloneGraph(attackedGraph);
			SimpleGraph<String, DefaultEdge> graphRandomEquiv = GraphUtil.cloneGraph(attackedGraph);
			
			AdjacencyAnonymizer.kEllAdjAnonymousTransformation(graphAnonymous, k, ell, t);
			
			if (graphAnonymous.edgeSet().size() - attackedGraph.edgeSet().size() > 0)
				GraphUtil.addRandomEdges(graphAnonymous.edgeSet().size() - attackedGraph.edgeSet().size(), graphRandomEquiv);
			else if (graphAnonymous.edgeSet().size() - attackedGraph.edgeSet().size() < 0)
				GraphUtil.removeRandomEdges(attackedGraph.edgeSet().size() - graphAnonymous.edgeSet().size(), graphRandomEquiv);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymous = new FloydWarshallShortestPaths<>(graphAnonymous);
			Statistics.printStatisticsK1(i, outAnonymizedWalkBased, graphAnonymous, floydAnonymous, attackerCount, victimCount, ell, attackedGraph, floydOriginal);
			
			//GraphUtil.outputSNAPFormat(graphAnonymous, fileNameOutAnonymizedWalkBased+".txt");
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomEquiv = new FloydWarshallShortestPaths<>(graphRandomEquiv);
			Statistics.printStatisticsK1(i, outRandomizedWalkBased, graphRandomEquiv, floydRandomEquiv, attackerCount, victimCount, ell, attackedGraph, floydOriginal);
			
			//GraphUtil.outputSNAPFormat(graphRandomEquiv, fileNameOutRandomEquivWalkBased+".txt");
			
		}
		
		outOriginalWalkBased.close();
		outAnonymizedWalkBased.close();
		outRandomizedWalkBased.close();
		
	}
	
	// On a randomly generated graph
	void oneRunPerturbationWalkBasedAttack(int n, int m, int attackerCount, int victimCount, int ell, String fileNameOutOriginalWalkBased, String fileNameOutAnonymizedWalkBased, String fileNameOutRandomEquivWalkBased) throws IOException {
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outAnonymizedWalkBased = new FileWriter(fileNameOutAnonymizedWalkBased+".DAT", true);
		Writer outRandomEquivWalkBased = new FileWriter(fileNameOutRandomEquivWalkBased+".DAT", true);
		
		final int startingVertex = attackerCount;
		
		//for (int i = 0; i < 100000; i++) {
		for (int i = 0; i < 5; i++) {
			
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = startingVertex;
				@Override
				public String createVertex() {
					int result = i;
					i++;
					return result+"";
				}
				
			};
			
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);			
			generator.generateGraph(graph, vertexFactory, null);
			
			simulateWalkBasedAttack(graph, attackerCount, victimCount);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(graph);
			Statistics.printStatistics(i, outOriginalWalkBased, graph, floydOriginal, fileNameOutOriginalWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			SimpleGraph<String, DefaultEdge> graphPerturbed = GraphUtil.cloneGraph(graph);
			SimpleGraph<String, DefaultEdge> graphRandomEquiv = GraphUtil.cloneGraph(graph);
			
			//AdjacencyAnonymizer.perturbOriginal1AntiresolvingSets(graphPerturbed, ell);
			AdjacencyAnonymizer.kEllAdjAnonymousTransformation(graphRandomEquiv, 2, ell, 1);
			
			if (graphPerturbed.edgeSet().size() - graph.edgeSet().size() > 0)
				GraphUtil.addRandomEdges(graphPerturbed.edgeSet().size() - graph.edgeSet().size(), graphRandomEquiv);
			else if (graphPerturbed.edgeSet().size() - graph.edgeSet().size() < 0)
				GraphUtil.removeRandomEdges(graph.edgeSet().size() - graphPerturbed.edgeSet().size(), graphRandomEquiv);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymous = new FloydWarshallShortestPaths<>(graphPerturbed);
			Statistics.printStatistics(i, outAnonymizedWalkBased, graphPerturbed, floydAnonymous, fileNameOutAnonymizedWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomEquiv = new FloydWarshallShortestPaths<>(graphRandomEquiv);
			Statistics.printStatistics(i, outRandomEquivWalkBased, graphRandomEquiv, floydRandomEquiv, fileNameOutRandomEquivWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
		}
		
		outOriginalWalkBased.close();
		outAnonymizedWalkBased.close();
		outRandomEquivWalkBased.close();
		
	}
	
	//==================================================================================================================
	
	public static void experimentsRandomPerturbation(String[] args) throws IOException {
		
		int[] vernums = new int[]{128, 256};
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		
		int[] attackerCounts = new int[]{1, 2, 4, 8};
		
		int ell = (int)(Math.log10(vernums[0])/Math.log10(2d));
		
		if (args.length == 4) {
			vernums = new int[1];
			vernums[0] = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackerCounts = new int[1];
			attackerCounts[0] = Integer.parseInt(args[2]);
			ell = Integer.parseInt(args[3]);
		}   // else the defaults defined above are taken
		
		for (int vernum : vernums) {
			
			ell = (int)(Math.log10(vernum)/Math.log10(2d));
			
			for (double density : densities) {
				
				int edgenum = getEdgeNum(vernum, density);
				
				for (int attackerCount : attackerCounts) {
					
					for (int victimCountMultiplier = 1; victimCountMultiplier <= 1; victimCountMultiplier++) {
						
						for (int numberEdgesToAdd = 1; numberEdgesToAdd < ((vernum * (vernum -1) / 2) - edgenum) / 10; numberEdgesToAdd++) {   // From 1 to one tenth of the possible
							
							AttackThreeMethod agg = new AttackThreeMethod();
							
							String fileNameOutOriginal = "Exp-2ell-Original-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount+"-Adding-"+numberEdgesToAdd;
							String fileNameOutRandomEquiv = "Exp-2ell-Perturbed-Random-V-"+vernum+"-D-"+density+"-WalkBased-A-"+attackerCount+"-Adding-"+numberEdgesToAdd;
							
							agg.oneRunRandomPerturbationWalkBasedAttack(vernum, edgenum, attackerCount, attackerCount * victimCountMultiplier, ell, numberEdgesToAdd, fileNameOutOriginal, fileNameOutRandomEquiv);
						}						
					}
				}
			}
		}	
	}
	
	void oneRunRandomPerturbationWalkBasedAttack(int n, int m, int attackerCount, int victimCount, int ell, int numberEdgesToAdd, String fileNameOutOriginalWalkBased, String fileNameOutRandomEquivWalkBased) throws IOException {
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outRandomEquivWalkBased = new FileWriter(fileNameOutRandomEquivWalkBased+".DAT", true);
		
		final int startingVertex = attackerCount;
		
		//for (int i = 0; i < 100000; i++) {
		for (int i = 0; i < 1000; i++) {
			
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = startingVertex;
				@Override
				public String createVertex() {
					int result = i;
					i++;
					return result+"";
				}
				
			};
			
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);			
			generator.generateGraph(graph, vertexFactory, null);
			
			simulateWalkBasedAttack(graph, attackerCount, victimCount);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(graph);
			Statistics.printStatistics(i, outOriginalWalkBased, graph, floydOriginal, fileNameOutOriginalWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
			SimpleGraph<String, DefaultEdge> graphRandomEquiv = GraphUtil.cloneGraph(graph);
			
			GraphUtil.addRandomEdges(numberEdgesToAdd, graphRandomEquiv);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomEquiv = new FloydWarshallShortestPaths<>(graphRandomEquiv);
			Statistics.printStatistics(i, outRandomEquivWalkBased, graphRandomEquiv, floydRandomEquiv, fileNameOutRandomEquivWalkBased, attackerCount, victimCount, graph, floydOriginal);
			
		}
		
		outOriginalWalkBased.close();
		outRandomEquivWalkBased.close();
		
	}
	
	//==================================================================================================================
		
	public static void generateTrainingPairsReversibilityStudy() throws IOException {
		
		String trainEgo = "1684", testEgo = "0";
		
		int [] addCounts = new int[]{1, 4, 5, 10, 30, 50};   // The size of every graph will be increased by the sum of all addition steps (1, 5, 10, 20, 50, 100)
		
		SimpleGraph<String, DefaultEdge> graphTrain = new FacebookEgoNetwork(DefaultEdge.class, trainEgo);
		SimpleGraph<String, DefaultEdge> graphTest = new FacebookEgoNetwork(DefaultEdge.class, testEgo);
		
		GraphUtil.outputSNAPFormat(graphTrain, "train-original.txt");
		GraphUtil.outputSNAPFormat(graphTest, "test-original.txt");
		
		TreeMap<Integer, Set<String>> nonEdgesTrain = new TreeMap<>();
		for (String v1 : graphTrain.vertexSet())
			for (String v2 : graphTrain.vertexSet())
				if (v1.compareTo(v2) < 0 && !graphTrain.containsEdge(v1, v2)) {
					int score = graphTrain.degreeOf(v1) + graphTrain.degreeOf(v2);
					if (nonEdgesTrain.containsKey(score))
						nonEdgesTrain.get(score).add(v1 + "," + v2);
					else {
						Set<String> ne = new TreeSet<>();
						ne.add(v1 + "," + v2);
						nonEdgesTrain.put(score, ne);
					}
				}
		
		ArrayList<String> addQueueTrain = new ArrayList<>();
		for (int score : nonEdgesTrain.keySet())
			for (String pair : nonEdgesTrain.get(score))
				addQueueTrain.add(pair);
		
		TreeMap<Integer, Set<String>> nonEdgesTest = new TreeMap<>();
		for (String v1 : graphTest.vertexSet())
			for (String v2 : graphTest.vertexSet())
				if (v1.compareTo(v2) < 0 && !graphTest.containsEdge(v1, v2)) {
					int score = graphTest.degreeOf(v1) + graphTest.degreeOf(v2);
					if (nonEdgesTest.containsKey(score))
						nonEdgesTest.get(score).add(v1 + "," + v2);
					else {
						Set<String> ne = new TreeSet<>();
						ne.add(v1 + "," + v2);
						nonEdgesTest.put(score, ne);
					}
				}
		
		ArrayList<String> addQueueTest = new ArrayList<>();
		for (int score : nonEdgesTest.keySet())
			for (String pair : nonEdgesTest.get(score))
				addQueueTest.add(pair);
		
		int totalAdded = 0;
		
		for (int addCnt : addCounts) {
			
			for (int i = totalAdded; i < totalAdded + addCnt; i++) {
				String[] verts = addQueueTrain.get(i).split(",");
				graphTrain.addEdge(verts[0], verts[1]);
				verts = addQueueTest.get(i).split(",");
				graphTest.addEdge(verts[0], verts[1]);
			}
			
			totalAdded += addCnt;
			GraphUtil.outputSNAPFormat(graphTrain, "train-added-" + totalAdded + ".txt");
			GraphUtil.outputSNAPFormat(graphTest, "test-added-" + totalAdded + ".txt");
		}
	}
	
	//==================================================================================================================
	
	public static void experimentsRandomPerturbationMultipleAttacks(String[] args) throws IOException {
		
		if (args.length == 5) {
			int vernum = Integer.parseInt(args[0]);
			double density = Double.parseDouble(args[1]);
			int edgenum = getEdgeNum(vernum, density);
			int attackCount = Integer.parseInt(args[2]);
			List<Integer> attackerCounts = new ArrayList<>();
			String attackerCntLabel = "-";
			for (int i = 0; i < attackCount; i++) {
				attackerCounts.add(Integer.parseInt(args[3]));
				attackerCntLabel += args[3] + "-";
			}
			int ell = Integer.parseInt(args[4]);
								
			for (int victimCountMultiplier = 1; victimCountMultiplier <= 1; victimCountMultiplier++) {
				
				List<Integer> victimCounts = new ArrayList<>();
				for (int attackerCount : attackerCounts)
					victimCounts.add(attackerCount * victimCountMultiplier);
				
				int maxNumEdgesToAdd = 100;
				if (((vernum * (vernum -1) / 2) - edgenum) / 10 < 100);
					maxNumEdgesToAdd = ((vernum * (vernum -1) / 2) - edgenum) / 10;
				
				for (int numberEdgesToAdd = 1; numberEdgesToAdd < maxNumEdgesToAdd; numberEdgesToAdd++) {   // From 1 to up to one tenth of the possible edges (or 100 if too many possible edges)
					
					AttackThreeMethod agg = new AttackThreeMethod();
					
					String fileNameOutOriginal = "Exp-2ell-Original-V-"+vernum+"-D-"+density+"-WalkBased-A"+attackerCntLabel+"Adding-"+numberEdgesToAdd;
					String fileNameOutRandomEquiv = "Exp-2ell-Perturbed-Random-V-"+vernum+"-D-"+density+"-WalkBased-A"+attackerCntLabel+"Adding-"+numberEdgesToAdd;
												
					agg.oneRunRandomPerturbationMultipleWalkBasedAttack(vernum, edgenum, attackerCounts, victimCounts, ell, numberEdgesToAdd, fileNameOutOriginal, fileNameOutRandomEquiv);
				}						
			}
		}	
	}
	
	void oneRunRandomPerturbationMultipleWalkBasedAttack(int n, int m, List<Integer> attackerCounts, List<Integer> victimCounts, int ell, int numberEdgesToAdd, String fileNameOutOriginalWalkBased, String fileNameOutRandomEquivWalkBased) throws IOException {
		
		Writer outRandomEquivWalkBased = new FileWriter(fileNameOutRandomEquivWalkBased+".DAT", true);
		
		int totalAttackers = 0;
		for (int i = 0; i < attackerCounts.size(); i++)
			totalAttackers += attackerCounts.get(i);
		final int startingVertex = totalAttackers;
		
		//for (int i = 0; i < 100000; i++) {
		for (int i = 0; i < 1000; i++) {
			
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = startingVertex;
				@Override
				public String createVertex() {
					int result = i;
					i++;
					return result+"";
				}
				
			};
			
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);			
			generator.generateGraph(graph, vertexFactory, null);
			
			simulateMultipleWalkBasedAttacks(graph, attackerCounts, victimCounts);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(graph);
			
			SimpleGraph<String, DefaultEdge> graphRandomEquiv = GraphUtil.cloneGraph(graph);
			
			GraphUtil.addRandomEdges(numberEdgesToAdd, graphRandomEquiv);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandomEquiv = new FloydWarshallShortestPaths<>(graphRandomEquiv);
			Statistics.printStatisticsMultipleAttacks(i, outRandomEquivWalkBased, graphRandomEquiv, floydRandomEquiv, fileNameOutRandomEquivWalkBased, attackerCounts, victimCounts, graph, floydOriginal);
			
		}
		
		outRandomEquivWalkBased.close();
		
	}
	
	//==================================================================================================================
	// Experiments PoPETs paper
	// Here, graphs are randomly generated given a density value
	
	// Experiment 1
	
	public static void experiment1PoPETsPaper(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 100;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		
		int attackerSizes[] = new int[]{1,4,8,16};
		
		if (args.length == 3) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackerSizes = new int[1];
			attackerSizes[0] = Integer.parseInt(args[2]);
		}   // else the defaults defined above are taken
		
		for (double density : densities) {
			for (int attackerSize : attackerSizes){
				int edgenum = getEdgeNum(vernum, density);				
				String fileNameOutOriginalWalkBased = "Original-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutAnonymizationWalkBased = "Anonymization-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutAnonymizationRandomWalkBased = "Anonymization-Random-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutTransformationWalkBased = "Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutTransformationRandomWalkBased = "Transformation-Random-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				oneRunExperiment1PoPETsPaper(vernum, edgenum, attackerSize, fileNameOutOriginalWalkBased, fileNameOutAnonymizationWalkBased, fileNameOutAnonymizationRandomWalkBased, fileNameOutTransformationWalkBased, fileNameOutTransformationRandomWalkBased);		
			}
		}
	}
	
	public static void oneRunExperiment1PoPETsPaper(int n, int m, int attackersCount, String fileNameOutOriginalWalkBased, 
			String fileNameOutAnonymizationWalkBased, String fileNameOutAnonymizationRandomWalkBased, 
			String fileNameOutTransformationWalkBased, String fileNameOutTransformationRandomWalkBased) throws NoSuchAlgorithmException, IOException {
		
		if (attackersCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outAnonymizationWalkBased = new FileWriter(fileNameOutAnonymizationWalkBased+".DAT", true);
		Writer outTransformationWalkBased = new FileWriter(fileNameOutTransformationWalkBased+".DAT", true);
		Writer outAnonymizationRandomWalkBased = new FileWriter(fileNameOutAnonymizationRandomWalkBased+".DAT", true);
		Writer outTransformationRandomWalkBased = new FileWriter(fileNameOutTransformationRandomWalkBased+".DAT", true);
		
		final int startingVertex = attackersCount;
		
		int victimsCountWalkBased = attackersCount;
		if (victimsCountWalkBased == 0)
			victimsCountWalkBased = 1;
		
		if (attackersCount + victimsCountWalkBased > n)
			victimsCountWalkBased = n - attackersCount;
		
		int total = 100000;
		
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
				
				simulateWalkBasedAttack(walkBasedAttackedGraph, attackersCount, victimsCountWalkBased);
				
				connInspector = new ConnectivityInspector<>(walkBasedAttackedGraph);
				
			} while (!connInspector.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalWalkBasedAttackedGraph = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);
			Statistics.printStatistics(i, outOriginalWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph, fileNameOutOriginalWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			//GraphUtil.outputSNAPFormat(walkBasedAttackedGraph, fileNameOutOriginalWalkBased + "-" + i + ".txt");
			
			SimpleGraph<String, DefaultEdge> randGraphEqvAnonWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			SimpleGraph<String, DefaultEdge> randGraphEqvTransWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			
			// Anonymization
			
			SimpleGraph<String, DefaultEdge> anonymousGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			OddCycle.anonymizeGraph(anonymousGraphWalkBased, floydOriginalWalkBasedAttackedGraph, 3);
			
			int addedEdgesAnonWalkBased = anonymousGraphWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesAnonWalkBased, randGraphEqvAnonWalkBased);

			if (randGraphEqvAnonWalkBased.edgeSet().size() != anonymousGraphWalkBased.edgeSet().size())
				throw new RuntimeException("The random graph has " + randGraphEqvAnonWalkBased.edgeSet().size() + ", which is different to the graphAnonymous = " + anonymousGraphWalkBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymousGraph = new FloydWarshallShortestPaths<>(anonymousGraphWalkBased);
			Statistics.printStatistics(i, outAnonymizationWalkBased, anonymousGraphWalkBased, floydAnonymousGraph, fileNameOutAnonymizationWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			//GraphUtil.outputSNAPFormat(graphAnonymousOddCycleWalkBased, fileNameOutOddCycleWalkBased + "-" + i + ".txt");
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandEqvAnonGraph = new FloydWarshallShortestPaths<>(randGraphEqvAnonWalkBased);			
			Statistics.printStatistics(i, outAnonymizationRandomWalkBased, randGraphEqvAnonWalkBased, floydRandEqvAnonGraph, fileNameOutAnonymizationRandomWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			
			// Transformation
			
			SimpleGraph<String, DefaultEdge> anonTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			OddCycle.anonymousTransformation(anonTransformedGraphWalkBased, floydOriginalWalkBasedAttackedGraph);
			
			int addedEdgesTransWalkBased = anonTransformedGraphWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			
			GraphUtil.addRandomEdges(addedEdgesTransWalkBased, randGraphEqvTransWalkBased);

			if (randGraphEqvTransWalkBased.edgeSet().size() != anonTransformedGraphWalkBased.edgeSet().size())
				throw new RuntimeException("The random graph has " + randGraphEqvTransWalkBased.edgeSet().size() + ", which is different to the graphAnonymous = " + anonTransformedGraphWalkBased.edgeSet().size());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydTransformedGraph = new FloydWarshallShortestPaths<>(anonTransformedGraphWalkBased);			
			Statistics.printStatistics(i, outTransformationWalkBased, anonTransformedGraphWalkBased, floydTransformedGraph, fileNameOutTransformationWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			//GraphUtil.outputSNAPFormat(graphAnonymousShortSingleCycleWalkBased, fileNameOutShortSingleCycleWalkBased + "-" + i + ".txt");
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandEqvTransGraph = new FloydWarshallShortestPaths<>(randGraphEqvTransWalkBased);
			Statistics.printStatistics(i, outTransformationRandomWalkBased, randGraphEqvTransWalkBased, floydRandEqvTransGraph, fileNameOutTransformationRandomWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
		}
		
		outOriginalWalkBased.close();
		outAnonymizationWalkBased.close();
		outAnonymizationRandomWalkBased.close();
		outTransformationWalkBased.close();
		outTransformationRandomWalkBased.close();
	}
	
	//------------------------------------------------------------------------------------------------------------------
	
	// Experiment 2
	
	public static void experiment2PoPETsPaper(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 100;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		
		int attackerSizes[] = new int[]{1,4,8,16};
		
		if (args.length == 3) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackerSizes = new int[1];
			attackerSizes[0] = Integer.parseInt(args[2]);
		}   // else the defaults defined above are taken
		
		for (double density : densities) {
			for (int attackerSize : attackerSizes){
				int edgenum = getEdgeNum(vernum, density);				
				String fileNameOutOriginalWalkBased = "Exp2.1-Original-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutDistTransformationWalkBased = "Exp2.1-Dist-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutDistTransformationRandomWalkBased = "Exp2.1-Dist-Transformation-Random-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutAdjTransformationWalkBased = "Exp2.1-Adj-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutAdjTransformationRandomWalkBased = "Exp2.1-Adj-Transformation-Random-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;				
				String fileNameOutDegreeWalkBased = "Exp2.1-Degree-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutDegreeRandomWalkBased = "Exp2.1-Degree-Random-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;				
				oneRunExperiment2PoPETsPaper(vernum, edgenum, attackerSize, fileNameOutOriginalWalkBased, fileNameOutDistTransformationWalkBased, fileNameOutDistTransformationRandomWalkBased, fileNameOutAdjTransformationWalkBased, fileNameOutAdjTransformationRandomWalkBased, fileNameOutDegreeWalkBased, fileNameOutDegreeRandomWalkBased, false, true);		
			}
		}
	}
	
	public static void oneRunExperiment2PoPETsPaper(int n, int m, int attackersCount, String fileNameOutOriginalWalkBased, 
			String fileNameOutDistTransformationWalkBased, String fileNameOutDistTransformationRandomWalkBased, 
			String fileNameOutAdjTransformationWalkBased, String fileNameOutAdjTransformationRandomWalkBased,
			String fileNameOutDegreeWalkBased, String fileNameOutDegreeRandomWalkBased, 
			boolean runActiveTransf, boolean runDegreeAnon) throws NoSuchAlgorithmException, IOException {
		
		if (attackersCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outDistTransformationWalkBased = null;
		Writer outAdjTransformationWalkBased = null;
		if (runActiveTransf) {
			outDistTransformationWalkBased = new FileWriter(fileNameOutDistTransformationWalkBased+".DAT", true);
			outAdjTransformationWalkBased = new FileWriter(fileNameOutAdjTransformationWalkBased+".DAT", true);
		}
		Writer outDegreeWalkBased = null;
		if (runDegreeAnon)
			outDegreeWalkBased = new FileWriter(fileNameOutDegreeWalkBased+".DAT", true);
		Writer outDistTransformationRandomWalkBased = null;
		Writer outAdjTransformationRandomWalkBased = null;
		if (runActiveTransf) {
			outDistTransformationRandomWalkBased = new FileWriter(fileNameOutDistTransformationRandomWalkBased+".DAT", true);
			outAdjTransformationRandomWalkBased = new FileWriter(fileNameOutAdjTransformationRandomWalkBased+".DAT", true);
		}
		Writer outDegreeRandomWalkBased = null;
		if (runDegreeAnon)
			outDegreeRandomWalkBased = new FileWriter(fileNameOutDegreeRandomWalkBased+".DAT", true);
		
		final int startingVertex = attackersCount;
		
		int victimsCountWalkBased = attackersCount;
		if (victimsCountWalkBased == 0)
			victimsCountWalkBased = 1;
		
		if (attackersCount + victimsCountWalkBased > n)
			victimsCountWalkBased = n - attackersCount;
		
		int total = 100000;
		
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
				
				if (runActiveTransf)
					simulateWalkBasedAttack(walkBasedAttackedGraph, attackersCount, victimsCountWalkBased);
				
				connInspector = new ConnectivityInspector<>(walkBasedAttackedGraph);
				
			} while (!connInspector.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalWalkBasedAttackedGraph = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);
			Statistics.printStatistics(i, outOriginalWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph, fileNameOutOriginalWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			
			SimpleGraph<String, DefaultEdge> randGraphEqvDistTransWalkBased = null;
			SimpleGraph<String, DefaultEdge> randGraphEqvAdjTransWalkBased = null;
			if (runActiveTransf) {
				randGraphEqvDistTransWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
				randGraphEqvAdjTransWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			}
			SimpleGraph<String, DefaultEdge> randGraphEqvkDegAnonWalkBased = null;
			if (runDegreeAnon) 
				randGraphEqvkDegAnonWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
			
			if (runActiveTransf) {
				
				// (>1,>1)-Anonymous transformation
				
				SimpleGraph<String, DefaultEdge> anonDistTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				
				OddCycle.anonymousTransformation(anonDistTransformedGraphWalkBased, floydOriginalWalkBasedAttackedGraph);
				
				int addedEdgesDistTransWalkBased = anonDistTransformedGraphWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
				
				GraphUtil.addRandomEdges(addedEdgesDistTransWalkBased, randGraphEqvDistTransWalkBased);
	
				if (randGraphEqvDistTransWalkBased.edgeSet().size() != anonDistTransformedGraphWalkBased.edgeSet().size())
					throw new RuntimeException("The random graph has " + randGraphEqvDistTransWalkBased.edgeSet().size() + ", which is different to the graphAnonymous = " + anonDistTransformedGraphWalkBased.edgeSet().size());
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydDistTransformedGraph = new FloydWarshallShortestPaths<>(anonDistTransformedGraphWalkBased);			
				Statistics.printStatistics(i, outDistTransformationWalkBased, anonDistTransformedGraphWalkBased, floydDistTransformedGraph, fileNameOutDistTransformationWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEqvDistTransGraph = new FloydWarshallShortestPaths<>(randGraphEqvDistTransWalkBased);
				Statistics.printStatistics(i, outDistTransformationRandomWalkBased, randGraphEqvDistTransWalkBased, floydRandEqvDistTransGraph, fileNameOutDistTransformationRandomWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				// (2,1)-Adjacency anonymous transformation
				
				SimpleGraph<String, DefaultEdge> anonAdjTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, 2);
							
				int addedEdgesAdjTransWalkBased = 0, removedEdgesAdjTransWalkBased = 0;
				for (String v1 : anonAdjTransformedGraphWalkBased.vertexSet())
					for (String v2 : anonAdjTransformedGraphWalkBased.vertexSet())
						if (v1.compareTo(v2) < 0)
							if (anonAdjTransformedGraphWalkBased.containsEdge(v1, v2) && !walkBasedAttackedGraph.containsEdge(v1, v2))
								addedEdgesAdjTransWalkBased++;
							else if (!anonAdjTransformedGraphWalkBased.containsEdge(v1, v2) && walkBasedAttackedGraph.containsEdge(v1, v2))
								removedEdgesAdjTransWalkBased++;
				
				GraphUtil.addRandomEdges(addedEdgesAdjTransWalkBased, randGraphEqvAdjTransWalkBased);
				GraphUtil.removeRandomEdges(removedEdgesAdjTransWalkBased, randGraphEqvAdjTransWalkBased);
	
				FloydWarshallShortestPaths<String, DefaultEdge> floydAdjTransformedGraph = new FloydWarshallShortestPaths<>(anonAdjTransformedGraphWalkBased);			
				Statistics.printStatistics(i, outAdjTransformationWalkBased, anonAdjTransformedGraphWalkBased, floydAdjTransformedGraph, fileNameOutAdjTransformationWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEqvAdjTransGraph = new FloydWarshallShortestPaths<>(randGraphEqvAdjTransWalkBased);
				Statistics.printStatistics(i, outAdjTransformationRandomWalkBased, randGraphEqvAdjTransWalkBased, floydRandEqvAdjTransGraph, fileNameOutAdjTransformationRandomWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			}
			
			// k-degree anonymization
			if (runDegreeAnon) {
			
				SimpleGraph<String, DefaultEdge> kDegAnonymousGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				
				DegreeAnonymityLiuTerzi.anonymizeGraph(kDegAnonymousGraphWalkBased, 2);
				
				int addedEdgeskDegAnonWalkBased = kDegAnonymousGraphWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
				
				GraphUtil.addRandomEdges(addedEdgeskDegAnonWalkBased, randGraphEqvkDegAnonWalkBased);
	
				if (randGraphEqvkDegAnonWalkBased.edgeSet().size() != kDegAnonymousGraphWalkBased.edgeSet().size())
					throw new RuntimeException("The random graph has " + randGraphEqvkDegAnonWalkBased.edgeSet().size() + ", which is different to the graphAnonymous = " + kDegAnonymousGraphWalkBased.edgeSet().size());
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydkDegAnonymousGraph = new FloydWarshallShortestPaths<>(kDegAnonymousGraphWalkBased);
				Statistics.printStatistics(i, outDegreeWalkBased, kDegAnonymousGraphWalkBased, floydkDegAnonymousGraph, fileNameOutDegreeWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
				FloydWarshallShortestPaths<String, DefaultEdge> floydRandEqvAnonGraph = new FloydWarshallShortestPaths<>(randGraphEqvkDegAnonWalkBased);			
				Statistics.printStatistics(i, outDegreeRandomWalkBased, randGraphEqvkDegAnonWalkBased, floydRandEqvAnonGraph, fileNameOutDegreeRandomWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			}
		}
		
		if (runActiveTransf) {
			outOriginalWalkBased.close();
			outDistTransformationWalkBased.close();
			outDistTransformationRandomWalkBased.close();
			outAdjTransformationWalkBased.close();
			outAdjTransformationRandomWalkBased.close();
		}
		if (runDegreeAnon) {
			outDegreeWalkBased.close();
			outDegreeRandomWalkBased.close();
		}
	}
	
	//------------------------------------------------------------------------------------------------------------------
	
	// Experiment 3
	
	public static void experiment3PoPETsPaper(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 100;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		
		int attackerSizes[] = new int[]{1,4,8,16};
		
		if (args.length == 3) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			int upperBoundAttSizes = Integer.parseInt(args[2]);
			attackerSizes = new int[upperBoundAttSizes - 1];
			for (int i = 2; i <= upperBoundAttSizes; i++)
				attackerSizes[i-2] = i;
		}   // else the defaults defined above are taken
		
		for (double density : densities) {
			for (int attackerSize : attackerSizes){
				int edgenum = getEdgeNum(vernum, density);				
				String fileNameOutOriginalWalkBased = "Exp3-Original-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;				
				String fileNameOutAdjTransformationWalkBased = "Exp3-Adj-Transformation-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;
				String fileNameOutAdjTransformationRandomWalkBased = "Exp3-Adj-Transformation-Random-V-" + vernum + "-D-" + density + "-WalkBased-A-" + attackerSize;												
				oneRunExperiment3PoPETsPaper(vernum, edgenum, attackerSize, fileNameOutOriginalWalkBased, fileNameOutAdjTransformationWalkBased, fileNameOutAdjTransformationRandomWalkBased);		
			}
		}
	}
	
	public static void oneRunExperiment3PoPETsPaper(int n, int m, int attackersCount, String fileNameOutOriginalWalkBased, 
			String fileNameOutAdjTransformationWalkBased, String fileNameOutAdjTransformationRandomWalkBased) throws NoSuchAlgorithmException, IOException {
		
		if (attackersCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
		
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		Writer outAdjTransformationWalkBased = new FileWriter(fileNameOutAdjTransformationWalkBased+".DAT", true);		
		Writer outAdjTransformationRandomWalkBased = new FileWriter(fileNameOutAdjTransformationRandomWalkBased+".DAT", true);
		
		final int startingVertex = attackersCount;
		
		int victimsCountWalkBased = attackersCount;
		if (victimsCountWalkBased == 0)
			victimsCountWalkBased = 1;
		
		if (attackersCount + victimsCountWalkBased > n)
			victimsCountWalkBased = n - attackersCount;
		
		//int total = 100000;
		int total = 1000;   // First run 1000 times to ensure that some figures are available for building the graphs
		
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
				
				simulateWalkBasedAttack(walkBasedAttackedGraph, attackersCount, victimsCountWalkBased);
				
				connInspector = new ConnectivityInspector<>(walkBasedAttackedGraph);
				
			} while (!connInspector.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalWalkBasedAttackedGraph = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);
			Statistics.printStatistics(i, outOriginalWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph, fileNameOutOriginalWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			
			SimpleGraph<String, DefaultEdge> randGraphEqvAdjTransWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph);
						
			// (2,attackersCount)-Adjacency anonymous transformation
			
			SimpleGraph<String, DefaultEdge> anonAdjTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			AdjacencyAnonymizer.kEllAdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, 2, attackersCount);
						
			int addedEdgesAdjTransWalkBased = anonAdjTransformedGraphWalkBased.edgeSet().size() - walkBasedAttackedGraph.edgeSet().size();
			GraphUtil.addRandomEdges(addedEdgesAdjTransWalkBased, randGraphEqvAdjTransWalkBased);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydAdjTransformedGraph = new FloydWarshallShortestPaths<>(anonAdjTransformedGraphWalkBased);			
			Statistics.printStatistics(i, outAdjTransformationWalkBased, anonAdjTransformedGraphWalkBased, floydAdjTransformedGraph, fileNameOutAdjTransformationWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydRandEqvAdjTransGraph = new FloydWarshallShortestPaths<>(randGraphEqvAdjTransWalkBased);
			Statistics.printStatistics(i, outAdjTransformationRandomWalkBased, randGraphEqvAdjTransWalkBased, floydRandEqvAdjTransGraph, fileNameOutAdjTransformationRandomWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
						
		}
		
		outOriginalWalkBased.close();
		outAdjTransformationWalkBased.close();
		outAdjTransformationRandomWalkBased.close();
	}
	
	//------------------------------------------------------------------------------------------------------------------
	
	// Experiment 4
	
	public static void experiment4PoPETsPaper(String [] args) throws NoSuchAlgorithmException, IOException {
		
		int vernum = 100;
		
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		
		int attackerSizes[] = new int[]{1,4,8,16};
		
		int maxKs[] = new int[]{1,4,8,16};
		
		if (args.length == 4) {
			vernum = Integer.parseInt(args[0]);
			densities = new double[1];
			densities[0] = Double.parseDouble(args[1]);
			attackerSizes = new int[1];
			attackerSizes[0] = Integer.parseInt(args[2]);
			maxKs = new int[1];
			maxKs[0] = Integer.parseInt(args[3]);			
		}   // else the defaults defined above are taken
		
		for (double density : densities) {
			for (int attackerSize : attackerSizes) {
				for (int maxK : maxKs) {
					oneRunExperiment4PoPETsPaper(vernum, density, attackerSize, maxK);
				}
			}
		}
	}
	
	public static void oneRunExperiment4PoPETsPaper(int n, double density, int attackersCount, int maxK) throws NoSuchAlgorithmException, IOException {
		
		int m = getEdgeNum(n, density);
		
		if (attackersCount > n) 
			throw new IllegalArgumentException("The number of attackers cannot be higher " + "than the number of vertices");
				
		String fileNameOutOriginalWalkBased = "Exp4-Original-V-" + n + "-D-" + density + "-WalkBased-A-" + attackersCount;
		Writer outOriginalWalkBased = new FileWriter(fileNameOutOriginalWalkBased+".DAT", true);
		String [] fileNameOutAdjTransformationWalkBased = new String[maxK - 1];
		Writer [] outAdjTransformationWalkBased = new Writer[maxK - 1];
		for (int k = 2; k <= maxK; k++) {
			fileNameOutAdjTransformationWalkBased[k-2] = "Exp4-" + k + "-1-Adj-Transformation-V-" + n + "-D-" + density + "-WalkBased-A-" + attackersCount;
			outAdjTransformationWalkBased[k-2] = new FileWriter(fileNameOutAdjTransformationWalkBased[k-2] + ".DAT", true);
		}
		
		// For answering to KAIS reviewers
		String fileNameOutDistAnonymizationWalkBased = "Exp4-1-Dist-Anonymization-V-" + n + "-D-" + density + "-WalkBased-A-" + attackersCount;
		Writer outDistAnonymizationWalkBased = new FileWriter(fileNameOutDistAnonymizationWalkBased + ".DAT", true);		
		String fileNameOutDistTransformationWalkBased = "Exp4-1-Dist-Transformation-V-" + n + "-D-" + density + "-WalkBased-A-" + attackersCount;
		Writer outDistTransformationWalkBased = new FileWriter(fileNameOutDistTransformationWalkBased + ".DAT", true);
		
		final int startingVertex = attackersCount;
		
		int victimsCountWalkBased = attackersCount;
		if (victimsCountWalkBased == 0)
			victimsCountWalkBased = 1;
		
		if (attackersCount + victimsCountWalkBased > n)
			victimsCountWalkBased = n - attackersCount;
		
		int total = 100000;
		
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
				
				simulateWalkBasedAttack(walkBasedAttackedGraph, attackersCount, victimsCountWalkBased);
				
				connInspector = new ConnectivityInspector<>(walkBasedAttackedGraph);
				
			} while (!connInspector.isGraphConnected());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalWalkBasedAttackedGraph = new FloydWarshallShortestPaths<>(walkBasedAttackedGraph);
			Statistics.printStatistics(i, outOriginalWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph, fileNameOutOriginalWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			
			for (int k = 2; k <= maxK; k++) {
				
				// (k,1)-adjacency anonymous transformation
				
				SimpleGraph<String, DefaultEdge> anonAdjTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
				
				AdjacencyAnonymizer.k1AdjAnonymousTransformation(anonAdjTransformedGraphWalkBased, k);
								
				FloydWarshallShortestPaths<String, DefaultEdge> floydAdjTransformedGraph = new FloydWarshallShortestPaths<>(anonAdjTransformedGraphWalkBased);			
				Statistics.printStatistics(i, outAdjTransformationWalkBased[k-2], anonAdjTransformedGraphWalkBased, floydAdjTransformedGraph, fileNameOutAdjTransformationWalkBased[k-2], attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
				
			}
			
			// For answering to KAIS reviewers
			
			// (>1,>1)-anonymization
						
			SimpleGraph<String, DefaultEdge> anonDistAnonymizedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			OddCycle.anonymizeGraph(anonDistAnonymizedGraphWalkBased, floydOriginalWalkBasedAttackedGraph, 3);
							
			FloydWarshallShortestPaths<String, DefaultEdge> floydDistAnonymizedGraph = new FloydWarshallShortestPaths<>(anonDistAnonymizedGraphWalkBased);			
			Statistics.printStatistics(i, outDistAnonymizationWalkBased, anonDistAnonymizedGraphWalkBased, floydDistAnonymizedGraph, fileNameOutDistAnonymizationWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
			
			// (2,Gamma_{G,1})-anonymization
			
			SimpleGraph<String, DefaultEdge> anonDistTransformedGraphWalkBased = GraphUtil.cloneGraph(walkBasedAttackedGraph); 
			
			OddCycle.anonymousTransformation(anonDistTransformedGraphWalkBased, floydOriginalWalkBasedAttackedGraph);
							
			FloydWarshallShortestPaths<String, DefaultEdge> floydDistTransformedGraph = new FloydWarshallShortestPaths<>(anonDistTransformedGraphWalkBased);			
			Statistics.printStatistics(i, outDistTransformationWalkBased, anonDistTransformedGraphWalkBased, floydDistTransformedGraph, fileNameOutDistTransformationWalkBased, attackersCount, victimsCountWalkBased, walkBasedAttackedGraph, floydOriginalWalkBasedAttackedGraph);
		}
		
		outOriginalWalkBased.close();
		for (int k = 2; k <= maxK; k++)
			outAdjTransformationWalkBased[k-2].close();
		outDistAnonymizationWalkBased.close();
		outDistTransformationWalkBased.close();
	}
	
	//==================================================================================================================
	
	static void simulateWalkBasedAttack(UndirectedGraph<String, DefaultEdge> graph, int attackerCount, int victimCount) {
		
		/* The graph is assumed to satisfy all requirements, notably vertices being labeled from attackerCount on, 
		 * and connectivity if required
		 */
		
		//System.out.println(graph.vertexSet());
		
		SecureRandom random = new SecureRandom();
			
		if (victimCount == 0)
			victimCount = 1;
		
		if (attackerCount + victimCount > graph.vertexSet().size())
			victimCount = graph.vertexSet().size() - attackerCount;
		
		for (int j = 0; j < attackerCount; j++)
			graph.addVertex(j+"");
		
		Hashtable<String, String> fingerprints = new Hashtable<>();
		for (int j = attackerCount; j < attackerCount + victimCount; j++) {
			String fingerprint = null;
			do {
				fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, attackerCount)-1) + 1);
				while (fingerprint.length() < attackerCount)
					fingerprint = "0" + fingerprint;
			} while (fingerprints.containsKey(fingerprint));
			
			fingerprints.put(fingerprint, fingerprint);
			
			for (int k = 0; k < fingerprint.length(); k++) {
				if (fingerprint.charAt(k) == '1'){
					graph.addEdge(j+"", (k + attackerCount - fingerprint.length())+"");
				}
			}
		}
		
		if (attackerCount > 1) {
			for (int k = 0; k < attackerCount - 1; k++) {
				graph.addEdge(k+"", (k+1)+"");
			}
		}				
		
		for (int k = 0; k < attackerCount - 2; k++) {
			for (int l = k + 2; l < attackerCount; l++) {
				if (random.nextBoolean() && !graph.containsEdge(k+"", l+"")) {
					graph.addEdge(k+"", l+"");
				}
			}
		}	
	}
	
	//==================================================================================================================
	
	static void simulateMultipleWalkBasedAttacks(UndirectedGraph<String, DefaultEdge> graph, List<Integer> attackerCounts, List<Integer> victimCounts) {
		
		/* The graph is assumed to satisfy all requirements, notably vertices being labeled from \sum_{i=1}^{n}attackerCounts[i] on, 
		 * and connectivity if required
		 */
		
		//System.out.println(graph.vertexSet());
		
		SecureRandom random = new SecureRandom();
		
		int addedAttackers = 0, totalAttackers = 0, processedVictims = 0;
		for (int i = 0; i < attackerCounts.size(); i++)
			totalAttackers += attackerCounts.get(i);
		
		for (int i = 0; i < attackerCounts.size(); i++) {
			
			int attackerCount = attackerCounts.get(i), victimCount = 1;
			
			if (i < victimCounts.size() && victimCounts.get(i) > 1)
				victimCount = victimCounts.get(i);
			
			// Check how this condition must change
			// Right now I'm calling this method with parameters that do not cause this problem
			//if (attackerCount + victimCount > graph.vertexSet().size())
			//	victimCount = graph.vertexSet().size() - attackerCount;
			
			for (int j = addedAttackers; j < addedAttackers + attackerCount; j++)
				graph.addVertex(j+"");
			
			addedAttackers += attackerCount;
			
			Hashtable<String, String> fingerprints = new Hashtable<>();
			for (int j = totalAttackers + processedVictims; j < totalAttackers + processedVictims + victimCount; j++) {
				String fingerprint = null;
				do {
					fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, attackerCount)-1) + 1);
					while (fingerprint.length() < attackerCount)
						fingerprint = "0" + fingerprint;
				} while (fingerprints.containsKey(fingerprint));
				
				fingerprints.put(fingerprint, fingerprint);
				
				for (int k = 0; k < fingerprint.length(); k++) {
					if (fingerprint.charAt(k) == '1'){
						graph.addEdge(j+"", (k + addedAttackers - fingerprint.length())+"");
					}
				}
			}
			
			processedVictims += victimCount;
			
			if (attackerCount > 1) {
				for (int k = addedAttackers - attackerCount; k < addedAttackers - 1; k++) {
					graph.addEdge(k+"", (k+1)+"");
				}
			}				
			
			for (int k = addedAttackers - attackerCount; k < addedAttackers - 2; k++) {
				for (int l = k + 2; l < addedAttackers; l++) {
					if (random.nextBoolean() && !graph.containsEdge(k+"", l+"")) {
						graph.addEdge(k+"", l+"");
					}
				}
			}
		}	
	}
	
}