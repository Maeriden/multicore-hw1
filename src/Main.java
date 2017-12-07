import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class Main
{
	static private final int ARRAY_MAX_PRINT_SIZE = 200;
	static private final ForkJoinPool fj_pool = new ForkJoinPool();
	
	
	static public
	void
	main(String[] args)
	{
		boolean do_seq_seq = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-ss"));
		boolean do_par_seq = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-ps"));
		boolean do_par_par = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-pp"));
		boolean do_desc = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-desc"));
		boolean do_rand = Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("-rand"));
		int cutoff = Integer.parseInt(Arrays.stream(args).filter(arg -> arg.matches("-c\\d+")).findFirst().orElse("-c0").substring(2));
		int array_len = Integer.parseInt(Arrays.stream(args).filter(arg -> arg.matches("-s\\d+")).findFirst().orElse("-s0").substring(2));
		int merge_cutoff = 1;
		
		if(!do_seq_seq && !do_par_seq && !do_par_par)
		{
			// Default to sequential test
			do_seq_seq = true;
		}
		if(!do_desc && !do_rand)
		{
			// Default to worst-case array
			do_desc = true;
		}
		if(cutoff < 1)
		{
			// Default to no cutoff
			cutoff = 1;
		}
		if(array_len < 1)
		{
			// Default to big array
			array_len = 100_000_000;
		}
		
		int[] unsorted = new int[array_len];
		if(do_desc)
		{
			System.out.print(String.format("Creating array of %,d descending integers... ", unsorted.length));
			
			long elapsed_time = generate_descending_array(unsorted);
			
			System.out.println(String.format("done in %dms", elapsed_time));
			if(unsorted.length <= ARRAY_MAX_PRINT_SIZE)
				System.out.println(Arrays.toString(unsorted));
			System.out.println();
			
			if(do_seq_seq)
				sequential_sequential(unsorted, cutoff);
			if(do_par_seq)
				parallel_sequential(unsorted, cutoff);
			if(do_par_par)
				parallel_parallel(unsorted, cutoff, merge_cutoff);
			
			System.gc();
		}
		
		if(do_desc && do_rand)
			System.out.println();
		
		if(do_rand)
		{
			System.out.print(String.format("Creating array of %,d random integers... ", unsorted.length));
			
			long elapsed_time = generate_random_array(unsorted);
			
			System.out.println(String.format("done in %dms", elapsed_time));
			if(unsorted.length <= ARRAY_MAX_PRINT_SIZE)
				System.out.println(Arrays.toString(unsorted));
			System.out.println();
			
			if(do_seq_seq)
				sequential_sequential(unsorted, cutoff);
			if(do_par_seq)
				parallel_sequential(unsorted, cutoff);
			if(do_par_par)
				parallel_parallel(unsorted, cutoff, merge_cutoff);
			
			System.gc();
		}
	}
	
	
	static private
	long
	generate_descending_array(int[] array)
	{
		long time_begin = System.currentTimeMillis();
		for(int i = 0; i < array.length; ++i)
			array[i] = array.length - i;
		long time_end = System.currentTimeMillis();
		return time_end - time_begin;
	}
	
	
	static private
	long
	generate_random_array(int[] array)
	{
		Random random = new Random(0);
		long time_begin = System.currentTimeMillis();
		for(int i = 0; i < array.length; ++i)
			array[i] = random.nextInt(array.length);
		long time_end = System.currentTimeMillis();
		return time_end - time_begin;
	}
	
	
	static private
	long
	sequential_sequential(int[] unsorted, int cutoff)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.out.print(String.format("Sorting %,d integers using sequential split, sequential merge (cutoff at %d)... ",
									   array.length, cutoff));
		
		long time_begin = System.currentTimeMillis();
		seqseq.MergeSort merge_sort = new seqseq.MergeSort(array, 0, array.length, cutoff);
		merge_sort.execute();
		long time_end = System.currentTimeMillis();
		assert is_sorted(array);
		
		long elapsed_time = time_end - time_begin;
		System.out.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.out.println(Arrays.toString(array));
		System.out.println();
		
		return elapsed_time;
	}
	
	
	static private
	long
	parallel_sequential(int[] unsorted, int cutoff)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.out.print(String.format("Sorting %,d integers using parallel split, sequential merge (cutoff at %d)... ",
									   array.length, cutoff));
		
		long time_begin = System.currentTimeMillis();
		parseq.MergeSort merge_sort = new parseq.MergeSort(array, 0, array.length, cutoff);
		Main.fj_pool.invoke(merge_sort);
		long time_end = System.currentTimeMillis();
		assert is_sorted(array);
		
		long elapsed_time = time_end - time_begin;
		System.out.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.out.println(Arrays.toString(array));
		System.out.println();
		
		return elapsed_time;
	}
	
	
	static private
	long
	parallel_parallel(int[] unsorted, int cutoff, int merge_cutoff)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.out.print(String.format("Sorting %,d integers using parallel split, parallel merge (cutoff at %d)... ",
									   array.length, cutoff));
		
		long time_begin = System.currentTimeMillis();
		parpar.MergeSort merge_sort = new parpar.MergeSort(array, 0, array.length, cutoff, merge_cutoff);
		Main.fj_pool.invoke(merge_sort);
		long time_end = System.currentTimeMillis();
		assert is_sorted(array);
		
		long elapsed_time = time_end - time_begin;
		System.out.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.out.println(Arrays.toString(array));
		System.out.println();
		
		return elapsed_time;
	}
	
	
	static private
	boolean
	is_sorted(int[] array)
	{
		for(int i = 0; i < array.length - 1; ++i)
		{
			if(array[i] > array[i + 1])
				return false;
		}
		return true;
	}
}
