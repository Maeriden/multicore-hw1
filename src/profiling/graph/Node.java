package profiling.graph;

import java.util.ArrayList;
import java.util.List;

public class Node<T>
{
	public final T data;
	public final List<Node<T>> adjacents = new ArrayList<>();
	

	public
	Node(T data)
	{
		this.data = data;
	}
	
	
	public
	boolean
	add_edge(Node<T> target)
	{
		if(this.adjacents.contains(target))
			return false;
		return this.adjacents.add(target);
	}
}
