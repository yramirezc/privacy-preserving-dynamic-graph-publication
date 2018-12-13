package real;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class URVMailGraph extends SimpleGraph<String, DefaultEdge> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2201631506342282914L;

	public URVMailGraph(Class<? extends DefaultEdge> edgeClass) {
		super(edgeClass);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("urv-email/urv-email.txt")));
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				String[] vertices = line.split(" ");  
				if (vertices.length != 3)
					continue;
				if (!vertices[0].equals(vertices[1])) {
					if (!this.containsVertex(vertices[0]))
						this.addVertex(vertices[0]); 
					if (!this.containsVertex(vertices[1]))
						this.addVertex(vertices[1]);
					this.addEdge(vertices[0], vertices[1]);
				}
			}
			reader.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}catch (IOException e2) {
			e2.printStackTrace();
		}
	}

}
