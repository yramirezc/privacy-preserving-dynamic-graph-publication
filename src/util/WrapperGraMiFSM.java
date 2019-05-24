package util;

import java.util.List;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import grami.search.Searcher;
import grami.utilities.Settings;

public class WrapperGraMiFSM implements FrequentSubgraphMinerSingleLargeGraph {
	
	protected Searcher<String, String> gramiSearcher;

	public WrapperGraMiFSM() {
	}

	@Override
	public List<List<UndirectedGraph<String, DefaultEdge>>> frequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph,	int minSupport) {
		
		// Create GraMi Searcher object from graph
		gramiSearcher = new Searcher<String, String>(graph, minSupport, 1);
		
		// Run GraMi search method
		gramiSearcher.initialize();
		gramiSearcher.search();
		
		System.out.println(gramiSearcher.result.toString());
		
		//
		// TODO: Continue from here
		//
		
		// TODO: Build return value from results of GraMi search
		return null;
	}

	@Override
	public List<List<Set<String>>> vertexSetsOfFrequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport) {
		
		// Create GraMi Searcher object from graph
		gramiSearcher = new Searcher<String, String>(graph, minSupport, 1);
		
		// Run GraMi search method
		gramiSearcher.initialize();
		gramiSearcher.search();
		
		// TODO: Build return value from results of GraMi search
		return null;
	}
	
	public static void main(String [] args) {		
		WrapperGraMiFSM wrapper = new WrapperGraMiFSM();
		List<List<UndirectedGraph<String, DefaultEdge>>> frSubGr = wrapper.frequentSubgraphs(BarabasiAlbertGraphGenerator.newGraph(20, 0, 10, 5, 3), 20);
		System.out.println(frSubGr.size());
	}

}
