package anonymization;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class IncrementalKMatchSequenceAnonymizer extends KMatchAnonymizerUsingMETIS {
	
	protected static int commonK;
	protected static boolean firstSnapshotAnonymized = false;
	
	public static void setCommonK(int k) {
		commonK = k;
	}
	
	public static void anonymizeFirstSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomized, String uniqueIdFileName) {
		anonymizeGraph(graph, commonK, randomized, uniqueIdFileName);   // Will need some extra actions and maybe some modifications
		firstSnapshotAnonymized = true;
	}
	
	public static void anonymizeNewSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomized, String uniqueIdFileName) {
		if (firstSnapshotAnonymized) {
			// TODO: stub to implement
		}
		else 
			anonymizeFirstSnapshot(graph, randomized, uniqueIdFileName);
	}

}
