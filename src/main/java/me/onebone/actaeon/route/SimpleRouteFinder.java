package me.onebone.actaeon.route;

public class SimpleRouteFinder extends RouteFinter{
	@Override
	public boolean search(){
		this.resetNodes();

		this.addNode(new Node(this.getDestination())); // just go straight

		return true;
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
		return nodes.size() > 0;
	}
}
