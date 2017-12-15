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
//		int[] merged = parallel_merge(array1, array2, merge_cutoff);
		Merge merger = new Merge(array1, array2, merge_cutoff);
		int[] merged = merger.compute();
		System.arraycopy(merged, 0,
						 array, begin,
						 merged.length);
	}
}
