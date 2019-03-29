package anonymization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import util.CombinationIterator;
import util.GraphUtil;

public abstract class AdjacencyAnonymizer {
	
	/***
	 * 
	 * Auxiliary subclasses
	 *
	 */
	
	static class VertDegreeInfo implements Comparable<VertDegreeInfo> {
		
		public String label;
		public Integer degree;
		
		public VertDegreeInfo(String lbl, int deg) {
			label = lbl;
			degree = new Integer(deg);
		}
		
		@Override
		public int compareTo(VertDegreeInfo vert) {
			if (this.degree.compareTo(vert.degree) == 0)
				return this.label.compareTo(vert.label);
			return this.degree.compareTo(vert.degree);
		}
		
		public boolean equals(VertDegreeInfo vert) {
			return (this.label.equals(vert.label) && this.degree.equals(vert.degree));
		}
		
		@Override
		public String toString() {
			return label + ":" + degree; 
		}
	}
	
	//==================================================================================================================
	
	static class VertexPair implements Comparable<VertexPair> {
		
		public String source, dest;
		
		public VertexPair(String src, String dst) {
			if (src.compareTo(dst) <= 0) {
				source = src;
				dest = dst;
			}
			else {
				source = dst;
				dest = src;
			}
		}

		@Override
		public int compareTo(VertexPair e) {
			if (this.source.compareTo(e.source) == 0)
				return this.dest.compareTo(e.dest);
			else
				return this.source.compareTo(e.source);
		}
		
		public boolean incident(String v) {
			return ((v.equals(source)) || (v.equals(dest)));
		}
		
		@Override
		public String toString() {
			return "(" + source + "," + dest + ")";
		}
		
	}
	
	//==================================================================================================================
	
	static class ScoredVertexPair extends VertexPair {
		
		public int score;

		public ScoredVertexPair(String src, String dst) {
			super(src, dst);
			score = 0;
		}
		
		public ScoredVertexPair(String src, String dst, int scr) {
			super(src, dst);
			score = scr;
		}
		
		@Override
		public int compareTo(VertexPair e) {
			if (this.source.compareTo(e.source) == 0)
				return this.dest.compareTo(e.dest);
			else
				return this.source.compareTo(e.source);
		}
		
		public int compareTo(ScoredVertexPair e) {
			if (this.score < e.score)
				return -1;
			else if (this.score > e.score)
				return 1;
			else   // this.score == e.score
				if (this.source.compareTo(e.source) == 0)
					return this.dest.compareTo(e.dest);
				else
					return this.source.compareTo(e.source);
		}
	}
	
	//==================================================================================================================
	
	/***
	 * 
	 * Anonymization, anonymous transformation and perturbation methods
	 * 
	 */
	
	//==================================================================================================================
	
	public static void enforceK1AdjAnonymity(UndirectedGraph<String, DefaultEdge> graph, int k) {
		
		/***
		 * Old implementation. Assumes that high-degree vertices need no action
		 */
		
		//System.out.println("Processing new graph");
		//System.out.println("Order: " + graph.vertexSet().size());
		//System.out.println("Original size: " + graph.edgeSet().size());
		//int minDegree = graph.vertexSet().size(), maxDegree = -1;
		//for (String v : graph.vertexSet()) {
		//	if (graph.degreeOf(v) < minDegree)
		//		minDegree = graph.degreeOf(v);
		//	if (graph.degreeOf(v) > maxDegree)
		//		maxDegree = graph.degreeOf(v);
		//}
		//System.out.println("Min degree: " + minDegree);
		//System.out.println("Max degree: " + maxDegree);
		//System.out.println("k: " + k);
		
		int n = graph.vertexSet().size();
			
		// Initialization
		
		Set<String> isolatedVertices = new HashSet<>();
		Set<VertDegreeInfo> lowDegreeVertices = new TreeSet<>(Collections.reverseOrder());
		Set<VertDegreeInfo> highEnoughDegreeVertices = new TreeSet<>();
		
		for (String v : graph.vertexSet()) {
			int deg = graph.degreeOf(v);
			if (deg == 0)
				isolatedVertices.add(v);
			else if (deg >= 1 && deg < k)
				lowDegreeVertices.add(new VertDegreeInfo(v, deg));
			else
				highEnoughDegreeVertices.add(new VertDegreeInfo(v, deg));
		}
		
		//System.out.println("Isolated vertices: " + isolatedVertices);
		//System.out.println("Low-degree vertices: " + lowDegreeVertices);
		//System.out.println("High-enough-degree vertices: " + highEnoughDegreeVertices);
		
		// Handle low-degree vertices
		
		while (lowDegreeVertices.size() != 0) {
			
			// Look for candidate edge addition
			
			String vert1 = null, vert2 = null;
			
			for (VertDegreeInfo v1 : lowDegreeVertices) {   // Traversed in decreasing order by degree
				
				vert1 = v1.label;
				
				// First look for another low-degree vertex
				for (VertDegreeInfo v2 : lowDegreeVertices)   // Traversed in decreasing order by degree
					if (!v2.equals(v1) && !graph.containsEdge(vert1, v2.label)) {
						vert2 = v2.label;
						break;
					}
				
				// Next, if necessary, look for another non-isolated vertex
				if (vert2 == null)   // Traversed in increasing order by degree
					for (VertDegreeInfo v2 : highEnoughDegreeVertices)
						if (!graph.containsEdge(vert1, v2.label)) {
							vert2 = v2.label;
							break;
						}
				
				// Finally, if necessary, take some isolated vertex
				if (vert2 == null)
					for (String v2 : isolatedVertices) {
						vert2 = v2;
						break;   // The purpose of the for-cycle is to take the first element
					}
				
				break;   // The purpose of the for-cycle is to take the first element
			}
			
			Set<String> secondVerts = new HashSet<>();
			if (vert2 != null)   // One appropriate candidate found
				secondVerts.add(vert2);
			else   // No appropriate candidate found, vert1 must be turned into a dominant vertex
				for (String v2 : graph.vertexSet())
					if (!v2.equals(vert1) && !graph.containsEdge(vert1, v2))
						secondVerts.add(v2);
			
			// Add edge(s)
			for (String v2 : secondVerts)
				graph.addEdge(vert1, v2);
			
			// Update degree infos
			
			// For vert1
			int newDegVert1 = graph.degreeOf(vert1);
			lowDegreeVertices.remove(new VertDegreeInfo(vert1, newDegVert1 - secondVerts.size()));   // vert1 was known to be a low-degree vertex before the edge addition(s)
			if (newDegVert1 < k)   // vert1 remains a low-degree vertex
				lowDegreeVertices.add(new VertDegreeInfo(vert1, newDegVert1));
			else   // vert1 has become a high-enough-degree vertex
				highEnoughDegreeVertices.add(new VertDegreeInfo(vert1,	newDegVert1));
			
			// For all secondVerts
			for (String v2 : secondVerts) {
				int newDegVert2 = graph.degreeOf(v2);
				if (newDegVert2 == 1) {   // v2 was an isolated vertex before the edge addition
					isolatedVertices.remove(v2);
					lowDegreeVertices.add(new VertDegreeInfo(v2, 1));
				}
				else if (newDegVert2 >= 2 && newDegVert2 < k) {   // v2 was a low-degree vertex before the edge addition and remains as such
					lowDegreeVertices.remove(new VertDegreeInfo(v2, newDegVert2 - 1));
					lowDegreeVertices.add(new VertDegreeInfo(v2, newDegVert2));
				}
				else if (newDegVert2 == k) {   // v2 was a low-degree vertex before the edge addition and has become a mid-degree vertex afterwards
					lowDegreeVertices.remove(new VertDegreeInfo(v2, k - 1));
					highEnoughDegreeVertices.add(new VertDegreeInfo(v2, k));
				}
				else if (newDegVert2 > k && newDegVert2 <= n - k - 1) {   // v2 was a high-enough-degree vertex before the edge addition
					highEnoughDegreeVertices.remove(new VertDegreeInfo(v2, newDegVert2 - 1));
					highEnoughDegreeVertices.add(new VertDegreeInfo(v2, newDegVert2));
				}					
			}
		}
		
		//System.out.println("Final size: " + graph.edgeSet().size());
		//System.out.println("=====================================================");
	}
	
	//==================================================================================================================
	
	public static boolean k1AdjAnonymousTransformation(UndirectedGraph<String, DefaultEdge> graph, int k) {
		
		boolean verbose = false, dominantForcedSomeTime = false; 
		
		int n = graph.vertexSet().size();
		
		if (k <= (n - 1) / 2) {
		
			if (verbose) {
				System.out.println("Processing new graph");
				System.out.println("Order: " + n);
				System.out.println("Original size: " + graph.edgeSet().size());
				int minDegree = graph.vertexSet().size(), maxDegree = -1;
				for (String v : graph.vertexSet()) {
					if (graph.degreeOf(v) < minDegree)
						minDegree = graph.degreeOf(v);
					if (graph.degreeOf(v) > maxDegree)
						maxDegree = graph.degreeOf(v);
				}
				System.out.println("Min degree: " + minDegree);
				System.out.println("Max degree: " + maxDegree);
				System.out.println("k: " + k);
			}
				
			// Initialization
						
			Set<String> originalLowDeg = new HashSet<>();
			Set<VertDegreeInfo> remainingLowDeg = new TreeSet<>(Collections.reverseOrder());
			Set<VertDegreeInfo> outOfRemainingLowDeg = new TreeSet<>();
			Set<String> originalHighDeg = new HashSet<>();
			
			for (String v : graph.vertexSet()) {
				int deg = graph.degreeOf(v);
				if (deg == 0) 
					outOfRemainingLowDeg.add(new VertDegreeInfo(v, 0));
				else if (deg >= 1 && deg < k) {
					originalLowDeg.add(v);
					remainingLowDeg.add(new VertDegreeInfo(v, deg));
				}
				else if (deg >= k && deg <= n - k - 1) 
					outOfRemainingLowDeg.add(new VertDegreeInfo(v, deg));
				else if (deg >= n - k - 1 && deg <= n - 2) {
					originalHighDeg.add(v);
					outOfRemainingLowDeg.add(new VertDegreeInfo(v, deg));
				}
				else;   // Dominant vertices are useless for the first stage, as no more edges can be linked to them 
			}
			
			if (verbose) {
				System.out.println("Low-degree vertices: " + originalLowDeg);
				System.out.println("High-degree vertices: " + originalHighDeg);
			}
			
			// Handle original low-degree vertices
			
			while (remainingLowDeg.size() != 0) {
				
				// Look for candidate edge addition
				String vert1 = null, vert2 = null;
				
				// First look for a pair of vertices in remainingLowDeg
				outer_loop: for (VertDegreeInfo v1 : remainingLowDeg) {   // By construction, it is traversed in decreasing order by current degree
					for (VertDegreeInfo v2 : remainingLowDeg)   // By construction, it is traversed in decreasing order by current degree
						if (!v2.equals(v1) && !graph.containsEdge(v1.label, v2.label)) {
							vert1 = v1.label;
							vert2 = v2.label;
							break outer_loop;
						}	
				}
				
				// Next, if necessary, look for a pair composed by a vertex in remainingLowDeg and a vertex in outOfRemainingLowDeg
				if (vert1 == null || vert2 == null) {
					outer_loop: for (VertDegreeInfo v1 : remainingLowDeg) {   // By construction, it is traversed in decreasing order by current degree   
						for (VertDegreeInfo v2 : outOfRemainingLowDeg)   // By construction, it is traversed in increasing order by degree
							if (!graph.containsEdge(v1.label, v2.label)) {
								vert1 = v1.label;
								vert2 = v2.label;
								break outer_loop;
							}
					}
				}
				
				if (vert1.equals(vert2)) {
					System.out.println("Happened");
					System.out.println("remainingLowDeg: " + remainingLowDeg);
					System.out.println("outOfRemainingLowDeg: " + outOfRemainingLowDeg);
				}
				// Add edge
				graph.addEdge(vert1, vert2);
				
				// Update degree infos
				
				// For vert1
				int newDegVert1 = graph.degreeOf(vert1);
				remainingLowDeg.remove(new VertDegreeInfo(vert1, newDegVert1 - 1));   // vert1 was known to be a low-degree vertex before the edge addition
				if (newDegVert1 < k)   // vert1 remains a low-degree vertex
					remainingLowDeg.add(new VertDegreeInfo(vert1, newDegVert1));
				else   // vert1 is no longer a low-degree vertex
					outOfRemainingLowDeg.add(new VertDegreeInfo(vert1,	newDegVert1));
				
				// For vert2
				int newDegVert2 = graph.degreeOf(vert2);
				if (newDegVert2 == 1) {   // v2 was an isolated vertex before the edge addition
					outOfRemainingLowDeg.remove(new VertDegreeInfo(vert2, 0));
					outOfRemainingLowDeg.add(new VertDegreeInfo(vert2, 1));
				}
				else if (newDegVert2 >= 2 && newDegVert2 < k) {   
					if (originalLowDeg.contains(vert2)) {   // v2 was in remainingLowDeg before the edge addition and should remain in there
						remainingLowDeg.remove(new VertDegreeInfo(vert2, newDegVert2 - 1));
						remainingLowDeg.add(new VertDegreeInfo(vert2, newDegVert2));
					}
					else {   // even though the degree is in [1,k-1], it was not an original low-degree vertex, so it is a formerly isolated vertex whose degree has been growing
						outOfRemainingLowDeg.remove(new VertDegreeInfo(vert2, newDegVert2 - 1));
						outOfRemainingLowDeg.add(new VertDegreeInfo(vert2, newDegVert2));
					}
				}
				else if (newDegVert2 == k) {   
					if (originalLowDeg.contains(vert2)) {   // v2 was in remainingLowDeg before the edge addition and now it should not
						remainingLowDeg.remove(new VertDegreeInfo(vert2, k - 1));
						outOfRemainingLowDeg.add(new VertDegreeInfo(vert2, k));
					}
					else {   // even though the degree had been so far in [1,k-1], it was not an original low-degree vertex, so it is a formerly isolated vertex whose degree has been growing
						outOfRemainingLowDeg.remove(new VertDegreeInfo(vert2, k - 1));
						outOfRemainingLowDeg.add(new VertDegreeInfo(vert2, k));
					}
				}
				else if (newDegVert2 > k) {   // v2 was in outOfRemainingLowDeg before the edge addition and should remain in there 
					outOfRemainingLowDeg.remove(new VertDegreeInfo(vert2, newDegVert2 - 1));
					if (newDegVert2 < n - 1)   // v2 did not become dominant
						outOfRemainingLowDeg.add(new VertDegreeInfo(vert2, newDegVert2));
				}
			}
			
			// Handle original high-degree vertices
			
			/** In some rare cases, the only choice for treating a high-degree vertex without removing edges 
			 * between it and an original low-degree vertex will be to make it dominant and re-start 
			 * the high-degree vertex treatment
			 */
			
			boolean dominantForced = false;
			
			do {
				
				dominantForced = false;
				
				// Re-initialize
				
				Set<VertDegreeInfo> remainingHighDeg = new TreeSet<>();
				Set<VertDegreeInfo> outOfRemainingHighNLowDeg = new TreeSet<>(Collections.reverseOrder());
				
				for (String v : graph.vertexSet()) {
					int deg = graph.degreeOf(v);
					if (originalHighDeg.contains(v)) {
						if (deg >= n - k - 1 && deg <= n - 2)
							remainingHighDeg.add(new VertDegreeInfo(v, deg));
						else;   // v became dominant, will not modify it again 
					}
					else if (!originalLowDeg.contains(v)) 
						outOfRemainingHighNLowDeg.add(new VertDegreeInfo(v, deg));
				}
				
				// Main part of high-degree vertex handling
				
				while (remainingHighDeg.size() != 0) {
					
					// Look for candidate edge removal
					String vert1 = null, vert2 = null;
					
					// First look for a pair of vertices in remainingHighDeg
					outer_loop: for (VertDegreeInfo v1 : remainingHighDeg) {   // By construction, it is traversed in increasing order by current degree
						for (VertDegreeInfo v2 : remainingHighDeg)   // By construction, it is traversed in increasing order by current degree
							if (!v2.equals(v1) && graph.containsEdge(v1.label, v2.label)) {
								vert1 = v1.label;
								vert2 = v2.label;
								break outer_loop;
							}	
					}
					
					// Next, if necessary, look for a pair composed by a vertex in remainingHighDeg and a vertex in outOfRemainingHighNLowDeg
					if (vert1 == null || vert2 == null) {
						outer_loop: for (VertDegreeInfo v1 : remainingHighDeg) {   // By construction, it is traversed in increasing order by current degree   
							for (VertDegreeInfo v2 : outOfRemainingHighNLowDeg)   // By construction, it is traversed in decreasing order by degree
								if (graph.containsEdge(v1.label, v2.label)) {
									vert1 = v1.label;
									vert2 = v2.label;
									break outer_loop;
								}
						}
					}
					
					if (vert1 != null && vert2 != null) {   // A removal is possible 
						
						// Remove edge
						graph.removeEdge(vert1, vert2);
						
						// Update degree infos
						
						// For vert1
						int newDegVert1 = graph.degreeOf(vert1);
						remainingHighDeg.remove(new VertDegreeInfo(vert1, newDegVert1 + 1));   // vert1 was known to be a high-degree vertex before the edge removal
						if (newDegVert1 > n - k - 1)   // vert1 remains a high-degree vertex
							remainingHighDeg.add(new VertDegreeInfo(vert1, newDegVert1));
						else   // vert1 is no longer a high-degree vertex
							outOfRemainingHighNLowDeg.add(new VertDegreeInfo(vert1,	newDegVert1));
						
						// For vert2
						int newDegVert2 = graph.degreeOf(vert2);
						if (newDegVert2 == n - 2) {   // v2 was a dominant vertex before the edge removal
							outOfRemainingHighNLowDeg.remove(new VertDegreeInfo(vert2, n - 1));
							outOfRemainingHighNLowDeg.add(new VertDegreeInfo(vert2, n - 2));
						}
						else if (newDegVert2 > n - k - 1 && newDegVert2 <= n - 3) {   
							if (originalHighDeg.contains(vert2)) {   // v2 was in remainingHighDeg before the edge addition and should remain in there
								remainingHighDeg.remove(new VertDegreeInfo(vert2, newDegVert2 + 1));
								remainingHighDeg.add(new VertDegreeInfo(vert2, newDegVert2));
							}
							else {   // even though the degree is in [n-k,n-2], it was not an original high-degree vertex, so it is a formerly dominant vertex whose degree has been lowering
								outOfRemainingHighNLowDeg.remove(new VertDegreeInfo(vert2, newDegVert2 + 1));
								outOfRemainingHighNLowDeg.add(new VertDegreeInfo(vert2, newDegVert2));
							}
						}
						else if (newDegVert2 == n - k - 1) {   
							if (originalHighDeg.contains(vert2)) {   // v2 was in remainingHighDeg before the edge addition and now it should not
								remainingHighDeg.remove(new VertDegreeInfo(vert2, n - k));
								outOfRemainingHighNLowDeg.add(new VertDegreeInfo(vert2, n - k - 1));
							}
							else {   // even though the degree had been so far in [n-k,n-2], it was not an original high-degree vertex, so it is a formerly dominant vertex whose degree has been lowering
								outOfRemainingHighNLowDeg.remove(new VertDegreeInfo(vert2, n - k));
								outOfRemainingHighNLowDeg.add(new VertDegreeInfo(vert2, n - k - 1));
							}
						}
						else if (newDegVert2 < n - k - 1) {   // v2 was in outOfRemainingHighNLowDeg before the edge addition and should remain in there 
							outOfRemainingHighNLowDeg.remove(new VertDegreeInfo(vert2, newDegVert2 + 1));
							if (newDegVert2 > 0)   // v2 did not become isolated
								outOfRemainingHighNLowDeg.add(new VertDegreeInfo(vert2, newDegVert2));
						}
					}
					else {   // No removal was possible, make largest degree non-dominant vertex dominant
						VertDegreeInfo hv = null;
						for (VertDegreeInfo vi : remainingHighDeg)   // At the end, hv will be the one with the largest degree
							hv = vi;
						for (String v : graph.vertexSet())
							if (!v.equals(hv.label) && !graph.containsEdge(v, hv.label))
								graph.addEdge(v, hv.label);
						dominantForced = true;
						dominantForcedSomeTime = true;
						break;
					}
					
				}
				
			} while (dominantForced);
			
			if (verbose) {
				System.out.println("Final size: " + graph.edgeSet().size());
				System.out.println("=====================================================");
			}
		}
		
		return dominantForcedSomeTime;
	}
	
	//==================================================================================================================
	
	public static void enforce2EllAdjAnonymity(UndirectedGraph<String, DefaultEdge> graph, int ell) {
		
		System.out.println("Processing new graph");
		System.out.println("Order: " + graph.vertexSet().size());
		System.out.println("Original size: " + graph.edgeSet().size());
		System.out.println("ell: " + ell);
		
		List<String> orderedVertexSet = new ArrayList<>(graph.vertexSet());
		boolean changesNeeded = true;
		int iterCount = 0;
		while (changesNeeded) {
			System.out.println("Iteration " + (iterCount + 1) + "...");
			changesNeeded = false;
			int resolvableCount = 0, antiresolvingCount = 0;
			//int optChanges = graph.vertexSet().size() + 1;
			int optChanges = -1;
			String uOpt = null, vOpt = null;
			Set<String> optDistinguishers = null;
			for (int i = 1; i <= ell; i++) {
				int thisSizeAntiresolvingCount = 0;
				CombinationIterator<String> combIterator = new CombinationIterator<>(orderedVertexSet, i);
				List<String> currentSet = combIterator.nextCombinationOrdered();
				while (currentSet != null) {
					// Obtain adjacency representations of every neighboring vertex
					TreeMap<String, Set<String>> vertsXRepr = new TreeMap<>();
					for (String v : neighborhood(graph, currentSet)) {
						String adjRepr = adjRepresentation(graph, v, currentSet);
						if (vertsXRepr.containsKey(adjRepr))
							vertsXRepr.get(adjRepr).add(v);
						else {
							Set<String> verts = new HashSet<>();
							verts.add(v);
							vertsXRepr.put(adjRepr, verts);
						}
					}
					// Get 1-adj-resolvable vertices
					Set<String> resolvables = new HashSet<>();
					for (String repr : vertsXRepr.keySet())
						if (vertsXRepr.get(repr).size() == 1) {
							resolvables.addAll(vertsXRepr.get(repr));
							resolvableCount++;
						}
					// Determine modification needing the smallest number of changes
					if (resolvables.size() > 1) {
						for (String v1 : resolvables)
							for (String v2 : resolvables)
								if (v1.compareTo(v2) < 0) {
									Set<String> distinguishers = distinguishingVertices(graph, currentSet, v1, v2);
									if (distinguishers.size() < optChanges) {
									//if (distinguishers.size() > optChanges) {
										optChanges = distinguishers.size();
										uOpt = v1;
										vOpt = v2;
										optDistinguishers = distinguishers;
										changesNeeded = true;
									}
								}
						antiresolvingCount++;
						thisSizeAntiresolvingCount++;
					}
					else if (resolvables.size() == 1) {
						String v1 = (String)resolvables.toArray()[0];
						for (String v2 : graph.vertexSet())
							if (!v1.equals(v2) && !currentSet.contains(v2)) {
								Set<String> distinguishers = distinguishingVertices(graph, currentSet, v1, v2);
								if (distinguishers.size() < optChanges) {
								//if (distinguishers.size() > optChanges) {
									optChanges = distinguishers.size();
									uOpt = v1;
									vOpt = v2;
									optDistinguishers = distinguishers;
									changesNeeded = true;
								}
							}
						antiresolvingCount++;
						thisSizeAntiresolvingCount++;
					}
					currentSet = combIterator.nextCombinationOrdered();
				}
				System.out.println("Found " + thisSizeAntiresolvingCount + " 1-antiresolving sets of size " + i);
			}
			// Perform modification
			if (changesNeeded)
				for (String v : optDistinguishers)
					if (graph.containsEdge(uOpt, v) && !graph.containsEdge(vOpt, v))
						graph.addEdge(vOpt, v);
					else if (graph.containsEdge(vOpt, v) && !graph.containsEdge(uOpt, v))
						graph.addEdge(uOpt, v);
			
			iterCount++;
			System.out.println("Iteration " + iterCount + " finished");
			System.out.println("Found " + antiresolvingCount + " 1-antiresolving sets");
			System.out.println("Found " + resolvableCount + " resolvables");
			System.out.println("Current size: " + graph.edgeSet().size());
		}
		
		System.out.println("Final size: " + graph.edgeSet().size());
		System.out.println("=====================================================");
	}
	
	//==================================================================================================================
	
	public static void kEllAdjAnonymousTransformation(UndirectedGraph<String, DefaultEdge> graph, int k, int ell, int t) {
		
		// We first force the graph to be at least (2,1)-adjacency anonymous, given that the changes performed to attain that purpose 
		// are never reverted by the remaining edge additions performed by this method 
		
		int origSize = graph.edgeSet().size();
		
		boolean verbose = false;
		
		if (verbose)
			System.out.println("Min degree before preprocessing: " + GraphUtil.minDegree(graph));
		
		enforceK1AdjAnonymity(graph, 2);
		
		if (verbose) {
			System.out.println("Initially added: " + (graph.edgeSet().size() - origSize));
			System.out.println("Min degree after preprocessing: " + GraphUtil.minDegree(graph));
		}
		
		// From now on, any 1-antiresolving set will have size at least 2
		
		// Create initial 2-antiresolving set index
		if (verbose) {
			System.out.println("Processing new graph");
			System.out.println("Order: " + graph.vertexSet().size());
			System.out.println("Original size: " + graph.edgeSet().size());
			System.out.println("ell: " + ell);
		}
		
		List<String> orderedVertexSet = new ArrayList<>(graph.vertexSet());
		
		TreeMap<VertexPair, Integer> candNonEdges = new TreeMap<>();
		for (int i = 0; i < orderedVertexSet.size() - 1; i++) {
			for (int j = i + 1; j < orderedVertexSet.size(); j++) 
				if (!graph.containsEdge(orderedVertexSet.get(i), orderedVertexSet.get(j))) { 
					candNonEdges.put(new VertexPair(orderedVertexSet.get(i), orderedVertexSet.get(j)), 0);
				}
		}
		
		if (verbose)
			System.out.println("Number of candidate non-edges: " + candNonEdges.size());
		
		// Find original antiresolving sets
		Map<List<String>, Set<String>> originalAntiresolving = new HashMap<>();   // Entry: [1-antiresolving set, list of 1-resolvable vertices]
		int countAntiResPrevSizes = 0, countUniquelyIdentifiable = 0, countRepr1 = 0;
		TreeMap<Integer,Integer> countIsomorphismClasses = new TreeMap<>();
		for (int i = 1; i <= ell; i++) {
			CombinationIterator<String> combIterator = new CombinationIterator<>(orderedVertexSet, i);
			List<String> currentSet = combIterator.nextCombinationOrdered();
			while (currentSet != null) { 
				
				// Obtain adjacency representations of every neighboring vertex
				TreeMap<String, Set<String>> vertsXRepr = new TreeMap<>();
				for (String v : neighborhood(graph, currentSet)) {
					String adjRepr = adjRepresentation(graph, v, currentSet);
					if (vertsXRepr.containsKey(adjRepr))
						vertsXRepr.get(adjRepr).add(v);
					else {
						Set<String> verts = new HashSet<>();
						verts.add(v);
						vertsXRepr.put(adjRepr, verts);
					}
				}
				
				// Determine whether currentSet is 1-antiresolving
				boolean currentIsUniquelyIdentifiable1Antiresolving = false, currentHasRepr1 = false;
				for (String repr : vertsXRepr.keySet())
					if (vertsXRepr.get(repr).size() < k) {   // Is k'-antiresolving with k' < k
						
						// Determine whether currentSet is uniquely retrievable
						if (!currentIsUniquelyIdentifiable1Antiresolving) {   // First (k' < k)-resolvable just found, this set hadn't been analyzed for uniquely identifiability
							List<String[]> candidates = GraphUtil.getPotentialAttackerCandidates(GraphUtil.getFingerprintDegrees(graph, currentSet), GraphUtil.getFingerprintLinks(graph, currentSet), graph);
							
							if (countIsomorphismClasses.keySet().contains(candidates.size()))
								countIsomorphismClasses.put(candidates.size(), countIsomorphismClasses.get(candidates.size()).intValue() + 1);
							else
								countIsomorphismClasses.put(candidates.size(), 1);
							
							
							
							if (candidates.size() <= t) {   // It is isomorphic with too few other sets
								countUniquelyIdentifiable++;
								currentIsUniquelyIdentifiable1Antiresolving = true;
								
								if (!repr.contains("2")) {
									countRepr1++;
									currentHasRepr1 = true;
								}
							}
							else
								break;
							
						}
						
						if (originalAntiresolving.containsKey(currentSet))
							originalAntiresolving.get(currentSet).addAll(vertsXRepr.get(repr));   // Despite calling addAll, a single element is being added
						else {
							HashSet<String> newEntry = new HashSet<String>();
							newEntry.addAll(vertsXRepr.get(repr));   // Despite calling addAll, a single element is being added
							originalAntiresolving.put(currentSet, newEntry);
						}
						
						currentIsUniquelyIdentifiable1Antiresolving = true;
						//break;   // Uncomment if no need for storing all (k' < k)-resolvables
					}
				
				if (currentIsUniquelyIdentifiable1Antiresolving) {
					for (VertexPair nonEdge : candNonEdges.keySet())
						if ((currentSet.contains(nonEdge.source) && !currentSet.contains(nonEdge.dest))   // This IS the appropriate condition for being a candidate. It is the ranking that should be improved  
							|| (currentSet.contains(nonEdge.dest) && !currentSet.contains(nonEdge.source)))
							candNonEdges.put(nonEdge, candNonEdges.get(nonEdge).intValue() + 1);
				}
				
				currentSet = combIterator.nextCombinationOrdered();
			}
			
			if (verbose)
				System.out.println("Original number of antiresolving sets of size " + i + ": " + (originalAntiresolving.size() - countAntiResPrevSizes));
			countAntiResPrevSizes = originalAntiresolving.size();
			
		}
		
		if (verbose) {
			System.out.println("Original number of antiresolving sets: " + originalAntiresolving.size());
			System.out.println("Original number of uniquely identifiable antiresolving sets: " + countUniquelyIdentifiable);
			System.out.println("Original number of vertices with representation (1,1,...,1): " + countRepr1);
			System.out.println("Isomorphism classes sizes:");
			for (Integer cnt : countIsomorphismClasses.keySet())
				System.out.println("\t" + cnt.toString() + ": " + countIsomorphismClasses.get(cnt).toString());
		}
		
		// Sort candidate non-edges by number of times they would modify some fingerprint of a 1-resolvable vertex w.r.t. a 1-antiresolving set
		TreeSet<ScoredVertexPair> sortedCandNoEdges = new TreeSet<>(Collections.reverseOrder());
		for (VertexPair nonEdge : candNonEdges.keySet()) {
			int count = candNonEdges.get(nonEdge);
			if (count > 0)
				sortedCandNoEdges.add(new ScoredVertexPair(nonEdge.source, nonEdge.dest, count));
		}
		
		// Eliminating original 1-antiresolving sets
		Map<List<String>, Set<String>> remainingAntiresolving = originalAntiresolving;
		while (remainingAntiresolving.size() > 0 && sortedCandNoEdges.size() > 0) {
			
			// Get first non-edge by score such that its inclusion does not re-introduce original 1-antiresolving sets that have already been made k-antiresolving
			ScoredVertexPair selectedNonEdge = null;
			int maxRemoved = -1;
			int minReintroduced = originalAntiresolving.size() + 1;
			int maxBalance = -originalAntiresolving.size() - 1;
			for (ScoredVertexPair svp : sortedCandNoEdges) {
				// Check that its inclusion does not re-introduce original 1-antiresolving sets that have already been made k-antiresolving
				int removed = 0, reintroduced = 0;
				for (List<String> origAntiRes : originalAntiresolving.keySet()) {
					boolean wouldBeLessThanKAntiresolving = isLessThanKAntiresolving(graph, svp.source, svp.dest, origAntiRes, k);
					if (remainingAntiresolving.keySet().contains(origAntiRes) && !wouldBeLessThanKAntiresolving)   // This would be removed here
						removed++;
					else if (!remainingAntiresolving.keySet().contains(origAntiRes) && wouldBeLessThanKAntiresolving)   // This was removed in a previous step and would be reintroduced here
						reintroduced++; 
				}
				
				if (removed > maxRemoved) {
					maxRemoved = removed;
					minReintroduced = reintroduced;
					selectedNonEdge = svp;
				}
				else if (removed == maxRemoved && reintroduced < minReintroduced) {
					minReintroduced = reintroduced;
					selectedNonEdge = svp;
				}
				
//				if (removed - reintroduced > maxBalance) {
//					maxBalance = removed - reintroduced;
//					selectedNonEdge = svp;
//				}
			}
			
			if (verbose)
				System.out.println("maxRemoved == " + maxRemoved + ", minReintroduced == " + minReintroduced);
			
			// If it was impossible to select one non-edge that does not reintroduce previously deleted 1-antiresolving sets, 
			// we will just take the best-scored one
			// NOW THIS SHOULD NEVER HAPPEN
			if (selectedNonEdge == null) {	
				for (ScoredVertexPair svp : sortedCandNoEdges) {
					selectedNonEdge = svp;
					break;
				}
			}
			
			// Add the selected edge to the graph
			graph.addEdge(selectedNonEdge.source, selectedNonEdge.dest); 
			candNonEdges.remove(selectedNonEdge);
			
			// Update information
			remainingAntiresolving = new HashMap<>();
			// Reinitialize candNonEdges
			for (VertexPair nonEdge : candNonEdges.keySet())
				candNonEdges.put(nonEdge, 0);
			for (List<String> origAntiRes : originalAntiresolving.keySet())
				if (is1Antiresolving(graph, origAntiRes)) {
					remainingAntiresolving.put(origAntiRes, originalAntiresolving.get(origAntiRes));
					// Update scores for candidate non-edges
					for (VertexPair nonEdge : candNonEdges.keySet())
						if ((origAntiRes.contains(nonEdge.source) && !origAntiRes.contains(nonEdge.dest))  
							|| (origAntiRes.contains(nonEdge.dest) && !origAntiRes.contains(nonEdge.source)))   // This IS the appropriate condition for being a candidate. It is the ranking that should be improved
							candNonEdges.put(nonEdge, candNonEdges.get(nonEdge).intValue() + 1);
				}
			if (verbose)
				System.out.println("Added edge " + selectedNonEdge.toString() + ". Number of remaining unperturbed antiresolving sets: " + remainingAntiresolving.size());
			if (remainingAntiresolving.size() > 0) {
				// Re-sort candidate non-edges
				sortedCandNoEdges = new TreeSet<>(Collections.reverseOrder());
				for (VertexPair nonEdge : candNonEdges.keySet()) {
					int count = candNonEdges.get(nonEdge).intValue();
					if (count > 0)
						sortedCandNoEdges.add(new ScoredVertexPair(nonEdge.source, nonEdge.dest, count));
				}
			}
		}
	}
	
	//==================================================================================================================
	
		public static void kEllAdjAnonymousTransformation(UndirectedGraph<String, DefaultEdge> graph, int k, int ell) {
			
			// We first force the graph to be at least (2,1)-adjacency anonymous, given that the changes performed to attain that purpose 
			// are never reverted by the remaining edge additions performed by this method 
			
			int origSize = graph.edgeSet().size();
			
			boolean verbose = false;
			
			if (verbose)
				System.out.println("Min degree before preprocessing: " + GraphUtil.minDegree(graph));
			
			enforceK1AdjAnonymity(graph, 2);
			
			if (verbose) {
				System.out.println("Initially added: " + (graph.edgeSet().size() - origSize));
				System.out.println("Min degree after preprocessing: " + GraphUtil.minDegree(graph));
			}
			
			// From now on, any 1-antiresolving set will have size at least 2
			
			// Create initial 2-antiresolving set index
			if (verbose) {
				System.out.println("Processing new graph");
				System.out.println("Order: " + graph.vertexSet().size());
				System.out.println("Original size: " + graph.edgeSet().size());
				System.out.println("ell: " + ell);
			}
			
			List<String> orderedVertexSet = new ArrayList<>(graph.vertexSet());
			
			TreeMap<VertexPair, Integer> candNonEdges = new TreeMap<>();
			for (int i = 0; i < orderedVertexSet.size() - 1; i++) {
				for (int j = i + 1; j < orderedVertexSet.size(); j++) 
					if (!graph.containsEdge(orderedVertexSet.get(i), orderedVertexSet.get(j))) { 
						candNonEdges.put(new VertexPair(orderedVertexSet.get(i), orderedVertexSet.get(j)), 0);
					}
			}
			
			if (verbose)
				System.out.println("Number of candidate non-edges: " + candNonEdges.size());
			
			// Find original antiresolving sets
			Map<List<String>, Set<String>> originalAntiresolving = new HashMap<>();   // Entry: [1-antiresolving set, list of 1-resolvable vertices]
			int countAntiResPrevSizes = 0, countRepr1 = 0;
			TreeMap<Integer,Integer> countIsomorphismClasses = new TreeMap<>();
			for (int i = 1; i <= ell; i++) {
				CombinationIterator<String> combIterator = new CombinationIterator<>(orderedVertexSet, i);
				List<String> currentSet = combIterator.nextCombinationOrdered();
				while (currentSet != null) { 
					
					// Obtain adjacency representations of every neighboring vertex
					TreeMap<String, Set<String>> vertsXRepr = new TreeMap<>();
					for (String v : neighborhood(graph, currentSet)) {
						String adjRepr = adjRepresentation(graph, v, currentSet);
						if (vertsXRepr.containsKey(adjRepr))
							vertsXRepr.get(adjRepr).add(v);
						else {
							Set<String> verts = new HashSet<>();
							verts.add(v);
							vertsXRepr.put(adjRepr, verts);
						}
					}
					
					for (String repr : vertsXRepr.keySet())
						if (vertsXRepr.get(repr).size() < k) {   // Is k'-antiresolving with k' < k
							
							List<String[]> candidates = GraphUtil.getPotentialAttackerCandidates(GraphUtil.getFingerprintDegrees(graph, currentSet), GraphUtil.getFingerprintLinks(graph, currentSet), graph);
							
							if (countIsomorphismClasses.keySet().contains(candidates.size()))
								countIsomorphismClasses.put(candidates.size(), countIsomorphismClasses.get(candidates.size()).intValue() + 1);
							else
								countIsomorphismClasses.put(candidates.size(), 1);
								
							if (!repr.contains("2")) {
								countRepr1++;
							}
								
							
							if (originalAntiresolving.containsKey(currentSet))
								originalAntiresolving.get(currentSet).addAll(vertsXRepr.get(repr));   // Despite calling addAll, a single element is being added
							else {
								HashSet<String> newEntry = new HashSet<String>();
								newEntry.addAll(vertsXRepr.get(repr));   // Despite calling addAll, a single element is being added
								originalAntiresolving.put(currentSet, newEntry);
							}
							
							
							//break;   // Uncomment if no need for storing all (k' < k)-resolvables
						}
					
					
						for (VertexPair nonEdge : candNonEdges.keySet())
							if ((currentSet.contains(nonEdge.source) && !currentSet.contains(nonEdge.dest))   // This IS the appropriate condition for being a candidate. It is the ranking that should be improved  
								|| (currentSet.contains(nonEdge.dest) && !currentSet.contains(nonEdge.source)))
								candNonEdges.put(nonEdge, candNonEdges.get(nonEdge).intValue() + 1);
					
					
					currentSet = combIterator.nextCombinationOrdered();
				}
				
				if (verbose)
					System.out.println("Original number of antiresolving sets of size " + i + ": " + (originalAntiresolving.size() - countAntiResPrevSizes));
				countAntiResPrevSizes = originalAntiresolving.size();
				
			}
			
			if (verbose) {
				System.out.println("Original number of antiresolving sets: " + originalAntiresolving.size());
				System.out.println("Original number of vertices with representation (1,1,...,1): " + countRepr1);
				System.out.println("Isomorphism classes sizes:");
				for (Integer cnt : countIsomorphismClasses.keySet())
					System.out.println("\t" + cnt.toString() + ": " + countIsomorphismClasses.get(cnt).toString());
			}
			
			// Sort candidate non-edges by number of times they would modify some fingerprint of a 1-resolvable vertex w.r.t. a 1-antiresolving set
			TreeSet<ScoredVertexPair> sortedCandNoEdges = new TreeSet<>(Collections.reverseOrder());
			for (VertexPair nonEdge : candNonEdges.keySet()) {
				int count = candNonEdges.get(nonEdge);
				if (count > 0)
					sortedCandNoEdges.add(new ScoredVertexPair(nonEdge.source, nonEdge.dest, count));
			}
			
			// Eliminating original 1-antiresolving sets
			Map<List<String>, Set<String>> remainingAntiresolving = originalAntiresolving;
			while (remainingAntiresolving.size() > 0 && sortedCandNoEdges.size() > 0) {
				
				// Get first non-edge by score such that its inclusion does not re-introduce original 1-antiresolving sets that have already been made k-antiresolving
				ScoredVertexPair selectedNonEdge = null;
				int maxRemoved = -1;
				int minReintroduced = originalAntiresolving.size() + 1;
				int maxBalance = -originalAntiresolving.size() - 1;
				for (ScoredVertexPair svp : sortedCandNoEdges) {
					// Check that its inclusion does not re-introduce original 1-antiresolving sets that have already been made k-antiresolving
					int removed = 0, reintroduced = 0;
					for (List<String> origAntiRes : originalAntiresolving.keySet()) {
						boolean wouldBeLessThanKAntiresolving = isLessThanKAntiresolving(graph, svp.source, svp.dest, origAntiRes, k);
						if (remainingAntiresolving.keySet().contains(origAntiRes) && !wouldBeLessThanKAntiresolving)   // This would be removed here
							removed++;
						else if (!remainingAntiresolving.keySet().contains(origAntiRes) && wouldBeLessThanKAntiresolving)   // This was removed in a previous step and would be reintroduced here
							reintroduced++; 
					}
					
					if (removed > maxRemoved) {
						maxRemoved = removed;
						minReintroduced = reintroduced;
						selectedNonEdge = svp;
					}
					else if (removed == maxRemoved && reintroduced < minReintroduced) {
						minReintroduced = reintroduced;
						selectedNonEdge = svp;
					}
					
//					if (removed - reintroduced > maxBalance) {
//						maxBalance = removed - reintroduced;
//						selectedNonEdge = svp;
//					}
				}
				
				if (verbose)
					System.out.println("maxRemoved == " + maxRemoved + ", minReintroduced == " + minReintroduced);
				
				// If it was impossible to select one non-edge that does not reintroduce previously deleted 1-antiresolving sets, 
				// we will just take the best-scored one
				// NOW THIS SHOULD NEVER HAPPEN
				if (selectedNonEdge == null) {	
					for (ScoredVertexPair svp : sortedCandNoEdges) {
						selectedNonEdge = svp;
						break;
					}
				}
				
				// Add the selected edge to the graph
				graph.addEdge(selectedNonEdge.source, selectedNonEdge.dest); 
				candNonEdges.remove(selectedNonEdge);
				
				// Update information
				remainingAntiresolving = new HashMap<>();
				// Reinitialize candNonEdges
				for (VertexPair nonEdge : candNonEdges.keySet())
					candNonEdges.put(nonEdge, 0);
				for (List<String> origAntiRes : originalAntiresolving.keySet())
					if (is1Antiresolving(graph, origAntiRes)) {
						remainingAntiresolving.put(origAntiRes, originalAntiresolving.get(origAntiRes));
						// Update scores for candidate non-edges
						for (VertexPair nonEdge : candNonEdges.keySet())
							if ((origAntiRes.contains(nonEdge.source) && !origAntiRes.contains(nonEdge.dest))  
								|| (origAntiRes.contains(nonEdge.dest) && !origAntiRes.contains(nonEdge.source)))   // This IS the appropriate condition for being a candidate. It is the ranking that should be improved
								candNonEdges.put(nonEdge, candNonEdges.get(nonEdge).intValue() + 1);
					}
				if (verbose)
					System.out.println("Added edge " + selectedNonEdge.toString() + ". Number of remaining unperturbed antiresolving sets: " + remainingAntiresolving.size());
				if (remainingAntiresolving.size() > 0) {
					// Re-sort candidate non-edges
					sortedCandNoEdges = new TreeSet<>(Collections.reverseOrder());
					for (VertexPair nonEdge : candNonEdges.keySet()) {
						int count = candNonEdges.get(nonEdge).intValue();
						if (count > 0)
							sortedCandNoEdges.add(new ScoredVertexPair(nonEdge.source, nonEdge.dest, count));
					}
				}
			}
		}
	
	//==================================================================================================================
	
	public static void perturbFingerprintsOriginal1AntiresolvingSets(UndirectedGraph<String, DefaultEdge> graph, int ell) {
		
		// Create initial 2-antiresolving set index
		System.out.println("Processing new graph");
		System.out.println("Order: " + graph.vertexSet().size());
		System.out.println("Original size: " + graph.edgeSet().size());
		System.out.println("ell: " + ell);
		
		List<String> orderedVertexSet = new ArrayList<>(graph.vertexSet());
		
		TreeMap<VertexPair, Integer> candNonEdges = new TreeMap<>();
		for (int i = 0; i < orderedVertexSet.size() - 1; i++) {
			for (int j = i + 1; j < orderedVertexSet.size(); j++) 
				if (!graph.containsEdge(orderedVertexSet.get(i), orderedVertexSet.get(j))) { 
					candNonEdges.put(new VertexPair(orderedVertexSet.get(i), orderedVertexSet.get(j)), 0);
				}
		}
		
		System.out.println("Number of candidate non-edges: " + candNonEdges.size());
		
		// Find original antiresolving sets
		Map<List<String>, Set<String>> originalAntiresolving = new HashMap<>();
		for (int i = 1; i <= ell; i++) {
			CombinationIterator<String> combIterator = new CombinationIterator<>(orderedVertexSet, i);
			List<String> currentSet = combIterator.nextCombinationOrdered();
			while (currentSet != null) {
				// Obtain adjacency representations of every neighboring vertex
				TreeMap<String, Set<String>> vertsXRepr = new TreeMap<>();
				for (String v : neighborhood(graph, currentSet)) {
					String adjRepr = adjRepresentation(graph, v, currentSet);
					if (vertsXRepr.containsKey(adjRepr))
						vertsXRepr.get(adjRepr).add(v);
					else {
						Set<String> verts = new HashSet<>();
						verts.add(v);
						vertsXRepr.put(adjRepr, verts);
					}
				}
				// Determine whether currentSet is 1-antiresolving
				for (String repr : vertsXRepr.keySet())
					if (vertsXRepr.get(repr).size() == 1) {   // Is 1-antiresolving
						if (originalAntiresolving.containsKey(currentSet))
							originalAntiresolving.get(currentSet).addAll(vertsXRepr.get(repr));   // Despite calling addAll, a single element is being added
						else {
							HashSet<String> newEntry = new HashSet<String>();
							newEntry.addAll(vertsXRepr.get(repr));   // Despite calling addAll, a single element is being added
							originalAntiresolving.put(currentSet, newEntry);
						}
						for (VertexPair nonEdge : candNonEdges.keySet())
							if ((currentSet.contains(nonEdge.source) && vertsXRepr.get(repr).contains(nonEdge.dest)) 
								|| (currentSet.contains(nonEdge.dest) && vertsXRepr.get(repr).contains(nonEdge.source)))
								candNonEdges.put(nonEdge, candNonEdges.get(nonEdge).intValue() + 1);
						//break;
					}
				
				currentSet = combIterator.nextCombinationOrdered();
			}
		}
		
		System.out.println("Original number of antiresolving sets: " + originalAntiresolving.size());
		
		// Sort candidate non-edges by number of original antiresolving sets they disturb
		TreeSet<ScoredVertexPair> sortedCandNoEdges = new TreeSet<>(Collections.reverseOrder());
		for (VertexPair nonEdge : candNonEdges.keySet()) {
			int count = candNonEdges.get(nonEdge).intValue();
			if (count > 0)
				sortedCandNoEdges.add(new ScoredVertexPair(nonEdge.source, nonEdge.dest, count));
		}
		
		// Perturbation	
		while (originalAntiresolving.size() > 0 && sortedCandNoEdges.size() > 0) {
			// Get first non-edge
			ScoredVertexPair firstNonEdge = (ScoredVertexPair)sortedCandNoEdges.toArray()[0]; 
			graph.addEdge(firstNonEdge.source, firstNonEdge.dest);
			Map<List<String>, Set<String>> remainingAntiresolving = new HashMap<>();
			for (List<String> antiRes : originalAntiresolving.keySet())
				if (!antiRes.contains(firstNonEdge.source) && !antiRes.contains(firstNonEdge.dest))
					remainingAntiresolving.put(antiRes, originalAntiresolving.get(antiRes));
				else   // As this 1-antiresolving set will no longer be accounted for, the counts for would-be incident non-edges must be decreased
					for (VertexPair nonEdge : candNonEdges.keySet())
						for (String resolvable : originalAntiresolving.get(antiRes))
							if ((antiRes.contains(nonEdge.source) && resolvable.equals(nonEdge.dest)) 
								|| (antiRes.contains(nonEdge.dest) && resolvable.equals(nonEdge.source)))
								candNonEdges.put(nonEdge, candNonEdges.get(nonEdge).intValue() - 1);
			System.out.println("Added edge " + firstNonEdge.toString() + ". Number of remaining unperturbed antiresolving sets: " + remainingAntiresolving.size());
			if (remainingAntiresolving.size() > 0) {
				// Re-sort candidate non-edges
				sortedCandNoEdges = new TreeSet<>(Collections.reverseOrder());
				for (VertexPair nonEdge : candNonEdges.keySet()) {
					int count = candNonEdges.get(nonEdge).intValue();
					if (count > 0)
						sortedCandNoEdges.add(new ScoredVertexPair(nonEdge.source, nonEdge.dest, count));
				}
			}
		}
	}
	
	//==================================================================================================================
	
	/***
	 * 
	 * Auxiliary service methods
	 * 
	 */
	
	//==================================================================================================================
	
	static Set<String> neighborhood(UndirectedGraph<String, DefaultEdge> graph, List<String> vertSet) {
		Set<String> nbhood = new HashSet<>();
		for (String v : vertSet)
			nbhood.addAll(Graphs.neighborListOf(graph, v));
		nbhood.removeAll(vertSet);
		return nbhood;
	}
	
	//==================================================================================================================
	
	static String adjRepresentation(UndirectedGraph<String, DefaultEdge> graph, String v, List<String> vertSet) {
		String representation = "";
		for (int i = 0; i < vertSet.size(); i++)
			if (graph.containsEdge(v, vertSet.get(i)))
				representation += "1";
			else
				representation += "2";
		return representation;
	}
	
	//==================================================================================================================
	
	static String adjRepresentation(UndirectedGraph<String, DefaultEdge> graph, String u, String v, String x, List<String> vertSet) {
		String representation = "";
		for (int i = 0; i < vertSet.size(); i++)
			if (graph.containsEdge(x, vertSet.get(i)) || (x.equals(u) && v.equals(vertSet.get(i))) || (x.equals(v) && u.equals(vertSet.get(i))))
				representation += "1";
			else
				representation += "2";
		return representation;
	}
	
	//==================================================================================================================
	
	static Set<String> distinguishingVertices(UndirectedGraph<String, DefaultEdge> graph, List<String> vertSet, String v1, String v2) {
		//System.out.println("vertSet: " + vertSet);
		//System.out.println("v1: " + v1);
		//System.out.println("v2: " + v2);
		Set<String> inter1 = new HashSet<>(vertSet);
		//System.out.println("N(v1): " + Graphs.neighborListOf(graph, v1));
		inter1.retainAll(Graphs.neighborListOf(graph, v1));
		//System.out.println("inter1: " + inter1);
		Set<String> inter2 = new HashSet<>(vertSet);
		//System.out.println("N(v1): " + Graphs.neighborListOf(graph, v1));
		inter2.retainAll(Graphs.neighborListOf(graph, v2));
		//System.out.println("inter2: " + inter2);
		Set<String> diff12 = new HashSet<>(inter1);
		diff12.removeAll(inter2);
		//System.out.println("diff12: " + diff12);
		Set<String> diff21 = new HashSet<>(inter2);
		diff21.removeAll(inter1);
		//System.out.println("diff21: " + diff21);
		diff12.addAll(diff21);
		//System.out.println("returns: " + diff12);
		return diff12;
	}
	
	//==================================================================================================================
	
	static boolean is1Antiresolving(UndirectedGraph<String, DefaultEdge> graph, List<String> vertSet) {
		// Obtain adjacency representations of every neighboring vertex
		TreeMap<String, Set<String>> vertsXRepr = new TreeMap<>();
		for (String v : neighborhood(graph, vertSet)) {
			String adjRepr = adjRepresentation(graph, v, vertSet);
			if (vertsXRepr.containsKey(adjRepr))
				vertsXRepr.get(adjRepr).add(v);
			else {
				Set<String> verts = new HashSet<>();
				verts.add(v);
				vertsXRepr.put(adjRepr, verts);
			}
		}		
		// Determine if vertSet is 1-antiresolving
		for (String repr : vertsXRepr.keySet())
			if (vertsXRepr.get(repr).size() == 1)
				return true;
		return false;
	}
	
	//==================================================================================================================
	
	static boolean isLessThanKAntiresolving(UndirectedGraph<String, DefaultEdge> graph, String u, String v, List<String> vertSet, int k) { 
		// Obtain adjacency representations of every neighboring vertex
		TreeMap<String, Set<String>> vertsXRepr = new TreeMap<>();
		for (String x : neighborhood(graph, vertSet)) {
			String adjRepr = adjRepresentation(graph, u, v, x, vertSet);
			if (vertsXRepr.containsKey(adjRepr))
				vertsXRepr.get(adjRepr).add(x);
			else {
				Set<String> verts = new HashSet<>();
				verts.add(x);
				vertsXRepr.put(adjRepr, verts);
			}
		}		
		// Determine if vertSet is 1-antiresolving
		for (String repr : vertsXRepr.keySet())
			if (vertsXRepr.get(repr).size() < k)
				return true;
		return false;
	}
	
	//==================================================================================================================
	
	static boolean checkAdjAnonymousTransf(UndirectedGraph<String, DefaultEdge> graph, int k, Set<String> originalLowDeg, Set<String> originalHighDeg) {
		int n = graph.vertexSet().size();
		for (String v : graph.vertexSet()) {
			int deg = graph.degreeOf(v);
			if (deg > 0 && deg < k && originalLowDeg.contains(v)) 
				return false;
			else if (deg > n - k - 1 && deg < n - 1 && originalHighDeg.contains(v))
				return false;
		}
		return true;
	}
	
}
