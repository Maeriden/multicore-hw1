package mchw1.profiling.graph;

import java.util.ArrayList;
import java.util.List;

public class Node<T>
{
	public final long id;
	public final T data;
	public final List<Node<T>> adjacents;
	
	
	Node(int id, T data)
	{
		this.id = id;
		this.data = data;
		this.adjacents = new ArrayList<>();
	}
	
	
	boolean
	add_edge(Node<T> target)
	{
		if(this.adjacents.contains(target))
			return false;
		return this.adjacents.add(target);
	}
}