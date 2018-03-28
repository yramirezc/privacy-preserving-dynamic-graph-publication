package anonymization.method2;

public class TreeNode {
	private int id;
	private int pId;  //the id of previous node of this node
	private int length;
	
	
	public TreeNode() {
	}

	public TreeNode(int id, int pId, int length) {
		super();
		this.id = id;
		this.pId = pId;
		this.length = length;
	}

	// getter setter
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public int getpId() {
		return pId;
	}
	public void setpId(int pId) {
		this.pId = pId;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	@Override
	public String toString() {
		return "TreeNode [id=" + id + ", pId=" + pId + ", length=" + length
				+ "]\r\n";
	}
	
}
