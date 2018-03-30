package Dimension;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Resolving {


	public static boolean isResolving(Set<String> resolving, SimpleGraph<String, DefaultEdge> graph, 
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		if (resolving.isEmpty()) return false;
		if (resolving.size() == graph.vertexSet().size()) return true;
		Hashtable<String, Set<String>> result = new Hashtable<>();
		for (String out : graph.vertexSet()){
			if (resolving.contains(out)) continue;
			String key = findMetricRepresentation(resolving, out, floyd);
			if (!result.containsKey(key)){
				result.put(key, new TreeSet<String>());
			}
			result.get(key).add(out);
		}
		//boolean b = false;
		for (Set<String> eqClass : result.values()){
			if (eqClass.size() > 1) return false;
			//if (eqClass.size() == k) b = true;
		}
		return true;
	}
	

	public static boolean isMultisetResolving(Set<String> resolving, SimpleGraph<String, DefaultEdge> graph, 
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		if (resolving.isEmpty()) return false;
		if (resolving.size() == graph.vertexSet().size()) return true;
		Hashtable<String, Set<String>> result = new Hashtable<>();
		for (String out : graph.vertexSet()){
			if (resolving.contains(out)) continue;
			String key = findMultisetMetricRepresentation(resolving, out, floyd);
			if (!result.containsKey(key)){
				result.put(key, new TreeSet<String>());
			}
			result.get(key).add(out);
		}
		for (Set<String> eqClass : result.values()){
			if (eqClass.size() > 1) return false;
		}
		return true;
	}
	

	public static String findMetricRepresentation(Set<String> resolving,  
			String vertex, FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		if (resolving.contains(vertex)) throw new RuntimeException("Metric representation of an internal vertex is not allowed");
		String key = "";
		for (String in : resolving){
			int distance = (int)floyd.shortestDistance(in, vertex);
			key += distance+"-";
		}
		return key;
	}

	public static String findMultisetMetricRepresentation(Set<String> resolving,  
			String vertex, FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		//if (resolving.contains(vertex)) throw new RuntimeException("Multiset Metric representation of an internal vertex is not allowed");
		SortedMap<Integer, Integer> multiset = new TreeMap<>(); 
		for (String in : resolving){
			int distance = (int)floyd.shortestDistance(in, vertex);
			if (multiset.containsKey(distance)) {
				multiset.put(distance, multiset.get(distance)+1);
			}
			else {
				multiset.put(distance, 1);
			}
		}
		String result = "";
		for (Integer distance  : multiset.keySet()) {
				result += distance+"->"+multiset.get(distance)+",";
		}
		return result;
	}
}
