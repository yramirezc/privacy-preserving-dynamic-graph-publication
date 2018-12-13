package link_prediction;

import java.util.Set;
import java.util.HashSet;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public abstract class NeighborhoodBasedScorer {
	
	public static double scoreJaccard(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2){
		if (graph.containsEdge(v1, v2))
			return 1.0;
		else {
			Set<String> intersection = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			intersection.retainAll(Graphs.neighborListOf(graph, v2));
			Set<String> union = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			union.addAll(Graphs.neighborListOf(graph, v2));
			return intersection.size() / (double)union.size();
		}
	}
	
	public static double scoreSoerensen(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2){
		if (graph.containsEdge(v1, v2))
			return 1.0;
		else {
			Set<String> intersection = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			intersection.retainAll(Graphs.neighborListOf(graph, v2));
			return intersection.size() / (double)(Graphs.neighborListOf(graph, v1).size() + Graphs.neighborListOf(graph, v2).size());
		}
	}
	
	public static double scoreCosineSimilarity(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2){
		if (graph.containsEdge(v1, v2))
			return 1.0;
		else {
			Set<String> intersection = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			intersection.retainAll(Graphs.neighborListOf(graph, v2));
			return intersection.size() / Math.sqrt(Graphs.neighborListOf(graph, v1).size() * Graphs.neighborListOf(graph, v2).size());
		}
	}
	
	public static double scoreLeichtHolmeNerman(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2){
		if (graph.containsEdge(v1, v2))
			return 1.0;
		else {
			Set<String> intersection = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			intersection.retainAll(Graphs.neighborListOf(graph, v2));
			return intersection.size() / (double)(Graphs.neighborListOf(graph, v1).size() * Graphs.neighborListOf(graph, v2).size());
		}
	}
	
	public static double scoreHubPromoted(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2){
		if (graph.containsEdge(v1, v2))
			return 1.0;
		else {
			Set<String> intersection = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			intersection.retainAll(Graphs.neighborListOf(graph, v2));
			return intersection.size() / Math.min((double)(Graphs.neighborListOf(graph, v1).size()), (double)(Graphs.neighborListOf(graph, v2).size()));
		}
	}
	
	public static double scoreHubDepressed(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2){
		if (graph.containsEdge(v1, v2))
			return 1.0;
		else {
			Set<String> intersection = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			intersection.retainAll(Graphs.neighborListOf(graph, v2));
			return intersection.size() / Math.max((double)(Graphs.neighborListOf(graph, v1).size()), (double)(Graphs.neighborListOf(graph, v2).size()));
		}
	}
	
	public static double scoreAdamicAdar(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2){
		if (graph.containsEdge(v1, v2))
			return 1.0;
		else {
			Set<String> intersection = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			intersection.retainAll(Graphs.neighborListOf(graph, v2));
			double sum = 0.0;
			for (String vInt : intersection)
				sum += 1.0 / Math.log(Graphs.neighborListOf(graph, vInt).size());
			return sum;
		}
	}
	
	public static double scoreResourceAllocation(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2){
		if (graph.containsEdge(v1, v2))
			return 1.0;
		else {
			Set<String> intersection = new HashSet<String>(Graphs.neighborListOf(graph, v1));
			intersection.retainAll(Graphs.neighborListOf(graph, v2));
			double sum = 0.0;
			for (String vInt : intersection)
				sum += 1.0 / Graphs.neighborListOf(graph, vInt).size();
			return sum;
		}
	}
	
}
