package link_prediction;

import java.util.ArrayList;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class WalkBasedFriendLink implements LongDistanceScorer {
	
	protected ArrayList<String> vertList;   // Instead of a set, we will have a list copy of the vertex set to have some traversal order
	protected OpenMapRealMatrix scores;
	
	public WalkBasedFriendLink(UndirectedGraph<String, DefaultEdge> graph, int maxPathLength){
		
		vertList = new ArrayList<String>(graph.vertexSet());
		
		// Create adjacency matrix
		OpenMapRealMatrix adjMatrix = new OpenMapRealMatrix(graph.vertexSet().size(), graph.vertexSet().size());
		
		// Initialize adjacency matrix
		for (int i = 0; i < vertList.size() - 1; i++)
			for (int j = i + 1; j < vertList.size(); j++)
				if (graph.containsEdge(vertList.get(i), vertList.get(j))) {
					adjMatrix.setEntry(i, j, 1.0);
					adjMatrix .setEntry(j, i, 1.0);
				}
		
		// Iterate updating FriendLink scores
		OpenMapRealMatrix matrixMultiplier = adjMatrix;
		scores = new OpenMapRealMatrix(graph.vertexSet().size(), graph.vertexSet().size());
		
		double demotionFactor = 1.0;
		
		for (int pLen = 2; pLen <= maxPathLength; pLen++) {
			
			demotionFactor *= (vertList.size() - pLen);
			
			matrixMultiplier = (OpenMapRealMatrix)matrixMultiplier.multiply(adjMatrix);
			
			for (int i = 0; i < vertList.size() - 1; i++)
				for (int j = i + 1; j < vertList.size(); j++) {
					double addendum = matrixMultiplier.getEntry(i, j)/((pLen - 1) * demotionFactor);
					scores.addToEntry(i, j, addendum);
					scores.addToEntry(j, i, addendum);
				}
		}
	}
	
	public double score(String v1, String v2) {
		return scores.getEntry(vertList.indexOf(v1), vertList.indexOf(v2));
	}

}
