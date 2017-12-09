package mchw1.parpar;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class MergeSort extends RecursiveAction
{
	protected final int[] array;
	protected final int[] buffer;
	protected final int begin;
	protected final int end;
	protected final int split_cutoff;
	protected final int merge_cutoff;
	
	
	public
	MergeSort(int[] array, int begin, int end, int split_cutoff, int merge_cutoff)
	{
		this.array = array;
		this.begin = begin;
		this.end   = end;
		this.split_cutoff = split_cutoff;
		this.merge_cutoff = merge_cutoff;
		this.buffer = new int[this.array.length];
	}
	
	
	private
	MergeSort(int[] array, int[] buffer, int begin, int end, int split_cutoff, int merge_cutoff)
	{
		this.array = array;
		this.begin = begin;
		this.end   = end;
		this.split_cutoff = split_cutoff;
		this.merge_cutoff = merge_cutoff;
		this.buffer = buffer;
	}
	
	
	@Override
	protected
	void
	compute()
	{
		split(this.array, this.buffer, this.begin, this.end, this.split_cutoff, this.merge_cutoff);
	}
	
	
	static private
	void
	split(int[] array, int[] buffer, int begin, int end, int cutoff, int merge_cutoff)
	{
		assert RecursiveAction.inForkJoinPool();
		
		int slice_length = end - begin;
		if(slice_length <= cutoff)
		{
			Arrays.sort(array, begin, end);
			return;
		}
		
		int mid = begin + (slice_length / 2);
		MergeSort left = new MergeSort(array, buffer, begin, mid, cutoff, merge_cutoff);
		MergeSort right = new MergeSort(array, buffer, mid, end, cutoff, merge_cutoff);
		
		right.fork();
		left.compute();
		right.join();
		
		int[] array1 = Arrays.copyOfRange(array, begin, mid);
		int[] array2 = Arrays.copyOfRange(array, mid, end);
		int[] merged = parallel_merge(array1, array2, merge_cutoff);
		System.arraycopy(merged, 0,
						 array, begin,
						 merged.length);
	}
	
	
	static private
	int[]
	parallel_merge(int[] array1, int[] array2, int cutoff)
	{
		if(array1.length + array2.length <= cutoff)
		{
			//noinspection UnnecessaryLocalVariable
			int[] result = merge(array1, array2);
			return result;
		}
		
		if(array1.length == 0 && array2.length == 0)
			return new int[0];
		if(array1.length == 1 && array2.length == 0)
			return array1;
		if(array1.length == 0 && array2.length == 1)
			return array2;
		if(array1.length == 1 && array2.length == 1)
			return new int[] {Math.min(array1[0], array2[0]), Math.max(array1[0], array2[0])};
		
		int[] large = array1.length >  array2.length ? array1 : array2;
		int[] small = array1.length <= array2.length ? array1 : array2;
		
		int median = large.length / 2;
		int small_index = Math.abs(Arrays.binarySearch(small, 0, small.length, large[median])) - 1;
		
		int[] result = new int[large.length + small.length];
		
		int[] s0 = Arrays.copyOfRange(large, 0, median);
		int[] s1 = Arrays.copyOfRange(small, 0, small_index);
		int[] res1 = parallel_merge(s0, s1, cutoff);
		
		s0 = Arrays.copyOfRange(large, median, large.length);
		s1 = Arrays.copyOfRange(small, small_index, small.length);
		int[] res2 = parallel_merge(s0, s1, cutoff);
		
		System.arraycopy(res1, 0, result, 0, res1.length);
		System.arraycopy(res2, 0, result, res1.length, res2.length);
		return result;
	}
	
	
	static private
	int[]
	merge(int[] array1, int[] array2)
	{
		int[] result = new int[array1.length + array2.length];
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
		
		return result;
	}
}
