package me.onebone.actaeon.route;

public class SimpleRouteFinder extends RouteFinter{
	@Override
	public boolean search(){
		this.resetNodes();

		this.addNode(new Node(this.getDestination())); // just go straight

		return false;
	}

	@Override
	public boolean isSearching(){
		return false;
	}

	@Override
	public boolean isDone(){
		return true;
	}
}
