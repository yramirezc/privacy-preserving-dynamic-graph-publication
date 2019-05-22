package util;

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
	public Set<Set<UndirectedGraph<String, DefaultEdge>>> frequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph,	int minSupport) {
		
		// Create GraMi Searcher object from graph
		gramiSearcher = new Searcher<String, String>(graph, minSupport, 1);
		
		// Run GraMi search method
		gramiSearcher.initialize();
		gramiSearcher.search();
		
		// TODO: Build return value from results of GraMi search
		return null;
	}

	@Override
	public Set<Set<Set<String>>> vertexSetsOfFrequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport) {
		
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
		Set<Set<UndirectedGraph<String, DefaultEdge>>> frSubGr = wrapper.frequentSubgraphs(BarabasiAlbertGraphGenerator.newGraph(100, 0, 50, 10, 3), 10);
		System.out.println(frSubGr.size());
	}

}
