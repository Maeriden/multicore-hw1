package mchw1.profiling.graph;

import java.util.ArrayList;
import java.util.List;

public class Node<V, E>
{
	public final List<Edge<E, V>> edges;
	public final V data;
	
	
	Node(V data)
	{
		this.edges = new ArrayList<>();
		this.data = data;
	}
	
	
	boolean
	add_edge(Node<V, E> target, E data)
	{
		if(this.edges.stream().anyMatch(e -> e.head == target))
			return false;
		Edge<E, V> edge = new Edge<>(this, target, data);
		return this.edges.add(edge);
	}
}
