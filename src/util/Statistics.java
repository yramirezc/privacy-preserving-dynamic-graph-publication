package util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import attacks.SybilAttackSimulator;
import attacks.SybilAttackSimulator.SubgraphSearchOvertimed;
import net.vivin.GenericTreeNode;
import test.AntiResolving;
import utilities.GraphParameterBasedUtilitiesJGraphT;

public class Statistics {
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	public static void printStatistics(Writer out, UndirectedGraph<String, DefaultEdge> graph, int[][] arcs, String name) throws IOException{
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
		int  k = AntiResolving.getAnonymityBasis(graph, floyd, 1);
		System.out.println("The "+name+" graph is ("+k+",1)-anonymous");
		double density = GraphUtil.computeDensity(graph);
		System.out.println("The density of the "+name+" graph is "+density);
		double diameter = GraphUtil.computeDiameter(graph, floyd);
		System.out.println("The diameter of the "+name+" graph is "+diameter);
		int connectivity = EdgeConnectivity.edgeConnectivity(arcs);
		System.out.println("The "+name+" graph connectivity is "+connectivity);

		//k, numberOfEdges, density, diameter, connectivity 
		out.append(k+" \t "+graph.vertexSet().size()+" \t "+density
				+" \t "+diameter+" \t "+connectivity+NEW_LINE);
		out.flush();
	}
	
	// This is the original version, which assumes a single attacker
	public static double getSuccessRate(int attackersSize, int victimsSize,
			UndirectedGraph<String, DefaultEdge> graph, UndirectedGraph<String, DefaultEdge> originalGraph) {
		int[] fingerprintDegree = new int[attackersSize];
		boolean[][] fingerprintLinks = new boolean[attackersSize][attackersSize];
		for (int i = 0; i < attackersSize; i++){
			fingerprintDegree[i] = originalGraph.degreeOf(i+"");
		}
		for (int i = 0; i < attackersSize; i++){
			for (int j = 0; j < attackersSize; j++){
				if (originalGraph.containsEdge(i+"", j+""))
					fingerprintLinks[i][j] = true;
				else fingerprintLinks[i][j] = false;
			}
		}
		List<String[]> candidates = getPotentialAttackerCandidates(fingerprintDegree, fingerprintLinks, graph);  
		//now we take one candidate randomly
		if (candidates.isEmpty()) return 0;
		/*Trujillo- Feb 4, 2016
		 * Now, for every victim, we obtain the original fingerprint and look 
		 * for the subset S of vertices with the same fingerprint.
		 * 	- If the subset is empty, then the success probability is 0
		 * 	- If the subset is not empty, but the original victim is not in S, 
		 * 		then again the probability of success is 0 
		 * 	- Otherwise the probability of success is 1/|S|*/   
		double success = 0;
		for (String[] candidate : candidates){
			double successProbForCandidate = 1;
			for (int victim = attackersSize; victim < attackersSize+victimsSize; victim++){
				/*Trujillo- Feb 9, 2016
				 * We first obtain the original fingerprint*/
				String originalFingerPrint = "";
				for (int i = 0; i < attackersSize; i++){
					if (originalGraph.containsEdge(i+"", victim+""))
						originalFingerPrint += "1";
					else originalFingerPrint += "0";
				}
				int cardinalityOfTheSubset = 0;
				boolean victimInsideSubset = false;
				for (String vertex : graph.vertexSet()) {
					String tmpFingerprint = "";
					boolean vertInCandidate = false;
					for (int i = 0; !vertInCandidate && i < candidate.length; i++) {
						if (vertex.equals(candidate[i]))
							vertInCandidate = true;
						else if (graph.containsEdge(candidate[i], vertex))
							tmpFingerprint += "1";
						else
							tmpFingerprint += "0";
					}
					if (!vertInCandidate && tmpFingerprint.equals(originalFingerPrint)) {
						cardinalityOfTheSubset++;
						if (victim == Integer.parseInt(vertex))
							victimInsideSubset = true;
					}
				}
				/*Trujillo- Feb 9, 2016
				 * Note that, the probability to identify this victim is either 0 or 
				 * 1/cardinalityOfTheSubset
				 * The total probability of identifying all victims is the product
				 * While the probability becomes 0 if at least one victim cannot be identified*/
				if (cardinalityOfTheSubset != 0 && victimInsideSubset && successProbForCandidate != 0)
					successProbForCandidate *= 1d/cardinalityOfTheSubset;
				else
					successProbForCandidate = 0;
			}
			/*Trujillo- Feb 9, 2016
			 * For each candidate we sum its probability of success. The total probability is the average*/
			success += successProbForCandidate;
		}
		return success/candidates.size();
	}
	
	// This new version assumes multiple attackers
	public static double getSuccessRateMultipleAttackers(List<Integer> attackersSizes, List<Integer> victimsSizes,
			UndirectedGraph<String, DefaultEdge> graph, UndirectedGraph<String, DefaultEdge> originalGraph, boolean allSuccessful) {
		
		int processedAttackers = 0, totalAttackers = 0, processedVictims = 0;
		for (int i = 0; i < attackersSizes.size(); i++)
			totalAttackers += attackersSizes.get(i);
		
		List<Double> individualAttackerProbabilities = new ArrayList<>();
		
		for (int cnt = 0; cnt < attackersSizes.size(); cnt++) {
			int attackersSize = attackersSizes.get(cnt);
			int victimsSize = victimsSizes.get(cnt);
			
			int[] fingerprintDegree = new int[attackersSize];
			boolean[][] fingerprintLinks = new boolean[attackersSize][attackersSize];
			for (int i = 0; i < attackersSize; i++) {
				fingerprintDegree[i] = originalGraph.degreeOf(i+processedAttackers+"");
			}
			for (int i = 0; i < attackersSize; i++) {
				for (int j = 0; j < attackersSize; j++) {
					if (originalGraph.containsEdge(i+processedAttackers+"", j+processedAttackers+""))
						fingerprintLinks[i][j] = true;
					else 
						fingerprintLinks[i][j] = false;
				}
			}
			
			List<String[]> candidates = getPotentialAttackerCandidates(fingerprintDegree, fingerprintLinks, graph);  
			
			if (candidates.isEmpty())   // This condition came from the original single attacker method
				if (allSuccessful)   // Here, as we are considering the probability of all attacks being successful, it remains valid
					return 0d;
				else {
					individualAttackerProbabilities.add(0d);
					processedAttackers += attackersSize;
					processedVictims += victimsSize;
					continue;
				}
			
			/*Trujillo- Feb 4, 2016
			 * Now, for every victim, we obtain the original fingerprint and look 
			 * for the subset S of vertices with the same fingerprint.
			 * 	- If the subset is empty, then the success probability is 0
			 * 	- If the subset is not empty, but the original victim is not in S, 
			 * 		then again the probability of success is 0 
			 * 	- Otherwise the probability of success is 1/|S|*/   
			double currentAttackerSuccess = 0d;
			for (String[] candidate : candidates) {
				double successProbForCandidate = 1d;
				for (int victim = totalAttackers + processedVictims; victim < totalAttackers + processedVictims + victimsSize; victim++) {
					/*Trujillo- Feb 9, 2016
					 * We first obtain the original fingerprint*/
					String originalFingerPrint = "";
					for (int i = processedAttackers; i < processedAttackers + attackersSize; i++) {
						if (originalGraph.containsEdge(i+"", victim+""))
							originalFingerPrint += "1";
						else 
							originalFingerPrint += "0";
					}
					int cardinalityOfTheSubset = 0;
					boolean victimInsideSubset = false;
					for (String vertex : graph.vertexSet()) {
						String tmpFingerprint = "";
						boolean vertInCandidate = false;
						for (int i = 0; !vertInCandidate && i < candidate.length; i++) {
							if (vertex.equals(candidate[i]))
								vertInCandidate = true;
							else if (graph.containsEdge(candidate[i], vertex))
								tmpFingerprint += "1";
							else
								tmpFingerprint += "0";
						}
						if (!vertInCandidate && tmpFingerprint.equals(originalFingerPrint)) {
							cardinalityOfTheSubset++;
							if (victim == Integer.parseInt(vertex))
								victimInsideSubset = true;
						}
					}
					/*Trujillo- Feb 9, 2016
					 * Note that, the probability to identify this victim is either 0 or 
					 * 1/cardinalityOfTheSubset
					 * The total probability of identifying all victims is the product
					 * While the probability becomes 0 if at least one victim cannot be identified*/
					if (cardinalityOfTheSubset != 0 && victimInsideSubset && successProbForCandidate != 0)
						successProbForCandidate *= 1d/cardinalityOfTheSubset;
					else
						successProbForCandidate = 0d;
				}
				/*Trujillo- Feb 9, 2016
				 * For each candidate we sum its probability of success. The total probability is the average*/
				currentAttackerSuccess += successProbForCandidate;
			}
			
			currentAttackerSuccess /= (double)candidates.size();
			individualAttackerProbabilities.add(currentAttackerSuccess);
			processedAttackers += attackersSize;
			processedVictims += victimsSize;
		}
		
		if (allSuccessful)
			return ProbabilityCombiner.conjunctionAssumingIndependence(individualAttackerProbabilities);
		else
			//return ProbabilityCombiner.randomlyPickingOne(individualAttackerProbabilities);
			return ProbabilityCombiner.disjunctionAssumingIndependence(individualAttackerProbabilities);
	}
	
	//public static List<UndirectedGraph<String, DefaultEdge>> getPotentialAttackerSubgraphs(int[] fingerprintDegrees, 
	public static List<String[]> getPotentialAttackerCandidates(int[] fingerprintDegrees, 
			boolean[][] fingerprintLinks, UndirectedGraph<String, DefaultEdge> graph) {
		GenericTreeNode<String> root = new GenericTreeNode<>("root");
		List<GenericTreeNode<String>> currentLevel = new LinkedList<>();
		List<GenericTreeNode<String>> nextLevel = new LinkedList<>();
		for (int i = 0; i < fingerprintDegrees.length; i++) {
			nextLevel = new LinkedList<>();
			for (String vertex : graph.vertexSet()) {
				int degree = graph.degreeOf(vertex);
				if (degree == fingerprintDegrees[i]){
					if (i == 0){
						/*Trujillo- Feb 4, 2016
						 * At the beggining we just need to add the node as a child of the root*/
						GenericTreeNode<String> newChild = new GenericTreeNode<>(vertex);
						root.addChild(newChild);
						nextLevel.add(newChild);
					}
					else{
						/*Trujillo- Feb 4, 2016
						 * Now we iterate over the last level and add the new vertex if possible*/
						for (GenericTreeNode<String> lastVertex : currentLevel){
							boolean ok = true;
							GenericTreeNode<String> tmp = lastVertex;
							int pos = i-1;
							while (!tmp.equals(root)){
								//we first check whether the vertex has been already considered
								if (tmp.getData().equals(vertex)){
									//this happens because this vertex has been considered already here
									ok = false;
									break;
								}
								//we also check that the link is consistent with fingerprintLinks
								if (graph.containsEdge(vertex, tmp.getData()) && !fingerprintLinks[i][pos]){
									ok = false;
									break;
								}
								if (!graph.containsEdge(vertex, tmp.getData()) && fingerprintLinks[i][pos]){
									ok = false;
									break;
								}
								pos--;
								tmp = tmp.getParent();
							}
							if (ok){
								//we should add this vertex as a child
								tmp = new GenericTreeNode<>(vertex);
								lastVertex.addChild(tmp);
								nextLevel.add(tmp);
							}
						}
					}
				}
			}
			/*Trujillo- Feb 4, 2016
			 * Now we iterate over the current level to check whether a branch could not continue
			 * in which case we remove it completely*/
			currentLevel = nextLevel;
		}
		/*Trujillo- Feb 4, 2016
		 * Now we build subgraphs out of this candidates*/
		//return buildListOfGraphs(root, graph, fingerprintDegrees.length);
		return buildListOfCandidates(root, graph, fingerprintDegrees.length, fingerprintDegrees.length);
		
	}

	public static List<UndirectedGraph<String, DefaultEdge>> buildListOfGraphs(GenericTreeNode<String> root, 
			UndirectedGraph<String, DefaultEdge> graph, int length){
		List<UndirectedGraph<String, DefaultEdge>> result = new LinkedList<>();
		if (length < 0) throw new RuntimeException();
		if (root.isALeaf()){
			if (length > 0) return result;
			UndirectedGraph<String, DefaultEdge> graphResult = new SimpleGraph<>(DefaultEdge.class);
			graphResult.addVertex(root.getData());
			result.add(graphResult);
			return result;
		}
		for (GenericTreeNode<String> child : root.getChildren()){
			List<UndirectedGraph<String, DefaultEdge>> subgraphs = buildListOfGraphs(child, graph, length-1);
			if (!root.isRoot()){
				for (UndirectedGraph<String, DefaultEdge> subgraph : subgraphs) {
					//we add the node and its connections
					subgraph.addVertex(root.getData());
					for (String vertex : subgraph.vertexSet()){
						if (vertex.equals(root.getData())) continue;
						if (graph.containsEdge(vertex, root.getData())) subgraph.addEdge(vertex, root.getData());
					}
				}
			}
			result.addAll(subgraphs);
		}
		return result;
	}
	
	public static List<String[]> buildListOfCandidates(GenericTreeNode<String> root, 
			UndirectedGraph<String, DefaultEdge> graph, int pos, int size){
		List<String[]> result = new LinkedList<>();
		if (pos < 0) throw new RuntimeException();
		if (root.isALeaf()){
			if (pos > 0) return result;
			String[] candidates = new String[size];
			candidates[size-pos-1] = root.getData();
			result.add(candidates);
			return result;
		}
		for (GenericTreeNode<String> child : root.getChildren()){
			List<String[]> subcandidates = buildListOfCandidates(child, graph, pos-1, size);
			if (!root.isRoot()){
				for (String[] subcandidate : subcandidates) {
					//we add the node and its connections
					subcandidate[size-pos-1] = root.getData();
				}
			}
			result.addAll(subcandidates);
		}
		return result;
	}
	

	//public static List<UndirectedGraph<String, DefaultEdge>> getPotentialAttackerSubgraphs(int[] fingerprintDegrees, 
	public static List<UndirectedGraph<String, DefaultEdge>> getPotentialAttackerSubgraphs(int[] fingerprintDegrees, 
			boolean[][] fingerprintLinks, UndirectedGraph<String, DefaultEdge> graph) {
		GenericTreeNode<String> root = new GenericTreeNode<>("root");
		List<GenericTreeNode<String>> currentLevel = new LinkedList<>();
		List<GenericTreeNode<String>> nextLevel = new LinkedList<>();
		for (int i = 0; i < fingerprintDegrees.length; i++) {
			nextLevel = new LinkedList<>();
			for (String vertex : graph.vertexSet()) {
				int degree = graph.degreeOf(vertex);
				if (degree == fingerprintDegrees[i]){
					if (i == 0){
						/*Trujillo- Feb 4, 2016
						 * At the beggining we just need to add the node as a child of the root*/
						GenericTreeNode<String> newChild = new GenericTreeNode<>(vertex);
						root.addChild(newChild);
						nextLevel.add(newChild);
					}
					else{
						/*Trujillo- Feb 4, 2016
						 * Now we iterate over the last level and add the new vertex if possible*/
						for (GenericTreeNode<String> lastVertex : currentLevel){
							boolean ok = true;
							GenericTreeNode<String> tmp = lastVertex;
							int pos = i-1;
							while (!tmp.equals(root)){
								//we first check whether the vertex has been already considered
								if (tmp.getData().equals(vertex)){
									//this happens because this vertex has been considered already here
									ok = false;
									break;
								}
								//we also check that the link is consistent with fingerprintLinks
								if (graph.containsEdge(vertex, tmp.getData()) && !fingerprintLinks[i][pos]){
									ok = false;
									break;
								}
								if (!graph.containsEdge(vertex, tmp.getData()) && fingerprintLinks[i][pos]){
									ok = false;
									break;
								}
								pos--;
								tmp = tmp.getParent();
							}
							if (ok){
								//we should add this vertex as a child
								tmp = new GenericTreeNode<>(vertex);
								lastVertex.addChild(tmp);
								nextLevel.add(tmp);
							}
						}
					}
				}
			}
			/*Trujillo- Feb 4, 2016
			 * Now we iterate over the current level to check whether a branch could not continue
			 * in which case we remove it completely*/
			currentLevel = nextLevel;
		}
		/*Trujillo- Feb 4, 2016
		 * Now we build subgraphs out of this candidates*/
		//return buildListOfGraphs(root, graph, fingerprintDegrees.length);
		return buildListOfGraphs(root, graph, fingerprintDegrees.length);
		
	}
	
	
	// This is the original version, which assumes a single attack
	public static void printStatistics(int index, Writer out, UndirectedGraph<String, 
			DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, String uniqueIdentifier,
			int attackersSize, int victimsSize, UndirectedGraph<String, 
			DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> originalFloyd) throws IOException {
		//System.out.println("Computing anonymity basis");
		//int  k = AntiResolving.getAnonymityBasis(graph, floyd, 1);
		int  k = 0;
		//System.out.println("The "+name+" graph is ("+k+",1)-anonymous");
		double density = GraphUtil.computeDensity(graph);
		double originalDensity = GraphUtil.computeDensity(originalGraph);
		double deltaDensity = density - originalDensity;
		//System.out.println("The density of the "+name+" graph is "+density);
		double diameterAnonymized = GraphUtil.computeDiameter(graph, floyd);
		double diameterOriginal = GraphUtil.computeDiameter(originalGraph, originalFloyd);
		double deltaDiameter = diameterOriginal - diameterAnonymized;
		//double diameter = 0;
		//System.out.println("The diameter of the "+name+" graph is "+diameter);
		double radiusAnonymized = GraphUtil.computeRadius(graph, floyd);
		double radiusOriginal = GraphUtil.computeRadius(originalGraph, originalFloyd);
		double deltaRadius = radiusOriginal - radiusAnonymized;
		int connectivity = EdgeConnectivity.edgeConnectivity(GraphUtil.transformIntoAdjacencyMatrix(graph));
		//int connectivity = 0;
		//System.out.println("The "+name+" graph connectivity is "+connectivity);
		double successRate = getSuccessRate(attackersSize, victimsSize, graph, originalGraph);
		//System.out.println("The success rate for "+name+"  is :"+successRate);
		
		double addedEdges = 0, removedEdges = 0;
		for (String v1 : graph.vertexSet())
			for (String v2 : graph.vertexSet())
				if (v1.compareTo(v2) < 0)
					if (graph.containsEdge(v1, v2) && !originalGraph.containsEdge(v1, v2))
						addedEdges++;
					else if (!graph.containsEdge(v1, v2) && originalGraph.containsEdge(v1, v2))
						removedEdges++;
		
		//System.out.println("The number of removed edges for "+name+"  is :"+removedEdges);
		double removedEdgesUpperbound = removedEdgesUpperbound(originalGraph, originalFloyd);
		//System.out.println("The number of removed edges upper bound for "+name+"  is :"+removedEdgesUpperbound);
		String lineToAppend = index+" \t "+k+" \t "+graph.vertexSet().size()+" \t "+density
				+" \t "+deltaDiameter+" \t "+deltaRadius+" \t "+connectivity+" \t "
				+successRate+" \t "+deltaDensity+" \t "+addedEdges+" \t "+removedEdges+" \t "+removedEdgesUpperbound;
//		CommunityBasedUtilitiesSNAP communityBasedUtilityCalculator = new CommunityBasedUtilitiesSNAP(uniqueIdentifier);
//		List<Integer> measures = new ArrayList<>();
//		measures.add(1);   // Set sizeDistributionKL to be computed
//		measures.add(2);   // Set macroF1 to be computed
//		//measures.add(3);   // Set CICE-BCubed F1 to be computed
//		List<Double> commUtilitiesBigClam = communityBasedUtilityCalculator.computeUtilities(originalGraph, graph, 0, measures);
//		for (double value : commUtilitiesBigClam)
//			lineToAppend += " \t "+value;
//		List<Double> commUtilitiesInfoMap = communityBasedUtilityCalculator.computeUtilities(originalGraph, graph, 5, measures);
//		for (double value : commUtilitiesInfoMap)
//			lineToAppend += " \t "+value;
//		List<Double> commUtilitiesCoDAInbound = communityBasedUtilityCalculator.computeUtilities(originalGraph, graph, 1, measures);
//		for (double value : commUtilitiesCoDAInbound)
//			lineToAppend += " \t "+value;
		lineToAppend += NEW_LINE;
		out.append(lineToAppend);
		out.flush();
	}
	
	// For one robust sybil attacker
	public static void printStatisticsRobustSybilsExp(int index, Writer out, UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, String uniqueIdentifier,
			int attackerCount, int victimCount, SybilAttackSimulator attackSimulator, UndirectedGraph<String, DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> originalFloyd) throws IOException {
		
		double successProb;
		try {
			successProb = attackSimulator.successProbability(attackerCount, victimCount, graph, originalGraph);
		} catch (SubgraphSearchOvertimed e) {
			successProb = 0d;
		}
		
		double addedEdges = 0, removedEdges = 0;
		for (String v1 : graph.vertexSet())
			for (String v2 : graph.vertexSet())
				if (v1.compareTo(v2) < 0)
					if (graph.containsEdge(v1, v2) && !originalGraph.containsEdge(v1, v2))
						addedEdges++;
					else if (!graph.containsEdge(v1, v2) && originalGraph.containsEdge(v1, v2))
						removedEdges++;
		
		String lineToAppend = index + " \t " + successProb + " \t " + addedEdges + " \t " + removedEdges + NEW_LINE;
		out.append(lineToAppend);
		out.flush();
	}
	
	// For one parameter combination of the sybil hiding experiment
	public static void printStatisticsSybilHidingExp(int index, Writer out, UndirectedGraph<String, DefaultEdge> perturbedGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydPerturbed, String uniqueIdentifier,
			int attackerCount, int victimCount, SybilAttackSimulator attackSimulator, UndirectedGraph<String, DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal) throws IOException {
		
		double successProb;
		try {
			successProb = attackSimulator.successProbability(attackerCount, victimCount, perturbedGraph, originalGraph);
		} catch (SubgraphSearchOvertimed e) {
			successProb = 0d;
		}
		
		int addedEdges = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(originalGraph, perturbedGraph);		
		int removedEdges = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(originalGraph, perturbedGraph);
		
		ConnectivityInspector<String, DefaultEdge> connPert = new ConnectivityInspector<>(perturbedGraph);
		String deltaDiameter = "Inf", deltaEffDiameter = "Inf", deltaRadius = "Inf";
		if (connPert.isGraphConnected()) {   // originalGraph is known to be connected
			deltaDiameter = "" + GraphParameterBasedUtilitiesJGraphT.deltaDiameter(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
			deltaEffDiameter = "" + GraphParameterBasedUtilitiesJGraphT.deltaEffectiveDiameter(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
			deltaRadius = "" + GraphParameterBasedUtilitiesJGraphT.deltaRadius(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
		}
		
		double deltaGlobalCC = GraphParameterBasedUtilitiesJGraphT.deltaGlobalClusteringCoefficient(originalGraph, perturbedGraph);
		double deltaAvgdLocalCC = GraphParameterBasedUtilitiesJGraphT.deltaAvgLocalClusteringCoefficient(originalGraph, perturbedGraph);
		double cosDD = GraphParameterBasedUtilitiesJGraphT.cosineSortedDegreeDistributions(originalGraph, perturbedGraph);
		
		String lineToAppend = index + "\t" + successProb + "\t" + addedEdges + "\t" + removedEdges + "\t" + deltaDiameter + "\t" + deltaEffDiameter + "\t" + deltaRadius + "\t" + deltaGlobalCC + "\t" + deltaAvgdLocalCC + "\t" + cosDD + NEW_LINE;
		out.append(lineToAppend);
		out.flush();
	}
	
	// For one parameter combination of the sybil hiding experiment
	public static void printUtilityStatisticsSybilHidingExp(int index, Writer out, UndirectedGraph<String, DefaultEdge> perturbedGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydPerturbed, String uniqueIdentifier,
			int attackerCount, int victimCount, UndirectedGraph<String, DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal) throws IOException {
		
		int addedEdges = GraphParameterBasedUtilitiesJGraphT.countEdgeAdditions(originalGraph, perturbedGraph);		
		int removedEdges = GraphParameterBasedUtilitiesJGraphT.countEdgeRemovals(originalGraph, perturbedGraph);
		
		ConnectivityInspector<String, DefaultEdge> connPert = new ConnectivityInspector<>(perturbedGraph);
		String deltaDiameter = "Inf", deltaEffDiameter = "Inf", deltaRadius = "Inf";
		if (connPert.isGraphConnected()) {   // originalGraph is known to be connected
			deltaDiameter = "" + GraphParameterBasedUtilitiesJGraphT.deltaDiameter(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
			deltaEffDiameter = "" + GraphParameterBasedUtilitiesJGraphT.deltaEffectiveDiameter(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
			deltaRadius = "" + GraphParameterBasedUtilitiesJGraphT.deltaRadius(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
		}
		
		double deltaGlobalCC = GraphParameterBasedUtilitiesJGraphT.deltaGlobalClusteringCoefficient(originalGraph, perturbedGraph);
		double deltaAvgdLocalCC = GraphParameterBasedUtilitiesJGraphT.deltaAvgLocalClusteringCoefficient(originalGraph, perturbedGraph);
		double cosDD = GraphParameterBasedUtilitiesJGraphT.cosineSortedDegreeDistributions(originalGraph, perturbedGraph);
		
		String lineToAppend = index + "\t" + addedEdges + "\t" + removedEdges + "\t" + deltaDiameter + "\t" + deltaEffDiameter + "\t" + deltaRadius + "\t" + deltaGlobalCC + "\t" + deltaAvgdLocalCC + "\t" + cosDD + NEW_LINE;
		out.append(lineToAppend);
		out.flush();
	}
	
	// This is the version for the (k,1)-adjacency experiments (actually it was not used afterwards, at least not in the ones in the KAIS paper)
	public static void printStatisticsK1(int index, Writer out, UndirectedGraph<String, DefaultEdge> graph, 
			FloydWarshallShortestPaths<String, DefaultEdge> floyd, int attackerCount, int victimCount, int k, 
			UndirectedGraph<String, DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> originalFloyd) throws IOException {
		double diameterAnonymized = GraphUtil.computeDiameter(graph, floyd);
		double diameterOriginal = GraphUtil.computeDiameter(originalGraph, originalFloyd);
		double deltaDiameter = diameterOriginal - diameterAnonymized;
		double radiusAnonymized = GraphUtil.computeRadius(graph, floyd);
		double radiusOriginal = GraphUtil.computeRadius(originalGraph, originalFloyd);
		double deltaRadius = radiusOriginal - radiusAnonymized;
		double successRate = getSuccessRate(attackerCount, victimCount, graph, originalGraph);
		double addedEdges = graph.edgeSet().size()-originalGraph.edgeSet().size();
		int addedEdgesLowerBound = addedEdgesLowerBoundK1(originalGraph, k);
		int addedEdgesUpperBound = addedEdgesUpperBoundK1(originalGraph, k);
		String lineToAppend = index+" \t "+graph.vertexSet().size()+" \t "+deltaDiameter+" \t "+deltaRadius+" \t "+successRate+" \t "+addedEdges+" \t "+addedEdgesLowerBound+" \t "+addedEdgesUpperBound;
		lineToAppend += NEW_LINE;
		out.append(lineToAppend);
		out.flush();
	}
	
	// This new version assumes multiple attacks
	public static void printStatisticsMultipleAttacks(int index, Writer out, UndirectedGraph<String, DefaultEdge> graph, 
			FloydWarshallShortestPaths<String, DefaultEdge> floyd, String uniqueIdentifier,
			List<Integer> attackersSizes, List<Integer> victimsSizes, UndirectedGraph<String, 
			DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> originalFloyd) throws IOException {
		int  k = 0;
		double density = GraphUtil.computeDensity(graph);
		double originalDensity = GraphUtil.computeDensity(originalGraph);
		double deltaDensity = density - originalDensity;
		double diameterAnonymized = GraphUtil.computeDiameter(graph, floyd);
		double diameterOriginal = GraphUtil.computeDiameter(originalGraph, originalFloyd);
		double deltaDiameter = diameterOriginal - diameterAnonymized;
		double radiusAnonymized = GraphUtil.computeRadius(graph, floyd);
		double radiusOriginal = GraphUtil.computeRadius(originalGraph, originalFloyd);
		double deltaRadius = radiusOriginal - radiusAnonymized;
		int connectivity = EdgeConnectivity.edgeConnectivity(GraphUtil.transformIntoAdjacencyMatrix(graph));
		double successRateAll = getSuccessRateMultipleAttackers(attackersSizes, victimsSizes, graph, originalGraph, true);
		double successRateOne = getSuccessRateMultipleAttackers(attackersSizes, victimsSizes, graph, originalGraph, false);
		double addedEdges = graph.edgeSet().size()-originalGraph.edgeSet().size();
		String lineToAppend = index+" \t "+k+" \t "+graph.vertexSet().size()+" \t "+density
				+" \t "+deltaDiameter+" \t "+deltaRadius+" \t "+connectivity+" \t "
				+successRateAll+" \t "+successRateOne+" \t "+deltaDensity+" \t "+addedEdges;
		lineToAppend += NEW_LINE;
		out.append(lineToAppend);
		out.flush();
	}
	
	public static double removedEdgesUpperbound(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		double total = 0;
		for (String v1 : graph.vertexSet()){
			double eccentricity = 0; 
			for (String v2 : graph.vertexSet()){
				if (v1.equals(v2)) continue;
				double distance = floyd.shortestDistance(v1, v2);
				if (eccentricity < distance){
					eccentricity = distance;
				}
			}
			total += eccentricity-1;
		}
		return total;
	}
	
	public static int addedEdgesUpperBoundK1(UndirectedGraph<String, DefaultEdge> graph, int k) {
		int sum = 0;
		for (String v : graph.vertexSet())
			if (graph.degreeOf(v) < k)
				sum += k - graph.degreeOf(v);
		return sum;
	}
	
	public static int addedEdgesLowerBoundK1(UndirectedGraph<String, DefaultEdge> graph, int k) {
		int upper = addedEdgesUpperBoundK1(graph, k);
		if (upper % 2 == 0)
			return upper / 2;
		else
			return (upper + 1) / 2;
	}

	/*Trujillo- Feb 16, 2016
	 * This method below needs to be updated*/
	public static void printStatisticsForFuture(int index, Writer out, UndirectedGraph<String, 
			DefaultEdge> graph, int[][] arcs, String name,
			int attackersSize, int victimsSize, UndirectedGraph<String, 
			DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> originalGraphFloyd) throws IOException{
		//System.out.println("Computing floyd");
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
		//System.out.println("Computing anonymity basis");
		//int  k = AntiResolving.getAnonymityBasis(graph, floyd, 1);
		int  k = 0;
		//System.out.println("The "+name+" graph is ("+k+",1)-anonymous");
		double density = GraphUtil.computeDensity(graph);
		int higherDensity = 1;
		double originalDensity = GraphUtil.computeDensity(originalGraph);  
		double percentageDensity =  Math.abs(density - originalDensity)/(higherDensity-originalDensity);
		//System.out.println("The percentage of distorted density "+name+"  is :"+percentageDensity);
		
		/*Trujillo- Feb 16, 2016
		 * Computing statistics related to diameter*/
		System.out.println("The density of the "+name+" graph is "+density);
		int diameter = GraphUtil.computeDiameter(graph, floyd);
		System.out.println("The diameter of the "+name+" graph is "+diameter);
		int longestDiameter = originalGraph.vertexSet().size()-1;
		int originalDiameter = GraphUtil.computeDiameter(originalGraph, originalGraphFloyd);  
		double percentageDiameter =  Math.abs((double)diameter - originalDiameter)/(longestDiameter-originalDiameter);
		System.out.println("The percentage of distorted diameter "+name+"  is :"+percentageDiameter);
		
		/*Trujillo- Feb 16, 2016
		 * Computing statistics related to connectivity*/
		//System.out.println("The diameter of the "+name+" graph is "+diameter);
		//int connectivity = EdgeConnectivity.edgeConnectivity(arcs);
		int connectivity = 0;
		
		//System.out.println("The "+name+" graph connectivity is "+connectivity);
		double successRate = getSuccessRate(attackersSize, victimsSize, graph, originalGraph);
		System.out.println("The success rate for "+name+"  is :"+successRate);
		int maxEdges = (originalGraph.vertexSet().size()-1)*originalGraph.vertexSet().size()/2;
		double percentageOfAddedEdges = Math.abs((double)graph.edgeSet().size() - originalGraph.edgeSet().size())/(maxEdges-originalGraph.edgeSet().size());
		System.out.println("The percentage of added edges for "+name+"  is :"+percentageOfAddedEdges);

		//k, numberOfEdges, density, diameter, connectivity 
		out.append(index+" \t "+k+" \t "+graph.vertexSet().size()+" \t "+density
				+" \t "+diameter+" \t "+connectivity+" \t "+successRate+" \t "+
				percentageDensity+" \t "+percentageDiameter+
				" \t "+percentageOfAddedEdges+NEW_LINE);
		/*out.append(index+" \t "+k+" \t "+graph.vertexSet().size()+" \t "+density
				+" \t "+diameter+" \t "+connectivity+" \t "+successRate+NEW_LINE);*/
		out.flush();
	}


}
