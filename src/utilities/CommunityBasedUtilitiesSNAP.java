package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import java.lang.Math;

import util.GraphUtil;

public class CommunityBasedUtilitiesSNAP {
	
	protected String uniquePrefixOriginal, uniquePrefixAnonymized;
	
	public CommunityBasedUtilitiesSNAP(String prefix) {
		uniquePrefixOriginal = prefix + "-original-";
		uniquePrefixAnonymized = prefix + "-anonymized-";
	}
	
	public List<Double> computeUtilities(UndirectedGraph<String, DefaultEdge> originalGraph,
									   UndirectedGraph<String, DefaultEdge> anonymizedGraph,
									   int algId, List<Integer> measureIds) throws IOException {
		try {
			HashSet<HashSet<String>> originalCommunities = new HashSet<>();
			HashSet<HashSet<String>> anonymizedCommunities = new HashSet<>();
			switch (algId) {
			case 0:   // BigClam
				originalCommunities = computeCommunitiesBigClam(originalGraph, uniquePrefixOriginal);
				anonymizedCommunities = computeCommunitiesBigClam(anonymizedGraph, uniquePrefixAnonymized);
				break;
			case 1:   // CoDA (inbound)
				originalCommunities = computeCommunitiesCoda(originalGraph, uniquePrefixOriginal, true);
				anonymizedCommunities = computeCommunitiesCoda(anonymizedGraph, uniquePrefixAnonymized, true);
				break;
			case 2:   // CoDA (outbound)
				originalCommunities = computeCommunitiesCoda(originalGraph, uniquePrefixOriginal, false);
				anonymizedCommunities = computeCommunitiesCoda(anonymizedGraph, uniquePrefixAnonymized, false);
				break;
			case 3:   // Girvan-Newman
				originalCommunities = computeCommunitiesOther(originalGraph, uniquePrefixOriginal, 1);
				anonymizedCommunities = computeCommunitiesOther(anonymizedGraph, uniquePrefixAnonymized, 1);
				break;
			case 4:   // Clauset-Newman-Moore
				originalCommunities = computeCommunitiesOther(originalGraph, uniquePrefixOriginal, 2);
				anonymizedCommunities = computeCommunitiesOther(anonymizedGraph, uniquePrefixAnonymized, 2);
				break;
			case 5:   // InfoMap
				originalCommunities = computeCommunitiesOther(originalGraph, uniquePrefixOriginal, 3);
				anonymizedCommunities = computeCommunitiesOther(anonymizedGraph, uniquePrefixAnonymized, 3);
				break;
			default:
				break;
			}
			List<Double> measureValues = new ArrayList<>();
			for (int measureId : measureIds) 
				switch (measureId) {
				case 0:   // difference in quantity
					measureValues.add((double)(originalCommunities.size() - anonymizedCommunities.size()));
					break;
				case 1:   // size distribution KL
					measureValues.add(sizeDistributionKL(originalCommunities, anonymizedCommunities));
					break;
				case 2:   // F1
					measureValues.add(twoWayAveragedF1(originalCommunities, anonymizedCommunities));
					break;
				default:
					measureValues.add(0d);
				}
			return measureValues;
		}
		catch (InterruptedException exep) {
			return new ArrayList<Double>();
		}
	}
	
	HashSet<HashSet<String>> computeCommunitiesBigClam(UndirectedGraph<String, DefaultEdge> graph, String prefix) throws IOException, InterruptedException {
		
		// Create input file for SNAP
		if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
			GraphUtil.outputSNAPFormat(graph, "C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\" + prefix + "Graph.txt");
		else
			GraphUtil.outputSNAPFormat(graph, "./Snap-3.0/examples/bigclam/" + prefix + "Graph.txt");
		
		// Run SNAP
		
		Runtime rt = Runtime.getRuntime();
		String command = null;
		if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
			command = "C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\bigclam -i:C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\" + prefix + "Graph.txt -o:" + prefix;
		else
			command = "./Snap-3.0/examples/bigclam/bigclam -i:./Snap-3.0/examples/bigclam/" + prefix + "Graph.txt -o:" + prefix;
		Process proc = rt.exec(command);
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		reader.close();
		proc.waitFor();
		
		// Load SNAP's output
		return readCommunitiesFromOutput(prefix + "cmtyvv.txt", true);
	}
	
	HashSet<HashSet<String>> computeCommunitiesCoda(UndirectedGraph<String, DefaultEdge> graph, String prefix, boolean computeInbound) throws IOException, InterruptedException {
				
		// Create input file for SNAP
		if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
			GraphUtil.outputSNAPFormat(graph, "C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\" + prefix + "Graph.txt");
		else
			GraphUtil.outputSNAPFormat(graph, "./Snap-3.0/examples/coda/" + prefix + "Graph.txt");
		
		// Run SNAP
		
		Runtime rt = Runtime.getRuntime();
		String command = null;
		if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
			command = "C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\coda -i:C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\" + prefix + "Graph.txt -o:" + prefix + " -g:1";
		else
			command = "./Snap-3.0/examples/coda/coda -i:./Snap-3.0/examples/coda/" + prefix + "Graph.txt -o:" + prefix + " -g:1";
		Process proc = rt.exec(command);
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		reader.close();
		proc.waitFor();
		
		// Load SNAP's output
		return readCommunitiesFromOutput(prefix + "cmtyvv." + ((computeInbound)? "in" : "out") + ".txt", true);
	}
	
	HashSet<HashSet<String>> computeCommunitiesOther(UndirectedGraph<String, DefaultEdge> graph, String prefix, int algId) throws IOException, InterruptedException {
		
		// Create input file for SNAP
		if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
			GraphUtil.outputSNAPFormat(graph, "C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\community\\" + prefix + "Graph.txt");
		else
			GraphUtil.outputSNAPFormat(graph, "./Snap-3.0/examples/community/" + prefix + "Graph.txt");
		
		// Run SNAP
		
		Runtime rt = Runtime.getRuntime();
		String command = null;
		if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
			command = "C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\community\\community -i:C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\community\\" + prefix + "Graph.txt -a:" + algId + " -o:" + prefix + "Out" + algId + ".txt";
		else
			command = "./Snap-3.0/examples/community/community -i:./Snap-3.0/examples/community/" + prefix + "Graph.txt -a:" + algId + " -o:" + prefix + "Out" + algId + ".txt";
		Process proc = rt.exec(command);
		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		reader.close();
		proc.waitFor();
		
		// Load SNAP's output and return
		return readCommunitiesFromOutput(prefix + "Out" + algId + ".txt", false);
	}
	
	HashSet<HashSet<String>> readCommunitiesFromOutput(String readerName, boolean containsListOfSets) throws IOException {
		
		if (containsListOfSets) {   // Outputs produced by BigClam and CoDA
			HashSet<HashSet<String>> communities = new HashSet<>();
			BufferedReader commReader = new BufferedReader(new FileReader(readerName));
	        String s = null;
	        while ((s = commReader.readLine()) != null) {
	            String[] stringArray = s.split("\\s+");
	            HashSet<String> community = new HashSet<String>();
	            for (String vert : stringArray)
	            	community.add(vert);
	            communities.add(community);   
	        }
	        commReader.close();
			return communities;
		}
		else {   // Outputs produced by Girvan-Newman, Clauset-Newman-Moore and InfoMap
			HashMap<String, HashSet<String>> numberedCommunities = new HashMap<>();			
			BufferedReader commReader = new BufferedReader(new FileReader(readerName));
	        String s = null;
	        while ((s = commReader.readLine()) != null) {
	        	if (!s.trim().startsWith("#")) {
		            String[] stringArray = s.split("\\s+");
		            String vertId = stringArray[0];
		            for (int ind = 1; ind < stringArray.length; ind++) {
		            	if (numberedCommunities.containsKey(stringArray[ind]))
		            		numberedCommunities.get(stringArray[ind]).add(vertId);
		            	else {
		            		HashSet<String> newComm = new HashSet<>();
		            		newComm.add(vertId);
		            		numberedCommunities.put(stringArray[ind], newComm);
		            	}
		            }
	        	}
	        }
	        commReader.close();
			return new HashSet<>(numberedCommunities.values());
		}
	}
	
	double sizeDistributionKL(HashSet<HashSet<String>> originalCommunities, HashSet<HashSet<String>> anonymizedCommunities) {
		if (originalCommunities.size() == 0 || anonymizedCommunities.size() == 0)
			return 0d;
		else {
			// Get size distribution for original graph
			HashMap<Integer, Integer> communityCountPerSizeOriginal = new HashMap<>();
			for (HashSet<String> comm : originalCommunities) {
				if (communityCountPerSizeOriginal.containsKey(comm.size()))
					communityCountPerSizeOriginal.replace(comm.size(), communityCountPerSizeOriginal.get(comm.size()) + 1);
				else
					communityCountPerSizeOriginal.put(comm.size(), 1);
			}
			
			// Get size distribution for anonymized graph
			HashMap<Integer, Integer> communityCountPerSizeAnonymized = new HashMap<>();
			for (HashSet<String> comm : anonymizedCommunities) {
				if (communityCountPerSizeAnonymized.containsKey(comm.size()))
					communityCountPerSizeAnonymized.replace(comm.size(), communityCountPerSizeAnonymized.get(comm.size()) + 1);
				else
					communityCountPerSizeAnonymized.put(comm.size(), 1);
			}
			
			// Compute KL divergence of anonymized distribution w.r.t. original distribution, i.e.  D_{KL}(original || anonymized)
			double kl_sum = 0d; 
			for (int size : communityCountPerSizeOriginal.keySet()) {
				double probValOrig = (double)communityCountPerSizeOriginal.get(size) / (double)originalCommunities.size();
				// To avoid divisions by 0, probabilities are smoothed
				double probValAnon = 0.01d / (double)communityCountPerSizeAnonymized.size();   // Smoothed prob if no community of that size exists in the anonymized graph
				if (communityCountPerSizeAnonymized.containsKey(size))
					probValAnon += 0.99d * (double)communityCountPerSizeAnonymized.get(size) / (double)anonymizedCommunities.size();   // Updated if some community of that size does exist in the anonymized graph 
				kl_sum += probValOrig * Math.log(probValOrig / probValAnon);
			}
			return kl_sum;
		}
	}
	
	double sizeVectorDistance(HashSet<HashSet<String>> originalCommunities, HashSet<HashSet<String>> anonymizedCommunities, int distType) {
		
		ArrayList<Integer> sizesOriginal = new ArrayList<>();
		
		for (HashSet<String> comm : originalCommunities)
			sizesOriginal.add(comm.size());
		
		Collections.sort(sizesOriginal);
		Collections.reverse(sizesOriginal);
		
		ArrayList<Integer> sizesAnonymized = new ArrayList<>();
		
		for (HashSet<String> comm : anonymizedCommunities)
			sizesAnonymized.add(comm.size());
		
		Collections.sort(sizesAnonymized);
		Collections.reverse(sizesAnonymized);
		
		double sumDiffs = 0d;
		
		for (int i = 0; i < Integer.min(sizesOriginal.size(), sizesAnonymized.size()); i++)
			sumDiffs += Math.pow(Math.abs(sizesOriginal.get(i) - sizesAnonymized.get(i)), distType);
		
		for (int i = sizesOriginal.size(); i < sizesAnonymized.size(); i++)
			sumDiffs += Math.pow(sizesAnonymized.get(i), distType);
		
		for (int i = sizesAnonymized.size(); i < sizesOriginal.size(); i++)
			sumDiffs += Math.pow(sizesOriginal.get(i), distType);
		
		return Math.pow(sumDiffs, 1d/(double)distType);
	}
	
	double coocJaccardIndex(HashSet<HashSet<String>> originalCommunities, HashSet<HashSet<String>> anonymizedCommunities) {
		
		HashMap<String, Integer> cooccurrencesOriginal = new HashMap<>();
		
		for (HashSet<String> comm : originalCommunities)
			for (String v1 : comm)
				for (String v2 : comm)
					if (v1.compareTo(v2) < 0) {
						String cooc = "(" + v1 + "," + v2 + ")";
						if (cooccurrencesOriginal.containsKey(cooc))
							cooccurrencesOriginal.put(cooc, cooccurrencesOriginal.get(cooc) + 1);
						else
							cooccurrencesOriginal.put(cooc, 1);
					}
		
		HashMap<String, Integer> cooccurrencesAnonymized = new HashMap<>();
		
		for (HashSet<String> comm : anonymizedCommunities)
			for (String v1 : comm)
				for (String v2 : comm)
					if (v1.compareTo(v2) < 0) {
						String cooc = "(" + v1 + "," + v2 + ")";
						if (cooccurrencesAnonymized.containsKey(cooc))
							cooccurrencesAnonymized.put(cooc, cooccurrencesAnonymized.get(cooc) + 1);
						else
							cooccurrencesAnonymized.put(cooc, 1);
					}
		
		HashSet<String> intersection = new HashSet<>(cooccurrencesOriginal.keySet());
		intersection.retainAll(cooccurrencesAnonymized.keySet());
		HashSet<String> union = new HashSet<>(cooccurrencesOriginal.keySet());
		union.addAll(cooccurrencesAnonymized.keySet());
		
		double sumNum = 0d, sumDen = 0d;
		for (String cooc : union) {
			if (intersection.contains(cooc)) {
				sumNum += Math.min(cooccurrencesOriginal.get(cooc), cooccurrencesAnonymized.get(cooc));
				sumDen += Math.max(cooccurrencesOriginal.get(cooc), cooccurrencesAnonymized.get(cooc));
			}
			else if (cooccurrencesOriginal.containsKey(cooc))
				sumDen += cooccurrencesOriginal.get(cooc);
			else
				sumDen += cooccurrencesAnonymized.get(cooc);
		}
		
		if (sumDen > 0)
			return sumNum / sumDen;
		else
			return 0d;
	}
	
	double twoWayAveragedF1(HashSet<HashSet<String>> originalCommunities, HashSet<HashSet<String>> anonymizedCommunities) {
		
		// Mapping anonymized into original
		double avgF1AnonIntoOrig = 0d;
		if (originalCommunities.size() > 0) {
			double sum = 0d;
			for (HashSet<String> origComm : originalCommunities) {
				double maxF1 = -1d;
				for (HashSet<String> anonComm : anonymizedCommunities) {
					double F1 = pairwiseF1(origComm, anonComm);
					if (F1 > maxF1)
						maxF1 = F1;
				}
				sum += maxF1;
			}
			avgF1AnonIntoOrig = sum / (double)originalCommunities.size();
		}
		
		// Mapping original into anonymized
		double avgF1OrigIntoAnon = 0d;
		if (anonymizedCommunities.size() > 0) {
			double sum = 0d;
			for (HashSet<String> anonComm : anonymizedCommunities) {
				double maxF1 = -1d;
				for (HashSet<String> origComm : originalCommunities) {
					double F1 = pairwiseF1(origComm, anonComm);
					if (F1 > maxF1)
						maxF1 = F1;
				}
				sum += maxF1;
			}
			avgF1OrigIntoAnon = sum / (double)anonymizedCommunities.size();
		}
		
		return 0.5 * avgF1AnonIntoOrig + 0.5 * avgF1OrigIntoAnon;
	}
	
	double pairwiseF1(HashSet<String> gold, HashSet<String> cand) {
		HashSet<String> intersection = new HashSet<>(gold);
		intersection.retainAll(cand);
		double precision = (double)intersection.size() / (double)cand.size();
		double recall = (double)intersection.size() / (double)gold.size();
		double F1 = 0d;
		if (precision + recall > 0)
			F1 = 2 * precision * recall / (precision + recall);
		return F1;
	}
	
	public static void main(String [] args) throws IOException {
		
		String [] egoNetworkIds = new String[]{"414", "107", "698", "348", "686", "1684", "3980", "0", "3437", "1912"};
		String [] anonymousVersions = new String[]{"oddcycle", "shortestcycle", "largestcycle"};
		CommunityBasedUtilitiesSNAP utilityComputer = new CommunityBasedUtilitiesSNAP("");
		boolean numberCiclesGiven = true;
		HashMap<String, Double> f1Values = new HashMap<>();
		HashMap<String, HashMap<String, Double>> ratios = new HashMap<>();
		HashMap<String, HashMap<String, Double>> relErrors = new HashMap<>();
		for (String avId: anonymousVersions) {
			ratios.put(avId, new HashMap<String, Double>());
			relErrors.put(avId, new HashMap<String, Double>());
		}
		
		System.out.println("BIGCLAM:");
		System.out.println("");
		
		HashSet<HashSet<String>> unionGoldStandardCommunitiesFacebook = new HashSet<>();
		HashSet<HashSet<String>> unionBigClamCommunitiesFacebook = new HashSet<>();
		
		System.out.println("F1 values per ego-network:");
		for (String enId : egoNetworkIds) {
			HashSet<HashSet<String>> goldStandardCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\Users\\yunior.ramirez\\workspace\\graph-anonymity\\facebook\\facebook\\" + enId + ".circles.nocid", true);
			unionGoldStandardCommunitiesFacebook.addAll(goldStandardCommunitiesFacebook);
			HashSet<HashSet<String>> bigclamCommunitiesOrigFacebook = null;
			if (numberCiclesGiven)   // Load communities computed with the number of circles given to the algorithm
				bigclamCommunitiesOrigFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\ego-networks\\facebook-replication-exp-ego-network-" + enId + "-original-cmtyvv.txt", true);
			else   // Load communities computed allowing the algorithm to determine the number of communities
				bigclamCommunitiesOrigFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\ego-networks\\facebook-replication-exp-ccng-ego-network-" + enId + "-original-cmtyvv.txt", true);
			unionBigClamCommunitiesFacebook.addAll(bigclamCommunitiesOrigFacebook);
			
			double f1Orig = utilityComputer.twoWayAveragedF1(goldStandardCommunitiesFacebook, bigclamCommunitiesOrigFacebook);
			f1Values.put(enId, f1Orig);
			System.out.println("Original " + enId + ": " + f1Orig);
			
			for (String avId : anonymousVersions) {
				HashSet<HashSet<String>> bigclamCommunitiesAnonFacebook = null;
				if (numberCiclesGiven)   // Load communities computed with the number of circles given to the algorithm
					bigclamCommunitiesAnonFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\ego-networks\\facebook-replication-exp-ego-network-" + enId + "-anonymized-" + avId + "-cmtyvv.txt", true);
				else   // Load communities computed allowing the algorithm to determine the number of communities
					bigclamCommunitiesAnonFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\ego-networks\\facebook-replication-exp-ccng-ego-network-" + enId + "-anonymized-" + avId + "-cmtyvv.txt", true);
				double f1Anon = utilityComputer.twoWayAveragedF1(goldStandardCommunitiesFacebook, bigclamCommunitiesAnonFacebook);
				System.out.println("Anon. ver. " + avId + " " + enId + ": " + f1Anon);
				System.out.println("\tF1: " + f1Anon);
				System.out.println("\tUtility (ratio): " + f1Anon / f1Orig);
				ratios.get(avId).put(enId, f1Anon / f1Orig);
				System.out.println("\tUtility (relative error): " + Math.abs(f1Anon - f1Orig) / f1Orig);
				relErrors.get(avId).put(enId, Math.abs(f1Anon - f1Orig) / f1Orig);
			}
		}
		
		double sum = 0d;
		for (String enId : f1Values.keySet())
			sum += f1Values.get(enId);
		double mean = sum / (double)f1Values.size();
		
		System.out.println("");
		System.out.println("Mean F1: " + mean);
		
		for (String avId : anonymousVersions) {
			sum = 0d;
			for (String enId : ratios.get(avId).keySet())
				sum += ratios.get(avId).get(enId);
			mean = sum / (double)ratios.get(avId).size();
			System.out.println("Mean ratio anon. ver. " + avId + ": " + mean);
			
			sum = 0d;
			for (String enId : relErrors.get(avId).keySet())
				sum += relErrors.get(avId).get(enId);
			mean = sum / (double)relErrors.get(avId).size();
			System.out.println("Mean rel. error anon. ver. " + avId + ": " + mean);
			
			sum = 0d;
			for (String enId : relErrors.get(avId).keySet())
				sum += relErrors.get(avId).get(enId) * relErrors.get(avId).get(enId);
			mean = sum / (double)relErrors.get(avId).size();
			System.out.println("Mean squared error anon. ver. " + avId + ": " + mean);
		}
		
		sum = 0d;
		for (String enId : f1Values.keySet())
			sum += (f1Values.get(enId) - mean) * (f1Values.get(enId) - mean);
		double stdDev = sum / (double)f1Values.size();
		System.out.println("Std. dev.: " + stdDev);
		
		System.out.println("");
		System.out.println("F1 comparing the union of circles with the union of the algorithm output: " + utilityComputer.twoWayAveragedF1(unionGoldStandardCommunitiesFacebook, unionBigClamCommunitiesFacebook));
		
		System.out.println("");
		System.out.println("Community detection on the full Facebook graph");
		HashSet<HashSet<String>> goldStandardCommunitiesFullFacebook = utilityComputer.readCommunitiesFromOutput("C:\\Users\\yunior.ramirez\\workspace\\graph-anonymity\\facebook-circles.txt", true);
		HashSet<HashSet<String>> bigclamCommunitiesFullFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\facebook-replication-exp-ccng-full-original-cmtyvv.txt", true);
		System.out.println("Score: " + utilityComputer.twoWayAveragedF1(goldStandardCommunitiesFullFacebook, bigclamCommunitiesFullFacebook));
				
		System.out.println("======================================================");
		System.out.println("CODA, inbound \\cup outbound:");
		System.out.println("");
				
		HashSet<HashSet<String>> unionCoDAAllCommunitiesFacebook = new HashSet<>();
		
		f1Values = new HashMap<>();
		ratios = new HashMap<>();
		for (String avId: anonymousVersions) {
			ratios.put(avId, new HashMap<String, Double>());
			relErrors.put(avId, new HashMap<String, Double>());
		}
		
		System.out.println("F1 values per ego-network:");
		for (String enId : egoNetworkIds) {
			HashSet<HashSet<String>> goldStandardCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\Users\\yunior.ramirez\\workspace\\graph-anonymity\\facebook\\facebook\\" + enId + ".circles.nocid", true);
			HashSet<HashSet<String>> codaAllCommunitiesFacebook = null;
			if (numberCiclesGiven) {   // Load communities computed with the number of circles given to the algorithm
				codaAllCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\ego-networks\\facebook-replication-exp-ego-network-" + enId + "-original-cmtyvv.in.txt", true);
				codaAllCommunitiesFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\ego-networks\\facebook-replication-exp-ego-network-" + enId + "-original-cmtyvv.out.txt", true));
			}
			else {   // Load communities computed allowing the algorithm to determine the number of communities
				codaAllCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\ego-networks\\facebook-replication-exp-ccng-ego-network-" + enId + "-original-cmtyvv.in.txt", true);
				codaAllCommunitiesFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\ego-networks\\facebook-replication-exp-ccng-ego-network-" + enId + "-original-cmtyvv.out.txt", true));
			}
			unionCoDAAllCommunitiesFacebook.addAll(codaAllCommunitiesFacebook);
			
			double f1Orig = utilityComputer.twoWayAveragedF1(goldStandardCommunitiesFacebook, codaAllCommunitiesFacebook);
			f1Values.put(enId, f1Orig);
			System.out.println("Orginal " + enId + ": " + f1Orig);
			
			for (String avId : anonymousVersions) {
				HashSet<HashSet<String>> codaAllCommunitiesAnonFacebook = null;
				if (numberCiclesGiven) {   // Load communities computed with the number of circles given to the algorithm
					codaAllCommunitiesAnonFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\ego-networks\\facebook-replication-exp-ego-network-" + enId + "-anonymized-" + avId + "-cmtyvv.in.txt", true);
					codaAllCommunitiesAnonFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\ego-networks\\facebook-replication-exp-ego-network-" + enId + "-anonymized-" + avId + "-cmtyvv.out.txt", true));
				}
				else {   // Load communities computed allowing the algorithm to determine the number of communities
					codaAllCommunitiesAnonFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\ego-networks\\facebook-replication-exp-ccng-ego-network-" + enId + "-anonymized-" + avId + "-cmtyvv.in.txt", true);
					codaAllCommunitiesAnonFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\ego-networks\\facebook-replication-exp-ccng-ego-network-" + enId + "-anonymized-" + avId + "-cmtyvv.out.txt", true));
				}
				double f1Anon = utilityComputer.twoWayAveragedF1(goldStandardCommunitiesFacebook, codaAllCommunitiesAnonFacebook);
				System.out.println("Anon. ver. " + avId + " " + enId + ": " + f1Anon);
				System.out.println("\tF1: " + f1Anon);
				System.out.println("\tUtility (ratio): " + f1Anon / f1Orig);
				ratios.get(avId).put(enId, f1Anon / f1Orig);
				System.out.println("\tUtility (relative error): " + Math.abs(f1Orig - f1Anon) / f1Orig);
				relErrors.get(avId).put(enId, Math.abs(f1Orig - f1Anon) / f1Orig);
			}
		}
		
		sum = 0d;
		for (String enId : f1Values.keySet())
			sum += f1Values.get(enId);
		mean = sum / (double)f1Values.size();
		
		System.out.println("");
		System.out.println("Mean: " + mean);
		
		for (String avId : anonymousVersions) {
			sum = 0d;
			for (String enId : ratios.get(avId).keySet())
				sum += ratios.get(avId).get(enId);
			mean = sum / (double)ratios.get(avId).size();
			System.out.println("Mean ratio anon. ver. " + avId + ": " + mean);
			
			sum = 0d;
			for (String enId : relErrors.get(avId).keySet())
				sum += relErrors.get(avId).get(enId);
			mean = sum / (double)relErrors.get(avId).size();
			System.out.println("Mean rel. error anon. ver. " + avId + ": " + mean);
			
			sum = 0d;
			for (String enId : relErrors.get(avId).keySet())
				sum += relErrors.get(avId).get(enId) * relErrors.get(avId).get(enId);
			mean = sum / (double)relErrors.get(avId).size();
			System.out.println("Mean squared error anon. ver. " + avId + ": " + mean);
		}
		
		sum = 0d;
		for (String enId : f1Values.keySet())
			sum += (f1Values.get(enId) - mean) * (f1Values.get(enId) - mean);
		stdDev = sum / (double)f1Values.size();
		System.out.println("Std. dev.: " + stdDev);
		
		System.out.println("");
		System.out.println("F1 comparing the union of circles with the union of the algorithm output: " + utilityComputer.twoWayAveragedF1(unionGoldStandardCommunitiesFacebook, unionCoDAAllCommunitiesFacebook));
		
		System.out.println("");
		System.out.println("Community detection on the full Facebook graph");
		//HashSet<HashSet<String>> goldStandardCommunitiesFullFacebook = utilityComputer.readCommunitiesFromOutput("C:\\Users\\yunior.ramirez\\workspace\\graph-anonymity\\facebook-circles.txt", true);
		HashSet<HashSet<String>> codaAllCommunitiesFullFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\facebook-replication-exp-full-original-cmtyvv.in.txt", true);
		codaAllCommunitiesFullFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\facebook-replication-exp-full-original-cmtyvv.out.txt", true));
		System.out.println("Score: " + utilityComputer.twoWayAveragedF1(goldStandardCommunitiesFullFacebook, codaAllCommunitiesFullFacebook));
	}
	
	public static void oldMainKAISSubmission(String [] args) throws IOException {
		
		String [] anonymizedVersions = new String[]{"anonymized-oddcycle", "anonymized-shortestcycle", "anonymized-largestcycle"};
		
		CommunityBasedUtilitiesSNAP utilityComputer = new CommunityBasedUtilitiesSNAP("");
		
		HashSet<HashSet<String>> circles = null;
		
		HashSet<HashSet<String>> originalCommunitiesFacebook = null, originalCommunitiesPanzarasa = null, originalCommunitiesURV = null;
		HashSet<HashSet<String>> anonymizedCommunitiesFacebook = null, anonymizedCommunitiesPanzarasa = null, anonymizedCommunitiesURV = null; 
		
		// Ego-networks
		
		String [] egoNetworkIds = new String[]{"0", "107", "348", "414", "686", "698", "1684", "1912", "3437", "3980"};
		
		for (String enId : egoNetworkIds) {
			System.out.println("");
			System.out.println("Ego network " + enId);
			System.out.println("");
			System.out.println("Version\t\t\t\tvs. circles\t\t\t\tvs. computed communities original");
			circles = utilityComputer.readCommunitiesFromOutput("C:\\Users\\yunior.ramirez\\workspace\\graph-anonymity\\facebook\\facebook\\" + enId + ".circles.nocid", true);
			HashSet<HashSet<String>> originalCommunities = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\ego-network-" + enId + "-original-size-given-cmtyvv.txt", true);
			System.out.println("Original\t\t\t\t" + utilityComputer.twoWayAveragedF1(circles, originalCommunities) + "\t\t\t\t" + utilityComputer.twoWayAveragedF1(originalCommunities, originalCommunities));
			for (String versionName : anonymizedVersions) {
				HashSet<HashSet<String>> anonymizedCommunities = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\ego-network-" + enId + "-" + versionName + "-size-given-cmtyvv.txt", true); 
				System.out.println(versionName + "\t\t\t\t" + utilityComputer.twoWayAveragedF1(circles, anonymizedCommunities) + "\t\t\t\t" + utilityComputer.twoWayAveragedF1(originalCommunities, anonymizedCommunities));
			}
		}
		
		// Facebook, comparing all outputs to set of circles
		
		circles = utilityComputer.readCommunitiesFromOutput("C:\\Users\\yunior.ramirez\\workspace\\graph-anonymity\\facebook-circles.txt", true);
		
		// BigClam
		
		System.out.println("Facebook, comparison vs circles, BigClam");
		System.out.println("");
		//originalCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\facebook-original-193c-cmtyvv.txt", true);
		originalCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\facebook-original-2nd-count-selection-cmtyvv.txt", true);
		System.out.println("original\t" + utilityComputer.twoWayAveragedF1(circles, originalCommunitiesFacebook));
		for (String versionName : anonymizedVersions) {
			anonymizedCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\facebook-" + versionName + "-2nd-count-selection-cmtyvv.txt", true);
			System.out.println(versionName + "\t" + utilityComputer.twoWayAveragedF1(circles, anonymizedCommunitiesFacebook));
		}
		
		// CoDA
		System.out.println("");
		System.out.println("Facebook, comparison vs circles, CoDA");
		System.out.println("");
		originalCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-original-193c-cmtyvv.in.txt", true);
		originalCommunitiesFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-original-193c-cmtyvv.out.txt", true));
		System.out.println("original\t" + utilityComputer.twoWayAveragedF1(circles, originalCommunitiesFacebook));
		for (String versionName : anonymizedVersions) {
			anonymizedCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-" + versionName + "-193c-cmtyvv.in.txt", true);
			anonymizedCommunitiesFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-" + versionName + "-193c-cmtyvv.out.txt", true));
			System.out.println(versionName + "\t" + utilityComputer.twoWayAveragedF1(circles, anonymizedCommunitiesFacebook));
		}
				
		// Facebook with 193 communities (number of circles)
		
		// BigClam
		System.out.println("");
		System.out.println("Facebook, 193 communities, BigClam");
		System.out.println("");
		originalCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\facebook-original-193c-cmtyvv.txt", true);
		for (String versionName : anonymizedVersions) {
			anonymizedCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\facebook-" + versionName + "-193c-cmtyvv.txt", true);
			System.out.println(versionName + "\t" + utilityComputer.twoWayAveragedF1(originalCommunitiesFacebook, anonymizedCommunitiesFacebook));
		}
		
		// CoDA
		System.out.println("");
		System.out.println("Facebook, 193 communities, CoDA");
		System.out.println("");
		originalCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-original-193c-cmtyvv.in.txt", true);
		originalCommunitiesFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-original-193c-cmtyvv.out.txt", true));
		for (String versionName : anonymizedVersions) {
			anonymizedCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-" + versionName + "-193c-cmtyvv.in.txt", true);
			anonymizedCommunitiesFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-" + versionName + "-193c-cmtyvv.out.txt", true));
			System.out.println(versionName + "\t" + utilityComputer.twoWayAveragedF1(originalCommunitiesFacebook, anonymizedCommunitiesFacebook));
		}
		
		// Sampling and exhaustive runs
		
		boolean sampling = (args.length != 1 || !args[0].equals("-exhaustive"));
		SecureRandom random = new SecureRandom();
		
		// BigClam
		
		HashMap<String, Double> sumsKLFB = new HashMap<>();
		HashMap<String, Double> sumsVDFB = new HashMap<>();
		HashMap<String, Double> sumsF1FB = new HashMap<>();
		for (String versionName : anonymizedVersions) {
			sumsKLFB.put(versionName, 0d);
			sumsVDFB.put(versionName, 0d);
			sumsF1FB.put(versionName, 0d);
		}
		HashMap<String, Double> sumsKLPanz = new HashMap<>();
		HashMap<String, Double> sumsVDPanz = new HashMap<>();
		HashMap<String, Double> sumsF1Panz = new HashMap<>();
		for (String versionName : anonymizedVersions) {
			sumsKLPanz.put(versionName, 0d);
			sumsVDPanz.put(versionName, 0d);
			sumsF1Panz.put(versionName, 0d);
		}
		HashMap<String, Double> sumsKLURV = new HashMap<>();
		HashMap<String, Double> sumsVDURV = new HashMap<>();
		HashMap<String, Double> sumsF1URV = new HashMap<>();
		for (String versionName : anonymizedVersions) {
			sumsKLURV.put(versionName, 0d);
			sumsVDURV.put(versionName, 0d);
			sumsF1URV.put(versionName, 0d);
		}
		int entryCount = 0;
		System.out.println("");
		if (sampling)
			System.out.println("Sampling BigClam");
		else
			System.out.println("Exhaustive on BigClam");
		
		for (int cc = 50; cc <= 150; cc++) {
			int countId = cc;
			if (sampling)
				countId = random.nextInt(101) + 50;				
			originalCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\facebook-original-" + countId + "c-cmtyvv.txt", true);
			originalCommunitiesPanzarasa = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\panzarasa-original-" + countId + "c-cmtyvv.txt", true);
			originalCommunitiesURV = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\urv-original-" + countId + "c-cmtyvv.txt", true);
			for (String versionName : anonymizedVersions) { 									
				anonymizedCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\facebook-" + versionName + "-" + countId + "c-cmtyvv.txt", true);					
				anonymizedCommunitiesPanzarasa = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\panzarasa-" + versionName + "-" + countId + "c-cmtyvv.txt", true);
				anonymizedCommunitiesURV = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\bigclam\\outputs\\global-mins-maxs\\urv-" + versionName + "-" + countId + "c-cmtyvv.txt", true);
				double klFB = utilityComputer.sizeDistributionKL(originalCommunitiesFacebook, anonymizedCommunitiesFacebook);
				double vdFB = utilityComputer.sizeVectorDistance(originalCommunitiesFacebook, anonymizedCommunitiesFacebook, 1);
				double f1FB = utilityComputer.twoWayAveragedF1(originalCommunitiesFacebook, anonymizedCommunitiesFacebook);
				sumsKLFB.put(versionName, sumsKLFB.get(versionName) + klFB);
				sumsVDFB.put(versionName, sumsVDFB.get(versionName) + vdFB);
				sumsF1FB.put(versionName, sumsF1FB.get(versionName) + f1FB);
				double klPanz = utilityComputer.sizeDistributionKL(originalCommunitiesPanzarasa, anonymizedCommunitiesPanzarasa);
				double vdPanz = utilityComputer.sizeVectorDistance(originalCommunitiesPanzarasa, anonymizedCommunitiesPanzarasa, 1);
				double f1Panz = utilityComputer.twoWayAveragedF1(originalCommunitiesPanzarasa, anonymizedCommunitiesPanzarasa);
				sumsKLPanz.put(versionName, sumsKLPanz.get(versionName) + klPanz);
				sumsVDPanz.put(versionName, sumsVDPanz.get(versionName) + vdPanz);
				sumsF1Panz.put(versionName, sumsF1Panz.get(versionName) + f1Panz);
				double klURV = utilityComputer.sizeDistributionKL(originalCommunitiesURV, anonymizedCommunitiesURV);
				double vdURV = utilityComputer.sizeVectorDistance(originalCommunitiesURV, anonymizedCommunitiesURV, 1);
				double f1URV = utilityComputer.twoWayAveragedF1(originalCommunitiesURV, anonymizedCommunitiesURV);
				sumsKLURV.put(versionName, sumsKLURV.get(versionName) + klURV);
				sumsVDURV.put(versionName, sumsVDURV.get(versionName) + vdURV);
				sumsF1URV.put(versionName, sumsF1URV.get(versionName) + f1URV);
			}
			entryCount++;
		}
		System.out.println("");
		System.out.println("Bigclam (averages)");
		System.out.println("");
		System.out.println("KL size distribution");
		System.out.println("");
		System.out.println("Method\tFacebook\tPanzarasa\tURV");
		for (String versionName : anonymizedVersions)
			System.out.println(versionName + "\t" + sumsKLFB.get(versionName) / (double)entryCount + "\t" + sumsKLPanz.get(versionName) / (double)entryCount + "\t" + sumsKLURV.get(versionName) / (double)entryCount);
		System.out.println("");
		System.out.println("Vector distance");
		System.out.println("");
		System.out.println("Method\tFacebook\tPanzarasa\tURV");
		for (String versionName : anonymizedVersions)
			System.out.println(versionName + "\t" + sumsVDFB.get(versionName) / (double)entryCount + "\t" + sumsVDPanz.get(versionName) / (double)entryCount + "\t" + sumsVDURV.get(versionName) / (double)entryCount);
		System.out.println("");
		System.out.println("F1");
		System.out.println("");
		System.out.println("Method\tFacebook\tPanzarasa\tURV");
		for (String versionName : anonymizedVersions)
			System.out.println(versionName + "\t" + sumsF1FB.get(versionName) / (double)entryCount + "\t" + sumsF1Panz.get(versionName) / (double)entryCount + "\t" + sumsF1URV.get(versionName) / (double)entryCount);
		System.out.println("");
		
		// CoDA
		
		sumsKLFB = new HashMap<>();
		sumsVDFB = new HashMap<>();
		sumsF1FB = new HashMap<>();
		for (String versionName : anonymizedVersions) {
			sumsKLFB.put(versionName, 0d);
			sumsVDFB.put(versionName, 0d);
			sumsF1FB.put(versionName, 0d);
		}
		sumsKLPanz = new HashMap<>();
		sumsVDPanz = new HashMap<>();
		sumsF1Panz = new HashMap<>();
		for (String versionName : anonymizedVersions) {
			sumsKLPanz.put(versionName, 0d);
			sumsVDPanz.put(versionName, 0d);
			sumsF1Panz.put(versionName, 0d);
		}
		sumsKLURV = new HashMap<>();
		sumsVDURV = new HashMap<>();
		sumsF1URV = new HashMap<>();
		for (String versionName : anonymizedVersions) {
			sumsKLURV.put(versionName, 0d);
			sumsVDURV.put(versionName, 0d);
			sumsF1URV.put(versionName, 0d);
		}
		entryCount = 0;
		System.out.println("");
		if (sampling)
			System.out.println("Sampling CoDA");
		else
			System.out.println("Exhaustive CoDA");
		for (int cc = 50; cc <= 150; cc++) {
			
			int countId = cc;
			if (sampling)
				countId = random.nextInt(101) + 50;
			originalCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-original-" + countId + "c-cmtyvv.in.txt", true);
			originalCommunitiesFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-original-" + countId + "c-cmtyvv.out.txt", true));
			originalCommunitiesPanzarasa = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\panzarasa-original-" + countId + "c-cmtyvv.in.txt", true);
			originalCommunitiesPanzarasa.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\panzarasa-original-" + countId + "c-cmtyvv.out.txt", true));
			originalCommunitiesURV = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\urv-original-" + countId + "c-cmtyvv.in.txt", true);
			originalCommunitiesURV.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\urv-original-" + countId + "c-cmtyvv.out.txt", true));
			for (String versionName : anonymizedVersions) {				
				anonymizedCommunitiesFacebook = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-" + versionName + "-" + countId + "c-cmtyvv.in.txt", true);
				anonymizedCommunitiesFacebook.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\facebook-" + versionName + "-" + countId + "c-cmtyvv.out.txt", true));
				anonymizedCommunitiesPanzarasa = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\panzarasa-" + versionName + "-" + countId + "c-cmtyvv.in.txt", true);
				anonymizedCommunitiesPanzarasa.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\panzarasa-" + versionName + "-" + countId + "c-cmtyvv.out.txt", true));
				anonymizedCommunitiesURV = utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\urv-" + versionName + "-" + countId + "c-cmtyvv.in.txt", true);
				anonymizedCommunitiesURV.addAll(utilityComputer.readCommunitiesFromOutput("C:\\cygwin64\\home\\yunior.ramirez\\graph-anonymity\\Snap-3.0\\examples\\coda\\outputs\\global-mins-maxs\\urv-" + versionName + "-" + countId + "c-cmtyvv.out.txt", true));				
				double klFB = utilityComputer.sizeDistributionKL(originalCommunitiesFacebook, anonymizedCommunitiesFacebook);
				double vdFB = utilityComputer.sizeVectorDistance(originalCommunitiesFacebook, anonymizedCommunitiesFacebook, 1);
				double f1FB = utilityComputer.twoWayAveragedF1(originalCommunitiesFacebook, anonymizedCommunitiesFacebook);
				sumsKLFB.put(versionName, sumsKLFB.get(versionName) + klFB);
				sumsVDFB.put(versionName, sumsVDFB.get(versionName) + vdFB);
				sumsF1FB.put(versionName, sumsF1FB.get(versionName) + f1FB);
				double klPanz = utilityComputer.sizeDistributionKL(originalCommunitiesPanzarasa, anonymizedCommunitiesPanzarasa);
				double vdPanz = utilityComputer.sizeVectorDistance(originalCommunitiesPanzarasa, anonymizedCommunitiesPanzarasa, 1);
				double f1Panz = utilityComputer.twoWayAveragedF1(originalCommunitiesPanzarasa, anonymizedCommunitiesPanzarasa);
				sumsKLPanz.put(versionName, sumsKLPanz.get(versionName) + klPanz);
				sumsVDPanz.put(versionName, sumsVDPanz.get(versionName) + vdPanz);
				sumsF1Panz.put(versionName, sumsF1Panz.get(versionName) + f1Panz);
				double klURV = utilityComputer.sizeDistributionKL(originalCommunitiesURV, anonymizedCommunitiesURV);
				double vdURV = utilityComputer.sizeVectorDistance(originalCommunitiesURV, anonymizedCommunitiesURV, 1);
				double f1URV = utilityComputer.twoWayAveragedF1(originalCommunitiesURV, anonymizedCommunitiesURV);
				sumsKLURV.put(versionName, sumsKLURV.get(versionName) + klURV);
				sumsVDURV.put(versionName, sumsVDURV.get(versionName) + vdURV);
				sumsF1URV.put(versionName, sumsF1URV.get(versionName) + f1URV);
			}
			entryCount++;
		}
		System.out.println("");
		System.out.println("CoDA (averages)");
		System.out.println("");
		System.out.println("KL size distribution");
		System.out.println("");
		System.out.println("Method\tFacebook\tPanzarasa\tURV");
		for (String versionName : anonymizedVersions)
			System.out.println(versionName + "\t" + sumsKLFB.get(versionName) / (double)entryCount + "\t" + sumsKLPanz.get(versionName) / (double)entryCount + "\t" + sumsKLURV.get(versionName) / (double)entryCount);
		System.out.println("");
		System.out.println("Vector distance");
		System.out.println("");
		System.out.println("Method\tFacebook\tPanzarasa\tURV");
		for (String versionName : anonymizedVersions)
			System.out.println(versionName + "\t" + sumsVDFB.get(versionName) / (double)entryCount + "\t" + sumsVDPanz.get(versionName) / (double)entryCount + "\t" + sumsVDURV.get(versionName) / (double)entryCount);
		System.out.println("");
		System.out.println("F1");
		System.out.println("");
		System.out.println("Method\tFacebook\tPanzarasa\tURV");
		for (String versionName : anonymizedVersions)
			System.out.println(versionName + "\t" + sumsF1FB.get(versionName) / (double)entryCount + "\t" + sumsF1Panz.get(versionName) / (double)entryCount + "\t" + sumsF1URV.get(versionName) / (double)entryCount);
		System.out.println("");
		
	}
}
