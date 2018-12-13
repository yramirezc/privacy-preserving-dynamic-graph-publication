package anonymization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import util.GraphUtil;

public abstract class DegreeAnonymityLiuTerzi {
	
	// Using Liu and Terzi's algorithms
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k) {
		// Get and sort degree sequence
		TreeMap<Integer, Set<String>> verticesByDegree = new TreeMap<>(Collections.reverseOrder());
		for (String v : graph.vertexSet()) {
			int degree = graph.degreeOf(v);
			if (verticesByDegree.containsKey(degree))
				verticesByDegree.get(degree).add(v);
			else {
				Set<String> verts = new TreeSet<>();
				verts.add(v);
				verticesByDegree.put(degree, verts);
			}
		}
		ArrayList<Integer> degreeSequence = new ArrayList<>();
		ArrayList<String> sortedVertices = new ArrayList<>();
		for (int degree : verticesByDegree.keySet())
			for (String v : verticesByDegree.get(degree)) {
				degreeSequence.add(degree);
				sortedVertices.add(v);
			}
		
		// Anonymize degree sequence
		ArrayList<Integer> anonymousDegreeSequence = new ArrayList<>(degreeSequence);
		anonymizeDegreeSequence(anonymousDegreeSequence, k);
		
		// Add edges to graph to match the new degree sequence
		
		ArrayList<String> sourcesNewEdges = new ArrayList<>();
		ArrayList<String> targetsNewEdges = new ArrayList<>();
		
		while (!getEdgseSet2RealizeDegreeSequence(graph, anonymousDegreeSequence, sortedVertices, sourcesNewEdges, targetsNewEdges)) {
			// According to Liu and Terzi's paper
			int ordSmallest = degreeSequence.size() - 1;
			while (ordSmallest >= 1 && degreeSequence.get(ordSmallest - 1).equals(degreeSequence.get(ordSmallest)))
				ordSmallest--;
			degreeSequence.set(ordSmallest, degreeSequence.get(ordSmallest) + 1);
			
			// A patch to get the method to end faster with high density graphs
			/*int ordLargest = 0;
			while (ordLargest < degreeSequence.size() - 1 && degreeSequence.get(ordLargest).equals(degreeSequence.get(ordLargest + 1)))
				ordLargest++;
			degreeSequence.set(ordLargest + 1, degreeSequence.get(ordLargest + 1) + 1);//*/
			
			// Anonymize the noisy degree sequence
			anonymousDegreeSequence = new ArrayList<>(degreeSequence);
			anonymizeDegreeSequence(anonymousDegreeSequence, k);
		}//*/
		
		// This is where the realization really occurs (edges are added)
		for (int i = 0; i < sourcesNewEdges.size(); i++)
			graph.addEdge(sourcesNewEdges.get(i), targetsNewEdges.get(i));
	}
	
	static void anonymizeDegreeSequence(List<Integer> degreeSequence, int k) {
		
//		System.out.println("Before:");
//		System.out.println(degreeSequence);
		
		// Initialize group costs
		int [][] groupCosts = new int[degreeSequence.size()][];
		for (int i = 0; i < degreeSequence.size(); i++) {
			groupCosts[i] = new int[degreeSequence.size() - i];
			groupCosts[i][0] = 0;   // To itself
			for (int j = 1; j < degreeSequence.size() - i; j++) {
				groupCosts[i][j] = groupCosts[i][j - 1] + degreeSequence.get(i) - degreeSequence.get(i + j);
			}
		}
		
		// Fill DP table 
		int [] subseqCosts = new int[degreeSequence.size()];
		int [] cutPoints = new int[degreeSequence.size()];
		for (int i = 0; i < degreeSequence.size(); i++) {
			if (i + 1 < 2 * k) {
				subseqCosts[i] = groupCosts[0][i];
				cutPoints[i] = -1;   // Means no more cutting
			}
			else {
				// Find best cut point
				//int start = Integer.max(k - 1, i - 2*k + 1);
				int start = k - 1;
				if (start < i - 2*k + 1)
					start = i - 2*k + 1;
				int minValue = subseqCosts[start] + groupCosts[start + 1][i - start - 1];
				int bestCut = start;
				for (int t = start + 1; t <= i - k; t++) {
					int val = subseqCosts[t] + groupCosts[t + 1][i - t - 1];
					if (val < minValue) {
						minValue = val;
						bestCut = t;
					}
				}
				subseqCosts[i] = minValue;
				cutPoints[i] = bestCut;
			}
		}
		
		// Trace back best solution and modify degree sequence
		int end = degreeSequence.size() - 1;
		do {
			int start = (cutPoints[end] < 0)? 0 : cutPoints[end] + 1;
			for (int i = start + 1; i <= end; i++)
				degreeSequence.set(i, degreeSequence.get(start));
			end = cutPoints[end];
		} while (end >= 0);
		
//		System.out.println("After:");
//		System.out.println(degreeSequence);
		
	}
	
	static boolean getEdgseSet2RealizeDegreeSequence(UndirectedGraph<String, DefaultEdge> graph, List<Integer> degreeSequence, List<String> sortedVertices, List<String> sourcesNewEdges, List<String> targetsNewEdges) {
		
		sourcesNewEdges.clear();
		targetsNewEdges.clear();
		
		SecureRandom random = new SecureRandom();
		
		List<Integer> additionalDegrees = new ArrayList<>();
		for (int i = 0; i < degreeSequence.size(); i++)
			additionalDegrees.add(degreeSequence.get(i) - graph.degreeOf(sortedVertices.get(i)));
		int sumDegrees = 0;
		for (int deg : additionalDegrees)
			sumDegrees += deg;
//		System.out.println("Sequence additional degrees:");
//		System.out.println(additionalDegrees);
//		System.out.println("sumAdditionalDegrees == " + sumDegrees);
		if (sumDegrees % 2 == 1)
			return false;
		
		while (true) {
			ArrayList<Integer> indicesNonZeroes = new ArrayList<>();
			for (int i = 0; i < additionalDegrees.size(); i++) 
				if (additionalDegrees.get(i) < 0)
					return false;
				else if (additionalDegrees.get(i) > 0)
					indicesNonZeroes.add(i);
			if (indicesNonZeroes.size() == 0)
				return true;
			int indPicked = indicesNonZeroes.get(random.nextInt(indicesNonZeroes.size()));
			int edgesToAdd = additionalDegrees.get(indPicked);
			additionalDegrees.set(indPicked, 0);
			// Sort remaining non-zero additional degree vertices decrementally by additional degree
			TreeMap<Integer, Set<String>> sortedNonZeroDegVerts = new TreeMap<>(Collections.reverseOrder());
			for (int ind : indicesNonZeroes)
				if (ind != indPicked) {
					int deg = additionalDegrees.get(ind);
					if (sortedNonZeroDegVerts.containsKey(deg))
						sortedNonZeroDegVerts.get(deg).add(sortedVertices.get(ind));
					else {
						Set<String> verts = new TreeSet<>();
						verts.add(sortedVertices.get(ind));
						sortedNonZeroDegVerts.put(deg, verts);
					}
				}
			
			// Determine new edges that should be added if the graph is realizable
			outerLoop: for (int deg : sortedNonZeroDegVerts.keySet())
				for (String v : sortedNonZeroDegVerts.get(deg))
					if (!graph.containsEdge(sortedVertices.get(indPicked), v)) {
						// graph.addEdge(sortedVertices.get(picked), v);    // We will only modify the graph when realization is successful
						sourcesNewEdges.add(sortedVertices.get(indPicked));
						targetsNewEdges.add(v);
						int indV = sortedVertices.indexOf(v); 
						additionalDegrees.set(indV, additionalDegrees.get(indV) - 1);
						edgesToAdd--;
						if (edgesToAdd <= 0)
							break outerLoop;
					}
			if (edgesToAdd > 0)
				return false;
		}
	}
	
	static boolean iskAnonymous(List<Integer> degreeSequence, int k) {
		TreeMap<Integer, Integer> degreeCounts = new TreeMap<>();
		for (int degree : degreeSequence)
			if (degreeCounts.containsKey(degree))
				degreeCounts.replace(degree, degreeCounts.get(degree) + 1);
			else
				degreeCounts.put(degree, 1);
		for (int degree : degreeCounts.keySet())
			if (degreeCounts.get(degree) < k)
				return false;
		return true;
	}
	
	static boolean iskAnonymous(UndirectedGraph<String, DefaultEdge> graph, int k) {
		List<Integer> degreeSequence = new ArrayList<>();
		for (String v : graph.vertexSet())
			degreeSequence.add(graph.degreeOf(v));
		return iskAnonymous(degreeSequence, k);
	}
	
	public static void main(String [] args) {
		SecureRandom random = new SecureRandom();
		/*ArrayList<ArrayList<Integer>> fails = new ArrayList<>();
		int exceptionCount = 0;
		for (int iter = 0; iter < 1000000; iter++) {
			int k = random.nextInt(19) + 2;
			int n = k * (random.nextInt(18) + 3) + random.nextInt(k);
			System.out.println("k == " + k + ", n == " + n);
			ArrayList<Integer> sequence = new ArrayList<>();
			for (int i = 0; i < n; i++) 
				sequence.add(random.nextInt(n - 2) + 1);
			sequence.sort(Collections.reverseOrder());
			try {
				anonymizeDegreeSequence(sequence, k);
			}
			catch (Exception e) {
				exceptionCount++;
				System.out.println("Exception occurred");
			}
			if (!iskAnonymous(sequence, k))
				fails.add(sequence);
		}
		System.out.println("Fails:");
		for (ArrayList<Integer> fail : fails)
			System.out.println(fail);
		System.out.println("Total: " + fails.size() + " fails");
		System.out.println(exceptionCount + " exceptions");//*/
		
		/*
		ArrayList<UndirectedGraph<String, DefaultEdge>> fails = new ArrayList<>();
		int exceptionCount = 0;
		for (int iter = 0; iter < 10000; iter++) {
			int k = random.nextInt(9) + 2;
			int n = k * (random.nextInt(8) + 3) + random.nextInt(k);
			double density = 0.1d * (random.nextInt(10) + 1);
			int m = (int)(density*n*(n-1)/2);
			System.out.println("k == " + k + ", n == " + n + ", m == " + m);
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = 0;
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
			int sumDegree = 0;
			for (String v : graph.vertexSet())
				sumDegree += graph.degreeOf(v);
			System.out.println("sumDegrees == " + sumDegree);
			SimpleGraph<String, DefaultEdge> anonymousGraph = GraphUtil.cloneGraph(graph);
			try {
				anonymizeGraph(anonymousGraph, k); 
				if (!iskAnonymous(anonymousGraph, k))
					fails.add(graph);
			}
			catch (Exception e) {
				exceptionCount++;
				System.out.println("Exception occurred");
			}
		}
		System.out.println("Fails:");
		for (UndirectedGraph<String, DefaultEdge> fail : fails)
			System.out.println(fail.toString());
		System.out.println("Total: " + fails.size() + " fails");
		System.out.println(exceptionCount + " exceptions");//*/
		
		
		int n = 200;
		// Create complete graph
		UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		for (int i = 0; i < n; i++) {
			graph.addVertex(i+"");
			for (int j = 0; j < i; j++)
				//if (random.nextInt(10) < 9)
					graph.addEdge(i+"", j+"");
		}
		// Attack
		graph.addVertex(n+"");
		graph.addEdge(n+"", "0");
		
		SimpleGraph<String, DefaultEdge> anonymousGraph = GraphUtil.cloneGraph(graph);
		// Anonymize
		try {
			anonymizeGraph(anonymousGraph, 2); 
			if (!iskAnonymous(anonymousGraph, 2))
				System.out.println("Not anonymized");
			else
				System.out.println("Anonymized");
		}
		catch (Exception e) {
			System.out.println("Exception occurred");
		}
		
	}

}
