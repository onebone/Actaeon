package me.onebone.actaeon.route;

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

	private boolean succeed = false;

	// https://en.wikipedia.org/wiki/A*_search_algorithm
	@Override
	public boolean search(){
		// TODO 경로 찾는 태스트를 매틱마다 하도록
		this.resetNodes();

		openSet.add(this.getStart());

		Vector3 destination = new Vector3(this.destination.x, getHighestUnder(this.destination.x, this.destination.y, this.destination.z) + 1, this.destination.z);

		gScore.put(this.getStart(), 0.0);
		fScore.put(this.getStart(), this.heuristic(this.getStart(), destination));

		while(!openSet.isEmpty()){
			if(closedSet.size() > 1000){ // eating too much memory
				break;
			}

			// the node in openSet having the lowest fScore[] value

			Vector3 current = null;
			for(Vector3 open : openSet){
				if(current == null || fScore.getOrDefault(open, Double.MAX_VALUE) < fScore.getOrDefault(current, Double.MAX_VALUE)){
					current = open;
				}
			}

			if(destination.floor().equals(current.floor())){ // current cannot be null: openSet is not empty
				List<Node> nodes = new LinkedList<>();
				nodes.add(new Node(destination));

				while((current = cameFrom.get(current)) != null){
					/*if(cameFrom.get(current) != null){
						Vector3 vec = cameFrom.get(cameFrom.get(current));
						if(vec != null){
							Vector3 sub = vec.subtract(current);
							if(Math.abs(sub.x) == Math.abs(sub.z)){
								current = vec; // skip next
								continue;
							}
						}
					}*/
					nodes.add(new Node(new Vector3((int) current.x + 0.5, (int)current.y, (int) current.z + 0.5)));
					level.addParticle(new cn.nukkit.level.particle.CriticalParticle(current, 4));// TODO test code
				}

				Collections.reverse(nodes);
				nodes.forEach(this::addNode);

				this.succeed = true;
				return true;
			}

			openSet.remove(current);
			closedSet.add(current);

			//     x
			//   x E x
			//     x

			int[][] check = new int[][]{
					new int[]{-1, 0},
					new int[]{0, -1}, new int[]{0, 1},
					new int[]{1, 0}
			};
			for(int[] c : check){
				int high = getHighestUnder(Math.floor(current.x) + c[0], Math.floor(current.y) + 2, Math.floor(current.z) + c[1]);
				if(high == -1 || Math.abs(high + 1 - current.y) >= 1){
					continue;
				}
				Vector3 neighbor = new Vector3(current.x + c[0], high + 1, current.z + c[1]);

				/*AxisAlignedBB aabb = this.getBoundingBox();
				if(this.getLevel().getCollisionBlocks(new AxisAlignedBB(
						neighbor.x - ((aabb.maxX - aabb.minX) / 2),
						neighbor.y,
						neighbor.z - ((aabb.maxZ - aabb.minZ) / 2),
						neighbor.x + ((aabb.maxX - aabb.minX) / 2),
						neighbor.y + (aabb.maxY - aabb.minY),
						neighbor.z + ((aabb.maxZ - aabb.minZ) / 2)
				)).length > 0) continue;*/
				if(!this.getLevel().getBlock(neighbor).canPassThrough()) continue;
				if(closedSet.contains(neighbor)) continue;

				double tentative_gScore = gScore.getOrDefault(current, Double.MAX_VALUE) + current.distance(neighbor);
				tentative_gScore = tentative_gScore < 0 ? Double.MAX_VALUE : tentative_gScore; // overflow
				if(!openSet.contains(neighbor)){
					openSet.add(neighbor);
				}else if(tentative_gScore >= gScore.getOrDefault(neighbor, Double.MAX_VALUE)){
					continue;
				}

				cameFrom.put(neighbor, current);
				gScore.put(neighbor, tentative_gScore);
				double f = gScore.getOrDefault(neighbor, Double.MAX_VALUE) + heuristic(neighbor, destination);
				fScore.put(neighbor, f < 0 ? Double.MAX_VALUE : f);
			}
		}

		this.succeed = false;
		return false;
	}

	private int getHighestUnder(double x, double dy, double z){
		for(int y=(int)dy;y >= 0; y--){
			if(!level.getBlock(new Vector3(x, y, z)).canPassThrough()) return y;
		}
		return -1;
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
	}

	@Override
	public boolean research(){
		return this.search();
	}

	@Override
	public boolean isSearching(){
		return false;
	}

	@Override
	public boolean isSuccess(){
		return this.succeed;
	}
}
