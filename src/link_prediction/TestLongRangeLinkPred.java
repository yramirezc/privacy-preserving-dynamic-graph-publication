package link_prediction;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public abstract class TestLongRangeLinkPred {

	public static void main(String[] args) {
		
		SimpleGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		graph.addVertex("a");
		graph.addVertex("b");
		graph.addVertex("c");
		graph.addVertex("d");
		graph.addVertex("e");
		
		graph.addVertex("f");
		graph.addVertex("g");
		graph.addVertex("h");
		graph.addVertex("i");
		graph.addVertex("j");
		graph.addVertex("k");
		
		graph.addVertex("l");
		
		graph.addEdge("a", "b");
		graph.addEdge("b", "c");
		graph.addEdge("c", "d");
		graph.addEdge("d", "e");
		
		graph.addEdge("d", "f");
		graph.addEdge("f", "g");
		graph.addEdge("c", "h");
		graph.addEdge("h", "i");
		graph.addEdge("i", "k");
		graph.addEdge("i", "j");
		
		graph.addEdge("f", "k");
		
		graph.addEdge("c", "l");
		graph.addEdge("l", "i");
		
		/*
		graph.addVertex("v1");
		graph.addVertex("vi-1");
		graph.addVertex("vi");
		graph.addVertex("vi+1");
		graph.addVertex("vi+2");
		graph.addVertex("vj");
		graph.addVertex("vj+1");
		graph.addVertex("vj+2");
		graph.addVertex("vj+3");
		graph.addVertex("vj+4");
		graph.addVertex("vm");
		graph.addVertex("w1");
		graph.addVertex("w");
		graph.addVertex("u");
		graph.addVertex("u1");
		graph.addVertex("u2");
		graph.addVertex("x");
		
		graph.addEdge("v1", "vi-1");
		graph.addEdge("vi-1", "vi");
		graph.addEdge("vi", "vi+1");
		graph.addEdge("vi+1", "x");
		graph.addEdge("x", "vi+2");	
		graph.addEdge("vi+2", "vj");
		graph.addEdge("vj", "vj+1");
		graph.addEdge("vj", "vj+3");
		graph.addEdge("vj+1", "vj+2");
		graph.addEdge("vj+3", "vj+4");
		graph.addEdge("vj+2", "vm");
		graph.addEdge("vj+4", "vm");
		graph.addEdge("vi", "w1");
		graph.addEdge("w1", "w");
		graph.addEdge("v1", "vi-1");
		graph.addEdge("v1", "u");
		graph.addEdge("u", "u1");
		graph.addEdge("u1", "u2");
		graph.addEdge("u2", "u");
		System.out.println(graph.toString());
		*/
		
		//WalkBasedKatz katzLink = new WalkBasedKatz(graph, 4, 0.25);
		PathBasedFriendLink friendLink = new PathBasedFriendLink(graph, 10);
		
		//System.out.println(katzLink.score("a", "b"));
		System.out.println(friendLink.score("a", "b"));
		
	}

}
