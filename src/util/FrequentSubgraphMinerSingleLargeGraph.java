package util;

import java.util.List;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface FrequentSubgraphMinerSingleLargeGraph {
	List<UndirectedGraph<String, DefaultEdge>> frequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport);
	UndirectedGraph<String, DefaultEdge> frequentSubgraphMaxEdgeCount(UndirectedGraph<String, DefaultEdge> graph, int minSupport);
}
