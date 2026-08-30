package solutions.trsoftware.commons.server.util.crypto.aes;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.security.SecureRandom;
import java.util.Random;
import java.util.function.Supplier;

import static solutions.trsoftware.commons.server.util.crypto.aes.benchmark.ConcurrentAESCipherOldImpl.*;


/**
 * Compares different {@link AESCipherImpl} implementations
 *
 * @author Alex
 * @since 11/5/2025
 */
@BenchmarkMode({Mode.Throughput/*, Mode.AverageTime*/})
//@Warmup(iterations = 2)
@Warmup(iterations = 3)
//@Fork(value = 1, warmups = 1)
@Fork(value = 1, warmups = 0)  // TODO: experimental disabling warmup forks
//@Measurement(time = 2)
//@Measurement(time = 5, iterations = 20)
//@Measurement(time = 2, iterations = 10)
//@Measurement(time = 4, iterations = 10)
//@Measurement(time = 4, iterations = 5)
//@Measurement(time = 4, iterations = 20)
//@Measurement(time = 2, iterations = 2)  // low value for quick testing
@Measurement(time = 5, iterations = 10)
@State(Scope.Thread)
//@Threads(10)
public class IvSupplierBenchmark {

  public enum Impl {
    Singleton(SimpleIvSupplier::new),
    ThreadLocal(ThreadLocalIvSupplier::new),
    Pooled(PooledIvSupplier::new),
    ;

    private final Supplier<IvSupplier> constructor;

    Impl(Supplier<IvSupplier> constructor) {
      this.constructor = constructor;
    }
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private static final Random rnd = new Random();

    @Param
    Impl impl;

    int nBytes = 49;  // TODO: make this a @Param?
    //    int nBytes = 23;

    IvSupplier ivSupplier;


    @Setup
    public void setUp(BenchmarkParams params) throws Exception {
      ivSupplier = impl.constructor.get();
    }

    public byte[] generateIV() {
      return ivSupplier.generateIV(nBytes);
    }
  }

  @Benchmark
  @Threads(1)
  public byte[] threads_01(BenchmarkState state) throws Exception {  // single thread
    return state.generateIV();
  }

  @Benchmark
  @Threads(2)
  public byte[] threads_02(BenchmarkState state) throws Exception {  // 2 threads
    return state.generateIV();
  }

  @Benchmark
  @Threads(5)
  public byte[] threads_05(BenchmarkState state) throws Exception {  // 5 threads
    return state.generateIV();
  }

  @Benchmark
  @Threads(10)
  public byte[] threads_10(BenchmarkState state) throws Exception {  // 10 threads
    return state.generateIV();
  }



  public static abstract class IvSupplierImpl implements IvSupplier {
    // TODO: maybe extract as a super for ThreadLocalIvSupplier / PooledIvSupplier (the latter might be trickier)

    @Override
    public byte[] generateIV(int ivLength) {
      return RandomUtils.randBytes(getSecureRandom(), ivLength);
    }

    protected abstract SecureRandom getSecureRandom();
  }

  /**
   * Uses a singleton {@link SecureRandom} instance.
   */
  public static class SimpleIvSupplier extends IvSupplierImpl {
    // NOTE: SecureRandom is thread-safe (see https://stackoverflow.com/a/1461624)
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public SecureRandom getSecureRandom() {
      return secureRandom;
    }
  }



}


