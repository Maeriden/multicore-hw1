package mchw1.algorithms.parpar;


import java.util.Arrays;
import java.util.concurrent.RecursiveTask;

class Merge extends RecursiveTask<int[]>
{
	protected final int[] slice_l;
	protected final int[] slice_r;
	protected final int cutoff;
	
	
	Merge(int[] slice_l, int[] slice_r, int cutoff)
	{
		this.slice_l = slice_l;
		this.slice_r = slice_r;
		this.cutoff = cutoff;
	}
	
	
	@Override
	protected int[] compute()
	{
		//noinspection UnnecessaryLocalVariable
		int[] result = parallel_merge(this.slice_l, this.slice_r, this.cutoff);
		return result;
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
		int small_index = Arrays.binarySearch(small, 0, small.length, large[median]);
		if (small_index < 0)
		{
			small_index = ~small_index;
		}
		
		int[] result = new int[large.length + small.length];
		
		int[] s0 = Arrays.copyOfRange(large, 0, median);
		int[] s1 = Arrays.copyOfRange(small, 0, small_index);
		Merge left = new Merge(s0, s1, cutoff);
		
		s0 = Arrays.copyOfRange(large, median, large.length);
		s1 = Arrays.copyOfRange(small, small_index, small.length);
		Merge right = new Merge(s0, s1, cutoff);
		
		right.fork();
		int[] res1 = left.compute();
		int[] res2 = right.join();
		
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
