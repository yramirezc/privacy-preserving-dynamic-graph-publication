package util;

import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class ConnectedErdosRenyiGraphGenerator {
	
	public static SimpleGraph<String, DefaultEdge> newGraph(int n, int firstVertId, double density) {
		
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		GraphGenerator<String, DefaultEdge, String> generator = null;
		final int fvId = firstVertId; 
		VertexFactory<String> vertexFactory = null;
		
		ConnectivityInspector<String, DefaultEdge> conn = null;
		
		do {
			generator = new RandomGraphGenerator<>(n, (int)(density * (n * (n - 1)) / 2));
			
			vertexFactory = new VertexFactory<String>() {
				int i = fvId;
				@Override
				public String createVertex() {
					int result = i;
					i++;
					return result+"";
				}
			};
			generator.generateGraph(graph, vertexFactory, null);
			conn = new ConnectivityInspector<>(graph);
		} while (!conn.isGraphConnected());
		
		return graph;
	}

}
