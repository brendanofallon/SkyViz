package app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Node {
	
	private String id;
	private Node parent = null;
	private ArrayList<Node> children;
	private double distToParent;
	
	boolean flag = false;
	
	public Node() {
		Random rng = new Random();
		id = "ind" + String.valueOf( rng.nextInt() ); 
		children = new ArrayList<Node>();
	}
	
	public Node(String id) {
		this.id = id; 
		children = new ArrayList<Node>();
	}
	
	public void setId(String newId) {
		id = newId;
	}
	
	public void setFlag() {
		flag = true;
	}
	
	public void setFlag(boolean b) {
		flag = b;
	}
	
	public boolean getFlag() {
		return flag;
	}
	
	public List<Node> getFlaggedOffspring() {
		List<Node> flagged = new ArrayList<Node>(2);
		for(Node kid : children) {
			if (kid.getFlag())
				flagged.add(kid);
		}
		return flagged;
	}
	
	public int getNumFlaggedOffspring() {
		int flagged = 0;
		for(Node kid : children) {
			if (kid.getFlag())
				flagged++;
		}
		return flagged;
	}
	
	public List<Node> getOffspring() {
		return children;
	}
		
	public String getId() { 
		return id;
	}
	
	public void addChild(Node c) {
		children.add(c);
	}
	
	public void addChild(Node c, double dist) {
		children.add(c);
		c.setParent(this, dist);
	}
	
	public void setDistToParent(double d) {
		distToParent = d;
	}
	
	public double getDistToParent() {
		return distToParent;
	}
	
	public boolean isTip() {
		return children.size()==0;
	}
	
	
	public void setParent(Node p) {
		parent = p;
	}
	
	public void setParent(Node p, double dist) {
		parent = p;
		distToParent = dist;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public int numChildren() {
		return children.size();
	}
	
	public Node getChild(int which) {
		if (which>children.size()) {
			System.err.println(" Tried to get child #" + which + " but there's only " + children.size() + " kids ..");
		}
		return children.get(which);
	}
	
}
