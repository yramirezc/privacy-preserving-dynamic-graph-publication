package anonymization.method2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VertexTwo {
	
	//id
	private String id;
	//k-anonymity
	private int k = 0 ;
	//the distance that make it k-anonymity
	private int kLength ;
	//whether is visited
	private boolean visited = false ;
	//the distance from this node to other nodes, map<id,treeNode>  
	private Map<String,TreeNode> anonymity = new HashMap<String,TreeNode>();

	
	public VertexTwo(){
	}
	public VertexTwo(String id) {
		super();
		
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getK() {
		return k;
	}
	public void setK(int k) {
		this.k = k;
	}
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public Map<String, TreeNode> getAnonymity() {
		return anonymity;
	}
	public void setAnonymity(Map<String, TreeNode> anonymity) {
		this.anonymity = anonymity;
	}
	public int getkLength() {
		return kLength;
	}
	public void setkLength(int kLength) {
		this.kLength = kLength;
	}
	@Override
	public String toString() {
		return "Vertex [ id=" + id + ", k=" + k
				+ ", visited=" + visited + "]";
	}
	//return k-anonymity  string
	public String getK_anonymity(){
		StringBuilder sb = new StringBuilder();
		sb.append("=========================================\r\n");
		sb.append(this.id+":"+this.k+"-anonymity");
		if(this.k==1){
			sb.append(":kLength="+this.kLength);
		}
		sb.append(":\r\n");
		Set<String> temp = this.anonymity.keySet();
		String str = "";
		sb.append(" id: ");
		for(String strTemp : temp){
			sb.append(strTemp+" ");
			str += this.anonymity.get(strTemp).getLength()+" ";
		}
		sb.append("\r\n");
		sb.append(" metricRepre: ");
		sb.append(str+"\r\n");
		sb.append("=========================================\r\n");
		return sb.toString();
	}
	
	public void initK(){
		int[] length = this.getlength();
		int max = this.getMax(length);
		if(1==max){
			this.k = length.length;
		}else{
			int[] nums = new int[max];
			for(int i=0;i<nums.length;i++){
				nums[i] = 0;
			}
			for(int i=0;i<length.length;i++){
				nums[length[i]-1]++;       //the array store the number of the same distance，e.g.,a[0] stores the number of vertices such that distance is 1

			}
			this.k = length.length;
			for(int i=0;i<nums.length;i++){
				if(nums[i]!=0 && nums[i]<=this.k){
					this.k = nums[i];
					this.kLength = i+1;
				}
			}
		}
		
	}
	//return the array of shortest distance from all the other nodes to this node
	public int[] getlength(){
		int[] result = new int[this.anonymity.size()];
		Set<String> temp = this.anonymity.keySet();
		int i=0;
		for(String str : temp){
			result[i++] = this.anonymity.get(str).getLength();
		}
		return result ;
	}
	//get eccentricity
	public int getMax(int[] data){
		int flag = 0;
		for(int i=0;i<data.length;i++){
			if(flag<data[i]){
				flag = data[i];
			}
		}
		return flag ;
	}
	
	public int getMaxLengthId(){
		int id = 0;
		Set<String> temp = this.anonymity.keySet();
		int max = 0;
		int tempInt = 0 ;
		for(String str : temp){
			tempInt = this.anonymity.get(str).getLength();
			if(tempInt>max){
				max = tempInt ;
				id = Integer.parseInt(str);
			}
		}
		return id ;
	}
	
	public int getEccentricity(){
		int ecc = 0;
		int temp = 0;
		Set<String> set = this.anonymity.keySet();
		for(String str : set){
			temp = this.anonymity.get(str).getLength();
			if(temp>ecc){
				ecc = temp;
			}
		}
		return ecc ;
	}


	//check whether there exist other node with the same distance 
	public boolean isOnlyLengthNode(TreeNode node){
		boolean flag = true;
		Set<String> temp = this.anonymity.keySet();
		for(String str : temp){
			if(!str.equals(String.valueOf(node.getId()))){//exclude itself
				if(this.anonymity.get(str).getLength() == node.getLength()){
					flag = false;
					break;
				}
			}
		}
		return flag ;
	}
	
	
	public String getAllTreeNodeStr(){
		StringBuilder sb = new StringBuilder();
		sb.append("====================================\r\n");
		sb.append(this.id+":");
		Set<String> temp = this.anonymity.keySet();
		for(String str : temp){
			sb.append(this.anonymity.get(str).toString());
		}
		sb.append("====================================\r\n");
		return sb.toString();
	}
}
