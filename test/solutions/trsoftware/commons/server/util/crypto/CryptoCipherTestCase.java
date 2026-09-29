package solutions.trsoftware.commons.server.util.crypto;

import com.google.common.collect.ImmutableMultiset;
import solutions.trsoftware.commons.server.testutil.MultithreadedTestHarness;
import solutions.trsoftware.commons.server.testutil.TestUtils;
import solutions.trsoftware.commons.server.util.RuntimeUtils;
import solutions.trsoftware.commons.server.util.crypto.aes.AESConstants;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.io.StringPrintStream;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.function.ThrowingRunnable;
import solutions.trsoftware.commons.shared.util.time.Stopwatch;

import java.lang.reflect.Constructor;
import java.security.GeneralSecurityException;
import java.util.*;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.TestUtils.sectionHeader;

/**
 * @author Alex
 * @since 12/5/2025
 */
public abstract class CryptoCipherTestCase extends BaseTestCase {

  protected byte[] secretKeyBytes;

  /** Can set this to {@code false} to omit all print statements */
  protected boolean verbose;
  private byte[] randomBytes;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    secretKeyBytes = AESConstants.generateKey();
    verbose = true;
  }

  @Override
  protected void tearDown() throws Exception {
    secretKeyBytes = null;
    super.tearDown();
  }

  /**
   * Tests encryption and decryption using the given cipher instance.
   *
   * @param cipher the cipher instance to test
   * @param plaintext argument for {@link CryptoCipher#encrypt(byte[])}
   * @return the return value of {@link CryptoCipher#encrypt(byte[])} for the given bytes
   * @throws GeneralSecurityException if thrown by cipher instance
   */
  protected byte[] testEncryption(CryptoCipher cipher, byte[] plaintext) throws GeneralSecurityException {
    byte[] encrypted = cipher.encrypt(plaintext);
    assertFalse(Arrays.equals(plaintext, encrypted));
    if (verbose) {
      System.out.printf("%s.encrypt(byte[%d]=0x[%s]) ->%n  byte[%d]=0x[%s]%n", cipher.getClass().getSimpleName(),
          plaintext.length, StringUtils.byteArrayToHex(plaintext, 4),
          encrypted.length, StringUtils.byteArrayToHex(encrypted, 4));
    }
    byte[] decrypted = cipher.decrypt(encrypted);
    assertArraysEqual(plaintext, decrypted);
    assertFalse(Arrays.equals(plaintext, encrypted));

    // test the overloaded encryption methods: e.g. encrypt(byte[], Base64.Encoder), encryptStringUtf8(String)
    assertArraysEqual(plaintext, cipher.decrypt(cipher.encrypt(plaintext, 0, plaintext.length)));
    String base64 = cipher.encrypt(plaintext, Base64.getUrlEncoder());
    if (verbose) System.out.printf("   b64[%d]=%s%n", base64.length(), base64);
    assertArraysEqual(plaintext, cipher.decrypt(base64, Base64.getUrlDecoder()));
    // also test the String shortcut methods (encryptStringUtf8 + decryptStringUtf8)
    assertEquals("foobar", cipher.decryptStringUtf8(cipher.encryptStringUtf8("foobar")));

    return encrypted;
  }

  /**
   * Invokes {@link #testEncryption(CryptoCipher, byte[])} for each of the given ciphers,
   * performing multiple iterations to be sure that no errors arise from reusing the same instance
   * (e.g. reusing the same IV for subsequent AES encryption operations)
   */
  protected <C extends CryptoCipher> void testEncryption(List<C> ciphers) throws Exception {
    for (CryptoCipher cipher : ciphers) {
      testEncryption(cipher);
    }
  }

  /**
   * Invokes {@link #testEncryption(CryptoCipher, byte[])} several times for the the given cipher,
   * to be sure that no errors arise from reusing the same instance multiple times
   * (e.g. from reusing the same IV for subsequent AES encryption operations)
   */
  protected void testEncryption(CryptoCipher cipher) throws GeneralSecurityException {
    int iterations = 5;
    for (int i = 0; i < iterations; i++) {
      testEncryption(cipher, randomInput());
    }
  }

  protected void testMultithreaded(CryptoCipher cipher, byte[] input, int nThreads, int iterationsPerThread) throws Exception {
    // TODO(8/18/2026): maybe extract static method (with the exception-checking & timing logic) to MultithreadedTestHarness
    MultithreadedTestHarness threadRunner = new MultithreadedTestHarness(
        (ThrowingRunnable)() -> testEncryption(cipher, input));
    Stopwatch stopwatch = Stopwatch.createStarted();
    Collection<Throwable> exceptions = threadRunner.run(nThreads, iterationsPerThread);
    stopwatch.stop();
    int totalIterations = iterationsPerThread * nThreads;
    System.out.printf("%s:%n  %s for %,d iterations using %d threads%n", cipher, stopwatch, totalIterations, nThreads);
    if (!exceptions.isEmpty()) {
      int size = exceptions.size();
      String errMsg = String.format("%d exception%s thrown by %s", size, size > 1 ? "s" : "", cipher);
      ImmutableMultiset<String> stackTraces = exceptions.stream().map(RuntimeUtils::printStackTrace).collect(ImmutableMultiset.toImmutableMultiset());
      StringPrintStream detailMsg = new StringPrintStream();
      detailMsg.printf("%s%n", sectionHeader(errMsg + ":"));
//      stackTraces.elementSet().forEach(stackTrace -> detailMsg.printf("%s%n", stackTrace));
      stackTraces.forEachEntry((stackTrace, count) -> {
        detailMsg.print(stackTrace);
        if (count > 1)
          detailMsg.printf("\t(encountered %d times)%n", count);
        detailMsg.println();
      });
      System.err.println(detailMsg);
      fail(errMsg);
    }
  }

  /**
   * Creates a random input (plaintext) for testing a {@link CryptoCipher#encrypt(byte[])} operation.
   */
  protected byte[] randomInput() {
    // Note: using array size different from AES block size (16), to test padding logic (if any)
    if (randomBytes == null) {
      randomBytes = RandomUtils.randBytes(17);
    }
    return randomBytes;
  }

  /**
   * TODO(8/19/2026): remove this unused method? dup in AESCipherTest.testAllImplementations
   */
  protected <T extends CryptoCipher> List<T> testAllImplementationsOf(Class<T> baseClass) throws Exception {
    List<Class<T>> implClasses = TestUtils.findAllSubTypesOf(baseClass);
    byte[] input = randomInput();  // Note: using array size different from AES block size (16), to test padding logic (if any)
    List<T> ciphers = new ArrayList<>();
    for (Class<T> cls : implClasses) {
      if (verbose) TestUtils.printSectionHeader("Testing " + cls.getSimpleName());
      Constructor<T> constructor = cls.getConstructor(byte[].class);
      T cipher = constructor.newInstance((Object)secretKeyBytes);
      ciphers.add(cipher);
      testEncryption(cipher, input);
    }
    return ciphers;
    // TODO: test that all impls produce the same result when given the same IV
    // make sure all impls produce the same output for the same IV
    /*byte[] iv = RandomUtils.randBytes(CryptoCipher.IV_LENGTH);
    Set<String> results = new LinkedHashSet<>();
    String expectedResult = null;
    for (CryptoCipher cipher : ciphers) {
      String clsName = cipher.getClass().getSimpleName();
      if (clsName.contains("_NoIV")) {
        // the "_NoIV" impls don't use an IV array for Cipher.init (they rely on the default IV gen behavior of the cipher class),
        // so we can't expect them to produce the same result each time
        continue;
      }
      String result = new ForwardingAESCipherGCM_FixedIV(cipher, iv)
          .encrypt(input, BaseEncoding.base64Url());
      // TODO: cont here: ForwardingAESCipherGCM_FixedIV doesn't work b/c delegate invokes its own generateIV method
      if (expectedResult == null)
        expectedResult = result;
      else {
        // this result should be the same as all preceding results
        assertEquals(clsName, expectedResult, result);
      }
    }*/
  }
}
