package util;

import java.util.Map;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface DistributionComputer {
	
	Map<Integer, Double> computeDistributionAsProbabilities(UndirectedGraph<String, DefaultEdge> graph);
	Map<Integer, Integer> computeDistributionAsCounts(UndirectedGraph<String, DefaultEdge> graph);
	Map<Integer, Set<String>> computeDistributionAsSets(UndirectedGraph<String, DefaultEdge> graph);

}
