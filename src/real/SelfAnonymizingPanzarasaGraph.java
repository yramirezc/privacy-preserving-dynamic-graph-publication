package real;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import test.AntiResolving;
import test.AntiResolvingPersistence.STATE;
import test.Output;

/**
 * 
 * @author yunior.ramirez
 * 
 * 29/03/2019
 * 
 * The purpose of this class is to contain the methods originally in PanzarasaGraph,
 * which will now be used as a loader only
 *
 */

public class SelfAnonymizingPanzarasaGraph extends PanzarasaGraph {

	private static final long serialVersionUID = 2056553994567426369L;

	public SelfAnonymizingPanzarasaGraph(Class<? extends DefaultEdge> edgeClass) {
		super(edgeClass);
	}
	
	public static void main(String[] args) throws IOException {
		System.setOut(new PrintStream(new File("panzarasa.txt")));
		SimpleGraph<String, DefaultEdge> graph = new PanzarasaGraph(DefaultEdge.class);
		System.out.println("Vertices = "+graph.vertexSet().size());
		System.out.println("Edges = "+graph.edgeSet().size());
		int m = 2;
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
		for (int k = 8; k >= 1; k--){
			Output result = AntiResolving.antiResolvingBasis(graph, floyd, k, m);
			//Output result = AntiResolvingPersistence.antiResolvingBasisPersistence(graph, floyd, k, m);
			if (result.output.equals(STATE.UNKNOWN)){
				System.out.println("The k-metric antidimension for k = "+k+" could not be found");
			}
			else if (result.output.equals(STATE.POSITIVE)){
				System.out.println("The k-metric antidimension for k = "+k+" is "+result.size);
				System.out.println("Vertices in the set");
				for (String v : result.antiResolvingBasis){
					System.out.println(v);
				}
			}
			else{
				System.out.println("The k-metric antidimension for k = "+k+" does not exist");
			}
		}		
		//checkGraphPrivacy(args);
		//makeTheGraphPrivate(args);
	}
	
	public static void checkGraphPrivacy(String[] args) throws IOException{
		SimpleGraph<String, DefaultEdge> graph = new PanzarasaGraph(DefaultEdge.class);
		int m = Integer.parseInt(args[0]);
		double l = Double.parseDouble(args[1]);
		//out.append("k \t POSITIVE \t NEGATIVE \t UNKNOWN "+NEW_LINE);
		//testAntiResolvingBasisToFile(k, m, out);
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
		int graphSize = graph.vertexSet().size();
		for (int k = 1; k <= graphSize; k++){
			Output result = AntiResolving.antiResolvingBasis(graph, floyd, k, m);
			//Output result = AntiResolvingPersistence.antiResolvingBasisPersistence(graph, floyd, k, m);
			if (result.output.equals(STATE.UNKNOWN)){
				System.out.println("UNKNOWN: The privacy is for k = "+k);
				break;
			}
			else if (result.output.equals(STATE.POSITIVE)){
				if (result.size <= graphSize*l/100){
					System.out.println("POSITIVE: The privacy is for k = "+k);
					break;
				}
			}
		}		
		System.out.println("NOT FOUND");
	}
	
	public static void makeTheGraphPrivate(String[] args) throws IOException{
		System.setOut(new PrintStream(new File("panzarasaPrivacy2.txt")));
		int m = Integer.parseInt(args[0]);
		double l = Double.parseDouble(args[1]);
		SimpleGraph<String, DefaultEdge> graph = new PanzarasaGraph(DefaultEdge.class);
		NeighborIndex<String, DefaultEdge> neighbord = new NeighborIndex<>(graph);
		long start = System.currentTimeMillis();
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
		Output result = AntiResolving.antiResolvingBasis(graph, floyd, 1, m);
		do{
			int graphSize = graph.vertexSet().size();
			double density = 2*(double)(graph.edgeSet().size())/(graphSize*(graphSize-1));
			System.out.println("Analyzing graph with "+graphSize+" vertices and "+graph.edgeSet().size()+" edges, and density "+density);
			//Output result = AntiResolvingPersistence.antiResolvingBasisPersistence(graph, floyd, 1, m);
			if (result.output.equals(STATE.UNKNOWN)){
				System.out.println("Unknown result, we continue");
				makeTheGraphMorePrivate(graph, neighbord);
				//removeHighDegreeVertex(graph, neighbord);
			}
			else if (result.output.equals(STATE.POSITIVE)){
				System.out.println("Positive result");
				if (result.size <= graphSize*l/100){
					System.out.println("antiresolvnig set small enough, we continue = "+result.size);
					//makeTheGraphMorePrivate(graph, neighbord);
					removeTheBasis(graph, neighbord, result.antiResolvingBasis);
					//removeHighDegreeVertex(graph, neighbord);
				}
				else{
					System.out.println("antiresolvnig set big enough, we stop. Size of the set is "+result.size);
					break;
				}
			}
			else{
				System.out.println("Negative result, we stop");
				break;
			}
			floyd = new FloydWarshallShortestPaths<>(graph);
			/*if (!AntiResolving.isAntiResolvingSet(floyd, result.antiResolvingBasis, graph, 1)){
				System.out.println("Recomputing the antiresolving set");
				result = AntiResolving.antiResolvingBasis(graph, floyd, 1, m);
			}*/
			long end = System.currentTimeMillis();
			System.out.println("This iteration took "+(end-start)/60000+" minutes");
			System.out.println("Worst case for finishing +"+((end-start)/60000)*graph.vertexSet().size());
			start = System.currentTimeMillis();
		}while (true);
		floyd = new FloydWarshallShortestPaths<>(graph);
		for (int k = 2; k < graph.vertexSet().size(); k++) {
			result = AntiResolving.antiResolvingBasis(graph, floyd, k, m);
			//Output result = AntiResolvingPersistence.antiResolvingBasisPersistence(graph, floyd, 1, m);
			if (result.output.equals(STATE.UNKNOWN)){
				System.out.println("Unknown result, we continue, k = "+k);
			}
			else if (result.output.equals(STATE.POSITIVE)){
				System.out.println("Positive result");
				if (result.size <= graph.vertexSet().size()*l/100){
					System.out.println("antiresolvnig set small enough, we stop = "+result.size+", k = "+k);
					break;
				}
				else{
					System.out.println("antiresolvnig set big enough, we continue. Size of the set is "+result.size+", k = "+k);
				}
			}
			else{
				System.out.println("Negative result, we continue");
			}
		}
	}

	private static void makeTheGraphMorePrivate(
			SimpleGraph<String, DefaultEdge> graph, NeighborIndex<String, DefaultEdge> neighbord) {
		int min = Integer.MAX_VALUE;
		TreeMap<Integer, List<String>> result = new TreeMap<>();
		List<String> tmp = null;
		for (String vertex : graph.vertexSet()){
			tmp = neighbord.neighborListOf(vertex);
			if (tmp.size() < min){
				min = tmp.size();
				result.put(min, new LinkedList<String>());
				result.get(min).add(vertex);
			}
			else if (tmp.size() == min){
				result.get(min).add(vertex);
			}
		}
		System.out.println(result.firstEntry().getValue().size()+" vertices, with degree "+min+", will be linked");
		String lastVertex = null;
		String firstVertex = null;
		for (String vertex : result.firstEntry().getValue()){
			if (lastVertex == null){
				firstVertex = vertex;
				lastVertex = vertex;
			}
			else{
				graph.addEdge(lastVertex, vertex);
				neighbord.edgeAdded(new GraphEdgeChangeEvent<String, DefaultEdge>(
						graph, GraphEdgeChangeEvent.EDGE_ADDED, graph.getEdge(lastVertex, vertex)));
				System.out.println("Edge added between "+lastVertex+" and "+vertex);
				lastVertex = vertex;
			}
		}
		graph.addEdge(lastVertex, firstVertex);
		neighbord.edgeAdded(new GraphEdgeChangeEvent<String, DefaultEdge>(
				graph, GraphEdgeChangeEvent.EDGE_ADDED, graph.getEdge(lastVertex, firstVertex)));
		System.out.println("Edge added between "+lastVertex+" and "+firstVertex);
	}
	
	private static void removeTheBasis(
			SimpleGraph<String, DefaultEdge> graph, NeighborIndex<String, DefaultEdge> neighbord, Set<String> basis) {
		for (String vertex : basis){
			System.out.println("Removing vertex "+vertex);
			graph.removeVertex(vertex);
			neighbord.vertexRemoved(new GraphVertexChangeEvent<>(graph, GraphVertexChangeEvent.VERTEX_REMOVED, vertex));
		}
	}

}
