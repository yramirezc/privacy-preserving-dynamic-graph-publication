package anonymization;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public abstract class KMatchAnonymizer {
	
	protected Map<String, List<String>> globalVAT;

	public abstract void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize);
	
	public abstract void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, boolean randomize);
	
	public abstract void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, int k, boolean randomize, String uniqueIdFileName);
	
	public abstract void anonymizeGraph(UndirectedGraph<String, DefaultEdge> graph, boolean randomize, String uniqueIdFileName);
	
	protected void alignBlocks(UndirectedGraph<String, DefaultEdge> graph, Map<String, List<String>> groupVAT) {
		
		List<String> vatKeys = new ArrayList<>(groupVAT.keySet());
		
		// First, add any dummy vertex in the VAT to fullGraph  
		for (int i = 0; i < vatKeys.size(); i++)
			for (int j = 0; j < groupVAT.get(vatKeys.get(i)).size(); j++)
				if (!graph.containsVertex(groupVAT.get(vatKeys.get(i)).get(j))) 
					graph.addVertex(groupVAT.get(vatKeys.get(i)).get(j));
		
		// Then, perform the alignment directly on fullGraph
		for (int i = 0; i < vatKeys.size() - 1; i++)
			for (int j = i + 1; j < vatKeys.size(); j++) {
				// Check if the edge exists in some block and doesn't exist in some other
				boolean edgeFound = false, nonEdgeFound = false;
				for (int k = 0; (!edgeFound || !nonEdgeFound) && k < groupVAT.get(vatKeys.get(i)).size(); k++) {
					if (graph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)))
						edgeFound = true; 
					if (!graph.containsEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k)))
						nonEdgeFound = true;
				}
				
				// Add edges to align blocks
				if (edgeFound && nonEdgeFound) 
					for (int k = 0; k < groupVAT.get(vatKeys.get(i)).size(); k++) 
						graph.addEdge(groupVAT.get(vatKeys.get(i)).get(k), groupVAT.get(vatKeys.get(j)).get(k));
			}
		
	}
	
	protected void copyCrossingEdges(UndirectedGraph<String, DefaultEdge> graph) {
		// When this method is called, all dummy vertices have already been added by one or several calls of alignBlocks
		List<String> vatKeys = new ArrayList<>(globalVAT.keySet());
		for (int i = 0; i < vatKeys.size(); i++)
			for (int j = 0; j < globalVAT.get(vatKeys.get(i)).size() - 1; j++)
				for (int p = i; p < vatKeys.size(); p++)
					for (int q = j + 1; q < globalVAT.get(vatKeys.get(p)).size(); q++) 
						if (graph.containsEdge(globalVAT.get(vatKeys.get(i)).get(j), globalVAT.get(vatKeys.get(p)).get(q))) {
							int len = globalVAT.get(vatKeys.get(i)).size();
							for (int offset = 1; offset < len; offset++) 
								graph.addEdge(globalVAT.get(vatKeys.get(i)).get((j + offset) % len), globalVAT.get(vatKeys.get(p)).get((q + offset) % len));
						}
	}
	
	protected void randomlyUniformizeCrossingEdges(UndirectedGraph<String, DefaultEdge> graph) {
		// When this method is called, all dummy vertices have already been added by one or several calls of alignBlocks
		SecureRandom random = new SecureRandom();
		List<String> vatKeys = new ArrayList<>(globalVAT.keySet());
		for (int i = 0; i < vatKeys.size(); i++)
			for (int j = 0; j < globalVAT.get(vatKeys.get(i)).size() - 1; j++)
				for (int p = i; p < vatKeys.size(); p++)
					for (int q = j + 1; q < globalVAT.get(vatKeys.get(p)).size(); q++) 
						if (graph.containsEdge(globalVAT.get(vatKeys.get(i)).get(j), globalVAT.get(vatKeys.get(p)).get(q))) {
							int len = globalVAT.get(vatKeys.get(i)).size();
							int countExistingCopies = 1;   // Counting current occurrence as a copy 
							for (int offset = 1; offset < len; offset++) 
								if (graph.containsEdge(globalVAT.get(vatKeys.get(i)).get((j + offset) % len), globalVAT.get(vatKeys.get(p)).get((q + offset) % len)))
									countExistingCopies++;
							// Decide whether to perform an edge copy, with probability countExistingCopies/len, or a removal, with probability (len-countExistingCopies)/len
							boolean performCopy = (random.nextInt(len) < countExistingCopies);
							if (performCopy)   // Uniformize by copying
								for (int offset = 1; offset < len; offset++)
									graph.addEdge(globalVAT.get(vatKeys.get(i)).get((j + offset) % len), globalVAT.get(vatKeys.get(p)).get((q + offset) % len));
							else   // Uniformize by removing
								for (int offset = 0; offset < len; offset++)   // Starts at offset 0 to first remove the current edge
									graph.removeEdge(globalVAT.get(vatKeys.get(i)).get((j + offset) % len), globalVAT.get(vatKeys.get(p)).get((q + offset) % len));
						}
	}
	
	protected Map<String, List<String>> cloneVATRowSet(Map<String, List<String>> origVATRowSet) {
		
		Map<String, List<String>> newVATRowSet = new TreeMap<>();
		
		for (String rid : origVATRowSet.keySet()) {
			List<String> newRow = new ArrayList<>();
			for (int i = 0; i < origVATRowSet.get(rid).size(); i++)
				newRow.add(origVATRowSet.get(rid).get(i));
			newVATRowSet.put(rid, newRow);
		}
		
		return newVATRowSet;
	}

}
