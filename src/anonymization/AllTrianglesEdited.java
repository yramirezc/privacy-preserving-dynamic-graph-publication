package anonymization;

import java.util.ArrayList;
import org.apache.commons.math3.util.Pair;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import link_prediction.LongDistanceScorer;
import link_prediction.PathBasedFriendLink;
import link_prediction.PathBasedKatz;
import link_prediction.WalkBasedFriendLink;
import link_prediction.WalkBasedKatz;

public abstract class AllTrianglesEdited extends BaseCycle {
	
	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd, int optChoice, int scorerChoice){
		int edges = graph.edgeSet().size();
		
		LongDistanceScorer linkScorer = initializeLinkScorer(graph, scorerChoice);
		
		int editionCount = 0;
		
		getRidOfEndVertices(graph, 1);
		
		System.out.println("It has been added "+(graph.edgeSet().size()-edges)+" edge(s) to get rid of end vertices");

		floyd = new FloydWarshallShortestPaths<>(graph);
				
		Transformation trans = findATransformation(floyd, graph, optChoice);
		while (trans != null){
			String v1 = trans.v1;
			
			int distViVj = (int)floyd.shortestDistance(trans.vj, trans.vi);
			
			// Initially put all triangles
			
			ArrayList<Pair<String, String>> edgesToAdd = new ArrayList<Pair<String, String>>();
			
			String vSecond = trans.vj;
			
			for (int edgeCnt = 0; edgeCnt < distViVj / 2 + 1; edgeCnt++){
				//we look for v_{Second-2}
				String vFirst = null;
				for (String tmpV : graph.vertexSet()){
					if (floyd.shortestDistance(tmpV, vSecond) == 2 && 
							floyd.shortestDistance(v1, tmpV) + 2 == floyd.shortestDistance(v1, vSecond)){
						vFirst = tmpV;
						break;
					}
				}
				//graph.addEdge(vFirst, vSecond);
				edgesToAdd.add(0, new Pair<String, String>(vFirst, vSecond));   // Here, the edge is not really added to the graph but to an auxiliary list
				vSecond = vFirst;
			}
			
			// Edition phase
			while (true) {
				double bestEdGain = 0;
				int bestEdFirstEdgeOrder = -1;
				Pair<String, String> bestEdSubstEdge = null;
				
				for (int i = 0; i < edgesToAdd.size() - 1; i++) {
					// Use the ShortSingleCycle heuristic to determine the substitution candidate
					String substFirst = edgesToAdd.get(i).getFirst();
					
					String substVj = (i < edgesToAdd.size() - 2)? edgesToAdd.get(i + 2).getFirst() : trans.vj;
					
					// We get the distance from the vertex following substFirst (the v_i here) to substVj
					int distFstVj = (int)floyd.shortestDistance(substFirst, substVj) - 1;
									
					int thirdDistFloor = distFstVj / 3;
					if (distFstVj % 3 == 2 && (substVj == trans.vm || i < edgesToAdd.size() - 2))   // Now, only the last substitution can rely on some vertex beyond the partial vj
						thirdDistFloor--;
					
					//we look for v_{substVj-thirdDistFloor} (or v_{substVj-thirdDistFloor+1} if j-i=3*thirdDistFloor+2 and j=m or i < addedEdges.size() - 2) 
					String substSecond = null;
					for (String tmpV : graph.vertexSet()){
						if (floyd.shortestDistance(tmpV, substVj) == thirdDistFloor && 
								floyd.shortestDistance(v1, tmpV) + thirdDistFloor == floyd.shortestDistance(v1, substVj)){
							substSecond = tmpV;
							break;
						}
					}
					
					// Determine gain from replacing i-th and (i+1)-th edge for a longer-reaching one
					double scoreFirst = linkScorer.score(edgesToAdd.get(i).getFirst(), edgesToAdd.get(i).getSecond());
					double scoreSecond = linkScorer.score(edgesToAdd.get(i + 1).getFirst(), edgesToAdd.get(i + 1).getSecond());
					double scoreSubst = linkScorer.score(substFirst, substSecond);
					//double gain = scoreSubst - (scoreFirst + scoreSecond) / 2.0;   // average
					double gain = scoreSubst - 2 * scoreFirst * scoreSecond / (double)(scoreFirst + scoreSecond);   // F1
					
					if (gain > bestEdGain) {
						bestEdGain = gain;
						bestEdFirstEdgeOrder = i;
						bestEdSubstEdge = new Pair<String, String>(substFirst, substSecond);
					}
				}
				
				if (bestEdGain > 0) {   // Some good substitution found
					edgesToAdd.remove(bestEdFirstEdgeOrder);   // removes first vertex of the selected pair
					edgesToAdd.remove(bestEdFirstEdgeOrder);   // removes second vertex of the selected pair, which has come to occupy this position after the first removal
					edgesToAdd.add(bestEdFirstEdgeOrder, bestEdSubstEdge);   // inserts substitute in the position from where the other two edges were removed
					editionCount++;
				}
				else
					break;
			}
			
			// Update graph after obtaining final edited set of edges that should be added
			for (Pair<String, String> edge : edgesToAdd)
				graph.addEdge(edge.getFirst(), edge.getSecond());
			
			floyd = new FloydWarshallShortestPaths<>(graph);
			trans = findATransformation(floyd, graph, optChoice);
		}
		System.out.println("Performed " + editionCount + " editions");
	}
	
	protected static LongDistanceScorer initializeLinkScorer(UndirectedGraph<String, DefaultEdge> graph, int scorerChoice) {
		LongDistanceScorer linkScorer = null;
		switch (scorerChoice) {
		case 0:
			linkScorer = new PathBasedFriendLink(graph, 20);   // Some tuning on the maxPathLength parameter may be necessary
			break;
		case 1:
			linkScorer = new WalkBasedFriendLink(graph, 20);   // Some tuning on the maxPathLength parameter may be necessary
			break;
		case 2:
			linkScorer = new PathBasedKatz(graph, 20, 0.9);   // Some tuning on the maxPathLength and beta parameters may be necessary
			break;
		default:   // case 3
			linkScorer = new WalkBasedKatz(graph, 20, 0.9);   // Some tuning on the maxPathLength and beta parameters may be necessary
			break;
		}
		return linkScorer;
	}

}
