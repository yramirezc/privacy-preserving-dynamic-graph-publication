package org.jgrapht.alg;

import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.util.VertexPowerSetIterator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;

/**
 * Bla bla bla
 *
 * @author Rolando Trujillo-Rasua
 * @since Feb 15, 2012
 */

public abstract class WeaklyConnectedSet {

    /**
     * Finds the number of colors required for a greedy coloring of the graph.
     *
     * @param g an undirected graph to find the chromatic number of
     *
     * @return integer the approximate chromatic number from the greedy
     * algorithm
     */
    public static <V, E> boolean isWeaklyConnectedSet(Subgraph<V, E, Graph<V, E>> vertexSet)
    {
    	Graph<V, E> graphBase = vertexSet.getBase();
    	Set<E> neighborEdges;
    	SimpleGraph<V, E> tmp = new SimpleGraph<V, E>(graphBase.getEdgeFactory());
		for (V vertex : graphBase.vertexSet()){
			tmp.addVertex(vertex);
		}
		for (V v1 : vertexSet.vertexSet()){
    		for (V v2 : graphBase.vertexSet()){
    			if (graphBase.containsEdge(v1, v2))
    				tmp.addEdge(v1, v2);
    		}
		}
		ConnectivityInspector<V, E> inspector = new ConnectivityInspector<V, E>(tmp);
    	return inspector.isGraphConnected();
    }
    
}
