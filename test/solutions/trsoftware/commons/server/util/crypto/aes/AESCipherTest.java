package solutions.trsoftware.commons.server.util.crypto.aes;

import solutions.trsoftware.commons.server.testutil.TestUtils;
import solutions.trsoftware.commons.server.util.crypto.CryptoCipher;
import solutions.trsoftware.commons.server.util.crypto.CryptoCipherTestCase;
import solutions.trsoftware.commons.server.util.crypto.aes.benchmark.LocalAESCipher2;
import solutions.trsoftware.commons.server.util.crypto.aes.benchmark.SynchronizedAESCipher;
import solutions.trsoftware.commons.server.util.reflect.ReflectionPredicates.Modifiers;
import solutions.trsoftware.commons.shared.annotations.NotThreadSafe;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.io.TablePrinter;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import javax.crypto.Cipher;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.*;
import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode.values;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThat;

/**
 * @author Alex
 * @since 12/4/2025
 */
@Slow
public class AESCipherTest extends CryptoCipherTestCase {

  private List<AESCipher> ciphers;

  public void setUp() throws Exception {
    super.setUp();
    ciphers = new ArrayList<>();
    byte[] key = secretKeyBytes;
    for (Mode mode : values()) {
      Collections.addAll(ciphers,
          new LocalAESCipher(key, mode),
          new LocalAESCipher2(key, mode),
          new SynchronizedAESCipher(key, mode),
          new ConcurrentAESCipher(key, mode)
      );
    }
  }

  @Override
  protected void tearDown() throws Exception {
    ciphers = null;
    super.tearDown();
  }

  public void testEncryption() throws Exception {
    testEncryption(ciphers);
  }

  public void testConcurrency() throws Exception {
    // TODO: code dup in AESCipherPoolTest.testEncryption
    List<AESCipher> threadSafeCiphers = ciphers.stream()
        .filter(cipher -> !cipher.getClass().isAnnotationPresent(NotThreadSafe.class))
        .collect(Collectors.toList());

    byte[] input = RandomUtils.randBytes(17);  // Note: using array size different from AES block size (16), to test padding logic (if any)
    int nThreads = 5;
    int iterationsPerThread = 1000;
    verbose = false;
    for (CryptoCipher cipher : threadSafeCiphers) {
      testMultithreaded(cipher, input, nThreads, iterationsPerThread);
    }
  }

  /**
   * Tests all classes derived from {@link AESCipher}
   */
  public void testAllImplementations() throws Exception {
    List<Class<AESCipher>> implClasses = TestUtils.findAllSubTypesOf(AESCipher.class);
    byte[] input = RandomUtils.randBytes(17);  // Note: using array size different from AES block size (16), to test padding logic (if any)
    for (Class<AESCipher> cls : implClasses) {
      if (Modifiers.isAbstract(cls))
        continue;  // can't instantiate an abstract class
      Constructor<AESCipher> constructor = cls.getConstructor(byte[].class, Mode.class);
      for (Mode mode : values()) {
        TestUtils.printSectionHeader(String.format("Testing %s(%s)", cls.getSimpleName(), mode));
        AESCipher cipher = constructor.newInstance(secretKeyBytes, mode);
        testEncryption(cipher);
      }
    }
  }

  /**
   * Tests that {@link AESCipherMode#getOutputSize(int, int)} never under-estimates the required size of output array
   */
  public void testGetOutputSize() throws Exception {
    List<Class<AESCipherMode>> modes = TestUtils.findAllSubTypesOf(AESCipherMode.class);
    AESKeySpec keySpec = new AESKeySpec(secretKeyBytes);
    int blockSize = BLOCK_SIZE;
    int opmode = Cipher.ENCRYPT_MODE;
    for (Class<AESCipherMode> modeClass : modes) {
      AESCipherMode impl = modeClass.newInstance();
      System.out.println(modeClass.getSimpleName() + ".getOutputSize");
      TablePrinter tp = new TablePrinter();
      for (int inputLen = blockSize - 3; inputLen <= blockSize * 2 + 1; inputLen++) {
        byte[] input = RandomUtils.randBytes(inputLen);
        int implOutputSizeEstimate = impl.getOutputSize(inputLen, opmode);
        Cipher cipher = impl.createCipher();
        cipher.init(opmode, keySpec);
        int cipherOutputSizeEstimate = cipher.getOutputSize(inputLen);
        // impl should never under-estimate the output size
        assertThat(implOutputSizeEstimate).isGreaterThanOrEqualTo(cipherOutputSizeEstimate);
        byte[] output = cipher.doFinal(input);
        int actualOutputSize = output.length;
        tp.newRow()
            .addCol("inputLen", inputLen)
            .addCol("implEstimate", implOutputSizeEstimate)
            .addCol("cipherEstimate", cipherOutputSizeEstimate)
            .addCol("actualOutSize", actualOutputSize);
      }
      tp.printTable();
    }
  }
}