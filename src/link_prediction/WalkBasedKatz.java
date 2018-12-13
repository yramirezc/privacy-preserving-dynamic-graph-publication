package link_prediction;

import java.util.ArrayList;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class WalkBasedKatz implements LongDistanceScorer {
	
	protected ArrayList<String> vertList;   // Instead of a set, we will have a list copy of the vertex set to have some traversal order
	protected OpenMapRealMatrix scores;
	
	public WalkBasedKatz(UndirectedGraph<String, DefaultEdge> graph, int maxPathLength, double beta){
		
		vertList = new ArrayList<String>(graph.vertexSet());
		//System.out.println("Vertex list");
		//System.out.println(vertList.toString());
		
		// Create adjacency matrix
		OpenMapRealMatrix adjMatrix = new OpenMapRealMatrix(graph.vertexSet().size(), graph.vertexSet().size());
		
		//System.out.println("Adjacency matrix at creation");
		//System.out.println(adjMatrix.toString());
		
		// Initialize adjacency matrix
		for (int i = 0; i < vertList.size() - 1; i++)
			for (int j = i + 1; j < vertList.size(); j++)
				if (graph.containsEdge(vertList.get(i), vertList.get(j))) {
					adjMatrix.setEntry(i, j, 1.0);
					adjMatrix .setEntry(j, i, 1.0);
				}
		
		//System.out.println("Adjacency matrix after initialization");
		//System.out.println(adjMatrix.toString());
		
		// Iterate updating Katz scores
		double scalarMultiplier = beta;
		OpenMapRealMatrix matrixMultiplier = adjMatrix;
		scores = (OpenMapRealMatrix)matrixMultiplier.scalarMultiply(scalarMultiplier);
		
		//System.out.println("Katz matrix at iteration for length 1");
		//System.out.println(scores.toString());
		
		for (int pLen = 2; pLen <= maxPathLength; pLen++) {
			scalarMultiplier *= beta;
			matrixMultiplier = (OpenMapRealMatrix)matrixMultiplier.multiply(adjMatrix);
			scores = (OpenMapRealMatrix)scores.add(matrixMultiplier.scalarMultiply(scalarMultiplier));
			
			//System.out.println("Adjacency matrix **" + pLen);
			//System.out.println(matrixMultiplier.toString());
			//System.out.println("Katz matrix at iteration for length " + pLen);
			//System.out.println(scores.toString());
		}
	}
	
	public double score(String v1, String v2) {
		return scores.getEntry(vertList.indexOf(v1), vertList.indexOf(v2));
	}

}
