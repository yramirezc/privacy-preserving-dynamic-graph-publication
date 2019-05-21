package util;

import java.io.IOException;
import org.jgrapht.graph.DefaultEdge;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;

public class EntryPointGenerateMetisInput {

	public static void main(String[] args) throws IOException {
		GraphUtil.generateMetisInput(new FacebookGraph(DefaultEdge.class), "C:\\cygwin64\\home\\yunior.ramirez\\metis-5.1.0\\graphs\\facebook-vw.txt", true);
		GraphUtil.generateMetisInput(new PanzarasaGraph(DefaultEdge.class), "C:\\cygwin64\\home\\yunior.ramirez\\metis-5.1.0\\graphs\\panzarasa-vw.txt", true);
		GraphUtil.generateMetisInput(new URVMailGraph(DefaultEdge.class), "C:\\cygwin64\\home\\yunior.ramirez\\metis-5.1.0\\graphs\\urv-vw.txt", true);
	}

}
