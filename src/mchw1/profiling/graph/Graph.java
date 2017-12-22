package mchw1.profiling.graph;

import java.util.*;

public class Graph<V, E>
{
	private final Map<V, Node<V, E>> nodes;
	
	
	public
	Graph()
	{
		this.nodes = new HashMap<>();
	}
	
	
	public
	boolean
	add_node(V data)
	{
		if(data == null)
			return false;
		if(this.nodes.containsKey(data))
			return false;
		Node<V, E> node = new Node<>(data);
		this.nodes.put(data, node);
		return true;
	}
	
	
	public
	boolean
	add_edge(V tail, V head, E data)
	{
		if(tail == null || head == null)
			return false;
		boolean added = false;
		Node<V, E> tail_node = this.nodes.get(tail);
		if(tail_node != null)
		{
			Node<V, E> head_node = this.nodes.get(head);
			if(head_node != null)
			{
				added = tail_node.add_edge(head_node, data);
			}
		}
		return added;
	}
	
	
	public synchronized
	boolean
	add_node_async(V data)
	{
		return this.add_node(data);
	}
	
	
	public synchronized
	boolean
	add_edge_async(V tail, V head, E data)
	{
		return this.add_edge(tail, head, data);
	}
	
	
	public
	Collection<Node<V, E>>
	get_nodes()
	{
		return this.nodes.values();
	}
}
