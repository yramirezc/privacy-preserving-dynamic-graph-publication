package clustering;

import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface VertexClusterer {
	public Set<Set<String>> getPartitionalClustering(UndirectedGraph<String, DefaultEdge> graph);
	public Set<Set<String>> getPartitionalClustering(UndirectedGraph<String, DefaultEdge> graph, int defaultClusterSize, int extraElemClusterCount);
}
