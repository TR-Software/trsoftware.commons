/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.testutil;

import junit.framework.TestCase;
import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.MemoryUnit;
import solutions.trsoftware.commons.shared.util.callables.Function0;
import solutions.trsoftware.commons.shared.util.stats.MaxComparable;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Alex, 1/2/14 n
 */
public abstract class BenchmarkingTestCase extends TestCase {

  /** Subclasses must implement this interface for each piece of code to be benchmarked */
  protected interface Task {
    /** Returns the name of the "contestant" */
    String getName();
    /** Executes a single instance of the code to be benchmarked */
    Object call(BenchmarkType benchType) throws Exception;
  }

  /** Subclasses must implement this method to produce the set of tasks to be measured */
  protected abstract List<? extends Task> makeTasks();

  public enum BenchmarkType {
    TIME("CPU benchmarks") {
      @Override
      public String format(double value) {
        long valueMillis = TimeUnit.NANOSECONDS.toMillis((long)value);
        if (valueMillis < 1000)
          return String.format("%d ms", valueMillis); // simply print the number of milliseconds
        else
          return Duration.formatAsClockTime(valueMillis, true); // print "MM:SS.millis"
      }
    },
    MEMORY("Memory benchmarks") {
      @Override
      public String format(double value) {
        MemoryUnit unit = MemoryUnit.bestForHuman(value);
        return String.format("%,1.2f %s", unit.fromBytes(value), unit.abbreviation);
      }
    };

    private String benchmarkName;

    BenchmarkType(String benchmarkName) {
      this.benchmarkName = benchmarkName;
    }

    /** Formats the measurement value as a string */
    public abstract String format(double value);
  }

  public static class BenchmarkResult implements Comparable<BenchmarkResult> {
    /** The name of the benchmarked task */
    private String name;
    /** The number of iterations it took to produce a measurement */
    private int iterations;
    /** The benchmark result (total time or memory) */
    private long measurement;
    /** The measurement per 1500 iterations */
    private double normalizedMeasurement;
    /** The type of value represented by the measurement (total time or memory) */
    private BenchmarkType measurementType;
    /** The normalizedMeasurement expressed as a multiple of the slowest task */
    private double multiplier;

    public BenchmarkResult(String name, int iterations, long measurement, BenchmarkType benchmarkType) {
      this.name = name;
      this.iterations = iterations;
      this.measurement = measurement;
      this.measurementType = benchmarkType;
      normalizedMeasurement = (double)measurement * 1000 / iterations;
    }

    @Override
    public int compareTo(BenchmarkResult other) {
      int cmp = Double.compare(normalizedMeasurement, other.normalizedMeasurement);
      if (cmp == 0)
        cmp = name.compareTo(other.name); // use the name as a tie-breaker
      return cmp;
    }
  }

  // TODO: separate this into 2 separate tests: 1 for cpu and 1 for memory, because the memory test can take a very long time, and it might not be needed every time
  // TODO: (or could just have an abstract method that decides whether to run the memory benchmarks)

  @Slow
  public void testBenchmarks() throws Exception {
    List<? extends Task> tasks = makeTasks();

    // TODO: use an overrideable method to check that the tasks all produce the same value

    // 1) Run the benchmarks
    EnumMap<BenchmarkType, SortedSet<BenchmarkResult>> results = new EnumMap<BenchmarkType, SortedSet<BenchmarkResult>>(BenchmarkType.class);
    for (Task task : tasks) {
      MapUtils.getOrInsert(results, BenchmarkType.TIME, SORTED_SET_FACTORY).add(runCpuBench(task));
      MapUtils.getOrInsert(results, BenchmarkType.MEMORY, SORTED_SET_FACTORY).add(runMemoryBench(task));
    }

    // 2) compute the multiplier for each result
    for (BenchmarkType type : BenchmarkType.values()) {
      SortedSet<BenchmarkResult> resultSet = results.get(type);
      BenchmarkResult lowestResult = resultSet.first();
      for (BenchmarkResult result : resultSet) {
        if (result == lowestResult)
          result.multiplier = 1;
        else
          result.multiplier = result.normalizedMeasurement / lowestResult.normalizedMeasurement;
      }
    }

    // 3) print the results
    printResults(results);
  }

  public static void printResults(EnumMap<BenchmarkType, SortedSet<BenchmarkResult>> results) {
    // TODO: optionally take a custom output stream (as an overrideable method)
    PrintStream out = System.out;
    // 1) pre-process the results to establish the length of the longest task name
    MaxComparable<Integer> maxTaskNameLen = new MaxComparable<Integer>();
    for (BenchmarkResult result : CollectionUtils.concatIter(results.values())) {
      maxTaskNameLen.update(result.name.length());
    }

    String nameColFormat = "%" + maxTaskNameLen.get() + "s";
    for (BenchmarkType type : BenchmarkType.values()) {
      SortedSet<BenchmarkResult> resultSet = results.get(type);
      out.printf("%s results%n", type.benchmarkName);
      out.printf(nameColFormat + "%10s%13s%10s%11s%n",
          "name", "run count", "total " + type.name().toLowerCase(), "per 1000", "multiplier");
      for (BenchmarkResult r : resultSet) {
        out.printf(nameColFormat + "%,10d%13s%10s%11.2f%n",
            r.name, r.iterations, type.format(r.measurement), type.format(r.normalizedMeasurement), r.multiplier);
      }
      out.println();
    }
  }

  /** The number of iterations will be computed to approximate this total duration for each test */
  private static final long TARGET_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(100);
  private static final long TARGET_MEMORY_DELTA = (long)MemoryUnit.MEGABYTES.toBytes(40);
  // TODO: make the above constants configurable (turn them into instance fields with default values)


  private static final Function0<SortedSet<BenchmarkResult>> SORTED_SET_FACTORY = new Function0<SortedSet<BenchmarkResult>>() {
    @Override
    public SortedSet<BenchmarkResult> call() {
      return new TreeSet<BenchmarkResult>();
    }
  };

  private static final Task EMPTY_TASK = new Task() {
    @Override
    public String getName() {
      return "EMPTY_TASK";
    }

    @Override
    public Object call(BenchmarkType benchType) throws Exception {
      return null;
    }
  };

  private static BenchmarkResult runCpuBench(Task task) throws Exception {
    // figure out how many iterations of the task to run in order to adequately test its CPU usage
    int iterations = 32;
    measureTime(task, iterations, true); // "warm up" the task before doing any measurement (so the JIT optimizations kick in)
    long totalNanos;
    do {
      iterations *= 2; // double the number of iterations attempted each time
      totalNanos = measureTime(task, iterations);
    } while (totalNanos < TARGET_TIME_NANOS);
    // subtract the time it takes to run an equal number of iterations of an empty task
    totalNanos -= measureTime(EMPTY_TASK, iterations, true);
    // at this point totalNanos represents an accurate measurement of an adequate number of iterations of this task
    return new BenchmarkResult(task.getName(), iterations, totalNanos, BenchmarkType.TIME);
  }

  private static BenchmarkResult runMemoryBench(Task task) throws Exception {
    // figure out how many iterations of the task to run in order to adequately test memory usage
    int iterations = 32;
    long memoryDelta;
    do {
      iterations *= 2; // double the number of iterations attempted each time
      memoryDelta = measureMemoryUsage(task, iterations);
    }
    while (memoryDelta < TARGET_MEMORY_DELTA);
    // subtract the memory used by  an equal number of iterations of an empty task
    memoryDelta -= measureMemoryUsage(EMPTY_TASK, iterations);
    // at this point memoryDelta represents a pretty accurate measurement of the task's memory consumption
    return new BenchmarkResult(task.getName(), iterations, memoryDelta, BenchmarkType.MEMORY);
  }

  private static long getHeapMemoryBytesUsed() {
    return TestUtils.calcSystemMemoryUsage(2);
  }

  /**
   * @return The difference in heap usage (in bytes) after collecting the results from the given number of iterations of
   * the given task into a LinkedList and the usage before.
   */
  private static long measureTime(Task task, int iterations) throws Exception {
    long start = System.nanoTime();
    Object result = null;
    for (int i = 0; i < iterations; i++) {
      result = task.call(BenchmarkType.TIME);
    }
    long end = System.nanoTime();
    // we check the following assertion for 2 reasons: 1) to check that there are no null results, and 2) so that
    // we can be sure that some super aggressive compiler optimization doesn't skip some of the iterations
    verifyTaskResult(task, result);
    long timeDelta = end - start;
//    System.out.printf("time('%s', %d) = %,d ns%n", task.getName(), iterations, timeDelta);
    return timeDelta;
  }

  /**
   * @return The difference in heap usage (in bytes) after collecting the results from the given number of iterations of
   * the given task into a LinkedList and the usage before.
   */
  private static long measureTime(Task task, int iterations, boolean warmUp) throws Exception {
    if (warmUp)
      measureTime(task, iterations); // "warm up" the task before doing any measurement (so the JIT optimizations kick in)
    return measureTime(task, iterations);
  }

  /**
   * @return The difference in heap usage (in bytes) after collecting the results from the given number of iterations of
   * the given task into a LinkedList and the usage before.
   */
  private static long measureMemoryUsage(Task task, int iterations) throws Exception {
    long bytesUsedBefore = getHeapMemoryBytesUsed();
    // we use LinkedList instead of ArrayList to make time and memory usage proportional to the number of objects inserted
    LinkedList<Object> results = new LinkedList<Object>();
    for (int i = 0; i < iterations; i++) {
      results.push(task.call(BenchmarkType.MEMORY));
    }
    long bytesUsedAfter = getHeapMemoryBytesUsed();
    // we run the verification step for 2 reasons: 1) to check that there are no null results, and 2) so that
    // we can be sure that some aggressive compiler optimization doesn't discard the reference to results until the
    // memory measurement has been done
    verifyTaskResults(task, results);
    return bytesUsedAfter - bytesUsedBefore;
  }

  private static void verifyTaskResults(Task task, LinkedList<Object> results) {
    for (Object result : results)
      verifyTaskResult(task, result);
  }

  private static void verifyTaskResult(Task task, Object result) {
    if (task != EMPTY_TASK)
      assertNotNull(result);
    else
      assertNull(result);
  }


}
