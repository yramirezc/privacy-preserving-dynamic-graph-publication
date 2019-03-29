package real;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import test.AntiResolving;
import test.AntiResolvingPersistence;
import test.AntiResolvingPersistence.STATE;
import test.Output;

public class PanzarasaGraph extends SimpleGraph<String, DefaultEdge>{

	private static final long serialVersionUID = 2201631506342282914L;

	public PanzarasaGraph(Class<? extends DefaultEdge> edgeClass) {
		super(edgeClass);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("realDataset/OClinks_w.dl")));
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				String[] vertices = line.split(" ");  
				if (vertices.length != 3) continue;
				if (!this.containsVertex(vertices[0])) this.addVertex(vertices[0]); 
				if (!this.containsVertex(vertices[1])) this.addVertex(vertices[1]);
				this.addEdge(vertices[0], vertices[1]);
			}
			reader.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}catch (IOException e2) {
			e2.printStackTrace();
		}
	}

}
