package anonymization;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

public abstract class AllTriangles extends BaseCycle {
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, int optChoice){
		//int edges = graph.edgeSet().size();
		
		getRidOfEndVertices(graph, 1);
		
		//System.out.println("It has been added "+(graph.edgeSet().size()-edges)+" edge(s) to get rid of end vertices");

		floyd = new FloydWarshallShortestPaths<>(graph);
				
		Transformation trans = findATransformation(floyd, graph, optChoice);
		while (trans != null){
			String v1 = trans.v1;
			
			int distViVj = (int)floyd.shortestDistance(trans.vj, trans.vi);
			
			String vSecond = trans.vj;
			
			for (int edgeCnt = 0; edgeCnt < distViVj / 2 + 1; edgeCnt++){
				//we look for v_{Second-2}
				String vFirst = null;
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(tmpV, vSecond) == 2 && 
							floyd.shortestDistance(v1, tmpV) + 2 == floyd.shortestDistance(v1, vSecond)){
						vFirst = tmpV;
						break;
					}
				}
				graph.addEdge(vFirst, vSecond);
				vSecond = vFirst;
			}
			
			floyd = new FloydWarshallShortestPaths<>(graph);
			trans = findATransformation(floyd, graph, optChoice);
		}
	}
}
