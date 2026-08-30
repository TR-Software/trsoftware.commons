package solutions.trsoftware.commons.server.util.crypto.aes;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import solutions.trsoftware.commons.server.util.crypto.CryptoCipher;
import solutions.trsoftware.commons.server.util.crypto.aes.benchmark.*;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.Base64;
import java.util.function.Function;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESCipherPoolTest.*;


/**
 * Compares different {@link AESCipherImpl} implementations
 *
 * @author Alex
 * @since 11/5/2025
 */
@BenchmarkMode({Mode.Throughput/*, Mode.AverageTime*/})

/*
// slow prod settings (thorough run)
@Fork(3)
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 10, time = 5)
*/

/*
// medium duration settings (less thorough run)
@Fork(2)
@Warmup(iterations = 4, time = 5)
@Measurement(iterations = 6, time = 4)
*/

// quick test settings
@Fork(1)
@Warmup(iterations = 4, time = 2)
@Measurement(iterations = 4, time = 2)

/*
// very quick test settings
@Fork(1)
@Warmup(iterations = 0)
@Measurement(time = 1, iterations = 1)
*/
public class CryptoCipherBenchmark2 {

  public enum CipherType {

    Local_1(key -> new LocalAESCipher(key, AESConstants.Mode.GCM), false),
    Local_2(key -> new LocalAESCipher2(key, AESConstants.Mode.GCM), false),
    Local_2B(key -> new LocalAESCipher2B(key, AESConstants.Mode.GCM), false),
    Local_3(key -> new LocalAESCipher3(key, AESConstants.Mode.GCM), false),
    Local_3A(key -> new LocalAESCipher3A(key, AESConstants.Mode.GCM), false),
    Local_3B(key -> new LocalAESCipher3B(key, AESConstants.Mode.GCM), false),

    /*
    Sync(key -> new SynchronizedAESCipher(key, AESConstants.Mode.GCM)),
    Conc(key -> new ConcurrentAESCipher(key, AESConstants.Mode.GCM)),
    Conc_TLR(key -> new ConcurrentAESCipher(key, AESConstants.Mode.GCM, new ConcurrentAESCipher.ThreadLocalIvSupplier())),
    Conc_PR(key -> new ConcurrentAESCipher(key, AESConstants.Mode.GCM, new ConcurrentAESCipher.PooledIvSupplier())),
    */

    /*Pool_1(key -> new AESCipherPool_LocalAESCipher(key, AESConstants.Mode.GCM)),
    Pool_2(key -> new AESCipherPool_LocalAESCipher2(key, AESConstants.Mode.GCM)),
    Pool_2B(key -> new AESCipherPool_LocalAESCipher2B(key, AESConstants.Mode.GCM)),
    Pool_3(key -> new AESCipherPool_LocalAESCipher3(key, AESConstants.Mode.GCM)),
    Pool_3A(key -> new AESCipherPool_LocalAESCipher3A(key, AESConstants.Mode.GCM)),*/
    Pool_3B(key -> new AESCipherPool_LocalAESCipher3B(key, AESConstants.Mode.GCM)),
    
    /*Pool2_1(key -> new AESCipherPool2_LocalAESCipher(key, AESConstants.Mode.GCM)),
    Pool2_2(key -> new AESCipherPool2_LocalAESCipher2(key, AESConstants.Mode.GCM)),
    Pool2_2B(key -> new AESCipherPool2_LocalAESCipher2B(key, AESConstants.Mode.GCM)),
    Pool2_3(key -> new AESCipherPool2_LocalAESCipher3(key, AESConstants.Mode.GCM)),
    Pool2_3A(key -> new AESCipherPool2_LocalAESCipher3A(key, AESConstants.Mode.GCM)),*/
    Pool2_3B(key -> new AESCipherPool2_LocalAESCipher3B(key, AESConstants.Mode.GCM)),
    
    /*Pool3_1(key -> new AESCipherPool3_LocalAESCipher(key, AESConstants.Mode.GCM)),
    Pool3_2(key -> new AESCipherPool3_LocalAESCipher2(key, AESConstants.Mode.GCM)),
    Pool3_2B(key -> new AESCipherPool3_LocalAESCipher2B(key, AESConstants.Mode.GCM)),
    Pool3_3(key -> new AESCipherPool3_LocalAESCipher3(key, AESConstants.Mode.GCM)),
    Pool3_3A(key -> new AESCipherPool3_LocalAESCipher3A(key, AESConstants.Mode.GCM)),*/
    Pool3_3B(key -> new AESCipherPool3_LocalAESCipher3B(key, AESConstants.Mode.GCM)),

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

    public CryptoCipher newInstance(byte[] secretKey) {
      return constructor.apply(secretKey);
    }
  }

  @State(Scope.Benchmark)
  // Not using Scope.Thread b/c want all threads to contend for the same CryptoCipher instance; same reason for keeping default @Setup(Level.Trial)
  public static class BenchmarkState {

    @Param
    CipherType cipherType;

    byte[] secretKey;
    CryptoCipher cipher;

    //    int nBytes = 23;
    int nBytes = 49;
    byte[] inputBytes;
    byte[] encryptedBytes;  // used for testing decrypt

    @Setup
    public void setUp(BenchmarkParams params) throws Exception {
      // TODO(8/19/2026): fail if working dir not empty? (to avoid overwriting prior results)

      int nThreads = params.getThreads();
      // exclude unsynchronized impl if threads > 1
      if (nThreads > 1 && !cipherType.threadSafe) {
        throw new RuntimeException(cipherType + " not supported in multithreaded runs");
      }

      secretKey = AESConstants.generateKey();
      cipher = cipherType.newInstance(secretKey);
      inputBytes = RandomUtils.randBytes(nBytes);
      encryptedBytes = cipher.encrypt(inputBytes);
    }
  }

  /*
   ================================================================================
   CryptoCipher.encrypt(byte[])
   ================================================================================
   */
  private byte[] encrypt(BenchmarkState state) throws Exception {
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
