package util;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * 
 * This class is a placeholder for real frequent subgraph mining methods, to be used for debugging other methods 
 * that need this type of output. 
 * For most graphs G=(V,E) (those where minSupport <= |E| <= (|V|*(|V| - 1)) / 2 - minSupport), 
 * it returns {{<v_i, v_j>_G | (v_i, v_j) \in E}, {<v_i, v_j>_G | (v_i, v_j) \notin E}}. 
 * Otherwise it returns {{<v_i, v_j>_G | (v_i, v_j) \in E}} if G is (almost) complete 
 * or {{<v_i, v_j>_G | (v_i, v_j) \notin E}} if G is (almost) empty.
 *
 */

public class FrequentSubgraphMinerTrivial implements FrequentSubgraphMiner {

	public FrequentSubgraphMinerTrivial() {
	}

	@Override
	public Set<Set<Set<String>>> vertexSetsOfFrequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int minSupport) {

		Set<Set<Set<String>>> vertexSetsFrequentSubgraphs = new TreeSet<>();
		ArrayList<String> vertList = new ArrayList<>(graph.vertexSet());
		
		if (graph.edgeSet().size() >= minSupport) {   // Return every K_2
			Set<Set<String>> k2GraphSet = new TreeSet<>();
			for (int i = 0; i < vertList.size() - 1; i++)
				for (int j = i + 1; j < vertList.size(); j++)
					if (graph.containsEdge(vertList.get(i), vertList.get(j))) {
						Set<String> k2Gr = new TreeSet<>();
						k2Gr.add(vertList.get(i));
						k2Gr.add(vertList.get(j));
						k2GraphSet.add(k2Gr);
					}
			vertexSetsFrequentSubgraphs.add(k2GraphSet);
		}
		
		if ((graph.vertexSet().size() * (graph.vertexSet().size() - 1)) / 2 - graph.edgeSet().size() >= minSupport) {   // Return every N_2
			Set<Set<String>> n2GraphSet = new TreeSet<>();
			for (int i = 0; i < vertList.size() - 1; i++)
				for (int j = i + 1; j < vertList.size(); j++)
					if (!graph.containsEdge(vertList.get(i), vertList.get(j))) {
						Set<String> n2Gr = new TreeSet<>();
						n2Gr.add(vertList.get(i));
						n2Gr.add(vertList.get(j));
						n2GraphSet.add(n2Gr);
					}
			vertexSetsFrequentSubgraphs.add(n2GraphSet);
		}
		
		return vertexSetsFrequentSubgraphs;
	}

	@Override
	public Set<Set<UndirectedGraph<String, DefaultEdge>>> frequentSubgraphs(UndirectedGraph<String, DefaultEdge> graph,	int minSupport) {
		
		Set<Set<UndirectedGraph<String, DefaultEdge>>> frequentSubgraphs = new TreeSet<>();
		ArrayList<String> vertList = new ArrayList<>(graph.vertexSet());
		
		if (graph.edgeSet().size() >= minSupport) {   // Return every K_2
			Set<UndirectedGraph<String, DefaultEdge>> k2GraphSet = new TreeSet<>();
			for (int i = 0; i < vertList.size() - 1; i++)
				for (int j = i + 1; j < vertList.size(); j++)
					if (graph.containsEdge(vertList.get(i), vertList.get(j))) {
						UndirectedGraph<String, DefaultEdge> k2Gr = new SimpleGraph<>(DefaultEdge.class);
						k2Gr.addVertex(vertList.get(i));
						k2Gr.addVertex(vertList.get(j));
						k2Gr.addEdge(vertList.get(i), vertList.get(j));
						k2GraphSet.add(k2Gr);
					}
			frequentSubgraphs.add(k2GraphSet);
		}
		
		if ((graph.vertexSet().size() * (graph.vertexSet().size() - 1)) / 2 - graph.edgeSet().size() >= minSupport) {   // Return every N_2
			Set<UndirectedGraph<String, DefaultEdge>> n2GraphSet = new TreeSet<>();
			for (int i = 0; i < vertList.size() - 1; i++)
				for (int j = i + 1; j < vertList.size(); j++)
					if (!graph.containsEdge(vertList.get(i), vertList.get(j))) {
						UndirectedGraph<String, DefaultEdge> n2Gr = new SimpleGraph<>(DefaultEdge.class);
						n2Gr.addVertex(vertList.get(i));
						n2Gr.addVertex(vertList.get(j));
						n2Gr.addEdge(vertList.get(i), vertList.get(j));
						n2GraphSet.add(n2Gr);
					}
			frequentSubgraphs.add(n2GraphSet);
		}
		
		return frequentSubgraphs;
	}

}
