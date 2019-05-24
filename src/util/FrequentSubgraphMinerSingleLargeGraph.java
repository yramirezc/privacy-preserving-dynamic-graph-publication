package util;

import java.util.List;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface FrequentSubgraphMinerSingleLargeGraph {
	List<List<UndirectedGraph<String, DefaultEdge>>> frequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport);
	List<List<Set<String>>> vertexSetsOfFrequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport);
}
