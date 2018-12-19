package multiset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.sun.xml.internal.stream.util.ThreadLocalBufferAllocator;

import Dimension.Resolving;
import util.PowerSetEnum;
import util.Print;

public class TreeDimension {

//	public static void main(String[] args) throws FileNotFoundException {
//		
//		System.setOut(new PrintStream(new File("out.txt")));
//		int depth = 8;
//		System.out.println("Starting");
//		int estimatedBound = 1; 
//		for (int i = 1; i <= depth; i++) {
//			Print.print("Looking bases for depth = "+i+" with estimated bound = "+estimatedBound);
//			SimpleGraph<String, DefaultEdge> tree = generateFullBinaryTree(i);
//			//iniBound = tree.vertexSet().size();
//			Print.printGraph(tree);
//			Set<Set<String>> bases = findMultisetBases(tree, estimatedBound);
//			System.out.println("The bases are: ");
//			for (Set<String> base : bases) {
//				Print.printList(base);
//			}
//			estimatedBound = bases.iterator().next().size()*2+1;
//		}
//		System.out.println("End");
//	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		File f = new File("./states");
		if (!f.exists()) f.mkdir();
		mainParallel(Integer.parseInt(args[0]));
	}

	public static void mainParallel(int degree) throws NumberFormatException, IOException {

		System.out.println("Starting for degree = "+degree);
		System.setOut(new PrintStream(new File("out-"+degree+".txt")));
		int estimatedLowerBound = 1; 
		int estimatedUpperBound = degree-1; 
		int depth = 1;
		Set<Set<String>> bases = null;
		do {
			Print.print("Looking bases for depth = "+depth+" with estimated lower bound = "
					+estimatedLowerBound+ " and estimated upper bound = "+estimatedUpperBound);
			SimpleGraph<String, DefaultEdge> tree = generateFullTree(depth, degree);
			//iniBound = tree.vertexSet().size();
			Print.printGraph(tree);
			ExecutorService executor = Executors.newFixedThreadPool(estimatedUpperBound-estimatedLowerBound+1);
			BaseFinder[] finders = new BaseFinder[estimatedUpperBound-estimatedLowerBound+1];
			Future[] finderStatus = new Future[estimatedUpperBound-estimatedLowerBound+1]; 
			for (int i = estimatedLowerBound; i<= estimatedUpperBound; i++) {
				finders[i-estimatedLowerBound] = new BaseFinder(tree, i, degree, depth);
				finderStatus[i-estimatedLowerBound]  = executor.submit(finders[i-estimatedLowerBound]);
			}
			Print.print("Waiting for threats to finish");
			int optimum = 0;
			bases = null;
			do {
				for (int i = 0; i < finders.length; i++) {
					if (finderStatus[i].isDone() && i == optimum) {
						if (finders[i].bases().isEmpty()) {
							optimum++;
						}
						else {
							bases = finders[i].bases();
							break;
						}
					}
				}
			} while (bases == null);
			executor.shutdownNow();
			Print.print("All threats finished");
			System.out.println("The bases are: ");
			for (Set<String> base : bases) {
				Print.printList(base);
			}
			estimatedLowerBound = bases.iterator().next().size()*degree;
			estimatedUpperBound = bases.iterator().next().size()*degree+degree-1;
			depth++;
		} while (bases.size() < degree+1);
		System.out.println("End");
	}

	public static void mainSequential(String[] args) throws FileNotFoundException {

		for (int degree = 2; degree < 9; degree++) {
			System.out.println("Starting for degree = "+degree);
			System.setOut(new PrintStream(new File("out-"+degree+".txt")));
			int estimatedLowerBound = 1; 
			int estimatedUpperBound = degree; 
			int depth = 1;
			Set<Set<String>> bases = null;
			do {
				Print.print("Looking bases for depth = "+depth+" with estimated lower bound = "
						+estimatedLowerBound+ " and estimated upper bound = "+estimatedUpperBound);
				SimpleGraph<String, DefaultEdge> tree = generateFullTree(depth, degree);
				//iniBound = tree.vertexSet().size();
				Print.printGraph(tree);
				bases = findMultisetBases(tree, estimatedLowerBound, estimatedUpperBound);
				System.out.println("The bases are: ");
				for (Set<String> base : bases) {
					Print.printList(base);
				}
				estimatedLowerBound = bases.iterator().next().size()*degree;
				estimatedUpperBound = bases.iterator().next().size()*degree+degree-1;
				depth++;
			} while (bases.size() < degree+1);
			System.out.println("End");
		}
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
	
	public static SimpleGraph<String, DefaultEdge> generateFullTree(int depth, int degree) {
		SimpleGraph<String, DefaultEdge> tree = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		long vertexSetSize = (long)(Math.pow(degree, depth+1)-1)/(degree-1);
		for (int i = 1; i <= vertexSetSize; i++) {
			tree.addVertex(i+"");
		}
		for (int level = 1; level <= depth; level++) {
			long firstVertex = (long)(Math.pow(degree, level)-1)/(degree-1)+1;
			long lastVertex = (long)(Math.pow(degree, level+1)-1)/(degree-1);
			for (long vertex = firstVertex; vertex <= lastVertex; vertex++) {
				String parentVertex = (((vertex-firstVertex)/degree)+(long)(Math.pow(degree, level-1)-1)/(degree-1)+1)+""; 
				tree.addEdge(parentVertex, vertex+"");
			}
		}
		return tree;
	}

	public static Set<Set<String>> findMultisetBases(SimpleGraph<String, DefaultEdge> tree, int estimatedLowerBound, 
			int estimatedUpperBound) {
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(tree);
		Set<String> vertices = tree.vertexSet();
		PowerSetEnum<String> powersetEnum = new PowerSetEnum<>(vertices, estimatedLowerBound, estimatedUpperBound, 0);
		
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
	
	public static class BaseFinder implements Runnable{

		SimpleGraph<String, DefaultEdge> tree;
		int baseSize;
		int degree;
		int startingSeedForEnumerator;
		Set<Set<String>> result;
		String fileNameEnum;
		String fileNameResult;
		
		public BaseFinder(SimpleGraph<String, DefaultEdge> tree, int baseSize, int degree, int depth) throws NumberFormatException, IOException {
			this.tree = tree;
			this.baseSize = baseSize;
			this.degree = degree;
			fileNameEnum = "./states/depth-"+depth+"-degree-"+degree+"-baseSize-"+baseSize+".txt";
			fileNameResult = "./states/depth-"+depth+"-degree-"+degree+"-baseSize-"+baseSize+".obj";
			File f = new File(fileNameEnum);
			if (!f.exists()) {
				Writer out = new FileWriter(fileNameEnum, false);
				out.write("0");
				out.close();
			}
			f = new File(fileNameResult);
			if (!f.exists()) {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
				out.writeObject(new HashSet<>());
				out.close();
			}
		}

		@Override
		public void run() {
			Print.print("Looking bases of size = "+baseSize);
			FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(tree);
			Set<String> vertices = tree.vertexSet();
			PowerSetEnum<String> powersetEnum = new PowerSetEnum<>(vertices, baseSize, baseSize, startingSeedForEnumerator);
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(fileNameEnum)));
				this.startingSeedForEnumerator = Integer.parseInt(br.readLine());
				br.close();
				System.out.println("Re-starting with seed: "+startingSeedForEnumerator+
						" in file name "+fileNameResult);
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(new File(fileNameResult)));
				result = (HashSet)input.readObject();
				input.close();
				System.out.println("Re-starting with bases in file name "+fileNameResult);
				for (Set<String> base : result) {
					System.out.println(base.toString());
				}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
			}
			
			int waitingPeriod = 1000;
			int rounds = 0;

			int minimum = vertices.size();
			while (powersetEnum.hasMoreElements()) {
			//for (Set<String> set : powerset) {
				Set<String> set = powersetEnum.nextElement();
				if (set.size() > minimum) continue;
				if (Resolving.isMultisetResolving(set, tree, floyd)){
					Print.print("Another optimal of size "+set.size()+" has been found:" + set.toString());
					result.add(set);
					result = removeIsomorphicResults(result, "1", floyd);
					if (result.size() > degree) {
						Print.print("Finished looking for bases of size = "+baseSize);
						return;
					}
				}
				rounds++;
				if (rounds == waitingPeriod) {
					ObjectOutputStream outResult;
					try {
						startingSeedForEnumerator += rounds;
						outResult = new ObjectOutputStream(new FileOutputStream(new File(fileNameResult)));
						outResult.writeObject(result);
						outResult.close();
						Writer outenum = new FileWriter(fileNameEnum, false);
						outenum.write(startingSeedForEnumerator+"");
						outenum.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rounds = 0;
				}
			}
		}

		public Set<Set<String>> bases(){
			return result;
		}
	}

}
