package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import util.DegreeDistributionComputer;
import util.DistributionComputer;
import util.GraphUtil;

public class GraphParameterBasedUtilitiesJGraphT {

	public static int countEdgeAdditions(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		int addedEdges = 0;
		List<String> vertListPert = new ArrayList<>(perturbedGraph.vertexSet());
		for (int i = 0; i < vertListPert.size() - 1; i++)
			for (int j = i + 1; j < vertListPert.size(); j++)
				if (perturbedGraph.containsEdge(vertListPert.get(i), vertListPert.get(j)) && !originalGraph.containsEdge(vertListPert.get(i), vertListPert.get(j)))
					addedEdges++;
		return addedEdges;
	}
	
	public static int countEdgeRemovals(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		int removedEdges = 0;
		List<String> vertListOrig = new ArrayList<>(originalGraph.vertexSet());
		for (int i = 0; i < vertListOrig.size() - 1; i++)
			for (int j = i + 1; j < vertListOrig.size(); j++)
				if (originalGraph.containsEdge(vertListOrig.get(i), vertListOrig.get(j)) && !perturbedGraph.containsEdge(vertListOrig.get(i), vertListOrig.get(j)))
					removedEdges++;
		return removedEdges;
	}
	
	public static int countEdgeFlips(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		return countEdgeAdditions(originalGraph, perturbedGraph) + countEdgeRemovals(originalGraph, perturbedGraph);
	}
	
	public static int deltaDiameter(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(originalGraph);
		FloydWarshallShortestPaths<String, DefaultEdge> floydPerturbed = new FloydWarshallShortestPaths<>(perturbedGraph);
		return deltaDiameter(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
	}
	
	public static int deltaDiameter(UndirectedGraph<String, DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal, UndirectedGraph<String, DefaultEdge> perturbedGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydPerturbed) {
		return GraphUtil.computeDiameter(perturbedGraph, floydPerturbed) - GraphUtil.computeDiameter(originalGraph, floydOriginal);
	}
	
	public static int deltaEffectiveDiameter(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(originalGraph);
		FloydWarshallShortestPaths<String, DefaultEdge> floydPerturbed = new FloydWarshallShortestPaths<>(perturbedGraph);
		return deltaEffectiveDiameter(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
	}
	
	public static int deltaEffectiveDiameter(UndirectedGraph<String, DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal, UndirectedGraph<String, DefaultEdge> perturbedGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydPerturbed) {
		return effectiveDiameter(perturbedGraph, floydPerturbed) - effectiveDiameter(originalGraph, floydOriginal);
	}
	
	public static int effectiveDiameter(UndirectedGraph<String, DefaultEdge> graph, FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		ArrayList<Integer> valueList = new ArrayList<>();
		for (String v1 : graph.vertexSet()) {
			for (String v2 : graph.vertexSet())
				if (v1 != v2) {
					int length = (int)floyd.shortestDistance(v1, v2);
					valueList.add(length);
				}
		}
		Collections.sort(valueList);
		return valueList.get((valueList.size() * 9) / 10);
	}
	
	public static int deltaRadius(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(originalGraph);
		FloydWarshallShortestPaths<String, DefaultEdge> floydPerturbed = new FloydWarshallShortestPaths<>(perturbedGraph);
		return deltaRadius(originalGraph, floydOriginal, perturbedGraph, floydPerturbed);
	}
	
	public static int deltaRadius(UndirectedGraph<String, DefaultEdge> originalGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal, UndirectedGraph<String, DefaultEdge> perturbedGraph, FloydWarshallShortestPaths<String, DefaultEdge> floydPerturbed) {
		return GraphUtil.computeRadius(perturbedGraph, floydPerturbed) - GraphUtil.computeRadius(originalGraph, floydOriginal);
	}
	
	public static double deltaGlobalClusteringCoefficient(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		return globalClusteringCoefficient(perturbedGraph) - globalClusteringCoefficient(originalGraph);
	}
	
	public static double globalClusteringCoefficient(UndirectedGraph<String, DefaultEdge> graph) {
		
		int connTriplesCount = 0, closedTriplesCount = 0;
		
		ArrayList<String> vertexList = new ArrayList<>(graph.vertexSet()); 
		for (int i = 0; i < vertexList.size() - 1; i++)
			for (int j = i + 1; j < vertexList.size(); j++) {
				Set<String> commonNeighbors = new HashSet<>(Graphs.neighborListOf(graph, vertexList.get(i)));
				commonNeighbors.retainAll(Graphs.neighborListOf(graph, vertexList.get(j)));
				commonNeighbors.remove(vertexList.get(i));
				commonNeighbors.remove(vertexList.get(j));
				if (graph.containsEdge(vertexList.get(i), vertexList.get(j))) {
					connTriplesCount += 3 * commonNeighbors.size();
					closedTriplesCount += 3 * commonNeighbors.size();
				}
				else
					connTriplesCount += commonNeighbors.size();			
			}
		
		return (double) closedTriplesCount / (double) connTriplesCount;
	}
	
	public static double deltaGlobalClusteringCoefficientAfterKMatchCopyOperation(UndirectedGraph<String, DefaultEdge> originalGraph, Map<String, List<String>> vat) {
		return globalClusteringCoefficientAfterKMatchCopyOperation(originalGraph, vat) - globalClusteringCoefficient(originalGraph);
	}
	
	public static double globalClusteringCoefficientAfterKMatchCopyOperation(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> vat) {
		
		int connTriplesCount = 0, closedTriplesCount = 0;
		
		// Compute the would-be neighbor lists of the perturbed graph 
		
		Map<String, Set<String>> neighborListsInPerturbedGraph = new TreeMap<>();
		for (String v : graph.vertexSet())
			neighborListsInPerturbedGraph.put(v, new TreeSet<String>());
		
		int columnCount = vat.get(vat.keySet().iterator().next()).size();
		ArrayList<String> vertexList = new ArrayList<>(graph.vertexSet());
		List<String> keyList = new ArrayList<>(vat.keySet());
		
		for (int i = 0; i < keyList.size() - 1; i++)
			for (int j = 0; j < vat.get(keyList.get(i)).size(); j++)
				for (int r = i + 1; r < keyList.size(); r++)
					for (int s = 0; s < vat.get(keyList.get(r)).size(); s++) 
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(vat.get(keyList.get(i)).get((j + k) % columnCount), vat.get(keyList.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(vat.get(keyList.get(i)).get(j)).add(vat.get(keyList.get(r)).get(s));
								neighborListsInPerturbedGraph.get(vat.get(keyList.get(r)).get(s)).add(vat.get(keyList.get(i)).get(j));
								break;
						}
					}
		
		// Compute connTriplesCount and closedTriplesCount according to neighborListsInPerturbedGraph
		
		for (int i = 0; i < vertexList.size() - 1; i++)
			for (int j = i + 1; j < vertexList.size(); j++) {
				Set<String> commonNeighborsInPerturbedGraph = new HashSet<>(neighborListsInPerturbedGraph.get(vertexList.get(i)));
				commonNeighborsInPerturbedGraph.retainAll(neighborListsInPerturbedGraph.get(vertexList.get(j)));
				commonNeighborsInPerturbedGraph.remove(vertexList.get(i));
				commonNeighborsInPerturbedGraph.remove(vertexList.get(j));
				if (graph.containsEdge(vertexList.get(i), vertexList.get(j))) {
					connTriplesCount += 3 * commonNeighborsInPerturbedGraph.size();
					closedTriplesCount += 3 * commonNeighborsInPerturbedGraph.size();
				}
				else
					connTriplesCount += commonNeighborsInPerturbedGraph.size();			
			}
		
		// Return global clustering coefficient
		
		return (double) closedTriplesCount / (double) connTriplesCount;
	}
	
	public static double deltaGlobalClusteringCoefficientAfterKMatchCopyOperation(UndirectedGraph<String, DefaultEdge> originalGraph, Map<String, List<String>> currentVAT, Map<String, List<String>> newVATRows) {
		return globalClusteringCoefficientAfterKMatchCopyOperation(originalGraph, currentVAT, newVATRows) - globalClusteringCoefficient(originalGraph);
	}
	
	public static double globalClusteringCoefficientAfterKMatchCopyOperation(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> currentVAT, Map<String, List<String>> newVATRows) {
		
		int connTriplesCount = 0, closedTriplesCount = 0;
		
		// Compute the would-be neighbor lists of the perturbed graph 
		
		Map<String, Set<String>> neighborListsInPerturbedGraph = new TreeMap<>();
		for (String v : graph.vertexSet())
			neighborListsInPerturbedGraph.put(v, new TreeSet<String>());
		
		int columnCount = currentVAT.get(currentVAT.keySet().iterator().next()).size();
		ArrayList<String> vertexList = new ArrayList<>(graph.vertexSet());
		List<String> keyListCurrentVAT = new ArrayList<>(currentVAT.keySet());
		List<String> keyListNewVATRows = new ArrayList<>(newVATRows.keySet());
		
		for (int i = 0; i < keyListCurrentVAT.size() - 1; i++)
			for (int j = 0; j < currentVAT.get(keyListCurrentVAT.get(i)).size(); j++) {
				
				for (int r = i + 1; r < keyListCurrentVAT.size(); r++)
					for (int s = 0; s < currentVAT.get(keyListCurrentVAT.get(r)).size(); s++) 
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(currentVAT.get(keyListCurrentVAT.get(i)).get((j + k) % columnCount), currentVAT.get(keyListCurrentVAT.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(i)).get(j)).add(currentVAT.get(keyListCurrentVAT.get(r)).get(s));
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(r)).get(s)).add(currentVAT.get(keyListCurrentVAT.get(i)).get(j));
								break;
						}
					}
				
				for (int r = 0; r < keyListNewVATRows.size(); r++)
					for (int s = 0; s < newVATRows.get(keyListNewVATRows.get(r)).size(); s++)
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(currentVAT.get(keyListCurrentVAT.get(i)).get((j + k) % columnCount), newVATRows.get(keyListNewVATRows.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(i)).get(j)).add(newVATRows.get(keyListNewVATRows.get(r)).get(s));
								neighborListsInPerturbedGraph.get(newVATRows.get(keyListNewVATRows.get(r)).get(s)).add(currentVAT.get(keyListCurrentVAT.get(i)).get(j));
								break;
							}
						}
			}
		
		// Compute connTriplesCount and closedTriplesCount according to neighborListsInPerturbedGraph
		
		for (int i = 0; i < vertexList.size() - 1; i++)
			for (int j = i + 1; j < vertexList.size(); j++) {
				Set<String> commonNeighborsInPerturbedGraph = new HashSet<>(neighborListsInPerturbedGraph.get(vertexList.get(i)));
				commonNeighborsInPerturbedGraph.retainAll(neighborListsInPerturbedGraph.get(vertexList.get(j)));
				commonNeighborsInPerturbedGraph.remove(vertexList.get(i));
				commonNeighborsInPerturbedGraph.remove(vertexList.get(j));
				if (graph.containsEdge(vertexList.get(i), vertexList.get(j))) {
					connTriplesCount += 3 * commonNeighborsInPerturbedGraph.size();
					closedTriplesCount += 3 * commonNeighborsInPerturbedGraph.size();
				}
				else
					connTriplesCount += commonNeighborsInPerturbedGraph.size();			
			}
		
		// Return global clustering coefficient
		
		return (double) closedTriplesCount / (double) connTriplesCount;
	}
	
	public static double deltaAvgLocalClusteringCoefficient(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		return avgLocalClusteringCoefficient(perturbedGraph) - avgLocalClusteringCoefficient(originalGraph);
	}
	
	public static double avgLocalClusteringCoefficient(UndirectedGraph<String, DefaultEdge> graph) {
		double sumLCCs = 0d;
		for (String v : graph.vertexSet()) {
			List<String> neighborhood = Graphs.neighborListOf(graph, v);
			int k = neighborhood.size();
			if (k >= 2) {
				int linkedNeighbors = 0;
				for (int i = 0; i < k - 1; i++)
					for (int j = i + 1; j < k; j++)
						if (graph.containsEdge(neighborhood.get(i), neighborhood.get(j)))
							linkedNeighbors++;
				sumLCCs += 2d * (double)linkedNeighbors / (double)(k * (k - 1));
			}
		}
		return sumLCCs / (double)graph.vertexSet().size();
	}
	
	// Compute cosine pairing vertices by pseudonym
	
	public static double cosineLabeledDegreeSequences(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		
		double cosSum = 0d, norm1Sum = 0d, norm2Sum = 0d;
		
		for (String v : originalGraph.vertexSet()) {
			int deg1 = originalGraph.degreeOf(v);
			norm1Sum += deg1 * deg1;
			if (perturbedGraph.containsVertex(v)) {
				int deg2 = perturbedGraph.degreeOf(v);
				norm2Sum += deg2 * deg2;
				cosSum += deg1 * deg2;
			}
		}
		
		return cosSum / (Math.sqrt(norm1Sum) * Math.sqrt(norm2Sum));
	}
	
	public static double cosineLabeledDegreeSequencesAfterKMatchCopyOperation(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> vat) {
		
		// Compute the would-be neighbor lists of the perturbed graph 
		
		Map<String, Set<String>> neighborListsInPerturbedGraph = new TreeMap<>();
		for (String v : graph.vertexSet())
			neighborListsInPerturbedGraph.put(v, new TreeSet<String>());
		
		int columnCount = vat.get(vat.keySet().iterator().next()).size();
		List<String> keyList = new ArrayList<>(vat.keySet());
		
		for (int i = 0; i < keyList.size() - 1; i++)
			for (int j = 0; j < vat.get(keyList.get(i)).size(); j++)
				for (int r = i + 1; r < keyList.size(); r++)
					for (int s = 0; s < vat.get(keyList.get(r)).size(); s++) 
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(vat.get(keyList.get(i)).get((j + k) % columnCount), vat.get(keyList.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(vat.get(keyList.get(i)).get(j)).add(vat.get(keyList.get(r)).get(s));
								neighborListsInPerturbedGraph.get(vat.get(keyList.get(r)).get(s)).add(vat.get(keyList.get(i)).get(j));
								break;
						}
					}
		
		// Compute cosine, according to neighborListsInPerturbedGraph
		
		double cosSum = 0d, norm1Sum = 0d, norm2Sum = 0d;
		
		for (String v : graph.vertexSet()) {
			int deg1 = graph.degreeOf(v);
			norm1Sum += deg1 * deg1;
			int deg2 = neighborListsInPerturbedGraph.get(v).size();
			norm2Sum += deg2 * deg2;
			cosSum += deg1 * deg2;
		}
		
		return cosSum / (Math.sqrt(norm1Sum) * Math.sqrt(norm2Sum));
	}
	
	public static double cosineLabeledDegreeSequencesAfterKMatchCopyOperation(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> currentVAT, Map<String, List<String>> newVATRows) {
		
		// Compute the would-be neighbor lists of the perturbed graph 
		
		Map<String, Set<String>> neighborListsInPerturbedGraph = new TreeMap<>();
		for (String v : graph.vertexSet())
			neighborListsInPerturbedGraph.put(v, new TreeSet<String>());
		
		int columnCount = currentVAT.get(currentVAT.keySet().iterator().next()).size();
		List<String> keyListCurrentVAT = new ArrayList<>(currentVAT.keySet());
		List<String> keyListNewVATRows = new ArrayList<>(newVATRows.keySet());
		
		for (int i = 0; i < keyListCurrentVAT.size() - 1; i++)
			for (int j = 0; j < currentVAT.get(keyListCurrentVAT.get(i)).size(); j++) {
				
				for (int r = i + 1; r < keyListCurrentVAT.size(); r++)
					for (int s = 0; s < currentVAT.get(keyListCurrentVAT.get(r)).size(); s++) 
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(currentVAT.get(keyListCurrentVAT.get(i)).get((j + k) % columnCount), currentVAT.get(keyListCurrentVAT.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(i)).get(j)).add(currentVAT.get(keyListCurrentVAT.get(r)).get(s));
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(r)).get(s)).add(currentVAT.get(keyListCurrentVAT.get(i)).get(j));
								break;
						}
					}
				
				for (int r = 0; r < keyListNewVATRows.size(); r++)
					for (int s = 0; s < newVATRows.get(keyListNewVATRows.get(r)).size(); s++)
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(currentVAT.get(keyListCurrentVAT.get(i)).get((j + k) % columnCount), newVATRows.get(keyListNewVATRows.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(i)).get(j)).add(newVATRows.get(keyListNewVATRows.get(r)).get(s));
								neighborListsInPerturbedGraph.get(newVATRows.get(keyListNewVATRows.get(r)).get(s)).add(currentVAT.get(keyListCurrentVAT.get(i)).get(j));
								break;
							}
						}
			}
		
		// Compute cosine, according to neighborListsInPerturbedGraph
		
		double cosSum = 0d, norm1Sum = 0d, norm2Sum = 0d;
		
		for (String v : graph.vertexSet()) {
			int deg1 = graph.degreeOf(v);
			norm1Sum += deg1 * deg1;
			int deg2 = neighborListsInPerturbedGraph.get(v).size();
			norm2Sum += deg2 * deg2;
			cosSum += deg1 * deg2;
		}
		
		return cosSum / (Math.sqrt(norm1Sum) * Math.sqrt(norm2Sum));
	}
	
	// Decrementally sort degree sequences, then compute cosine pairing values by order
	
	public static double cosineUnlabeledDegreeSequences(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		
		double cosSum = 0d, norm1Sum = 0d, norm2Sum = 0d;
		
		ArrayList<Integer> degreesOrig = new ArrayList<>();
		for (String v : originalGraph.vertexSet())
			degreesOrig.add(originalGraph.degreeOf(v));
		Collections.sort(degreesOrig, Collections.reverseOrder());
		
		ArrayList<Integer> degreesPert = new ArrayList<>();
		for (String v : perturbedGraph.vertexSet())
			degreesPert.add(perturbedGraph.degreeOf(v));
		Collections.sort(degreesPert, Collections.reverseOrder());
		
		if (degreesOrig.size() < degreesPert.size())
			for (int i = degreesOrig.size(); i < degreesPert.size(); i++)
				degreesOrig.add(0);
		else if (degreesOrig.size() > degreesPert.size())
			for (int i = degreesPert.size(); i < degreesOrig.size(); i++)
				degreesPert.add(0);
		
		for (int i = 0; i < degreesOrig.size(); i++) {
			int deg1 = degreesOrig.get(i);
			norm1Sum += deg1 * deg1;	
			int deg2 = degreesPert.get(i);
			norm2Sum += deg2 * deg2;
			cosSum += deg1 * deg2;
			
		}
		
		return cosSum / (Math.sqrt(norm1Sum) * Math.sqrt(norm2Sum));
	}
	
	public static double cosineUnlabeledDegreeSequencesAfterKMatchCopyOperation(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> vat) {
		
		double cosSum = 0d, norm1Sum = 0d, norm2Sum = 0d;
		
		// Obtain ordered degree list of the graph
		
		ArrayList<Integer> degreesOrig = new ArrayList<>();
		for (String v : graph.vertexSet())
			degreesOrig.add(graph.degreeOf(v));
		Collections.sort(degreesOrig, Collections.reverseOrder());
		
		// Compute the would-be neighbor lists of the perturbed graph 
		
		Map<String, Set<String>> neighborListsInPerturbedGraph = new TreeMap<>();
		for (String v : graph.vertexSet())
			neighborListsInPerturbedGraph.put(v, new TreeSet<String>());
		
		int columnCount = vat.get(vat.keySet().iterator().next()).size();
		List<String> keyList = new ArrayList<>(vat.keySet());
		
		for (int i = 0; i < keyList.size() - 1; i++)
			for (int j = 0; j < vat.get(keyList.get(i)).size(); j++)
				for (int r = i + 1; r < keyList.size(); r++)
					for (int s = 0; s < vat.get(keyList.get(r)).size(); s++) 
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(vat.get(keyList.get(i)).get((j + k) % columnCount), vat.get(keyList.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(vat.get(keyList.get(i)).get(j)).add(vat.get(keyList.get(r)).get(s));
								neighborListsInPerturbedGraph.get(vat.get(keyList.get(r)).get(s)).add(vat.get(keyList.get(i)).get(j));
								break;
						}
					}
		
		// Obtain the would-be ordered degree list of the perturbed graph, according to neighborListsInPerturbedGraph
		
		ArrayList<Integer> degreesPert = new ArrayList<>();
		for (String v : graph.vertexSet())
			degreesPert.add(neighborListsInPerturbedGraph.get(v).size());
		Collections.sort(degreesPert, Collections.reverseOrder());
		
		// Compute cosine
		
		if (degreesOrig.size() < degreesPert.size())
			for (int i = degreesOrig.size(); i < degreesPert.size(); i++)
				degreesOrig.add(0);
		else if (degreesOrig.size() > degreesPert.size())
			for (int i = degreesPert.size(); i < degreesOrig.size(); i++)
				degreesPert.add(0);
		
		for (int i = 0; i < degreesOrig.size(); i++) {
			int deg1 = degreesOrig.get(i);
			norm1Sum += deg1 * deg1;	
			int deg2 = degreesPert.get(i);
			norm2Sum += deg2 * deg2;
			cosSum += deg1 * deg2;
			
		}
		
		return cosSum / (Math.sqrt(norm1Sum) * Math.sqrt(norm2Sum));
	}
	
	public static double cosineUnlabeledDegreeSequencesAfterKMatchCopyOperation(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> currentVAT, Map<String, List<String>> newVATRows) {
		
		double cosSum = 0d, norm1Sum = 0d, norm2Sum = 0d;
		
		// Obtain ordered degree list of the graph
		
		ArrayList<Integer> degreesOrig = new ArrayList<>();
		for (String v : graph.vertexSet())
			degreesOrig.add(graph.degreeOf(v));
		Collections.sort(degreesOrig, Collections.reverseOrder());
		
		// Compute the would-be neighbor lists of the perturbed graph 
		
		Map<String, Set<String>> neighborListsInPerturbedGraph = new TreeMap<>();
		for (String v : graph.vertexSet())
			neighborListsInPerturbedGraph.put(v, new TreeSet<String>());
		
		int columnCount = currentVAT.get(currentVAT.keySet().iterator().next()).size();
		List<String> keyListCurrentVAT = new ArrayList<>(currentVAT.keySet());
		List<String> keyListNewVATRows = new ArrayList<>(newVATRows.keySet());
		
		for (int i = 0; i < keyListCurrentVAT.size() - 1; i++)
			for (int j = 0; j < currentVAT.get(keyListCurrentVAT.get(i)).size(); j++) {
				
				for (int r = i + 1; r < keyListCurrentVAT.size(); r++)
					for (int s = 0; s < currentVAT.get(keyListCurrentVAT.get(r)).size(); s++) 
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(currentVAT.get(keyListCurrentVAT.get(i)).get((j + k) % columnCount), currentVAT.get(keyListCurrentVAT.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(i)).get(j)).add(currentVAT.get(keyListCurrentVAT.get(r)).get(s));
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(r)).get(s)).add(currentVAT.get(keyListCurrentVAT.get(i)).get(j));
								break;
						}
					}
				
				for (int r = 0; r < keyListNewVATRows.size(); r++)
					for (int s = 0; s < newVATRows.get(keyListNewVATRows.get(r)).size(); s++)
						for (int k = 0; k < columnCount; k++) {
							if (graph.containsEdge(currentVAT.get(keyListCurrentVAT.get(i)).get((j + k) % columnCount), newVATRows.get(keyListNewVATRows.get(r)).get((s + k) % columnCount))) {
								neighborListsInPerturbedGraph.get(currentVAT.get(keyListCurrentVAT.get(i)).get(j)).add(newVATRows.get(keyListNewVATRows.get(r)).get(s));
								neighborListsInPerturbedGraph.get(newVATRows.get(keyListNewVATRows.get(r)).get(s)).add(currentVAT.get(keyListCurrentVAT.get(i)).get(j));
								break;
							}
						}
			}
		
		// Obtain the would-be ordered degree list of the perturbed graph, according to neighborListsInPerturbedGraph
		
		ArrayList<Integer> degreesPert = new ArrayList<>();
		for (String v : graph.vertexSet())
			degreesPert.add(neighborListsInPerturbedGraph.get(v).size());
		Collections.sort(degreesPert, Collections.reverseOrder());
		
		// Compute cosine
		
		if (degreesOrig.size() < degreesPert.size())
			for (int i = degreesOrig.size(); i < degreesPert.size(); i++)
				degreesOrig.add(0);
		else if (degreesOrig.size() > degreesPert.size())
			for (int i = degreesPert.size(); i < degreesOrig.size(); i++)
				degreesPert.add(0);
		
		for (int i = 0; i < degreesOrig.size(); i++) {
			int deg1 = degreesOrig.get(i);
			norm1Sum += deg1 * deg1;	
			int deg2 = degreesPert.get(i);
			norm2Sum += deg2 * deg2;
			cosSum += deg1 * deg2;
			
		}
		
		return cosSum / (Math.sqrt(norm1Sum) * Math.sqrt(norm2Sum));
	}
	
	public static double klDivergenceDegreeDistributions(UndirectedGraph<String, DefaultEdge> originalGraph, UndirectedGraph<String, DefaultEdge> perturbedGraph) {
		
		DistributionComputer distComp = new DegreeDistributionComputer();
		
		Map<Integer, Double> distOrig = distComp.computeDistributionAsProbabilities(originalGraph);
		Map<Integer, Double> distPert = distComp.computeDistributionAsProbabilities(perturbedGraph);
		
		double klDiv = 0d;
		double entropyOrig = 0d;
		
		for (int vo : distOrig.keySet()) {
			entropyOrig += -(distOrig.get(vo) * (Math.log(distOrig.get(vo)) / Math.log(2d)));
			if (distPert.containsKey(vo)) 
				klDiv += distOrig.get(vo) * (Math.log(distOrig.get(vo) / distPert.get(vo)) / Math.log(2d));
			else
				klDiv += distOrig.get(vo) * (Math.log(distOrig.get(vo) / distPert.get(-1)) / Math.log(2d));
		}
		
		return (entropyOrig + klDiv) / entropyOrig;
		
	}
	
}
