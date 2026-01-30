package solutions.trsoftware.commons.shared.io;

import com.google.common.collect.Iterators;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReferenceArray;


/**
 * Test performance of {@link Base64Encoding} implementations.
 *
 * @author Alex
 * @since 11/5/2025
 */
@BenchmarkMode({Mode.Throughput/*, Mode.AverageTime*/})
@Warmup(iterations = 2)
//@Warmup(iterations = 3)
@Fork(value = 1, warmups = 0)  // don't need any warmup forks
//@Measurement(time = 2)
//@Measurement(time = 5, iterations = 20)
@Measurement(time = 5, iterations = 10)
//@Measurement(time = 2, iterations = 10)
//@Measurement(time = 4, iterations = 10)
//@Measurement(time = 4, iterations = 5)
//@Measurement(time = 4, iterations = 20)
//@Measurement(time = 2, iterations = 2)  // low value for quick testing
@State(Scope.Thread)
//@Threads(10)
public class Base64EncodingBenchmark {

  public enum LookupStrategy {
    /**
     * {@link Base64Encoding#getInstance(boolean, boolean)} using {@link AtomicReferenceArray}
     */
    AtomicRefArr,
    /**
     * {@link Base64Encoding#getInstance(boolean, boolean)} using static "holder" classes
     */
    HolderPattern,
    ;
  }

  @State(Scope.Thread)
  public static class BenchmarkState {
    private static final Random rnd = new Random();

    @Param
    LookupStrategy lookupStrategy;

    @Param(value = "false")
    boolean allPerms = false;

    int nBytes = 14;
    byte[] inputBytes;
    private List<boolean[]> argPermutations;
    private Iterator<boolean[]> argIterator;

    @Setup
    public void setUp(BenchmarkParams params) throws Exception {
      inputBytes = new byte[nBytes];
      rnd.nextBytes(inputBytes);

      // iterator for getInstance arg permutations (url/withoutPadding)
      argPermutations = Arrays.asList(
          new boolean[]{false, false},
          new boolean[]{false, true},
          new boolean[]{true, false},
          new boolean[]{true, true}
      );
      argIterator = Iterators.cycle(argPermutations);
    }

    Base64Encoding nextInstance() {
      if (allPerms) {
        boolean[] args = argIterator.next();
        switch (lookupStrategy) {
          case AtomicRefArr:
            return Base64Encoding.getInstance(args[0], args[1]);
          case HolderPattern:
            return getSingletonInstance(args[0], args[1]);
        }
      }
      else {
        // return a specific instance without any selection logic slowing down HolderPattern
        switch (lookupStrategy) {
          case AtomicRefArr:
            return Base64Encoding.getInstance(true, true);
          case HolderPattern:
            return Base64Encoding.base64UrlNoPadding();
        }
      }
      throw new IllegalStateException();  // should never reach this
    }
  }

  @Benchmark
  @Threads(1)
  public Base64Encoding getInstance(BenchmarkState state) {
    return state.nextInstance();
  }

  @Benchmark
  @Threads(2)
  public Base64Encoding getInstance_Threads_02(BenchmarkState state) {
    return state.nextInstance();
  }

  @Benchmark
  @Threads(5)
  public Base64Encoding getInstance_Threads_05(BenchmarkState state) {
    return state.nextInstance();
  }

  @Benchmark
  @Threads(10)
  public Base64Encoding getInstance_Threads_10(BenchmarkState state) {
    return state.nextInstance();
  }

  @Benchmark
  @Threads(1)
  public String encode(BenchmarkState state) {
    return state.nextInstance().encode(state.inputBytes);
  }

  @Benchmark
  @Threads(2)
  public String encode_Threads_02(BenchmarkState state) {
    return state.nextInstance().encode(state.inputBytes);
  }

  @Benchmark
  @Threads(5)
  public String encode_Threads_05(BenchmarkState state) {
    return state.nextInstance().encode(state.inputBytes);
  }

  @Benchmark
  @Threads(10)
  public String encode_Threads_10(BenchmarkState state) {
    return state.nextInstance().encode(state.inputBytes);
  }



  /**
   * Alternative to the original {@link Base64Encoding#getInstance(boolean, boolean)} implementation,
   * using static instance "holder" classes instead of {@link AtomicReferenceArray}
   * @param url
   * @param withoutPadding
   * @return
   */
  public static Base64Encoding getSingletonInstance(boolean url, boolean withoutPadding) {
    if (url) {
      return withoutPadding ? Holder_Base64Url_NoPadding.INSTANCE : Holder_Base64Url.INSTANCE;
    }
    else {
      return withoutPadding ? Holder_Base64_NoPadding.INSTANCE : Holder_Base64.INSTANCE;
    }
  }

  // instance "holder" classes:

  private static class Holder_Base64 {
    static final Base64Encoding INSTANCE = new Base64EncodingImpl(false, false);
  }

  private static class Holder_Base64_NoPadding {
    static final Base64Encoding INSTANCE = new Base64EncodingImpl(false, true);
  }

  private static class Holder_Base64Url {
    static final Base64Encoding INSTANCE = new Base64EncodingImpl(true, false);
  }

  private static class Holder_Base64Url_NoPadding {
    static final Base64Encoding INSTANCE = new Base64EncodingImpl(true, true);
  }



}
