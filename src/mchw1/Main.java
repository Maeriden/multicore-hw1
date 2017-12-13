package mchw1;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;
import mchw1.profiling.GraphExporter;
import mchw1.profiling.NodeData;
import mchw1.profiling.graph.Graph;

import java.io.File;
import java.io.IOException;
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
		CommandLineOptions opts = argp_get_command_line_options(args);
		if(opts == null)
			return;
		if(!opts.ss && !opts.ps && !opts.pp)
			return;
		if(opts.split_cutoff < 1)
			return;
		if(opts.merge_cutoff < 1)
			return;
		if(opts.array_length < 1)
			return;
		
		
		int split_cutoff = opts.split_cutoff;
		int merge_cutoff = opts.merge_cutoff;
		int array_length = opts.array_length;
		String gexf = opts.gexf;
		
		
		String generation_type;
		int[] unsorted = new int[array_length];
		if(opts.desc)
		{
			generation_type = "desc";
			generate_descending_array(unsorted);
		}
		else
		{
			generation_type = "rand";
			generate_random_array(unsorted);
		}
		
		
		if(opts.ss)
		{
			Graph<NodeData> exec_dag = gexf != null ? new Graph<>() : null;
			sequential_sequential(unsorted, split_cutoff, exec_dag);
			if(gexf != null)
				GraphExporter.export_gexf(exec_dag, new File(gexf + "." + generation_type + ".ss.gexf"));
		}
		
		if(opts.ps)
		{
			Graph<NodeData> exec_dag = gexf != null ? new Graph<>() : null;
			parallel_sequential(unsorted, split_cutoff, exec_dag);
			if(gexf != null)
				GraphExporter.export_gexf(exec_dag, new File(gexf + "." + generation_type + ".ps.gexf"));
		}
		
		if(opts.pp)
		{
			Graph<NodeData> exec_dag = gexf != null ? new Graph<>() : null;
			parallel_parallel(unsorted, split_cutoff, merge_cutoff);
			if(gexf != null)
				GraphExporter.export_gexf(exec_dag, new File(gexf + "." + generation_type + ".pp.gexf"));
		}
	}
	
	
	static private
	void
	generate_descending_array(int[] result)
	{
		System.out.print(String.format("Creating array of %,d descending integers... ", result.length));
		
		long time_begin = System.currentTimeMillis();
		for(int i = 0; i < result.length; ++i)
			result[i] = result.length - i;
		long time_end = System.currentTimeMillis();
		
		long elapsed_time = time_end - time_begin;
		System.out.println(String.format("done in %dms", elapsed_time));
		if(result.length <= ARRAY_MAX_PRINT_SIZE)
			System.out.println(Arrays.toString(result));
		System.out.println();
	}
	
	
	static private
	void
	generate_random_array(int[] result)
	{
		System.out.print(String.format("Creating array of %,d random integers... ", result.length));
		
		Random random = new Random(0);
		long time_begin = System.currentTimeMillis();
		for(int i = 0; i < result.length; ++i)
			result[i] = random.nextInt(result.length);
		long time_end = System.currentTimeMillis();
		
		long elapsed_time = time_end - time_begin;
		System.out.println(String.format("done in %dms", elapsed_time));
		if(result.length <= ARRAY_MAX_PRINT_SIZE)
			System.out.println(Arrays.toString(result));
		System.out.println();
	}
	
	
	static private
	void
	sequential_sequential(int[] unsorted, int cutoff, Graph<NodeData> exec_dag)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.out.print(String.format("Sorting %,d integers using sequential split, sequential merge (cutoff at %d)... ",
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
		System.out.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.out.println(Arrays.toString(array));
		System.out.println();
	}
	
	
	static private
	void
	parallel_sequential(int[] unsorted, int cutoff, Graph<NodeData> exec_dag)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.out.print(String.format("Sorting %,d integers using parallel split, sequential merge (cutoff at %d)... ",
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
		System.out.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.out.println(Arrays.toString(array));
		System.out.println();
	}
	
	
	static private
	void
	parallel_parallel(int[] unsorted, int cutoff, int merge_cutoff)
	{
		int[] array = Arrays.copyOf(unsorted, unsorted.length);
		System.out.print(String.format("Sorting %,d integers using parallel split, parallel merge (cutoff at %d)... ",
									   array.length, cutoff));
		
		long time_begin = System.currentTimeMillis();
		mchw1.parpar.MergeSort merge_sort = new mchw1.parpar.MergeSort(array, 0, array.length, cutoff, merge_cutoff);
		Main.fj_pool.invoke(merge_sort);
		long time_end = System.currentTimeMillis();
		assert is_sorted(array);
		
		long elapsed_time = time_end - time_begin;
		System.out.println(String.format("done in %dms", elapsed_time));
		if(array.length <= ARRAY_MAX_PRINT_SIZE)
			System.out.println(Arrays.toString(array));
		System.out.println();
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
	CommandLineOptions
	jopt_get_command_line_options(String[] args)
	{
		OptionParser jopt = new OptionParser(false);
		
		OptionSpec<Void> help = jopt
			.acceptsAll(Arrays.asList("?", "h", "help"), "Show this help and exit. If present, ignores all other options")
			.forHelp();
		
		OptionSpec<Void> ss = jopt
			.accepts("ss", "Run sequential-sequential merge sort.");
		OptionSpec<Void> ps = jopt
			.accepts("ps", "Run parallel-sequential merge sort.");
		OptionSpec<Void> pp = jopt
			.accepts("pp", "Run parallel-parallel merge sort.");
		OptionSpec<Void> desc = jopt
			.acceptsAll(Arrays.asList("d", "desc"), "Generate descending numbers (worst-case scenario).");
		OptionSpec<Integer> split_cutoff = jopt
			.acceptsAll(Arrays.asList("c", "split-cutoff"), "Cutoff parameter for the divide operation.")
			.withRequiredArg()
			.ofType(int.class)
			.required()
			.defaultsTo(1);
		OptionSpec<Integer> merge_cutoff = jopt
			.acceptsAll(Arrays.asList("m", "merge-cutoff"), "Cutoff parameter for the conquer operation.")
			.requiredIf(pp)
			.withRequiredArg()
			.ofType(int.class)
			.required()
			.defaultsTo(1);
		OptionSpec<Integer> array_length = jopt
			.acceptsAll(Arrays.asList("n", "numbers"), "Amount of numbers to sort. Will be [n,1] with --desc.")
			.withRequiredArg()
			.ofType(int.class)
			.required()
			.defaultsTo(100_000);
		OptionSpec<String> gexf = jopt
			.acceptsAll(Arrays.asList("g", "gexf"), "File name to write gexf-formatted execution data. Do not include file extension.")
			.withRequiredArg()
			.ofType(String.class);
		
		OptionSet options = jopt.parse(args);
		
		if(options.has(help))
		{
			try
			{
				jopt.printHelpOn(System.err);
			}
			catch(IOException exception)
			{
				exception.printStackTrace();
			}
			return null;
		}
		
		CommandLineOptions opts = new CommandLineOptions();
		opts.ss = options.has(ss);
		opts.ps = options.has(ps);
		opts.pp = options.has(pp);
		opts.desc = options.has(desc);
		opts.split_cutoff = options.valueOf(split_cutoff);
		opts.merge_cutoff = options.valueOf(merge_cutoff);
		opts.array_length = options.valueOf(array_length);
		opts.gexf = options.valueOf(gexf);
		
		return opts;
	}
	
	
	static private
	CommandLineOptions
	argp_get_command_line_options(String[] args)
	{
		ArgumentParser argp = ArgumentParsers.newFor("mergesort").build()
											 .defaultHelp(true);
		
		ArgumentGroup group_algorithm = argp.addArgumentGroup("Algorithms");
		group_algorithm.addArgument("--ss")
					   .action(Arguments.storeTrue())
					   .help("Run sequential-sequential merge sort");
		group_algorithm.addArgument("--ps")
					   .action(Arguments.storeTrue())
					   .help("Run parallel-sequential merge sort");
		group_algorithm.addArgument("--pp")
					   .action(Arguments.storeTrue())
					   .help("Run parallel-parallel merge sort");
		
		ArgumentGroup group_generation = argp.addArgumentGroup("Number generation");
		group_generation.addArgument("--desc")
						.action(Arguments.storeTrue())
						.help("Generate descending numbers (worst-case scenario)");
		
		ArgumentGroup group_tuning = argp.addArgumentGroup("Tuning");
		group_tuning.addArgument("--split-cutoff")
					.type(int.class)
					.setDefault(1)
					.metavar("CUTOFF")
					.help("Cutoff value for the divide operation");
		group_tuning.addArgument("--merge-cutoff")
					.type(int.class)
					.setDefault(1)
					.metavar("CUTOFF")
					.help("Cutoff value for the conquer operation");
		
		ArgumentGroup group_profiling = argp.addArgumentGroup("Profiling");
		group_profiling.addArgument("--gexf")
					   .metavar("STRING")
					   .help("File name to write gexf-formatted execution data (do not include file extension)");
		
		argp.addArgument("array-length")
			.type(int.class)
			.required(true)
			.metavar("N")
			.help("Amount of numbers to sort (will be [N..1] with --desc)");
		
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
			exception.printStackTrace();
		}
		return null;
	}
}


class CommandLineOptions
{
	@Arg boolean ss;
	@Arg boolean ps;
	@Arg boolean pp;
	@Arg boolean desc;
	@Arg int split_cutoff;
	@Arg int merge_cutoff;
	@Arg int array_length;
	@Arg String gexf;
}
