package anonymization;

import org.apache.commons.math3.geometry.spherical.twod.Vertex;
import org.jgraph.graph.Edge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.UndirectedSubgraph;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KMatchAnonymizer {

	private static final String INPUT_PATH = "FSMalgo/FSMfiles/input/gspanInput";
	private static final String INPUT_PATH_GSD = "FSMfiles/input/gspanInput";
	private static final String OUTPUT_PATH = "FSMalgo/FSMfiles/output/gspanOutput";
	private static final String OUTPUT_PATH_GSD = "FSMfiles/output/gspanOutput";

	public static void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k) {

		UndirectedGraph<String, DefaultEdge> noIdGraph = new SimpleGraph<>(DefaultEdge.class);
		ArrayList<ArrayList<UndirectedSubgraph>> groupU = new ArrayList<>();

		//Generate G' by removing all identity information from G.
		//removing the name of each vertices, i.e. removing their identity
//		for (String s : graph.vertexSet()) {
//			s = "";
//		}

		noIdGraph = graph;

		//TODO Algorithm 5
		groupU=partitionAndCluster(noIdGraph,k);

		//for each group Ui do Algorithm 2

		for (ArrayList<UndirectedSubgraph> P: groupU ) {
			groupU.set(groupU.indexOf(P),graphAlignment(P));

		}


		//Replace each block Pij by its alignment block Pij' to obtain anonymized network G′′

		//For all crossing edges, perform edge-copy to obtain anonymized network G∗. (Algorithm 4)

		//Output G∗
	}

	////////////////////////Algorithm 5\\\\\\\\\\\\\\\\\\\\\\\\
	private static ArrayList<ArrayList<UndirectedSubgraph>> partitionAndCluster(UndirectedGraph<String, DefaultEdge> graph, int k) {


		ArrayList<ArrayList<UndirectedSubgraph>> groupU = new ArrayList<>();
		//UndirectedGraph<String, DefaultEdge> fSubGraph = new SimpleGraph<String, DefaultEdge>();

		while (!graph.edgeSet().isEmpty()) {
			groupU = frequentSubgraphMining(groupU, graph, k);

		}
		return groupU;

	}

	private static ArrayList<ArrayList<UndirectedSubgraph>> frequentSubgraphMining(ArrayList<ArrayList<UndirectedSubgraph>> groupU, UndirectedGraph<String, DefaultEdge> graph, int k) {

		ArrayList<UndirectedSubgraph> blockP = new ArrayList<>();
		File inputFile = new File(INPUT_PATH);
		//first part of the algorithm 5

		append(inputFile, "t # 0");

		for (String v : graph.vertexSet()) {
			append(inputFile,"v "+ v + " 0");
		}
		for (DefaultEdge e : graph.edgeSet()){
			append(inputFile,"e "+ graph.getEdgeSource(e) + " " + graph.getEdgeTarget(e) + " 0");
		}

		try {
			Process p = Runtime.getRuntime().exec("java -jar FSMalgo/gsd.jar -d "+ INPUT_PATH_GSD +  " -r "+OUTPUT_PATH_GSD+" -s 3");
			int result = p.waitFor();
			if (result != 0) {
				System.out.println("Process failed with status: " + result);
			}
		}catch (Exception e){
			System.out.println(" process not read"+e);
		}

		return groupU;
	}


	////////////////////////Algorithm 2\\\\\\\\\\\\\\\\\\\\\\\\


	private static ArrayList<UndirectedSubgraph> graphAlignment(ArrayList<UndirectedSubgraph> P) {

		Map<Vertex,Vertex> AVT;
		//call to Algorithm 3
		AVT = constructAVT(P);


		return P;
	}
	////////////////////////Algorithm 3\\\\\\\\\\\\\\\\\\\\\\\\

	private static Map<Vertex,Vertex> constructAVT(ArrayList<UndirectedSubgraph> P) {
		Map<Vertex,Vertex> AVT = new HashMap<>();

		return AVT;


	}

	////////////////////////Utility methods \\\\\\\\\\\\\\\\\\\\\\\\

	//append to a file
	public static void append(File aFile, String content) {
		try {
			PrintStream p = new PrintStream(new BufferedOutputStream(new FileOutputStream(aFile, true)));
			p.println(content);
			p.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(aFile);
		}
	}


	
	public static void main(String [] args) {
		
		// Create graph of Fig. 4 in the k-automorphism paper
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		for (int i = 1; i < 11; i++)
			graph.addVertex(i+"");
		
		graph.addEdge("1", "2");
		graph.addEdge("1", "4");
		graph.addEdge("1", "6");
		graph.addEdge("2", "3");
		graph.addEdge("3", "4");
		graph.addEdge("4", "5");
		graph.addEdge("5", "6");
		graph.addEdge("6", "7");
		graph.addEdge("7", "8");
		graph.addEdge("7", "9");
		graph.addEdge("7", "10");
		graph.addEdge("8", "9");
		graph.addEdge("8", "10");
		
		int origEdgeCount = graph.edgeSet().size();

		ArrayList<ArrayList<UndirectedSubgraph>> groupU = new ArrayList<>();
		frequentSubgraphMining(groupU,graph,2);

		// Apply the method with k=2
		//anonymizeGraph(graph, 2);
		
		// Report effect of anonymization on the graph
		//System.out.println("Number of edge modifications: " + Math.abs(graph.edgeSet().size() - origEdgeCount));
		
	}

}
