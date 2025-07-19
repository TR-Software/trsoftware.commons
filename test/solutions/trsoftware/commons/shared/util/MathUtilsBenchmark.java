package solutions.trsoftware.commons.shared.util;

import com.google.gwt.core.shared.GwtIncompatible;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Compares performance of {@link MathUtils} methods.
 *
 * @author Alex
 * @since 2/16/2023
 */
@GwtIncompatible
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 1)
@Fork(value = 1, warmups = 1)
@Measurement(time = 2)
@State(Scope.Thread)
public class MathUtilsBenchmark {

  // Results saved in MathUtilsBenchmark.ods

  @State(Scope.Benchmark)
  public static class LogarithmBenchmarkConfig {
    /**
     * The number of args to test
     */
    @Param({"100", "1000", "10000"})
    int n;

    int[] args;

    @Setup
    public void setUp() {
      Random rnd = new Random(0);
      // generate n random positive ints
      args = IntStream.generate(rnd::nextInt).filter(i -> i > 0).limit(n).toArray();
    }
  }

  /**
   * Tests the performance of the bitwise {@link MathUtils#log2(int)} method
   * vs. the general-purpose {@link MathUtils#log(double, double)} method.
   * @see #log2Double
   */
  @Benchmark
  public void log2Bitwise(LogarithmBenchmarkConfig config, Blackhole blackhole) {
    for (int i : config.args) {
      blackhole.consume(MathUtils.log2(i));
    }
  }

  /**
   * Tests the same calculations as {@link #log2Bitwise} using the general-purpose version {@link MathUtils#log(double, double)}
   */
  @Benchmark
  public void log2Double(LogarithmBenchmarkConfig config, Blackhole blackhole) {
    for (int i : config.args) {
      blackhole.consume(MathUtils.log(2, i));
    }
  }

  /**
   * Same as {@link #log2Double} but casts the results to {@code int}, to have the same result type as {@link MathUtils#log2(int)}
   */
  @Benchmark
  public void log2DoubleInt(LogarithmBenchmarkConfig config, Blackhole blackhole) {
    for (int i : config.args) {
      blackhole.consume((int)MathUtils.log(2, i));
    }
  }

}
