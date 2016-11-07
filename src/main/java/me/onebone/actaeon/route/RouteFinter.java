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
		this.destination = new Vector3(destination.x, destination.y, destination.z);

		this.resetNodes();
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
		if(nodes.size() == 0) throw new IllegalStateException("There is no path found");

		return nodes.size() > this.current + 1;
	}

	/**
	 * Move to next node
	 * @return true if succeed
	 */
	public boolean next(){
		if(nodes.size() == 0) throw new IllegalStateException("There is no path found");

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
		if(nodes.size() == 0) throw new IllegalStateException("There is no path found.");
		return nodes.get(current);
	}

	public void arrived(){
		this.nodes.clear();
	}

	public boolean hasRoute(){
		return this.nodes.size() > 0;
	}

	/**
	 * Search for route
	 * @return true if finding path is done. It also returns true even if there is no route.
	 */
	public abstract boolean search();

	/**
	 * Re-search route to destination
	 * @return true if finding path is done.
	 */
	public abstract boolean research();

	/**
	 * @return true if searching is not end
	 */
	public abstract boolean isSearching();

	/**
	 * @return true if finding route was success
	 */
	public abstract boolean isSuccess();
}
