package me.onebone.actaeon.route;

import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public abstract class RouteFinter{
	private int current = 0;
	protected Vector3 destination = null;
	protected List<Node> nodes = new ArrayList<>();

	public void setDestination(Vector3 destination){
		this.destination = destination;
	}

	public Vector3 getDestination(){
		return new Vector3(destination.x, destination.y, destination.z);
	}

	protected void resetNodes(){
		this.nodes.clear();

		this.current = 0;
	}

	protected void addNode(Node node){
		this.nodes.add(node);
	}

	/**
	 * @return true if it has next node to go
	 */
	public boolean hasNext(){
		return nodes.size() > this.current + 1;
	}

	/**
	 * Move to next node
	 * @return true if succeed
	 */
	public boolean next(){
		if(this.hasNext()){
			this.current++;
			return true;
		}
		return false;
	}

	/**
	 * Returns if the entity has reached the node
	 * @return true if reached
	 */
	public boolean hasReachedNode(Vector3 vec){
		Vector3 cur = this.get().getNode();

		return NukkitMath.floorDouble(vec.x) == cur.x
				&& NukkitMath.floorDouble(vec.y) == cur.y
				&& NukkitMath.floorDouble(vec.z) == cur.z;
	}

	/**
	 * Gets node of current
	 * @return current node
	 */
	public Node get(){
		return nodes.get(current);
	}

	/**
	 * Search for route
	 * @return true if extra process for finding route it not needed
	 */
	public abstract boolean search();

	/**
	 * @return true if searching is not end
	 */
	public abstract boolean isSearching();

	/**
	 * @return true if finding route is done
	 */
	public abstract boolean isDone();
}
