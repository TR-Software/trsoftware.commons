/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.testutil;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import solutions.trsoftware.commons.shared.util.*;
import solutions.trsoftware.commons.shared.util.compare.RichComparable;
import solutions.trsoftware.commons.shared.util.stats.MaxComparable;
import solutions.trsoftware.commons.shared.util.text.DurationFormat;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple benchmarking facility, supporting both {@linkplain BenchmarkType#TIME CPU} and
 * {@linkplain BenchmarkType#MEMORY memory} benchmarks.
 *
 * TODO: rename as "BenchmarkRunner"
 *
 * TODO: document usage
 *
 * TODO: remove code duplicated with {@link BenchmarkingTestCase}
 *
 * TODO: this duplicates {@link PerformanceComparison}
 *
 *
 * <p style="color: #6495ed; font-weight: bold;">
 *   TODO: consider using <a href="http://openjdk.java.net/projects/code-tools/jmh/">JMH</a> for accurate CPU benchmarks
 * </p>
 * @author Alex
 * @since 1/2/14
 */
public class BenchmarkRunner {

  /**
   * Represents a piece of code to be benchmarked.
   * <p>
   * {@linkplain BenchmarkType#TIME CPU benchmarks} should implement {@link #run()} to execute a single iteration of the
   * code to be benchmarked.
   * <p>
   * {@linkplain BenchmarkType#MEMORY Memory benchmarks} should implement {@link MemoryTask} instead.
   * <p>
   * Either way, subclasses may want to override {@link #getName()}.
   */
  public interface Task extends Runnable {
    /**
     * @implSpec the names of the tasks in each batch should be unique
     * @return the name of this contender
     */
    default String getName() {
      return getClass().getSimpleName();
    }
  }

  /**
   * A {@linkplain BenchmarkType#MEMORY Memory benchmark}: subclasses should implement {@link #get()} to produce a
   * single instance of an object whose size is to be measured.
   * <p>
   * NOTE: this interface provides a default implementation of {@link #run()}, so it can also be used as a
   * {@linkplain BenchmarkType#TIME CPU benchmark}, which allows measuring both the memory usage and time required to
   * create the objects.
   *
   * @param <T> the type of object being produced
   *   TODO: may want to remove the type param, since we don't care about the actual type
   */
  public interface MemoryTask<T> extends Task, Supplier<T> {
    /**
     * Default implementation simply delegates to {@link #get()}
     */
    @Override
    default void run() {
      get();
    }
  }


  public enum BenchmarkType {
    TIME("CPU benchmark") {
      /**
       * Formats the given amount of time in the most human-friendly unit without losing too much precision.
       * @param value time in nanoseconds
       */
      @Override
      public String format(final double value) {
        assert value >= 0;
        /*
        TODO: what we really want to do here is to use the best repr for comparing several values
        (i.e. it won't do if they all have the same repr when they're actually different),
        so this method should ideally take multiple values, and pick a repr unit that ensures they all have a unique string
         */
        /*
        We'll try to use the smallest time unit that's able to represent the value with at most 3 integer and 3 fractional digits
        TODO: extract this code to a util class (either TimeUnit, TimeValue, or TimeUtils, or DurationFormat)
         */
        SharedNumberFormat numberFormat = new SharedNumberFormat("###.###");
        for (TimeUnit unit : TimeUnit.values()) {
          double unitValue = unit.from(TimeUnit.NANOSECONDS, value);
          if (unitValue < 1000)
            return numberFormat.format(unitValue) + " " + unit.abbreviation;
          if (unit == TimeUnit.SECONDS)
            break; // the value is >= 1000 seconds: fall back on our "MM:SS.millis" format
        }
        // if we have a value >= 1000 seconds: fall back on our "MM:SS.millis" format
        // (NOTE: this is very unlikely for a benchmark that was run by this class)
        return DurationFormat.getDefaultInstance(true).format(TimeUnit.NANOSECONDS.toMillis(value));
      }
    },
    MEMORY("Memory benchmark") {
      @Override
      public String format(double value) {
        MemoryUnit unit = MemoryUnit.bestForHuman(value);
        return String.format("%,1.2f %s", unit.fromBytes(value), unit.abbreviation);
      }
    };

    private final String prettyName;

    BenchmarkType(String prettyName) {
      this.prettyName = prettyName;
    }

    public String getPrettyName() {
      return prettyName;
    }

    /** Formats the measurement value as a string */
    public abstract String format(double value);
  }

  public static class Result implements Comparable<Result> {
    /** The type of value represented by the measurement (total time or memory) */
    private BenchmarkType measurementType;
    /** The name of the benchmarked task */
    private String name;
    /** The number of iterations it took to produce this measurement */
    private long iterations;
    /** The benchmark result (total time or memory) */
    private long totalMeasurement;
    /** The measurement per 1000 iterations */
    private double normalizedMeasurement;
    /** The {@link #normalizedMeasurement} expressed as a multiple of the slowest task */
    private double multiplier;

    Result(BenchmarkType benchmarkType, String name, long iterations, long measurement) {
      this.measurementType = benchmarkType;
      this.name = name;
      this.iterations = iterations;
      this.totalMeasurement = measurement;
      normalizeMeasurement();
    }

    private void normalizeMeasurement() {
      normalizedMeasurement = (double)totalMeasurement * 1000 / iterations;
    }

    @Override
    public int compareTo(Result other) {
      return Double.compare(normalizedMeasurement, other.normalizedMeasurement);
    }

    public BenchmarkType getMeasurementType() {
      return measurementType;
    }

    public String getName() {
      return name;
    }

    public long getIterations() {
      return iterations;
    }

    public long getTotalMeasurement() {
      return totalMeasurement;
    }

    public double getNormalizedMeasurement() {
      return normalizedMeasurement;
    }

    public double getMultiplier() {
      return multiplier;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("type", measurementType)
          .add("iterations", String.format("%,d", iterations))
          .add("total", measurementType.format(totalMeasurement))
          .add("normalized", measurementType.format(normalizedMeasurement))
          .add("multiplier", String.format("%,.2f", multiplier))
          .toString();
    }

    /**
     * Creates a new instance representing the sum of this and other.
     * <p>
     * <b>NOTE:</b> Use the {@code static} factory method {@link #combine(Result, Result)} instead.
     */
    public Result add(@Nonnull Result other) {
      assert this.measurementType == other.measurementType;
      if (!Objects.equals(this.name, other.name)) {
        System.err.printf("Attempting to merge results with different names (%s and %s)%n", this.name, other.name);
        this.name += '/' + other.name;
      }
      return new Result(this.measurementType, this.name,
          this.iterations + other.iterations,
          this.totalMeasurement + other.totalMeasurement);
    }

    /**
     * Creates a new instance by "adding" the given results together.
     */
    public static Result combine(Result a, Result b) {
      return a.add(b);
    }
  }

  public static class ResultSet {
    private BenchmarkType benchmarkType;
    /**
     * The individual {@linkplain Task task} results, sorted in natural order
     */
    private ImmutableList<Result> results;

    /**
     * The {@linkplain #results} indexed by {@linkplain Result#getName() name}
     * (this map has the same ordering as {@link #results})
     */
    private ImmutableMap<String, Result> resultsByName;

    ResultSet(BenchmarkType benchmarkType, Collection<Result> results) {
      this.benchmarkType = benchmarkType;
      this.results = ImmutableList.sortedCopyOf(results);
      resultsByName = this.results.stream().collect(ImmutableMap.toImmutableMap(Result::getName, Function.identity()));
      assignMultipliers();
    }

    /**
     * Computes and assigns {@link Result#multiplier} for each entry in the results.
     */
    private void assignMultipliers() {
      if (!results.isEmpty()) {
        Result lowestResult = results.get(0);
        lowestResult.multiplier = 1;
        for (int i = 1; i < results.size(); i++) {
          Result result = results.get(i);
          result.multiplier = result.normalizedMeasurement / lowestResult.normalizedMeasurement;
        }
      }
    }

    public BenchmarkType getBenchmarkType() {
      return benchmarkType;
    }

    public ImmutableList<Result> getResults() {
      return results;
    }

    public ImmutableMap<String, Result> getResultsByName() {
      return resultsByName;
    }

    public Result getResultByName(String key) {
      return resultsByName.get(key);
    }

    /**
     * Prints results to {@link System#out}
     * @return this instance, for call chaining
     */
    public ResultSet printResults() {
      return printResults(System.out);
    }

    /**
     * Prints results to the given stream
     * @return this instance, for call chaining
     */
    public ResultSet printResults(PrintStream out) {
      BenchmarkType type = this.benchmarkType;
      out.printf("%s results%n", type.prettyName);
      // 1) pre-process the results to establish the length of the longest task name (to determine the width of the column)
      // TODO: use something like solutions.trsoftware.commons.server.memquery.output.FixedWidthPrinter to avoid having to specify field widths in printf statements
      MaxComparable<Integer> maxTaskNameLen = new MaxComparable<Integer>();
      for (Result result : results) {
        maxTaskNameLen.update(result.name.length());
      }
      String nameColFormat = "%" + maxTaskNameLen.get() + "s";
      out.printf(nameColFormat + "%14s%13s%14s%11s%n",
                "name", "iterations", "total_" + type.name().toLowerCase(), "per_1000", "multiplier");
      for (Result r : results) {
        out.printf(nameColFormat + "%,14d%13s%14s%11.2f%n",
            r.name, r.iterations, type.format(r.totalMeasurement), type.format(r.normalizedMeasurement), r.multiplier);
      }
      out.println();
      return this;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("type", benchmarkType)
          .add("results", results)
          .toString();
    }

    /**
     * Creates a new instance by merging the two arguments.
     * Can be used as a method reference for {@link java.util.stream.Stream#reduce(BinaryOperator)}
     */
    public static ResultSet combine(ResultSet rs1, ResultSet rs2) {
      BenchmarkType type = rs1.benchmarkType;
      assert type == rs2.benchmarkType;
      // 2) merge the results
      Map<String, Result> mergedResults = new LinkedHashMap<>(rs1.resultsByName);
      MapUtils.mergeAll(mergedResults, rs2.resultsByName, Result::combine);
      return new ResultSet(type, mergedResults.values());
    }
  }


  /**
   * Default value for {@link #targetTimeNanos}: 100 ms
   */
  private static final long TARGET_TIME_NANOS = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(100);
  /**
   * Default value for {@link #targetMemoryDelta}: 40 MB
   */
  private static final long TARGET_MEMORY_DELTA = (long)MemoryUnit.MEGABYTES.toBytes(40);
  // TODO: make the above constants configurable (turn them into instance fields with default values)

  /**
   * The number of iterations for ({@linkplain BenchmarkType#TIME CPU benchmarks})
   * will be computed to approximate this total duration for each test (specified in nanoseconds).
   */
  private long targetTimeNanos = TARGET_TIME_NANOS;

  /**
   * The number of iterations ({@linkplain BenchmarkType#MEMORY memory benchmarks})
   * will be computed to approximate this total amount of memory allocation (specified in bytes).
   */
  private long targetMemoryDelta = TARGET_MEMORY_DELTA;

  /**
   * If {@code true}, will run each task for the same number of iterations.
   * Otherwise will run each task only long-enough to satisfy the minimums.
   * NOTE: setting this flag to {@code false} will save time, but the {@linkplain Result#getTotalMeasurement() total
   * time/memory measurements} will not be mutually comparable between the {@linkplain Result results} for the different
   * tasks (but that's not really a problem, because the reported {@linkplain Result#getMultiplier() multiplier} on which
   * the results are ranked is derived based on the {@linkplain Result#getNormalizedMeasurement()
   * normalized measurement per 100 iterations}).
   *
   * @see #targetTimeNanos
   * @see #targetMemoryDelta
   */
  private boolean equalNumIterations = false;

  /**
   * If {@code true}, will reuse partial measurements taken while computing the total number of required iterations.
   * In other words, will run all tasks for a number of iterations, then if more iterations are needed, will run them
   * again for an additional number iterations, and so on, until the target is reached
   * (see {@link #targetTimeNanos} and {@link #targetMemoryDelta}).  The measurements from each step will be added
   * together at the end to produce the final results.
   * This can save a lot of time, but may produce less-accurate because the tasks will be interleaved.
   * <p>
   * Otherwise will use only the final measurement produced by running each task (sequentially) for the exact number
   * of iterations required (after running them for a smaller numbers of iterations on each step to determine the
   * optimal number of iterations needed to produce the final result).
   *
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: this setting applies only when {@link #equalNumIterations} is also enabled
   * </p>
   */
  private boolean incremental = true;

  /**
   * Will print debugging output to this stream if not {@code null}
   */
  private PrintStream debug = null;


  public long getTargetTimeNanos() {
    return targetTimeNanos;
  }

  /**
   * @param targetTimeNanos value for {@link #targetTimeNanos} (in nanoseconds)
   * @return this instance, for call chaining
   */
  public BenchmarkRunner setTargetTimeNanos(long targetTimeNanos) {
    this.targetTimeNanos = targetTimeNanos;
    return this;
  }

  public long getTargetMemoryDelta() {
    return targetMemoryDelta;
  }

  /**
   * @param targetMemoryDelta value for {@link #targetMemoryDelta} (in bytes)
   * @return this instance, for call chaining
   */
  public BenchmarkRunner setTargetMemoryDelta(long targetMemoryDelta) {
    this.targetMemoryDelta = targetMemoryDelta;
    return this;
  }

  public boolean isEqualNumIterations() {
    return equalNumIterations;
  }

  /**
   * @param equalNumIterations value for {@link #equalNumIterations}
   * @return this instance, for call chaining
   */
  public BenchmarkRunner setEqualNumIterations(boolean equalNumIterations) {
    this.equalNumIterations = equalNumIterations;
    return this;
  }

  public PrintStream getDebug() {
    return debug;
  }

  /**
   * Can pass a print stream for debug-level messages, if desired.  Pass {@code null} to disable debugging output.
   * @param debug unless {@code null}, will print debugging messages to this stream
   * @return this instance, for call chaining
   */
  public BenchmarkRunner setDebug(PrintStream debug) {
    this.debug = debug;
    return this;
  }

  public boolean isIncremental() {
    return incremental;
  }

  /**
   * @param incremental value for {@link #incremental}
   * @return this instance, for call chaining
   */
  public BenchmarkRunner setIncremental(boolean incremental) {
    this.incremental = incremental;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("targetTimeNanos", BenchmarkType.TIME.format(targetTimeNanos))
        .add("targetMemoryDelta", BenchmarkType.MEMORY.format(targetMemoryDelta))
        .add("equalNumIterations", equalNumIterations)
        .add("incremental", incremental)
        .toString();
  }

  /**
   * Runs all benchmarks on the given tasks, and prints the results.
   * @return the result sets for each {@linkplain BenchmarkType benchmark type}
   */
  public EnumMap<BenchmarkType, ResultSet> runAllBenchmarks(List<? extends Task> tasks) {
    // TODO: rewrite this using the new methods run[Cpu|Memory]Benchmarks
    // 1) run the benchmarks
    EnumMap<BenchmarkType, List<Result>> results = new EnumMap<>(BenchmarkType.class);
    for (Task task : tasks) {
      results.computeIfAbsent(BenchmarkType.TIME, LIST_FACTORY).add(runCpuBench(task));
      if (task instanceof MemoryTask) {
        results.computeIfAbsent(BenchmarkType.MEMORY, LIST_FACTORY).add(runMemoryBench((MemoryTask)task));
      }
    }
    // 2) construct the ResultSet for each entry
    EnumMap<BenchmarkType, ResultSet> resultSets = new EnumMap<>(BenchmarkType.class);
    for (Map.Entry<BenchmarkType, List<Result>> entry : results.entrySet()) {
      BenchmarkType type = entry.getKey();
      ResultSet resultSet = new ResultSet(type, entry.getValue());
      resultSets.put(type, resultSet);
    }
    // 3) print the results
    resultSets.values().forEach(ResultSet::printResults);
    return resultSets;
  }


  public <T extends Task> ResultSet runCpuBenchmarks(List<T> tasks) {
    if (equalNumIterations) {
      // use the new algorithm that runs all tasks incrementally
      @SuppressWarnings("unchecked")
      TaskRunner runner = new CpuTaskRunner((List<Task>)tasks, targetTimeNanos);
      return runner.runBenchmarks();
    }
    else {
      // use the old algorithm that runs each task sequentially
      List<Result> results = new ArrayList<>(tasks.size());
      for (Task task : tasks) {
        results.add(runCpuBench(task));
      }
      return new ResultSet(BenchmarkType.TIME, results);
      // NOTE: could use the following stream version to run the tests in parallel, but that would probably taint the results
      // results = tasks.parallelStream().map(this::runCpuBench).collect(Collectors.toCollection(TreeSet::new));
    }
    // TODO: get rid of the code duplication between the above 2 branches, to reuse the same code for running memory benchmarks
  }


  public <T extends MemoryTask> ResultSet runMemoryBenchmarks(List<T> tasks) {
    if (equalNumIterations) {
      // use the new algorithm that runs all tasks incrementally
      @SuppressWarnings("unchecked")
      TaskRunner runner = new MemoryTaskRunner((List<MemoryTask>)tasks, targetTimeNanos);
      return runner.runBenchmarks();
    }
    else {
      // use the old algorithm that runs each task sequentially
      List<Result> results = new ArrayList<>(tasks.size());
      for (T task : tasks) {
        results.add(runMemoryBench(task));
      }
      return new ResultSet(BenchmarkType.MEMORY, results);
      // NOTE: could use the following stream version to run the tests in parallel, but that would probably taint the results
      // results = tasks.parallelStream().map(this::runCpuBench).collect(Collectors.toCollection(TreeSet::new));
    }
    // TODO: get rid of the code duplication between the above 2 branches, to reuse the same code for running memory benchmarks
  }


  /**
   * Runs a set of benchmarking tasks {@linkplain Task tasks} for the optimal number of iterations, such that
   * 1. each task runs for at least some min amount of time ({@linkplain BenchmarkType#TIME CPU benchmarks})
   *    or uses at least some min amount of memory ({@linkplain BenchmarkType#MEMORY memory benchmarks})
   * 2. all tasks run the same number of iterations
   */
  private abstract class TaskRunner<T extends Task> {
    private final BenchmarkType type;
    /**
     * The number of iterations will be increased incrementally until this minimum measurement is satisfied for all tasks
     */
    private final long target;
    private List<T> tasks;
    private PriorityQueue<PQEntry> pq = new PriorityQueue<>();  // partial results prioritized by lowest time
    private long totalIterationCount = 0; // for debugging: the number of iterations that were actually run

    TaskRunner(BenchmarkType type, List<T> tasks, long measurementTarget) {
      this.type = type;
      this.tasks = tasks;
      this.target = measurementTarget;
    }

    /**
     * Run the given task for the given number of iterations and return the total measurement.
     * @return the time (nanos) if this is a {@linkplain BenchmarkType#TIME CPU benchmark}
     * or amount of memory (bytes) if this is a ({@linkplain BenchmarkType#MEMORY memory benchmark}.
     */
    final long runTask(T task, long iterations) {
      totalIterationCount += iterations;
      return doRunTask(task, iterations);
    }

    /**
     * Run the given task for the given number of iterations and return the total measurement.
     * @return the time (nanos) if this is a {@linkplain BenchmarkType#TIME CPU benchmark}
     * or amount of memory (bytes) if this is a ({@linkplain BenchmarkType#MEMORY memory benchmark}.
     */
    abstract long doRunTask(T task, long iterations);

    /**
     * @return a task that doesn't do anything (its measurement will be subtracted from that of an actual task)
     */
    abstract T getNoOpTask();

    ResultSet runBenchmarks() {
      /*
      We want to run all tasks for the same number of iterations, such that each tasks runs for >= min amount of time
      or memory.
      */
      long iterations = 32;  // start with a small number of iterations, and add more later if needed TODO: allow configuring a different starting value (like targetTimeNanos)
      // We begin by filling the priority queue with the results of running a small number of iterations
      fillQueue(iterations);
      // now keep adding more iterations (if needed) until the threshold measurement is attained for all tasks
      int stepCounter = 0;  // the number of times the loop body has been executed
      PQEntry lowestEntry;
      while ((lowestEntry = pq.peek()).measurement < target) {
        if (debug != null)
          debug.printf("DEBUG: (%2d) results after %,d iterations: %s%n",
              stepCounter, iterations, CollectionUtils.sortedCopy(pq));
        long lowestMeasurement = lowestEntry.measurement;
        stepCounter++;
        // increase the number of iterations and re-run all tasks
        long additionalIterations = iterations;  // by default we double the number of iterations at each step
        // if the current lowest measurement is at least 10% of what we need, use a more intelligent estimate
        if (lowestMeasurement >= target / 10) {
          // estimate the number of iterations remaining by solving the following equation for N:
          // currentValue/iterations = targetValue/N
          // -> currentValue * N = targetValue * iterations   ("cross-multiply")
          // -> N = (targetValue * iterations) / currentValue
          long estimatedTotalIterations = (long)Math.ceil((double)iterations * target / lowestMeasurement);
          additionalIterations = Math.max(10L, estimatedTotalIterations - iterations); // do at least 10 more (to make sure we're making progress at each step)
          if (debug != null)
            debug.printf("DEBUG: (%2d) estimating will need %,d iterations to reach %s (%,d iterations ran so far to reach %s)%n",
                stepCounter, estimatedTotalIterations, type.format(target), iterations, type.format(lowestMeasurement));
        }
        if (debug != null)
          debug.printf("DEBUG: (%2d) adding %,d more iterations (%,d iterations ran so far to reach %s)%n",
              stepCounter, additionalIterations, iterations, type.format(lowestMeasurement));
        // TODO: temp experiment - figure out which of the following implementations is faster / more accurate
        if (incremental) {
          addMoreIterations(additionalIterations);
          iterations += additionalIterations;
        }
        else {
          iterations += additionalIterations;
          pollAll();
          fillQueue(iterations);
        }
      }
      // get a measurement of running the same number of iterations of an empty task
      long discount = runTask(getNoOpTask(), iterations);
      // TODO: make sure the discount is less than any given measurement, otherwise the results will be negative
      if (debug != null)
        debug.printf("DEBUG: finished %,d iterations of each task; applying discount of %s to each result%n", iterations, type.format(discount));
      // create the result set, applying the above discount to each result
      List<Result> results = new ArrayList<>();
      while (!pq.isEmpty()) {
        PQEntry entry = pq.poll();
        assert entry.iterations == iterations;
        results.add(new Result(type, entry.task.getName(), iterations, entry.measurement - discount));
      }
      if (debug != null)
        debug.printf("DEBUG: actual iteration count per task: %,d%n",
            totalIterationCount / (tasks.size() + 1 /*adding 1 to include the no-op task*/));
      return new ResultSet(type, results);
    }

    /**
     * Fills the {@link #pq} with the results of running all the tasks for the given number of iterations.
     */
    private void fillQueue(long iterations) {
      for (T task : tasks) {
        pq.offer(new PQEntry(task, iterations));
      }
    }

    private void addMoreIterations(long additionalIterations) {
      // TODO: experimental
      for (PQEntry entry : pollAll()) {
        entry.increment(additionalIterations);
        pq.offer(entry);
      }
    }

    /**
     * Removes all entries from {@link #pq} by repeatedly invoking {@link PriorityQueue#poll()} until it's empty.
     * @return the removed entries, in priority order
     */
    private List<PQEntry> pollAll() {
      ArrayList<PQEntry> ret = new ArrayList<>();
      while (!pq.isEmpty())
        ret.add(pq.poll());
      return ret;
    }

    /**
     * Represents a partial measurement on a particular task.
     * Intended to be used with a {@link PriorityQueue} to help come up with an optimal number of iterations.
     *
     * TODO: probably don't need a separate class for this (can probably just use {@link Result})
     */
    private class PQEntry implements RichComparable<PQEntry> {
      private T task;
      private long iterations;
      /** The partial measurement (total time or memory) */
      private long measurement;

      private PQEntry(T task, long iterations) {
        this.task = task;
        this.iterations = iterations;
        measurement = runTask(task, iterations);
      }

      void increment(long additionalIterations) {
        measurement += runTask(task, additionalIterations);
        iterations += additionalIterations;
      }

      @Override
      public int compareTo(@NotNull PQEntry other) {
        return Long.compare(this.measurement, other.measurement);
      }

      @Override
      public String toString() {
        return String.format("%s{%,d, %s}", task.getName(), iterations, type.format(measurement));
      }
    }
  }

  private class CpuTaskRunner extends TaskRunner<Task> {

    CpuTaskRunner(List<Task> tasks, long measurementTarget) {
      super(BenchmarkType.TIME, tasks, measurementTarget);
    }

    @Override
    long doRunTask(Task task, long iterations) {
      return measureTime(task, iterations);
    }

    @Override
    Task getNoOpTask() {
      return NO_OP;
    }

  }

  private class MemoryTaskRunner extends TaskRunner<MemoryTask> {

    MemoryTaskRunner(List<MemoryTask> tasks, long measurementTarget) {
      super(BenchmarkType.MEMORY, tasks, measurementTarget);
    }

    @Override
    long doRunTask(MemoryTask task, long iterations) {
      return measureMemoryUsage(task, iterations);
    }

    @Override
    MemoryTask getNoOpTask() {
      return NO_OP;
    }

  }


  private static final Function<BenchmarkType, List<Result>> LIST_FACTORY = x -> new ArrayList<>();


  /**
   * A task that doesn't do anything or return anything (will be used to determine the overhead of the harnessing code).
   */
  private static final MemoryTask NO_OP = new MemoryTask<Void>() {
    @Override
    public String getName() {
      return "NO_OP";
    }
    @Override
    public void run() {}
    @Override
    public Void get() {return null;}
  };

  private Result runCpuBench(Task task) {
    // figure out how many iterations of the task to run in order to adequately test its CPU usage
    int iterations = 32;
    measureTime(task, iterations, true); // "warm up" the task before doing any measurement (so the JIT optimizations kick in)
    long totalNanos;
    do {
      iterations *= 2; // double the number of iterations attempted each time
      totalNanos = measureTime(task, iterations);
    } while (totalNanos < targetTimeNanos);
    // subtract the time it takes to run an equal number of iterations of an empty task
    totalNanos -= measureTime(NO_OP, iterations, true);
    // at this point totalNanos represents an accurate measurement of an adequate number of iterations of this task
    return new Result(BenchmarkType.TIME, task.getName(), iterations, totalNanos);
  }

  private Result runMemoryBench(MemoryTask task) {
    // figure out how many iterations of the task to run in order to adequately test memory usage
    int iterations = 32;
    long memoryDelta;
    do {
      iterations *= 2; // double the number of iterations attempted each time
      memoryDelta = measureMemoryUsage(task, iterations);
    }
    while (memoryDelta < targetMemoryDelta);
    // subtract the memory used by  an equal number of iterations of an empty task
    memoryDelta -= measureMemoryUsage(NO_OP, iterations);
    // at this point memoryDelta represents a pretty accurate measurement of the task's memory consumption
    return new Result(BenchmarkType.MEMORY, task.getName(), iterations, memoryDelta);
  }

  private static long getHeapMemoryBytesUsed() {
    return TestUtils.calcSystemMemoryUsage(true);
  }

  /**
   * @return the time (in nanos) taken to execute the given number of iterations of the given task.
   */
  private static long measureTime(Task task, long iterations) {
    long start = System.nanoTime();
    for (long i = 0; i < iterations; i++) {
      task.run();
    }
    long end = System.nanoTime();
    return end - start;
  }

  /**
   * @param warmUp if {@code true}, will do a "warm-up" run first, before recording the actual measurement.
   * @return the time (in nanos) taken to execute the given number of iterations of the given task
   */
  private static long measureTime(Task task, int iterations, boolean warmUp) {
    if (warmUp)
      measureTime(task, iterations); // "warm up" the task before doing any measurement (so the JIT optimizations kick in)
    return measureTime(task, iterations);
  }

  /**
   * @return The difference in heap usage (in bytes) after collecting the results from the given number of iterations of
   * the given task into a LinkedList and the usage before.
   */
  private static long measureMemoryUsage(MemoryTask task, long iterations) {
    long bytesUsedBefore = getHeapMemoryBytesUsed();
    // we use LinkedList instead of ArrayList to make time and memory usage proportional to the number of objects inserted
    LinkedList<Object> results = new LinkedList<Object>();
    for (long i = 0; i < iterations; i++) {
      results.push(task.get());
    }
    long bytesUsedAfter = getHeapMemoryBytesUsed();
    // we run the verification step for 2 reasons: 1) to check that there are no null results, and 2) so that
    // we can be sure that some aggressive compiler optimization doesn't discard the reference to results until the
    // memory measurement has been done
    verifyTaskResults(task, results);
    return bytesUsedAfter - bytesUsedBefore;
  }

  private static void verifyTaskResults(Task task, List<Object> results) {
    for (Object result : results)
      verifyTaskResult(task, result);
  }

  private static void verifyTaskResult(Task task, Object result) {
    assert result != null || task == NO_OP;
  }


}
