package solutions.trsoftware.commons.server.util.crypto.aes;

import com.google.common.annotations.VisibleForTesting;
import solutions.trsoftware.commons.server.util.SecureRandomUtils;
import solutions.trsoftware.commons.server.util.ServerStringUtils;
import solutions.trsoftware.commons.server.util.crypto.CryptoCipher;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.*;

/**
 * Helper class for performing AES encryption/decryption using a {@link Cipher#getInstance(String) javax.crypto.Cipher}
 * (e.g. {@value AESCipherMode_CBC#TRANSFORMATION_SPEC} or {@value AESCipherMode_GCM#TRANSFORMATION_SPEC}).
 * <p>
 * All implementations of this base class provide various forms of caching {@link Cipher} instances for future reuse,
 * since {@linkplain Cipher#getInstance(String) constructing a new Cipher instance} is the most expensive step of
 * the encryption/decryption process.
 * <ul>
 *   <li>{@link LocalAESCipher}: encapsulates a single {@link Cipher} instance (not threadsafe, but can be used with a {@link ThreadLocal})</li>
 *   <li>{@link SynchronizedAESCipher}: synchronized version of {@link LocalAESCipher}</li>
 *   <li>{@link ConcurrentAESCipher}: optimal for high concurrency: creates as many {@link Cipher} instances as the number
 *   of threads using it concurrently</li>
 * </ul>
 * Specifically, {@link SynchronizedAESCipher} and {@link ConcurrentAESCipher}
 * can be used as a global singletons shared by multiple threads,
 * while {@link LocalAESCipher} is suitable for reuse only within a single thread.
 *
 * @author Alex
 * @since 12/3/2025
 */
public abstract class AESCipher implements CryptoCipher {

  protected final SecretKey secretKey;
  protected final AESCipherMode mode;
  protected final int ivLength;

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @param mode the AES mode (e.g. CBC or GCM)
   * @throws NullPointerException if an argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   */
  public AESCipher(byte[] key, Mode mode) {
    this(key, mode.getInstance());
  }

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @param mode the AES mode implementation (e.g. CBC or GCM)
   * @throws NullPointerException if an argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   */
  @VisibleForTesting
  protected AESCipher(byte[] key, AESCipherMode mode) {
    secretKey = new AESKeySpec(key);
    this.mode = requireNonNull(mode, "mode");
    ivLength = mode.getIvLength();
  }

  public SecretKey getSecretKey() {
    return secretKey;
  }

  @Override
  public String getAlgorithm() {
    return mode.getTransformationSpec();
  }

  protected Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
    // TODO: maybe make this method abstract, to force subclasses to cache the cipher instance(s)
    return createCipher();
  }

  protected Cipher createCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
    return mode.createCipher();
  }

  /**
   * Encrypts the given data by invoking {@link Cipher#doFinal(byte[], int, int)} on a
   * cipher instance initialized with the {@linkplain #secretKey secret key}.
   *
   * @param input the input buffer to be encrypted
   * @param inputOffset the offset in {@code input} where the input starts
   * @param inputLen the input length
   * @return an array with the leading bytes containing the randomly-chosen initialization vector (IV),
   * and the rest containing the encrypted data; this array can be passed to {@link #decrypt(byte[])}
   */
  @Override
  public byte[] encrypt(byte[] input, int inputOffset, int inputLen) throws GeneralSecurityException {
    int opmode = Cipher.ENCRYPT_MODE;
    byte[] iv = generateIV(ivLength);
    AlgorithmParameterSpec params = mode.createAlgorithmParameterSpec(iv);
    int expectedOutLen = mode.getOutputSize(inputLen, opmode);
    byte[] output = new byte[iv.length + expectedOutLen];
    int outLen = initAndDoFinal(opmode, params,
        input, inputOffset, inputLen, output, iv.length);
    if (outLen < expectedOutLen) {
      // in the (unlikely) case that we overestimated outLen, have to down-size the output array (see CipherCore.doFinal(byte[], int, int))
      output = Arrays.copyOf(output, iv.length + outLen);
      // TODO: temp - logging a warning if estimate doesn't match actual size
      Logger.getLogger(getClass().getName()).warning(() ->
          String.format("Incorrect outLen estimate (expected: %d, actual: %d)", expectedOutLen, outLen));
    }
    System.arraycopy(iv, 0, output, 0, iv.length);
    return output;
  }

  /**
   * Reverses the encryption performed by {@link #encrypt(byte[])} or {@link #encrypt(byte[], int, int)}.
   *
   * @param ciphertext: an array where the first {@link #getIvLength()} bytes contain the initialization vector (IV),
   *   and the rest contain the data encrypted using a {@linkplain #getCipher() cipher} initialized with this IV.
   * @return the decrypted data
   */
  @Override
  public byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException {
    AlgorithmParameterSpec params = mode.createAlgorithmParameterSpec(ciphertext, 0, ivLength);
    return initAndDoFinal(Cipher.DECRYPT_MODE, params, ciphertext, ivLength, ciphertext.length - ivLength);
  }

  protected byte[] generateIV(int ivLength) {
    return SecureRandomUtils.getBytes(ivLength);
  }

  /**
   * Invokes {@link #initAndDoFinal(Cipher, int, AlgorithmParameterSpec, byte[], int, int)}
   * using the {@link Cipher} instance returned by {@link #getCipher()}.
   * <p>
   * Subclasses can override this method to provide any needed synchronization or resource pooling.
   *
   * @return result of the {@link Cipher#doFinal} invocation
   * @throws GeneralSecurityException if the {@link Cipher} threw an exception
   */
  protected byte[] initAndDoFinal(int mode, AlgorithmParameterSpec params,
                                  byte[] input, int inputOffset, int inputLen) throws GeneralSecurityException {
    return initAndDoFinal(getCipher(), mode, params, input, inputOffset, inputLen);
  }

  /**
   * Initializes the given cipher instance and invokes {@link Cipher#doFinal(byte[], int, int)}.
   *
   * @param input the input buffer to be encrypted
   * @param inputOffset the offset in {@code input} where the input starts
   * @param inputLen the input length
   * @return result of the {@link Cipher#doFinal} invocation
   * @throws GeneralSecurityException if the {@link Cipher} threw an exception
   */
  protected final byte[] initAndDoFinal(Cipher cipher, int mode, AlgorithmParameterSpec params,
                                  byte[] input, int inputOffset, int inputLen) throws GeneralSecurityException {
    cipher.init(mode, secretKey, params);
    return cipher.doFinal(input, inputOffset, inputLen);
  }


  // TODO: maybe use the following methods to encrypt in all cases (like the _ArrayOpt subclasses do)

  /**
   * Invokes {@link #initAndDoFinal(Cipher, int, AlgorithmParameterSpec, byte[], int, int, byte[], int)}
   * using the {@link Cipher} instance returned by {@link #getCipher()}.
   * <p>
   * Subclasses can override this method to provide any needed synchronization or resource pooling.
   *
   * @return result of the {@link Cipher#doFinal} invocation
   * @throws GeneralSecurityException if the {@link Cipher} threw an exception
   */
  protected int initAndDoFinal(int mode, AlgorithmParameterSpec params,
                               byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws GeneralSecurityException {
    return initAndDoFinal(getCipher(), mode, params, input, inputOffset, inputLen, output, outputOffset);
  }

  /**
   * Initializes the given cipher instance and invokes {@link Cipher#doFinal(byte[], int, int, byte[], int)}.
   *
   * @param input the input buffer
   * @param inputOffset the offset in {@code input} where the input starts
   * @param inputLen the input length
   * @param output the buffer for the result
   * @param outputOffset the offset in {@code output} where the result is stored
   * @return result of the {@link Cipher#doFinal} invocation
   * @throws GeneralSecurityException if the {@link Cipher} threw an exception
   */
  protected final int initAndDoFinal(Cipher cipher, int mode, AlgorithmParameterSpec params,
                               byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws GeneralSecurityException {
    cipher.init(mode, secretKey, params);
    return cipher.doFinal(input, inputOffset, inputLen, output, outputOffset);
  }

  /**
   * Generates a key that can be used with the {@value AESConstants#AES_ALGORITHM} algorithm
   * @return a 16-byte key
   * @see KeyGenerator
   */
  public static byte[] randomKey() throws GeneralSecurityException {
    KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
    SecretKey secretKey = keyGen.generateKey();
    return secretKey.getEncoded();
  }

  /** Generates a random key */
  public static void main(String[] args) throws GeneralSecurityException {
    byte[] key = AESCipher.randomKey();
    System.out.println("Random key:");
    System.out.printf("byte[%d]: %s%n", key.length, Arrays.toString(key));
    System.out.println("ServerStringUtils.urlSafeBase64Encode(key): " + ServerStringUtils.urlSafeBase64Encode(key));
    System.out.println("Base64.getEncoder().encodeToString(key): " + Base64.getEncoder().encodeToString(key));
  }

}
