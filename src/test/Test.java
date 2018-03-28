package test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgraph.graph.Edge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.DominationSet;
import org.jgrapht.alg.WeaklyConnectedDominationSet;
import org.jgrapht.generate.HyperCubeGraphGenerator;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.ClassBasedVertexFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Test {

	public static void main(String[] args) {
		testWeaklyConnectedDominationSet();
		//testDominationSet();
	}
	
	private static void testWeaklyConnectedDominationSet(){
		for (int k = 6; k < 8; k++){
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = 0;
				@Override
				public String createVertex() {
					i++;
					return i+"";
				}
				
			};
			System.out.println("Computing domination set for k = "+k);
			HyperCubeGraphGenerator<String, DefaultEdge> hyperCube = new HyperCubeGraphGenerator<String, DefaultEdge>(k);
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
			Map<String, String> map = new HashMap<String, String>();
			hyperCube.generateGraph(graph, vertexFactory, map);
			System.out.println("Hyper cube for k = "+k);
			for (String v : map.keySet()){
				System.out.println(v+" = "+map.get(v));
			}
			System.out.println("The domination set is");
			Set<String> dominatioSet = WeaklyConnectedDominationSet.findByBruteForceWeaklyConnectedDominationSet(graph);
			System.out.println(dominatioSet);
		}
	}
	
	private static void testDominationSet(){
		
		for (int k = 1; k < 8; k++){
			VertexFactory<String> vertexFactory = new VertexFactory<String>(){
				int i = 0;
				@Override
				public String createVertex() {
					i++;
					return i+"";
				}
				
			};
			System.out.println("Computing domination set for k = "+k);
			HyperCubeGraphGenerator<String, DefaultEdge> hyperCube = new HyperCubeGraphGenerator<String, DefaultEdge>(k);
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
			Map<String, String> map = new HashMap<String, String>();
			hyperCube.generateGraph(graph, vertexFactory, map);
			System.out.println("Hyper cube for k = "+k);
			for (String v : map.keySet()){
				System.out.println(v+" = "+map.get(v));
			}
			System.out.println("The domination set is");
			Set<String> dominatioSet = DominationSet.findByBruteForceDominationSet(graph);
			System.out.println(dominatioSet);
		}
	}
}
