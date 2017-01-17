package me.onebone.actaeon.route;

import cn.nukkit.level.Level;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public abstract class RouteFinder{
	private int current = 0;
	protected Vector3 destination = null, start = null;
	protected List<Node> nodes = new ArrayList<>();
	protected Level level = null;
	protected AxisAlignedBB aabb = null;

	public void setPositions(Level level, Vector3 start, Vector3 dest, AxisAlignedBB bb){
		this.setLevel(level);
		this.setStart(start);
		this.setDestination(dest);
		this.setBoundingBox(bb);
	}

	public void setStart(Vector3 start){
		if(start == null) throw new IllegalArgumentException("Cannot set start as null");

		this.start = new Vector3(start.x, start.y, start.z);
	}

	public Vector3 getStart(){
		if(start == null) return null;

		return new Vector3(start.x, start.y, start.z);
	}

	public void setDestination(Vector3 destination){
		if(destination == null) throw new IllegalArgumentException("Cannot set destination as null");

		this.destination = new Vector3(destination.x, destination.y, destination.z);
	}

	public Vector3 getDestination(){
		if(destination == null) return null;

		return new Vector3(destination.x, destination.y, destination.z);
	}

	public void setLevel(Level level){
		if(level == null) throw new IllegalArgumentException("Level cannot be null");

		this.level = level;
	}

	public Level getLevel(){
		return this.level;
	}

	public void setBoundingBox(AxisAlignedBB bb){
		if(bb == null){
			this.aabb = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		}

		this.aabb = bb;
	}

	public AxisAlignedBB getBoundingBox(){
		if(this.aabb == null) return new AxisAlignedBB(0, 0, 0, 0, 0, 0);

		return this.aabb.clone();
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
		Vector3 cur = this.get().getVector3();

		/*return NukkitMath.floorDouble(vec.x) ==  NukkitMath.floorDouble(cur.x)
				&& NukkitMath.floorDouble(vec.y) == NukkitMath.floorDouble(cur.y)
				&& NukkitMath.floorDouble(vec.z) == NukkitMath.floorDouble(cur.z);*/
		return vec.x == cur.x
				//&& vec.y == cur.y
				&& vec.z == cur.z;
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
