package attacks;

import java.util.TimerTask;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public abstract class SybilAttackSimulator {
	
	public static final String NEW_LINE = System.getProperty("line.separator");

	public abstract void simulateAttackerSubgraphCreation(UndirectedGraph<String, DefaultEdge> graph, int attackerCount, int victimCount);
	public abstract double successProbability(int attackerCount, int victimCount, UndirectedGraph<String, DefaultEdge> graph, UndirectedGraph<String, DefaultEdge> originalGraph); 
	
	protected boolean limitRunningTime = false;
	protected int timeCapSubgraphSearchMins = 60;
	protected volatile boolean subgraphSearchOvertimed = false;
	
	protected class StopSubgraphSearchTask extends TimerTask {
        public void run() {
        	if (limitRunningTime) 
        		subgraphSearchOvertimed = true;
        }
    }
	
	public void setRunningTimeCap(int minsSubgraphSearch) {
		limitRunningTime = true;
		timeCapSubgraphSearchMins = minsSubgraphSearch;
	}
	
	// Will take the default time cap
	public void setRunningTimeCap() {
		limitRunningTime = true;
	}

}
