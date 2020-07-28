package anonymization;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

public abstract class LargeSingleCycle extends BaseCycle {
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, int optChoice) {
		
		if (optChoice == 4 || optChoice >= 6)
			optChoice = 5;   // 4 makes no sense for this method
		
		getRidOfEndVertices(graph, 1);
		
		//System.out.println("It has been added "+(graph.edgeSet().size()-edges)+" edge(s) to get rid of end vertices");

		floyd = new FloydWarshallShortestPaths<>(graph);
		//System.out.println(graph.toString());
		Transformation trans = findATransformation(floyd, graph, optChoice);
		while (trans != null){
			String v1 = trans.v1;
			String vm = trans.vm;
			if (((int)floyd.shortestDistance(v1, vm)) % 2 == 0)   // m - 1 is even, we will add (v_1, v_m)
				graph.addEdge(v1, vm);
			else {   // m - 1 is odd, we will add (v_2, v_m)
				//we look for v_2
				String v2 = null;
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(v1, tmpV) == 1 && floyd.shortestDistance(tmpV, vm) + 1 == floyd.shortestDistance(v1, vm)) {
						v2 = tmpV;
						break;
					}
				}
				graph.addEdge(v2, vm);
			}
			floyd = new FloydWarshallShortestPaths<>(graph);
			trans = findATransformation(floyd, graph, optChoice);
		}
	}

}
