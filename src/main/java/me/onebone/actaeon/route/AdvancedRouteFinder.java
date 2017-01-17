package me.onebone.actaeon.route;

import cn.nukkit.block.Block;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancedRouteFinder extends RouteFinder{
	private Set<Vector3> closedSet = new HashSet<>(),
			openSet = new HashSet<>();

	private Map<Vector3, Vector3> cameFrom = new HashMap<>();

	private Map<Vector3, Double> gScore = new HashMap<>(),
			fScore = new HashMap<>();

	private boolean succeed = false, searching = false;

	private Vector3 realDestination = null;


	// https://en.wikipedia.org/wiki/A*_search_algorithm
	@Override
	public boolean search(){
		return false;
	}

	private int getHighestUnder(double x, double dy, double z){
		for(int y=(int)dy;y >= 0; y--){
			Block block = level.getBlock(new Vector3(x, y, z));
			if(!canWalkOn(block)) return -1;

			if(!block.canPassThrough()) return y;
		}
		return -1;
	}

	private boolean canWalkOn(Block block){
		return !(block.getId() == Block.LAVA || block.getId() == Block.STILL_LAVA);
	}

	private boolean checkBlocks(Block[] blocks, AxisAlignedBB bb){
		for(Block block : blocks) if(!block.getBoundingBox().intersectsWith(bb)) return false;
		return true;
	}

	private double heuristic(Vector3 one, Vector3 two){
		return one.distance(two);
	}

	@Override
	public void resetNodes(){
		super.resetNodes();

		this.gScore.clear(); this.fScore.clear();
		this.openSet.clear(); this.closedSet.clear();
		this.cameFrom.clear();

		openSet.add(this.getStart());

		this.realDestination = new Vector3(this.destination.x, getHighestUnder(this.destination.x, this.destination.y, this.destination.z) + 1, this.destination.z);

		gScore.put(this.getStart(), 0.0);
		fScore.put(this.getStart(), this.heuristic(this.getStart(), this.realDestination));
	}

	@Override
	public boolean research(){
		this.resetNodes();

		return this.search();
	}

	@Override
	public boolean isSearching(){
		return this.searching;
	}

	@Override
	public boolean isSuccess(){
		return this.succeed;
	}
}
