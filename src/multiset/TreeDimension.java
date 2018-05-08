package multiset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import Dimension.Resolving;
import util.PowerSetEnum;
import util.Print;

public class TreeDimension {

	public static void main(String[] args) throws FileNotFoundException {
		
		for (int n = 1; n <= 56; n++) {
			System.out.println("Iterating from 2 to the power of "+(n-1)+" to 2 to the power of "+n);
			for (int i = (int)Math.pow(2, n-1); i < Math.pow(2, n); i++) {
			}
		}
		System.setOut(new PrintStream(new File("out.txt")));
		int depth = 8;
		System.out.println("Starting");
		int estimatedBound = 1; 
		for (int i = 1; i <= depth; i++) {
			Print.print("Looking bases for depth = "+i+" with estimated bound = "+estimatedBound);
			SimpleGraph<String, DefaultEdge> tree = generateFullBinaryTree(i);
			//iniBound = tree.vertexSet().size();
			Print.printGraph(tree);
			Set<Set<String>> bases = findMultisetBases(tree, estimatedBound);
			System.out.println("The bases are: ");
			for (Set<String> base : bases) {
				Print.printList(base);
			}
			estimatedBound = bases.iterator().next().size()*2+1;
		}
		System.out.println("End");
	}
	
	public static SimpleGraph<String, DefaultEdge> generateFullBinaryTree(int depth) {
		SimpleGraph<String, DefaultEdge> tree = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		for (int i = 1; i < ((int)Math.pow(2, depth+1)); i++) {
			tree.addVertex(i+"");
		}
		for (int i = 1; i < ((int)Math.pow(2, depth)); i++) {
			String child1 = 2*i+"";
			String child2 = (2*i+1)+"";
			tree.addEdge(i+"", child1);
			tree.addEdge(i+"", child2);
		}
		return tree;
	}
	
	public static Set<Set<String>> findMultisetBases(SimpleGraph<String, DefaultEdge> tree, int estimatedBound) {
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(tree);
		Set<String> vertices = tree.vertexSet();
		PowerSetEnum<String> powersetEnum = new PowerSetEnum<>(vertices, estimatedBound-1, estimatedBound+1);
		
		Set<Set<String>> result = new HashSet<>();
		int minimum = vertices.size();
		while (powersetEnum.hasMoreElements()) {
		//for (Set<String> set : powerset) {
			Set<String> set = powersetEnum.nextElement();
			if (set.size() > minimum) continue;
			if (Resolving.isMultisetResolving(set, tree, floyd)){
				if (set.size() == minimum) {
					Print.print("Another optimal of size "+set.size()+" has been found:" + set.toString());
					result.add(set);
				}
				if (set.size() < minimum) {
					Print.print("A NEW optimal of size "+set.size()+" has been found:" + set.toString());
					result = new HashSet<>();
					result.add(set);
					minimum = set.size();
				}
			}
		}
		result = removeIsomorphicResults(result, "1", floyd);
		return result;
	}

	private static Set<Set<String>> removeIsomorphicResults(Set<Set<String>> bases, String rootVertex,
			FloydWarshallShortestPaths<String, DefaultEdge> floyd) {
		Hashtable<String, Set<String>> result = new Hashtable<>();
		for (Set<String> base : bases) {
			String multisetRepresentation = Resolving.findMultisetMetricRepresentation(base, rootVertex, floyd);
			if (!result.containsKey(multisetRepresentation)) {
				result.put(multisetRepresentation, base);
			}
		}
		Set<Set<String>> finalResult = new HashSet<>();
		for (String representation : result.keySet()) {
			finalResult.add(result.get(representation));
		}
		return finalResult;
	}

}
