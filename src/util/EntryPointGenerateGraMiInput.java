package util;

import java.io.IOException;
import org.jgrapht.graph.DefaultEdge;
import real.FacebookGraph;
import real.PanzarasaGraph;
import real.URVMailGraph;

public class EntryPointGenerateGraMiInput {

	public static void main(String[] args) throws IOException {
		GraphUtil.generateGraMiInput(new FacebookGraph(DefaultEdge.class), "C:\\Users\\yunior.ramirez\\up_n_down\\GraMi\\Datasets\\facebook1.lg");
		GraphUtil.generateGraMiInput(new PanzarasaGraph(DefaultEdge.class), "C:\\Users\\yunior.ramirez\\up_n_down\\GraMi\\Datasets\\panzarasa1.lg");
		GraphUtil.generateGraMiInput(new URVMailGraph(DefaultEdge.class), "C:\\Users\\yunior.ramirez\\up_n_down\\GraMi\\Datasets\\urv1.lg");
		GraphUtil.generateGraMiInput(BarabasiAlbertGraphGenerator.newGraph(100, 0, 50, 10, 3), "C:\\Users\\yunior.ramirez\\up_n_down\\GraMi\\Datasets\\scale-free-2.lg");
	}

}
