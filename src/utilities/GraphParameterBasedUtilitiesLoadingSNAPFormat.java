package utilities;

import java.util.Locale;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import util.GraphUtil;

public class GraphParameterBasedUtilitiesLoadingSNAPFormat {

	public static void main(String[] args) {
		
		String [] anonymizedVersions = new String[]{"anonymized-oddcycle", "anonymized-shortestcycle", "anonymized-largestcycle"};
		
		System.out.println("");		
		System.out.println("Method\tratioGlobalCC Facebook\tratioGlobalCC Panzarasa\tratioGlobalCC URV");

		UndirectedGraph<String, DefaultEdge> originalFacebook = null, originalPanzarasa = null, originalURV = null, anonymizedFacebook = null, anonymizedPanzarasa = null, anonymizedURV = null;
		if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0) {   // Running on Windows
			originalFacebook = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonymity-gaia\\down\\outputs\\disk\\RealSocNet\\global-mins-maxs-in-paper\\facebook-original.txt");
			originalPanzarasa = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonymity-gaia\\down\\outputs\\disk\\RealSocNet\\global-mins-maxs-in-paper\\panzarasa-original.txt");
			originalURV = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonymity-gaia\\down\\outputs\\disk\\RealSocNet\\global-mins-maxs-in-paper\\urv-original.txt");
		}
		else {
			originalFacebook = GraphUtil.loadSNAPFormat("facebook-original.txt");
			originalPanzarasa = GraphUtil.loadSNAPFormat("panzarasa-original.txt");
			originalURV = GraphUtil.loadSNAPFormat("urv-original.txt");
		}
		
		double glbCCFacebookOrig = GraphParameterBasedUtilitiesJGraphT.globalClusteringCoefficient(originalFacebook);
		System.out.println("Global CC original Facebook: " + glbCCFacebookOrig);
		double glbCCPanzarasaOrig = GraphParameterBasedUtilitiesJGraphT.globalClusteringCoefficient(originalPanzarasa);
		System.out.println("Global CC original Panzarasa: " + glbCCPanzarasaOrig);
		double glbCCURVOrig = GraphParameterBasedUtilitiesJGraphT.globalClusteringCoefficient(originalURV);
		System.out.println("Global CC original URV: " + glbCCURVOrig);
		
		for (String versionName : anonymizedVersions) {
			
			if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0) {   // Running on Windows
				anonymizedFacebook = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonymity-gaia\\down\\outputs\\disk\\RealSocNet\\global-mins-maxs-in-paper\\facebook-" + versionName + ".txt");
				anonymizedPanzarasa = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonymity-gaia\\down\\outputs\\disk\\RealSocNet\\global-mins-maxs-in-paper\\panzarasa-" + versionName + ".txt");
				anonymizedURV = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonymity-gaia\\down\\outputs\\disk\\RealSocNet\\global-mins-maxs-in-paper\\urv-" + versionName + ".txt");
			}
			else {
				anonymizedFacebook = GraphUtil.loadSNAPFormat("facebook-" + versionName + ".txt");
				anonymizedPanzarasa = GraphUtil.loadSNAPFormat("panzarasa-" + versionName + ".txt");
				anonymizedURV = GraphUtil.loadSNAPFormat("urv-" + versionName + ".txt");
			}
						
			double glbCCFacebookAnon = GraphParameterBasedUtilitiesJGraphT.globalClusteringCoefficient(anonymizedFacebook);
			System.out.println("Global CC anonymized Facebook: " + glbCCFacebookAnon);
			double ratioGlobalCCFacebook = glbCCFacebookAnon / glbCCFacebookOrig;
			System.out.println("Facebook ratio: " + ratioGlobalCCFacebook);
			
			double glbCCPanzarasaAnon = GraphParameterBasedUtilitiesJGraphT.globalClusteringCoefficient(anonymizedPanzarasa);
			System.out.println("Global CC anonymized Panzarasa: " + glbCCPanzarasaAnon);
			double ratioGlobalCCPanzarasa = glbCCPanzarasaAnon / glbCCPanzarasaOrig;
			System.out.println("Panzarasa ratio: " + ratioGlobalCCPanzarasa);
			
			double glbCCURVAnon = GraphParameterBasedUtilitiesJGraphT.globalClusteringCoefficient(anonymizedURV);
			System.out.println("Global CC anonymized URV: " + glbCCURVAnon);
			double ratioGlobalCCURV = glbCCURVAnon / glbCCURVOrig;
			System.out.println("URV ratio: " + ratioGlobalCCURV);
			
			System.out.println(versionName + "\t" + ratioGlobalCCFacebook + "\t" + ratioGlobalCCPanzarasa + "\t" + ratioGlobalCCURV);
		}
	}

}
