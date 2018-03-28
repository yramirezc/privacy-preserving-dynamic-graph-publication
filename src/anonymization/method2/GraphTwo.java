package anonymization.method2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import anonymization.BaseGraph;

//import org.apache.log4j.Logger;



public class GraphTwo extends BaseGraph {
	
//	private Logger log = Logger.getLogger(GraphTwo.class);
	
	
	private VertexTwo[] vertices;
	
	public GraphTwo(int vexnum) {
		super(vexnum);
		vertices = new VertexTwo[vexnum];
		for(int i=0;i<vexnum;i++){
			vertices[i] = new VertexTwo(""+i);
		}
	}
	/**
	 * 
	 * broad first search=bfs
	 */
	@Override
	public void bfs(){
		for(int i=0;i<this.vexnum;i++){
			bfs(i);
		}
	}
	/**
	 * 
	 */
	@Override
	public void bfs(int start){
		this.refreshVisited();
		
		VertexTwo vertex = this.vertices[start];
		vertex.setVisited(true);
		int[] temp = new int[1];
		temp[0] = start ;
		
		int level = 1 ;
		int[] result = this.getAllNextVertexs(temp,vertex,level);
		
		while(result!=null && result.length>0){
			level++;
			result = this.getAllNextVertexs(result,vertex,level);
		}
		vertex.initK();
	}
	
	/**
	 * set Vertex is not visited
	 */
	private void refreshVisited(){
		for(int i=0;i<this.vexnum;i++){
			this.vertices[i].setVisited(false);
		}
	}
	/**
	 * get the neighbors
	 * @param vertexs
	 * @param vertex
	 * @param level
	 * @return
	 */
	private int[] getAllNextVertexs(int[] vertexs,VertexTwo vertex,int level){
		int[] result = null;
		String temp = "";
		String str = "";
		String[] strs = null;
		for(int i=0;i<vertexs.length;i++){
			temp = this.getNextVertexs(vertexs[i],vertex,level);
			if(!"".equals(temp)){
				str +=temp;
			}
		}
		if(!"".equals(str)){
			
			str = str.substring(0,str.length()-1);
			
			if(str.contains(",")){
				strs = str.split(",");
			}else{
				result = new int[1];
				result[0] = Integer.parseInt(str);
			}
		}
		if(strs!=null){
			result = new int[strs.length];
			for(int i=0;i<result.length;i++){
				result[i] = Integer.parseInt(strs[i]);
			}
		}
		
		return result;
	}
	
	
	/**
	 * get all the neighbors
	 * @param index
	 * @param vertext
	 * @param level
	 * @return
	 */
	private String getNextVertexs(int index,VertexTwo vertext,int level){
		String str = "";
		
		for(int i=0;i<this.vexnum;i++){
			if(index==i){
				continue;
			}
		
			if(this.arcs[index][i]==1 && this.vertices[i].isVisited()==false){
				this.vertices[i].setVisited(true);
				vertext.getAnonymity().put(String.valueOf(i), new TreeNode(i,index,level));
				str +=i+",";
			}
		}
		return str ;
	}
	/**
	 * compute the value of k in k-anonymity for each vertex
	 * @return 字符串-用于输出
	 */
	@Override
	public String getVertexK_anonymity(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<this.vexnum;i++){
			sb.append(this.vertices[i].getK_anonymity());
		}
		return sb.toString();
	}
	/**
	 * CPA, create a smallest cycle
	 */
	@Override
	public void getImprovedGraph(){
		this.bfs();
		while(this.isOne_anonymity()){
			for(int i=0;i<this.vexnum;i++){
				if(this.vertices[i].getK()==1){
					VertexTwo vertex = this.vertices[i];
					int kLength = vertex.getkLength();
					Set<String> set = vertex.getAnonymity().keySet();       
					
					for(String key : set){
						if(vertex.getAnonymity().get(key).getLength()==kLength){
							//find the vertex which make it 1-anonymity
							TreeNode treeNode = vertex.getAnonymity().get(key);
							List<TreeNode> list = new ArrayList<TreeNode>();
							
							//store all the unique distance into list,the distance is from big to small
							while(vertex.isOnlyLengthNode(treeNode)){
				//				System.out.println("TreeNode:"+ treeNode.toString());
								list.add(treeNode);
								treeNode = vertex.getAnonymity().get(String.valueOf(treeNode.getpId()));
							} 
							int id = treeNode.getId(); //w,the predecessor of the nearest eye-catching node in the eccentricity path 
							//for(int j=0;j<list.size();j++){
							//	this.addEdge(pid,list.get(j).getId());
							//}
							
							//check the number of nodes in list
							if(list.size()%2==0){//if cycle is odd, connect w and y
								this.addEdge(id,list.get(0).getId());
							}else{// connect predecessor of w and y
								this.addEdge(vertex.getAnonymity().get(String.valueOf(id)).getpId(),list.get(0).getId());
							}
							this.bfs(); 
							break;
							
						}
					}
					this.bfs(); 
//					System.out.println("Method2，Now the diameter of the graph is " + this.getDiameter());
					break;
				}
			}
			
		}
		
	//	System.out.println("==============Method2====================\r\n");
	//	System.out.println("AddedEdgeNum:"+ this.addedEdgeNum);
	//	System.out.println("After Method2:\r\n" + this.toString());
	//	System.out.println(this.getVertexK_anonymity());
	//	System.out.println("Method2,The improved graph is ("+this.getMinK()+",l)-anonymity");
	}
	/**
	 * This method I did not use finally, it is not CPA or EPA
	 * 
	 * 
	 */
	@Override
	public void getImprovedGraph_two(){
		this.bfs();
		while(this.isOne_anonymity()){
			for(int i=0;i<this.vexnum;i++){
				if(this.vertices[i].getK()==1){
					VertexTwo vertex = this.vertices[i];
					int kLength = vertex.getkLength();
					Set<String> set = vertex.getAnonymity().keySet();         
					
					for(String str : set){
						if(vertex.getAnonymity().get(str).getLength()==kLength){
							//find out the node makes it 1-anonymity
							TreeNode treeNode = vertex.getAnonymity().get(str);
							if(treeNode.getLength()%2==0){
								this.addEdge(treeNode.getId(), Integer.parseInt(vertex.getId()));
								TreeNode tempTreeNode = treeNode;
								int length = treeNode.getLength();
								while(length>1){
									tempTreeNode = vertex.getAnonymity().get(String.valueOf(tempTreeNode.getpId()));
									length = tempTreeNode.getLength();
								}
								this.addEdge(tempTreeNode.getId(),treeNode.getId());
							}
							this.bfs();  
							break;
						}
					}
					this.bfs();
					break;
				}
			}
			
		}
		
		//System.out.println("==============Method2====================\r\n");
	//	System.out.println("AddedEdgeNum:"+ this.addedEdgeNum);
	//	System.out.println("After Method2:\r\n" + this.toString());
	//	System.out.println(this.getVertexK_anonymity());
	//	System.out.println("Method2,The improved graph is ("+this.getMinK()+",l)-anonymity");
		
	}
	/**
	 * 
	 * EPA
	 */
	@Override
	public void getImprovedGraph_one(){
		this.bfs();
		while(this.isOne_anonymity()){
			for(int i=0;i<this.vexnum;i++){
				if(this.vertices[i].getK()==1){
					VertexTwo vertex = this.vertices[i];
					
					Set<String> set = vertex.getAnonymity().keySet();        
					
					TreeNode pTreeNode = null;
					TreeNode nTreeNode = null;
					for(String index : set){
						nTreeNode = vertex.getAnonymity().get(index);
						if(pTreeNode == null){
							pTreeNode = nTreeNode ;
						}
						if(nTreeNode.getLength()>pTreeNode.getLength()){
							pTreeNode = nTreeNode ;
						}
					}
					//pTreeNode is the farthest node ,y
					if(pTreeNode.getLength()%2==0){//if the distance between v and y is even,then the size of the cycle is odd 
						this.addEdge(pTreeNode.getId(), Integer.parseInt(vertex.getId()));
					}else{//if the distance is odd, then connect descendant of v ,named v', and y.
						TreeNode tempTreeNode = pTreeNode;
						int length = pTreeNode.getLength();
						while(length>1){
							tempTreeNode = vertex.getAnonymity().get(String.valueOf(tempTreeNode.getpId()));
							length = tempTreeNode.getLength();
						}
						this.addEdge(tempTreeNode.getId(),pTreeNode.getId());//connect v' and y
					}
					
					this.bfs();
					break;
				}
			}
			
		}
		
		//System.out.println("==============Method2====================\r\n");
	//	System.out.println("AddedEdgeNum:"+ this.addedEdgeNum);
	//	System.out.println("After Method2:\r\n" + this.toString());
	//	System.out.println(this.getVertexK_anonymity());
	//	System.out.println("Method2,The improved graph is ("+this.getMinK()+",l)-anonymity");
		
	}
	
	
	//for every vertex whether is 1-anonymity
	public boolean isOne_anonymity(){
		for(int i=0;i<this.vexnum;i++){
			if(this.vertices[i].getK()==1){
				return true;
			}
		}
		return false;
	}
	/**
	 * get minimum k
	 * then the graph is k-anonymity
	 */
	@Override
	public int getMinK(){
		int temp = this.vexnum;
		for(int i=0;i<this.vexnum;i++){
			if(this.vertices[i].getK()<temp){
				temp = this.vertices[i].getK();
			}
		}
		return temp;
	}
	/**
	 * get diameter
	 * @return
	 */
	@Override
	public int getDiameter(){
		int diameter = 0 ;
		int temp = 0 ;
		for(int i=0;i<this.vexnum;i++){
			temp = this.vertices[i].getEccentricity();
			if(diameter<temp){
				diameter = temp ;
			}
		}
		return diameter ;
	}
	/**
	 * 
	 * @return
	 */
	public String getVertexAnonymityTreeNode(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<this.vexnum;i++){
			sb.append(this.vertices[i].getAllTreeNodeStr());
		}
		return sb.toString();
	}
	
	
	public VertexTwo[] getVertices() {
		return vertices;
	}
	public void setVertices(VertexTwo[] vertices) {
		this.vertices = vertices;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Vertiecs:{\r\n");
		for(int i=0;i<this.vexnum;i++){
			sb.append(this.vertices[i].toString()+"\r\n");
		}
		sb.append("}\r\n");
		sb.append("arcs:\r\n");
		for(int i=0;i<this.vexnum;i++){
			sb.append("[");
			for(int j=0;j<this.vexnum;j++){
				if(i==j){
					sb.append(" # ");
				}else{
					sb.append(" "+arcs[i][j]+" ");
				}
			}
			sb.append("]\r\n");
		}
		return sb.toString();
	}
}
