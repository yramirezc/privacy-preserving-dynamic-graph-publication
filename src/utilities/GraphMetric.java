package utilities;

import java.util.*;

public interface GraphMetric {
	
	public void Graph(int nodenumber,int edgenumber);
	public int getNodesNum();
	public void setNodesNum(int nodesNum); 
	public int getEdgesNum();
	public void setEdgesNum(int edgesNum);
	public void setGraph(int nodeNum,int edgeNum);
	
	/// *** Generate some special graphs *** ///
	public NajGraph completeGraph (int nodeNum);
	public NajGraph pathGraph(int nodeNum);
	public NajGraph cycleGraph(int nodeNum);
	public NajGraph starGraph(int nodeNum);
	
	/// *** Calculate some properties of a graph *** ///
	public int degreeOfNode(int node);
	public int[] degreeSeq();
	public double[] degreeDistribution();
	public HashMap<Set<Integer>,Integer>  jointdegreeSeq();
	public HashMap<Set<Integer>,Double> joinDegreeDist();
	public Set<Integer> neighbors(int node);
	public Set<Integer> neighbors(Set<Integer> nodeSet);
	public int distance(int node1,int node2);
	public void allDistanceFromNode(int node);
	public void allDistance();
	public int numberOfShPath(int node1,int node2);
	
	/// *** Graph metrics *** ///
	public double betweennes(int node);
	public double closenees(int node);
	public double graphCentrality(int node);
	public int stressCentrality(int node);
	public double clusterCo(int node);
	public double globalClustrinCo();
	
	/// *** Some Distance *** ///
	public double k_l_divergenz(double x,double y);
	public double k_l_metric(double[] X ,double[] Y);
	public double euclid_distance(double x,double y);
	public double euclid_metric(double[] X,double[] Y);
	public double innerProduct(double[] X,double[] Y);
	public double cosine(double[] X,double[] Y);
	
	public void showGraph();
	public void randomPermutation(int n, int perm[]);
	public Set<Integer> intersection(Set<Integer> A , Set<Integer> B);
	public Set<Integer> subtraction(Set<Integer> A , Set<Integer> B);
	public Set<Integer> SetThePair(int node1,int node2);

	/// *** Distance between two graphs *** ///
	public int nodeNumDiff(NajGraph g1,NajGraph g2);
	public int edgeNumDiff(NajGraph g1,NajGraph g2);
	public int edgeDiffrence(NajGraph g1,NajGraph g2);
	public double degDiffrence(NajGraph g1,NajGraph g2);
	public double joindeg_K_L_distance(NajGraph g1,NajGraph g2);
	public double globalClustrinDiffrence(NajGraph g1,NajGraph g2);

}
