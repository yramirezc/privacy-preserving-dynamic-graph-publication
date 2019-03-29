package anonymization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

public abstract class BaseCycle {
	
	protected static void getRidOfEndVertices(UndirectedGraph<String, DefaultEdge> graph, int methodChoice) {
		
		switch (methodChoice) {
		case 0:   // Original method
			List<String> endVertices = new ArrayList<>();
			for (String v : graph.vertexSet()){
				if (graph.degreeOf(v) == 1)
					endVertices.add(v);
			}
			if (endVertices.isEmpty())
				return;
			else if (endVertices.size() == 1){
				String v1 = endVertices.get(0);
				for (String v2 : graph.vertexSet()){
					if (!v1.equals(v2) && !graph.containsEdge(v1, v2)){
						graph.addEdge(v1, v2);
						break;
					}
				}
			}
			else{
				for (int i = 0; i < endVertices.size()-1; i+= 2){
					graph.addEdge(endVertices.get(i), endVertices.get(i+1));
				}
				if (endVertices.size() % 2 == 1){
					String v1 = endVertices.get(endVertices.size()-1);
					for (String v2 : graph.vertexSet()){
						if (!v1.equals(v2) && !graph.containsEdge(v1, v2)){
							graph.addEdge(v1, v2);
							break;
						}
					}
				}
			}
			break;
		case 1:
			/* Ramirez - Aug 12, 2016
			 * Now, end vertices will be removed by adding edges from each one of them to all vertices at distance 2
			 * sharing the best hub-depressed link prediction score. This score prevents (or at least does not
			 * contribute to) highlighting hubs, which would then be easier to re-identify.
			 * Ramirez - Sep 8, 2016
			 * It has now been modified to add only one edge to a randomly chosen vertex among those sharing the best
			 * value of that score
			 * Ramirez - Oct 15, 2016
			 * And re-modified to add simply a randomly chosen vertex at distance 2
			 * */
			
			SecureRandom random = new SecureRandom();
			for (String v : graph.vertexSet()) {
				if (graph.degreeOf(v) == 1) {
					String parent = Graphs.neighborListOf(graph, v).get(0);
					String v1 = Graphs.neighborListOf(graph, parent).get(random.nextInt(Graphs.neighborListOf(graph, parent).size()));
					while (v1.equals(v))
						v1 = Graphs.neighborListOf(graph, parent).get(random.nextInt(Graphs.neighborListOf(graph, parent).size()));
					graph.addEdge(v, v1);
				}
			}
			break;
		default:
			break;
		}
	}
		
	public static void getRidOfExtremeDegreeVertices(UndirectedGraph<String, DefaultEdge> graph, int methodChoice) {
		
		switch (methodChoice) {
		case 0:   // Fewer edge modifications
			List<String> endVertices = new ArrayList<>();
			List<String> degreeNm2Vertices = new ArrayList<>();
			for (String v : graph.vertexSet()){
				if (graph.degreeOf(v) == 1)
					endVertices.add(v);
				else if (graph.degreeOf(v) == graph.vertexSet().size() - 2)
					degreeNm2Vertices.add(v);
			}

			// Increase by at least 1 the degree of end-vertices
			if (endVertices.size() == 1) {
				String v1 = endVertices.get(0);
				for (String v2 : graph.vertexSet()) {
					if (!v1.equals(v2) && !graph.containsEdge(v1, v2)) {
						graph.addEdge(v1, v2);
						break;
					}
				}
			}
			else if (endVertices.size() > 1) {
				for (int i = 0; i < endVertices.size()-1; i+= 2) {
					graph.addEdge(endVertices.get(i), endVertices.get(i+1));
				}
				if (endVertices.size() % 2 == 1) {
					String v1 = endVertices.get(endVertices.size()-1);
					for (String v2 : graph.vertexSet()) {
						if (!v1.equals(v2) && !graph.containsEdge(v1, v2)) {
							graph.addEdge(v1, v2);
							break;
						}
					}
				}
			}
			
			// Decrease by at least 1 the degree of vertices with degree n-2
			if (degreeNm2Vertices.size() == 1) {
				String v1 = degreeNm2Vertices.get(0);
				for (String v2 : graph.vertexSet()) {
					if (!v1.equals(v2) && graph.containsEdge(v1, v2)) {
						graph.removeEdge(v1, v2);
						break;
					}
				}
			}
			else if (degreeNm2Vertices.size() > 1) {
				for (int i = 0; i < degreeNm2Vertices.size()-1; i+= 2) {
					graph.removeEdge(degreeNm2Vertices.get(i), degreeNm2Vertices.get(i+1));
				}
				if (degreeNm2Vertices.size() % 2 == 1) {
					String v1 = degreeNm2Vertices.get(degreeNm2Vertices.size()-1);
					for (String v2 : graph.vertexSet()) {
						if (!v1.equals(v2) && graph.containsEdge(v1, v2)) {
							graph.removeEdge(v1, v2);
							break;
						}
					}
				}
			}
			break;
		case 1:   // Several best-scored edges added, (maybe) several edges removed, not necessarily worst-scored
			
			SecureRandom random = new SecureRandom();
			
			boolean mustProcessEndVertices = true;   // At this point, we don't know if the graph has end-vertices, but we must at least do the first search
			
			while (mustProcessEndVertices) {
				
				for (String v : graph.vertexSet()) {
					if (graph.degreeOf(v) == 1) {
						String parent = Graphs.neighborListOf(graph, v).get(0);
						List<String> desirableCandidates = new ArrayList<>();
						// First try to link to a brother of degree different from n-2
						for (String v1 : Graphs.neighborListOf(graph, parent)) 
							if (!v1.equals(v) && graph.degreeOf(v1) != graph.vertexSet().size() - 2)
								desirableCandidates.add(v1);
						if (desirableCandidates.size() != 0) {
							String v1 = desirableCandidates.get(random.nextInt(desirableCandidates.size()));
							graph.addEdge(v, v1);
						}
						else {   // If there are no brothers of different from n-2, try to link to another arbitrary vertex of degree different from n-2
							List<String> allCandidates = new ArrayList<>();
							for (String v1 : graph.vertexSet()) 
								if (!v1.equals(v)) {
									if (graph.degreeOf(v1) != graph.vertexSet().size() - 2 && !graph.containsEdge(v, v1))
										desirableCandidates.add(v1);
									if (!graph.containsEdge(v, v1))
										allCandidates.add(v1);
								}
							if (desirableCandidates.size() != 0) {
								String v1 = desirableCandidates.get(random.nextInt(desirableCandidates.size()));
								graph.addEdge(v, v1);
							}
							else {   // If cannot link to an arbitrary vertex of degree at most n-3, link to any vertex (will cause it to have degree n-1) 
								String v1 = allCandidates.get(random.nextInt(allCandidates.size()));
								graph.addEdge(v, v1);
							}
						}
					}
				}
				
				// At this point, all vertices have degree 0 or >= 2, as end-vertices have been processed
				mustProcessEndVertices = false;
				
				for (String v : graph.vertexSet()) {
					if (graph.degreeOf(v) == graph.vertexSet().size() - 2) {
						List<String> desirableCandidates = new ArrayList<>();
						// First try to de-link from a child of degree at least 3
						for (String v1 : Graphs.neighborListOf(graph, v)) 
							if (graph.degreeOf(v1) > 2)
								desirableCandidates.add(v1);
						if (desirableCandidates.size() != 0) {
							String v1 = desirableCandidates.get(random.nextInt(desirableCandidates.size()));
							graph.removeEdge(v, v1);
						}
						else {   // If all children have degree 2 then link to the sole non-child 
							for (String v1 : graph.vertexSet()) {
								if (!v1.equals(v) && !graph.containsEdge(v, v1)) {
									graph.addEdge(v, v1);
									if (graph.degreeOf(v1) == 1)
										mustProcessEndVertices = true;
									break;
								}
									
							}
						}
					}
				}
				
			}
			break;
		default:
			break;
		}
	}
	
	static class Transformation{
		public String v1, vi, vj, vm;
		public TreeMap<Double, String> resolvables;

		public Transformation(String v1, String vm, TreeMap<Double, String> res) {
			super();
			this.v1 = v1;
			this.vm = vm;
			this.resolvables = res;
			this.vi = this.resolvables.firstEntry().getValue();   //the first resolvable is the first of the treemap "resolvables"
			this.vj = this.resolvables.lastEntry().getValue();   //the last resolvable is the last of the treemap "resolvables"
		}
		
	}
	
	protected static Transformation findATransformation(FloydWarshallShortestPaths<String, DefaultEdge> floyd,
			UndirectedGraph<String, DefaultEdge> graph, int optChoice) {
		double distance;
		/* Ramirez - Aug 12, 2016
		 * Allow to choose between returning the first transformation found and return that of minimum/maximum eccentricity
		 * optChoice == 0: return first transformation found
		 * optChoice == 1: return minimum eccentricity transformation
		 * optChoice == 2: return maximum eccentricity transformation
		 * Ramirez - Sep 8, 2016
		 * optChoice == 3: find all transformations and then randomly return an arbitrary one
		 * Ramirez - Oct 24, 2016
		 * optChoice == 4: return transformation leading to the creation of the smallest cycle
		 * optChoice == 5: return transformation leading to the creation of the largest cycle
		 * */
		double optimumComparisonValue = (optChoice == 1 || optChoice == 4)? Double.MAX_VALUE : -1d;
		ArrayList<Transformation> eligibleTransformations = new ArrayList<>();
		for (String v1 : graph.vertexSet()) {
			TreeMap<Double, LinkedList<String>> distances = new TreeMap<>();
			for (String v2 : graph.vertexSet()) {
				if (v1.equals(v2))
					continue;
				distance = floyd.shortestDistance(v1, v2);
				if (!distances.containsKey(distance)) {
					distances.put(distance, new LinkedList<String>());
				}
				distances.get(distance).add(v2);
			}
			TreeMap<Double, String> resolvables = new TreeMap<>();
			for (LinkedList<String> anonymitySet : distances.values()) {
				if (anonymitySet.size() == 1) {
					String v2 = anonymitySet.getFirst();
					resolvables.put(floyd.shortestDistance(v1, v2), v2);
				}
			}
			if (!resolvables.isEmpty()) {  //in this case v1 is a 1-antiresolving set
				//the eccentricity path can be found in the "distances" treeMap
				String vm = distances.lastEntry().getValue().getFirst();
				if (optChoice == 0)
					return new Transformation(v1, vm, resolvables);
				else if (optChoice == 3)
					eligibleTransformations.add(new Transformation(v1, vm, resolvables));
				else {
					double compVal = 0d;
					switch (optChoice) {
					case 1:
					case 2:
						compVal = distances.lastEntry().getKey();
						break;
					case 4:
						compVal = (double)orderCycleToAdd(floyd, v1, resolvables.firstEntry().getValue(), resolvables.lastEntry().getValue(), vm, 1);
						break;
					case 5:
						compVal = (double)orderCycleToAdd(floyd, v1, resolvables.firstEntry().getValue(), resolvables.lastEntry().getValue(), vm, 2);
						break;
					default:;
					}
					if ((compVal < optimumComparisonValue && (optChoice == 1 || optChoice == 4)) ||
						(compVal > optimumComparisonValue && (optChoice == 2 || optChoice == 5))) {
						optimumComparisonValue = compVal;
						eligibleTransformations.clear();
						eligibleTransformations.add(new Transformation(v1, vm, resolvables));
					}
					else if (compVal == optimumComparisonValue)
						eligibleTransformations.add(new Transformation(v1, vm, resolvables));
				}
			}
		}
		if (eligibleTransformations.size() == 0)
			return null;
		else {
			SecureRandom random = new SecureRandom();
			return eligibleTransformations.get(random.nextInt(eligibleTransformations.size()));
		}
	}
	
	protected static Set<String> findOriginal1Antires(FloydWarshallShortestPaths<String, DefaultEdge> floyd,
			UndirectedGraph<String, DefaultEdge> graph) {
		double distance;
		TreeSet<String> originalAntires = new TreeSet<>();
		for (String v1 : graph.vertexSet()) {
			TreeMap<Double, LinkedList<String>> distances = new TreeMap<>();
			for (String v2 : graph.vertexSet()) {
				if (v1.equals(v2))
					continue;
				distance = floyd.shortestDistance(v1, v2);
				if (!distances.containsKey(distance)) {
					distances.put(distance, new LinkedList<String>());
				}
				distances.get(distance).add(v2);
			}
			TreeMap<Double, String> resolvables = new TreeMap<>();
			for (LinkedList<String> anonymitySet : distances.values()) {
				if (anonymitySet.size() == 1) {
					String v2 = anonymitySet.getFirst();
					resolvables.put(floyd.shortestDistance(v1, v2), v2);
				}
			}
			if (!resolvables.isEmpty()) {  //in this case v1 is a 1-antiresolving set
				//the eccentricity path can be found in the "distances" treeMap
				originalAntires.add(v1);
			}
		}
		
		return originalAntires;
	}
	
	protected static Transformation findATransformation(FloydWarshallShortestPaths<String, DefaultEdge> floyd,
			UndirectedGraph<String, DefaultEdge> graph, Set<String> originalAntires) {
		double distance;
		ArrayList<Transformation> eligibleTransformations = new ArrayList<>();
		for (String v1 : originalAntires) {
			TreeMap<Double, LinkedList<String>> distances = new TreeMap<>();
			for (String v2 : graph.vertexSet()) {
				if (v1.equals(v2))
					continue;
				distance = floyd.shortestDistance(v1, v2);
				if (!distances.containsKey(distance)) {
					distances.put(distance, new LinkedList<String>());
				}
				distances.get(distance).add(v2);
			}
			TreeMap<Double, String> resolvables = new TreeMap<>();
			for (LinkedList<String> anonymitySet : distances.values()) {
				if (anonymitySet.size() == 1) {
					String v2 = anonymitySet.getFirst();
					resolvables.put(floyd.shortestDistance(v1, v2), v2);
				}
			}
			if (!resolvables.isEmpty()) {  //in this case v1 is a 1-antiresolving set
				//the eccentricity path can be found in the "distances" treeMap
				String vm = distances.lastEntry().getValue().getFirst();
				eligibleTransformations.add(new Transformation(v1, vm, resolvables));
			}
		}
		if (eligibleTransformations.size() == 0)
			return null;
		else {
			SecureRandom random = new SecureRandom();
			return eligibleTransformations.get(random.nextInt(eligibleTransformations.size()));
		}
	}
		
	public static int orderCycleToAdd(FloydWarshallShortestPaths<String, DefaultEdge> floyd, String v1, String vi, String vj, String vm, int optChoice) {
		/* optChoice == 0: Original odd-order cycle
		 * optChoice == 1: Smallest-order cycle
		 * optChoice == 2: Largest-order cycle
		 * */
		int ecc = (int)floyd.shortestDistance(v1, vm);
		int distVjVi = (int)floyd.shortestDistance(vj, vi);
		switch (optChoice) {
		case 0:
			if (distVjVi % 2 == 1)
				return distVjVi + 2;
			else
				return distVjVi + 3;
		case 1:
			if (distVjVi == 0 || (distVjVi == 2 && vj.equals(vm)))
				return 3;
			else {
				int thirdDistFloor = distVjVi / 3;
				if (distVjVi % 3 == 2 && vj.equals(vm))
					thirdDistFloor--;
				return distVjVi + 2 - thirdDistFloor;
			}
		case 2:
			if (ecc % 2 == 0)
				return ecc + 1;
			else
				return ecc;
		default:
			return ecc + 1;
		}
	}
}
