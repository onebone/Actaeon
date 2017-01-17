package me.onebone.actaeon.route;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.level.Position;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AdvancedRouteFinder extends RouteFinder{
	private boolean succeed = false, searching = false;

	private Vector3 realDestination = null;

	private TreeSet<Node> open = new TreeSet<>((o1, o2) -> o1.f == -1 ? -1 : (int) Math.floor(o1.f - o2.f));
	private Set<Node> closed = new HashSet<>();

	// https://en.wikipedia.org/wiki/A*_search_algorithm
	@Override
	public boolean search(){
		Node start = new Node(this.getStart());
		start.f = start.g = 0;

		open.add(start);

		int limit = 1000;
		while(!open.isEmpty() && limit-- > 0){
			Node node = open.pollLast();
			if(node.getVector3().floor().equals(this.realDestination)){
				List<Node> nodes = new ArrayList<>();

				nodes.add(node);
				while((node = node.getParent()) != null){
					//level.addParticle(new cn.nukkit.level.particle.CriticalParticle(node.getVector3(), 3));
					nodes.add(node);
				}
				Collections.reverse(nodes);

				nodes.forEach(this::addNode);
				this.succeed = true; this.searching = false;
				return true;
			}

			closed.add(node);

			for(Node neighbor : this.getNeighbors(node)){
				if(closed.contains(neighbor)) continue;

				double tentative_gScore = node.g + neighbor.getVector3().distance(node.getVector3());

				if(!open.contains(neighbor)) open.add(neighbor);
				else if(tentative_gScore >= neighbor.g && neighbor.g != -1) continue;

				neighbor.setParent(node);
				neighbor.g = tentative_gScore;
				neighbor.f = neighbor.g + this.heuristic(neighbor.getVector3(), this.realDestination);
			}
		}
		return this.succeed = this.searching = false;
	}

	public Set<Node> getNeighbors(Node node){
		Set<Node> nodes = new HashSet<>();

		Vector3 vec = node.getVector3();
		boolean s1, s2, s3, s4;

		if(s1 = isWalkableAt(vec.add(1))){
			nodes.add(new Node(vec.add(1)));
		}

		if(s2 = isWalkableAt(vec.add(-1))){
			nodes.add(new Node(vec.add(-1)));
		}

		if(s3 = isWalkableAt(vec.add(0, 0, 1))){
			nodes.add(new Node(vec.add(0, 0, 1)));
		}

		if(s4 = isWalkableAt(vec.add(0, 0, -1))){
			nodes.add(new Node(vec.add(0, 0, -1)));
		}

		if(s1 && s3 && isWalkableAt(vec.add(1, 0, 1))){
			nodes.add(new Node(vec.add(1, 0, 1)));
		}

		if(s1 && s4 && isWalkableAt(vec.add(1, 0, -1))){
			nodes.add(new Node(vec.add(1, 0, -1)));
		}

		if(s2 && s3 && isWalkableAt(vec.add(-1, 0, 1))){
			nodes.add(new Node(vec.add(-1, 0, 1)));
		}

		if(s2 && s4 && isWalkableAt(vec.add(-1, 0, -1))){
			nodes.add(new Node(vec.add(-1, 0, -1)));
		}

		return nodes;
	}

	private Block getHighestUnder(double x, double dy, double z){
		for(int y=(int)dy;y >= 0; y--){
			Block block = level.getBlock(new Vector3(x, y, z));

			if(!canWalkOn(block)) return block;
			if(!block.canPassThrough()) return block;
		}
		return null;
	}

	private boolean isWalkableAt(Vector3 vec){
		Block block = this.getHighestUnder(vec.x, vec.y, vec.z);
		if(block == null) return false;

		double diff = (block.y - vec.y);

		return -4 < diff && diff < 2 && canWalkOn(block); // TODO: 동물의 종류에 따라 다름
	}

	private boolean canWalkOn(Block block){
		return !(block.getId() == Block.LAVA || block.getId() == Block.STILL_LAVA);
	}

	private boolean checkBlocks(Block[] blocks, AxisAlignedBB bb){
		for(Block block : blocks) if(!block.getBoundingBox().intersectsWith(bb)) return false;
		return true;
	}

	private double heuristic(Vector3 one, Vector3 two){
		double dx = Math.abs(one.x - two.x);
		double dy = Math.abs(one.z - two.z);
		return (Math.sqrt(2) - 1) * Math.min(dx, dy) + Math.max(dx, dy);
	}

	@Override
	public void resetNodes(){
		super.resetNodes();

		Block block = this.getHighestUnder(this.destination.x, this.destination.y, this.destination.z);
		if(block == null){
			block = new BlockAir();
			block.position(new Position(this.destination.x, 0, this.destination.z));
		}

		this.realDestination = new Vector3(this.destination.x, block.y + 1, this.destination.z).floor();
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
