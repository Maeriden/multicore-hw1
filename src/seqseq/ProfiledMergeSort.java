package seqseq;

import profiling.NodeData;
import profiling.graph.Graph;

import java.util.Arrays;

public class ProfiledMergeSort extends MergeSort
{
	private final Graph<NodeData> exec_dag;
	
	
	public
	ProfiledMergeSort(int[] array, int begin, int end, int cutoff, Graph<NodeData> exec_dag)
	{
		super(array, begin, end, cutoff);
		this.exec_dag = exec_dag;
	}
	
	
	public
	void
	execute()
	{
		split(super.array, super.buffer, super.begin, super.end, super.split_cutoff,
			  this.exec_dag, System.nanoTime());
	}
	
	
	static private
	NodeData
	split(int[] array, int[] buffer, int begin, int end, int cutoff,
		  Graph<NodeData> exec_dag, long time_epoch)
	{
		long time_begin_split = System.nanoTime() - time_epoch;
		
		int slice_length = end - begin;
		if(slice_length <= cutoff)
		{
			long time_begin_sort = System.nanoTime() - time_epoch;
			Arrays.sort(array, begin, end);
			long time_end_sort = System.nanoTime() - time_epoch;
			
			NodeData node_sort = new NodeData(NodeData.Type.SORT,
											  Thread.currentThread().getId(),
											  time_begin_sort, time_end_sort);
			exec_dag.add_node(node_sort);
			return node_sort;
		}
		
		
		int mid = begin + (slice_length / 2);
		
		//long time_begin_l = System.nanoTime() - time_epoch;
		NodeData node_l = split(array, buffer, begin, mid, cutoff, exec_dag, time_epoch);
		//long time_end_l = System.nanoTime() - time_epoch;
		
		//long time_begin_r = System.nanoTime() - time_epoch;
		NodeData node_r = split(array, buffer, mid, end, cutoff, exec_dag, time_epoch);
		//long time_end_r = System.nanoTime() - time_epoch;
		
		
		//long time_begin_merge = System.nanoTime() - time_epoch;
		NodeData node_merge = merge(array, buffer, begin, mid, end, exec_dag, time_epoch);
		//long time_end_merge = System.nanoTime() - time_epoch;
		
		System.arraycopy(buffer, begin,
						 array, begin,
						 slice_length);
		long time_end_split = System.nanoTime() - time_epoch;
		
		
		NodeData node_split = new NodeData(NodeData.Type.SPLIT,
										   Thread.currentThread().getId(),
										   time_begin_split, time_end_split);
		exec_dag.add_node(node_split);
		
		exec_dag.add_edge(node_split, node_l);
		exec_dag.add_edge(node_split, node_r);
		
		exec_dag.add_edge(node_l, node_merge);
		exec_dag.add_edge(node_r, node_merge);
		
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
										   time_begin_merge, time_end_merge);
		exec_dag.add_node(node_merge);
		return node_merge;
	}
}
