package real;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import util.GraphUtil;

public class FacebookEgoNetwork extends SimpleGraph<String, DefaultEdge> {
	
	private static final long serialVersionUID = 2201631506342282914L;

	public FacebookEgoNetwork(Class<? extends DefaultEdge> edgeClass, String egoNetworkId) {
		super(edgeClass);
		try {
			boolean onlyNeighbors = (egoNetworkId.endsWith("-only-neighbors")); 
			if (onlyNeighbors)
				egoNetworkId = egoNetworkId.substring(0, egoNetworkId.length() - 15);   // Remove the "-only-neighbors" suffix 
			else
				this.addVertex(egoNetworkId);
			BufferedReader reader = new BufferedReader(new FileReader(new File("facebook/facebook/" + egoNetworkId + ".edges")));
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				String[] vertices = line.split(" ");					
				if (vertices.length == 2) {
					if (!this.containsVertex(vertices[0])) {
						this.addVertex(vertices[0]);
						if (!onlyNeighbors)
							this.addEdge(egoNetworkId, vertices[0]);
					}
					if (!this.containsVertex(vertices[1])) {
						this.addVertex(vertices[1]);
						if (!onlyNeighbors)
							this.addEdge(egoNetworkId, vertices[1]);
					}
					this.addEdge(vertices[0], vertices[1]);
				}
			}
			reader.close();
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

}
