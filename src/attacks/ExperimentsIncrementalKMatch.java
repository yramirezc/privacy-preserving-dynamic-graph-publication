package attacks;

import java.util.Map;
import java.util.TreeMap;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import anonymization.IncrementalKMatchSequenceAnonymizer;
import util.BarabasiAlbertGraphGenerator;
import util.GraphUtil;

public class ExperimentsIncrementalKMatch {

	public static void main(String[] args) {
		
		if (args.length == 2) {
			
			IncrementalKMatchSequenceAnonymizer.setMetisFailureCount(0);
			
			int[] ks = {2, 5, 8};
			
			Map<Integer, Map<Integer, Integer>> timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNoOptStriclyBetterThanSimAnnealingUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS = new TreeMap<>();
					
			Map<Integer, Map<Integer, Integer>> timesMETISNoOptWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNotMETISNoOptWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesMETISRandLocalSearchWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNotMETISRandLocalSearchWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesMETISSimAnnealingWasBest = new TreeMap<>();
			Map<Integer, Map<Integer, Integer>> timesNotMETISSimAnnealingWasBest = new TreeMap<>();
			
			int[] snapshots = {1, 2, 3};
			for (int ss : snapshots) {
				
				timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.put(ss, new TreeMap<Integer, Integer>());
				
				timesMETISNoOptWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesNotMETISNoOptWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesMETISRandLocalSearchWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesNotMETISRandLocalSearchWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesMETISSimAnnealingWasBest.put(ss, new TreeMap<Integer, Integer>());
				timesNotMETISSimAnnealingWasBest.put(ss, new TreeMap<Integer, Integer>());
				
				for (int k : ks) {
					
					timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(ss).put(k, 0);
					timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(ss).put(k, 0);
					timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(ss).put(k, 0);
					timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(ss).put(k, 0);
					timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(ss).put(k, 0);
					timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(ss).put(k, 0);
					
					timesMETISNoOptWasBest.get(ss).put(k, 0);
					timesNotMETISNoOptWasBest.get(ss).put(k, 0);
					timesMETISRandLocalSearchWasBest.get(ss).put(k, 0);
					timesNotMETISRandLocalSearchWasBest.get(ss).put(k, 0);
					timesMETISSimAnnealingWasBest.get(ss).put(k, 0);
					timesNotMETISSimAnnealingWasBest.get(ss).put(k, 0);
				
				}
			}
			
			int maxIterCount = 100;
			
			for (int iter = 0; iter < maxIterCount; iter++) {
				
				System.out.println("Iteration # " + iter);
				
				// Create anonymizers for k \in ks
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersUsingMETISNoOptimization = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersNotUsingMETISNoOptimization = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersUsingMETISRandLocalSearch = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersNotUsingMETISRandLocalSearch = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersUsingMETISSimAnnealing = new TreeMap<>();
				Map<Integer, IncrementalKMatchSequenceAnonymizer> anonymizersNotUsingMETISSimAnnealing = new TreeMap<>();
				for (int k : ks) {
					anonymizersUsingMETISNoOptimization.put(k, new IncrementalKMatchSequenceAnonymizer(k, 0, args[0], args[1]));
					anonymizersNotUsingMETISNoOptimization.put(k, new IncrementalKMatchSequenceAnonymizer(k, 0));
					anonymizersUsingMETISRandLocalSearch.put(k, new IncrementalKMatchSequenceAnonymizer(k, 1, 2, args[0], args[1]));   // objFn labeled degree sequence similarity
					anonymizersNotUsingMETISRandLocalSearch.put(k, new IncrementalKMatchSequenceAnonymizer(k, 1, 2));   // objFn labeled degree sequence similarity
					anonymizersUsingMETISSimAnnealing.put(k, new IncrementalKMatchSequenceAnonymizer(k, 2, 2, args[0], args[1]));   // objFn labeled degree sequence similarity
					anonymizersNotUsingMETISSimAnnealing.put(k, new IncrementalKMatchSequenceAnonymizer(k, 2, 2));   // objFn labeled degree sequence similarity
				}
				
				System.out.println("First snapshot");
				
				UndirectedGraph<String, DefaultEdge> graph = BarabasiAlbertGraphGenerator.newGraph(200, 0, 50, 5, 3);
					
				int origEdgeCount = graph.edgeSet().size(), origVertexCount = graph.vertexSet().size();
			
				// Apply the method with k \in ks
				for (int k : ks) {
					
					System.out.println("\tk = " + k);
					
					//System.out.println("\tCopying crossing edges:");
					
					UndirectedGraph<String, DefaultEdge> cloneMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					
					anonymizersUsingMETISNoOptimization.get(k).anonymizeGraph(cloneMETISNoOpt, false, "TesterMETISNoOpt");
					anonymizersNotUsingMETISNoOptimization.get(k).anonymizeGraph(cloneNotMETISNoOpt, false, "TesterNoMETISNoOpt");
					anonymizersUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneMETISRandLocalSearch, false, "TesterMETISRandLocalSearch");
					anonymizersNotUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneNotMETISRandLocalSearch, false, "TesterNoMETISRandLocalSearch");
					anonymizersUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneMETISSimAnnealing, false, "TesterMETISSimAnnealing");
					anonymizersNotUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneNotMETISSimAnnealing, false, "TesterNoMETISSimAnnealing");
					
					// Report effect of anonymization on the graph
					System.out.println("\t\tOriginal vertex count: " + origVertexCount);
					System.out.println("\t\tFinal vertex count using METIS, no optimization: " + cloneMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, no optimization: " + cloneNotMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, randomized local search: " + cloneMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, simulated annealing: " + cloneMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tOriginal edge count: " + origEdgeCount);
					System.out.println("\t\tFinal edge count using METIS, no optimization: " + cloneMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, no optimization: " + cloneNotMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, randomized local search: " + cloneMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, simulated annealing: " + cloneMETISSimAnnealing.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.edgeSet().size());
					int deltaEdgeMETISNoOpt = cloneMETISNoOpt.edgeSet().size() - origEdgeCount;
					int minDeltaEdge = deltaEdgeMETISNoOpt;
					System.out.println("\t\tDelta using METIS, no optimization: " + deltaEdgeMETISNoOpt + " (" + ((double)deltaEdgeMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISNoOpt = cloneNotMETISNoOpt.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISNoOpt < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISNoOpt;
					System.out.println("\t\tDelta not using METIS, no optimization: " + deltaEdgeNotMETISNoOpt + " (" + ((double)deltaEdgeNotMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISRandLocalSearch = cloneMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISRandLocalSearch;
					System.out.println("\t\tDelta using METIS, randomized local search: " + deltaEdgeMETISRandLocalSearch + " (" + ((double)deltaEdgeMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISRandLocalSearch = cloneNotMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISRandLocalSearch;
					System.out.println("\t\tDelta not using METIS, randomized local search: " + deltaEdgeNotMETISRandLocalSearch + " (" + ((double)deltaEdgeNotMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISSimAnnealing = cloneMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISSimAnnealing;
					System.out.println("\t\tDelta using METIS, simulated annealing: " + deltaEdgeMETISSimAnnealing + " (" + ((double)deltaEdgeMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISSimAnnealing = cloneNotMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISSimAnnealing;
					System.out.println("\t\tDelta not using METIS, simulated annealing: " + deltaEdgeNotMETISSimAnnealing + " (" + ((double)deltaEdgeNotMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					
					if (deltaEdgeMETISNoOpt == minDeltaEdge)
						timesMETISNoOptWasBest.get(1).put(k, timesMETISNoOptWasBest.get(1).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt == minDeltaEdge)
						timesNotMETISNoOptWasBest.get(1).put(k, timesNotMETISNoOptWasBest.get(1).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch == minDeltaEdge)
						timesMETISRandLocalSearchWasBest.get(1).put(k, timesMETISRandLocalSearchWasBest.get(1).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch == minDeltaEdge)
						timesNotMETISRandLocalSearchWasBest.get(1).put(k, timesNotMETISRandLocalSearchWasBest.get(1).get(k) + 1);
					if (deltaEdgeMETISSimAnnealing == minDeltaEdge)
						timesMETISSimAnnealingWasBest.get(1).put(k, timesMETISSimAnnealingWasBest.get(1).get(k) + 1);
					if (deltaEdgeNotMETISSimAnnealing == minDeltaEdge)
						timesNotMETISSimAnnealingWasBest.get(1).put(k, timesNotMETISSimAnnealingWasBest.get(1).get(k) + 1);
					
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(1).put(k, timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(1).put(k, timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(1).put(k, timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).put(k, timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch < deltaEdgeMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(1).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch < deltaEdgeNotMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k) + 1);
					
				}
				
				System.out.println("");
				
				System.out.println("Second snapshot");
				
				graph = BarabasiAlbertGraphGenerator.newGraph(225, 0, (SimpleGraph<String, DefaultEdge>)graph, 5);
				
				origEdgeCount = graph.edgeSet().size(); 
				origVertexCount = graph.vertexSet().size();
				
				// Apply the method with k \in ks
				for (int k : ks) {
					
					System.out.println("\tk = " + k);
					
					UndirectedGraph<String, DefaultEdge> cloneMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					
					anonymizersUsingMETISNoOptimization.get(k).anonymizeGraph(cloneMETISNoOpt, false, "TesterMETISNoOpt");
					anonymizersNotUsingMETISNoOptimization.get(k).anonymizeGraph(cloneNotMETISNoOpt, false, "TesterNoMETISNoOpt");
					anonymizersUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneMETISRandLocalSearch, false, "TesterMETISRandLocalSearch");
					anonymizersNotUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneNotMETISRandLocalSearch, false, "TesterNoMETISRandLocalSearch");
					anonymizersUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneMETISSimAnnealing, false, "TesterMETISSimAnnealing");
					anonymizersNotUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneNotMETISSimAnnealing, false, "TesterNoMETISSimAnnealing");
					
					// Report effect of anonymization on the graph
					System.out.println("\t\tOriginal vertex count: " + origVertexCount);
					System.out.println("\t\tFinal vertex count using METIS, no optimization: " + cloneMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, no optimization: " + cloneNotMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, randomized local search: " + cloneMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, simulated annealing: " + cloneMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tOriginal edge count: " + origEdgeCount);
					System.out.println("\t\tFinal edge count using METIS, no optimization: " + cloneMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, no optimization: " + cloneNotMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, randomized local search: " + cloneMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, simulated annealing: " + cloneMETISSimAnnealing.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.edgeSet().size());
					int deltaEdgeMETISNoOpt = cloneMETISNoOpt.edgeSet().size() - origEdgeCount;
					int minDeltaEdge = deltaEdgeMETISNoOpt;
					System.out.println("\t\tDelta using METIS, no optimization: " + deltaEdgeMETISNoOpt + " (" + ((double)deltaEdgeMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISNoOpt = cloneNotMETISNoOpt.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISNoOpt < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISNoOpt;
					System.out.println("\t\tDelta not using METIS, no optimization: " + deltaEdgeNotMETISNoOpt + " (" + ((double)deltaEdgeNotMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISRandLocalSearch = cloneMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISRandLocalSearch;
					System.out.println("\t\tDelta using METIS, randomized local search: " + deltaEdgeMETISRandLocalSearch + " (" + ((double)deltaEdgeMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISRandLocalSearch = cloneNotMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISRandLocalSearch;
					System.out.println("\t\tDelta not using METIS, randomized local search: " + deltaEdgeNotMETISRandLocalSearch + " (" + ((double)deltaEdgeNotMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISSimAnnealing = cloneMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISSimAnnealing;
					System.out.println("\t\tDelta using METIS, simulated annealing: " + deltaEdgeMETISSimAnnealing + " (" + ((double)deltaEdgeMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISSimAnnealing = cloneNotMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISSimAnnealing;
					System.out.println("\t\tDelta not using METIS, simulated annealing: " + deltaEdgeNotMETISSimAnnealing + " (" + ((double)deltaEdgeNotMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					
					if (deltaEdgeMETISNoOpt == minDeltaEdge)
						timesMETISNoOptWasBest.get(2).put(k, timesMETISNoOptWasBest.get(2).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt == minDeltaEdge)
						timesNotMETISNoOptWasBest.get(2).put(k, timesNotMETISNoOptWasBest.get(2).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch == minDeltaEdge)
						timesMETISRandLocalSearchWasBest.get(2).put(k, timesMETISRandLocalSearchWasBest.get(2).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch == minDeltaEdge)
						timesNotMETISRandLocalSearchWasBest.get(2).put(k, timesNotMETISRandLocalSearchWasBest.get(2).get(k) + 1);
					if (deltaEdgeMETISSimAnnealing == minDeltaEdge)
						timesMETISSimAnnealingWasBest.get(2).put(k, timesMETISSimAnnealingWasBest.get(2).get(k) + 1);
					if (deltaEdgeNotMETISSimAnnealing == minDeltaEdge)
						timesNotMETISSimAnnealingWasBest.get(2).put(k, timesNotMETISSimAnnealingWasBest.get(2).get(k) + 1);
					
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(2).put(k, timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(2).put(k, timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(2).put(k, timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).put(k, timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch < deltaEdgeMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(2).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch < deltaEdgeNotMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k) + 1);
					
				}
				
				System.out.println("");
							
				System.out.println("Third snapshot");
				
				graph = BarabasiAlbertGraphGenerator.newGraph(250, 0, (SimpleGraph<String, DefaultEdge>)graph, 5);
				
				origEdgeCount = graph.edgeSet().size(); 
				origVertexCount = graph.vertexSet().size();
				
				// Apply the method with k \in ks
				for (int k : ks) {
					
					System.out.println("\tk = " + k);
					
					UndirectedGraph<String, DefaultEdge> cloneMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISNoOpt = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISRandLocalSearch = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					UndirectedGraph<String, DefaultEdge> cloneNotMETISSimAnnealing = GraphUtil.cloneGraph(graph);
					
					anonymizersUsingMETISNoOptimization.get(k).anonymizeGraph(cloneMETISNoOpt, false, "TesterMETISNoOpt");
					anonymizersNotUsingMETISNoOptimization.get(k).anonymizeGraph(cloneNotMETISNoOpt, false, "TesterNoMETISNoOpt");
					anonymizersUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneMETISRandLocalSearch, false, "TesterMETISRandLocalSearch");
					anonymizersNotUsingMETISRandLocalSearch.get(k).anonymizeGraph(cloneNotMETISRandLocalSearch, false, "TesterNoMETISRandLocalSearch");
					anonymizersUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneMETISSimAnnealing, false, "TesterMETISSimAnnealing");
					anonymizersNotUsingMETISSimAnnealing.get(k).anonymizeGraph(cloneNotMETISSimAnnealing, false, "TesterNoMETISSimAnnealing");
					
					// Report effect of anonymization on the graph
					System.out.println("\t\tOriginal vertex count: " + origVertexCount);
					System.out.println("\t\tFinal vertex count using METIS, no optimization: " + cloneMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, no optimization: " + cloneNotMETISNoOpt.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, randomized local search: " + cloneMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.vertexSet().size());
					System.out.println("\t\tFinal vertex count using METIS, simulated annealing: " + cloneMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tFinal vertex count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.vertexSet().size());
					System.out.println("\t\tOriginal edge count: " + origEdgeCount);
					System.out.println("\t\tFinal edge count using METIS, no optimization: " + cloneMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, no optimization: " + cloneNotMETISNoOpt.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, randomized local search: " + cloneMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, randomized local search: " + cloneNotMETISRandLocalSearch.edgeSet().size());
					System.out.println("\t\tFinal edge count using METIS, simulated annealing: " + cloneMETISSimAnnealing.edgeSet().size());
					System.out.println("\t\tFinal edge count not using METIS, simulated annealing: " + cloneNotMETISSimAnnealing.edgeSet().size());
					int deltaEdgeMETISNoOpt = cloneMETISNoOpt.edgeSet().size() - origEdgeCount;
					int minDeltaEdge = deltaEdgeMETISNoOpt;
					System.out.println("\t\tDelta using METIS, no optimization: " + deltaEdgeMETISNoOpt + " (" + ((double)deltaEdgeMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISNoOpt = cloneNotMETISNoOpt.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISNoOpt < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISNoOpt;
					System.out.println("\t\tDelta not using METIS, no optimization: " + deltaEdgeNotMETISNoOpt + " (" + ((double)deltaEdgeNotMETISNoOpt * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISRandLocalSearch = cloneMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISRandLocalSearch;
					System.out.println("\t\tDelta using METIS, randomized local search: " + deltaEdgeMETISRandLocalSearch + " (" + ((double)deltaEdgeMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISRandLocalSearch = cloneNotMETISRandLocalSearch.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISRandLocalSearch < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISRandLocalSearch;
					System.out.println("\t\tDelta not using METIS, randomized local search: " + deltaEdgeNotMETISRandLocalSearch + " (" + ((double)deltaEdgeNotMETISRandLocalSearch * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeMETISSimAnnealing = cloneMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeMETISSimAnnealing;
					System.out.println("\t\tDelta using METIS, simulated annealing: " + deltaEdgeMETISSimAnnealing + " (" + ((double)deltaEdgeMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					int deltaEdgeNotMETISSimAnnealing = cloneNotMETISSimAnnealing.edgeSet().size() - origEdgeCount;
					if (deltaEdgeNotMETISSimAnnealing < minDeltaEdge)
						minDeltaEdge = deltaEdgeNotMETISSimAnnealing;
					System.out.println("\t\tDelta not using METIS, simulated annealing: " + deltaEdgeNotMETISSimAnnealing + " (" + ((double)deltaEdgeNotMETISSimAnnealing * 100d / (double)origEdgeCount) + "%)");
					
					if (deltaEdgeMETISNoOpt == minDeltaEdge)
						timesMETISNoOptWasBest.get(3).put(k, timesMETISNoOptWasBest.get(3).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt == minDeltaEdge)
						timesNotMETISNoOptWasBest.get(3).put(k, timesNotMETISNoOptWasBest.get(3).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch == minDeltaEdge)
						timesMETISRandLocalSearchWasBest.get(3).put(k, timesMETISRandLocalSearchWasBest.get(3).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch == minDeltaEdge)
						timesNotMETISRandLocalSearchWasBest.get(3).put(k, timesNotMETISRandLocalSearchWasBest.get(3).get(k) + 1);
					if (deltaEdgeMETISSimAnnealing == minDeltaEdge)
						timesMETISSimAnnealingWasBest.get(3).put(k, timesMETISSimAnnealingWasBest.get(3).get(k) + 1);
					if (deltaEdgeNotMETISSimAnnealing == minDeltaEdge)
						timesNotMETISSimAnnealingWasBest.get(3).put(k, timesNotMETISSimAnnealingWasBest.get(3).get(k) + 1);
					
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(3).put(k, timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISRandLocalSearch)
						timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(3).put(k, timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeMETISNoOpt < deltaEdgeMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(3).put(k, timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeNotMETISNoOpt < deltaEdgeNotMETISSimAnnealing)
						timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).put(k, timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeMETISRandLocalSearch < deltaEdgeMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(3).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k) + 1);
					if (deltaEdgeNotMETISRandLocalSearch < deltaEdgeNotMETISSimAnnealing)
						timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).put(k, timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k) + 1);
					
				}
				
				System.out.println("");
				System.out.println("Summary so far:");
				
				System.out.println("");
				System.out.println("\tNumber of times using METIS and applying no optimization was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISNoOptWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISNoOptWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISNoOptWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISNoOptWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISNoOptWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISNoOptWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times not using METIS and applying no optimization was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISNoOptWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISNoOptWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISNoOptWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISNoOptWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISNoOptWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISNoOptWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
								
				System.out.println("");
				System.out.println("\tNumber of times using METIS and applying randomized local search was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISRandLocalSearchWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISRandLocalSearchWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");				
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISRandLocalSearchWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISRandLocalSearchWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");				
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISRandLocalSearchWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISRandLocalSearchWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times not using METIS and applying randomized local search was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISRandLocalSearchWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISRandLocalSearchWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISRandLocalSearchWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISRandLocalSearchWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");				
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISRandLocalSearchWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISRandLocalSearchWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times using METIS and applying simulated annealing was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISSimAnnealingWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISSimAnnealingWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISSimAnnealingWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISSimAnnealingWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesMETISSimAnnealingWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesMETISSimAnnealingWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times not using METIS and applying simulated annealing was best:");
				System.out.println("\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISSimAnnealingWasBest.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISSimAnnealingWasBest.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISSimAnnealingWasBest.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISSimAnnealingWasBest.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\tk= " + k + ": " + timesNotMETISSimAnnealingWasBest.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNotMETISSimAnnealingWasBest.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
								
				System.out.println("");
				System.out.println("\tNumber of times using no optimization was strictly better than randomized local search:");
				System.out.println("\t\tUsing METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tNot using METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanRandLocalSearchNotUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times using no optimization was strictly better than simulated annealing:");
				System.out.println("\t\tUsing METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");				
				System.out.println("\t\tNot using METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesNoOptStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
				System.out.println("\tNumber of times using randomized local search was strictly better than simulated annealing:");
				System.out.println("\t\tUsing METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tNot using METIS:");
				System.out.println("\t\tFirst snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(1).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tSecond snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(2).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				System.out.println("\t\tThird snapshot:");
				for (int k : ks)
					System.out.println("\t\t\tk= " + k + ": " + timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k) + "/" + (ks.length * (iter + 1)) + " (" + (double)(100 * timesRandLocalSearchStriclyBetterThanSimAnnealingNotUsingMETIS.get(3).get(k)) / (double)(ks.length * (iter + 1)) + "%)");
				
				System.out.println("");
			}
			
			System.out.println("METIS failed " + IncrementalKMatchSequenceAnonymizer.getMetisFailureCount() + " times");
			System.out.println();
			
		}
	}

}
