package util;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DegreeDistributionComputer implements DistributionComputer {

	@Override
	public Map<Integer, Double> computeDistributionAsProbabilities(UndirectedGraph<String, DefaultEdge> graph) {
		Map<Integer, Double> distribution = new HashMap<>();
		distribution.put(-1, 1d / (double)graph.vertexSet().size());
		for (String v : graph.vertexSet()) {
			int deg = graph.degreeOf(v);
			if (distribution.containsKey(deg))
				distribution.put(deg, distribution.get(deg) + 1d / (double)graph.vertexSet().size());
			else
				distribution.put(deg, 2d / (double)graph.vertexSet().size());
		}
		return distribution;
	}

	@Override
	public Map<Integer, Integer> computeDistributionAsCounts(UndirectedGraph<String, DefaultEdge> graph) {
		Map<Integer, Integer> distribution = new HashMap<>();
		for (String v : graph.vertexSet()) {
			int deg = graph.degreeOf(v);
			if (distribution.containsKey(deg))
				distribution.put(deg, distribution.get(deg) + 1);
			else
				distribution.put(deg, 1);
		}
		return distribution;
	}

}
