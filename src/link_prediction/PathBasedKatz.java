package link_prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import util.GraphUtil;

public class PathBasedKatz implements LongDistanceScorer {
	
	protected ArrayList<String> vertList;   // Instead of a set, we will have a list copy of the vertex set to have some traversal order
	//protected OpenMapRealMatrix scores;
	protected HashMap<String, HashMap<String, Double>> scores;
	
	public PathBasedKatz(UndirectedGraph<String, DefaultEdge> graph, int maxPathLength, double beta) {
		
		vertList = new ArrayList<String>(graph.vertexSet());
		//System.out.println("Vertex list");
		//System.out.println(vertList.toString());
		
		// Count the numbers of paths between every pair of vertices
		HashMap<String, HashMap<String, HashMap<Integer, Integer>>> pathCounts = new HashMap<>();
		DefaultDirectedGraph<String, DefaultEdge> digraph = GraphUtil.createDirectedClone(graph);   // This digraph clone is necessary because no algorithm like AllDirectedPaths is available for undirected graph objects
		AllDirectedPaths<String, DefaultEdge> pathCounter = new AllDirectedPaths<>(digraph);
		
		for (int i = 0; i < vertList.size() - 1; i++) {
			HashMap<String, HashMap<Integer, Integer>> row = new HashMap<>();
			pathCounts.put(vertList.get(i), row);
			for (int j = i + 1; j < vertList.size(); j++) {
				HashMap<Integer, Integer> entry = new HashMap<>();
				pathCounts.get(vertList.get(i)).put(vertList.get(j), entry);
				List<GraphPath<String, DefaultEdge>> allPaths = pathCounter.getAllPaths(vertList.get(i), vertList.get(j), true, maxPathLength);
				// Apparently, getAllPaths does not consider edges as length-1 paths.
				// However, since we will be computing scores for pairs of non-adjacent vertices, the contribution of those paths would be the same for all analyzed pairs 
				for (GraphPath<String, DefaultEdge> path : allPaths) {
					int length = path.getEdgeList().size();
					if (pathCounts.get(vertList.get(i)).get(vertList.get(j)).containsKey(length))
						pathCounts.get(vertList.get(i)).get(vertList.get(j)).put(length, pathCounts.get(vertList.get(i)).get(vertList.get(j)).get(length) + 1);
					else
						pathCounts.get(vertList.get(i)).get(vertList.get(j)).put(length, 1);
				}
			}
		}
		
		// Compute Katz scores
		scores = new HashMap<>();
		
		for (int i = 0; i < vertList.size() - 1; i++){
			HashMap<String, Double> row = new HashMap<>();
			for (int j = i + 1; j < vertList.size(); j++) {
				double score = 0.0, scalarMultiplier = beta;
				for (int pLen = 2; pLen <= maxPathLength; pLen++) {
					scalarMultiplier *= beta;
					if (pathCounts.get(vertList.get(i)).get(vertList.get(j)).containsKey(pLen))
						score += scalarMultiplier * pathCounts.get(vertList.get(i)).get(vertList.get(j)).get(pLen).doubleValue();
				}
				row.put(vertList.get(j), score);
			}
			scores.put(vertList.get(i), row);
		}
		
		//System.out.println("Katz scores");
		//System.out.println(scores.toString());
	}
	
	public double score(String v1, String v2) {
		int ind1 = vertList.indexOf(v1), ind2 = vertList.indexOf(v2);
		if (ind1 < ind2)
			return scores.get(vertList.get(ind1)).get(vertList.get(ind2)).doubleValue();
		else if (ind1 > ind2)
			return scores.get(vertList.get(ind2)).get(vertList.get(ind1)).doubleValue();
		else
			return 0.0;
	}

}
