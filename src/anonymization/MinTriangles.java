package anonymization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Map;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import util.GraphUtil;

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
			
			System.out.println();
		}
		System.out.println("Numbers of added edges taking first transformation found:        " + numbersAddedEdgesFirst.toString());
		System.out.println("Numbers of added edges prioritizing minimum eccentricity values: " + numbersAddedEdgesMinEcc.toString());
		System.out.println("Numbers of added edges prioritizing maximum eccentricity values: " + numbersAddedEdgesMaxEcc.toString());
		
	}

}
