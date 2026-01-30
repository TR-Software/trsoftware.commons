package solutions.trsoftware.commons.server.util.crypto;

import com.google.common.hash.Hashing;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import solutions.trsoftware.commons.server.util.crypto.mac.ConcurrentMacFunction;
import solutions.trsoftware.commons.server.util.crypto.mac.GuavaHashFunction;
import solutions.trsoftware.commons.server.util.crypto.mac.MacFunctionPrototype;

import javax.crypto.Mac;
import java.util.Random;
import java.util.function.Function;


/**
 * Compares different MAC implementations.
 *
 * @author Alex
 * @since 11/5/2025
 */
@BenchmarkMode({Mode.Throughput/*, Mode.AverageTime*/})
@Warmup(iterations = 2)
//@Warmup(iterations = 3)
//@Fork(value = 1, warmups = 1)
@Fork(value = 1, warmups = 0)  // TODO: experimental disabling warmup forks
//@Measurement(time = 2)
@Measurement(time = 5, iterations = 20)
//@Measurement(time = 5, iterations = 10)
//@Measurement(time = 2, iterations = 10)
//@Measurement(time = 4, iterations = 10)
//@Measurement(time = 4, iterations = 5)
//@Measurement(time = 4, iterations = 20)
//@Measurement(time = 2, iterations = 2)  // low value for quick testing
@State(Scope.Thread)
//@Threads(10)
public class MacFunctionBenchmark {

  public enum MacType {
    // TODO:
    Base(key -> new MacFunctionPrototype("HmacSHA256", key)),
    Local(key -> new LocalMac(key, "HmacSHA256"), false),
//    Synchronized(key -> new SynchronizedMac(key, "HmacSHA256")),
    Concurrent(key -> new ConcurrentMacFunction("HmacSHA256", key)),
    Guava(key -> new GuavaHashFunction(Hashing.hmacSha256(key))),
    ;


    /**
     * Instantiates a new {@link MacFunction} with the given secret key
     */
    private final Function<byte[], MacFunction> instanceCreator;
    private final boolean threadSafe;

    MacType(Function<byte[], MacFunction> instanceCreator) {
      this(instanceCreator, true);
    }

    MacType(Function<byte[], MacFunction> instanceCreator, boolean threadSafe) {
      this.instanceCreator = instanceCreator;
      this.threadSafe = threadSafe;
    }

    public MacFunction createMacFunction(byte[] bytes) {
      return instanceCreator.apply(bytes);
    }
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private static final Random rnd = new Random();

    @Param
    MacType macType;

    byte[] secretKeyBytes;
    MacFunction mac;

//    int nBytes = 23;
    int nBytes = 49;
    byte[] inputBytes;

    @Setup
    public void setUp(BenchmarkParams params) throws Exception {
      int nThreads = params.getThreads();
      if (nThreads > 1 && !macType.threadSafe) {
        throw new RuntimeException(macType + " not supported in multithreaded runs");
      }

      secretKeyBytes = new byte[32];
      rnd.nextBytes(secretKeyBytes);
      mac = macType.createMacFunction(secretKeyBytes);

      inputBytes = new byte[nBytes];
      rnd.nextBytes(inputBytes);
    }
  }

  public byte[] hash(BenchmarkState state) throws Exception {
    // TODO: inline this method and unimplement Function
    return state.mac.hash(state.inputBytes);
  }

  @Benchmark
  @Threads(1)
  public byte[] hash_Threads_01(BenchmarkState state) throws Exception {  // single thread
    return hash(state);
  }

  @Benchmark
  @Threads(2)
  public byte[] hash_Threads_02(BenchmarkState state) throws Exception {  // 2 threads
    return hash(state);
  }

  @Benchmark
  @Threads(5)
  public byte[] hash_Threads_05(BenchmarkState state) throws Exception {  // 5 threads
    return hash(state);
  }

  @Benchmark
  @Threads(10)
  public byte[] hash_Threads_10(BenchmarkState state) throws Exception {  // 10 threads
    return hash(state);
  }


  public static class LocalMac extends MacFunctionPrototype {

    protected final Mac mac;

    public LocalMac(byte[] key, String algorithm) {
      super(algorithm, key);
      mac = super.getMac();
    }

    @Override
    public Mac getMac() {
      return mac;
    }
  }

  public static class SynchronizedMac extends LocalMac {

    public SynchronizedMac(byte[] key, String algorithm) {
      super(key, algorithm);
    }

    @Override  // overriding to make synchronized
    public synchronized byte[] hash(byte[] input, int offset, int len) {
      return super.hash(input, offset, len);
    }
  }


}
