package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class WrapperMETIS {
	
	String executableName;
	String workDirName;

	public WrapperMETIS(String eName, String wName) {
		executableName = eName;
		workDirName = wName;
	}
	
	public List<UndirectedGraph<String, DefaultEdge>> getPartitionSubgraphs(UndirectedGraph<String, DefaultEdge> graph, int k, String uniqueIdFileName, boolean weightedVertices) throws IOException, InterruptedException {
		
		// Run METIS
		int startingVertId = GraphUtil.minVertexId(graph);
		int vertIdOffset = 1 - startingVertId;   // To make sure that vertex ids start at 1 as required by METIS
		runMETIS(graph, k, uniqueIdFileName, weightedVertices, vertIdOffset);
		
		// Load output
		Map<String, Set<String>> vertsXPart = loadOutputFromFile(workDirName + java.io.File.separator + "workingGraph-" + uniqueIdFileName + ".txt.part." + k, startingVertId);
		
		// Build partition subgraphs
		List<UndirectedGraph<String, DefaultEdge>> partitions = new ArrayList<>();
		for (String pid : vertsXPart.keySet())
			partitions.add(GraphUtil.inducedSubgraph(graph, vertsXPart.get(pid)));
		
		return partitions;
	}
	
	public Map<String, Set<String>> getPartitionVertSets(UndirectedGraph<String, DefaultEdge> graph, int k, String uniqueIdFileName, boolean weightedVertices) throws IOException, InterruptedException {
		
		// Run METIS
		int startingVertId = GraphUtil.minVertexId(graph);
		int vertIdOffset = 1 - startingVertId;   // To make sure that vertex ids start at 1 as required by METIS
		runMETIS(graph, k, uniqueIdFileName, weightedVertices, vertIdOffset);
				
		// Load output
		Map<String, Set<String>> vertsXPart = loadOutputFromFile(workDirName + java.io.File.separator + "workingGraph-" + uniqueIdFileName + ".txt.part." + k, startingVertId);
		
		return vertsXPart;
	}
	
	protected void runMETIS(UndirectedGraph<String, DefaultEdge> graph, int k, String uniqueIdFileName, boolean weightedVertices, int vertIdOffset) throws IOException, InterruptedException {
		
		// Generate input
		generateInputFile(graph, workDirName + java.io.File.separator + "workingGraph-" + uniqueIdFileName + ".txt", weightedVertices, vertIdOffset);
		
		// Run METIS
		Runtime rt = Runtime.getRuntime();
		String command = executableName + " " + workDirName + java.io.File.separator + "workingGraph-" + uniqueIdFileName + ".txt " + k;
		Process proc = rt.exec(command);
		proc.waitFor();
		int exitVal = proc.exitValue();
		if (exitVal != 0)   // Unsuccessful execution 	
			throw new RuntimeException();
		
	}
	
	protected void generateInputFile(UndirectedGraph<String, DefaultEdge> graph, String fileName, boolean weightedVertices, int vertIdOffset) throws IOException { 
		final String NEW_LINE = System.getProperty("line.separator");
		int maxDeg = -1;
		if (weightedVertices) {
			for (String v : graph.vertexSet())
				if (graph.degreeOf(v) > maxDeg)
					maxDeg = graph.degreeOf(v);
			maxDeg++;   // So we get weight 1 for vertices of degree maxDeg, instead of 0
		}
		Writer metisInputWriter = new FileWriter(fileName, false);
		if (weightedVertices)
			metisInputWriter.append("" + graph.vertexSet().size() + " " + graph.edgeSet().size() + " 010 1" + NEW_LINE);
		else
			metisInputWriter.append("" + graph.vertexSet().size() + " " + graph.edgeSet().size() + NEW_LINE);
		for (String v : graph.vertexSet()) {
			String line = ""; 
			if (weightedVertices)
				line += (maxDeg - graph.degreeOf(v)) + " ";
			for (String w : Graphs.neighborListOf(graph, v))
				line += (Integer.parseInt(w) + vertIdOffset) + " ";
			metisInputWriter.append(line.trim() + NEW_LINE);
		}
		metisInputWriter.close();
	}
	
	protected Map<String, Set<String>> loadOutputFromFile(String fileName, int startingVertId) throws IOException {
		Map<String, Set<String>> vertsXPart = new TreeMap<>();
		int vertId = startingVertId;
		BufferedReader metisOuputReader = new BufferedReader(new FileReader(new File(fileName)));
		for (String line = metisOuputReader.readLine(); line != null; line = metisOuputReader.readLine()) {
			if (!line.trim().equals("")) {
				if (vertsXPart.containsKey(line.trim()))
					vertsXPart.get(line.trim()).add(vertId+"");
				else {
					Set<String> newPart = new TreeSet<>();
					newPart.add(vertId+"");
					vertsXPart.put(line.trim(), newPart);
				}
				vertId++;
			}
		}
		metisOuputReader.close();
		return vertsXPart;
	}

}
