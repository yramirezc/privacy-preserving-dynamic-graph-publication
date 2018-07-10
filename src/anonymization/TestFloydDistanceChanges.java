package anonymization;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public abstract class TestFloydDistanceChanges {

	public static void main(String[] args) {
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		for (int i = 0; i < 20; i++)
			graph.addVertex("v"+i);
		for (int i = 0; i < 19; i++)
			graph.addEdge("v"+i, "v"+(i+1));
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
		System.out.println(graph.toString());
		System.out.println("d="+floyd.shortestDistance("v0", "v19"));
		graph.addEdge("v8", "v12");
		System.out.println(graph.toString());
		System.out.println("d="+floyd.shortestDistance("v0", "v19"));
		graph.addEdge("v3", "v15");
		System.out.println(graph.toString());
		System.out.println("d="+floyd.shortestDistance("v0", "v19"));
	}

}
