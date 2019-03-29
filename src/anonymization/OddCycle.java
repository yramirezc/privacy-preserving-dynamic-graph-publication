package anonymization;

import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

public abstract class OddCycle extends BaseCycle {
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, int optChoice){
		
		if (optChoice >= 6)
			optChoice = 3;
		
		getRidOfEndVertices(graph, 1);

		floyd = new FloydWarshallShortestPaths<>(graph);
		
		Transformation trans = findATransformation(floyd, graph, optChoice);
		while (trans != null) {
			String v1 = trans.v1;
			String vi = trans.vi;
			String vj = trans.vj;
			if (((int)floyd.shortestDistance(trans.vj, trans.vi)) % 2 == 1) {
				//we look for v_{i-1}
				String viPred = null;
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(tmpV, vi) == 1 && 
							floyd.shortestDistance(v1, tmpV) + 1 == floyd.shortestDistance(v1, vi)){
						viPred = tmpV;
						break;
					}
				}
				graph.addEdge(viPred, vj);
			}
			else {
				//we look for v_{i-2}
				String viPredPred = null;
				if (floyd.shortestDistance(v1, vi) < 2)
					throw new RuntimeException("The distace is = "+floyd.shortestDistance(v1, vi)+", which is too short"+
								". The degree of v1 is = "+graph.degreeOf(v1));
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(tmpV, vi) == 2 && 
							floyd.shortestDistance(v1, tmpV) + 2 == floyd.shortestDistance(v1, vi)){
						viPredPred = tmpV;
						break;
					}
				}
				graph.addEdge(viPredPred, vj);
			}
			floyd = new FloydWarshallShortestPaths<>(graph);
			trans = findATransformation(floyd, graph, optChoice);
		}
	}
	
	public static void anonymousTransformation(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		
		getRidOfEndVertices(graph, 1);

		floyd = new FloydWarshallShortestPaths<>(graph);
		
		Set<String> originalAntires = findOriginal1Antires(floyd, graph);
		
		Transformation trans = findATransformation(floyd, graph, originalAntires);
		while (trans != null) {
			String v1 = trans.v1;
			String vi = trans.vi;
			String vj = trans.vj;
			if (((int)floyd.shortestDistance(trans.vj, trans.vi)) % 2 == 1) {
				//we look for v_{i-1}
				String viPred = null;
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(tmpV, vi) == 1 && 
							floyd.shortestDistance(v1, tmpV) + 1 == floyd.shortestDistance(v1, vi)){
						viPred = tmpV;
						break;
					}
				}
				graph.addEdge(viPred, vj);
			}
			else {
				//we look for v_{i-2}
				String viPredPred = null;
				if (floyd.shortestDistance(v1, vi) < 2)
					throw new RuntimeException("The distace is = "+floyd.shortestDistance(v1, vi)+", which is too short"+
								". The degree of v1 is = "+graph.degreeOf(v1));
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(tmpV, vi) == 2 && 
							floyd.shortestDistance(v1, tmpV) + 2 == floyd.shortestDistance(v1, vi)){
						viPredPred = tmpV;
						break;
					}
				}
				graph.addEdge(viPredPred, vj);
			}
			floyd = new FloydWarshallShortestPaths<>(graph);
			trans = findATransformation(floyd, graph, originalAntires);
		}
	}
	
}
