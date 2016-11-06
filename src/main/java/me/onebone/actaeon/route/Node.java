package me.onebone.actaeon.route;

import cn.nukkit.math.Vector3;

public class Node{
	private Vector3 node;

	public Node(double x, double y, double z){
		this.node = new Vector3(x, y, z);
	}

	public Node(Vector3 vec){
		if(vec == null){
			throw new IllegalArgumentException("Node cannot be null");
		}

		this.node = new Vector3(vec.x, vec.y, vec.z);
	}

	public Vector3 getNode(){
		return new Vector3(node.x, node.y, node.z);
	}
}
