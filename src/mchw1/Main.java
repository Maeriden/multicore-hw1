package mchw1;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;
import mchw1.profiling.EdgeData;
import mchw1.profiling.GraphExporter;
import mchw1.profiling.NodeData;
import mchw1.profiling.graph.Graph;

import java.io.File;
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
		CommandLineOptions opts = argparse4j_get_command_line_options(args);
		if(opts == null)
			return;
		if(opts.split_cutoff < 1)
			throw new IllegalArgumentException("split-cutoff must be > 0");
		if(opts.merge_cutoff < 1)
			throw new IllegalArgumentException("merge-cutoff must be > 0");
		if(opts.array_length < 1)
			throw new IllegalArgumentException("Array is empty");
		
		int     split_cutoff = opts.split_cutoff;
		int     merge_cutoff = opts.merge_cutoff;
		int     array_length = opts.array_length;
		String  gexf         = opts.gexf;
		boolean do_build_dag = (opts.print_fork) || (opts.gexf != null);
		
		
		int[] unsorted = new int[array_length];
		if(opts.desc)
			generate_descending_array(unsorted);
		else
			generate_random_array(unsorted);
		
		
		long                      exec_time = -1;
		Graph<NodeData, EdgeData> exec_dag  = null;
		if(do_build_dag)
			exec_dag = new Graph<>();
		
		switch(opts.algo)
		{
			case SEQ_SEQ:
			{
				exec_time = sequential_sequential(unsorted, split_cutoff, exec_dag);
			} break;
			
			case PAR_SEQ:
			{
				exec_time = parallel_sequential(unsorted, split_cutoff, exec_dag);
			} break;
			
			case PAR_PAR:
			{
				exec_time = parallel_parallel(unsorted, split_cutoff, merge_cutoff, exec_dag);
			} break;
		}
		
		
		if(opts.gexf != null)
		{
			assert exec_dag != null;
			GraphExporter.export_gexf(exec_dag, new File(opts.gexf + ".gexf"));
		}
		
		
		if(opts.print_fork)
		{
			assert exec_dag != null;
			int fork_count = get_fork_count(exec_dag);
			System.out.println(Integer.toString(fork_count));
		}
		else
		{
			System.out.println(Long.toString(exec_time));
		}
	}
	
	
	static private
	void
	generate_descending_array(int[] result)
	{
		System.err.print(String.format("Creating array of %,d descending integers... ", result.length));
		
		long time_begin = System.currentTimeMillis();
		for(int i = 0; i < result.length; ++i)
			result[i] = result.length - i;
		long time_end = System.currentTimeMillis();
		
		long elapsed_time = time_end - time_begin;
		System.err.println(String.format("done in %dms", elapsed_time));
		if(result.length <= ARRAY_MAX_PRINT_SIZE)
		{
			System.err.println(Arrays.toString(result));
			System.err.println();
		}
		System.err.flush();
	}
	
	
	static private
	void
	generate_random_array(int[] result)
	{
		System.err.print(String.format("Creating array of %,d random integers... ", result.length));
		
		Random random = new Random(0);
		long time_begin = System.currentTimeMillis();
		for(int i = 0; i < result.length; ++i)
			result[i] = random.nextInt(result.length);
		long time_end = System.currentTimeMillis();
		
		long elapsed_time = time_end - time_begin;
		System.err.println(String.format("done in %dms", elapsed_time));
		if(result.length <= ARRAY_MAX_PRINT_SIZE)
		{
			System.err.println(Arrays.toString(result));
			System.err.println();
		}
		System.err.flush();
	}
	
	
	static private
	long
	sequential_sequential(int[] unsorted, int cutoff, Graph<NodeData, EdgeData> exec_dag)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.err.print(String.format("Sorting %,d integers using sequential split, sequential merge (cutoff at %d)... ",
									   array.length, cutoff));
		
		mchw1.seqseq.MergeSort merge_sort;
		if(exec_dag == null)
			merge_sort = new mchw1.seqseq.MergeSort(array, 0, array.length, cutoff);
		else
			merge_sort = new mchw1.seqseq.ProfiledMergeSort(array, 0, array.length, cutoff, exec_dag);
		
		long time_begin = System.currentTimeMillis();
		merge_sort.execute();
		long time_end = System.currentTimeMillis();
		assert is_sorted(array);
		
		long elapsed_time = time_end - time_begin;
		System.err.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.err.println(Arrays.toString(array));
		System.err.println();
		System.err.flush();
		
		return elapsed_time;
	}
	
	
	static private
	long
	parallel_sequential(int[] unsorted, int cutoff, Graph<NodeData, EdgeData> exec_dag)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.err.print(String.format("Sorting %,d integers using parallel split, sequential merge (cutoff at %d)... ",
									   array.length, cutoff));
		
		mchw1.parseq.MergeSort merge_sort;
		if(exec_dag == null)
			merge_sort = new mchw1.parseq.MergeSort(array, 0, array.length, cutoff);
		else
			merge_sort = new mchw1.parseq.ProfiledMergeSort(array, 0, array.length, cutoff, exec_dag);
		
		long time_begin = System.currentTimeMillis();
		Main.fj_pool.invoke(merge_sort);
		long time_end = System.currentTimeMillis();
		assert is_sorted(array);
		
		long elapsed_time = time_end - time_begin;
		System.err.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.err.println(Arrays.toString(array));
		System.err.println();
		System.err.flush();
		
		return elapsed_time;
	}
	
	
	static private
	long
	parallel_parallel(int[] unsorted, int split_cutoff, int merge_cutoff, Graph<NodeData, EdgeData> exec_dag)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.err.print(String.format("Sorting %,d integers using parallel split, parallel merge (split_cutoff at %d, merge_cutoff at %d)... ",
									   array.length, split_cutoff, merge_cutoff));
		
		mchw1.parpar.MergeSort merge_sort;
		if(exec_dag == null)
			merge_sort = new mchw1.parpar.MergeSort(array, 0, array.length, split_cutoff, merge_cutoff);
		else
			merge_sort = new mchw1.parpar.ProfiledMergeSort(array, 0, array.length, split_cutoff, merge_cutoff, exec_dag);
		
		long time_begin = System.currentTimeMillis();
		Main.fj_pool.invoke(merge_sort);
		long time_end = System.currentTimeMillis();
		assert is_sorted(array);
		
		long elapsed_time = time_end - time_begin;
		System.err.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.err.println(Arrays.toString(array));
		System.err.println();
		System.err.flush();
		
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
	
	
	static private
	int
	get_fork_count(Graph<NodeData, EdgeData> exec_dag)
	{
		return exec_dag.get_nodes().stream().mapToInt(n -> n.data.fork_count).max().orElse(0);
	}
	
	
	static private
	CommandLineOptions
	argparse4j_get_command_line_options(String[] args)
	{
		ArgumentParser argp = ArgumentParsers.newFor("mergesort").build()
											 .defaultHelp(false);
		
		ArgumentGroup group_algorithm = argp.addArgumentGroup("Algorithms");
		group_algorithm.addArgument("--algo")
		            .type(Arguments.enumStringType(AlgorithmType.class))
		            .setDefault(AlgorithmType.SEQ_SEQ)
		            .help("Algorithm used for sorting (default: ss)");
		
		ArgumentGroup group_generation = argp.addArgumentGroup("Number generation");
		group_generation.addArgument("--desc")
						.action(Arguments.storeTrue())
						.help("Generate descending numbers [N, 1] (default: random numbers)");
		
		ArgumentGroup group_tuning = argp.addArgumentGroup("Tuning");
		group_tuning.addArgument("--split-cutoff")
					.type(int.class)
					.setDefault(1)
					.metavar("CUTOFF")
					.help("Cutoff value for the divide operation (default: 1)");
		group_tuning.addArgument("--merge-cutoff")
					.type(int.class)
					.setDefault(1)
					.metavar("CUTOFF")
					.help("Cutoff value for the conquer operation (default: 1)");
		
		ArgumentGroup group_profiling = argp.addArgumentGroup("Profiling");
		group_profiling.addArgument("--gexf")
					   .metavar("PATH")
					   .help("Dump execution data into a gexf file at the given PATH (do not include file extension)");
		group_profiling.addArgument("--fork")
		               .action(Arguments.storeTrue())
		               .dest("print_fork")
		               .help("Print fork count to stdout (default: print execution time)");
		
		argp.addArgument("array-length")
			.type(int.class)
			.required(true)
			.metavar("N")
			.help("Amount of numbers to sort");
		
		try
		{
			CommandLineOptions opts = new CommandLineOptions();
			argp.parseArgs(args, opts);
			return opts;
		}
		catch(HelpScreenException ignored)
		{
		
		}
		catch(ArgumentParserException exception)
		{
			System.err.println(exception.getMessage());
			argp.printUsage();
		}
		return null;
	}
}


enum AlgorithmType
{
	SEQ_SEQ { public String toString() { return "ss"; } },
	PAR_SEQ { public String toString() { return "ps"; } },
	PAR_PAR { public String toString() { return "pp"; } }
}


class CommandLineOptions
{
	@Arg AlgorithmType algo;
	@Arg boolean desc;
	@Arg int split_cutoff;
	@Arg int merge_cutoff;
	@Arg int array_length;
	@Arg String gexf;
	@Arg boolean print_fork;
}
