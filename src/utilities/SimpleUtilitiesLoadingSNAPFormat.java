package utilities;

import java.util.Locale;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import util.DegreeDistributionComputer;
import util.DistributionComputer;
import util.GraphUtil;

public class SimpleUtilitiesLoadingSNAPFormat {

	public static void main(String[] args) {
		String [] anonymizedVersions = new String[]{"anonymized-oddcycle", "anonymized-shortestcycle", "anonymized-largestcycle"};
		
		/*
		String [] egoNetworkIds = new String[]{"0", "107", "348", "414", "686", "698", "1684", "1912", "3437", "3980"};
		
		for (String enId : egoNetworkIds) {
			System.out.println("");
			System.out.println("Ego network " + enId);
			System.out.println("");
			UndirectedGraph<String, DefaultEdge> original = null;
			if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
				original = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonimity-gaia\\down\\outputs\\disk\\RealSocNet\\ego-networks-anonymized-noatt\\ego-network-" + enId + "-original.txt");
			else
				original = GraphUtil.loadSNAPFormat("ego-network-" + enId + "-original.txt");
			System.out.println("|V|=" + original.vertexSet().size());
			System.out.println("|E|=" + original.edgeSet().size());
			FloydWarshallShortestPaths<String, DefaultEdge> floydOriginal = new FloydWarshallShortestPaths<>(original);
			double diameterOriginal = GraphUtil.computeDiameter(original, floydOriginal);
			System.out.println("D=" + diameterOriginal);
			double radiusOriginal = GraphUtil.computeRadius(original, floydOriginal);
			System.out.println("r=" + radiusOriginal);
			System.out.println("Anonymized versions");
			for (String versionName : anonymizedVersions) {
				System.out.println("\t" + versionName);
				UndirectedGraph<String, DefaultEdge> anonymized = null;
				if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).indexOf("win") >= 0)   // Running on Windows
					anonymized = GraphUtil.loadSNAPFormat("C:\\Users\\yunior.ramirez\\up_n_down\\graph-anonimity-gaia\\down\\outputs\\disk\\RealSocNet\\ego-networks-anonymized-noatt\\ego-network-" + enId + "-" + versionName + ".txt");
				else
					anonymized = GraphUtil.loadSNAPFormat("ego-network-" + enId + "-" + versionName + ".txt");
				System.out.println("\t\t|E'|=" + anonymized.edgeSet().size() + "(delta: " + (anonymized.edgeSet().size() - original.edgeSet().size()) + ")");
				FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymized = new FloydWarshallShortestPaths<>(anonymized);
				double diameterAnonymized = GraphUtil.computeDiameter(anonymized, floydAnonymized);
				System.out.println("\t\tD'=" + diameterAnonymized + "(delta: " + (diameterAnonymized - diameterOriginal) + ")");
				double radiusAnonymized = GraphUtil.computeRadius(anonymized, floydAnonymized);
				System.out.println("\t\tr'=" + radiusAnonymized + "(delta: " + (radiusAnonymized - radiusOriginal) + ")");
			}
		}//*/
		
		//System.out.println("Diameters/radiuses of anonymized versions");
		System.out.println("");
		//System.out.println("Method\tD Facebook\tD Panzarasa\tD URV\tr Facebook\trPanzarasa\rURV");
		//System.out.println("Method\tDD URV\tDr URV");
//		System.out.println("Method\tD|E| Facebook\tD|E| Panzarasa\tD|E| URV");
//		System.out.println("Method\tdeltaEffDiameter Facebook\tdeltaEffDiameter Panzarasa\tdeltaEffDiameter URV");
//		System.out.println("Method\tklDegreeDistr Facebook\tklDegreeDistr Panzarasa\tklDegreeDistr URV");
		System.out.println("Method\tratioGlobalCC Facebook\tratioGlobalCC Panzarasa\tratioGlobalCC URV");
//		System.out.println("Method\tratioLocalCC Facebook\tratioLocalCC Panzarasa\tratioLocalCC URV");
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
//		FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalFacebook = new FloydWarshallShortestPaths<>(originalFacebook);
//		System.out.println("Facebook: D = " + GraphUtil.computeDiameter(originalFacebook, floydOriginalFacebook) + ", r = " + GraphUtil.computeRadius(originalFacebook, floydOriginalFacebook) + ", bound = " + Statistics.removedEdgesUpperbound(originalFacebook, floydOriginalFacebook));
//		FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalPanzarasa = new FloydWarshallShortestPaths<>(originalPanzarasa);
//		System.out.println("Panzarasa: D = " + GraphUtil.computeDiameter(originalPanzarasa, floydOriginalPanzarasa) + ", r = " + GraphUtil.computeRadius(originalPanzarasa, floydOriginalPanzarasa) + ", bound = " + Statistics.removedEdgesUpperbound(originalPanzarasa, floydOriginalPanzarasa));
//		FloydWarshallShortestPaths<String, DefaultEdge> floydOriginalURV = new FloydWarshallShortestPaths<>(originalURV);
//		System.out.println("URV: D = " + GraphUtil.computeDiameter(originalURV, floydOriginalURV) + ", r = " + GraphUtil.computeRadius(originalURV, floydOriginalURV) + ", bound = " + Statistics.removedEdgesUpperbound(originalURV, floydOriginalURV));
		
		double glbCCFacebookOrig = GraphUtil.computeGlobalClusteringCoefficient(originalFacebook);
		System.out.println("Global CC original Facebook: " + glbCCFacebookOrig);
		double glbCCPanzarasaOrig = GraphUtil.computeGlobalClusteringCoefficient(originalPanzarasa);
		System.out.println("Global CC original Panzarasa: " + glbCCPanzarasaOrig);
		double glbCCURVOrig = GraphUtil.computeGlobalClusteringCoefficient(originalURV);
		System.out.println("Global CC original URV: " + glbCCURVOrig);
//		
//		double avlocCCFacebookOrig = GraphUtil.computeAvgLocalClusteringCoefficient(originalFacebook);
//		System.out.println("Avg local CC original Facebook: " + avlocCCFacebookOrig);
//		double avlocCCPanzarasaOrig = GraphUtil.computeAvgLocalClusteringCoefficient(originalPanzarasa);
//		System.out.println("Avg local CC original Panzarasa: " + avlocCCPanzarasaOrig);
//		double avlocCCURVOrig = GraphUtil.computeAvgLocalClusteringCoefficient(originalURV);
//		System.out.println("Avg local CC original URV: " + avlocCCURVOrig);
		
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
//			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymizedFacebook = new FloydWarshallShortestPaths<>(anonymizedFacebook);
//			double diameterAnonymizedFacebook = GraphUtil.computeDiameter(anonymizedFacebook, floydAnonymizedFacebook);
//			double radiusAnonymizedFacebook = GraphUtil.computeRadius(anonymizedFacebook, floydAnonymizedFacebook);
//			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymizedPanzarasa = new FloydWarshallShortestPaths<>(anonymizedPanzarasa);
//			double diameterAnonymizedPanzarasa = GraphUtil.computeDiameter(anonymizedPanzarasa, floydAnonymizedPanzarasa);
//			double radiusAnonymizedPanzarasa = GraphUtil.computeRadius(anonymizedPanzarasa, floydAnonymizedPanzarasa);
//			FloydWarshallShortestPaths<String, DefaultEdge> floydAnonymizedURV = new FloydWarshallShortestPaths<>(anonymizedURV);
//			double diameterAnonymizedURV = GraphUtil.computeDiameter(anonymizedURV, floydAnonymizedURV);
//			double radiusAnonymizedURV = GraphUtil.computeRadius(anonymizedURV, floydAnonymizedURV);
			
			//double deltaDiameterFacebook = GraphUtil.computeDiameter(anonymizedFacebook, floydAnonymizedFacebook) - GraphUtil.computeDiameter(originalFacebook, floydOriginalFacebook);
			//double deltaDiameterPanzarasa = GraphUtil.computeDiameter(anonymizedPanzarasa, floydAnonymizedPanzarasa) - GraphUtil.computeDiameter(originalPanzarasa, floydOriginalPanzarasa);
			//double deltaDiameterURV = GraphUtil.computeDiameter(anonymizedURV, floydAnonymizedURV) - GraphUtil.computeDiameter(originalURV, floydOriginalURV);
			
			//double deltaRadiusFacebook = GraphUtil.computeRadius(anonymizedFacebook, floydAnonymizedFacebook) - GraphUtil.computeRadius(originalFacebook, floydOriginalFacebook);
			//double deltaRadiusPanzarasa = GraphUtil.computeRadius(anonymizedPanzarasa, floydAnonymizedPanzarasa) - GraphUtil.computeRadius(originalPanzarasa, floydOriginalPanzarasa);			
			//double deltaRadiusURV = GraphUtil.computeRadius(anonymizedURV, floydAnonymizedURV) - GraphUtil.computeRadius(originalURV, floydOriginalURV);
			
//			double deltaEffDiameterFacebook = GraphUtil.computeEffectiveDiameter(anonymizedFacebook, floydAnonymizedFacebook) - GraphUtil.computeEffectiveDiameter(originalFacebook, floydOriginalFacebook);
//			double deltaEffDiameterPanzarasa = GraphUtil.computeEffectiveDiameter(anonymizedPanzarasa, floydAnonymizedPanzarasa) - GraphUtil.computeEffectiveDiameter(originalPanzarasa, floydOriginalPanzarasa);
//			double deltaEffDiameterURV = GraphUtil.computeEffectiveDiameter(anonymizedURV, floydAnonymizedURV) - GraphUtil.computeEffectiveDiameter(originalURV, floydOriginalURV);
			
//			double deltaEdgeCountFacebook = anonymizedFacebook.edgeSet().size() - originalFacebook.edgeSet().size();
//			double deltaEdgeCountPanzarasa = anonymizedPanzarasa.edgeSet().size() - originalPanzarasa.edgeSet().size();			
//			double deltaEdgeCountURV = anonymizedURV.edgeSet().size() - originalURV.edgeSet().size();
			
//			DistributionComputer distrComp = new DegreeDistributionComputer();
//			double klDegreeDistrFacebook = GraphUtil.computeDegreeDistributionCosineUtility1(originalFacebook, anonymizedFacebook);
//			double klDegreeDistrPanzarasa = GraphUtil.computeDegreeDistributionCosineUtility1(originalPanzarasa, anonymizedPanzarasa);
//			double klDegreeDistrURV = GraphUtil.computeDegreeDistributionCosineUtility1(originalURV, anonymizedURV);
			
			
			
			double glbCCFacebookAnon = GraphUtil.computeGlobalClusteringCoefficient(anonymizedFacebook);
			System.out.println("Global CC anonymized Facebook: " + glbCCFacebookAnon);
			double ratioGlobalCCFacebook = glbCCFacebookAnon / glbCCFacebookOrig;
			System.out.println("Facebook ratio: " + ratioGlobalCCFacebook);
			
			
			
			double glbCCPanzarasaAnon = GraphUtil.computeGlobalClusteringCoefficient(anonymizedPanzarasa);
			System.out.println("Global CC anonymized Panzarasa: " + glbCCPanzarasaAnon);
			double ratioGlobalCCPanzarasa = glbCCPanzarasaAnon / glbCCPanzarasaOrig;
			System.out.println("Panzarasa ratio: " + ratioGlobalCCPanzarasa);
			
			
			
			double glbCCURVAnon = GraphUtil.computeGlobalClusteringCoefficient(anonymizedURV);
			System.out.println("Global CC anonymized URV: " + glbCCURVAnon);
			double ratioGlobalCCURV = glbCCURVAnon / glbCCURVOrig;
			System.out.println("URV ratio: " + ratioGlobalCCURV);
			
			//System.out.println(versionName + "\t" + deltaDiameterPanzarasa + "\t" + deltaDiameterFacebook + "\t" + deltaRadiusPanzarasa + "\t" + deltaRadiusFacebook);
			//System.out.println(versionName + "\t" + deltaDiameterURV + "\t" + deltaRadiusURV);
//			System.out.println(versionName + "\t" + deltaEffDiameterFacebook + "\t" + deltaEffDiameterPanzarasa + "\t" + deltaEffDiameterURV);
//			System.out.println(versionName + "\t" + deltaEdgeCountFacebook + "\t" + deltaEdgeCountPanzarasa + "\t" + deltaEdgeCountURV);
//			System.out.println(versionName + "\t" + klDegreeDistrFacebook + "\t" + klDegreeDistrPanzarasa + "\t" + klDegreeDistrURV);
			System.out.println(versionName + "\t" + ratioGlobalCCFacebook + "\t" + ratioGlobalCCPanzarasa + "\t" + ratioGlobalCCURV);
			//System.out.println(versionName + "\t" + diameterAnonymizedFacebook + "\t" + diameterAnonymizedPanzarasa + "\t" + diameterAnonymizedURV + "\t" + radiusAnonymizedFacebook + "\t" + radiusAnonymizedPanzarasa + "\t" + radiusAnonymizedURV);
			
			
			
//			double avlocCCFacebookAnon = GraphUtil.computeAvgLocalClusteringCoefficient(anonymizedFacebook);
//			System.out.println("Avg local CC anonymized Facebook: " + avlocCCFacebookAnon);
//			double ratioAvLocCCFacebook = avlocCCFacebookAnon / avlocCCFacebookOrig;
//			System.out.println("Facebook ratio: " + ratioAvLocCCFacebook);
//			
//			
//			
//			double avlocCCPanzarasaAnon = GraphUtil.computeAvgLocalClusteringCoefficient(anonymizedPanzarasa);
//			System.out.println("Avg local CC anonymized Panzarasa: " + avlocCCPanzarasaAnon);
//			double ratioAvLocCCPanzarasa = avlocCCPanzarasaAnon / avlocCCPanzarasaOrig;
//			System.out.println("Panzarasa ratio: " + ratioAvLocCCPanzarasa);
//			
//			
//			
//			double avlocCCURVAnon = GraphUtil.computeAvgLocalClusteringCoefficient(anonymizedURV);
//			System.out.println("Avg local CC anonymized URV: " + avlocCCURVAnon);
//			double ratioAvLocCCURV = avlocCCURVAnon / avlocCCURVOrig;
//			System.out.println("URV ratio: " + ratioAvLocCCURV);
//			
//			System.out.println(versionName + "\t" + ratioAvLocCCFacebook + "\t" + ratioAvLocCCPanzarasa + "\t" + ratioAvLocCCURV);
		}
	}

}
