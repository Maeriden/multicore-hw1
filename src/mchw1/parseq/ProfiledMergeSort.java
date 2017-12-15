package mchw1.parseq;

import mchw1.profiling.NodeData;
import mchw1.profiling.graph.Graph;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class ProfiledMergeSort extends MergeSort
{
	private final Graph<NodeData> exec_dag;
	private final long time_epoch;
	
	private NodeData node_dag;
	
	
	public
	ProfiledMergeSort(int[] array, int begin, int end, int split_cutoff,
					  Graph<NodeData> exec_dag)
	{
		super(array, begin, end, split_cutoff);
		this.exec_dag = exec_dag;
		this.time_epoch = System.nanoTime();
	}
	
	
	private
	ProfiledMergeSort(int[] array, int[] buffer, int begin, int end, int split_cutoff,
					  Graph<NodeData> exec_dag, long time_epoch)
	{
		super(array, buffer, begin, end, split_cutoff);
		this.exec_dag = exec_dag;
		this.time_epoch = time_epoch;
	}
	
	
	@Override
	protected
	void
	compute()
	{
		this.node_dag = split(this.array, this.buffer, this.begin, this.end, this.split_cutoff,
							  this.exec_dag, this.time_epoch);
	}
	
	
	static private
	NodeData
	split(int[] array, int[] buffer, int begin, int end, int cutoff,
		  Graph<NodeData> exec_dag, long time_epoch)
	{
		assert RecursiveAction.inForkJoinPool();
		
		long time_begin_split = System.nanoTime() - time_epoch;
		
		int slice_length = end - begin;
		if(slice_length <= cutoff)
		{
			long time_begin_sort = System.nanoTime() - time_epoch;
			Arrays.sort(array, begin, end);
			long time_end_sort = System.nanoTime() - time_epoch;
			
			NodeData node_sort = new NodeData(NodeData.Type.SORT,
											  Thread.currentThread().getId(),
											  time_begin_sort, time_end_sort,
											  0);
			exec_dag.add_node_async(node_sort);
			return node_sort;
		}
		
		int mid = begin + (slice_length / 2);
		ProfiledMergeSort left = new ProfiledMergeSort(array, buffer, begin, mid, cutoff, exec_dag, time_epoch);
		ProfiledMergeSort right = new ProfiledMergeSort(array, buffer, mid, end, cutoff, exec_dag, time_epoch);
		
		right.fork();
		left.compute();
		right.join();
		
		NodeData node_merge = merge(array, buffer, begin, mid, end, exec_dag, time_epoch);
		System.arraycopy(buffer, begin,
						 array, begin,
						 end - begin);
		
		long time_end_split = System.nanoTime() - time_epoch;
		
		
		NodeData node_l = left.node_dag;
		NodeData node_r = right.node_dag;
		NodeData node_split = new NodeData(NodeData.Type.SPLIT,
										   Thread.currentThread().getId(),
										   time_begin_split, time_end_split,
										   1 + node_l.fork_count + node_r.fork_count);
		exec_dag.add_node_async(node_split);
		
		exec_dag.add_edge_async(node_split, node_l);
		exec_dag.add_edge_async(node_split, node_r);
		
		exec_dag.add_edge_async(node_l, node_merge);
		exec_dag.add_edge_async(node_r, node_merge);
		
		return node_split;
	}
	
	
	static private
	NodeData
	merge(int[] array, int[] buffer, int begin, int mid, int end,
		  Graph<NodeData> exec_dag, long time_epoch)
	{
		long time_begin_merge = System.nanoTime() - time_epoch;
		{
			int slice_l_length = mid - begin;
			int slice_r_length = end - mid;
			
			int li = 0, ri = 0;
			while(li < slice_l_length && ri < slice_r_length)
			{
				if(array[begin + li] < array[mid + ri])
				{
					buffer[begin + li + ri] = array[begin + li];
					++li;
				}
				else
				{
					buffer[begin + li + ri] = array[mid + ri];
					++ri;
				}
			}
			
			if(li < slice_l_length)
			{
				System.arraycopy(array, begin + li,
								 buffer, begin + li + ri,
								 slice_l_length - li);
			}
			
			if(ri < slice_r_length)
			{
				System.arraycopy(array, mid + ri,
								 buffer, begin + li + ri,
								 slice_r_length - ri);
			}
		}
		long time_end_merge = System.nanoTime() - time_epoch;
		
		NodeData node_merge = new NodeData(NodeData.Type.MERGE,
										   Thread.currentThread().getId(),
										   time_begin_merge, time_end_merge,
										   0);
		exec_dag.add_node_async(node_merge);
		return node_merge;
	}
}
