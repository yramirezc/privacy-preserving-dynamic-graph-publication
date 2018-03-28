package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class CollectData {

	
	public static void main(String[] args) throws IOException {
		collectData();
	}
	
	public static void collectData() throws IOException {
		//File root = new File("./data10/"); 
		//File root = new File("./data11/"); 
		File root = new File("./data12/"); 
		List<File> filesBasis = getFilesBasis(root);
		List<File> filesSet = getFilesSet(root);
		Ratio resolving = collectDataSet(filesSet);
		Ratio basis = collectDataBasis(filesBasis);
		printTable(basis, "basis.DAT");
		printTable(resolving, "resolving.DAT");
	}

	private static void printTable(Ratio basis, String name) throws IOException {
		Hashtable<Integer, BufferedWriter> writers = new Hashtable<>();
		TreeSet<Integer> ks = new TreeSet<>();
		for (Pair p : basis.positive.keySet()){
			if (!writers.containsKey(p.y)){
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("m-"+p.y+"-"+name), false));
				writers.put(p.y, writer);
			}
			ks.add(p.x);
		}
		for (int m : writers.keySet()){
			for (int k : ks){
				String line = k+" \t ";
				for (Pair p : basis.positive.keySet()){
					if (p.x != k || p.y != m) continue;
					line += basis.positive.get(p)+" \t ";
					line += basis.negative.get(p)+" \t ";
					line += basis.unknown.get(p)+" \t ";
				}
				writers.get(m).write(line);
				writers.get(m).newLine();
			}
		}
		for (BufferedWriter writer: writers.values()) writer.close();
	}

	private static Ratio collectDataSet(List<File> files) throws IOException {
		System.out.println("Collecting resolving sets");
		return collectData(files, "set");
	}
	private static Ratio collectDataBasis(List<File> files) throws IOException {
		System.out.println("Collecting basis");
		return collectData(files, "basis");
	}

	static class Pair implements Comparable{
		public int x; 
		public int y;
		
		public Pair(int x, int y){
			this.x = x;
			this.y = y;
		}

		@Override
		public int compareTo(Object o) {
			Pair tmp = (Pair)o;
			if (x < tmp.x) return -1;
			else if (x == tmp.x){
				if (y < tmp.y) return -1;
				else if (y == tmp.y) return 0;
				else return 1;
			}
			else return 1;
		}
	}
	
	public static class Ratio{
		TreeMap<Pair, Double> positive;
		TreeMap<Pair, Double> negative;
		TreeMap<Pair, Double> unknown;
		public Ratio(TreeMap<Pair, Double> positive, TreeMap<Pair, Double> negative, 
				TreeMap<Pair, Double> unknown){
			this.positive = positive;
			this.negative = negative;
			this.unknown = unknown;
		}
		
	}
	
	private static Ratio collectData(List<File> files, String pattern) throws IOException {
		TreeMap<Pair, Integer> positive = new TreeMap<>();
		TreeMap<Pair, Integer> negative = new TreeMap<>();
		TreeMap<Pair, Integer> unknown = new TreeMap<>();
		TreeMap<Pair, Double> positiveResult = new TreeMap<>();
		TreeMap<Pair, Double> negativeResult = new TreeMap<>();
		TreeMap<Pair, Double> unknownResult = new TreeMap<>();
		int k,m,p, n,u;
		for (File f : files) {
			String fileName = f.getName();
			String[] split = fileName.split("(-|\\.)");
			k = Integer.parseInt(split[2]);
			m = Integer.parseInt(split[4]);
			Pair key = new Pair(k, m);
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();
			p = 0;
			n = 0;
			u = 0;
			while(line != null){
				if (line.contains("POSITIVE")) p++;
				if (line.contains("NEGATIVE")) n++;
				if (line.contains("UNKNOWN")) u++;
				line = reader.readLine();
			}
			if (!positive.containsKey(key)) positive.put(key, 0);
			if (!negative.containsKey(key)) negative.put(key, 0);
			if (!unknown.containsKey(key)) unknown.put(key, 0);
			positive.put(key, positive.get(key)+p);
			negative.put(key, negative.get(key)+n);
			unknown.put(key, unknown.get(key)+u);
		}
		for (Pair key : positive.keySet()) {
			String fileName1 = pattern+"-k-"+key.x+"-m-"+key.y+".DAT";
			String fileName2 = pattern+"-m-"+key.y+".DAT";
			File outFile1 = new File(fileName1);
			File outFile2 = new File(fileName2);
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(outFile1, false));
			BufferedWriter writer2 = new BufferedWriter(new FileWriter(outFile2, true));
			writer1.write("#POSITIVE \t NEGATIVE \t UNKNOWN");
			writer1.newLine();
			double total = positive.get(key)+negative.get(key)+unknown.get(key);
			double positiveRatio = ((double)positive.get(key)*100)/total;
			double negativeRatio = ((double)negative.get(key)*100)/total;
			double unknownRatio = ((double)unknown.get(key)*100)/total;
			positiveResult.put(key, positiveRatio);
			negativeResult.put(key, negativeRatio);
			unknownResult.put(key, unknownRatio);
			writer1.write(positiveRatio+" \t "+
						((double)negative.get(key)*100)/total+" \t "+((double)unknown.get(key)*100)/total);
			writer1.close();
			writer2.append(key.x+" \t "+((double)positive.get(key)*100)/total+" \t "+
						((double)negative.get(key)*100)/total+" \t "+((double)unknown.get(key)*100)/total);
			writer2.newLine();
			writer2.close();
			System.out.println("For k = "+key.x+" and m = "+key.y+" there were "+total+" data points");
		}
		return new Ratio(positiveResult, negativeResult, unknownResult);
	}

	private static List<File> getFilesBasis(File root) {
		return getFiles(root, "outputBasis-k");
	}
	private static List<File> getFilesSet(File root) {
		return getFiles(root, "output-k");
	}
	
	private static List<File> getFiles(File root, String pattern) {
		File[] files = root.listFiles();
		List<File> result = new LinkedList<>();
		if (files == null) return result;
		List<File> tmp = null;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()){
				tmp = getFiles(files[i], pattern);
				result.addAll(tmp);
			}
			else{
				if (files[i].getName().contains(pattern)){
					result.add(files[i]);
				}
			}
		}
		return result;
	}
}
