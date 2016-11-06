package me.onebone.actaeon.route;

import cn.nukkit.math.Vector2;

public class Node{
	private Vector2 node;

	public Node(Vector2 node){
		if(this.node == null){
			throw new IllegalArgumentException("Node cannot be null");
		}

		this.node = new Vector2(node.x, node.y);
	}

	public Vector2 getNode(){
		return new Vector2(node.x, node.y);
	}
}
