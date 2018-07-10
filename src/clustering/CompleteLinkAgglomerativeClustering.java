package clustering;

import java.util.HashSet;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import net.vivin.GenericTree;
import net.vivin.GenericTreeNode;

public class CompleteLinkAgglomerativeClustering extends HierarchicalClustering {
	
	public CompleteLinkAgglomerativeClustering() {
		super();
	}
	
	@Override
	protected void createHierarchy(UndirectedGraph<String, DefaultEdge> graph, int defaultClusterSize, int extraElemClusterCount) {
		this.defaultClusterSize = defaultClusterSize;
		this.extraElemClusterCount = extraElemClusterCount;
		this.currentExtraCount = 0;
		this.createHierarchyAux(graph);
	}
	
	@Override
	protected void createHierarchy(UndirectedGraph<String, DefaultEdge> graph) {
		this.defaultClusterSize = -1;
		this.extraElemClusterCount = -1;
		this.currentExtraCount = 0;
		this.createHierarchyAux(graph);
	}
	
	protected void createHierarchyAux(UndirectedGraph<String, DefaultEdge> graph) {
		hierarchy = new GenericTree<>();
		GenericTreeNode<Set<String>> root = new GenericTreeNode<>(graph.vertexSet());
		for (String vert : graph.vertexSet()) {
			Set<String> leafInfo = new HashSet<String>();
			leafInfo.add(vert);
			root.addChild(new GenericTreeNode<Set<String>>(leafInfo));
		}
		hierarchy.setRoot(root);
		while (createLink(graph, true));
		while (createLink(graph, false));
	}
	
	protected boolean createLink(UndirectedGraph<String, DefaultEdge> graph, boolean keepTreeBalanced) {
		
		boolean smallClustersRemaining = false;
		for (GenericTreeNode<Set<String>> inmediateChild : hierarchy.getRoot().getChildren()) {
			if (inmediateChild.getData().size() < defaultClusterSize) {
				smallClustersRemaining = true;
				break;
			}
		}
		
		if (smallClustersRemaining) {
			
			int inmediateClusterCount = hierarchy.getRoot().getChildren().size();
			double maxSimilarity = 0d;
			int maxSimClust1 = -1, maxSimClust2 = -1;
			
			for (int i = 0; i < inmediateClusterCount - 1; i++)
				for (int j = i + 1; j < inmediateClusterCount; j++) {
					
					if (!keepTreeBalanced || canLink(i,j)) {
						double similarity = ClusterSimilarityCalculator.contTableCompleteLinkClusterSimilarity(graph, hierarchy.getRoot().getChildAt(i).getData(), hierarchy.getRoot().getChildAt(j).getData());
						if (similarity > maxSimilarity) {
							maxSimilarity = similarity;
							maxSimClust1 = i;
							maxSimClust2 = j;
						}
					}
				}
			
			if (maxSimClust1 != -1 && maxSimClust2 != -1) {
				Set<String> newNodeInfo = new HashSet<>(hierarchy.getRoot().getChildAt(maxSimClust1).getData());
				newNodeInfo.addAll(hierarchy.getRoot().getChildAt(maxSimClust2).getData());
				if (keepTreeBalanced)
					updateLinkabilityInfo(newNodeInfo);
				GenericTreeNode<Set<String>> subTreeRoot = new GenericTreeNode<>(newNodeInfo);
				subTreeRoot.addChild(hierarchy.getRoot().getChildAt(maxSimClust1));
				subTreeRoot.addChild(hierarchy.getRoot().getChildAt(maxSimClust2));
				hierarchy.getRoot().getChildren().set(maxSimClust1, subTreeRoot);
				hierarchy.getRoot().getChildren().remove(maxSimClust2);
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}
	
	boolean canLink(int i, int j) {
		if (defaultClusterSize == -1)
			return true;
		/* The following is true because in this implementation clusters do not overlap.
		 * To allow for overlapping, the union should be actually computed and its size taken.
		 */ 
		int unionSize = hierarchy.getRoot().getChildAt(i).getData().size() + hierarchy.getRoot().getChildAt(j).getData().size();
		if (unionSize <= defaultClusterSize)
			return true;
		if (unionSize == defaultClusterSize + 1 && currentExtraCount < extraElemClusterCount)
			return true;
		return false;
	}
	
	void updateLinkabilityInfo(Set<String> newNodeInfo) {
		if (defaultClusterSize != -1 && newNodeInfo.size() == defaultClusterSize + 1)
			currentExtraCount++;
	}

}
