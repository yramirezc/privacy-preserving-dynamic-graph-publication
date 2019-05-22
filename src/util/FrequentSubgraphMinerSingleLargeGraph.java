package util;

import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface FrequentSubgraphMinerSingleLargeGraph {
	Set<Set<UndirectedGraph<String, DefaultEdge>>> frequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport);
	Set<Set<Set<String>>> vertexSetsOfFrequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport);
}
