package anonymization;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class KMatchAnonymizer {

	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k) {
		//test first commit with correct account
	}
	
	public static void main(String [] args) {
		
		// Create graph of Fig. 4 in the k-automorphism paper
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		for (int i = 1; i < 11; i++)
			graph.addVertex(i+"");
		
		graph.addEdge("1", "2");
		graph.addEdge("1", "4");
		graph.addEdge("1", "6");
		graph.addEdge("2", "3");
		graph.addEdge("3", "4");
		graph.addEdge("4", "5");
		graph.addEdge("5", "6");
		graph.addEdge("6", "7");
		graph.addEdge("7", "8");
		graph.addEdge("7", "9");
		graph.addEdge("7", "10");
		graph.addEdge("8", "9");
		graph.addEdge("8", "10");
		
		int origEdgeCount = graph.edgeSet().size();
		
		// Apply the method with k=2
		anonymizeGraph(graph, 2);
		
		// Report effect of anonymization on the graph
		System.out.println("Number of edge modifications: " + Math.abs(graph.edgeSet().size() - origEdgeCount));
		
	}

}
