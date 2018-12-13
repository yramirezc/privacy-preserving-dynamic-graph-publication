package clustering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class ClusterSimilarityCalculator {
	
	public static double contTableCompleteLinkClusterSimilarity(UndirectedGraph<String, DefaultEdge> graph, Set<String> cluster1, Set<String> cluster2) {	
		if (cluster1.size() > 0 && cluster2.size() > 0) {
			double minSimilarity = (double)graph.vertexSet().size();
			for (String v1 : cluster1)
				for (String v2 : cluster2) {
					double similarity = contTableVertSimilarity(graph, v1, v2);
					if (similarity < minSimilarity)
						minSimilarity = similarity;
				}
			return minSimilarity;
		}
		return 0d;
	}
	
	public static double contTableSingleLinkClusterSimilarity(UndirectedGraph<String, DefaultEdge> graph, Set<String> cluster1, Set<String> cluster2) {	
		if (cluster1.size() > 0 && cluster2.size() > 0) {
			double maxSimilarity = -1d;
			for (String v1 : cluster1)
				for (String v2 : cluster2) {
					double similarity = contTableVertSimilarity(graph, v1, v2);
					if (similarity > maxSimilarity)
						maxSimilarity = similarity;
				}
			return maxSimilarity;
		}
		return 0d;
	}
	
	public static double contTableAverageLinkClusterSimilarity(UndirectedGraph<String, DefaultEdge> graph, Set<String> cluster1, Set<String> cluster2) {	
		if (cluster1.size() > 0 && cluster2.size() > 0) {
			double sumSimilarities = 0d;
			for (String v1 : cluster1)
				for (String v2 : cluster2)
					sumSimilarities += contTableVertSimilarity(graph, v1, v2);
			return sumSimilarities / (double)(cluster1.size() * cluster2.size());
		}
		return 0d;
	}
	
	public static double contTableVertSimilarity(UndirectedGraph<String, DefaultEdge> graph, String v1, String v2) {
		if (graph.containsVertex(v1) && graph.containsVertex(v2)) {
			List<String> neighborhood1 = Graphs.neighborListOf(graph, v1);
			List<String> neighborhood2 = Graphs.neighborListOf(graph, v2);
			Set<String> inBothNeighborhoods = new HashSet<>(neighborhood1);
			inBothNeighborhoods.retainAll(neighborhood2);
			Set<String> outOfBothNeighborhoods = new HashSet<>(graph.vertexSet());
			outOfBothNeighborhoods.remove(v1);
			outOfBothNeighborhoods.remove(v2);
			outOfBothNeighborhoods.removeAll(neighborhood1);
			outOfBothNeighborhoods.removeAll(neighborhood2);
			return (double)(inBothNeighborhoods.size() + outOfBothNeighborhoods.size());
		}
		else
			return 0d;
	}

}
