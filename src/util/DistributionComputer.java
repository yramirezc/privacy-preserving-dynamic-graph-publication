package util;

import java.util.Map;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface DistributionComputer {
	
	Map<Integer, Double> computeDistributionAsProbabilities(UndirectedGraph<String, DefaultEdge> graph);
	Map<Integer, Integer> computeDistributionAsCounts(UndirectedGraph<String, DefaultEdge> graph);

}
