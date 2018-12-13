package anonymization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import util.GraphUtil;

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
	
	public static void main(String[] args) {
		/*
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		graph.addVertex("v1");
		graph.addVertex("vi-1");
		graph.addVertex("vi");
		graph.addVertex("vi+1");
		graph.addVertex("vi+2");
		graph.addVertex("vj");
		graph.addVertex("vj+1");
		graph.addVertex("vj+2");
		graph.addVertex("vj+3");
		graph.addVertex("vj+4");
		graph.addVertex("vm");
		graph.addVertex("w1");
		graph.addVertex("w");
		graph.addVertex("u");
		graph.addVertex("u1");
		graph.addVertex("u2");
		
		graph.addVertex("x");
		
		graph.addEdge("v1", "vi-1");
		graph.addEdge("vi-1", "vi");
		graph.addEdge("vi", "vi+1");
		
		//graph.addEdge("vi+1", "vi+2");
		graph.addEdge("vi+1", "x");
		graph.addEdge("x", "vi+2");
		
		graph.addEdge("vi+2", "vj");
		graph.addEdge("vj", "vj+1");
		graph.addEdge("vj", "vj+3");
		graph.addEdge("vj+1", "vj+2");
		graph.addEdge("vj+3", "vj+4");
		graph.addEdge("vj+2", "vm");
		graph.addEdge("vj+4", "vm");
		graph.addEdge("vi", "w1");
		graph.addEdge("w1", "w");
		graph.addEdge("v1", "vi-1");
		graph.addEdge("v1", "u");
		graph.addEdge("u", "u1");
		graph.addEdge("u1", "u2");
		graph.addEdge("u2", "u");
		System.out.println(graph.toString());
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
		anonymizeGraph(graph, floyd);
		System.out.println(graph.toString());
		
		SimpleGraph<String, DefaultEdge> graph1 = new SimpleGraph<>(DefaultEdge.class);
		
		graph1.addVertex("x");
		graph1.addVertex("u2");
		graph1.addVertex("u1");
		graph1.addVertex("u");
		graph1.addVertex("w");
		graph1.addVertex("w1");
		graph1.addVertex("vm");
		graph1.addVertex("vj+4");
		graph1.addVertex("vj+3");
		graph1.addVertex("vj+2");
		graph1.addVertex("vj+1");
		graph1.addVertex("vj");
		graph1.addVertex("vi+2");
		graph1.addVertex("vi+1");
		graph1.addVertex("vi");
		graph1.addVertex("vi-1");
		graph1.addVertex("v1");
		
		graph1.addEdge("v1", "vi-1");
		graph1.addEdge("vi-1", "vi");
		graph1.addEdge("vi", "vi+1");
		graph1.addEdge("vi+1", "x");
		graph1.addEdge("x", "vi+2");
		graph1.addEdge("vi+2", "vj");
		graph1.addEdge("vj", "vj+1");
		graph1.addEdge("vj", "vj+3");
		graph1.addEdge("vj+1", "vj+2");
		graph1.addEdge("vj+3", "vj+4");
		graph1.addEdge("vj+2", "vm");
		graph1.addEdge("vj+4", "vm");
		graph1.addEdge("vi", "w1");
		graph1.addEdge("w1", "w");
		graph1.addEdge("v1", "vi-1");
		graph1.addEdge("v1", "u");
		graph1.addEdge("u", "u1");
		graph1.addEdge("u1", "u2");
		graph1.addEdge("u2", "u");
		System.out.println(graph1.toString());
		FloydWarshallShortestPaths<String, DefaultEdge> floyd1 = new FloydWarshallShortestPaths<>(graph1);
		anonymizeGraph(graph1, floyd1);
		System.out.println(graph1.toString());
		*/
		
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("v1");
		labels.add("vi-1");
		labels.add("vi");
		labels.add("vi+1");
		labels.add("vi+2");
		labels.add("vj");
		labels.add("vj+1");
		labels.add("vj+2");
		labels.add("vj+3");
		labels.add("vj+4");
		labels.add("vm");
		labels.add("w1");
		labels.add("w");
		labels.add("u");
		labels.add("u1");
		labels.add("u2");
		labels.add("x");
		
		SecureRandom random = new SecureRandom();
		
		ArrayList<Integer> numbersAddedEdgesFirst = new ArrayList<Integer>();
		ArrayList<Integer> numbersAddedEdgesMinEcc = new ArrayList<Integer>();
		ArrayList<Integer> numbersAddedEdgesMaxEcc = new ArrayList<Integer>();
		ArrayList<Integer> numbersAddedEdgesRandom = new ArrayList<Integer>();
		
		for (int i = 0; i < 500; i++){
			
			Collections.shuffle(labels, random);
			
			SimpleGraph<String, DefaultEdge> graphShuffledVerts = new SimpleGraph<>(DefaultEdge.class);
			
			for (int index = 0; index < labels.size(); index++)
				graphShuffledVerts.addVertex(labels.get(index));
			
			graphShuffledVerts.addEdge("v1", "vi-1");
			graphShuffledVerts.addEdge("vi-1", "vi");
			graphShuffledVerts.addEdge("vi", "vi+1");
			graphShuffledVerts.addEdge("vi+1", "x");
			graphShuffledVerts.addEdge("x", "vi+2");
			graphShuffledVerts.addEdge("vi+2", "vj");
			graphShuffledVerts.addEdge("vj", "vj+1");
			graphShuffledVerts.addEdge("vj", "vj+3");
			graphShuffledVerts.addEdge("vj+1", "vj+2");
			graphShuffledVerts.addEdge("vj+3", "vj+4");
			graphShuffledVerts.addEdge("vj+2", "vm");
			graphShuffledVerts.addEdge("vj+4", "vm");
			graphShuffledVerts.addEdge("vi", "w1");
			graphShuffledVerts.addEdge("w1", "w");
			graphShuffledVerts.addEdge("v1", "vi-1");
			graphShuffledVerts.addEdge("v1", "u");
			graphShuffledVerts.addEdge("u", "u1");
			graphShuffledVerts.addEdge("u1", "u2");
			graphShuffledVerts.addEdge("u2", "u");
			
			int origEdgeCount = graphShuffledVerts.edgeSet().size();
			
			SimpleGraph<String, DefaultEdge> clone1GraphShuffledVerts = GraphUtil.cloneGraph(graphShuffledVerts);
			SimpleGraph<String, DefaultEdge> clone2GraphShuffledVerts = GraphUtil.cloneGraph(graphShuffledVerts);
			SimpleGraph<String, DefaultEdge> clone3GraphShuffledVerts = GraphUtil.cloneGraph(graphShuffledVerts);
						
			System.out.println("Original graph:");
			System.out.println(graphShuffledVerts.toString());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydShuffledVerts = new FloydWarshallShortestPaths<>(graphShuffledVerts);
			anonymizeGraph(graphShuffledVerts, floydShuffledVerts, 0);
			numbersAddedEdgesFirst.add(graphShuffledVerts.edgeSet().size() - origEdgeCount);
			System.out.println("Graph anonymized taking first transformation found:");
			System.out.println(graphShuffledVerts.toString());
			
			floydShuffledVerts = new FloydWarshallShortestPaths<>(clone1GraphShuffledVerts);
			anonymizeGraph(clone1GraphShuffledVerts, floydShuffledVerts, 1);
			numbersAddedEdgesMinEcc.add(clone1GraphShuffledVerts.edgeSet().size() - origEdgeCount);
			System.out.println("Graph anonymized prioritizing minimum eccentriciy values:");
			System.out.println(clone1GraphShuffledVerts.toString());
			
			floydShuffledVerts = new FloydWarshallShortestPaths<>(clone2GraphShuffledVerts);
			anonymizeGraph(clone2GraphShuffledVerts, floydShuffledVerts, 2);
			numbersAddedEdgesMaxEcc.add(clone2GraphShuffledVerts.edgeSet().size() - origEdgeCount);
			System.out.println("Graph anonymized prioritizing maximum eccentriciy values:");
			System.out.println(clone2GraphShuffledVerts.toString());
			
			floydShuffledVerts = new FloydWarshallShortestPaths<>(clone3GraphShuffledVerts);
			anonymizeGraph(clone3GraphShuffledVerts, floydShuffledVerts, 3);
			numbersAddedEdgesRandom.add(clone3GraphShuffledVerts.edgeSet().size() - origEdgeCount);
			System.out.println("Graph anonymized taking random transformation:");
			System.out.println(clone3GraphShuffledVerts.toString());
			
			
			
			System.out.println();
		}
		System.out.println("Numbers of added edges taking first transformation found:        " + numbersAddedEdgesFirst.toString());
		System.out.println("   Average: " + avg(numbersAddedEdgesFirst));
		System.out.println("Numbers of added edges prioritizing minimum eccentricity values: " + numbersAddedEdgesMinEcc.toString());
		System.out.println("   Average: " + avg(numbersAddedEdgesMinEcc));
		System.out.println("Numbers of added edges prioritizing maximum eccentricity values: " + numbersAddedEdgesMaxEcc.toString());
		System.out.println("   Average: " + avg(numbersAddedEdgesMaxEcc));
		System.out.println("Numbers of added edges taking random transformation:             " + numbersAddedEdgesRandom.toString());
		System.out.println("   Average: " + avg(numbersAddedEdgesRandom));
	}
	
	static double avg(List<Integer> list) {
		double sum = 0d;
		for (double elem : list)
			sum += (double)elem;
		return sum / list.size();
	}

}
