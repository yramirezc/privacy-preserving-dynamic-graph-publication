package anonymization;

import java.util.Map;
import java.util.TreeMap;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

public abstract class MinTriangles extends BaseCycle {
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, int optChoice){
		int edges = graph.edgeSet().size();
		
		getRidOfEndVertices(graph, 1);
		
		System.out.println("It has been added "+(graph.edgeSet().size()-edges)+" edge(s) to get rid of end vertices");

		floyd = new FloydWarshallShortestPaths<>(graph);
		floyd.getDiameter();   // This call is placed here so all necessary computations are performed before comparisons using shortestDistance start.
		
		Transformation trans = findATransformation(floyd, graph, optChoice);
		while (trans != null){
			String v1 = trans.v1;
			TreeMap<Double, String> resolvables = trans.resolvables;
			
			boolean lastAdditionCoveredTwoVerts = false;   // true if both v_k and v_{k-1} are 1-resolvable, as the edge v_{k-2}v_k makes then both stop being 1-resolvable
						
			for (Map.Entry<Double, String> entry : resolvables.descendingMap().entrySet())
				if (lastAdditionCoveredTwoVerts)   // Nothing to do here, just skip it
					lastAdditionCoveredTwoVerts = false;
				else {
					// look for v_{k-1} and v_{k-2}
					String vkPred = null, vkPred2 = null;
					for (String tmpV : graph.vertexSet()){
						if (floyd.shortestDistance(tmpV, entry.getValue()) == 1 && 
								floyd.shortestDistance(v1, tmpV) + 1 == floyd.shortestDistance(v1, entry.getValue()))
							vkPred = tmpV;
						if (floyd.shortestDistance(tmpV, entry.getValue()) == 2 && 
								floyd.shortestDistance(v1, tmpV) + 2 == floyd.shortestDistance(v1, entry.getValue()))
							vkPred2 = tmpV;
						if (vkPred != null && vkPred2 != null)
							break;
					}
					
					if (resolvables.containsValue(vkPred))
						lastAdditionCoveredTwoVerts = true;
					
					graph.addEdge(vkPred2, entry.getValue());
				}
			
			floyd = new FloydWarshallShortestPaths<>(graph);
			floyd.getDiameter();   // This call is placed here so all necessary computations are performed before comparisons using shortestDistance start.
			trans = findATransformation(floyd, graph, optChoice);
		}
	}

}
