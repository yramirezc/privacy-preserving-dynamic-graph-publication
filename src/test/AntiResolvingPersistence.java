package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgraph.graph.Edge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.DominationSet;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.WeaklyConnectedDominationSet;
import org.jgrapht.generate.HyperCubeGraphGenerator;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.ClassBasedVertexFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.graph.SimpleGraph;
import org.omg.CORBA.UNKNOWN;

public class AntiResolvingPersistence {

	public static final int MAX_N = 100;
	public static final int MAX_K = 8;
	public static final int ITERATIONS = 1000;
	
	public static final String NEW_LINE = System.getProperty("line.separator");

	
	private static void printTime(String msg, long milliSeconds) {
		long minutes = milliSeconds/60000; 
		if (minutes > 0) System.out.println(msg+", took "+minutes+" minutes");
		//System.out.println(msg+", took "+minutes+" minutes");
	}
	
	public static void mainForOne(String[] args) throws IOException {
		//System.setOut(new PrintStream(new File("out2.txt")));
		int k = Integer.parseInt(args[0]);
		int m = Integer.parseInt(args[1]);
		Writer out = new FileWriter("output-k-"+k+"-m-"+m+".txt", true);
		//out.append("k \t POSITIVE \t NEGATIVE \t UNKNOWN "+NEW_LINE);
		System.out.println("Analyzing k = "+k+" and m = "+m);
		testAntiResolvingToFile(k, m, out);
		System.out.println("Done k = "+k+" and m = "+m);
		out.close();
	}

	public static void mainForOneBasis(String[] args) throws IOException {
		//System.setOut(new PrintStream(new File("outBasis1.txt")));
		int k = Integer.parseInt(args[0]);
		int m = Integer.parseInt(args[1]);
		Writer out = new FileWriter("outputBasis-k-"+k+"-m-"+m+".txt", true);
		//out.append("k \t POSITIVE \t NEGATIVE \t UNKNOWN "+NEW_LINE);
		System.out.println("Analyzing k = "+k+" and m = "+m);
		testAntiResolvingBasisToFile(k, m, out);
		System.out.println("Done k = "+k+" and m = "+m);
		out.close();
	}

	public static void mainForPrivacyMeasure(String[] args) throws IOException {
		//System.setOut(new PrintStream(new File("outBasis1.txt")));
		int m = Integer.parseInt(args[0]);
		double l = Double.parseDouble(args[1]);
		Writer out = new FileWriter("outputPrivacy-m-"+m+"-l-"+l+".txt", true);
		//out.append("k \t POSITIVE \t NEGATIVE \t UNKNOWN "+NEW_LINE);
		System.out.println("Analyzing l = "+l);
		//testAntiResolvingBasisToFile(k, m, out);
		testPrivacyToFile(m, l, out);
		System.out.println("Done l = "+l);
		out.close();
	}

	public static void main(String[] args) throws IOException {
		mainForOne(args);
		//mainForOneBasis(args);
		//mainForPrivacyMeasure(args);
	}
	
	
	
	private static void testAntiResolvingToFile(int k, int depth, Writer out) throws IOException{
		SecureRandom random = new SecureRandom(); 
		VertexFactory<String> vertexFactory = new VertexFactory<String>(){
			int i = 0;
			@Override
			public String createVertex() {
				i++;
				return i+"";
			}
			
		};
		long start, end;
		for (int i = 0; i < ITERATIONS; i++){
			int n = random.nextInt(MAX_N)+k+2;
			int m = random.nextInt((n*(n-1))/2);
			System.out.println("Computing antiresolving set for k = "+k+" n = "+n+" and m = "+m);
			start = System.currentTimeMillis();
			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
			generator.generateGraph(graph, vertexFactory, null);
			FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
			Output result = antiResolvingPersistence(graph, floyd, k, depth);
			end = System.currentTimeMillis();
			printTime((i+1)+" out of "+ITERATIONS, end-start);
			out.append(result.output+NEW_LINE);
			out.flush();
		}
	}

	private static void testAntiResolvingBasisToFile(int k, int depth, Writer out) throws IOException{
		SecureRandom random = new SecureRandom(); 
		VertexFactory<String> vertexFactory = new VertexFactory<String>(){
			int i = 0;
			@Override
			public String createVertex() {
				i++;
				return i+"";
			}
			
		};
		long start, end;
		for (int i = 0; i < ITERATIONS; i++){
			int n = random.nextInt(MAX_N)+k+2;
			int m = random.nextInt((n*(n-1))/2);
			System.out.println("Computing antiresolving basis for k = "+k+" n = "+n+" and m = "+m);
			start = System.currentTimeMillis();
			RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);
			UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
			generator.generateGraph(graph, vertexFactory, null);
			FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
			Output result = antiResolvingBasisPersistence(graph, floyd, k, depth);
			end = System.currentTimeMillis();
			printTime((i+1)+" out of "+ITERATIONS, end-start);
			out.append(result.output+NEW_LINE);
			out.flush();
		}
	}

	private static void testPrivacyToFile(int depth, double l, Writer out) throws IOException{
		SecureRandom random = new SecureRandom(); 
		VertexFactory<String> vertexFactory = new VertexFactory<String>(){
			int i = 0;
			@Override
			public String createVertex() {
				i++;
				return i+"";
			}
			
		};
		long start, end;
		long totalTime = 0; 
		for (int i = 0; i < ITERATIONS; i++){
			int n = random.nextInt(MAX_N)+2;
			int m = random.nextInt((n*(n-1))/2);
			//System.out.println("Computing privacy for n = "+n+" and m = "+m);
			start = System.currentTimeMillis();
			for (int k = 1; k <= n; k++){
				RandomGraphGenerator<String, DefaultEdge> generator = new RandomGraphGenerator<>(n, m);
				UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
				generator.generateGraph(graph, vertexFactory, null);
				FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(graph);
				//System.out.println("Computing basis for k = "+k+" n = "+n+" and m = "+m);
				Output result = antiResolvingBasisPersistence(graph, floyd, k, depth);
				if (result.output.equals(STATE.UNKNOWN)){
					//System.out.println("UNKNOWN: The privacy is for k = "+k);
					out.append(k+NEW_LINE);
					out.flush();
					break;
				}
				else if (result.output.equals(STATE.POSITIVE)){
					if (result.size <= n*l/100){
						//System.out.println("POSITIVE: The privacy is for k = "+k);
						out.append(k+NEW_LINE);
						out.flush();
						break;
					}
				}
			}
			end = System.currentTimeMillis();
			printTime((i+1)+" out of "+ITERATIONS, end-start);
			totalTime += end-start;
			System.out.println("Expected remaining time = "+((ITERATIONS-i)/60000d)*(totalTime/(i+1))+" minutes");
		}
	}

	private static Output antiResolvingPersistence(UndirectedGraph<String, DefaultEdge> graph, 
			FloydWarshallShortestPaths<String, DefaultEdge> floyd, int k, int depth) {
		String name = "current";
		PersistentList currentCandidates = new PersistentList(name+"_"+k+"_"+depth+".DAT");
		boolean next = true;
		Set<String> set;
		for (String v : graph.vertexSet()){
			set = new TreeSet<>();
			set.add(v);
			set = applyFunctionF(graph, floyd, set, k);
			if (isAntiResolvingSet(floyd, set, graph, k)) return new Output(STATE.POSITIVE, set.size(), set);
			currentCandidates.add(set);
		}
		for (int h = 2; h <= depth; h++){
			int i = 0;
			if (next) name = "next";
			else name = "current";
			PersistentList nextCandidates = new PersistentList(name+"_"+k+"_"+depth+".DAT");
			for (Set<String> setI : currentCandidates){
				int j = 0;
				for (Set<String> setJ : currentCandidates){
					if (j < i+1) {
						j++;
						continue;
					}
					if (!inclusion(setI, setJ)){
						set = new TreeSet<>();
						set.addAll(setI);
						set.addAll(setJ);
						set = applyFunctionF(graph, floyd, set, k);
						if (isAntiResolvingSet(floyd, set, graph, k)) return new Output(STATE.POSITIVE, set.size(), set);
						nextCandidates.add(set);
					}
					j++;
				}
				i++;
			}
			currentCandidates = nextCandidates;
			next = !next;
		}
		for (Set<String> lastSet : currentCandidates){
			if (lastSet.size() < graph.vertexSet().size()) return new Output(STATE.UNKNOWN, 0, null);
		}
		return new Output(STATE.NEGATIVE, 0, null);
	}

	public static Output antiResolvingBasisPersistence(UndirectedGraph<String, DefaultEdge> graph, 
			FloydWarshallShortestPaths<String, DefaultEdge> floyd, int k, int depth) {
		String name = "currentBasis";
		PersistentList currentCandidates = new PersistentList(name+"_"+k+"_"+depth+".DAT");
		boolean next = true;
		Set<String> set;
		long minCardinalityOfBasis = Long.MAX_VALUE; 
		long minCardinality = Long.MAX_VALUE;
		Set<String> basis = null;
		for (String v : graph.vertexSet()){
			set = new TreeSet<>();
			set.add(v);
			set = applyFunctionF(graph, floyd, set, k);
			if (minCardinality > set.size()) minCardinality = set.size();
			if (isAntiResolvingSet(floyd, set, graph, k)) {
				//we already found an antiresolving set
				if (minCardinalityOfBasis > set.size()) {
					minCardinalityOfBasis = set.size();
					basis = set;
				}
			}
			currentCandidates.add(set);
		}
		if (basis != null && minCardinalityOfBasis <= minCardinality) return new Output(STATE.POSITIVE, minCardinalityOfBasis, basis);
		for (int h = 2; h <= depth; h++){
			int i = 0;
			if (next) name = "nextBasis";
			else name = "currentBasis";
			PersistentList nextCandidates = new PersistentList(name+"_"+k+"_"+depth+".DAT");
			minCardinality = Long.MAX_VALUE;
			for (Set<String> setI : currentCandidates){
				int j = 0;
				for (Set<String> setJ : currentCandidates){
					if (j < i+1) {
						j++;
						continue;
					}
					if (!inclusion(setI, setJ)){
						set = new TreeSet<>();
						set.addAll(setI);
						set.addAll(setJ);
						set = applyFunctionF(graph, floyd, set, k);
						if (minCardinality > set.size()) minCardinality = set.size();
						if (isAntiResolvingSet(floyd, set, graph, k)) {
							if (minCardinalityOfBasis > set.size()) {
								minCardinalityOfBasis = set.size();
								basis = set;
							}
						}
						nextCandidates.add(set);
					}
					j++;
				}
				i++;
			}
			currentCandidates = nextCandidates;
			next = !next;
			if (basis != null && minCardinalityOfBasis <= minCardinality) return new Output(STATE.POSITIVE, minCardinalityOfBasis, basis);
		}
		for (Set<String> lastSet : currentCandidates){
			if (lastSet.size() < graph.vertexSet().size()) return new Output(STATE.UNKNOWN, 0, null);
		}
		return new Output(STATE.NEGATIVE, 0, null);
	}
	
	private static Set<String> applyFunctionF(
			UndirectedGraph<String, DefaultEdge> graph,
			FloydWarshallShortestPaths<String, DefaultEdge> floyd,
			Set<String> set, int k) {
		Hashtable<String, Set<String>> eqClasses = getEqClasses(floyd, set, graph, k);
		Set<String> result = new TreeSet<>();
		result.addAll(set);
		boolean recursive = false;
		for (Set<String> eqClass : eqClasses.values()){
			if (eqClass.size() < k){
				result.addAll(eqClass);
				recursive = true;
			}
		}
		if (recursive) return applyFunctionF(graph, floyd, result, k);
		else return result;
	}

	private static boolean inclusion(Set<String> setI, Set<String> setJ) {
		return setI.containsAll(setJ) || setJ.containsAll(setI); 
	}


	private static boolean isAntiResolvingSet(
			FloydWarshallShortestPaths<String, DefaultEdge> floyd,
			Set<String> antiResolving,
			UndirectedGraph<String, DefaultEdge> graph, int k) {
		if (antiResolving.isEmpty()) return false;
		if (antiResolving.size() == graph.vertexSet().size()) return false;
		Hashtable<String, Set<String>> result = new Hashtable<>();
		for (String out : graph.vertexSet()){
			if (antiResolving.contains(out)) continue;
			String key = "";
			for (String in : antiResolving){
				int distance = (int)floyd.shortestDistance(in, out);
				key += distance+"-";
			}
			if (!result.containsKey(key)){
				result.put(key, new TreeSet<String>());
			}
			result.get(key).add(out);
		}
		boolean b = false;
		for (Set<String> eqClass : result.values()){
			if (eqClass.size() < k) return false;
			if (eqClass.size() == k) b = true;
		}
		return b;
	}

	private static Hashtable<Integer, Set<String>>  getEqClasses(
			FloydWarshallShortestPaths<String, DefaultEdge> floyd, String v,
			Set<String> antiResolving,
			UndirectedGraph<String, DefaultEdge> graph, int k) {
		Hashtable<Integer, Set<String>> result = new Hashtable<>();
		for (String x : graph.vertexSet()){
			if (antiResolving.contains(x) || x.equals(v)) continue;
			int distance = (int)floyd.shortestDistance(v, x);
			if (!result.containsKey(distance)){
				result.put(distance, new TreeSet<String>());
			}
			result.get(distance).add(x);
		}
		return result;
	}
	
	private static Hashtable<String, Set<String>>  getEqClasses(
			FloydWarshallShortestPaths<String, DefaultEdge> floyd,
			Set<String> set,
			UndirectedGraph<String, DefaultEdge> graph, int k) {
		Hashtable<String, Set<String>> result = new Hashtable<>();
		for (String x : graph.vertexSet()){
			if (set.contains(x)) continue;
			String distance = "";
			for (String y : set){
				distance += floyd.shortestDistance(x, y);
			}
			if (!result.containsKey(distance)){
				result.put(distance, new TreeSet<String>());
			}
			result.get(distance).add(x);
		}
		return result;
	}
	
	
	public enum STATE{
		POSITIVE, NEGATIVE, UNKNOWN
	}
	

}
