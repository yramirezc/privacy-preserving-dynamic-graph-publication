package utilities;

import java.io.IOException;
import java.util.Locale;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import link_prediction.LongDistanceScorer;
import link_prediction.WalkBasedKatz;
import util.GraphUtil;

public class LinkPredictionBasedUtilities {
	
	public static double weightedSum(String originalGraphPath, String anonymizedGraphPath) {
		return weightedSum(GraphUtil.loadSNAPFormat(originalGraphPath), GraphUtil.loadSNAPFormat(anonymizedGraphPath));
	}
	
	public static double weightedSum(UndirectedGraph<String, DefaultEdge> originalGraph,
			   UndirectedGraph<String, DefaultEdge> anonymizedGraph) {
		double sum = 0d;
		LongDistanceScorer linkScorer = new WalkBasedKatz(originalGraph, 6, 0.85);   // 6 for small word hypothesis
		for (String v1 : anonymizedGraph.vertexSet())
			for (String v2 : anonymizedGraph.vertexSet())
				if (!v1.equals(v2) && anonymizedGraph.containsEdge(v1, v2) && !originalGraph.containsEdge(v1, v2))
					sum += linkScorer.score(v1, v2);
		return sum;
	}
	
	public static void main(String [] args) throws IOException {
		
		String [] anonymizedVersions = new String[]{"anonymized-oddcycle", "randomly-anonymized-equiv-oddcycle",
				"anonymized-shortestcycle", "randomly-anonymized-equiv-shortestcycle",
				"anonymized-largestcycle", "randomly-anonymized-equiv-largestcycle"};
		System.out.println("Katz-weighted sum");
		System.out.println("");
		System.out.println("Method\tFacebook\tPanzarasa");
		UndirectedGraph<String, DefaultEdge> originalFacebook = null, originalPanzarasa = null, anonymizedFacebook = null, anonymizedPanzarasa = null;
		if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0) {   // Running on Windows
			originalFacebook = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonimity-gaia\\down\\outputs\\disk\\RealSocNet\\facebook-original.txt");
			originalPanzarasa = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonimity-gaia\\down\\outputs\\disk\\RealSocNet\\panzarasa-original.txt");
		}
		else {
			originalFacebook = GraphUtil.loadSNAPFormat("facebook-original.txt");
			originalPanzarasa = GraphUtil.loadSNAPFormat("panzarasa-original.txt");
		}
		for (String versionName : anonymizedVersions) {
			if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0) {   // Running on Windows
				anonymizedFacebook = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonimity-gaia\\down\\outputs\\disk\\RealSocNet\\facebook-" + versionName + ".txt");
				anonymizedPanzarasa = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonimity-gaia\\down\\outputs\\disk\\RealSocNet\\panzarasa-" + versionName + ".txt");
			}
			else {
				anonymizedFacebook = GraphUtil.loadSNAPFormat("facebook-" + versionName + ".txt");
				anonymizedPanzarasa = GraphUtil.loadSNAPFormat("panzarasa-" + versionName + ".txt");
			}
			double utilityFacebook = LinkPredictionBasedUtilities.weightedSum(originalFacebook, anonymizedFacebook);
			double utilityPanzarasa = LinkPredictionBasedUtilities.weightedSum(originalPanzarasa, anonymizedPanzarasa);
			System.out.println(versionName + "\t" + utilityFacebook + "\t" + utilityPanzarasa);
		}
	}
	
}
