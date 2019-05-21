package real;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class FacebookGraph extends SimpleGraph<String, DefaultEdge> {

	private static final long serialVersionUID = -5391903961911964028L;
	
	public FacebookGraph(Class<? extends DefaultEdge> edgeClass) {
		super(edgeClass);
		try {
			File folder = new File("facebook/facebook");
			File[] files = folder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					if (name.contains("edges")) return true;
					else return false;
				}
			});
			for (File file : files) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				for (String line = reader.readLine(); line != null; line = reader.readLine()){
					String[] vertices = line.split(" ");  
					if (vertices.length == 2) {
						if (!this.containsVertex(vertices[0])) this.addVertex(vertices[0]); 
						if (!this.containsVertex(vertices[1])) this.addVertex(vertices[1]);
						this.addEdge(vertices[0], vertices[1]);
					}
				}
				reader.close();
			}
			BufferedReader reader = new BufferedReader(new FileReader(new File("facebook/facebook_combined.txt")));
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				String[] vertices = line.split(" ");					
				if (vertices.length == 2) {
					if (!this.containsVertex(vertices[0])) this.addVertex(vertices[0]); 
					if (!this.containsVertex(vertices[1])) this.addVertex(vertices[1]);
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
