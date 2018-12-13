package anonymization;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import clustering.ClusterSimilarityCalculator;
import util.GraphUtil;

public class ClusterEqualizer {
	
	public static void enforceTwinEqClass(UndirectedGraph<String, DefaultEdge> graph, Set<String> cluster, int equalizationStrategy, int twinType) {
		
		if (cluster.size() >= 2) {   // Nothing to do if cluster.size() <= 1
			
			switch (equalizationStrategy) {
			case 0:   // Find a pseudo-centroid according to averaged contTableVertSimilarity and make all other vertices twins to it
				
				// Find pseudo-centroid according to averaged contTableVertSimilarity
				double maxAvgSimilarity = 0d;
				String modelVertex = null;
				for (String v1 : cluster) {
					double avgSimilarity = 0d;
					for (String v2 : cluster) 
						if (!v1.equals(v2)) 
							avgSimilarity += ClusterSimilarityCalculator.contTableVertSimilarity(graph, v1, v2);
					avgSimilarity /= (double)(cluster.size() - 1);
					if (avgSimilarity > maxAvgSimilarity) {
						maxAvgSimilarity = avgSimilarity;
						modelVertex = v1;
					}
				}
				
				// Turning remaining vertices into twins of modelVertex
				// Equalize open neighborhoods
				for (String twinCand : cluster)
					if (!twinCand.equals(modelVertex))
						for (String v : graph.vertexSet()) {
							if (!cluster.contains(v))
								if (graph.containsEdge(modelVertex, v) && !graph.containsEdge(twinCand, v))
									graph.addEdge(twinCand, v);
								else if (!graph.containsEdge(modelVertex, v) && graph.containsEdge(twinCand, v))
									graph.removeEdge(twinCand, v);	
						}
				
				
				break;   // end of case 0
			
			case 1:   // Majority voting scheme. For every vertex out of the cluster, members vote whether they should all be connected to it or disconnected from it
				
				SecureRandom random = new SecureRandom();
				for (String v1 : graph.vertexSet())
					if (!cluster.contains(v1)) {
						int neighborsInCluster = 0;
						for (String v2 : cluster)
							if (graph.containsEdge(v1, v2))
								neighborsInCluster++;
						// Ties are broken by a "coin toss"
						if (neighborsInCluster > cluster.size() - neighborsInCluster 
							|| (neighborsInCluster == cluster.size() - neighborsInCluster && random.nextBoolean())) {
							// All members of cluster will be neighbors of v1
							for (String v2 : cluster)
								if (!graph.containsEdge(v1, v2))
									graph.addEdge(v1, v2);
						}
						else {
							// No member of cluster will be neighbor of v1
							for (String v2 : cluster)
								if (graph.containsEdge(v1, v2))
									graph.removeEdge(v1, v2);
						}
					}
				break;   // end of case 1
			
			case 2:   // The neighborhoods of all members of the cluster are set as equal to their union
				
				Set<String> allNeighbors = new HashSet<>();
				for (String v : cluster)
					allNeighbors.addAll(Graphs.neighborListOf(graph, v));
				allNeighbors.removeAll(cluster);
				for (String v1 : cluster)
					for (String v2 : allNeighbors)
						if (!graph.containsEdge(v1, v2))
							graph.addEdge(v1, v2);
				break;   // end of case 2
			default:;
			}
			
			
			/* Determine whether a false (true) twin eq. class will be created and remove (add) necessary edges
			 * twinType == 0: choose the type that involves modifying the graph as less as possible
			 * twinType > 0 (usually 1): create a true twin eq. class
			 * twinType < 0 (usually -1): create a false twin eq. class
			 */
			
			if (twinType < 0 || (twinType == 0 && GraphUtil.computeDensity(GraphUtil.inducedSubgraph(graph, cluster)) < 0.5d)) {  // Less changes are necessary to create a false twin eq. class
				for (String v1 : cluster)
					for (String v2 : cluster)
						if (!v1.equals(v2))
							if (graph.containsEdge(v1, v2))
								graph.removeEdge(v1, v2);
			}
			else {   // Less changes are necessary to create a true twin eq. class
				for (String v1 : cluster)
					for (String v2 : cluster)
						if (!v1.equals(v2))
							if (!graph.containsEdge(v1, v2))
								graph.addEdge(v1, v2);
			}
			
		}  
	}
	
}
