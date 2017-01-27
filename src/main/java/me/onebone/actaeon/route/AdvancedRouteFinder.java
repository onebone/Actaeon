package me.onebone.actaeon.route;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import me.onebone.actaeon.entity.Climbable;
import me.onebone.actaeon.entity.Fallable;
import me.onebone.actaeon.entity.MovingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancedRouteFinder extends RouteFinder{
	private boolean succeed = false, searching = false;

	private Vector3 realDestination = null;

	private Set<Node> open = new HashSet<>();

	private Grid grid = new Grid();

	public AdvancedRouteFinder(MovingEntity entity){
		super(entity);
	}

	@Override
	public boolean search(){
		if(this.getStart() == null || this.getDestination() == null){
			return this.succeed = this.searching = false;
		}

		this.resetNodes();
		Node start = new Node(this.getStart().floor());
		start.f = start.g = 0;

		open.add(start);
		this.grid.putNode(start.getVector3(), start);

		Node endNode = new Node(this.realDestination.floor());
		this.grid.putNode(endNode.getVector3(), endNode);

		this.succeed = false;
		this.searching = true;

		int limit = 500;
		while(!open.isEmpty() && limit-- > 0){
			Node node = null;

			double f = Double.MAX_VALUE;
			for(Node cur : this.open){
				if(cur.f < f && cur.f != -1){
					node = cur;
					f = cur.f;
				}
			}

			if(endNode.equals(node)){
				List<Node> nodes = new ArrayList<>();

				nodes.add(node);
				while((node = node.getParent()) != null){
					node.add(0.5, 0, 0.5);
					//level.addParticle(new cn.nukkit.level.particle.CriticalParticle(node.getVector3(), 3));

					nodes.add(node);
				};
				Collections.reverse(nodes);
				nodes.remove(0);

				nodes.forEach(this::addNode);
				this.succeed = true; this.searching = false;
				return true;
			}

			node.closed = true;
			open.remove(node);

			for(Node neighbor : this.getNeighbors(node)){
				if(neighbor.closed) continue;

				double tentative_gScore = node.g + neighbor.getVector3().distance(node.getVector3());

				if(!open.contains(neighbor)) open.add(neighbor);
				else if(neighbor.g != -1 && tentative_gScore >= neighbor.g) continue;

				neighbor.setParent(node);
				neighbor.g = tentative_gScore;
				neighbor.f = neighbor.g + this.heuristic(neighbor.getVector3(), endNode.getVector3());
			}
		}

		return this.succeed = this.searching = false;
	}

	public Set<Node> getNeighbors(Node node){
		Set<Node> neighbors = new HashSet<>();

		Vector3 vec = node.getVector3();
		boolean s1, s2, s3, s4;

		double y;
		if(s1 = (y = isWalkableAt(vec.add(1))) != -256){
			neighbors.add(this.grid.getNode(vec.add(1, y)));
		}

		if(s2 = (y = isWalkableAt(vec.add(-1))) != -256){
			neighbors.add(this.grid.getNode(vec.add(-1, y)));
		}

		if(s3 = (y = isWalkableAt(vec.add(0, 0, 1))) != -256){
			neighbors.add(this.grid.getNode(vec.add(0, y, 1)));
		}

		if(s4 = (y = isWalkableAt(vec.add(0, 0, -1))) != -256){
			neighbors.add(this.grid.getNode(vec.add(0, y, -1)));
		}

		if(s1 && s3 && (y = isWalkableAt(vec.add(1, 0, 1))) != -256){
			neighbors.add(this.grid.getNode(vec.add(1, y, 1)));
		}

		if(s1 && s4 && (y = isWalkableAt(vec.add(1, 0, -1))) != -256){
			neighbors.add(this.grid.getNode(vec.add(1, y, -1)));
		}

		if(s2 && s3 && (y = isWalkableAt(vec.add(-1, 0, 1))) != -256){
			neighbors.add(this.grid.getNode(vec.add(-1, y, 1)));
		}

		if(s2 && s4 && (y = isWalkableAt(vec.add(-1, 0, -1))) != -256){
			neighbors.add(this.grid.getNode(vec.add(-1, y, -1)));
		}

		return neighbors;
	}

	private Block getHighestUnder(double x, double dy, double z){
		for(int y=(int)dy;y >= 0; y--){
			Block block = level.getBlock(new Vector3(x, y, z));

			if(!canWalkOn(block)) return block;
			if(!block.canPassThrough()) return block;
		}
		return null;
	}

	private double isWalkableAt(Vector3 vec){
		Block block = this.getHighestUnder(vec.x, vec.y + 2, vec.z);
		if(block == null) return -256;

		double diff = (block.y - vec.y) + 1;

		if((this.entity instanceof Fallable || -4 < diff) && (this.entity instanceof Climbable || diff <= 1) && canWalkOn(block)){
			return diff;
		}
		return -256;
	}

	private boolean canWalkOn(Block block){
		return !(block.getId() == Block.LAVA || block.getId() == Block.STILL_LAVA);
	}

	private double heuristic(Vector3 one, Vector3 two){
		double dx = Math.abs(one.x - two.x);
		double dy = Math.abs(one.y - two.y);
		double dz = Math.abs(one.z - two.z);

		double max = Math.max(dx, dz);
		double min = Math.min(dx, dz);

		return 0.414 * min + max + dy;
	}

	@Override
	public void resetNodes(){
		super.resetNodes();

		this.grid.clear();
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

	private class Grid{
		private Map<Double, Map<Double, Map<Double, Node>>> grid = new HashMap<>();

		public void clear(){
			grid.clear();
		}

		public void putNode(Vector3 vec, Node node){
			vec = vec.floor();

			if(!grid.containsKey(vec.x)){
				grid.put(vec.x, new HashMap<>());
			}

			if(!grid.get(vec.x).containsKey(vec.y)){
				grid.get(vec.x).put(vec.y, new HashMap<>());
			}

			grid.get(vec.x).get(vec.y).put(vec.z, node);
		}

		public Node getNode(Vector3 vec){
			vec = vec.floor();

			if(!grid.containsKey(vec.x) || !grid.get(vec.x).containsKey(vec.y) || !grid.get(vec.x).get(vec.y).containsKey(vec.z)){
				Node node = new Node(vec.x, vec.y, vec.z);
				this.putNode(node.getVector3(), node);
				return node;
			}

			return grid.get(vec.x).get(vec.y).get(vec.z);
		}
	}
}
