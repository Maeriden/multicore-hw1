package mchw1.profiling.graph;

import java.util.*;

public class Graph<T>
{
	private final Map<T, Node<T>> nodes;
	
	
	public
	Graph()
	{
		this.nodes = new HashMap<>();
	}
	
	
	public
	boolean
	add_node(T data)
	{
		if(data == null)
			return false;
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
		if(tail == null || head == null)
			return false;
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
	
	
	public synchronized
	boolean
	add_node_async(T data)
	{
		if(data == null)
			return false;
		if(this.nodes.containsKey(data))
			return false;
		Node<T> node = new Node<>(data);
		this.nodes.put(data, node);
		return true;
	}
	
	
	public synchronized
	boolean
	add_edge_async(T tail, T head)
	{
		if(tail == null || head == null)
			return false;
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
