package anonymization;

import java.util.LinkedList;
import java.util.TreeMap;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

public class FeasibilityTester {
	
	public static void doCheckOnTransformations(UndirectedGraph<String, DefaultEdge> graph,
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		double distance;
		double maxEccentricity = -1, maxI = -1, maxJ = -1, maxJminusI = -1;
		for (String v1 : graph.vertexSet()){
			TreeMap<Double, LinkedList<String>> distances = new TreeMap<>();
			for (String v2 : graph.vertexSet()){
				if (v1.equals(v2)) continue;
				distance = floyd.shortestDistance(v1, v2);
				if (!distances.containsKey(distance)){
					distances.put(distance, new LinkedList<String>());
				}
				distances.get(distance).add(v2);
			}
			TreeMap<Double, String> resolvables = new TreeMap<>();
			for (LinkedList<String> anonymitySet : distances.values()){
				if (anonymitySet.size() == 1) {
					String v2 = anonymitySet.getFirst();
					resolvables.put(floyd.shortestDistance(v1, v2), v2);
				}
			}
			if (!resolvables.isEmpty()) {   //in this case v1 is a 1-antiresolving set
				
				System.out.println("Transformation:");
				String vm = distances.lastEntry().getValue().getFirst();
				double eccentricity = floyd.shortestDistance(v1, vm);
				System.out.println("Eccentricity: " + eccentricity);
				if (eccentricity > maxEccentricity)
					maxEccentricity = eccentricity;
				double orderI = floyd.shortestDistance(v1, resolvables.firstEntry().getValue()) + 1;
				System.out.println("Order of i: " + orderI);
				if (orderI > maxI)
					maxI = orderI;
				double orderJ = floyd.shortestDistance(v1, resolvables.lastEntry().getValue()) + 1;
				System.out.println("Order of j: " + orderJ);
				if (orderJ > maxJ)
					maxJ = orderJ;
				double distJI = orderJ - orderI;
				System.out.println("j-i: " + distJI);
				if (distJI > maxJminusI)
					maxJminusI = distJI;
				System.out.println("Orders of 1-resolvables: " + resolvables.keySet().toString());				
				System.out.println("=========================================");
				}
		}
		System.out.println("Max eccentricity: " + maxEccentricity);
		System.out.println("Max i: " + maxI);
		System.out.println("Max j: " + maxJ);
		System.out.println("Max j-i: " + maxJminusI);
	}
	
	public static int maxJminusI(UndirectedGraph<String, DefaultEdge> graph,
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		double maxJminI = -1;
		for (String v1 : graph.vertexSet()){
			TreeMap<Double, LinkedList<String>> distances = new TreeMap<>();
			for (String v2 : graph.vertexSet()){
				if (v1.equals(v2)) continue;
				double distance = floyd.shortestDistance(v1, v2);
				if (!distances.containsKey(distance)){
					distances.put(distance, new LinkedList<String>());
				}
				distances.get(distance).add(v2);
			}
			TreeMap<Double, String> resolvables = new TreeMap<>();
			for (LinkedList<String> anonymitySet : distances.values()){
				if (anonymitySet.size() == 1) {
					String v2 = anonymitySet.getFirst();
					resolvables.put(floyd.shortestDistance(v1, v2), v2);
				}
			}
			if (!resolvables.isEmpty()) {   //in this case v1 is a 1-antiresolving set
				double distJI = floyd.shortestDistance(v1, resolvables.lastEntry().getValue()) - floyd.shortestDistance(v1, resolvables.firstEntry().getValue());
				if (distJI > maxJminI)
					maxJminI = distJI;
				}
		}
		return (int)maxJminI;
	}
}
