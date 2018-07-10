package utilities;

import java.util.*;


public class GraphDistance {
	
	
public static boolean compare_nodeSet(NJM_Graph g1,NJM_Graph g2){
		   int min_node=Math.min(g1.getNodesNum(), g2.getNodesNum());
			for(int v_int=0;v_int<min_node;++v_int){
				if(g1.nodeSet.get(v_int)!= g2.nodeSet.get(v_int))  return false;
				else continue;
			} 
return true;
}

public static int nodeNumDiff(NJM_Graph g1,NJM_Graph g2){
	   int d=g1.getNodesNum()-g2.getNodesNum();
return d;
}
public static int edgeNumDiff(NJM_Graph g1,NJM_Graph g2){
	    int d=Math.abs(g1.getEdgesNum()-g2.getEdgesNum());
return d;
}

public static int edgeDiffrence(NJM_Graph g1,NJM_Graph g2){
	   int d=0;
	   Set<Set<Integer>> g1_edge=g1.getEdgeSet();
	   Set<Set<Integer>> g2_edge=g2.getEdgeSet();
		
	   for(Set<Integer> pair:g1_edge) if(!g2_edge.contains(pair)) ++d; 
	   for(Set<Integer> pair:g2_edge) if(!g1_edge.contains(pair)) ++d;
return d;	   
}

public static double degDiffrence(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] d_g1=g1.degreeDistribution();
	   double[] d_g2=g2.degreeDistribution();
	   d=Utility.k_l_metric(d_g1, d_g2);
return d;
}
public static double joindeg_K_L_distance(NJM_Graph g1,NJM_Graph g2){
	   double k_l_distance=0;
	   HashMap<Set<Integer>,Double> d_g1=g1.joinDegreeDist();
	   HashMap<Set<Integer>,Double> d_g2=g2.joinDegreeDist();
	   	   
	   for(Set<Integer> pair:d_g1.keySet())
		   if(d_g2.keySet().contains(pair))
			   k_l_distance+=Utility.k_l_divergenz(d_g1.get(pair) , d_g2.get(pair));
   
return k_l_distance;
}
public static double globalClustrinDiffrence(NJM_Graph g1,NJM_Graph g2){
	   double d=Math.abs(g1.globalClustrinCo()-g2.globalClustrinCo());
return d;
}
public static double betweenness_Euclid(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] g1_betweenness=new double[g1.getNodesNum()];
	   double[] g2_betweenness=new double[g2.getNodesNum()];
	   
	   for(int node=0;node<g1.getNodesNum();++node)
		   g1_betweenness[node]=g1.betweennes(node);
	   for(int node=0;node<g2.getNodesNum();++node)
		   g2_betweenness[node]=g2.betweennes(node);
	   
	   d=Utility.euclid_metric(g1_betweenness, g2_betweenness);
return d;
}
/// **** 
public static double betweenness_KL(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] g1_betweenness=new double[g1.getNodesNum()];
	   double[] g2_betweenness=new double[g2.getNodesNum()];
	   
	   for(int node=0;node<g1.getNodesNum();++node)
		   g1_betweenness[node]=g1.betweennes(node);
	   for(int node=0;node<g2.getNodesNum();++node)
		   g2_betweenness[node]=g2.betweennes(node);
	   
	   d=Utility.k_l_metric(g1_betweenness,g2_betweenness);
return d;
}
/*
clusterCo
*/
public static double closenees_Euclid(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] g1_closenees=new double[g1.getNodesNum()];
	   double[] g2_closenees=new double[g2.getNodesNum()];
	   
	   for(int node=0;node<g1.getNodesNum();++node)
		   g1_closenees[node]=g1.closenees(node);
	   for(int node=0;node<g2.getNodesNum();++node)
		   g2_closenees[node]=g2.closenees(node);
	   
	   d=Utility.euclid_metric(g1_closenees, g2_closenees);
return d;
}
public static double closenees_KL(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] g1_closenees=new double[g1.getNodesNum()];
	   double[] g2_closenees=new double[g2.getNodesNum()];
	   
	   for(int node=0;node<g1.getNodesNum();++node)
		   g1_closenees[node]=g1.closenees(node);
	   for(int node=0;node<g2.getNodesNum();++node)
		   g2_closenees[node]=g2.closenees(node);
	   
	   d=Utility.k_l_metric(g1_closenees,g2_closenees);
return d;
}
public static double graphCentrality_Euclid(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] g1_graphCentrality=new double[g1.getNodesNum()];
	   double[] g2_graphCentrality=new double[g2.getNodesNum()];
	   
	   for(int node=0;node<g1.getNodesNum();++node)
		   g1_graphCentrality[node]=g1.closenees(node);
	   for(int node=0;node<g2.getNodesNum();++node)
		   g2_graphCentrality[node]=g2.closenees(node);
	   
	   d=Utility.euclid_metric(g1_graphCentrality, g2_graphCentrality);
return d;
}
public static double graphCentrality_KL(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] g1_graphCentrality=new double[g1.getNodesNum()];
	   double[] g2_graphCentrality=new double[g2.getNodesNum()];
	   
	   for(int node=0;node<g1.getNodesNum();++node)
		   g1_graphCentrality[node]=g1.closenees(node);
	   for(int node=0;node<g2.getNodesNum();++node)
		   g2_graphCentrality[node]=g2.closenees(node);
	   
	   d=Utility.k_l_metric(g1_graphCentrality, g2_graphCentrality);
return d;
}

public static double stressCentrality_Euclid(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] g1_stressCentrality=new double[g1.getNodesNum()];
	   double[] g2_stressCentrality=new double[g2.getNodesNum()];
	   
	   for(int node=0;node<g1.getNodesNum();++node)
		   g1_stressCentrality[node]=g1.closenees(node);
	   for(int node=0;node<g2.getNodesNum();++node)
		   g2_stressCentrality[node]=g2.closenees(node);
	   
	   d=Utility.euclid_metric(g1_stressCentrality, g2_stressCentrality);
return d;
}
public static double stressCentrality_KL(NJM_Graph g1,NJM_Graph g2){
	   double d=0;
	   double[] g1_stressCentrality=new double[g1.getNodesNum()];
	   double[] g2_stressCentrality=new double[g2.getNodesNum()];
	   
	   for(int node=0;node<g1.getNodesNum();++node)
		   g1_stressCentrality[node]=g1.closenees(node);
	   for(int node=0;node<g2.getNodesNum();++node)
		   g2_stressCentrality[node]=g2.closenees(node);
	   
	   d=Utility.k_l_metric(g1_stressCentrality, g2_stressCentrality);
return d;
}

/* End of Class:*/ }
