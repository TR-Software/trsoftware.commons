package solutions.trsoftware.commons.shared.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.io.BaseEncoding;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import solutions.trsoftware.commons.shared.io.codec.BigIntRadixCodec;

import java.util.Base64;
import java.util.Random;


/**
 * Compares Java's {@link java.util.Base64} vs. Guava's {@link BaseEncoding}
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
//@Measurement(time = 5, iterations = 10)
//@Measurement(time = 2, iterations = 10)
@Measurement(time = 4, iterations = 10)
//@Measurement(time = 4, iterations = 5)
//@Measurement(time = 4, iterations = 20)
//@Measurement(time = 2, iterations = 2)  // low value for quick testing
@State(Scope.Thread)
//@Threads(10)
@GwtIncompatible("java.util.Base64")
public class Base64Benchmark {
  
  public enum EncoderType {
    /**
     * Java's {@link Base64}
     */
    JAVA,
    /**
     * Guava's {@link BaseEncoding}
     */
    GUAVA,
    /**
     * {@link BigIntRadixCodec#BASE_62}
     */
    BIRC_62
    ;
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {
    private static final Random rnd = new Random();

    @Param
    EncoderType encoderType;

    int nBytes = 14;
    byte[] inputBytes;

    @Setup
    public void setUp(BenchmarkParams params) throws Exception {
      inputBytes = new byte[nBytes];
      rnd.nextBytes(inputBytes);
      // TODO: temp setting 1st byte to 0x01, which gives BIRC.BASE62 a slight advantage if arr starts with a 6-byte timestamp before year 2039)
      inputBytes[0] = 1;
    }
  }

  @Benchmark
  public String encode(BenchmarkState state) throws Exception {
    byte[] bytes = state.inputBytes;
    switch (state.encoderType) {
      case JAVA:
        return Base64.getUrlEncoder().encodeToString(bytes);
      case GUAVA:
        return BaseEncoding.base64Url().encode(bytes);
      case BIRC_62:
        return BigIntRadixCodec.BASE_62.encodeUnsignedBytes(bytes);
    }
    throw new IllegalStateException();  // should never reach this
  }



}
