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

		gScore.put(this.getStart(), 0.0);
		fScore.put(this.getStart(), this.heuristic(this.getStart(), this.getDestination()));

		while(!openSet.isEmpty()){
			// the node in openSet having the lowest fScore[] value
			Vector3 current = null;
			for(Vector3 open : openSet){
				if(current == null || fScore.getOrDefault(open, Double.MAX_VALUE) < fScore.getOrDefault(current, Double.MAX_VALUE)){
					current = open;
				}
			}

			if(this.getDestination().floor().equals(current.floor())){ // current cannot be null: openSet is not empty
				List<Node> nodes = new LinkedList<>();
				nodes.add(new Node(current));

				while((current = cameFrom.get(current)) != null){
					nodes.add(new Node(current));
				}

				Collections.reverse(nodes);
				nodes.forEach(this::addNode);

				this.succeed = true;
				return true;
			}

			openSet.remove(current);
			closedSet.add(current);

			for(int i = -1; i <= 1; i++){
				for(int j = -1; j <= 1; j++){
					for(int k = -1; k <= 1; k++){
						if(i == 0 && j == 0 && k == 0 || (j != 0 && (i == 0 || k == 0))) continue; // == current
						Vector3 neighbor = current.add(i, j, k);
						if(closedSet.contains(neighbor)) continue;

						double tentative_gScore = gScore.get(current) + current.distance(neighbor);
						if(!openSet.contains(neighbor)){
							openSet.add(neighbor);
						}else if(tentative_gScore >= gScore.get(neighbor)){
							continue;
						}

						cameFrom.put(neighbor, current);
						gScore.put(neighbor, tentative_gScore);
						fScore.put(neighbor, gScore.get(neighbor) + heuristic(neighbor, this.getDestination()));
					}
				}
			}
		}

		this.succeed = false;
		return false;
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
