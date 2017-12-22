package mchw1.parpar;

import mchw1.profiling.EdgeData;
import mchw1.profiling.NodeData;
import mchw1.profiling.graph.Graph;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class ProfiledMergeSort extends MergeSort
{
	private final Graph<NodeData, EdgeData> exec_dag;
	private final long time_epoch;
	
	private NodeData node_dag;
	
	
	public
	ProfiledMergeSort(int[] array, int begin, int end, int split_cutoff, int merge_cutoff,
	                  Graph<NodeData, EdgeData> exec_dag)
	{
		super(array, begin, end, split_cutoff, merge_cutoff);
		this.exec_dag = exec_dag;
		this.time_epoch = System.nanoTime();
	}
	
	
	private
	ProfiledMergeSort(int[] array, int[] buffer, int begin, int end, int split_cutoff, int merge_cutoff,
	                  Graph<NodeData, EdgeData> exec_dag, long time_epoch)
	{
		super(array, buffer, begin, end, split_cutoff, merge_cutoff);
		this.exec_dag = exec_dag;
		this.time_epoch = time_epoch;
	}
	
	
	@Override
	protected
	void
	compute()
	{
		this.node_dag = split(this.array, this.buffer, this.begin, this.end, this.split_cutoff, this.merge_cutoff,
		                      this.exec_dag, this.time_epoch);
	}
	
	
	static private
	NodeData
	split(int[] array, int[] buffer, int begin, int end, int split_cutoff, int merge_cutoff,
	      Graph<NodeData, EdgeData> exec_dag, long time_epoch)
	{
		assert RecursiveAction.inForkJoinPool();
		
		int slice_length = end - begin;
		if(slice_length <= split_cutoff)
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
		
		
		ProfiledMergeSort split_l;
		ProfiledMergeSort split_r;
		ProfiledMerge     merge;
		long time_begin_split = System.nanoTime() - time_epoch;
		{
			int mid = begin + (slice_length / 2);
			split_l = new ProfiledMergeSort(array, buffer, begin, mid, split_cutoff, merge_cutoff,
			                                exec_dag, time_epoch);
			split_r = new ProfiledMergeSort(array, buffer, mid,   end, split_cutoff, merge_cutoff,
			                                exec_dag, time_epoch);
			
			split_r.fork();
			split_l.compute();
			split_r.join();
			
			int[] array_1 = Arrays.copyOfRange(array, begin, mid);
			int[] array_2 = Arrays.copyOfRange(array, mid, end);
			merge = new ProfiledMerge(array_1, array_2, merge_cutoff,
			                          exec_dag, time_epoch);
			int[] merged = merge.compute();
			System.arraycopy(merged, 0,
			                 array, begin,
			                 merged.length);
		}
		long time_end_split = System.nanoTime() - time_epoch;
		
		
		NodeData node_l = split_l.node_dag;
		NodeData node_r = split_r.node_dag;
		NodeData node_merge = merge.node_dag;
		NodeData node_split = new NodeData(NodeData.Type.SPLIT,
		                                   Thread.currentThread().getId(),
		                                   time_begin_split, time_end_split,
		                                   1 + node_l.fork_count + node_r.fork_count + node_merge.fork_count);
		exec_dag.add_node_async(node_split);
		
		exec_dag.add_edge_async(node_split, node_l,     new EdgeData(EdgeData.Type.CALL));
		exec_dag.add_edge_async(node_split, node_r,     new EdgeData(EdgeData.Type.CALL));
		exec_dag.add_edge_async(node_split, node_merge, new EdgeData(EdgeData.Type.CALL));
		
		exec_dag.add_edge_async(node_l, node_merge, new EdgeData(EdgeData.Type.DATA));
		exec_dag.add_edge_async(node_r, node_merge, new EdgeData(EdgeData.Type.DATA));
		
		return node_split;
	}
	
	
//	static private
//	NodeData
//	merge(int[] array, int[] buffer, int begin, int mid, int end,
//	      Graph<NodeData> exec_dag, long time_epoch)
//	{
//		long time_begin_merge = System.nanoTime() - time_epoch;
//		{
//			int slice_l_length = mid - begin;
//			int slice_r_length = end - mid;
//
//			int li = 0, ri = 0;
//			while(li < slice_l_length && ri < slice_r_length)
//			{
//				if(array[begin + li] < array[mid + ri])
//				{
//					buffer[begin + li + ri] = array[begin + li];
//					++li;
//				}
//				else
//				{
//					buffer[begin + li + ri] = array[mid + ri];
//					++ri;
//				}
//			}
//
//			if(li < slice_l_length)
//			{
//				System.arraycopy(array, begin + li,
//				                 buffer, begin + li + ri,
//				                 slice_l_length - li);
//			}
//
//			if(ri < slice_r_length)
//			{
//				System.arraycopy(array, mid + ri,
//				                 buffer, begin + li + ri,
//				                 slice_r_length - ri);
//			}
//		}
//		long time_end_merge = System.nanoTime() - time_epoch;
//
//		NodeData node_merge = new NodeData(NodeData.Type.MERGE,
//		                                   Thread.currentThread().getId(),
//		                                   time_begin_merge, time_end_merge,
//		                                   0);
//		exec_dag.add_node_async(node_merge);
//		return node_merge;
//	}
}
