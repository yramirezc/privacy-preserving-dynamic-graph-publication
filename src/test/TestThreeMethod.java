package test;
//This is to compare CPA and EPA on random graphs which are generated 
//by myself not Jgrapht.

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import anonymization.BaseGraph;
import attacks.AttackThreeMethod;
import attacks.AttackerUtil;

import test.AntiResolvingPersistence.STATE;
import util.EdgeConnectivity;
import util.GraphUtil;
import util.Statistics;


public class TestThreeMethod {
	

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		//number of vertices
		int verNum = Integer.parseInt(args[0]);
		//density of the graph
		double density = Double.parseDouble(args[1]);
		//number of graphs to be evaluated
		Writer outOriginal = new FileWriter("original-V-"+verNum+"-D-"+density+".DAT", true);
		Writer outCpa = new FileWriter("CPA-V-"+verNum+"-D-"+density+".DAT", true);
		Writer outEpa = new FileWriter("EPA-V-"+verNum+"-D-"+density+".DAT", true);
		Writer outCpaRandom = new FileWriter("CPARandom-V-"+verNum+"-D-"+density+".DAT", true);
		Writer outEpaRandom = new FileWriter("EPARandom-V-"+verNum+"-D-"+density+".DAT", true);
		System.out.println("Analyzing verNum is "+verNum+", density is "+density);
		int edgeNum = getEdgeNum(verNum,density);
		mainMethod(verNum, edgeNum, density, outOriginal, outCpa, outEpa, outCpaRandom, outEpaRandom);
		outOriginal.close();
		outCpa.close();
		outEpa.close();
		outCpaRandom.close();
		outEpaRandom.close();
	}
	/**
	 * According to the density, compute the number of edges
	 * @param vexnum
	 * @param density
	 * @return
	 */
	public static int getEdgeNum(int vexnum , double density){
		return (int)(density*vexnum*(vexnum-1)/2);
	}
	
	
	public static void mainMethod(int verNum,int edgeNum, double density,
			Writer outOriginal, Writer outCpa, Writer outEpa,
			Writer outCpaRandom, Writer outEpaRandom) throws NoSuchAlgorithmException, IOException{
		System.out.println("<======start  verNum:"+verNum+",density:"+
			density+",edgeNum:"+edgeNum+"===========================>");
		
		SecureRandom random = new SecureRandom(); 
		/*Trujillo- Jan 21, 2016
		 * This loops will iterate over all graphs, which are by the 
		 * way going to be generate within this loop and the analyzed*/
		for (int i = 0; i < 10000; i++){
			System.out.println("===============================================");
			UndirectedGraph<String, DefaultEdge> graph = null;
			ConnectivityInspector<String, DefaultEdge> connectivity = null;
			System.out.println("Looking for a connected random graph");
			do{
				VertexFactory<String> vertexFactory = new VertexFactory<String>(){
					int i = 0;
					@Override
					public String createVertex() {
						int result = i;
						i++;
						return result+"";
					}
					
				};
				graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
				RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(verNum, edgeNum);
				generator.generateGraph(graph, vertexFactory, null);
				connectivity = new ConnectivityInspector<>(graph);
			}while(!connectivity.isGraphConnected());
			System.out.println("A connected random graph with "+graph.vertexSet().size()+" vertices" +
					" and "+graph.edgeSet().size()+" edges was found");
			
			/*Trujillo- Jan 25, 2016
			 * Next, we compute (if we can) the value of k such that
			 * the graph is (k,1)-anonymous*/
			int[][] matrix = GraphUtil.transformIntoAdjacencyMatrix(graph);
			Statistics.printStatistics(outOriginal, graph, matrix, "original");
			BaseGraph graphTwo1Random = GraphUtil.transformIntoGraphTwo(graph);
			BaseGraph graphTwo2Random = GraphUtil.transformIntoGraphTwo(graph);

			/*Trujillo- Jan 25, 2016
			 * Next, we proceed to anonymize the graph.*/
			
			/*Trujillo- Jan 25, 2016
			 * Here we transform the graph such that it does
			 * not contain end vertices.*/
			int addedEdgeNumDegreeOne =0;
			while(GraphUtil.hasDegreeOfOnePoint(matrix)){				
				addedEdgeNumDegreeOne += GraphUtil.handleDegreeOfOnePoint(matrix);
			}
			System.out.println("Dealing with end-vertices for this graph adds " + addedEdgeNumDegreeOne + " edges.");
			
			/*Trujillo- Jan 26, 2016
			 * From now in we will use the new version of arc, which does not 
			 * contain end vertices*/
			
			graph = GraphUtil.transformIntoJGraphT(matrix);
			
			BaseGraph graphTwo1 = GraphUtil.transformIntoGraphTwo(graph);
			BaseGraph graphTwo2 = GraphUtil.transformIntoGraphTwo(graph);
			
			graphTwo1.getImprovedGraph(); //anonymizing through CPA
			
			int addedEdges = graphTwo1.getNumberOfEdges() - graph.edgeSet().size();
			
			graphTwo1Random.addRandomEdges(addedEdges+addedEdgeNumDegreeOne);
			
			System.out.println(addedEdges+" edges has been added in the graph using CPA.");
			
			UndirectedGraph<String, DefaultEdge> graphTwo1Back = GraphUtil.transformIntoJGraphT(graphTwo1);
			int[][] arcs1Back = GraphUtil.transformIntoAdjacencyMatrix(graphTwo1Back);
			Statistics.printStatistics(outCpa, graphTwo1Back, arcs1Back, "CPA");
			
			UndirectedGraph<String, DefaultEdge> graphTwo1BackRandom = GraphUtil.transformIntoJGraphT(graphTwo1Random);
			int[][] arcs1BackRandom = GraphUtil.transformIntoAdjacencyMatrix(graphTwo1BackRandom);
			
			Statistics.printStatistics(outCpaRandom, graphTwo1BackRandom, arcs1BackRandom, "CPA-Random");
		
		    //anonymizing by EPA
			graphTwo2.getImprovedGraph_one();
			
			addedEdges = graphTwo2.getNumberOfEdges() - graph.edgeSet().size();
			
			graphTwo2Random.addRandomEdges(addedEdges+addedEdgeNumDegreeOne);

			System.out.println(addedEdges+" edges has been added in the graph using EPA_bochuan.");
			
			UndirectedGraph<String, DefaultEdge> graphTwo2Back = GraphUtil.transformIntoJGraphT(graphTwo2);
			int[][] arcs2Back = GraphUtil.transformIntoAdjacencyMatrix(graphTwo2Back);
			Statistics.printStatistics(outEpa, graphTwo2Back, arcs2Back, "EPA");

			UndirectedGraph<String, DefaultEdge> graphTwo2BackRandom = GraphUtil.transformIntoJGraphT(graphTwo2Random);
			int[][] arcs2BackRandom = GraphUtil.transformIntoAdjacencyMatrix(graphTwo2BackRandom);
			
			Statistics.printStatistics(outEpaRandom, graphTwo2BackRandom, arcs2BackRandom, "EPA-Random");
		}
	}
	
}
