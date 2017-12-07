package seqseq;

import java.util.Arrays;

public class MergeSort
{
	private final int[] array;
	private final int[] buffer;
	private final int begin;
	private final int end;
	private final int cutoff;
	
	
	public
	MergeSort(int[] array, int begin, int end, int cutoff)
	{
		this.array = array;
		this.begin = begin;
		this.end   = end;
		this.cutoff = cutoff;
		this.buffer = new int[this.array.length];
	}
	
	
	public
	void
	execute()
	{
		sort(this.array, this.buffer, this.begin, this.end, this.cutoff);
	}
	
	
	static private
	void
	sort(int[] array, int[] buffer, int begin, int end, int cutoff)
	{
		int slice_length = end - begin;
		if(slice_length <= cutoff)
		{
			Arrays.sort(array, begin, end);
			return;
		}
		
		int mid = begin + (slice_length / 2);
		sort(array, buffer, begin, mid, cutoff);
		sort(array, buffer, mid, end, cutoff);
		
		merge(array, buffer, begin, mid, end);
		System.arraycopy(buffer, begin,
						 array, begin,
						 end - begin);
	}
	
	
	static private
	void
	merge(int[] array, int[] buffer, int begin, int mid, int end)
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
}
