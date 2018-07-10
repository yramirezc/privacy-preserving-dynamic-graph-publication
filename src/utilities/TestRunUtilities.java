package utilities;

//package org.jgrapht.demo;

import java.net.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestRunUtilities {

	public static void main(String[] args) {
		
	
        SimpleGraph<String, DefaultEdge> origGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        origGraph.addVertex("v1");
		origGraph.addVertex("v2");
		origGraph.addVertex("v3");
		origGraph.addVertex("v4");
		origGraph.addVertex("v5");
		origGraph.addVertex("v6");
		origGraph.addEdge("v1","v2");
		origGraph.addEdge("v4","v2");
		origGraph.addEdge("v4","v5");
		origGraph.addEdge("v5","v6");
		origGraph.addVertex("vi");
		System.out.println(origGraph.toString());
		
		System.out.println(" new graph: ");
		NJM_Graph myGraph=new NJM_Graph(origGraph);
		//myGraph.allDistance();
		//myGraph.showGraph();
		System.out.println("");
		myGraph.showSimpleGraph();
		
		/*System.out.println(" complete graph");
		NJM_Graph g=NJM_Graph.completeGraph(5);
		g.showGraph();*/
		
		SimpleGraph<String, DefaultEdge> origGraph2 = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        origGraph2.addVertex("v2");
        origGraph2.addVertex("v1");
		origGraph2.addVertex("v3");
		origGraph2.addVertex("v4");
		origGraph2.addVertex("v5");
		origGraph2.addVertex("vi");
		origGraph2.addVertex("v6");
		origGraph2.addEdge("v1","v3");
		origGraph2.addEdge("v4","v3");
		origGraph2.addEdge("v4","vi");
		origGraph2.addEdge("v5","v6");
		
		NJM_Graph myGraph2=new NJM_Graph(origGraph2);
		//myGraph.allDistance();
		//myGraph.showGraph();
		System.out.println("");
		myGraph2.showSimpleGraph();
		
		
		
}


/* End Of Class */ }
