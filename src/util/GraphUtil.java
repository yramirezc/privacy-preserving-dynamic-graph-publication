package util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import anonymization.BaseGraph;
import anonymization.method2.GraphTwo;

public class GraphUtil {

	public static int[][] transformIntoAdjacencyMatrix(
			UndirectedGraph<String, DefaultEdge> graph) {
		Set<String> vertices = graph.vertexSet();
		int[][] matrix = new int[vertices.size()][vertices.size()];
		for (String v1 : vertices) {
			int v1Int = Integer.parseInt(v1);
			for (String v2 : vertices) {
				int v2Int = Integer.parseInt(v2);
				if (graph.containsEdge(v1,  v2)) matrix[v1Int][v2Int] = 1;
				else matrix[v1Int][v2Int] = 0;
			}
		}
		return matrix;
	}
	
	public static GraphTwo transformIntoGraphTwo(
			UndirectedGraph<String, DefaultEdge> graph) {
		Set<String> vertices = graph.vertexSet();
		GraphTwo result = new GraphTwo(graph.vertexSet().size());
		for (String v1 : vertices) {
			int v1Int = Integer.parseInt(v1);
			for (String v2 : vertices) {
				int v2Int = Integer.parseInt(v2);
				if (graph.containsEdge(v1,  v2)) result.addEdge(v1Int, v2Int);
			}
		}
		return result;
	}
	
	public static UndirectedGraph<String, DefaultEdge>  transformIntoJGraphT(BaseGraph graph) {
		return transformIntoJGraphT(graph.getArcs());
	}
	
	public static UndirectedGraph<String, DefaultEdge>  transformIntoJGraphT(int[][] matrix) {
		UndirectedGraph<String, DefaultEdge> result = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		for (int i = 0; i < matrix.length; i++) {
			result.addVertex(i+"");
		}
		for (int i = 0; i < matrix.length; i++) {	
			for (int j = i+1; j < matrix.length; j++) {
				if (matrix[i][j] == 1)
					result.addEdge(i+"", j+"");
			}
		}
		return result;
	}
	
	
	/**
	 * judge whether there exist end-vertex when given a matrix
	 * @param arcs 
	 * @return 
	 */
	public static boolean hasDegreeOfOnePoint(int[][] arcs){
		int vexnum = arcs[0].length;
		for(int i=0;i<vexnum;i++){
			int temp = 0 ;
			for(int j=0;j<vexnum;j++){
				if(i==j){
					continue;
				}
				temp +=arcs[i][j];
			}
			if(temp==1){
				return true ;
			}
		}
		return false ;
	}

	//=============This is my method to deal with end-vertex- start-==========================================================//
	/**
	 * 
	 * @param arcs
	 */
	public static int handleDegreeOfOnePoint(int[][] arcs){
		int vexnum = arcs[0].length;
		int result = 0;
		for(int m=0;m<vexnum;m++){
        	int value =0;
        	for(int n=0;n<vexnum;n++){
        		if(m==n){
        			continue;
        		}
        		value +=arcs[m][n];
        	}
        	if(value==1){
        		result++;
        		tradeOneVertex(arcs,m);
        	}
        }
		return result;
	}

	/**
	 * @param arcs 
	 * @param u    
	 */
	public static void tradeOneVertex(int[][] arcs,int u){
		//1 find the only neighbor of this end-vertex ,v
		int v ;
		int[] vs = getNextVertex(arcs, u);
		v = vs[0];
		if(vs.length>1){
			throw new RuntimeException("this node is not end-vertex");
		}
		
		//2,find out the neighbor of v, w
		int[] nextV = getNextVertex(arcs, v);
		
		//3 find out w with highest degree
		int maxW = 0;
		int edges = 0;
		for(int i=0;i<nextV.length;i++){
			int[] nextW = getNextVertex(arcs,nextV[i]);
			if(nextW.length>edges){
				edges = nextW.length;
				maxW = nextV[i];
			}
		}
		
		//connect end-vertex and the neighbor of end-vertex's neighbor who has the highest degree
		
		arcs[u][maxW] = 1 ;
		arcs[maxW][u] = 1 ;
	}

	/**
	 * get the neighbor of u
	 * @param arcs 
	 * @param u    
	 * @return
	 */
	public static int[] getNextVertex(int[][] arcs,int u){
		String temp = "";
		int[] vertex ;
		for(int i=0;i<arcs[0].length;i++){
			if(arcs[u][i]==1){
				if(u!=i){
					temp += i+",";
				}
			}
		}
		if("".equals(temp)){
			throw new RuntimeException("this is isolated node");
		}
		temp = temp.substring(0,temp.length()-1);
		if(temp.contains(",")){
			String[] strs = temp.split(",");
			vertex = new int[strs.length];
			for(int i=0;i<strs.length;i++){
				vertex[i] = Integer.parseInt(strs[i]);
			}
		}else{
			vertex = new int[1];
			vertex[0] = Integer.parseInt(temp);
		}
		return vertex;
	}

	public static int getRandomNum(int num) throws NoSuchAlgorithmException{
		SecureRandom rng = new SecureRandom();
		return rng.nextInt(num);
	//	return (int) Math.round(Math.random()*num);
	}

	public static double computeDensity(
			UndirectedGraph<String, DefaultEdge> graph) {
		int e = graph.edgeSet().size();
		int n = graph.vertexSet().size();
		return (double)(2*e)/(n*(n-1));
	}

	public static double computeDensity(
			BaseGraph graph) {
		int e = graph.getNumberOfEdges();
		int n = graph.getVexnum();
		return (double)(2*e)/(n*(n-1));
	}
	
	//add an edge between i and j
	public static void addEdge(int[][] arcs,int i, int j) {
		if (i == j){
			//check whether it is the same node
			throw new RuntimeException("We do not allow loops ("+i+" = "+j+")");
		}else{
			arcs[i][j] = 1;		
			arcs[j][i] = 1;
		}
	}

	public static int[][] getRandomGraph(int vexnum, int edgenum, boolean b) {
		SimpleGraph<String, DefaultEdge> graph = null;
		ConnectivityInspector<String, DefaultEdge> connectivity = null;
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
			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(vexnum, edgenum);
			generator.generateGraph(graph, vertexFactory, null);
			connectivity = new ConnectivityInspector<>(graph);
		}while(!connectivity.isGraphConnected());
		return transformIntoAdjacencyMatrix(graph);
	}
	
	/**
	 * combine two matrix to one
	 * @param arcs1 the first matrix
	 * @param arcs2  the second matrix
	 * @return
	 */
	public static int[][] combineGraph(int[][] arcs1,int[][] arcs2){
		int length1 = arcs1.length;
		int length2 = arcs2.length;
		int length = length1 + length2;
		int[][] arcs = new int[length][length];
		
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				if(i<length1 && j<length1){
					arcs[i][j] = arcs1[i][j];
				}else if(i>=length1 && j>=length1){
					arcs[i][j] = arcs2[i-length1][j-length1];
				}else{
					arcs[i][j] = 0;
				}
			}						
		}
		return arcs  ;
	}

	public static int computeDiameter(
			UndirectedGraph<String, DefaultEdge> graph,
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		int longest = 0;
		for (String v1 : graph.vertexSet()){
			for (String v2 : graph.vertexSet()){
				int length = (int)floyd.shortestDistance(v1, v2);
				if (length > longest) longest = length;
			}
		}
		return longest;
	}

	public static SimpleGraph<String, DefaultEdge> cloneGraph(
			UndirectedGraph<String, DefaultEdge> graph) {
		SimpleGraph<String, DefaultEdge> result = new SimpleGraph<>(DefaultEdge.class);
		for (String v : graph.vertexSet()){
			result.addVertex(v);
		}
		for (String v1 : graph.vertexSet()){
			for (String v2 : graph.vertexSet()){
				if (graph.containsEdge(v1, v2))
					result.addEdge(v1, v2);
			}
		}
		return result;
	}

	public static void addRandomEdges(int toAdd, SimpleGraph<String, DefaultEdge> graph) {
		SecureRandom random = new SecureRandom();
		int numberOfEdges = graph.edgeSet().size();
		int vexNum = graph.vertexSet().size();
		if (numberOfEdges+toAdd > vexNum*(vexNum-1)/2){
			throw new RuntimeException("no sufficient edges");
		}
		int counter = 0;
		Hashtable<String, ArrayList<String>> availableEdges = new Hashtable<>();
		String[] vertices = new String[graph.vertexSet().size()];
		int index =  0;
		for (String v : graph.vertexSet()){
			vertices[index++] = v;
		}
		for (int i = 0; i < vertices.length-1; i++){
			String v1 = vertices[i];
			for (int j = i+1; j < vertices.length; j++){
				String v2 = vertices[j];
				if (graph.containsEdge(v1, v2)) continue;
				if (availableEdges.containsKey(v1)){
					availableEdges.get(v1).add(v2);
				}
				else{
					ArrayList<String> tmp = new ArrayList<>();
					tmp.add(v2);
					availableEdges.put(v1, tmp);
				}
			}
		}
		String[] keys = new String[availableEdges.size()];
		index =  0;
		for (String key : availableEdges.keySet())
			keys[index++] = key;
		for (int i = 0; i < toAdd; i++){
			int v1 = random.nextInt(keys.length);
			ArrayList<String> candidates = availableEdges.get(keys[v1]);
			int v2 = random.nextInt(candidates.size());
			graph.addEdge(keys[v1], candidates.get(v2));
			//next we remove this edge
			candidates.remove(v2);
			if (candidates.isEmpty()){
				availableEdges.remove(keys[v1]);
				//because we remove a pair we will need to recompute
				keys = new String[availableEdges.size()];
				index =  0;
				for (String key : availableEdges.keySet())
					keys[index++] = key;
			}
		}
		/*while (counter < toAdd){
			int v1 = random.nextInt(vexNum);
			int v2 = random.nextInt(vexNum);
			if (v1== v2 || graph.containsEdge(v1+"", v2+"")) continue;
			graph.addEdge(v1+"", v2+"");
			counter++;
		}*/
	}

}
