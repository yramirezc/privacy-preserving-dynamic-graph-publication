package anonymization;

import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import clustering.CompleteLinkAgglomerativeClustering;
import clustering.VertexClusterer;

public class ClusteringBasedAnonymizer {
	
	protected VertexClusterer clusterer;
	
	public ClusteringBasedAnonymizer() {
		clusterer = new CompleteLinkAgglomerativeClustering();
	}
	
	public void enforcekEllAnonymity(UndirectedGraph<String, DefaultEdge> graph, int k, int ell) {
		Set<Set<String>> clusters = clusterer.getPartitionalClustering(graph, k + ell, graph.vertexSet().size() % (k + ell));
		for (Set<String> clust : clusters)
			ClusterEqualizer.enforceTwinEqClass(graph, clust, 2, -1);
	}
}
