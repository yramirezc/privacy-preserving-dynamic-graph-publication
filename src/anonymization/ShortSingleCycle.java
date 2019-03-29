package anonymization;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

public abstract class ShortSingleCycle extends BaseCycle {
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, int optChoice) {
		
		if (optChoice >= 5)
			optChoice = 4;   // 5 makes no sense for this method
		
		getRidOfEndVertices(graph, 1);
		
		//System.out.println("It has been added "+(graph.edgeSet().size()-edges)+" edge(s) to get rid of end vertices");

		floyd = new FloydWarshallShortestPaths<>(graph);
		Transformation trans = findATransformation(floyd, graph, optChoice);
		while (trans != null){
			String v1 = trans.v1;
			String vm = trans.vm;
			String vi = trans.vi;
			String vj = trans.vj;
			
			int distViVj = (int)floyd.shortestDistance(trans.vj, trans.vi);
			
			/* Particular cases:
			 * 1: if v_i = v_j, we need to add v_{i-2}v_{j}
			 * 2: if d(v_i,v_j) == 2 and j == m, we need to add v_{i-2}v_{j}
			 */
			if (distViVj == 0 || (distViVj == 2 && vj.equals(vm))) {
				// we look for v_{i-2}
				String viPred2 = null;
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(tmpV, vi) == 2 && 
							floyd.shortestDistance(v1, tmpV) + 2 == floyd.shortestDistance(v1, vi)){
						viPred2 = tmpV;
						break;
					}
				}
				graph.addEdge(viPred2, vj);
			}
			else {   // General case: we will add v_{i-1}v_b with b computed by the general rule
				// we look for v_{i-1}
				String viPred = null;
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(tmpV, vi) == 1 && 
							floyd.shortestDistance(v1, tmpV) + 1 == floyd.shortestDistance(v1, vi)){
						viPred = tmpV;
						break;
					}
				}
				
				int thirdDistFloor = distViVj / 3;
				if (distViVj % 3 == 2 && vj.equals(vm))
					thirdDistFloor--;
				//we look for v_{j-thirdDistFloor} (or v_{j-thirdDistFloor+1} if j-i=3*thirdDistFloor+2 and j=m) 
				String vMid = null;
				for (String tmpV : graph.vertexSet()){
					if (((int)floyd.shortestDistance(tmpV, vj)) == thirdDistFloor && 
							floyd.shortestDistance(v1, tmpV) + thirdDistFloor == floyd.shortestDistance(v1, vj)){
						vMid = tmpV;
						break;
					}
				}
				
				graph.addEdge(viPred, vMid);   // now the edge is added from v_{i-1} to v_{j-thirdDistFloor} (or v_{j-thirdDistFloor+1} if j-i=3*thirdDistFloor+2 and j=m)
			}
			
			floyd = new FloydWarshallShortestPaths<>(graph);
			trans = findATransformation(floyd, graph, optChoice);
		}
	}
	
}
