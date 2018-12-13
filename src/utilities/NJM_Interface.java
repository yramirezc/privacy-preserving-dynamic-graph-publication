package utilities;

import java.text.DecimalFormat;
import java.util.*;
import org.jgraph.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import Jama.*; 

public interface NJM_Interface {

	public void NJM_Graph();
	public void NJM_Graph(int nodenumber,int edgenumber);
	public void NJM_Graph(SimpleGraph<String, DefaultEdge> simple_graph);
	public int getNodesNum();
	
	public void setNodesNum(int nodesNum);
	public int getEdgesNum();
	public Set<Set<Integer>> getEdgeSet();
	public void setEdgesNum(int edgesNum);
	public void setGraph(int nodeNum,int edgeNum);
	public ArrayList<String> getNodeSet();
	public void setNodeSet(Set<String> set);
	/// ************************************** Add Edge (int,int) *************************************** ///
	public void addEdge(int node1,int node2);
	public void addEdge(String v1,String v2);

	public NJM_Graph completeGraph (int nodeNum);
	public NJM_Graph pathGraph(int nodeNum);
	public NJM_Graph cycleGraph(int nodeNum);
	public NJM_Graph starGraph(int nodeNum);
	public int degreeOfNode(int node);
	
	                       /// *** Degree Sequence *** ///
	public int[] degreeSeq();
	
	                       /// *** Degree Distribution *** ///

	public double[] degreeDistribution();
	
	                       /// *** Join Degree *** ///

	public HashMap<Set<Integer>,Integer>  jointdegreeSeq();
		   
	/// *******************************  Should be checked ***************************** ///
	public HashMap<Set<Integer>,Double> joinDegreeDist();
		
	/// ********************************* Neighbors  Set ********************************* ///
	                       /// *** Neighbors of one Node *** ///
	public Set<Integer> neighbors(int node);
	       
	                       /// *** Neighbors of the set of nodes ***///
	                       
	public Set<Integer> neighbors(Set<Integer> nodeSet);
		   
	/// ********************************* Distance Metrics  ********************************* ///
	                       /// *** Distance Between two nodes ***///
	public int distance(int node1,int node2);
		
	                     /// *** Distance between Node 1 and all other nodes with number of node is upper *** /// 
	public void allDistanceFromNode(int node);
		
	public void allDistance();
	
	/// ********************************* Paths metrics  ********************************* ///
	public int numberOfShPath(int node1,int node2);
	    	
	    	
	public double[] eigenValue();
		   
	/// ********************************* Centrality measures ***************************************** ///
	public double betweennes(int node);
	
	public double graphCentrality(int node);
	
	public int stressCentrality(int node);
	
	/// ****************************************** Clustring Coefficient ***************************************** ///
	public double clusterCo(int node);	public double globalClustrinCo();

	/// ********************************* Graph Presentation  ********************************* ///
	public void showGraph();

/* End Of File */}
