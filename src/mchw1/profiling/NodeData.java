package mchw1.profiling;

public class NodeData
{
	public enum Type
	{
		SPLIT,
		SORT,
		MERGE,
		MERGE_PARALLEL
	}
	
	
	public final Type type;
	public final long thread_id;
	public final long time_begin_computation;
	public final long time_end_computation;
	public final int slice_1_begin;
	public final int slice_1_end;
	public final int slice_2_begin;
	public final int slice_2_end;
	public final int fork_count;
	
	
	public
	NodeData(Type type, long thread_id,
	         long time_begin_computation, long time_end_computation,
	         int slice_1_begin, int slice_1_end,
	         int fork_count)
	{
		this.type = type;
		this.thread_id = thread_id;
		this.time_begin_computation = time_begin_computation;
		this.time_end_computation = time_end_computation;
		this.slice_1_begin = slice_1_begin;
		this.slice_1_end = slice_1_end;
		this.fork_count = fork_count;
		this.slice_2_begin = -1;
		this.slice_2_end = -1;
	}
	
	
	public
	NodeData(Type type, long thread_id,
	         long time_begin_computation, long time_end_computation,
	         int slice_1_begin, int slice_1_end, int slice_2_begin, int slice_2_end,
	         int fork_count)
	{
		this.type = type;
		this.thread_id = thread_id;
		this.time_begin_computation = time_begin_computation;
		this.time_end_computation = time_end_computation;
		this.slice_1_begin = slice_1_begin;
		this.slice_1_end = slice_1_end;
		this.fork_count = fork_count;
		this.slice_2_begin = slice_2_begin;
		this.slice_2_end = slice_2_end;
	}
}
