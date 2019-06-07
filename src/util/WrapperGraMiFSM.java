package util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import grami.search.Searcher;
import real.FacebookGraph;

public class WrapperGraMiFSM implements FrequentSubgraphMinerSingleLargeGraph {
	
	protected Searcher<String, String> gramiSearcher;

	@Override
	public List<UndirectedGraph<String, DefaultEdge>> frequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport) {
		
		// Create GraMi Searcher object from graph
		gramiSearcher = new Searcher<String, String>(graph, minSupport, 1);
		
		// Run GraMi search method
		gramiSearcher.initialize();
		gramiSearcher.search();
		
		// Build return value from results of GraMi search
		List<UndirectedGraph<String, DefaultEdge>> freqSubgraphTemplates = new ArrayList<>();
		for (int i = 0; i < gramiSearcher.result.size(); i++) {
			UndirectedGraph<String, DefaultEdge> fsgTemplate = new SimpleGraph<>(DefaultEdge.class);
			BitSet nodes = gramiSearcher.result.get(i).getNodes();
			for (int nodeIdx = nodes.nextSetBit(0); nodeIdx >= 0; nodeIdx = nodes.nextSetBit(nodeIdx + 1)) 
				fsgTemplate.addVertex(nodeIdx + "");
			
			BitSet edges = gramiSearcher.result.get(i).getEdges();
			for (int edgeIdx = edges.nextSetBit(0); edgeIdx >= 0; edgeIdx = edges.nextSetBit(edgeIdx + 1)) {
				int node1=gramiSearcher.result.get(i).getNodeA(edgeIdx);
				int node2=gramiSearcher.result.get(i).getNodeB(edgeIdx);
				fsgTemplate.addEdge(node1+"", node2+"");
			}
			freqSubgraphTemplates.add(fsgTemplate);
		}
		
		return freqSubgraphTemplates;
	}
	
	@Override
	public UndirectedGraph<String, DefaultEdge> frequentSubgraphMaxEdgeCount(UndirectedGraph<String, DefaultEdge> graph, int minSupport) {
		List<UndirectedGraph<String, DefaultEdge>> freqSubgraphs = frequentSubgraphs(graph, minSupport);
		List<UndirectedGraph<String, DefaultEdge>> fsgMaxEdgeCount = null;
		int maxEdgeCount = -1;
		for (UndirectedGraph<String, DefaultEdge> fsg : freqSubgraphs)
			if (fsg.edgeSet().size() > maxEdgeCount) {
				maxEdgeCount = fsg.edgeSet().size();
				fsgMaxEdgeCount = new ArrayList<>();
				fsgMaxEdgeCount.add(GraphUtil.cloneGraph(fsg));
			}
			else if (fsg.edgeSet().size() == maxEdgeCount)
				fsgMaxEdgeCount.add(GraphUtil.cloneGraph(fsg));
		SecureRandom random = new SecureRandom();
		return fsgMaxEdgeCount.get(random.nextInt(fsgMaxEdgeCount.size()));
	}
	
	public static void main(String [] args) {		
		WrapperGraMiFSM wrapper = new WrapperGraMiFSM();
		//List<UndirectedGraph<String, DefaultEdge>> frSubGr = wrapper.frequentSubgraphs(RegularRingGraphGenerator.newGraph(10, 0, 2), 2);
		List<UndirectedGraph<String, DefaultEdge>> frSubGr = wrapper.frequentSubgraphs(new FacebookGraph(DefaultEdge.class), 20);
		System.out.println(frSubGr.size());
	}
	
}
