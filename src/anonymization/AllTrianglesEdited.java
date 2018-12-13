package anonymization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.commons.math3.util.Pair;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import link_prediction.LongDistanceScorer;
import link_prediction.PathBasedFriendLink;
import link_prediction.PathBasedKatz;
import link_prediction.WalkBasedFriendLink;
import link_prediction.WalkBasedKatz;
import util.GraphUtil;

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

	public static void main(String[] args) {
		
		/*
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		graph.addVertex("v1");
		graph.addVertex("vi-1");
		graph.addVertex("vi");
		graph.addVertex("vi+1");
		graph.addVertex("vi+2");
		graph.addVertex("vj");
		graph.addVertex("vj+1");
		graph.addVertex("vj+2");
		graph.addVertex("vj+3");
		graph.addVertex("vj+4");
		graph.addVertex("vm");
		graph.addVertex("w1");
		graph.addVertex("w");
		graph.addVertex("u");
		graph.addVertex("u1");
		graph.addVertex("u2");
		
		graph.addVertex("x");
		
		graph.addEdge("v1", "vi-1");
		graph.addEdge("vi-1", "vi");
		graph.addEdge("vi", "vi+1");
		
		//graph.addEdge("vi+1", "vi+2");
		graph.addEdge("vi+1", "x");
		graph.addEdge("x", "vi+2");
		
		graph.addEdge("vi+2", "vj");
		graph.addEdge("vj", "vj+1");
		graph.addEdge("vj", "vj+3");
		graph.addEdge("vj+1", "vj+2");
		graph.addEdge("vj+3", "vj+4");
		graph.addEdge("vj+2", "vm");
		graph.addEdge("vj+4", "vm");
		graph.addEdge("vi", "w1");
		graph.addEdge("w1", "w");
		graph.addEdge("v1", "vi-1");
		graph.addEdge("v1", "u");
		graph.addEdge("u", "u1");
		graph.addEdge("u1", "u2");
		graph.addEdge("u2", "u");
		System.out.println(graph.toString());
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<String, DefaultEdge>(graph);
		anonymizeGraph(graph, floyd, 2, 2);   // Taking max ecc first to speedup getting to the debug phase I'm interested in
		System.out.println(graph.toString());
		
		/*
		SimpleGraph<String, DefaultEdge> graph1 = new SimpleGraph<>(DefaultEdge.class);
		
		graph1.addVertex("x");
		graph1.addVertex("u2");
		graph1.addVertex("u1");
		graph1.addVertex("u");
		graph1.addVertex("w");
		graph1.addVertex("w1");
		graph1.addVertex("vm");
		graph1.addVertex("vj+4");
		graph1.addVertex("vj+3");
		graph1.addVertex("vj+2");
		graph1.addVertex("vj+1");
		graph1.addVertex("vj");
		graph1.addVertex("vi+2");
		graph1.addVertex("vi+1");
		graph1.addVertex("vi");
		graph1.addVertex("vi-1");
		graph1.addVertex("v1");
		
		graph1.addEdge("v1", "vi-1");
		graph1.addEdge("vi-1", "vi");
		graph1.addEdge("vi", "vi+1");
		graph1.addEdge("vi+1", "x");
		graph1.addEdge("x", "vi+2");
		graph1.addEdge("vi+2", "vj");
		graph1.addEdge("vj", "vj+1");
		graph1.addEdge("vj", "vj+3");
		graph1.addEdge("vj+1", "vj+2");
		graph1.addEdge("vj+3", "vj+4");
		graph1.addEdge("vj+2", "vm");
		graph1.addEdge("vj+4", "vm");
		graph1.addEdge("vi", "w1");
		graph1.addEdge("w1", "w");
		graph1.addEdge("v1", "vi-1");
		graph1.addEdge("v1", "u");
		graph1.addEdge("u", "u1");
		graph1.addEdge("u1", "u2");
		graph1.addEdge("u2", "u");
		System.out.println(graph1.toString());
		FloydWarshallShortestPaths<String, DefaultEdge> floyd1 = new FloydWarshallShortestPaths<>(graph1);
		anonymizeGraph(graph1, floyd1);
		System.out.println(graph1.toString());*/
		
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("v1");
		labels.add("vi-1");
		labels.add("vi");
		labels.add("vi+1");
		labels.add("vi+2");
		labels.add("vj");
		labels.add("vj+1");
		labels.add("vj+2");
		labels.add("vj+3");
		labels.add("vj+4");
		labels.add("vm");
		labels.add("w1");
		labels.add("w");
		labels.add("u");
		labels.add("u1");
		labels.add("u2");
		labels.add("x");
		
		SecureRandom random = new SecureRandom();
		
		ArrayList<Integer> numbersAddedEdgesFirst = new ArrayList<Integer>();
		ArrayList<Integer> numbersAddedEdgesMinEcc = new ArrayList<Integer>();
		ArrayList<Integer> numbersAddedEdgesMaxEcc = new ArrayList<Integer>();
		
		for (int i = 0; i < 500; i++){
			
			Collections.shuffle(labels, random);
			
			SimpleGraph<String, DefaultEdge> graphShuffledVerts = new SimpleGraph<>(DefaultEdge.class);
			
			for (int index = 0; index < labels.size(); index++)
				graphShuffledVerts.addVertex(labels.get(index));
			
			graphShuffledVerts.addEdge("v1", "vi-1");
			graphShuffledVerts.addEdge("vi-1", "vi");
			graphShuffledVerts.addEdge("vi", "vi+1");
			graphShuffledVerts.addEdge("vi+1", "x");
			graphShuffledVerts.addEdge("x", "vi+2");
			graphShuffledVerts.addEdge("vi+2", "vj");
			graphShuffledVerts.addEdge("vj", "vj+1");
			graphShuffledVerts.addEdge("vj", "vj+3");
			graphShuffledVerts.addEdge("vj+1", "vj+2");
			graphShuffledVerts.addEdge("vj+3", "vj+4");
			graphShuffledVerts.addEdge("vj+2", "vm");
			graphShuffledVerts.addEdge("vj+4", "vm");
			graphShuffledVerts.addEdge("vi", "w1");
			graphShuffledVerts.addEdge("w1", "w");
			graphShuffledVerts.addEdge("v1", "vi-1");
			graphShuffledVerts.addEdge("v1", "u");
			graphShuffledVerts.addEdge("u", "u1");
			graphShuffledVerts.addEdge("u1", "u2");
			graphShuffledVerts.addEdge("u2", "u");
			
			int origEdgeCount = graphShuffledVerts.edgeSet().size();
			
			SimpleGraph<String, DefaultEdge> clone1GraphShuffledVerts = GraphUtil.cloneGraph(graphShuffledVerts);
			SimpleGraph<String, DefaultEdge> clone2GraphShuffledVerts = GraphUtil.cloneGraph(graphShuffledVerts);
						
			System.out.println("Original graph:");
			System.out.println(graphShuffledVerts.toString());
			
			FloydWarshallShortestPaths<String, DefaultEdge> floydShuffledVerts = new FloydWarshallShortestPaths<>(graphShuffledVerts);
			anonymizeGraph(graphShuffledVerts, floydShuffledVerts, 0, 2);
			numbersAddedEdgesFirst.add(graphShuffledVerts.edgeSet().size() - origEdgeCount);
			System.out.println("Graph anonymized taking first transformation found:");
			System.out.println(graphShuffledVerts.toString());
			
			floydShuffledVerts = new FloydWarshallShortestPaths<>(clone1GraphShuffledVerts);
			anonymizeGraph(clone1GraphShuffledVerts, floydShuffledVerts, 1, 2);
			numbersAddedEdgesMinEcc.add(clone1GraphShuffledVerts.edgeSet().size() - origEdgeCount);
			System.out.println("Graph anonymized prioritizing minimum eccentriciy values:");
			System.out.println(clone1GraphShuffledVerts.toString());
			
			floydShuffledVerts = new FloydWarshallShortestPaths<>(clone2GraphShuffledVerts);
			anonymizeGraph(clone2GraphShuffledVerts, floydShuffledVerts, 2, 2);
			numbersAddedEdgesMaxEcc.add(clone2GraphShuffledVerts.edgeSet().size() - origEdgeCount);
			System.out.println("Graph anonymized prioritizing maximum eccentriciy values:");
			System.out.println(clone2GraphShuffledVerts.toString());
			
			System.out.println();
		}
		System.out.println("Numbers of added edges taking first transformation found:        " + numbersAddedEdgesFirst.toString());
		System.out.println("Numbers of added edges prioritizing minimum eccentricity values: " + numbersAddedEdgesMinEcc.toString());
		System.out.println("Numbers of added edges prioritizing maximum eccentricity values: " + numbersAddedEdgesMaxEcc.toString());
	}

}
