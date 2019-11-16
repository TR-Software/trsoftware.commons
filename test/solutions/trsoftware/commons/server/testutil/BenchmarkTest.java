package solutions.trsoftware.commons.server.testutil;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.*;

import static solutions.trsoftware.commons.server.testutil.Benchmark.*;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.addFromSupplier;

/**
 * Unit tests for {@link Benchmark}
 *
 * @author Alex
 * @since 10/31/2019
 */
public class BenchmarkTest extends TestCase {

  public void testPrintResults() throws Exception {
    Random rnd = new Random(1234);
    // 1) test the BenchmarkType.format(double) method, which is used by printResults
    // create some random monotonically increasing values starting at 0
    TreeSet<Double> values = new TreeSet<>();
    values.add(0.0);
    // generate some random values between 0 and 1
    addFromSupplier(values, 5, rnd::nextDouble);
    // generate some random values in each thousands range (log scale); e.g. [0,1), [1,1000}, [1000, 1000_000), etc.
    // (this approximates having values in the ranges of nanos, micros, millis, seconds, etc)

    for (int i = 1; i <= 6; i++) {
      double min = Math.pow(1000., i-1);
      double max = Math.pow(1000., i);
      addFromSupplier(values, 5, () -> RandomUtils.nextDoubleInRange(rnd, min, max));
    }

    for (BenchmarkType type : BenchmarkType.values()) {
      // test the formatting on monotonically increasing values starting at 0
      System.out.printf("========== Testing %s.format(double): ==========%n", type);
      for (Double value : values) {
        System.out.printf("%24s -> %32s%n", value, type.format(value));
      }
      System.out.println();
    }
  }

  /**
   * Tests {@link Benchmark.ResultSet#combine(ResultSet, ResultSet)}
   * (and, as a consequence, also {@link Benchmark.Result#add(Result)})
   */
  @Slow
  public void testCombine() throws Exception {
    Benchmark benchmarkRunner = new Benchmark();
    benchmarkRunner.setEqualNumIterations(true);

    ListMultimap<BenchmarkType, ResultSet> resultSets = Multimaps.newListMultimap(
        new EnumMap<>(BenchmarkType.class), ArrayList::new);

    System.out.println("Running benchmarks with " + benchmarkRunner);
    // run the benchmark on tasks that generate primitive and wrapper arrays of various powers of 2
    int runCount = 0;
    for (int i = 10; i < 20; i+=3) {
      runCount++;
      int size = 1 << i;
      List<MemoryTask<?>> tasks = Arrays.asList(
          new GenerateIntArray(size),
          new GenerateIntegerArray(size)
      );
      System.out.println("\nTasks: " + tasks);
      for (BenchmarkType type : BenchmarkType.values()) {
        try (Duration duration = new Duration(type.getPrettyName())) {
          ResultSet resultSet = type == BenchmarkType.TIME
              ? benchmarkRunner.runCpuBenchmarks(tasks)
              : benchmarkRunner.runMemoryBenchmarks(tasks);
          resultSet.printResults();
          resultSets.put(type, resultSet);
        }
      }
    }
    System.out.println();
    System.out.println(StringUtils.repeat('═', 80));
    System.out.printf("Combined results for the %d runs:%n", runCount);
    System.out.println(StringUtils.repeat('═', 80));
    // combine the results from the above runs
    for (BenchmarkType type : resultSets.keySet()) {
      ResultSet combined = resultSets.get(type).stream().reduce(ResultSet::combine).get();
      combined.printResults();
      // TODO: verify some assertions on the combined ResultSet
    }

    fail("TODO"); // TODO: verify some assertions on each combined ResultSet above


  }

  // Some benchmark task examples:

  /**
   * Builds a primitive {@code int[]} of consecutive integers between 0 and the size of the array.
   * This is similar to {@link java.util.Arrays#fill(int[], int)}
   */
  private static class GenerateIntArray implements MemoryTask<int[]> {

    private final int size;
    /**
     * The {@link #get()} method will store its return value in this field, in an attempt to bypass any
     * dead code elimination that might performed by javac or the JVM.
     */
    private int[] result;

    public GenerateIntArray(int size) {
      this.size = size;
    }

    @Override
    public int[] get() {
      int[] ret = new int[size];
      for (int i = 0; i < ret.length; i++) {
        ret[i] = i;
      }
      return this.result = ret;
    }

    @Override
    public String toString() {
      return new StringBuilder("Generate_int[").append(size).append(']').toString();
    }
  }

  /**
   * Builds a {@code Integer[]} of wrappers for consecutive integers between 0 and the size of the array.
   * This is similar to {@link java.util.Arrays#fill(Object[], Object)}
   */
  private static class GenerateIntegerArray implements MemoryTask<Integer[]> {

    private final int size;
    /**
     * The {@link #get()} method will store its return value in this field, in an attempt to bypass any
     * dead code elimination that might performed by javac or the JVM.
     */
    private Integer[] result;

    public GenerateIntegerArray(int size) {
      this.size = size;
    }

    @Override
    public Integer[] get() {
      Integer[] ret = new Integer[size];
      for (int i = 0; i < ret.length; i++) {
        ret[i] = i;
      }
      return this.result = ret;
    }

    @Override
    public String toString() {
      return new StringBuilder("Generate_Integer[").append(size).append(']').toString();
    }
  }
  
  
}