package anonymization;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
import util.GraphUtil;

//import org.apache.log4j.Logger;



/**
 * base graph
 * realize some common method
 */
public abstract class BaseGraph {
	
//	private Logger log = Logger.getLogger(BaseGraph.class);
	
	// If two vertices are connected then it is 1, else it is 0.
	protected int[][] arcs;  
	// number of vertices
	protected int vexnum;
	// number of added edges
	protected int numberOfEdges = 0;

	
	public int getNumberOfEdges() {
		return numberOfEdges;
	}
	
	public BaseGraph(int vexnum) {
		
		this.vexnum = vexnum;
		arcs = new int[this.vexnum][this.vexnum];
		for (int i = 0; i < vexnum; i++) {
			for (int j = 0; j < vexnum; j++) {
				arcs[i][j] = 0;
			}
		}
	//	log.info(" BaseGraph initialization finishes.");
	}
	/**
	 * add edge to base graph
	 * @param i
	 * @param j
	 */
	public void addEdge(int i, int j) {
		if (i == j){
			return;         			    
		}else{
			if (arcs[i][j] == 1){
				if (arcs[j][i] == 0) throw new RuntimeException("The graph should be undirected");
				else return;//nothing to be added
			}
			else{
				if (arcs[j][i] == 1) throw new RuntimeException("The graph should be undirected");
				else {
					arcs[i][j] = 1;					
					arcs[j][i] = 1;
					numberOfEdges++;
				}
			}
		}
	}
	
	/*public void addRandomEdge() throws NoSuchAlgorithmException{
		int count = GraphUtil.getRandomNum(this.vexnum*(this.vexnum-1)/2);
		for(int i=0;i<count;i++){
			this.addEdge(GraphUtil.getRandomNum(this.vexnum-1), GraphUtil.getRandomNum(this.vexnum-1));
		}
	}*/
	
	/**
	 * deal with isolated vertex
	 * @throws NoSuchAlgorithmException 
	 */
	public void handleIndependentPoint() throws NoSuchAlgorithmException{
		int temp = 0 ;
		for(int i=0;i<this.vexnum;i++){
			if(this.checkIsIndependentPoint(i)){		
				temp = i ;
				while(i==temp){                        	
					temp = GraphUtil.getRandomNum(this.vexnum-1);//
					this.addEdge(i, temp);
				}
			}
		}

	}
	/**
	 * judge whether it is isolated vertex
	 * 
	 * @param index
	 * @return
	 */
	private boolean checkIsIndependentPoint(int index){   
		int result = 0 ;
		for(int i=0;i<this.vexnum;i++){
			if(i==index){				
				continue;
			}
			result += this.arcs[index][i];
		}
		if(result>0){					//not isolated vertex
			return false;
		}
		return true;
	}
	
	
	
	/**
	 * 
	 * get array of end-vertices
	 * @return
	 */
	public int[] getDegreeOfOnePoint(){
		String str = "";
		for(int i=0;i<this.vexnum;i++){
			int temp = 0 ;
			for(int j=0;j<this.vexnum;j++){
				if(i==j){
					continue;
				}
				temp +=this.arcs[i][j];
			}
			if(temp==1){    //degree=1
				str +=i+",";
			}
		}
		if(!"".equals(str)){
			str = str.substring(0,str.length()-1);
			int[] result = null;
			
			if(str.contains(",")){					//more than one end-vertex
				String[] strs = str.split(",");
				result = new int[strs.length];
				for(int i=0;i<strs.length;i++){
					result[i] = Integer.parseInt(strs[i]);
				}
			}else{                                 //only one end-vertex
				result = new int[1];
				result[0]=Integer.parseInt(str);
			}
			return result ;
		}else{
			return null;
		}
	}
	/**
	 * Check whether there is end-vertex
	 * 
	 * @return boolean
	 */
	public boolean checkIsDegreeOfOnePoint(){
		for(int i=0;i<this.vexnum;i++){
			int temp = 0 ;
			for(int j=0;j<this.vexnum;j++){
				if(i==j){
					continue;
				}
				temp +=this.arcs[i][j];
			}
			if(temp==1){               //if degree = 1
//				log.info("there exist end-vertex");
//				log.info(this.toString());
				return true ;
			}
		}
		return false ;
	}
	
	/**
	 * get all the neighbor of one vertex
	 * @param index
	 * @return array of neighbor
	 */
	public int[] getLinkedVertexs(int index){
		String str = "";
		
		for(int i=0;i<this.vexnum;i++){
			if(index==i){
				continue;
			}
			//is neighbor but not visited
			if(this.arcs[index][i]==1){
				str +=i+",";
			}
		}
		int[] nexVexs = null;
		
		if(!"".equals(str)){
			str = str.substring(0,str.length()-1);
			if(str.contains(",")){
				String[] strs = str.split(",");
				nexVexs = new int[strs.length];
				for(int i=0 ;i<strs.length;i++){
					nexVexs[i] = Integer.parseInt(strs[i]);
				}
			}else{
				nexVexs = new int[1];
				nexVexs[0] = Integer.parseInt(str);
			}
		}
		return nexVexs;
	}
	
	
	public void getImprovedGraph_one(){
		System.out.println("ImprovedGraph_one ");
	}
		public void getImprovedGraph_two(){
		System.out.println("ImprovedGraph_two");
	}
	
	//=====abstract method=====================================================//
	/**
	 * 
	 * broad first search=bfs
	 */
	public abstract void bfs();
	
	/**
	 * 
	 * @param index
	 */
	public abstract void bfs(int index);
	
	/**
	 * compute all the distances to one vertex
	 * @return 
	 */
	public abstract String getVertexK_anonymity();
	
	/**
	 * get diameter of graph
	 * @return
	 */
	public abstract int getDiameter();
	/**
	 * anonymize graph
	 */
	public abstract void getImprovedGraph();
	/**
	 * get the smallest k->  k-anonymity
	 * 
	 * @return
	 */
	public abstract int getMinK();
	
	//=========getter and setter methods========================================//
	
	public int[][] getArcs() {
		return arcs;
	}
	public void setArcs(int[][] arcs) {
		this.arcs = arcs;
	}
	public int getVexnum() {
		return vexnum;
	}
	public void setVexnum(int vexnum) {
		this.vexnum = vexnum;
	}

	public void addRandomEdges(int toAdd) {
		SecureRandom random = new SecureRandom();
		if (numberOfEdges+toAdd > getVexnum()*(getVexnum()-1)/2){
			throw new RuntimeException("no sufficient edges");
		}
		int counter = 0;
		while (counter < toAdd){
			int v1 = random.nextInt(getVexnum());
			int v2 = random.nextInt(getVexnum());
			if (v1== v2 || arcs[v1][v2] == 1) continue;
			arcs[v1][v2] = 1;
			arcs[v2][v1] = 1;
			counter++;
		}
	}
	
	
}
