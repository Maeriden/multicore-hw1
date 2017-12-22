package mchw1.parpar;

import mchw1.profiling.EdgeData;
import mchw1.profiling.NodeData;
import mchw1.profiling.graph.Graph;
import mchw1.utils.Tuple2;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.binarySearch;

class ProfiledMerge extends Merge
{
	private final Graph<NodeData, EdgeData> exec_dag;
	private final long time_epoch;
	
	NodeData node_dag;
	
	
	ProfiledMerge(int[] slice_l, int[] slice_r, int cutoff,
	              Graph<NodeData, EdgeData> exec_dag, long time_epoch)
	{
		super(slice_l, slice_r, cutoff);
		this.exec_dag = exec_dag;
		this.time_epoch = time_epoch;
	}
	
	
	@Override
	protected int[] compute()
	{
		// TODO: Compute begin and end indices
		Tuple2<int[], NodeData> result = parallel_merge(this.slice_l, this.slice_r, 0, 0, 0, 0, this.cutoff,
		                                                this.exec_dag, this.time_epoch);
		this.node_dag = result.b;
		return result.a;
	}
	
	
	static private
	Tuple2<int[], NodeData>
	parallel_merge(int[] array1, int[] array2, int begin1, int end1, int begin2, int end2, int cutoff,
	               Graph<NodeData, EdgeData> exec_dag, long time_epoch)
	{
		if(array1.length + array2.length <= cutoff)
		{
			//noinspection UnnecessaryLocalVariable
			Tuple2<int[], NodeData> result = merge(array1, array2, begin1, end1, begin2, end2,
			                                       exec_dag, time_epoch);
			return result;
		}
		
		assert !(array1.length == 0 && array2.length == 0);
		assert !(array1.length == 1 && array2.length == 0);
		assert !(array1.length == 0 && array2.length == 1);
		
		if(array1.length == 1 && array2.length == 1)
		{
			long time_begin_merge = System.nanoTime() - time_epoch;
			int[] result = new int[] {min(array1[0], array2[0]), max(array1[0], array2[0])};
			long time_end_merge = System.nanoTime() - time_epoch;
			
			NodeData node_merge = new NodeData(NodeData.Type.MERGE,
			                                   Thread.currentThread().getId(),
			                                   time_begin_merge, time_end_merge,
			                                   begin1, end1, begin2, end2,
			                                   0);
			return new Tuple2<>(result, node_merge);
		}
		
		ProfiledMerge merge_1;
		ProfiledMerge merge_2;
		int[] result;
		long time_begin_merge = System.nanoTime() - time_epoch;
		{
			int[] large = array1.length >  array2.length ? array1 : array2;
			int[] small = array1.length <= array2.length ? array1 : array2;
			
			int large_median = large.length / 2;
			int small_index = abs(binarySearch(small, 0, small.length, large[large_median])) - 1;
			
			
			int[] s0 = Arrays.copyOfRange(large, 0, large_median);
			int[] s1 = Arrays.copyOfRange(small, 0, small_index);
			merge_1 = new ProfiledMerge(s0, s1, cutoff,
			                            exec_dag, time_epoch);
			
			s0 = Arrays.copyOfRange(large, large_median, large.length);
			s1 = Arrays.copyOfRange(small, small_index, small.length);
			merge_2 = new ProfiledMerge(s0, s1, cutoff,
			                            exec_dag, time_epoch);
			
			merge_2.fork();
			int[] merged_1 = merge_1.compute();
			int[] merged_2 = merge_2.join();
			
			result = new int[large.length + small.length];
			System.arraycopy(merged_1, 0, result, 0,               merged_1.length);
			System.arraycopy(merged_2, 0, result, merged_1.length, merged_2.length);
		}
		long time_end_merge = System.nanoTime() - time_epoch;
		
		
		NodeData node_1 = merge_1.node_dag;
		NodeData node_2 = merge_2.node_dag;
		NodeData node_merge = new NodeData(NodeData.Type.MERGE_PARALLEL,
		                                   Thread.currentThread().getId(),
		                                   time_begin_merge, time_end_merge,
		                                   begin1, end1, begin2, end2,
		                                   1 + node_1.fork_count + node_2.fork_count);
		exec_dag.add_node_async(node_merge);
		
		exec_dag.add_edge_async(node_merge, node_1, new EdgeData(EdgeData.Type.CALL));
		exec_dag.add_edge_async(node_merge, node_2, new EdgeData(EdgeData.Type.CALL));
		
		return new Tuple2<>(result, node_merge);
	}
	
	
	static private
	Tuple2<int[], NodeData>
	merge(int[] array1, int[] array2, int begin1, int end1, int begin2, int end2,
	      Graph<NodeData, EdgeData> exec_dag, long time_epoch)
	{
		int[] result;
		long time_begin_merge = System.nanoTime() - time_epoch;
		{
			result = new int[array1.length + array2.length];
			int li = 0, ri = 0;
			while(li < array1.length && ri < array2.length)
			{
				if(array1[li] < array2[ri])
				{
					result[li + ri] = array1[li];
					++li;
				}
				else
				{
					result[li + ri] = array2[ri];
					++ri;
				}
			}
			if(li < array1.length)
			{
				System.arraycopy(array1, li,
				                 result, li + ri,
				                 array1.length - li);
			}
			if(ri < array2.length)
			{
				System.arraycopy(array2, ri,
				                 result, li + ri,
				                 array2.length - ri);
			}
		}
		long time_end_merge = System.nanoTime() - time_epoch;
		
		
		NodeData node_merge = new NodeData(NodeData.Type.MERGE,
		                                   Thread.currentThread().getId(),
		                                   time_begin_merge, time_end_merge,
		                                   begin1, end1, begin2, end2,
		                                   0);
		exec_dag.add_node_async(node_merge);
		
		return new Tuple2<>(result, node_merge);
	}
}
