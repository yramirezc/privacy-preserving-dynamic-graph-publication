package attacks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import net.vivin.GenericTree;
import net.vivin.GenericTreeNode;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import anonymization.BaseGraph;
import anonymization.OddCycle;

import test.AntiResolving;
import util.EdgeConnectivity;
import util.GraphUtil;
import util.Statistics;




public class AttackThreeMethod {
	
	
	public static int getEdgeNum(int vexnum , double density){
		return (int)(density*vexnum*(vexnum-1)/2);
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		//main50All(args);
		main250(args);
	}
	public static void main250(String[] args) throws NoSuchAlgorithmException, IOException {
		//System.setOut(new PrintStream("report.txt"));
		//int vernum = Integer.parseInt(args[0]);
		//int vernum = 250;
		int vernum = 50;
		double density = Double.parseDouble(args[0]);
		int attackers = Integer.parseInt(args[1]);	
		
		int edgenum = getEdgeNum(vernum,density);
		
		AttackThreeMethod agg = new AttackThreeMethod();

		System.out.println("Now the density is "+ density+ " with "+ attackers+" attackers and edgenumber:"+edgenum);

		Writer outOriginal = new FileWriter("original-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
		Writer outCpa = new FileWriter("CPA-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
		Writer outCpaRandom = new FileWriter("CPARandom-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);

		agg.testingWalkBasedAttack(vernum,edgenum,attackers,outOriginal, outCpa, outCpaRandom);
		
		outOriginal.close();
		outCpa.close();
		outCpaRandom.close();

		System.out.println("========================================");
		
	}
	
	public static void main50All(String[] args) throws NoSuchAlgorithmException, IOException {
		//System.setOut(new PrintStream("report.txt"));
		//int vernum = Integer.parseInt(args[0]);
		int vernum = 100;
		//double[] densities = new double[]{0.025, 0.05, 0.075, 0.1, 0.125, 0.15, 0.175, 0.2, 0.225, 0.25, 0.275, 0.3};
		//double[] densities = new double[]{0.5, 0.75, 0.1, 0.125, 0.15, 0.175, 0.2, 0.225, 0.25, 0.275, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		//double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		double[] densities = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
		for (double density :densities){
			
			System.out.println("Working out density = "+density);
			int attackersTotal[] = new int[]{1,2,4,8,16};
			
			for (int attackers : attackersTotal){
				int edgenum = getEdgeNum(vernum,density);
				
				AttackThreeMethod agg = new AttackThreeMethod();
		
				System.out.println("Now the density is "+ density+ " with "+ attackers+" attackers and edgenumber:"+edgenum);
		
				Writer outOriginal = new FileWriter("original-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
				Writer outCpa = new FileWriter("CPA-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
				Writer outCpaRandom = new FileWriter("CPARandom-V-"+vernum+"-D-"+density+"-A-"+attackers+".DAT", true);
		
				agg.testingWalkBasedAttack(vernum,edgenum,attackers,outOriginal, outCpa, outCpaRandom);
				
				outOriginal.close();
				outCpa.close();
				outCpaRandom.close();
				System.out.println("========================================");
				
			}
		}
		
	}

	public void testingWalkBasedAttack(int n,int m, int attackersSize,
			Writer outOriginal, Writer outCpa,
			Writer outCpaRandom) throws NoSuchAlgorithmException, IOException{
		
		if (attackersSize > n) throw new IllegalArgumentException("The number of attacker cannot be higher " +
				"than the number of vertices");

		/*Trujillo- Feb 3, 2016
		 * We will first define the number of targeted vertices
		 * We take the square of the number of attacker as suggested 
		 * in the original paper*/
		//int victimsSize = attackersSize*attackersSize/2;
		//int victimsSize = 1;
		int victimsSize = attackersSize;
		if (victimsSize == 0) victimsSize = 1;
		
		/*Trujillo- Feb 3, 2016
		 * The degree of each attacking vertex should be between log n and 
		 * log n/2*/
		int maxDegree = (int)(Math.log10(n)/Math.log10(2));
		
		/*Trujillo- Feb 3, 2016
		 * it may be the case that the number of victims is higher than the number of vertices
		 * In this case we restrict the number of victims to remaining vertices*/

		if (attackersSize+victimsSize > n) victimsSize = m-attackersSize;
		
		SecureRandom random = new SecureRandom(); 
		/*Trujillo- Jan 21, 2016
		 * This loops will iterate over all graphs, which are by the 
		 * way going to be generate within this loop and the analyzed*/
		double timeDifAvg = 0;
		int total = 250000;

		for (int i = 1; i <= total; i++){
			System.out.println("This is the "+i+"th graph.");
			System.out.println("===============================================");
			double timeIni = System.currentTimeMillis();
			UndirectedGraph<String, DefaultEdge> graph = null;
			ConnectivityInspector<String, DefaultEdge> connectivity = null;
			do{
				//System.out.println("Looking for a connected random graph");
				/*Trujillo- Feb 3, 2016
				 * The structure of the vertex factory is as follows:
				 * - vertices from 0 to attackersSize -1 are attackers
				 * - vertices from attackersSize to attackersSize+victimsSize-1 are victims
				 * - The remaining vertices are just normal*/
				final int startingVertex = attackersSize;
				VertexFactory<String> vertexFactory = new VertexFactory<String>(){
					int i = startingVertex;
					@Override
					public String createVertex() {
						int result = i;
						i++;
						return result+"";
					}
					
				};
				
				/*Trujillo- Feb 3, 2016
				 * Next, we choose the degree of each attacking vertex*/
				/*int[] degrees = new int[attackersSize];
				int totalExternalDegrees = 0;
				for (int j = 0; j < degrees.length; j++) {
					degrees[j] = minDegree + random.nextInt(maxDegree-minDegree) + 1;
					totalExternalDegrees += degrees[j];
				}*/
				
				/*Trujillo- Feb 4, 2016
				 * We should also account for the number of internal edges, which is
				 * in average the x(x-1)/4 where x is the number of attackers node*/
				
				graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

				RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);
				
				generator.generateGraph(graph, vertexFactory, null);

				/*Trujillo- Feb 12, 2016
				 * Once that graph has been created, we add the attacker vertices*/
				for (int j = 0; j < attackersSize; j++) {
					graph.addVertex(j+"");
				}
				
				/*Trujillo- Feb 3, 2016
				 * Next we add the edges between victims and to the attackers. This
				 * links should give a unique fingerprint to each victim*/
				Hashtable<String, String> fingerprints = new Hashtable<>();
				for (int j = attackersSize; j < attackersSize+victimsSize; j++){
					//int degree = minDegree + random.nextInt(maxDegree-minDegree) + 1;
					String fingerprint = null;
					do{
						fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, attackersSize)-1) + 1);
						//next we add zeros to the left
						while (fingerprint.length() < attackersSize)
							fingerprint = "0"+fingerprint;
					}while (fingerprints.containsKey(fingerprint));
					
					//System.out.println("Fingerprint found = "+fingerprint);
					fingerprints.put(fingerprint, fingerprint);
					
					//next we actually add the edges to the graph 
					for (int k = 0; k < fingerprint.length(); k++) {
						if (fingerprint.charAt(k) == '1'){
							//System.out.println("adding edge ("+j+","+(k+attackersSize-fingerprint.length())+")");
							graph.addEdge(j+"", (k+attackersSize-fingerprint.length())+"");
						}
					}
					
				}
				
				/*Trujillo- Feb 3, 2016
				 * We also create the path 0-1-2... form by attacker nodes*/
				/*if (attackersSize > 1){
					for (int k = 0; k < attackersSize-1; k++) {
						graph.addEdge(k+"", (k+1)+"");
					}
				}*/
				
				/*Trujillo- Feb 3, 2016
				 * And finally we add random edges between the attacker nodes*/
				for (int k = 0; k < attackersSize; k++) {
					for (int l = k+1; l < attackersSize; l++) {
						if (random.nextBoolean()) {
							graph.addEdge(k+"", l+"");
						}
					}						
				}
				/*Trujillo- Feb 3, 2016
				 * After all of this, we still need to make sure that the graph is connected*/
				connectivity = new ConnectivityInspector<>(graph);
			}while(!connectivity.isGraphConnected());
			System.out.println("A connected random graph with "+graph.vertexSet().size()+" vertices" +
					" and "+graph.edgeSet().size()+" edges was found, whith density = "+GraphUtil.computeDensity(graph));
			
			/*Trujillo- Jan 25, 2016
			 * Next, we compute (if we can) the value of k such that
			 * the graph is (k,1)-anonymous*/
			//int[][] matrix = GraphUtil.transformIntoAdjacencyMatrix(graph);
			
			//System.out.println("Computing floyd for original");
			FloydWarshallShortestPaths<String, DefaultEdge> originalGraphFloyd = new FloydWarshallShortestPaths<>(graph);

			Statistics.printStatistics(i, outOriginal, graph, originalGraphFloyd, "original",attackersSize, victimsSize, graph, originalGraphFloyd);
			
			//System.out.println("The degree of the victim node at the orignal graph is "+graph.degreeOf("1"));
			//System.out.println("The degree of the attacker node at the orignal graph is "+graph.degreeOf("0"));
			
			//System.out.println("Cloning original");
			SimpleGraph<String, DefaultEdge> graphRandom = GraphUtil.cloneGraph(graph);

			//System.out.println("Dealing with end-vertices for this graph adds " + addedEdgeNumDegreeOne + " edges.");
			
			/*Trujillo- Jan 26, 2016
			 * From now in we will use the new version of arc, which does not 
			 * contain end vertices*/
			
			//System.out.println("The degree of the victim node after removing end vertices is "+graphWithoutEndVertices.degreeOf("1"));
			//System.out.println("The degree of the attacker node node after removing end vertices is "+graphWithoutEndVertices.degreeOf("0"));
			
			SimpleGraph<String, DefaultEdge> graphAnonymous = GraphUtil.cloneGraph(graph);

			//System.out.println("Anonymizing graph");
			//this method below anonymize the graph
			OddCycle.anonymizeGraph(graphAnonymous, originalGraphFloyd);
			
			int addedEdges = graphAnonymous.edgeSet().size() - graph.edgeSet().size();
			
			
			GraphUtil.addRandomEdges(addedEdges, graphRandom);

			if (graphRandom.edgeSet().size() != graphAnonymous.edgeSet().size())
				throw new RuntimeException("The random graph has "+graphRandom.edgeSet().size()+
						", which is different to the graphAnonymous = "+graphAnonymous.edgeSet().size());
			//System.out.println(addedEdges+" edges has been added in the graph using CPA.");
			
			//System.out.println("The degree of the victim node after applying CPA is "+graphTwo1Back.degreeOf("1"));
			//System.out.println("The degree of the attacker node node after applying CPA is "+graphTwo1Back.degreeOf("0"));
			FloydWarshallShortestPaths<String, DefaultEdge> anonymousGraphFloyd = new FloydWarshallShortestPaths<>(graphAnonymous);
			
			Statistics.printStatistics(i, outCpa, graphAnonymous, anonymousGraphFloyd, "CPA",attackersSize, victimsSize, graph, originalGraphFloyd);
			
			//System.out.println("The degree of the victim node after applying CPA-random is "+graphTwo1BackRandom.degreeOf("1"));
			//System.out.println("The degree of the attacker node node after applying CPA-random is "+graphTwo1BackRandom.degreeOf("0"));
			FloydWarshallShortestPaths<String, DefaultEdge> randomGraphFloyd = new FloydWarshallShortestPaths<>(graphRandom);
			
			Statistics.printStatistics(i, outCpaRandom, graphRandom, randomGraphFloyd, "CPA-Random",attackersSize, victimsSize, graph, originalGraphFloyd);
		
			double timeEnd = System.currentTimeMillis();
			double timeDif = timeEnd - timeIni;
			timeDifAvg = ((i-1)*timeDifAvg + timeDif)/i;
			double avgTakenTime = 2.7777 * timeDifAvg/Math.pow(10, 7);
			System.out.println("This round took " + avgTakenTime+ " hours for density = "+GraphUtil.computeDensity(graph));
			System.out.println("Remaining time = "+(avgTakenTime*(total-i))+" hours");
		}

	}
	
}