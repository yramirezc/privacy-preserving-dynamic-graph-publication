package util;

import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import net.vivin.GenericTreeNode;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import attacks.AttackThreeMethod;
import attacks.Attacker;

import test.AntiResolving;

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
		double fails = 0;
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
				/*Trujillo- Feb 9, 2016
				 * Next, we look for a subset of vertices with the same fingerprint
				 * to the set of candidates*/
				List<String> subset = new LinkedList<>();
				int cardinalityOfTheSubset = 0;
				boolean victimInsideSubset = false;
				for (String vertex : graph.vertexSet()){
					String tmpFingerprint = "";
					for (int i = 0; i < candidate.length; i++){
						if (graph.containsEdge(candidate[i], vertex))
							tmpFingerprint+="1";
						else tmpFingerprint+="0";
					}
					if (tmpFingerprint.equals(originalFingerPrint)){
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
				if (cardinalityOfTheSubset != 0 && victimInsideSubset
						&& successProbForCandidate != 0) successProbForCandidate *= 1d/cardinalityOfTheSubset;
				else successProbForCandidate = 0;
			}
			/*Trujillo- Feb 9, 2016
			 * For each candidate we sum its probability of success. The total probability is the average*/
			success += successProbForCandidate;
		}
		return success/candidates.size();
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
	
	

	
	public static void printStatistics(int index, Writer out, UndirectedGraph<String, 
			DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, String name,
			int attackersSize, int victimsSize, UndirectedGraph<String, 
			DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> originalFloyd) throws IOException{
		//System.out.println("Computing anonymity basis");
		//int  k = AntiResolving.getAnonymityBasis(graph, floyd, 1);
		int  k = 0;
		//System.out.println("The "+name+" graph is ("+k+",1)-anonymous");
		double density = GraphUtil.computeDensity(graph);
		double originalDensity = GraphUtil.computeDensity(originalGraph);
		double distortedDensity = originalDensity-density;
		//System.out.println("The density of the "+name+" graph is "+density);
		//double diameter = GraphUtil.computeDiameter(graph, floyd);
		double diameter = 0;
		//System.out.println("The diameter of the "+name+" graph is "+diameter);
		//int connectivity = EdgeConnectivity.edgeConnectivity(arcs);
		int connectivity = 0;
		//System.out.println("The "+name+" graph connectivity is "+connectivity);
		double successRate = getSuccessRate(attackersSize, victimsSize, graph, originalGraph);
		System.out.println("The success rate for "+name+"  is :"+successRate);
		double removedEdges = graph.edgeSet().size()-originalGraph.edgeSet().size();
		System.out.println("The number of removed edges for "+name+"  is :"+removedEdges);
		double removedEdgesUpperbound = removedEdgesUpperbound(originalGraph, originalFloyd);
		//System.out.println("The number of removed edges upper bound for "+name+"  is :"+removedEdgesUpperbound);		
		out.append(index+" \t "+k+" \t "+graph.vertexSet().size()+" \t "+density
				+" \t "+diameter+" \t "+connectivity+" \t "
				+successRate+" \t "+distortedDensity+" \t "+removedEdges+" \t "+removedEdgesUpperbound+NEW_LINE);
		out.flush();
	}

	private static double removedEdgesUpperbound(
			UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
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
