package solutions.trsoftware.commons.server.util.crypto.aes;

import solutions.trsoftware.commons.server.util.crypto.CryptoCipher;
import solutions.trsoftware.commons.server.util.crypto.CryptoCipherTestCase;
import solutions.trsoftware.commons.server.util.crypto.aes.benchmark.*;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode.values;

/**
 * @author Alex
 * @since 8/17/2026
 */
public class AESCipherPoolTest extends CryptoCipherTestCase {

  public void testEncryption() throws Exception {
    // TODO(8/17/2026): code dup in AESCipherTest.testEncryption + AESCipherTest.testConcurrency
    byte[] input = RandomUtils.randBytes(17);  // Note: using array size different from AES block size (16), to test padding logic (if any)
    byte[] key = secretKeyBytes;
    List<CryptoCipher> ciphers = new ArrayList<>();
    for (AESConstants.Mode mode : values()) {
      Collections.addAll(ciphers,
          new AESCipherPool_LocalAESCipher(key, mode),
          new AESCipherPool_LocalAESCipher2(key, mode),
          new AESCipherPool_LocalAESCipher2B(key, mode),
          new AESCipherPool_LocalAESCipher3(key, mode),
          new AESCipherPool_LocalAESCipher3A(key, mode),
          new AESCipherPool_LocalAESCipher3B(key, mode),
          
          new AESCipherPool2_LocalAESCipher(key, mode),
          new AESCipherPool2_LocalAESCipher2(key, mode),
          new AESCipherPool2_LocalAESCipher2B(key, mode),
          new AESCipherPool2_LocalAESCipher3(key, mode),
          new AESCipherPool2_LocalAESCipher3A(key, mode),
          new AESCipherPool2_LocalAESCipher3B(key, mode),
          
          new AESCipherPool3_LocalAESCipher(key, mode),
          new AESCipherPool3_LocalAESCipher2(key, mode),
          new AESCipherPool3_LocalAESCipher2B(key, mode),
          new AESCipherPool3_LocalAESCipher3(key, mode),
          new AESCipherPool3_LocalAESCipher3A(key, mode),
          new AESCipherPool3_LocalAESCipher3B(key, mode)

      );
    }

    int nThreads = 10;
    int iterationsPerThread = 1000;
    verbose = false;
    for (CryptoCipher cipher : ciphers) {
      testMultithreaded(cipher, input, nThreads, iterationsPerThread);
    }
  }
  
  /*
  ================================================================================
  AESCipherPool subclasses using alternate LocalAESCipher version
  ================================================================================
  */

  public static class AESCipherPool_LocalAESCipher extends AESCipherPool<LocalAESCipher> {
    public AESCipherPool_LocalAESCipher(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher createWorker() {
      return new LocalAESCipher(secretKey.getEncoded(), mode.getMode());
    }
  }


  public static class AESCipherPool_LocalAESCipher2 extends AESCipherPool<LocalAESCipher2> {
    public AESCipherPool_LocalAESCipher2(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher2 createWorker() {
      return new LocalAESCipher2(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool_LocalAESCipher2B extends AESCipherPool<LocalAESCipher2B> {
    public AESCipherPool_LocalAESCipher2B(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher2B createWorker() {
      return new LocalAESCipher2B(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool_LocalAESCipher3 extends AESCipherPool<LocalAESCipher3> {
    public AESCipherPool_LocalAESCipher3(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3 createWorker() {
      return new LocalAESCipher3(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool_LocalAESCipher3A extends AESCipherPool<LocalAESCipher3A> {
    public AESCipherPool_LocalAESCipher3A(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3A createWorker() {
      return new LocalAESCipher3A(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool_LocalAESCipher3B extends AESCipherPool<LocalAESCipher3B> {
    public AESCipherPool_LocalAESCipher3B(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3B createWorker() {
      return new LocalAESCipher3B(secretKey.getEncoded(), mode.getMode());
    }
  }
  
  /*
  ================================================================================
  AESCipherPool2 subclasses using alternate LocalAESCipher version
  ================================================================================
  */

  public static class AESCipherPool2_LocalAESCipher extends AESCipherPool2<LocalAESCipher> {
    public AESCipherPool2_LocalAESCipher(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher createWorker() {
      return new LocalAESCipher(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool2_LocalAESCipher2 extends AESCipherPool2<LocalAESCipher2> {
    public AESCipherPool2_LocalAESCipher2(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher2 createWorker() {
      return new LocalAESCipher2(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool2_LocalAESCipher2B extends AESCipherPool2<LocalAESCipher2B> {
    public AESCipherPool2_LocalAESCipher2B(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher2B createWorker() {
      return new LocalAESCipher2B(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool2_LocalAESCipher3 extends AESCipherPool2<LocalAESCipher3> {
    public AESCipherPool2_LocalAESCipher3(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3 createWorker() {
      return new LocalAESCipher3(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool2_LocalAESCipher3A extends AESCipherPool2<LocalAESCipher3A> {
    public AESCipherPool2_LocalAESCipher3A(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3A createWorker() {
      return new LocalAESCipher3A(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool2_LocalAESCipher3B extends AESCipherPool2<LocalAESCipher3B> {
    public AESCipherPool2_LocalAESCipher3B(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3B createWorker() {
      return new LocalAESCipher3B(secretKey.getEncoded(), mode.getMode());
    }
  }
  
  /*
  ================================================================================
  AESCipherPool3 subclasses using alternate LocalAESCipher version
  ================================================================================
  */

  public static class AESCipherPool3_LocalAESCipher extends AESCipherPool3<LocalAESCipher> {
    public AESCipherPool3_LocalAESCipher(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher createWorker() {
      return new LocalAESCipher(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool3_LocalAESCipher2 extends AESCipherPool3<LocalAESCipher2> {
    public AESCipherPool3_LocalAESCipher2(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher2 createWorker() {
      return new LocalAESCipher2(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool3_LocalAESCipher2B extends AESCipherPool3<LocalAESCipher2B> {
    public AESCipherPool3_LocalAESCipher2B(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher2B createWorker() {
      return new LocalAESCipher2B(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool3_LocalAESCipher3 extends AESCipherPool3<LocalAESCipher3> {
    public AESCipherPool3_LocalAESCipher3(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3 createWorker() {
      return new LocalAESCipher3(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool3_LocalAESCipher3A extends AESCipherPool3<LocalAESCipher3A> {
    public AESCipherPool3_LocalAESCipher3A(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3A createWorker() {
      return new LocalAESCipher3A(secretKey.getEncoded(), mode.getMode());
    }
  }

  public static class AESCipherPool3_LocalAESCipher3B extends AESCipherPool3<LocalAESCipher3B> {
    public AESCipherPool3_LocalAESCipher3B(byte[] key, AESConstants.Mode mode) {
      super(key, mode);
    }

    @Override
    protected LocalAESCipher3B createWorker() {
      return new LocalAESCipher3B(secretKey.getEncoded(), mode.getMode());
    }
  }
}