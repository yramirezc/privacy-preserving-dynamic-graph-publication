package util;

import java.security.SecureRandom;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class WattsStrogatzGraphGenerator {
	
	public static SimpleGraph<String, DefaultEdge> newGraph(int n, int firstVertId, int k, double rho) {
		
		// Force k to be even (by excess) and at least 2
		if (k < 2)
			k = 2;
		else if (k % 2 == 1)   
			k++;
		
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		// Create vertex set
		for (int i = firstVertId; i < firstVertId + n; i++)
			graph.addVertex(i+"");
		
		// Initialize edge set
		for (int i = 0; i < n; i++) 
			for (int j = 1; j <= k/2; j++)
				graph.addEdge(firstVertId + i + "", firstVertId + (i + j) % n + "");
		
		// Randomize edge set
		SecureRandom randomRewriting = new SecureRandom();
		SecureRandom randomSelector = new SecureRandom();
		for (int i = 0; i < n; i++) {
			for (int j = 1; j <= k/2; j++)
				if (randomRewriting.nextDouble() < rho) {   // Re-write this edge with probability rho
					boolean replacingEdgeFound = false;
					while (!replacingEdgeFound) {
						int r = randomSelector.nextInt(n);
						if (r != i && !graph.containsEdge(firstVertId + i + "", firstVertId + r + "")) {
							graph.addEdge(firstVertId + i + "", firstVertId + r + "");
							replacingEdgeFound = true;
						}
					}
					graph.removeEdge(firstVertId + i + "", firstVertId + (i + j) % n + "");
				}
		}
		
		return graph;
	}

}
