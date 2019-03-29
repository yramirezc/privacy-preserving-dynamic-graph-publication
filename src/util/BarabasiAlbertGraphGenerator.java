package util;

import java.security.SecureRandom;
import java.util.ArrayList;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class BarabasiAlbertGraphGenerator {

	public static SimpleGraph<String, DefaultEdge> newGraph(int n, int firstVertId, int m0, int m, int seedTypeId) {
		
		if (m > m0)
			m = m0;
				
		SimpleGraph<String, DefaultEdge> seedGraph = null;
		
		GraphGenerator<String, DefaultEdge, String> generator = null;
		
		final int fvId = firstVertId; 
		VertexFactory<String> vertexFactory = null;
		
		switch (seedTypeId) {
		case 1:   // m-regular ring graph 
			seedGraph = RegularRingGraphGenerator.newGraph(m0, firstVertId, m);   
			break;
		case 2:   // m-regular small-world graph
			seedGraph = RegularSmallWorldGraphGenerator.newGraph(m0, firstVertId, m);
		case 3:   // Connected Erdos-Renyi random graph
			seedGraph = ConnectedErdosRenyiGraphGenerator.newGraph(m0, firstVertId, 0.5);   // Connected ER graph of density 0.5
			break;
		default:    // Complete graph (cases ..., -2, -1, 0, 4, 5, ...)
			generator = new CompleteGraphGenerator<>(m0);
			seedGraph = new SimpleGraph<>(DefaultEdge.class);
			vertexFactory = new VertexFactory<String>() {
				int i = fvId;
				@Override
				public String createVertex() {
					int result = i;
					i++;
					return result+"";
				}
			};
			generator.generateGraph(seedGraph, vertexFactory, null);
		}
		
		return newGraph(n, firstVertId, seedGraph, m);
	}
	
	public static SimpleGraph<String, DefaultEdge> newGraph(int n, int firstVertId, SimpleGraph<String, DefaultEdge> seedGraph, int m) {
		
		if (m > seedGraph.vertexSet().size())
			m = seedGraph.vertexSet().size();
		
		SimpleGraph<String, DefaultEdge> graph = GraphUtil.cloneGraph(seedGraph);
		ArrayList<String> vertices = new ArrayList<>(graph.vertexSet());
				
		// Add remaining n - seedGraph.vertexSet().size() vertices
		
		// Create structures for handling probabilities
		SecureRandom randomSelector = new SecureRandom();
		
		ArrayList<String> tokens = new ArrayList<>();
		for (int i = 0; i < seedGraph.vertexSet().size(); i++) 
			for (int j = 0; j < graph.degreeOf(vertices.get(i)); j++)
				tokens.add(vertices.get(i));
		
		// Add vertices
		for (int i = seedGraph.vertexSet().size(); i < n; i++) {
			graph.addVertex(firstVertId + i + "");
			// Randomly add m edges linking the new vertex to existing ones
			for (int j = 0; j < m; j++) {   
				boolean newEdgeAdded = false;
				while (!newEdgeAdded) {
					String v = tokens.get(randomSelector.nextInt(tokens.size()));
					if (!graph.containsEdge(v, firstVertId + i + "")) {
						graph.addEdge(v, firstVertId + i + "");
						newEdgeAdded = true;
					}
				}
			}
			// Update probability-handling structures
			for (String v : Graphs.neighborListOf(graph, firstVertId + i + ""))
				tokens.add(v);
			for (int j = 0; j < m; j++)
				tokens.add(firstVertId + i + "");
		}
		
		return graph;
		
	}

}
