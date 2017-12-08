package profiling.graph;

import java.util.*;

public class Graph<T>
{
	private final Map<T, Node<T>> nodes = new HashMap<>();
	
	
	public
	boolean
	add_node(T data)
	{
		if(this.nodes.containsKey(data))
			return false;
		Node<T> node = new Node<>(data);
		this.nodes.put(data, node);
		return true;
	}
	
	
	public
	boolean
	add_edge(T tail, T head)
	{
		boolean added = false;
		Node<T> tail_node = this.nodes.get(tail);
		if(tail_node != null)
		{
			Node<T> head_node = this.nodes.get(head);
			if(head_node != null)
			{
				added = tail_node.add_edge(head_node);
			}
		}
		return added;
	}
	
	
	public
	Collection<Node<T>>
	get_nodes()
	{
		return this.nodes.values();
	}
}
