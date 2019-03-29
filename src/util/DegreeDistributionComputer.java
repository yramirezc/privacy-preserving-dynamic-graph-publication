package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import real.FacebookGraph;

public class DegreeDistributionComputer implements DistributionComputer {

	@Override
	public Map<Integer, Double> computeDistributionAsProbabilities(UndirectedGraph<String, DefaultEdge> graph) {
		Map<Integer, Double> distribution = new HashMap<>();
		distribution.put(-1, 1d);
		double mass = 1d;
		for (String v : graph.vertexSet()) {
			int deg = graph.degreeOf(v);
			if (distribution.containsKey(deg)) {
				distribution.put(deg, distribution.get(deg) + 1d);
				mass += 1d;
			}
			else {
				distribution.put(deg, 2d);
				mass += 2d;
			}
		}
		
		for (int d : distribution.keySet())
			distribution.put(d, distribution.get(d) / mass);
		
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
	
	public static void main(String [] args) {
		
		UndirectedGraph<String, DefaultEdge> graph = new FacebookGraph(DefaultEdge.class);
		
		DegreeDistributionComputer degDistComp = new DegreeDistributionComputer();
		System.out.println(degDistComp.computeDistributionAsCounts(graph).toString());
	}

	@Override
	public Map<Integer, Set<String>> computeDistributionAsSets(UndirectedGraph<String, DefaultEdge> graph) {
		Map<Integer, Set<String>> distribution = new HashMap<>();
		for (String v : graph.vertexSet()) {
			int deg = graph.degreeOf(v);
			if (distribution.containsKey(deg))
				distribution.get(deg).add(v);
			else {
				Set<String> newEntry = new HashSet<>();
				newEntry.add(v);
				distribution.put(deg, newEntry);
			}
		}
		return distribution;
	}

}
