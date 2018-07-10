package utilities;

///import java.awt.List;
import java.util.ArrayList;


public class Path{
	
	ArrayList<Integer> pathSeq=new ArrayList<Integer>();
	int startNode;
	int endNode;
	int length;
	ArrayList<ArrayList<Integer>> lpath=new ArrayList<ArrayList<Integer>>();
	
	public Path() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Path(int node) {
		
		this.startNode = node;
		this.endNode=node;
		this.length=0;
		ArrayList<Integer> tmp=  new ArrayList<Integer>();
		tmp.clear();
		tmp.add(node);
		this.pathSeq= tmp;
		//this.pathSeq.add(node);
		//System.out.println(this.pathSeq);
		
	}
	public Path add(int node){
		
		this.endNode=node;
		this.length++;
		this.pathSeq.add(node);
		return this;
	}
	
 public Object addAll(Path p){
		
		this.endNode=p.endNode;
		this.length=this.length+p.length;
		this.pathSeq.addAll(p.pathSeq);
		//System.out.print(this.pathSeq);
		return this;
	}
	
	public void printPath(){
		System.out.print("Length :  "+this.length);
  	System.out.print("   Start :  "+this.startNode);
  	System.out.print("   End :  "+this.endNode);
  	System.out.println("   Seq :  "+this.pathSeq);
	}
}