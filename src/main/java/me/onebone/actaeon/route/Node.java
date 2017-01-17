package me.onebone.actaeon.route;

import cn.nukkit.math.Vector3;

public class Node{
	private Node parent = null;

	private Vector3 node;
	public double f = -1;
	public double g = -1;

	public Node(double x, double y, double z){
		this.node = new Vector3(x, y, z);
	}

	public Node(Vector3 vec){
		if(vec == null){
			throw new IllegalArgumentException("Node cannot be null");
		}

		this.node = new Vector3(vec.x, vec.y, vec.z);
	}

	public Vector3 getVector3(){
		return new Vector3(node.x, node.y, node.z);
	}

	public String toString(){
		return "Node (x=" + this.node.x + ", y=" + this.node.y + ", " + this.node.z + ")";
	}

	public void setParent(Node node){
		this.parent = node;
	}

	public Node getParent(){
		return this.parent;
	}
}
