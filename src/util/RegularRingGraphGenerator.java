package util;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class RegularRingGraphGenerator {
	
	public static SimpleGraph<String, DefaultEdge> newGraph(int n, int firstVertId, int k) {
		
		// Force k to be even (by excess) and at least 2
		if (k < 2)
			k = 2;
		else if (k % 2 == 1)   
			k++;
		
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		// Create vertex set
		for (int i = firstVertId; i < firstVertId + n; i++)
			graph.addVertex(i+"");
		
		// Create edge set
		for (int i = 0; i < n; i++) 
			for (int j = 1; j <= k/2; j++)
				graph.addEdge(firstVertId + i + "", firstVertId + (i + j) % n + "");
		
		return graph;
	}

}
