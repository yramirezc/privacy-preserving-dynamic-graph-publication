package util;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class RegularSmallWorldGraphGenerator {
	
	public static SimpleGraph<String, DefaultEdge> newGraph(int n, int firstVertId, int k) {
		
		if (n % 2 == 0)   // Force k to be odd (by excess) and at least 3 
			if (k < 3)
				k = 3;
			else if (k % 2 == 0)   
				k++;
		else   // Force k to be even (by excess) and at least 2
			if (k < 2)
				k = 2;
			else if (k % 2 == 1)
				k++;
		
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		// Create vertex set
		for (int i = firstVertId; i < firstVertId + n; i++)
			graph.addVertex(i+"");
		
		// Create edge set
		// First create outer cycle
		for (int i = 0; i < n; i++)
			graph.addEdge(firstVertId + i + "", firstVertId + (i + 1) % n + "");
		// Create remaining edges
		for (int i = 0; i < n; i++) {   	
			if (n % 2 == 0) {   // Every vertex has one antipodal vertex
				// Add edge to antipodal vertex
				if (!graph.containsEdge(firstVertId + i + "", firstVertId + (i + n / 2) % n + ""))
					graph.addEdge(firstVertId + i + "", firstVertId + (i + n / 2) % n + "");
				// Add edges to remaining vertices
				for (int j = 1; j <= (k-3)/2; j++) {
					if (!graph.containsEdge(firstVertId + i + "", firstVertId + (i + n/2 - j) % n + ""))
						graph.addEdge(firstVertId + i + "", firstVertId + (i + n/2 - j) % n + "");
					if (!graph.containsEdge(firstVertId + i + "", firstVertId + (i + n/2 + j) % n + ""))
						graph.addEdge(firstVertId + i + "", firstVertId + (i + n/2 + j) % n + "");
				}
			}
			else {   // Every vertex has two antipodal vertices
				for (int j = 0; j < (k-2)/2; j++) {
					if (!graph.containsEdge(firstVertId + i + "", firstVertId + (i + n/2 - j) % n + ""))
						graph.addEdge(firstVertId + i + "", firstVertId + (i + n/2 - j) % n + "");
					if (!graph.containsEdge(firstVertId + i + "", firstVertId + (i - n/2 + j) % n + ""))
						graph.addEdge(firstVertId + i + "", firstVertId + (i - n/2 + j) % n + "");
				}
			}
		}
		
		return graph;
	}

}
