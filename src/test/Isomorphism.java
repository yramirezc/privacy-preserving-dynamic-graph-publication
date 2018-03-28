package test;

import java.util.List;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import util.Statistics;

import attacks.AttackThreeMethod;

public class Isomorphism {

	public static void main(String[] args) {
		VertexFactory<String> vertexFactory = new VertexFactory<String>(){
			int i = 0;
			@Override
			public String createVertex() {
				int result = i;
				i++;
				return result+"";
			}
			
		};
		UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

		RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(6, 10);
		
		generator.generateGraph(graph, vertexFactory, null);

		System.out.println("Initial graph");
		System.out.println(graph.toString()); 
		
		int[] fingerprintDegree = new int[]{3,3,4};
		
		boolean[][] fingerprintLinks = new boolean[][]{
				new boolean[]{false, true, true},
				new boolean[]{true, false, true},
				new boolean[]{true, true, false},
		};
		
		List<UndirectedGraph<String, DefaultEdge>> result = Statistics.getPotentialAttackerSubgraphs(fingerprintDegree, 
				fingerprintLinks, graph);
		for (UndirectedGraph<String, DefaultEdge> undirectedGraph : result) {
			System.out.println("A candidate");
			System.out.println(undirectedGraph.toString()); 
		}
		
		List<String[]> result2 = Statistics.getPotentialAttackerCandidates(fingerprintDegree, 
				fingerprintLinks, graph);
		for (String[] candidate : result2) {
			System.out.println("A candidate");
			for (int i = 0; i < candidate.length; i++) {
				System.out.print(candidate[i]+","); 
			}
			System.out.println("");
		}
		
	}
}
