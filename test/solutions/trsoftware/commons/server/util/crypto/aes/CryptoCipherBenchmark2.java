package solutions.trsoftware.commons.server.util.crypto.aes;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import solutions.trsoftware.commons.server.util.crypto.CryptoCipher;

import java.util.Base64;
import java.util.Random;
import java.util.function.Function;


/**
 * Compares different {@link AESCipher} implementations
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
@Measurement(time = 5, iterations = 10)
//@Measurement(time = 2, iterations = 10)
//@Measurement(time = 4, iterations = 10)
//@Measurement(time = 4, iterations = 5)
//@Measurement(time = 4, iterations = 20)
//@Measurement(time = 2, iterations = 2)  // low value for quick testing
@State(Scope.Thread)
//@Threads(10)
public class CryptoCipherBenchmark2 {

  public enum CipherType {
    // ****** New implementations: ******
//    CBC_Local1(LocalAESCipherCBC::new, false),
//    CBC_Local2(key -> new LocalAESCipher(key, AESConstants.Mode.CBC), false),
    
//    GCM_Local1(LocalAESCipherGCM::new, false),
//    GCM_Local2(key -> new LocalAESCipher(key, AESConstants.Mode.GCM), false),

    GCM_Local(key -> new LocalAESCipher(key, AESConstants.Mode.GCM), false),
    GCM_Sync(key -> new SynchronizedAESCipher(key, AESConstants.Mode.GCM)),
    GCM_Conc2(key -> new ConcurrentAESCipher(key, AESConstants.Mode.GCM)),
    GCM_Conc2_TLR(key -> new ConcurrentAESCipher(key, AESConstants.Mode.GCM, new ConcurrentAESCipher.ThreadLocalIvSupplier())),
    GCM_Conc2_PR(key -> new ConcurrentAESCipher(key, AESConstants.Mode.GCM, new ConcurrentAESCipher.PooledIvSupplier())),

    ;

    private final Function<byte[], CryptoCipher> constructor;
    private final boolean threadSafe;

    CipherType(Function<byte[], CryptoCipher> constructor) {
      this(constructor, true);
    }

    CipherType(Function<byte[], CryptoCipher> constructor, boolean threadSafe) {
      this.constructor = constructor;
      this.threadSafe = threadSafe;
    }
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private static final Random rnd = new Random();

    @Param
    CipherType cipherType;

    byte[] secretKeyBytes;
    CryptoCipher cipher;

//    int nBytes = 23;
    int nBytes = 49;
    byte[] inputBytes;
    byte[] encryptedBytes;  // used for testing decrypt

    @Setup
    public void setUp(BenchmarkParams params) throws Exception {
      AESConstants.Mode mode;
      int nThreads = params.getThreads();
      // exclude unsynchronized impl if threads > 1
      if (nThreads > 1 && !cipherType.threadSafe) {
        throw new RuntimeException(cipherType + " not supported in multithreaded runs");
      }

      secretKeyBytes = AESCipher.randomKey();
      cipher = cipherType.constructor.apply(secretKeyBytes);
      inputBytes = new byte[nBytes];
      rnd.nextBytes(inputBytes);
      encryptedBytes = cipher.encrypt(inputBytes);
    }
  }

  /*
   ================================================================================
   CryptoCipher.encrypt(byte[])
   ================================================================================
   */
  public byte[] encrypt(BenchmarkState state) throws Exception {
    return state.cipher.encrypt(state.inputBytes);
  }

  @Benchmark
  @Threads(1)
  public byte[] encrypt_Threads_01(BenchmarkState state) throws Exception {  // single thread
    return encrypt(state);
  }

  @Benchmark
  @Threads(2)
  public byte[] encrypt_Threads_02(BenchmarkState state) throws Exception {  // 2 threads
    return encrypt(state);
  }

  @Benchmark
  @Threads(5)
  public byte[] encrypt_Threads_05(BenchmarkState state) throws Exception {  // 5 threads
    return encrypt(state);
  }

  @Benchmark
  @Threads(10)
  public byte[] encrypt_Threads_10(BenchmarkState state) throws Exception {  // 10 threads
    return encrypt(state);
  }

  /*
   ================================================================================
   CryptoCipher.decrypt(byte[])
   ================================================================================
   */
  public byte[] decrypt(BenchmarkState state) throws Exception {
    return state.cipher.decrypt(state.encryptedBytes);
  }

  @Benchmark
  @Threads(1)
  public byte[] decrypt_Threads_01(BenchmarkState state) throws Exception {  // single thread
    return decrypt(state);
  }

  @Benchmark
  @Threads(2)
  public byte[] decrypt_Threads_02(BenchmarkState state) throws Exception {  // 2 threads
    return decrypt(state);
  }

  @Benchmark
  @Threads(5)
  public byte[] decrypt_Threads_05(BenchmarkState state) throws Exception {  // 5 threads
    return decrypt(state);
  }

  @Benchmark
  @Threads(10)
  public byte[] decrypt_Threads_10(BenchmarkState state) throws Exception {  // 10 threads
    return decrypt(state);
  }
  
  
  /*
   ================================================================================
   CryptoCipher.encrypt(byte[], Base64.Encoder)
   ================================================================================
   */
  public String encryptToString(BenchmarkState state) throws Exception {
    return state.cipher.encrypt(state.inputBytes, Base64.getUrlEncoder());
  }

  @Benchmark
  @Threads(1)
  public String encryptToString_Threads_01(BenchmarkState state) throws Exception {  // single thread
    return encryptToString(state);
  }

  @Benchmark
  @Threads(2)
  public String encryptToString_Threads_02(BenchmarkState state) throws Exception {  // 2 threads
    return encryptToString(state);
  }

  @Benchmark
  @Threads(5)
  public String encryptToString_Threads_05(BenchmarkState state) throws Exception {  // 5 threads
    return encryptToString(state);
  }

  @Benchmark
  @Threads(10)
  public String encryptToString_Threads_10(BenchmarkState state) throws Exception {  // 10 threads
    return encryptToString(state);
  }



}
