package anonymization;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import test.AntiResolving;

public abstract class OddCycle {
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd){
		getRidOfEndVertices(graph);
		floyd = new FloydWarshallShortestPaths<>(graph);
		//System.out.println(graph.toString());
		Transformation trans = findATransformation(floyd, graph);
		while (trans != null){
			String v1 = trans.v1;
			String vm = trans.vm;
			String vi = trans.vi;
			String vj = trans.vj;
			if (((int)floyd.shortestDistance(trans.vj, trans.vi)) % 2 == 1){
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
			else{
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
			trans = findATransformation(floyd, graph);
		}
	}
	
	private static void getRidOfEndVertices(
			UndirectedGraph<String, DefaultEdge> graph) {
		List<String> endVertices = new ArrayList<>();
		for (String v : graph.vertexSet()){
			if (graph.degreeOf(v) == 1) endVertices.add(v);
		}
		if (endVertices.isEmpty()) return;
		else if (endVertices.size() == 1){
			String v1 = endVertices.get(0);
			for (String v2 : graph.vertexSet()){
				if (!v1.equals(v2) && !graph.containsEdge(v1, v2)){
					graph.addEdge(v1, v2);
					break;
				}
			}
		}
		else{
			for (int i = 0; i < endVertices.size()-1; i+= 2){
				graph.addEdge(endVertices.get(i), endVertices.get(i+1));
			}
			if (endVertices.size() % 2 == 1){
				String v1 = endVertices.get(endVertices.size()-1);
				for (String v2 : graph.vertexSet()){
					if (!v1.equals(v2) && !graph.containsEdge(v1, v2)){
						graph.addEdge(v1, v2);
						break;
					}
				}
			}
		}
	}

	public static Transformation findATransformation(
			FloydWarshallShortestPaths<String, DefaultEdge> floyd,
			UndirectedGraph<String, DefaultEdge> graph) {
		double distance;
		for (String v1 : graph.vertexSet()){
			TreeMap<Double, LinkedList<String>> distances = new TreeMap<>();
			for (String v2 : graph.vertexSet()){
				if (v1.equals(v2)) continue;
				distance = floyd.shortestDistance(v1, v2);
				if (!distances.containsKey(distance)){
					distances.put(distance, new LinkedList<String>());
				}
				distances.get(distance).add(v2);
			}
			TreeMap<Double, String> resolvables = new TreeMap<>();
			for (LinkedList<String> anonymitySet : distances.values()){
				if (anonymitySet.size() == 1) {
					String v2 = anonymitySet.getFirst();
					resolvables.put(floyd.shortestDistance(v1, v2), v2);
				}
			}
			if (!resolvables.isEmpty()){
				//in this case v1 is a 1-antiresolving set
				//the eccentricity path can be found in the "distances" treeMap
				String vm = distances.lastEntry().getValue().getFirst();
				//the last resolvable is the last of the treemap "resolvgin"
				String vj = resolvables.lastEntry().getValue();
				//the first resolving is the first of the treemap "resolvgin"
				String vi = resolvables.firstEntry().getValue();
				return new Transformation(v1, vi, vj, vm);
			}
		}
		return null;
	}

	static class Transformation{
		public String v1, vi, vj, vm;

		public Transformation(String v1, String vi, String vj, String vm) {
			super();
			this.v1 = v1;
			this.vi = vi;
			this.vj = vj;
			this.vm = vm;
		}
		
	}
	
	
	
	public static void main(String[] args) {
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
		graph.addEdge("v1", "vi-1");
		graph.addEdge("vi-1", "vi");
		graph.addEdge("vi", "vi+1");
		graph.addEdge("vi+1", "vi+2");
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
	}
}
