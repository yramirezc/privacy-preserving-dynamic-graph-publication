package anonymization;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class IncrementalKMatchSequenceAnonymizer {
	
	protected int commonK = 2;
	protected boolean firstSnapshotAnonymized = false;
	
	public IncrementalKMatchSequenceAnonymizer(int k) {
		super();
		setCommonK(k);
	}
	
	public void setCommonK(int k) {
		commonK = k;
	}
	
	public void anonymizeFirstSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomized, String uniqueIdFileName) {
		// Based on anonymizeGraph(graph, commonK, randomized, uniqueIdFileName);
		// Will need some extra actions and maybe some modifications
		// The static version adds dummy vertices, the dynamic one will not
		firstSnapshotAnonymized = true;
	}
	
	public void anonymizeNewSnapshot(UndirectedGraph<String, DefaultEdge> graph, boolean randomized, String uniqueIdFileName) {
		if (firstSnapshotAnonymized) {
			
		}
		else 
			anonymizeFirstSnapshot(graph, randomized, uniqueIdFileName);
	}
	
}
