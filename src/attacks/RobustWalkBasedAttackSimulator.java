package attacks;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import codecs.Hamming74Code;
import codecs.LinearCode;
import codecs.NonEncodingCode;
import util.FSimCoincidenceCount;
import util.FingerprintSimilarity;
import util.GraphUtil;

public class RobustWalkBasedAttackSimulator extends SybilAttackSimulator {
	
	protected class FingerprintSetMatchingReturnValue {
		public Set<Map<Integer, String>> matches; 
		public int maxSimilarity;
		public FingerprintSetMatchingReturnValue(Set<Map<Integer, String>> matches, int maxSimilarity) {
			this.matches = matches;
			this.maxSimilarity = maxSimilarity;
		}
	}
	
	protected int maxEditDistance;
	protected boolean applySybilDegSeqOptimization;
	protected boolean useUniformlyDistributedFingerprints;
	protected boolean applyApproxFingerprintMatching;
	protected LinearCode codec;
	protected Set<String> uniformlyDistributedFingerprints;
	int lengthUnifDistFingerprints; 

	public RobustWalkBasedAttackSimulator(int maxEditDist, boolean sybDegSeqOpt, boolean apprFgPrMatch, boolean useErrorCorrectingFingerprints) {   // This constructor ignores fingerprint dispersion. Kept for back-compatibility
		maxEditDistance = maxEditDist;
		applySybilDegSeqOptimization = sybDegSeqOpt;
		useUniformlyDistributedFingerprints = false;
		uniformlyDistributedFingerprints = null;
		lengthUnifDistFingerprints = -1;
		applyApproxFingerprintMatching = apprFgPrMatch;
		if (useErrorCorrectingFingerprints)
			codec = new Hamming74Code();
		else
			codec = new NonEncodingCode();
	}
	
	public RobustWalkBasedAttackSimulator(int maxEditDist, boolean sybDegSeqOpt, boolean unifDistrFgPr, boolean apprFgPrMatch, boolean useErrorCorrectingFingerprints) {
		maxEditDistance = maxEditDist;
		applySybilDegSeqOptimization = sybDegSeqOpt;
		useUniformlyDistributedFingerprints = unifDistrFgPr;
		uniformlyDistributedFingerprints = null;
		lengthUnifDistFingerprints = -1;
		applyApproxFingerprintMatching = apprFgPrMatch;
		if (useErrorCorrectingFingerprints)
			codec = new Hamming74Code();
		else
			codec = new NonEncodingCode();
	}

	@Override
	public void simulateAttackerSubgraphCreation(UndirectedGraph<String, DefaultEdge> graph, int attackerCount, int victimCount) {
		
		/* The graph is assumed to satisfy all requirements, notably vertices being labeled from attackerCount on, 
		 * and connectivity if required
		 */
		
		SecureRandom random = new SecureRandom();
		int nonRedundantAttackerCount = codec.maxPossibleMessageLength(attackerCount);
		/**
		 * In an initial implementation, the number of sybils for the non-error-correcting fingerprint was set and then the larger number of sybils 
		 * for error-correcting fingerprints was determined. Now we do the opposite, an upper bound on the number of sybils is set, 
		 * and the code determines the maximum number of non-redundant fingerprints that can be encoded into this bound   
		 * */
		//int finalAttackerCount = attackerCount;
		//int chunkCount = (attackerCount % codec.getMessageLength() == 0)? attackerCount / codec.getMessageLength() : 1 + attackerCount / codec.getMessageLength();
		//finalAttackerCount = chunkCount * codec.getCodewordLength();  
		
		if (victimCount == 0)
			victimCount = 1;
		
		if (attackerCount + victimCount > graph.vertexSet().size())
			victimCount = graph.vertexSet().size() - attackerCount;
		
		// Add attacker vertices
		for (int j = 0; j < attackerCount; j++)
			graph.addVertex(j+"");
		
		if (useUniformlyDistributedFingerprints) {
			
			if (uniformlyDistributedFingerprints == null || uniformlyDistributedFingerprints.size() != victimCount || lengthUnifDistFingerprints != attackerCount) {   // Once a set of uniformly distributed fingerprints is created, it will be used later on as many times as possible
				lengthUnifDistFingerprints = attackerCount;
				uniformlyDistributedFingerprints = generateDistributedSetOfFingerprints(victimCount, attackerCount);
				if (uniformlyDistributedFingerprints.size() > victimCount) {
					ArrayList<String> fpArray = new ArrayList<>(uniformlyDistributedFingerprints);
					uniformlyDistributedFingerprints = new TreeSet<>();
					for (int mdf = 0; mdf < victimCount; mdf++) {
						int indSel = random.nextInt(fpArray.size());
						uniformlyDistributedFingerprints.add(fpArray.get(indSel));
						fpArray.remove(indSel);
					}
				}
			}
			// else the already available set of uniformly distributed fingerprints will be used 
			
			// Once the set of uniformly distributed fingerprints is ensured to be available, add necessary edges to the graph
			for (int j = attackerCount; j < attackerCount + victimCount; j++)
				for (String fingerprint : uniformlyDistributedFingerprints)	
					for (int k = 0; k < fingerprint.length(); k++) 
						if (fingerprint.charAt(k) == '1') 
							graph.addEdge(j+"", (k + attackerCount - fingerprint.length()) + "");
		}
		else {
			// Default to the manner in which fingerprints are generated in the original walk-based attack
			Hashtable<String, String> fingerprints = new Hashtable<>();
			for (int j = attackerCount; j < attackerCount + victimCount; j++) {
				String fingerprint = null;
				do {
					fingerprint = generateRandomFingerprint(random, nonRedundantAttackerCount);
					fingerprint = codec.encode(fingerprint);
					for (int z = 0; z < codec.trailingZeroCount(attackerCount); z++) 
						fingerprint = fingerprint + "0";
				} while (fingerprints.containsKey(fingerprint));
				
				fingerprints.put(fingerprint, fingerprint);
				
				for (int k = 0; k < fingerprint.length(); k++) 
					if (fingerprint.charAt(k) == '1') 
						graph.addEdge(j+"", (k + attackerCount - fingerprint.length()) + "");
			}
			
		}
		
		// Force existence of path setting order of sybils
		if (attackerCount > 1) 
			for (int k = 0; k < attackerCount - 1; k++) 
				graph.addEdge(k + "", (k+1) + "");
		
		if (applySybilDegSeqOptimization) {
		    
			/* 
		     * Eppstein et al.'s scale-free random graph
			 */
			
//			int avDegree = (attackerCount - 1) / 4;   // Needs to be parameterized
//			double gamma = 2.5;   // Needs to be parameterized
//			double K0 = avDegree * ((gamma - 2) * (gamma - 2)) / ((gamma - 1) * (gamma - 1));   // This parameter is defined in Eppstein et al.'s paper
//			for (int k = 0; k < attackerCount - 2; k++) {
//				for (int l = k + 2; l < attackerCount; l++) {
//					double prob = K0 * Math.pow((k + 1) * (l + 1) * Math.pow(attackerCount, gamma - 3), -1d / (gamma - 1));   // This computation is defined in Eppstein et al.'s paper
//					if (random.nextDouble() < prob && !graph.containsEdge(k + "", l + "")) {
//						graph.addEdge(k + "", l + "");
//					}
//				}
//			}
			
			/* 
		     * Erdos-Renyi with large p 
			 */
			
			double p = 0.75d;
			
			for (int k = 0; k < attackerCount - 2; k++) {
				for (int l = k + 2; l < attackerCount; l++) {
					if (random.nextDouble() < p && !graph.containsEdge(k + "", l + "")) {
						graph.addEdge(k + "", l + "");
					}
				}
			}
			
		}
		else {
			// Default to the manner in which internal connections are generated in the original walk-based attack
			for (int k = 0; k < attackerCount - 2; k++) {
				for (int l = k + 2; l < attackerCount; l++) {
					if (random.nextBoolean() && !graph.containsEdge(k + "", l + "")) {
						graph.addEdge(k + "", l + "");
					}
				}
			}
		}
		
	}
	
	protected String generateRandomFingerprint(SecureRandom random, int fpLength) {
		String fingerprint = Integer.toBinaryString(random.nextInt((int)Math.pow(2, fpLength) - 1) + 1);
		while (fingerprint.length() < fpLength)
			fingerprint = "0" + fingerprint;
		return fingerprint;
	}
	
	protected String generateRandomFingerprint(SecureRandom random, int fpLength, int approxNumberOfOnes) {
		String fingerprint = "";
		for (int i = 0; i < fpLength; i++)
			if (random.nextInt(fpLength) < approxNumberOfOnes)
				fingerprint += "1";
			else
				fingerprint += "0";
		return fingerprint;
	}
	
	protected Set<String> generateDistributedSetOfFingerprints(int fpCount, int fpLength) {
		int boundDist = 1;
		UndirectedGraph<String, DefaultEdge> grid = generateGrid(fpLength, boundDist);
		Set<String> independentSet = GraphUtil.greedyMaxIndependentSet(grid);
		Set<String> fingerprints = independentSet;
		while (independentSet.size() >= fpCount && boundDist <= fpLength) {
			boundDist++;
			grid = generateGrid(fpLength, boundDist);
			independentSet = GraphUtil.greedyMaxIndependentSet(grid);
			if (independentSet.size() >= fpCount)
				fingerprints = independentSet;
		}
		return fingerprints;
	}
	
	protected UndirectedGraph<String, DefaultEdge> generateGrid(int fpLength, int boundDist) {
		UndirectedGraph<String, DefaultEdge> grid = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		for (int i = 1; i < Math.pow(2, fpLength); i++) {   // The vertices of the grid will be all binary strings of length fpLength, except "00...0"
			String fingerprint = Integer.toBinaryString(i); 
			while (fingerprint.length() < fpLength)
				fingerprint = "0" + fingerprint;
			grid.addVertex(fingerprint);
			// Generate all edges from the new fingerprint to previously added fingerprints at distance boundDist or less
			for (String v : grid.vertexSet()) 
				if (!v.equals(fingerprint) && fpDistance(v, fingerprint) <= boundDist) 
					grid.addEdge(v, fingerprint);
		}
		return grid;
	}
	
	protected int fpDistance(String fp1, String fp2) {
		if (fp1.length() != fp2.length())
			return Integer.MAX_VALUE;
		int sizeSimmDiff = 0;
		for (int i = 0; i < fp1.length(); i++)
			if (fp1.charAt(i) != fp2.charAt(i))
				sizeSimmDiff++;
		return sizeSimmDiff;
	}

	@Override
	public double successProbability(int attackerCount, int victimCount, UndirectedGraph<String, DefaultEdge> anonymizedGraph, UndirectedGraph<String, DefaultEdge> originalGraph) {
		
		/**
		 * In an initial implementation, the number of sybils for the non-error-correcting fingerprint was set and then the larger number of sybils 
		 * for error-correcting fingerprints was determined. Now we do the opposite, an upper bound on the number of sybils is set, 
		 * and the code determines the maximum number of non-redundant fingerprints that can be encoded into this bound   
		 * */
		//int finalAttackerCount = attackerCount;
		//int chunkCount = (attackerCount % codec.getMessageLength() == 0)? attackerCount / codec.getMessageLength() : 1 + attackerCount / codec.getMessageLength();
		//finalAttackerCount = chunkCount * codec.getCodewordLength();
		
		int[] sybilVertexDegrees = new int[attackerCount];
		boolean[][] sybilVertexLinks = new boolean[attackerCount][attackerCount];
		
		for (int i = 0; i < attackerCount; i++) {   // Attackers are assumed to be the first attackerCount vertices in the graph, because of the manner in which the attack was simulated 
			sybilVertexDegrees[i] = originalGraph.degreeOf(i + "");
		}
		
		for (int i = 0; i < attackerCount; i++) {
			for (int j = 0; j < attackerCount; j++) {
				if (originalGraph.containsEdge(i + "", j + ""))
					sybilVertexLinks[i][j] = true;
				else 
					sybilVertexLinks[i][j] = false;
			}
		}
		
		List<String[]> candidates = getPotentialAttackerCandidatesBFS(sybilVertexDegrees, sybilVertexLinks, anonymizedGraph);  
		
		if (candidates.isEmpty()) 
			return 0;   
		
		double sumPartialSuccessProbs = 0;
		for (String[] candidate : candidates) { 
			
			double successProbForCandidate = 1d; 
			
			ArrayList<String> originalFingerprints = new ArrayList<>();
			for (int victim = attackerCount; victim < attackerCount + victimCount; victim++) {
				String fingerprint = "";
				for (int i = 0; i < attackerCount; i++) {
					if (originalGraph.containsEdge(i + "", victim + ""))
						fingerprint += "1";
					else 
						fingerprint += "0";
				}
				originalFingerprints.add(fingerprint);
			}
			
			if (applyApproxFingerprintMatching) {
				
				// We first find all the existing exact matchings, because no approximate search needs to be done for them
				
				Set<String> candAsSet = new HashSet<>();
				for (String cv : candidate)
					candAsSet.add(cv);
				
				// Compute all fingerprints
				HashMap<String, String> allFingerprints = new HashMap<>();
				for (String v : anonymizedGraph.vertexSet()) {
					String pvFingerprint = "";
					for (int i = 0; i < attackerCount; i++)
						if (anonymizedGraph.containsEdge(v, candidate[i]))
							pvFingerprint += "1";
						else
							pvFingerprint += "0";
					/**
					 * In an initial implementation, we were decoding the fingerprint for correcting possible errors.
					 * Now, we only use the encoding to increase edit-distance, but do not attempt to decode at this step 
					 */
					//pvFingerprint = codec.correctedCodeWord(pvFingerprint);
					if (pvFingerprint.indexOf("1") != -1)
						allFingerprints.put(v, pvFingerprint);
				}
				
				Set<Integer> exactlyMatchedVictims = new HashSet<>();
				boolean exactMatchFailed = false;
				
				for (int victim = attackerCount; victim < attackerCount + victimCount; victim++) {
					int bucketSizeVictim = 0;
					boolean victimInBucket = false;
					for (String v : anonymizedGraph.vertexSet()) 
						if (!candAsSet.contains(v) && allFingerprints.containsKey(v) && allFingerprints.containsKey("" + victim) && allFingerprints.get("" + victim).equals(allFingerprints.get(v))) {
							bucketSizeVictim++;
							if (v.equals("" + victim)) {
								victimInBucket = true;
								exactlyMatchedVictims.add(victim);
							}
							allFingerprints.remove(v);   // This will no longer be a potential candidate for approximate matchings
						}
					if (bucketSizeVictim > 0) {
						if (victimInBucket)
							successProbForCandidate *= 1d / (double)bucketSizeVictim;
						else { 
							successProbForCandidate = 0d;
							exactMatchFailed = true;
							break;
						}
					}
				}
				
				if (!exactMatchFailed && exactlyMatchedVictims.size() < originalFingerprints.size()) 
					if (originalFingerprints.size() - exactlyMatchedVictims.size() <= allFingerprints.size()) {
						
						//FingerprintSetMatchingReturnValue matchingResult = approxFingerprintMatching(anonymizedGraph, candidate, allFingerprints, originalFingerprints, matchedVictims);
						FingerprintSetMatchingReturnValue matchingResult = approxFingerprintMatching(allFingerprints, originalFingerprints, exactlyMatchedVictims, attackerCount);
						
						if (matchingResult.maxSimilarity <= 0)
							successProbForCandidate = 0d;
						else
							successProbForCandidate *= 1d / (double)matchingResult.matches.size();
						
					}
					else   // The remaining fingerprints are too few for the remaining unmatched victims 
						successProbForCandidate = 0d;
			}
			else {
				
				// We will apply exact fingerprint matching, which will be based on Rolando's implementation
				
				for (int victim = attackerCount; victim < attackerCount + victimCount; victim++) {
					
					int cardinalityOfTheSubset = 0;
					boolean victimInsideSubset = false;
					for (String vertex : anonymizedGraph.vertexSet()) {
						String tmpFingerprint = "";
						boolean vertInCandidate = false;
						for (int i = 0; !vertInCandidate && i < candidate.length; i++) {
							if (vertex.equals(candidate[i]))
								vertInCandidate = true;
							else if (anonymizedGraph.containsEdge(candidate[i], vertex))
								tmpFingerprint += "1";
							else
								tmpFingerprint += "0";
						}
						if (!vertInCandidate) {
							/**
							 * In an initial implementation, we were decoding the fingerprint for correcting possible errors.
							 * Now, we only use the encoding to increase edit-distance, but do not attempt to decode at this step 
							 */
							//if (useErrorCorrectingFingerprints)
								//tmpFingerprint = codec.correctedCodeWord(tmpFingerprint);
							if (tmpFingerprint.equals(originalFingerprints.get(victim - attackerCount))) {
								cardinalityOfTheSubset++;
								if (victim == Integer.parseInt(vertex))
									victimInsideSubset = true;
							}
						}
					}
					
					/* As implemented by Rolando, the probability to identify this victim 
					 * is either 0 or 1/cardinalityOfTheSubset
					 * The total probability of identifying all victims is the product
					 * While the probability becomes 0 if at least one victim cannot be identified*/
					
					if (cardinalityOfTheSubset != 0 && victimInsideSubset && successProbForCandidate != 0)
						successProbForCandidate *= 1d / cardinalityOfTheSubset;
					else
						successProbForCandidate = 0;
				}
			}
			
			// For each candidate we sum its probability of success. The total probability is the average
			sumPartialSuccessProbs += successProbForCandidate;
		}
		return sumPartialSuccessProbs / candidates.size();
	}
	
	protected List<String[]> getPotentialAttackerCandidatesBFS(int[] fingerprintDegrees, boolean[][] fingerprintLinks, UndirectedGraph<String, DefaultEdge> anonymizedGraph) {
		
		int minLocDistValue = 1 + (anonymizedGraph.vertexSet().size() * (anonymizedGraph.vertexSet().size() - 1)) / 2;   // One more than the maximal possible distance (the total amount of edges). This is "positive infinity" in this context.
		Set<String> vertsMinDistValue = new HashSet<>();
		for (String v : anonymizedGraph.vertexSet()) {
			int dist = Math.abs(fingerprintDegrees[0] - anonymizedGraph.degreeOf(v));   // If called, the function edgeEditDistanceWeaklyInduced would return this value. We don't to avoid creating the singleton lists
			if (dist < minLocDistValue) {
				minLocDistValue = dist;
				vertsMinDistValue.clear();
				vertsMinDistValue.add(v);
			}
			else if (dist == minLocDistValue)
				vertsMinDistValue.add(v);
		}
		
		List<String[]> finalCandidates = new ArrayList<>();
		
		if (minLocDistValue <= maxEditDistance) 
			if (fingerprintDegrees.length == 1) 
				for (String v : vertsMinDistValue)
					finalCandidates.add(new String[]{v});
			else {   // fingerprintDegrees.length > 1
				
				// Explore recursively
				int minGlbDistValue = 1 + (anonymizedGraph.vertexSet().size() * (anonymizedGraph.vertexSet().size() - 1)) / 2;   // One more than the maximal possible distance (the total amount of edges). This is "positive infinity" in this context.
				List<List<String>> candidatesMinGlb = new ArrayList<>();
				for (String v : vertsMinDistValue) {
					List<String> currentPartialCandidate = new ArrayList<>();
					currentPartialCandidate.add(v);
					List<List<String>> returnedPartialCandidates = new ArrayList<>();
					int glbDist = getPotentialAttackerCandidatesBFS(fingerprintDegrees, fingerprintLinks, anonymizedGraph, currentPartialCandidate, returnedPartialCandidates);
					if (glbDist < minGlbDistValue) {
						minGlbDistValue = glbDist;
						candidatesMinGlb.clear();
						candidatesMinGlb.addAll(returnedPartialCandidates);
					}
					else if (glbDist == minGlbDistValue)
						candidatesMinGlb.addAll(returnedPartialCandidates);
				}
				
				for (List<String> cand : candidatesMinGlb)
					finalCandidates.add(cand.toArray(new String[cand.size()]));
			}
		
		return finalCandidates;
	}
	
	protected int getPotentialAttackerCandidatesBFS(int[] fingerprintDegrees, boolean[][] fingerprintLinks, UndirectedGraph<String, DefaultEdge> anonymizedGraph, List<String> currentPartialCandidate, List<List<String>> partialCandidates2Return) {
		
		int minLocDistValue = 1 + (anonymizedGraph.vertexSet().size() * (anonymizedGraph.vertexSet().size() - 1)) / 2;   // One more than the maximal possible distance (the total amount of edges). This is "positive infinity" in this context.
		Set<String> vertsMinDistValue = new HashSet<>();
		
		List<String> currentSybPrefix = new ArrayList<>();
		for (int i = 0; i < currentPartialCandidate.size() + 1; i++)
			currentSybPrefix.add("" + i);
		
		for (String v : anonymizedGraph.vertexSet()) 
			if (!currentPartialCandidate.contains(v)) {
			
				List<String> newCand = new ArrayList<>(currentPartialCandidate);
				newCand.add(v);
				
				int dist = edgeEditDistanceWeaklyInduced(anonymizedGraph, currentSybPrefix, newCand);
				
				if (dist < minLocDistValue) {
					minLocDistValue = dist;
					vertsMinDistValue.clear();
					vertsMinDistValue.add(v);
				}
				else if (dist == minLocDistValue)
					vertsMinDistValue.add(v);
			}
		
		if (minLocDistValue <= maxEditDistance)
			if (fingerprintDegrees.length <= currentPartialCandidate.size() + 1) {
				for (String v : vertsMinDistValue) {
					List<String> newCand = new ArrayList<>(currentPartialCandidate);
					newCand.add(v);
					partialCandidates2Return.add(newCand);
				}
				return minLocDistValue;
			}
			else {
			
				// Explore recursively
				int minGlbDistValue = 1 + (anonymizedGraph.vertexSet().size() * (anonymizedGraph.vertexSet().size() - 1)) / 2;   // One more than the maximal possible distance (the total amount of edges). This is "positive infinity" in this context.
				List<List<String>> candidatesMinGlb = new ArrayList<>();
				for (String v : vertsMinDistValue) {
					List<String> newCurrentPartialCandidate = new ArrayList<>(currentPartialCandidate);
					newCurrentPartialCandidate.add(v);
					List<List<String>> returnedPartialCandidates = new ArrayList<>();
					int glbDist = getPotentialAttackerCandidatesBFS(fingerprintDegrees, fingerprintLinks, anonymizedGraph, newCurrentPartialCandidate, returnedPartialCandidates);
					if (glbDist < minGlbDistValue) {
						minGlbDistValue = glbDist;
						candidatesMinGlb.clear();
						candidatesMinGlb.addAll(returnedPartialCandidates);
					}
					else if (glbDist == minGlbDistValue)
						candidatesMinGlb.addAll(returnedPartialCandidates);
				}
				
				partialCandidates2Return.addAll(candidatesMinGlb);
				return minGlbDistValue;
			}
		else   // minLocDistValue > maxEditDistance
			return 1 + (anonymizedGraph.vertexSet().size() * (anonymizedGraph.vertexSet().size() - 1)) / 2;   // One more than the maximal possible distance (the total amount of edges). This is "positive infinity" in this context.
	}
	
	protected FingerprintSetMatchingReturnValue approxFingerprintMatching(UndirectedGraph<String, DefaultEdge> anonymizedGraph, String[] candidateSybilVerts, HashMap<String, String> fingerprintsPossibleVictims, ArrayList<String> originalFingerprints, Set<Integer> matchedOrigFingerprints) {
				
		FingerprintSimilarity fsim = new FSimCoincidenceCount();
				
		// Find all choices for the first match. Each choice is a pair (yi,v) where yi is a real victim.
		int maxSim = -1;
		Map<Integer, Set<String>> bestLocalMatches = new HashMap<>(); 
		
		for (int ordOrigFP = 0; ordOrigFP < originalFingerprints.size(); ordOrigFP++)
			if (!matchedOrigFingerprints.contains(ordOrigFP)) {
				for (String pv : fingerprintsPossibleVictims.keySet()) {
					int sim = fsim.similarity(originalFingerprints.get(ordOrigFP), fingerprintsPossibleVictims.get(pv));	
					if (sim > maxSim) {
						maxSim = sim;
						bestLocalMatches.clear();
						Set<String> pvictims = new HashSet<>();
						pvictims.add(pv);
						bestLocalMatches.put(ordOrigFP, pvictims);
					}
					else if (sim == maxSim) {
						if (bestLocalMatches.containsKey(ordOrigFP))
							bestLocalMatches.get(ordOrigFP).add(pv);
						else {
							Set<String> pvictims = new HashSet<>();
							pvictims.add(pv);
							bestLocalMatches.put(ordOrigFP, pvictims);
						}
					}
				}
			}
		
		// Get matches  
		
		Set<Map<Integer, String>> allMatches = new HashSet<>();
		
		if (originalFingerprints.size() == 1) {   // No more matches to do after this one
			for (Integer ordOrigFP : bestLocalMatches.keySet()) { 
				for (String v : bestLocalMatches.get(ordOrigFP)) {
					Map<Integer, String> entry = new HashMap<>();
					entry.put(ordOrigFP, v);
					allMatches.add(entry);
				}				
			}
			return new FingerprintSetMatchingReturnValue(allMatches, maxSim);
		}
		else {   // originalFingerprints.size() > 1. Go recursively
			
			int maxSimRestOfMatching = -1;
			
			for (Integer ordOrigFP : bestLocalMatches.keySet())
				for (String pv : bestLocalMatches.get(ordOrigFP)) {
					Set<Integer> newMatchedOrigFingerprints = new HashSet<>();
					newMatchedOrigFingerprints.add(ordOrigFP);
					HashMap<String, String> fingerprintsRemainingPossibleVictims = new HashMap<>(fingerprintsPossibleVictims);
					fingerprintsRemainingPossibleVictims.remove(pv);
					
					FingerprintSetMatchingReturnValue resultRemainingMatching = approxFingerprintMatching(fingerprintsRemainingPossibleVictims, originalFingerprints, newMatchedOrigFingerprints, candidateSybilVerts.length);
					
					if (resultRemainingMatching.maxSimilarity > maxSimRestOfMatching) {
						maxSimRestOfMatching = resultRemainingMatching.maxSimilarity;
						allMatches.clear();
						for (Map<Integer, String> mms : resultRemainingMatching.matches) {
							mms.put(ordOrigFP, pv);
							allMatches.add(mms);
						}
					}
					else if (resultRemainingMatching.maxSimilarity == maxSimRestOfMatching) 
						for (Map<Integer, String> mms : resultRemainingMatching.matches) {
							mms.put(ordOrigFP, pv);
							allMatches.add(mms);
						}
				}
			
			if (maxSimRestOfMatching <= 0)
				return new FingerprintSetMatchingReturnValue(allMatches, -1);
			else 
				return new FingerprintSetMatchingReturnValue(allMatches, maxSimRestOfMatching);
			}
	}
	
	// This function was initially meant to be the recursive step of the function above. Now it will be used as the main one
	protected FingerprintSetMatchingReturnValue approxFingerprintMatching(Map<String, String> fingerprintsPossibleVictims, List<String> originalFingerprints, Set<Integer> matchedOrigFingerprints, int attackerCount) {
		
		FingerprintSimilarity fsim = new FSimCoincidenceCount();
		
		// Find all choices for the next match. Each choice is a pair (yi,v) where yi is a real victim.
		int maxSim = -1;
		Map<Integer, Set<String>> bestLocalMatches = new HashMap<>(); 
		
		for (int ordOrigFP = 0; ordOrigFP < originalFingerprints.size(); ordOrigFP++)
			if (!matchedOrigFingerprints.contains(ordOrigFP)) {
				for (String pv : fingerprintsPossibleVictims.keySet()) {
					int sim = fsim.similarity(originalFingerprints.get(ordOrigFP), fingerprintsPossibleVictims.get(pv));	
					if (sim > maxSim) {
						maxSim = sim;
						bestLocalMatches.clear();
						Set<String> pvictims = new HashSet<>();
						pvictims.add(pv);
						bestLocalMatches.put(ordOrigFP, pvictims);
					}
					else if (sim == maxSim) {
						if (bestLocalMatches.containsKey(ordOrigFP))
							bestLocalMatches.get(ordOrigFP).add(pv);
						else {
							Set<String> pvictims = new HashSet<>();
							pvictims.add(pv);
							bestLocalMatches.put(ordOrigFP, pvictims);
						}
					}
			}
		}
		
		// Get matches  
		
		Set<Map<Integer, String>> allMatches = new HashSet<>();
		
		if (maxSim == -1)   // This happens if there were too few available fingerprints 
			return new FingerprintSetMatchingReturnValue(allMatches, -1);
		
		if (matchedOrigFingerprints.size() + 1 < originalFingerprints.size()) {   // Recursion can continue at least one more level from here 
			
			int maxSimRestOfMatching = -1;
			
			for (Integer ordOrigFP : bestLocalMatches.keySet())
				for (String pv : bestLocalMatches.get(ordOrigFP)) {
					Set<Integer> newMatchedOrigFingerprints = new HashSet<>();
					newMatchedOrigFingerprints.add(ordOrigFP);
					HashMap<String, String> fingerprintsRemainingPossibleVictims = new HashMap<>(fingerprintsPossibleVictims);
					fingerprintsRemainingPossibleVictims.remove(pv);
					
					FingerprintSetMatchingReturnValue resultRemainingMatching = approxFingerprintMatching(fingerprintsRemainingPossibleVictims, originalFingerprints, newMatchedOrigFingerprints, attackerCount);
					
					if (resultRemainingMatching.maxSimilarity != -1) {
						if (resultRemainingMatching.maxSimilarity > maxSimRestOfMatching) {
							maxSimRestOfMatching = resultRemainingMatching.maxSimilarity;
							allMatches.clear();
							for (Map<Integer, String> mms : resultRemainingMatching.matches) {
								mms.put(ordOrigFP, pv);
								allMatches.add(mms);
							}
						}
						else if (resultRemainingMatching.maxSimilarity == maxSimRestOfMatching) 
							for (Map<Integer, String> mms : resultRemainingMatching.matches) {
								mms.put(ordOrigFP, pv);
								allMatches.add(mms);
							}
					}
					
				}
			
			if (maxSimRestOfMatching <= 0)
				return new FingerprintSetMatchingReturnValue(allMatches, -1);
			else 
				return new FingerprintSetMatchingReturnValue(allMatches, maxSimRestOfMatching);
			
		}
		else {   // Recursion stops when matchedOrigFingerprints.size() == originalFingerprints.size() - 1, that is, we are doing the last matching  
			boolean realVictimMatched = false;
			for (Integer ordOrigFP : bestLocalMatches.keySet()) { 
				for (String v : bestLocalMatches.get(ordOrigFP)) {
					Map<Integer, String> entry = new HashMap<>();
					entry.put(ordOrigFP, v);
					allMatches.add(entry);
					if (v.equals("" + attackerCount + ordOrigFP))
						realVictimMatched = true;
				}
			}
			if (realVictimMatched)
				return new FingerprintSetMatchingReturnValue(allMatches, maxSim);
			else
				return new FingerprintSetMatchingReturnValue(allMatches, -1);
		}		
	}
	
	protected int edgeEditDistanceInduced(UndirectedGraph<String, DefaultEdge> graph, List<String> vertSet1, List<String> vertSet2) {
		if (vertSet1.size() == vertSet2.size()) {
			int diffCount = 0;
			for (int i = 0; i < vertSet1.size() - 1; i++)
				for (int j = i + 1; j < vertSet1.size(); j++) 
					if ((graph.containsEdge(vertSet1.get(i), vertSet1.get(j)) && !graph.containsEdge(vertSet2.get(i), vertSet2.get(j))) 
						|| (!graph.containsEdge(vertSet1.get(i), vertSet1.get(j)) && graph.containsEdge(vertSet2.get(i), vertSet2.get(j))))
						diffCount++;
			return diffCount;
		}
		return 1 + (graph.vertexSet().size() * (graph.vertexSet().size() - 1)) / 2;   // One more than the maximal possible distance (the total amount of edges). This is "positive infinity" in this context. 
	}
	
	protected int edgeEditDistanceWeaklyInduced(UndirectedGraph<String, DefaultEdge> graph, List<String> vertSet1, List<String> vertSet2) {
		if (vertSet1.size() == vertSet2.size()) {
			int diffCount = 0;
			List<Integer> externalDegrees1 = new ArrayList<>();
			List<Integer> externalDegrees2 = new ArrayList<>();
			for (int i = 0; i < vertSet1.size(); i++) {
				externalDegrees1.add(graph.degreeOf(vertSet1.get(i)));
				externalDegrees2.add(graph.degreeOf(vertSet2.get(i)));
			}
			for (int i = 0; i < vertSet1.size() - 1; i++) 
				for (int j = i + 1; j < vertSet1.size(); j++) 
					if (graph.containsEdge(vertSet1.get(i), vertSet1.get(j))) {
						externalDegrees1.set(i, externalDegrees1.get(i) - 1);
						if (graph.containsEdge(vertSet2.get(i), vertSet2.get(j))) 
							externalDegrees2.set(i, externalDegrees2.get(i) - 1);
						else
							diffCount++;
					}
					else   // !graph.containsEdge(vertSet1.get(i), vertSet1.get(j))
						if (graph.containsEdge(vertSet2.get(i), vertSet2.get(j))) {
							diffCount++;
							externalDegrees2.set(i, externalDegrees2.get(i) - 1);
						}
						// else !graph.containsEdge(vertSet2.get(i), vertSet2.get(j))
			
			int dist = diffCount;
			for (int i = 0; i < vertSet1.size(); i++)
				dist += Math.abs(externalDegrees1.get(i) - externalDegrees2.get(i));
						
			return dist;
		}
		return 1 + (graph.vertexSet().size() * (graph.vertexSet().size() - 1)) / 2;   // One more than the maximal possible distance (the total amount of edges). This is "positive infinity" in this context. 
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// Deprecated or unused methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected List<String[]> getPotentialAttackerCandidatesDP(int[] fingerprintDegrees, boolean[][] fingerprintLinks, UndirectedGraph<String, DefaultEdge> anonymizedGraph) {
		
		// Unlike a standard attack, where every time a minimum value is not unique we arbitrarily take the first choice 
		// for which this minimum occurs, here we store all of them and return them as candidates
		
		// Create table
		List<Map<String, Integer>> tableDistances = new ArrayList<>();
		List<Map<String, Set<List<String>>>> tablePrecedingVertices = new ArrayList<>();
		
		// Fill first row
		Map<String, Integer> rowDistTab = new HashMap<>();
		Map<String, Set<List<String>>> rowPrecVertTab = new HashMap<>();
		List<String> emptySetPrecVerts = new ArrayList<>();
		Set<List<String>> setEmptySet = new HashSet<>();
		setEmptySet.add(emptySetPrecVerts);
		for (String v : anonymizedGraph.vertexSet()) {
			rowDistTab.put(v, Math.abs(fingerprintDegrees[0] - anonymizedGraph.degreeOf(v)));
			rowPrecVertTab.put(v, setEmptySet);
		}
		tableDistances.add(rowDistTab);
		tablePrecedingVertices.add(rowPrecVertTab);
		
		// Fill remaining rows
		for (int i = 1; i < fingerprintDegrees.length; i++) {
			rowDistTab = new HashMap<>();
			rowPrecVertTab = new HashMap<>();
			for (String v : anonymizedGraph.vertexSet()) {
				int degreeDiff = Math.abs(fingerprintDegrees[i] - anonymizedGraph.degreeOf(v));
				int minDistValue = Integer.MAX_VALUE;
				Set<String> vertsMinDist = new HashSet<>();
				for (String v1 : anonymizedGraph.vertexSet()) 
					for (List<String> precSet : tablePrecedingVertices.get(i - 1).get(v1)) 
						if (!precSet.contains(v)) {
							int prevDistValue = tableDistances.get(i - 1).get(v1);
							if (prevDistValue < minDistValue) {
								minDistValue = prevDistValue;
								vertsMinDist.clear();
								vertsMinDist.add(v1);
							}
							else if (prevDistValue == minDistValue) 
								vertsMinDist.add(v1);
						}
				System.out.println("Min dist val row " + (i - 1) + ": " + minDistValue);
				rowDistTab.put(v, degreeDiff + minDistValue);
				Set<List<String>> setsPrecVerts = new HashSet<>();
				for (String vmin : vertsMinDist) {
					for (List<String> cand : tablePrecedingVertices.get(i - 1).get(vmin)) {
						ArrayList<String> preffix = new ArrayList<>(cand);
						preffix.add(vmin);
						setsPrecVerts.add(preffix);
					}
				}
				rowPrecVertTab.put(v, setsPrecVerts);
			}
			tableDistances.add(rowDistTab);
			tablePrecedingVertices.add(rowPrecVertTab);
		}
		
		// Get best sets from last row
		int glbMinDistValue = Integer.MAX_VALUE;
		Set<String> vertsGlbMinDist = new HashSet<>();
		for (String v : anonymizedGraph.vertexSet()) 
			if (tableDistances.get(fingerprintDegrees.length - 1).containsKey(v)) {
				int tblVal = tableDistances.get(fingerprintDegrees.length - 1).get(v); 
				if (tblVal < glbMinDistValue) {
					glbMinDistValue = tblVal;
					vertsGlbMinDist.clear();
					vertsGlbMinDist.add(v);
				}
				else if (tblVal == glbMinDistValue)
					vertsGlbMinDist.add(v);
			}
		
		List<String[]> candidates = new ArrayList<>();
		for (String vgmin : vertsGlbMinDist) 
			for (List<String> cand : tablePrecedingVertices.get(fingerprintDegrees.length).get(vgmin)) {
				ArrayList<String> copyCand = new ArrayList<>(cand);
				copyCand.add(vgmin);
				candidates.add(copyCand.toArray(new String[copyCand.size()]));
			}
		
		return candidates;
	}
	
	protected Set<ArrayList<String>> approxFingerprintMatchings(String[] candidateSybilVerts, UndirectedGraph<String, DefaultEdge> anonymizedGraph, ArrayList<String> originalFingerprints, Set<Integer> matchedOrigFingerprints) {
		
		FingerprintSimilarity fsim = new FSimCoincidenceCount();
		
		HashSet<String> possibleVictims = new HashSet<>();
		for (int i = 0; i < candidateSybilVerts.length; i++)
			possibleVictims.addAll(Graphs.neighborListOf(anonymizedGraph, candidateSybilVerts[i]));
		for (int i = 0; i < candidateSybilVerts.length; i++)
			possibleVictims.remove(candidateSybilVerts[i]);
		
		HashMap<String, String> fingerprintsPossibleVictims = new HashMap<>();
		
		// Find best match (and simultaneously store fingerprints of possible victims for next computations)
		int maxSim = -1;
		HashMap<String, Set<Integer>> ordsMaxSim = new HashMap<>();
		for (String v : possibleVictims) {
			String pvFingerprint = "";
			for (int i = 0; i < candidateSybilVerts.length; i++)
				if (anonymizedGraph.containsEdge(v, candidateSybilVerts[i]))
					pvFingerprint += "1";
				else
					pvFingerprint += "0";
			pvFingerprint = codec.correctedCodeWord(pvFingerprint);
			fingerprintsPossibleVictims.put(v, pvFingerprint);
			for (int j = 0; j < originalFingerprints.size(); j++)
				if (!matchedOrigFingerprints.contains(j)) {
					int sim = fsim.similarity(originalFingerprints.get(j), pvFingerprint);
					if (sim > maxSim) {
						maxSim = sim;
						ordsMaxSim.clear();
						Set<Integer> vals = new HashSet<>();
						vals.add(j);
						ordsMaxSim.put(v, vals);
					}
					else if (sim == maxSim)
						if (ordsMaxSim.containsKey(v))
							ordsMaxSim.get(v).add(j);
						else {
							Set<Integer> vals = new HashSet<>();
							vals.add(j);
							ordsMaxSim.put(v, vals);
						}
				}
		}
		
		Set<ArrayList<String>> allMatches = new HashSet<>();
		
		if (originalFingerprints.size() == 1) 
			for (String v : ordsMaxSim.keySet()) {
				ArrayList<String> vset = new ArrayList<>();
				vset.add(v);
				allMatches.add(vset);
			}
		else {   // originalFingerprints.size() > 1
			for (String v : ordsMaxSim.keySet())
				for (int j : ordsMaxSim.get(v)) {
					Set<Integer> newMatchedOrigFingerprints = new HashSet<>(matchedOrigFingerprints);
					newMatchedOrigFingerprints.add(j);
					HashMap<String, String> fingerprintsRemainingPossibleVictims = new HashMap<>(fingerprintsPossibleVictims);
					fingerprintsRemainingPossibleVictims.remove(v);
					Set<ArrayList<String>> allRemainingMatches = approxFingerprintMatchingsRemainder(fingerprintsRemainingPossibleVictims, originalFingerprints, newMatchedOrigFingerprints);
					for (ArrayList<String> rm : allRemainingMatches) {
						rm.add(0, v);
						allMatches.add(rm);
					}
				}
		}
		
		return allMatches;
	}

	protected Set<ArrayList<String>> approxFingerprintMatchingsRemainder(Map<String, String> fingerprintsPossibleVictims, List<String> originalFingerprints, Set<Integer> matchedOrigFingerprints) {

		FingerprintSimilarity fsim = new FSimCoincidenceCount();
		
		// Find next best match
		double maxSim = -1d;
		HashMap<String, Set<Integer>> ordsMaxSim = new HashMap<>();
		for (String v : fingerprintsPossibleVictims.keySet()) {
			for (int j = 0; j < originalFingerprints.size(); j++)
				if (!matchedOrigFingerprints.contains(j)) {
					double sim = fsim.similarity(originalFingerprints.get(j), fingerprintsPossibleVictims.get(v));
					if (sim > maxSim) {
						maxSim = sim;
						ordsMaxSim.clear();
						Set<Integer> vals = new HashSet<>();
						vals.add(j);
						ordsMaxSim.put(v, vals);
					}
					else if (sim == maxSim)
						if (ordsMaxSim.containsKey(v))
							ordsMaxSim.get(v).add(j);
						else {
							Set<Integer> vals = new HashSet<>();
							vals.add(j);
							ordsMaxSim.put(v, vals);
						}
				}
		}
		
		Set<ArrayList<String>> allMatches = new HashSet<>();
		
		if (matchedOrigFingerprints.size() + 1 < originalFingerprints.size()) {
			for (String v : ordsMaxSim.keySet())
				for (int j : ordsMaxSim.get(v)) {
					Set<Integer> newMatchedOrigFingerprints = new HashSet<>(matchedOrigFingerprints);
					newMatchedOrigFingerprints.add(j);
					HashMap<String, String> fingerprintsRemainingPossibleVictims = new HashMap<>(fingerprintsPossibleVictims);
					fingerprintsRemainingPossibleVictims.remove(v);
					Set<ArrayList<String>> allRemainingMatches = approxFingerprintMatchingsRemainder(fingerprintsRemainingPossibleVictims, originalFingerprints, newMatchedOrigFingerprints);
					for (ArrayList<String> rm : allRemainingMatches) {
						rm.add(0, v);
						allMatches.add(rm);
					}
				}
		}
		else   // Recursion stops when matchedOrigFingerprints.size() + 1 == originalFingerprints.size()
			for (String v : ordsMaxSim.keySet()) {
				ArrayList<String> vset = new ArrayList<>();
				vset.add(v);
				allMatches.add(vset);
			}
		
		return allMatches;
	}
	
}
