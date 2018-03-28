package org.jgrapht.alg;

import java.util.List;
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

public abstract class DominationSet {

    /**
     * Finds the minimum domination set by brute force.
     *
     * @param g an undirected graph to find the minimum domination set of
     *
     * @return integer the minimum domination set of g
     */
    public static <V, E> Set<V> findByBruteForceDominationSet(UndirectedGraph<V, E> g)
    {
    	VertexPowerSetIterator<V> iterator = new VertexPowerSetIterator<V>(g.vertexSet());
    	Set<V> vertexSet;
    	int min = Integer.MAX_VALUE;
    	Set<V> dominationSet = null;
    	long ini = System.currentTimeMillis();
    	long end;
    	while (iterator.hasNext()){
    		vertexSet = iterator.next();
    		//System.out.println("Analyzing "+vertexSet);
    		if (isDominationSet(new Subgraph<V, E, Graph<V,E>>(g, vertexSet))){
    			if (vertexSet.size() < min){
    				min = vertexSet.size();
    				dominationSet = vertexSet;
    			}
    		}
    		end = System.currentTimeMillis();
    		if (end - ini > 3600000){
    			System.out.println("Best result found so far "+dominationSet);
    			ini = end;
    		}
    	}
    	return dominationSet;
    }
	
    /**
     * Finds the number of colors required for a greedy coloring of the graph.
     *
     * @param g an undirected graph to find the chromatic number of
     *
     * @return integer the approximate chromatic number from the greedy
     * algorithm
     */
    public static <V, E> boolean isDominationSet(Subgraph<V, E, Graph<V, E>> vertexSet)
    {
    	Graph<V, E> graphBase = vertexSet.getBase();
    	Set<E> neighborEdges;
    	for (V vertex : graphBase.vertexSet()){
    		if (vertexSet.containsVertex(vertex)) continue;
    		neighborEdges = graphBase.edgesOf(vertex);
    		boolean ok = false; 
			for (V vertexOnDominationSet : vertexSet.vertexSet()){
				if (graphBase.containsEdge(vertexOnDominationSet, vertex)){
						ok = true;
						break;
				}
			}
    		if (!ok) return false;
    	}
    	return true;
    }
    
}
