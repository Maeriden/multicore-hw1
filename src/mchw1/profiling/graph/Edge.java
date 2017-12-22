package mchw1.profiling.graph;

public class Edge<E, V>
{
	public final Node<V, E> tail;
	public final Node<V, E> head;
	public final E data;
	
	
	public
	Edge(Node<V, E> tail, Node<V, E> head, E data)
	{
		this.tail = tail;
		this.head = head;
		this.data = data;
	}
}
