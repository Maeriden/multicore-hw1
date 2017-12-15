package mchw1.profiling;

public class NodeData
{
	public enum Type
	{
		SPLIT,
		SORT,
		MERGE
	}
	
	
	public final Type type;
	public final long thread_id;
	public final long time_begin_computation;
	public final long time_end_computation;
	public final int fork_count;
	
	
	public
	NodeData(Type type, long thread_id, long time_begin_computation, long time_end_computation, int fork_count)
	{
		this.type = type;
		this.thread_id = thread_id;
		this.time_begin_computation = time_begin_computation;
		this.time_end_computation = time_end_computation;
		this.fork_count = fork_count;
	}
}
