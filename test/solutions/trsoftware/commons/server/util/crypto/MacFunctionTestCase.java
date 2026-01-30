package solutions.trsoftware.commons.server.util.crypto;

import solutions.trsoftware.commons.server.testutil.MultithreadedTestHarness;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertEmpty;

/**
 * @author Alex
 * @since 1/13/2026
 */
public abstract class MacFunctionTestCase extends BaseTestCase {

  /**
   * The subset of Mac algorithms defined in the
   * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac">
   *   Java Cryptography Architecture Standard Algorithm Names</a>
   * that are also supported by Guava's {@link com.google.common.hash.Hashing} class
   */
  static String[] MAC_ALGORITHMS = {
      "HmacSHA1",
//      "HmacSHA224",
      "HmacSHA256",
//      "HmacSHA384",
      "HmacSHA512",
      "HmacMD5",
  };

  protected byte[] secretKeyBytes;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    secretKeyBytes = RandomUtils.randBytes(32);
  }

  @Override
  protected void tearDown() throws Exception {
    secretKeyBytes = null;
    super.tearDown();
  }

  // TODO: pull abstract method to super test case:
  protected abstract MacFunction createMacFunction(String algorithm, byte[] keyBytes);

  public void testHash() throws Exception {
    for (String algorithm : MAC_ALGORITHMS) {
      testHash(algorithm);
    }
  }

  private void testHash(String algorithm) throws Exception {
    MacFunction macFunction = createMacFunction(algorithm, secretKeyBytes);
    Mac mac = Mac.getInstance(algorithm);  // will compare MacFunction against raw Mac with same params
    mac.init(new SecretKeySpec(secretKeyBytes, algorithm));

    byte[] input = RandomUtils.randBytes(16);
    assertArraysEqual(mac.doFinal(input), macFunction.hash(input));

    // test the subrange version (taking int offset, int len)
    assertArraysEqual(mac.doFinal(Arrays.copyOfRange(input, 1, 5+1)),
        macFunction.hash(input, 1, 5));
  }

  public void testGetMacLength() throws Exception {
    for (String algorithm : MAC_ALGORITHMS) {
      testGetMacLength(algorithm);
    }
  }

  private void testGetMacLength(String algorithm) throws NoSuchAlgorithmException {
    MacFunction macFunction = createMacFunction(algorithm, secretKeyBytes);
    Mac mac = Mac.getInstance(algorithm);  // will compare MacFunction against raw Mac with same params
    assertEquals(mac.getMacLength(), macFunction.getMacLength());
  }

  /**
   * Tests that the {@link MacFunction} instance returned by {@link #createMacFunction(String, byte[])} can be used
   * concurrently by multiple threads.
   */
  @Slow
  public void testConcurrency() throws Exception {
    String algorithm = "HmacSHA256";  // see https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac
    MacFunction macFunction = createMacFunction(algorithm, secretKeyBytes);
    Mac mac = Mac.getInstance(algorithm);  // will compare MacFunction against raw Mac with same params
    mac.init(new SecretKeySpec(secretKeyBytes, algorithm));
    byte[] input = RandomUtils.randBytes(16);
    byte[] expectedHash = mac.doFinal(input);

    int nThreads = 32;
    int iterationsPerThread = 100_000;
    MultithreadedTestHarness threadRunner = new MultithreadedTestHarness(() -> {
      byte[] result = macFunction.hash(input);
      assertArraysEqual(expectedHash, result);
    });
    long t0 = System.nanoTime();
    Collection<Throwable> exceptions = threadRunner.run(nThreads, iterationsPerThread);
    long t1 = System.nanoTime();
    assertEmpty("Exceptions", exceptions);
    // Note: this throughput is just a rough estimate; see MacFunctionBenchmark for more reliable data
    double opsPerSecond = (double)(nThreads * iterationsPerThread) / TimeUnit.NANOSECONDS.to(TimeUnit.SECONDS, t1 - t0);
    System.out.printf("%s throughput:%n  %,.0f ops/s%n", macFunction, opsPerSecond);
  }


}