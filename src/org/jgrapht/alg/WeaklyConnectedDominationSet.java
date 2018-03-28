package org.jgrapht.alg;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.util.VertexPowerSetIterator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Subgraph;

/**
 * Bla bla bla
 *
 * @author Rolando Trujillo-Rasua
 * @since Feb 15, 2012
 */

public abstract class WeaklyConnectedDominationSet {

    /**
     * Finds the minimum domination set by brute force.
     *
     * @param g an undirected graph to find the minimum domination set of
     *
     * @return integer the minimum domination set of g
     */
    public static <V, E> Set<V> findByBruteForceWeaklyConnectedDominationSet(UndirectedGraph<V, E> g)
    {
    	//VertexPowerSetIterator<V> iterator = new VertexPowerSetIterator<V>(g.vertexSet());
    	Set<V> vertexSet;
    	int min = Integer.MAX_VALUE;
    	Set<V> dominationSet = null;
    	long end;
    	Random r = new Random();
    	long cont = 0;
    	long ini = System.currentTimeMillis();
    	while (true){
    		//vertexSet = iterator.next();
    		vertexSet = new HashSet<V>();
    		for (V vert : g.vertexSet()){
    			if (r.nextBoolean()) vertexSet.add(vert);
        		if (isWeaklyConnectedDominationSet(new Subgraph<V, E, Graph<V,E>>(g, vertexSet))){
        			if (vertexSet.size() < min){
        				min = vertexSet.size();
        				dominationSet = vertexSet;
        			}
        			break;
        		}
    		}
    		//System.out.println("Analyzing "+vertexSet);
			cont++;
    		end = System.currentTimeMillis();
    		if (end - ini > 3600000){
    			System.out.println("Best result found so far "+dominationSet+" cont = "+cont);
    			//System.out.println("Expected time to finish "+((end-ini)/(1000*60*60*24))*(Math.pow(2, 64)-cont));
    			ini = end;
    		}
    		if (cont > Math.pow(2, 64))
    			return dominationSet;
    	}
    }
	
    /**
     * Finds the number of colors required for a greedy coloring of the graph.
     *
     * @param g an undirected graph to find the chromatic number of
     *
     * @return integer the approximate chromatic number from the greedy
     * algorithm
     */
    public static <V, E> boolean isWeaklyConnectedDominationSet(Subgraph<V, E, Graph<V, E>> vertexSet)
    {
    	return DominationSet.isDominationSet(vertexSet) && WeaklyConnectedSet.isWeaklyConnectedSet(vertexSet);
    }
    
}
