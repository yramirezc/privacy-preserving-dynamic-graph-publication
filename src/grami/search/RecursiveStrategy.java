/**
 * created May 16, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package grami.search;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import grami.algorithm_interface.Algorithm;
import grami.dataStructures.DFSCode;
import grami.dataStructures.HPListGraph;
import grami.dataStructures.StaticData;
import grami.utilities.DfscodesCache;
import grami.utilities.Settings;


//import de.parsemis.utils.Frequented;

/**
 * This class represents the local recursive strategy.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class RecursiveStrategy<NodeType, EdgeType> implements
		Strategy<NodeType, EdgeType> {
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * Added by Yunior Ramírez-Cruz, 07/06/2019
	 * 
	 * Put a cap on the running time for the entire search process.
	 * After this time elapses, the search will be stopped ASAP
	 * 
	 */
	
	class StopFullSearchTask extends TimerTask {
        public void run() {
        	if (Settings.globalRunningTimeLimited)
        		declareSearchOvertimed();
        	else if (Settings.verbose)
        		System.out.println(Settings.globalRunningTimeCapMins + " minutes passed but search will not be declared overtimed");
        }
    }
	
	private volatile boolean searchOvertimed = false;
	private Timer timer;
	
	public void declareSearchOvertimed() {
		searchOvertimed = true;
		if (Settings.verbose)
			System.out.println("Declared overtimed");
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Extender<NodeType, EdgeType> extender;

	private Collection<HPListGraph<NodeType, EdgeType>> ret;
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.Strategy#search(de.parsemis.miner.Algorithm,
	 *      int)
	 */
	public Collection<HPListGraph<NodeType, EdgeType>> search(  //INITIAL NODES SEARCH
			final Algorithm<NodeType, EdgeType> algo,int freqThresh) {
		ret = new ArrayList<HPListGraph<NodeType, EdgeType>>();
		
		extender = algo.getExtender(freqThresh);
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// YR (07/06/2019)
		searchOvertimed = false;
		timer = new Timer(true);
		timer.schedule(new StopFullSearchTask(), Settings.globalRunningTimeCapMins * 60 * 1000);   // Declare search overtimed after Settings.globalRunningTimeCapMins minutes
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo.initialNodes(); !searchOvertimed && it.hasNext();) {   // YR: also stop looping if overtimed
						
			final SearchLatticeNode<NodeType, EdgeType> code = it.next();
			final long time = System.currentTimeMillis();
//			if (VERBOSE) {
//				out.print("doing seed " + code + " ...");
//			}
//			if (VVERBOSE) {
//				out.println();
//			}
			
			search(code);
			it.remove();
			
			//remove frequent edge labels that are already processed - test test test before approval
			double edgeLabel = Double.parseDouble(code.getHPlistGraph().getEdgeLabel(code.getHPlistGraph().getEdge(0, 1)).toString());
			int node1Label = Integer.parseInt(code.getHPlistGraph().getNodeLabel(0).toString());
			int node2Label = Integer.parseInt(code.getHPlistGraph().getNodeLabel(1).toString());
			String signature;
			if(node1Label<node2Label)
				signature = node1Label+"_"+edgeLabel+"_"+node2Label;
			else
				signature = node2Label+"_"+edgeLabel+"_"+node1Label;
			StaticData.hashedEdges.remove(signature);

//			if (VERBOSE) {
//				out.println("\tdone (" + (System.currentTimeMillis() - time)
//						+ " ms)");
			//}
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// YR (07/06/2019)
		timer.cancel();
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void search(final SearchLatticeNode<NodeType, EdgeType> node) {  //RECURSIVE NODES SEARCH
		
		if (!searchOvertimed) {
			
			//System.out.println("Getting Children");
			final Collection<SearchLatticeNode<NodeType, EdgeType>> tmp = extender
					.getChildren(node);
			//System.out.println("finished Getting Children");
			//System.out.println(node.getLevel());
			for (final SearchLatticeNode<NodeType, EdgeType> child : tmp) {
//				if (VVVERBOSE) {
//					out.println("doing " + child);
//				}
				//System.out.println("   branching into: "+child);
				//System.out.println("   ---------------------");
				search(child);
				
				
			}
			
		}

//		if (VVERBOSE) {
//			out.println("node " + node + " done. Store: " + node.store()
//					+ " children " + tmp.size() + " freq "
//					+ ((Frequented) node).frequency());
//		}
		if (node.store()) {
			node.store(ret);
		} else {
			node.release();
		}

		node.finalizeIt();
	}

}
