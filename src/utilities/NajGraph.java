package utilities;

import java.text.DecimalFormat;
import java.util.*;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import Jama.*;

public class NajGraph {
	
	private int nodesNum;
	   private int edgesNum;
	   private Set<Set<Integer>> edgeSet=new HashSet<Set<Integer>>();
	   public HashMap<Set<Integer>,Integer> distance=new HashMap<Set<Integer>,Integer>();

/// ********************************* Constructors ********************************* /// 
public NajGraph() {
	   this.nodesNum=0;
	   this.edgesNum=0;
}

public NajGraph(int nodenumber,int edgenumber){
	   
	   this.nodesNum=nodenumber;
	   this.edgesNum=edgenumber;
	   
	   int maxEdge=nodesNum*(nodesNum-1)/2;
    int[] edgPerm=new int[maxEdge];
	   Utility.randomPermutation(maxEdge, edgPerm);
	    
	   for(int i=0;i< maxEdge;i++){
		   if(edgPerm[i]<edgesNum){
		      int tmpInt=(int) (1+Math.sqrt(1+8*i))/2;
		      Set<Integer> pair=new HashSet<Integer>();
		      pair.add(tmpInt);
		      pair.add(i-tmpInt*(tmpInt-1)/2);
  		  //this.distance.put(pair, 1);
		      this.edgeSet.add(pair);
		      }
		   else continue;
	      }
	for(Set<Integer> pair:this.edgeSet)
		this.distance.put(pair,1);
}

	public NajGraph(SimpleGraph<String, DefaultEdge> graphToCopy){
		// TODO: Fill this to create a NajGraph which is a copy of graphToCopy
	}


/// ********************************* Getter- Setter ********************************* ///
public int getNodesNum() {
	return nodesNum;
}

public void setNodesNum(int nodesNum) {
	if(nodesNum>= 0)
	this.nodesNum = nodesNum;
}
public int getEdgesNum() {
	return edgesNum;
}
public Set<Set<Integer>> getEdgeSet(){
	return this.edgeSet;
}

public void setEdgesNum(int edgesNum) {
	this.edgesNum = edgesNum;
}
public void setGraph(int nodeNum,int edgeNum){
	
	   System.out.print(" Graph Nodes Set : { 0");
	   for(int i=1;i<nodeNum;++i)
		   System.out.print(" , "+i);
	   System.out.println(" }");
	   System.out.println("Enter the edges : ");
	
	   while(this.edgeSet.size()<edgeNum)
	   {
		   Scanner scanner = new Scanner(System.in);
		   int node1=scanner.nextInt();
		   int node2=scanner.nextInt();
		   Set<Integer> pair=Utility.SetThePair(node1,node2);
		   this.edgeSet.add(pair);
		   scanner.close();
		}
	   
}
/// ******************************** Generate Some special Graphs **************************************** ///
public NajGraph completeGraph (int nodeNum){
	   NajGraph g=new NajGraph();
	   g.nodesNum=nodeNum;
	   g.edgesNum=nodeNum * (nodeNum-1)/2;
	   for(int i=0;i<nodeNum;++i)
		   for(int j=i+1;j<nodeNum;++j){
			   Set<Integer> pair=Utility.SetThePair(i, j);
			   g.edgeSet.add(pair);
		   }
return g;
}
     /// *** Path *** ///
public NajGraph pathGraph(int nodeNum){
	NajGraph g=new NajGraph();
	   g.nodesNum=nodeNum;
	   g.edgesNum=nodeNum - 1;
	   for(int i=0;i<nodeNum-1;++i){
		   Set<Integer> pair=Utility.SetThePair(i, i+1);
		   g.edgeSet.add(pair);
		   }
return g;
}
   /// *** Cycle *** ///
public NajGraph cycleGraph(int nodeNum){
	NajGraph g=pathGraph(nodeNum);
	      Set<Integer> pair=Utility.SetThePair(0, nodeNum-1);
		   g.edgeSet.add(pair);
return g;
}
public NajGraph starGraph(int nodeNum){
	   NajGraph g=new NajGraph();
	   g.nodesNum=nodeNum;
	   g.edgesNum=nodeNum-1;
	   for(int i=1;i<nodeNum;++i){
		   Set<Integer> pair=Utility.SetThePair(0, i);
		   g.edgeSet.add(pair);
	   }
return g;
}

/// ********************************* Degree Metrics ********************************* ///
public int degreeOfNode(int node){
    int degree=0;
    
    for(int i=0;i<this.nodesNum;i++){
     	if(i==node) continue;
 	    Set<Integer> pair=Utility.SetThePair(node,i);
 	    if(this.edgeSet.contains(pair)) degree++;
 	    }
return degree;
}
                    /// *** Degree Sequence *** ///
public int[] degreeSeq(){
    int[] degMatrix=new int[this.nodesNum];
	
	    for(int i=0;i<this.nodesNum;i++)
		  degMatrix[i]=0;	
	
	    for(Set<Integer> edge:this.edgeSet){
	       for(int node:edge)
		   degMatrix[node]++;}
return degMatrix;
}
                    /// *** Degree Distribution *** ///

public double[] degreeDistribution(){
	final double d=(double)1/ this.nodesNum;  /// 1/n
	double[] degDisMatrix=new double[this.nodesNum];
	int[] degMatrix=this.degreeSeq();
	for(int i=0;i<this.nodesNum;i++)
     degDisMatrix[i]=0;
	
	for(int i=0;i<this.nodesNum;i++)
	 degDisMatrix[degMatrix[i]]+=d;
	
return degDisMatrix;
}
                    /// *** Join Degree *** ///

public HashMap<Set<Integer>,Integer>  jointdegreeSeq(){
	   HashMap<Set<Integer>,Integer> joinDeg=new HashMap<Set<Integer>,Integer>();
	   int[] degreeArr=this.degreeSeq();
	   
	   for(Set<Integer> pair:this.edgeSet){
		   Integer pairNode[]=pair.toArray(new Integer[pair.size()]);
		   int d_0=degreeArr[pairNode[0]];
		   int d_1=degreeArr[pairNode[1]];
		   Set<Integer> degreePair=Utility.SetThePair(d_0, d_1);
		   if(joinDeg.containsKey(degreePair)){
			   int m=joinDeg.get(degreePair)+1;
			   joinDeg.replace(degreePair, m);
		   }else
			   joinDeg.put(degreePair, 1);
	   }
	 // System.out.println(joinDeg);
return joinDeg;
}
/// *******************************  Should be checked ***************************** ///
public HashMap<Set<Integer>,Double> joinDegreeDist(){
	HashMap<Set<Integer>,Integer> joindegSeq=this.jointdegreeSeq();
	Set<Set<Integer>> pairDegree=joindegSeq.keySet();
	
	HashMap<Set<Integer>,Double> JDD=new HashMap<Set<Integer>,Double>();
	
	for(Set<Integer> pair:pairDegree){
		
		DecimalFormat df=new DecimalFormat("0.0000");
		double coefficient= (pair.size()==1) ? 1:2;
		String tmpstr=df.format(coefficient * joindegSeq.get(pair)/(2*this.edgesNum));
		double tmpNum= Double.parseDouble(tmpstr);
		JDD.put(pair, tmpNum);
	}
		//System.out.println(JDD);
return JDD;
}
/// ********************************* Neighbors  Set ********************************* ///
                    /// *** Neighbors of one Node *** ///
public Set<Integer> neighbors(int node){
    Set<Integer> neighborsOfNode=new HashSet<Integer>();
	   
	   for(int i=0; i<this.nodesNum;i++){
	       if (i==node) continue;
		   Set<Integer> pair=Utility.SetThePair(node, i);
		   if( this.edgeSet.contains(pair))
		    neighborsOfNode.add(i);
		   }
return neighborsOfNode;
}
                    /// *** Neighbors of the set of nodes ***///
                    
public Set<Integer> neighbors(Set<Integer> nodeSet){
	   Set<Integer> neighborsOfSet=new HashSet<Integer>();
	   
	   for(Set<Integer> pair:this.edgeSet){
		   Integer pairNode[]=pair.toArray(new Integer[pair.size()]);
		   if(nodeSet.contains(pairNode[0])) neighborsOfSet.add(pairNode[1]);
		   if(nodeSet.contains(pairNode[1])) neighborsOfSet.add(pairNode[0]);
		   }
return neighborsOfSet;
}
/// ********************************* Distance Metrics  ********************************* ///
                    /// *** Distance Between two nodes ***///
public int distance(int node1,int node2){
	int distance=0;
	Set<Integer> tempSet=new HashSet<Integer>();
	tempSet.add(node1);
	do{
		if (tempSet.contains(node2)) return distance; 
		else
			{distance++;
		     tempSet=this.neighbors(tempSet);
		     }
	  }while( distance<this.nodesNum || tempSet.contains(node2) );
return distance;
}
                  /// *** Distance between Node 1 and all other nodes with number of node is upper *** /// 
public void allDistanceFromNode(int node){
	Set <Integer> A=this.neighbors(node);
	Set <Integer> checkingSet=new HashSet<Integer>();
	for(int i=0;i<=node;i++)
		checkingSet.add(i);
	checkingSet.addAll(A);
	int distance=1;
	do{
		Set<Integer> B=this.neighbors(A);
		B.removeAll(checkingSet); 
		distance++;
		
		for(int n:B){
			Set<Integer> pair=new HashSet<Integer>();
			pair.add(node);
			pair.add(n);
			this.distance.put(pair,distance);
		    }
		
		checkingSet.addAll(B);
		A.clear();
		A.addAll(B);
		B.clear();
		
	}while(!A.isEmpty());
}

public void allDistance(){
int maxEdge=this.nodesNum * (this.nodesNum-1)/2;
for(int i=0;(i<this.nodesNum-1 && this.distance.keySet().size()< maxEdge) ;i++)
	 {this.allDistanceFromNode(i);
	 }
}
/// ********************************* Paths metrics  ********************************* ///
public int numberOfShPath(int node1,int node2){
 int numOfShortPath=0;
 Set<Integer> p1=Utility.SetThePair(node1, node2);
 System.out.println(p1);
 int distance=this.distance.get(p1);
 
 switch (distance){
 case 1: return 1; 
 case 2: 
	   Set<Integer> tmpSet1=Utility.intersection(this.neighbors(node1),this.neighbors(node2));
	   return tmpSet1.size();
 default:
      Set <Integer> A=new HashSet<Integer>(); 
	    A.add(node1);
	    Set <Integer> checkSet=new HashSet<Integer>(); 
	    checkSet.add(node1);
	   
	    Path path=new Path(node1);
	        	    //	this.neighbors(node1);
	    Set <Path> allPath=new HashSet<Path>(); 
	    allPath.add(path);
	    Set<Path> newPath=new HashSet<Path>(); 
	    Set<Integer> tmpSet=new HashSet<Integer>();
	    
	    for(int i=0;i<distance-1;i++){
	    for(Path p:allPath){
	    	int endPoint=p.endNode;
 	        Set<Integer> endNeighbors=this.neighbors(endPoint);
 	        endNeighbors.remove(checkSet);
	    	
 	        for(int a: endNeighbors){
	          Path tmpPath=new Path();
	          tmpPath.addAll(p);
	          tmpPath.add(a);
	          newPath.add(tmpPath);
	          tmpPath.add(a);
	          }
	    	}
	    
	    checkSet.addAll(tmpSet);
	    tmpSet.clear();
	    allPath.clear();
	    allPath.addAll(newPath);
	    newPath.clear();
	    }
	    
	    for(Path p:allPath){
	    	Set <Integer> pair =Utility.SetThePair(p.endNode , node2);
	    	if(this.edgeSet.contains(pair)) numOfShortPath++;
	    	else continue;
	    }
    }
 
return numOfShortPath;
}
public double[] eigenValue(){
	   double[] eigenvalue=new double[this.nodesNum];
	   double[][] adjMatrix=new double[this.nodesNum][this.nodesNum];
	   
	   for(int node1=0;node1<this.nodesNum;++node1){
		   adjMatrix[node1][node1]=0;
		   for(int node2=node1+1;node2<this.nodesNum;++node2){
			   Set<Integer> pair=Utility.SetThePair(node1, node2);
			   if(this.edgeSet.contains(pair))
				   adjMatrix[node1][node2]=adjMatrix[node2][node1]=1;
			   else 
				   adjMatrix[node1][node2]=adjMatrix[node2][node1]=0;}
	   }
	   //System.out.println("This is the graph adgancy matric:");
	   /*for(int i=0;i<this.nodesNum;++i){
		   for(int j=0;j<this.nodesNum;++j)
			   System.out.print(adjMatrix[i][j]+"  ");
		   System.out.println();
	   }*/
	   Matrix M=new Matrix(adjMatrix);
	   M.print(4, 0);
	      EigenvalueDecomposition E=M.eig();
	      double[] d = E.getRealEigenvalues();
	      for(int i=0;i<4;++i)
	    	  System.out.println(d[i]);
	      
return eigenvalue;	   
}

/// ********************************* Centrality measures ***************************************** ///
public double betweennes(int node){
    double betweenNum=0;

for(int i=0;i<this.nodesNum;i++){
	if(i==node) continue;  
	for(int j=i+1;j<this.nodesNum ;j++){
	   if(j==node) continue;
    Set<Integer> i_node=Utility.SetThePair(node, i);
    Set<Integer> j_node=Utility.SetThePair(node, j);
    Set<Integer> i_j=Utility.SetThePair(i,j);
	  
  if(this.distance.get(i_j)== this.distance.get(i_node)+ this.distance.get(j_node)){
	    System.out.println(" true ");
     double a=this.numberOfShPath(i, node) * 1.0;
	    double b=this.numberOfShPath(node, j) * 1.0;
	    double c=this.numberOfShPath(i, j) * 1.0;
	    if (c!=0) betweenNum=a*b/c+betweenNum; 
	    System.out.println("this  "+ betweenNum);
		}
		 else continue;}
}
return betweenNum;
}
/// ********************************* Closeness Centrality ******************************** ///
public double closenees(int node){
    double C_C;
    int    tmpNum=0;
    //int    maxDistance=0;
    for(int i=0;i<this.nodesNum;i++){
 	   if (i==node) continue;
 	   Set<Integer> pair=Utility.SetThePair(node, i);
 	   int tmpDistance=this.distance.get(pair);
 	   //if (maxDistance < tmpDistance) maxDistance=tmpDistance;
 	   tmpNum+=tmpDistance;
    }
    C_C=1.0/tmpNum;
return C_C;
}
/// ********************************* Graph Centrality ************************************ ///
public double graphCentrality(int node){
 double C_G;
 int    maxDistance=0;
 for(int i=0;i<this.nodesNum;i++){
	   if (i==node) continue;
	   Set<Integer> pair=Utility.SetThePair(node, i);
	   int tmpDistance=this.distance.get(pair);
	   if (maxDistance < tmpDistance) maxDistance=tmpDistance;
	   //tmpNum+=tmpDistance;
 }
 C_G=1/maxDistance;
return C_G;
}
/// ********************************* Stress Centrality *********************************** ///
public int stressCentrality(int node){
	   int C_S=0;
	
	   ///////////////////
for(int i=0;i<this.nodesNum;i++){
		if(i==node) continue;  
		for(int j=i+1;j<this.nodesNum ;j++){
		   if(j==node) continue;
	       Set<Integer> i_node=Utility.SetThePair(node, i);
	       Set<Integer> j_node=Utility.SetThePair(node, j);
	       Set<Integer> i_j=Utility.SetThePair(i,j);
		  
	       if(this.distance.get(i_j)== this.distance.get(i_node)+ this.distance.get(j_node)){
		    System.out.println(" true ");
	        int tmpNum=this.numberOfShPath(i, node) * this.numberOfShPath(node, j);
	        C_S+=tmpNum;
		    }
}}
return C_S;
}
/// ****************************************** Clustring Coefficient ***************************************** ///
public double clusterCo(int node){
	   double C_c=0;
	   int N_v=0;
	   int degree=this.degreeOfNode(node);
	   System.out.println("degree: "+degree);
	   if (degree<=1) return 0;
	   Set<Integer> neighbors=this.neighbors(node);
	   System.out.println("Start CLustring");
	   System.out.println(neighbors);
	   
	   for(int i:neighbors)
		   for(int j:neighbors ){
			   if(i<=j) continue;
			   Set<Integer> pair=Utility.SetThePair(i,j);			   
			   if(this.edgeSet.contains(pair))
				   {++N_v;
				   System.out.println(pair);}				      
		}
	   System.out.println(N_v);
	   C_c=2.0 *N_v/(degree*(degree-1));
return C_c;}

/// ********************************* Global Clustring Coefficient of a Graph ****************************************************************** ///
public double globalClustrinCo(){
	   double gcc=0;
	   int    triangleNum=0;
	   int    triplePathNum=0;
	   int[]  degreeSequence=this.degreeSeq();
	   
	   for(int node=0;node<this.nodesNum;++node){
		   if(degreeSequence[node]>1) triplePathNum+= degreeSequence[node]* (degreeSequence[node]-1)/2;
		   Set<Integer> neighborSet=this.neighbors(node);		   
		   for(int i:neighborSet)
			   for(int j:neighborSet ){
				   if(i<=j) continue;
				   Set<Integer> pair=Utility.SetThePair(i,j);			   
				   if(this.edgeSet.contains(pair))
					   {++triangleNum;
					   System.out.println(pair);}      
		  }
	   }
	   System.out.println(triangleNum);
	   System.out.println(triplePathNum);
	   gcc=triangleNum * 1.0/triplePathNum;
return gcc;
}

/// ********************************* Graph Presentation  ********************************* ///
public void showGraph(){
 	System.out.println("  Number of nodes : "+ this.nodesNum);
 	System.out.println("  Number of edges : "+ this.edgesNum);
 	
 	System.out.println("this is the set of edge  : " + this.edgeSet);
    		
int[][] adjMatrix=new int[this.nodesNum][this.nodesNum];
for(int i=0;i<this.nodesNum;i++)
 for(int j=i;j<this.nodesNum;j++)
 	{ 
 	Set<Integer> pair=new HashSet<Integer>();
 	pair.add(i);
 	pair.add(j);
 	
 	if(this.distance.containsKey(pair))
 	     adjMatrix[i][j]=adjMatrix[j][i]=1;
 	   else 
 	     adjMatrix[i][j]=adjMatrix[j][i]=0;
 	}
 	
 	System.out.print("  *");
 	for(int i=0;i<this.nodesNum;i++)
 		System.out.print("  "+ i);
 	System.out.println();
 	for(int i=0;i<this.nodesNum;i++)
 		System.out.print("----");
 	System.out.println();
 	for(int i=0;i<this.nodesNum;i++)
 		{
 		System.out.print(i+ " |");
 		
 		for(int j=0;j<this.nodesNum;j++)
 		 System.out.print("  "+ adjMatrix[i][j]);
 		System.out.println();
 		}
 	System.out.println();

}
}
