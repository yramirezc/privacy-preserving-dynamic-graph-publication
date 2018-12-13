package clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import net.vivin.GenericTree;
import net.vivin.GenericTreeNode;

public abstract class HierarchicalClustering implements VertexClusterer {
	 
	protected int defaultClusterSize;
	protected int extraElemClusterCount;
	protected int currentExtraCount;
	protected GenericTree<Set<String>> hierarchy;
	
	public HierarchicalClustering() {
	}

	@Override
	public Set<Set<String>> getPartitionalClustering(UndirectedGraph<String, DefaultEdge> graph) {
		createHierarchy(graph);
		return preRootLevelClusters();
	}
	
	@Override
	public Set<Set<String>> getPartitionalClustering(UndirectedGraph<String, DefaultEdge> graph, int defaultClusterSize, int extraElemClusterCount) {
		createHierarchy(graph, defaultClusterSize, extraElemClusterCount);
		return preRootLevelClusters();
	}
	
	Set<Set<String>> preRootLevelClusters() {
		Set<Set<String>> clusters = new HashSet<>();
		List<Integer> sizes = new ArrayList<>();
		for (GenericTreeNode<Set<String>> inmediateChild : hierarchy.getRoot().getChildren()) {
			clusters.add(inmediateChild.getData());
			sizes.add(inmediateChild.getData().size());
		}
		System.out.println(sizes.toString());
		return clusters;
	}
	
	protected abstract void createHierarchy(UndirectedGraph<String, DefaultEdge> graph);
	
	protected abstract void createHierarchy(UndirectedGraph<String, DefaultEdge> graph, int defaultClusterSize, int extraElemClusterCount);

}
