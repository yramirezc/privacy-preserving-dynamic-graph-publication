package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DebugGraphics;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import anonymization.BaseGraph;
import anonymization.method2.GraphTwo;
import net.vivin.GenericTreeNode;

public class GraphUtil {
	
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	static class VertexPair implements Comparable<VertexPair> {
		
		public String source, dest;
		
		public VertexPair(String src, String dst) {
			if (src.compareTo(dst) <= 0) {
				source = src;
				dest = dst;
			}
			else {
				source = dst;
				dest = src;
			}
		}

		@Override
		public int compareTo(VertexPair e) {
			if (this.source.compareTo(e.source) == 0)
				return this.dest.compareTo(e.dest);
			else
				return this.source.compareTo(e.source);
		}
		
		public boolean incident(String v) {
			return ((v.equals(source)) || (v.equals(dest)));
		}
		
		@Override
		public String toString() {
			return "(" + source + "," + dest + ")";
		}
		
	}

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

	public static int computeDiameter(UndirectedGraph<String, DefaultEdge> graph,
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		int longest = 0;
		for (String v1 : graph.vertexSet()) {
			for (String v2 : graph.vertexSet())
				if (v1 != v2) {
					int length = (int)floyd.shortestDistance(v1, v2);
					if (length > longest)
						longest = length;
				}
		}
		return longest;
	}
	
	public static int computeRadius(UndirectedGraph<String, DefaultEdge> graph,
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		int minEcc = Integer.MAX_VALUE;
		for (String v1 : graph.vertexSet()) {
			int eccentricity = 0;
			for (String v2 : graph.vertexSet())
				if (v1 != v2) {
					int length = (int)floyd.shortestDistance(v1, v2);
					if (length > eccentricity)
						eccentricity = length;
				}
			if (eccentricity < minEcc)
				minEcc = eccentricity;
		}
		return minEcc;
	}
	
	public static int computeEffectiveDiameter(UndirectedGraph<String, DefaultEdge> graph,
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
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
	
	public static double computeDistributionKLUtility(UndirectedGraph<String, DefaultEdge> origGraph, 
			UndirectedGraph<String, DefaultEdge> modifGraph, DistributionComputer distComp) {
		
		Map<Integer, Double> distOrig = distComp.computeDistributionAsProbabilities(origGraph);
		Map<Integer, Double> distModif = distComp.computeDistributionAsProbabilities(modifGraph);
		
		double klDiv = 0d;
		double entropyOrig = 0d;
		
		for (int vo : distOrig.keySet()) {
			entropyOrig += -(distOrig.get(vo) * (Math.log(distOrig.get(vo)) / Math.log(2d)));
			if (distModif.containsKey(vo)) 
				klDiv += distOrig.get(vo) * (Math.log(distOrig.get(vo) / distModif.get(vo)) / Math.log(2d));
			else
				klDiv += distOrig.get(vo) * (Math.log(distOrig.get(vo) / distModif.get(-1)) / Math.log(2d));
		}
		
		//return klDiv;
		return (entropyOrig + klDiv) / entropyOrig;
		
	}
	
	public static double computeDegreeDistributionCosineUtility(UndirectedGraph<String, DefaultEdge> origGraph, 
			UndirectedGraph<String, DefaultEdge> modifGraph) {
		
		double cosSum = 0d, norm1Sum = 0d, norm2Sum = 0d;
		
		for (String v : origGraph.vertexSet()) {
			int deg1 = origGraph.degreeOf(v);
			norm1Sum += deg1 * deg1;
			if (modifGraph.containsVertex(v)) {
				int deg2 = modifGraph.degreeOf(v);
				norm2Sum += deg2 * deg2;
				cosSum += deg1 * deg2;
			}
		}
		
		return cosSum / (Math.sqrt(norm1Sum) * Math.sqrt(norm2Sum));
	}
	
	public static double computeDegreeDistributionCosineUtility1(UndirectedGraph<String, DefaultEdge> origGraph, 
			UndirectedGraph<String, DefaultEdge> modifGraph) {
		
		double cosSum = 0d, norm1Sum = 0d, norm2Sum = 0d;
		
		ArrayList<Integer> degreesOrig = new ArrayList<>();
		for (String v : origGraph.vertexSet())
			degreesOrig.add(origGraph.degreeOf(v));
		Collections.sort(degreesOrig, Collections.reverseOrder());
		
		ArrayList<Integer> degreesModif = new ArrayList<>();
		for (String v : modifGraph.vertexSet())
			degreesModif.add(modifGraph.degreeOf(v));
		Collections.sort(degreesModif, Collections.reverseOrder());
		
		if (degreesOrig.size() < degreesModif.size())
			for (int i = degreesOrig.size(); i < degreesModif.size(); i++)
				degreesOrig.add(0);
		else if (degreesOrig.size() > degreesModif.size())
			for (int i = degreesModif.size(); i < degreesOrig.size(); i++)
				degreesModif.add(0);
		
		for (int i = 0; i < degreesOrig.size(); i++) {
			int deg1 = degreesOrig.get(i);
			norm1Sum += deg1 * deg1;	
			int deg2 = degreesModif.get(i);
			norm2Sum += deg2 * deg2;
			cosSum += deg1 * deg2;
			
		}
		
		return cosSum / (Math.sqrt(norm1Sum) * Math.sqrt(norm2Sum));
	}
	
	public static double computeGlobalClusteringCoefficient(UndirectedGraph<String, DefaultEdge> graph) {
		int connTriplesCount = 0, closedTriplesCount = 0;
		
		ArrayList<String> vertexList = new ArrayList<>(graph.vertexSet()); 
		for (int i = 0; i < vertexList.size() - 1; i++)
			for (int k = i + 1; k < vertexList.size(); k++) {
				Set<String> commonNeighbors = new HashSet<>(Graphs.neighborListOf(graph, vertexList.get(i)));
				commonNeighbors.retainAll(Graphs.neighborListOf(graph, vertexList.get(k)));
				commonNeighbors.remove(vertexList.get(i));
				commonNeighbors.remove(vertexList.get(k));
				if (graph.containsEdge(vertexList.get(i), vertexList.get(k))) {
					connTriplesCount += 3 * commonNeighbors.size();
					closedTriplesCount += 3 * commonNeighbors.size();
				}
				else
					connTriplesCount += commonNeighbors.size();			
			}
				
		
//		for (String v1 : graph.vertexSet())
//			for (String v2 : graph.vertexSet())
//				if (!v2.equals(v1)) 
//					for (String v3 : graph.vertexSet())
//						if (!v3.equals(v1) && !v3.equals(v2) && graph.containsEdge(v1, v2) && graph.containsEdge(v2, v3)) {
//							connTriplesCount++;
//							if (graph.containsEdge(v1, v3))
//								closedTriplesCount++;
//						}
		return (double) closedTriplesCount / (double) connTriplesCount;
	}
	
	public static double computeAvgLocalClusteringCoefficient(UndirectedGraph<String, DefaultEdge> graph) {
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
	
	public static DefaultDirectedGraph<String, DefaultEdge> createDirectedClone(UndirectedGraph<String, DefaultEdge> graph) {
		DefaultDirectedGraph<String, DefaultEdge> result = new DefaultDirectedGraph<>(DefaultEdge.class);
		for (String v : graph.vertexSet()){
			result.addVertex(v);
		}
		for (String v1 : graph.vertexSet()){
			for (String v2 : graph.vertexSet()){
				if (graph.containsEdge(v1, v2)) {
					result.addEdge(v1, v2);
					result.addEdge(v2, v1);
				}
			}
		}
		return result;
	}

	public static void addRandomEdges(int toAdd, SimpleGraph<String, DefaultEdge> graph) {
		SecureRandom random = new SecureRandom();
		int numberOfEdges = graph.edgeSet().size();
		int vexNum = graph.vertexSet().size();
		if (numberOfEdges+toAdd > vexNum*(vexNum-1)/2){
			throw new RuntimeException("No sufficient missing edges");
		}
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
	
	public static void removeRandomEdges(int toRemove, SimpleGraph<String, DefaultEdge> graph) {
		if (toRemove > graph.edgeSet().size())
			throw new RuntimeException("No sufficient removable edges");
		SecureRandom random = new SecureRandom();
		ArrayList<DefaultEdge> edgeList = new ArrayList<>(graph.edgeSet());
		for (int i = 0; i < toRemove; i++) {
			int orderEdgeToDelete = random.nextInt(edgeList.size());
			graph.removeEdge(edgeList.get(orderEdgeToDelete));
			edgeList.remove(orderEdgeToDelete);
		}
	}
	
	public static void flipRandomEdges(int flipCount, SimpleGraph<String, DefaultEdge> graph) {
		SecureRandom random = new SecureRandom();
		Set<VertexPair> flippedVertexPairs = new TreeSet<>();
		List<String> vertexList = new ArrayList<>(graph.vertexSet()); 
		for (int i = 0; i < flipCount; i++) {
			String v1 = vertexList.get(random.nextInt(vertexList.size()));
			String v2 = vertexList.get(random.nextInt(vertexList.size()));
			VertexPair vp = new VertexPair(v1, v2);
			while (v1.equals(v2) || flippedVertexPairs.contains(vp)) {
				v1 = vertexList.get(random.nextInt(vertexList.size()));
				v2 = vertexList.get(random.nextInt(vertexList.size()));
				vp = new VertexPair(v1, v2);
			}
			if (graph.containsEdge(v1, v2))
				graph.removeEdge(v1, v2);
			else
				graph.addEdge(v1, v2);
		}
	}

	public static UndirectedGraph<String, DefaultEdge> transformRealSocNetIntoOurFormat(
			UndirectedGraph<String, DefaultEdge> panzarasaGraph, int numberOfattackers, 
			Set<String> verticesToKeep) {
		UndirectedGraph<String, DefaultEdge> result = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		SecureRandom random = new SecureRandom();
		int[] index = new int[verticesToKeep.size()];
		for (int i = 0; i < index.length; i++){
			index[i] = i+numberOfattackers;
		}
		HashMap<String, String> randomMapping = new HashMap<>();
		int lastIndex = index.length;
		for (String v : verticesToKeep){
			int chosen = random.nextInt(lastIndex);
			randomMapping.put(v, index[chosen]+"");
			result.addVertex(index[chosen]+"");
			index[chosen] = index[lastIndex-1];
			lastIndex--;
			
		}
		for (String v1 : verticesToKeep){
			for (String v2 : verticesToKeep){
				//if (v1.equals(v2)) continue;
				if (panzarasaGraph.containsEdge(v1,  v2))
					result.addEdge(randomMapping.get(v1), randomMapping.get(v2));
			}
		}
		return result;
	}
	
	public static UndirectedGraph<String, DefaultEdge> sampleRealSocialNetwork(
			UndirectedGraph<String, DefaultEdge> realSocNetGraph, int numberOfattackers, 
			Set<String> verticesToKeep, int upperBoundVertsToKeep) {
		
		UndirectedGraph<String, DefaultEdge> result = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		SecureRandom random = new SecureRandom();
		
		HashSet<String> sampledVertsToKeep = null;
		if (verticesToKeep.size() > upperBoundVertsToKeep) {
			ArrayList<String> vertsToKeep = new ArrayList<>(verticesToKeep);
			Collections.shuffle(vertsToKeep, random);
			sampledVertsToKeep = new HashSet<>(vertsToKeep.subList(0, upperBoundVertsToKeep));
		}
		else
			sampledVertsToKeep = new HashSet<>(verticesToKeep);
		
		int[] index = new int[sampledVertsToKeep.size()];
		for (int i = 0; i < index.length; i++){
			index[i] = i+numberOfattackers;
		}
		HashMap<String, String> randomMapping = new HashMap<>();
		int lastIndex = index.length;
		for (String v : sampledVertsToKeep) {
			int chosen = random.nextInt(lastIndex);
			randomMapping.put(v, index[chosen]+"");
			result.addVertex(index[chosen]+"");
			index[chosen] = index[lastIndex-1];
			lastIndex--;
		}
		
		for (String v1 : sampledVertsToKeep){
			for (String v2 : sampledVertsToKeep){
				if (realSocNetGraph.containsEdge(v1, v2))
					result.addEdge(randomMapping.get(v1), randomMapping.get(v2));
			}
		}
		return result;
	}
	
	public static void outputSNAPFormat(UndirectedGraph<String, DefaultEdge> graph, String writerName) throws IOException {
		ArrayList<String> vertList = new ArrayList<>(graph.vertexSet());
		Writer graphWriter = new FileWriter(writerName, false);
		for (int i = 0; i < vertList.size() - 1; i++)
			for (int j = i + 1; j < vertList.size(); j++)
				if (graph.containsEdge(vertList.get(i), vertList.get(j)))
					graphWriter.append(vertList.get(i).toString()+"\t"+vertList.get(j).toString()+NEW_LINE);
		graphWriter.flush();
		graphWriter.close();
	}
	
	public static UndirectedGraph<String, DefaultEdge> loadSNAPFormat(String readerName) {
		try {
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
			BufferedReader reader = new BufferedReader(new FileReader(readerName));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				String[] vertices = line.split("\t");  
				if (vertices.length == 2) {
					if (!graph.containsVertex(vertices[0]))
						graph.addVertex(vertices[0]); 
					if (!graph.containsVertex(vertices[1]))
						graph.addVertex(vertices[1]);
					graph.addEdge(vertices[0], vertices[1]);
				}
			}
			reader.close();
			return graph;
		}
		catch (IOException ioEx) {
			return new SimpleGraph<>(DefaultEdge.class);
		}
	}
	
	public static UndirectedGraph<String, DefaultEdge> inducedSubgraph(UndirectedGraph<String, DefaultEdge> graph, Set<String> subset) {
		UndirectedGraph<String, DefaultEdge> subgraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		for (String v : subset)
			if (graph.containsVertex(v))
				subgraph.addVertex(v);
		for (String v1 : subgraph.vertexSet())
			for (String v2 : subgraph.vertexSet())
				if (!v1.equals(v2) && graph.containsEdge(v1, v2))
					subgraph.addEdge(v1, v2);
		return subgraph;
	}
	
	public static int minDegree(UndirectedGraph<String, DefaultEdge> graph) {
		int minDegree = graph.vertexSet().size();
		for (String v : graph.vertexSet())
			if (graph.degreeOf(v) < minDegree)
				minDegree = graph.degreeOf(v);
		return minDegree;
	}
	
	public static int maxDegree(UndirectedGraph<String, DefaultEdge> graph) {
		int maxDegree = -1;
		for (String v : graph.vertexSet())
			if (graph.degreeOf(v) > maxDegree)
				maxDegree = graph.degreeOf(v);
		return maxDegree;
	}
	
	public static int[] getFingerprintDegrees(UndirectedGraph<String, DefaultEdge> graph, List<String> vertexSet) {
		int[] fingerprint = new int[vertexSet.size()];
		for (int i = 0; i < vertexSet.size(); i++) 
			fingerprint[i] = graph.degreeOf(vertexSet.get(i));
		return fingerprint;
	}
	
	public static boolean[][] getFingerprintLinks(UndirectedGraph<String, DefaultEdge> graph, List<String> vertexSet) {
		boolean[][] fingerprint = new boolean[vertexSet.size()][vertexSet.size()];
		for (int i = 0; i < vertexSet.size(); i++)
			for (int j = 0; j < vertexSet.size(); j++) 
				if (graph.containsEdge(vertexSet.get(i), vertexSet.get(j)))
					fingerprint[i][j] = true;
				else 
					fingerprint[i][j] = false;
		return fingerprint;
	}
	
	// This is a replica of methods that were oiriginally in Statistics.java
	
	public static List<String[]> getPotentialAttackerCandidates(int[] fingerprintDegrees, 
			boolean[][] fingerprintLinks, UndirectedGraph<String, DefaultEdge> graph) {
		GenericTreeNode<String> root = new GenericTreeNode<>("root");
		List<GenericTreeNode<String>> currentLevel = new LinkedList<>();
		List<GenericTreeNode<String>> nextLevel = new LinkedList<>();
		for (int i = 0; i < fingerprintDegrees.length; i++) {
			nextLevel = new LinkedList<>();
			for (String vertex : graph.vertexSet()) {
				int degree = graph.degreeOf(vertex);
				if (degree == fingerprintDegrees[i]){
					if (i == 0){
						/*Trujillo- Feb 4, 2016
						 * At the beggining we just need to add the node as a child of the root*/
						GenericTreeNode<String> newChild = new GenericTreeNode<>(vertex);
						root.addChild(newChild);
						nextLevel.add(newChild);
					}
					else{
						/*Trujillo- Feb 4, 2016
						 * Now we iterate over the last level and add the new vertex if possible*/
						for (GenericTreeNode<String> lastVertex : currentLevel){
							boolean ok = true;
							GenericTreeNode<String> tmp = lastVertex;
							int pos = i-1;
							while (!tmp.equals(root)){
								//we first check whether the vertex has been already considered
								if (tmp.getData().equals(vertex)){
									//this happens because this vertex has been considered already here
									ok = false;
									break;
								}
								//we also check that the link is consistent with fingerprintLinks
								if (graph.containsEdge(vertex, tmp.getData()) && !fingerprintLinks[i][pos]){
									ok = false;
									break;
								}
								if (!graph.containsEdge(vertex, tmp.getData()) && fingerprintLinks[i][pos]){
									ok = false;
									break;
								}
								pos--;
								tmp = tmp.getParent();
							}
							if (ok){
								//we should add this vertex as a child
								tmp = new GenericTreeNode<>(vertex);
								lastVertex.addChild(tmp);
								nextLevel.add(tmp);
							}
						}
					}
				}
			}
			/*Trujillo- Feb 4, 2016
			 * Now we iterate over the current level to check whether a branch could not continue
			 * in which case we remove it completely*/
			currentLevel = nextLevel;
		}
		/*Trujillo- Feb 4, 2016
		 * Now we build subgraphs out of this candidates*/
		//return buildListOfGraphs(root, graph, fingerprintDegrees.length);
		return buildListOfCandidates(root, graph, fingerprintDegrees.length, fingerprintDegrees.length);
		
	}
	
	public static List<String[]> buildListOfCandidates(GenericTreeNode<String> root, 
			UndirectedGraph<String, DefaultEdge> graph, int pos, int size){
		List<String[]> result = new LinkedList<>();
		if (pos < 0) throw new RuntimeException();
		if (root.isALeaf()){
			if (pos > 0) return result;
			String[] candidates = new String[size];
			candidates[size-pos-1] = root.getData();
			result.add(candidates);
			return result;
		}
		for (GenericTreeNode<String> child : root.getChildren()){
			List<String[]> subcandidates = buildListOfCandidates(child, graph, pos-1, size);
			if (!root.isRoot()){
				for (String[] subcandidate : subcandidates) {
					//we add the node and its connections
					subcandidate[size-pos-1] = root.getData();
				}
			}
			result.addAll(subcandidates);
		}
		return result;
	}
	
	public static Set<String> greedyMaxIndependentSet(UndirectedGraph<String, DefaultEdge> graph, boolean workOnCopyOfGraph) {
		if (workOnCopyOfGraph)
			return greedyMaxIndependentSet(cloneGraph(graph));
		else 
			return greedyMaxIndependentSet(graph);
	}
	
	// This implementation modifies the graph received as parameter
	public static Set<String> greedyMaxIndependentSet(UndirectedGraph<String, DefaultEdge> graph) {
		
		boolean foundNonIsolated = false;
		
		// Main step, iteratively find minimum degree non-isolated vertex and remove all its neighbors
		do {
			foundNonIsolated = false;
			int minDegree = Integer.MAX_VALUE;
			String vertMinDegree = "";   // This will be updated at least in the first iteration, since minDegree was initialized with a value greater than any possible value in the graph
			for (String v : graph.vertexSet())
				if (graph.degreeOf(v) > 0 && graph.degreeOf(v) < minDegree) {
					foundNonIsolated = true;
					minDegree = graph.degreeOf(v);
					vertMinDegree = v;
				}
			if (foundNonIsolated) {
				List<String> neighbors = Graphs.neighborListOf(graph, vertMinDegree);
				for (String n : neighbors)
					graph.removeVertex(n);
			}	
		} while (foundNonIsolated);
		
		return graph.vertexSet();
	}
	
}

