package solutions.trsoftware.commons.server.util.crypto.aes;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.logging.Logger;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.*;

/**
 * Base class for AES encryption/decryption using a {@link Cipher#getInstance(String) javax.crypto.Cipher}
 * configured in a particular AES operating mode
 * (e.g. {@value AESCipherMode_CBC#TRANSFORMATION_SPEC} or {@value AESCipherMode_GCM#TRANSFORMATION_SPEC}).
 * <p>
 * All implementations of this base class provide various forms of caching {@link Cipher} instances for future reuse,
 * since {@linkplain Cipher#getInstance(String) constructing a new Cipher instance} is the most expensive step of
 * the encryption/decryption process.
 * <ul>
 *   <li>{@link LocalAESCipher}: encapsulates a single {@link Cipher} instance
 *       (not threadsafe, but can be used with a {@link ThreadLocal})</li>
 *   <li>{@link ConcurrentAESCipher}: optimal for high concurrency: creates as many {@link Cipher} instances as
 *       the max number of threads using it concurrently</li>
 * </ul>
 *
 * @author Alex
 * @since 12/3/2025
 */
public abstract class AESCipherImpl extends AESCipher {

  protected final SecureRandom rnd;

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @param mode the AES mode (e.g. CBC or GCM)
   * @throws NullPointerException if an argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   */
  public AESCipherImpl(byte[] key, Mode mode) {
    super(key, mode.getInstance());
    rnd = new SecureRandom();
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
    AESParameterSpec params = getParametersToEncrypt();
    int ivLength = this.ivLength;
    byte[] iv = params.getIV();
    assert iv.length == ivLength;
    int expectedOutLen = mode.getOutputSize(inputLen, opmode);
    byte[] output = new byte[ivLength + expectedOutLen];
    int outLen = initAndDoFinal(opmode, params,
        input, inputOffset, inputLen, output, ivLength);
    if (outLen < expectedOutLen) {
      // in the (unlikely) case that we overestimated outLen, have to down-size the output array (see CipherCore.doFinal(byte[], int, int))
      output = Arrays.copyOf(output, ivLength + outLen);
      // TODO: temp - logging a warning if estimate doesn't match actual size (should never actually happen)
      Logger.getLogger(getClass().getName()).warning(() ->
          String.format("Incorrect outLen estimate (expected: %d, actual: %d)", expectedOutLen, outLen));
    }
    System.arraycopy(iv, 0, output, 0, ivLength);
    return output;
  }

  /**
   * Reverses the encryption performed by {@link #encrypt(byte[])} or {@link #encrypt(byte[], int, int)}.
   *
   * @param ciphertext: an array where the first {@link #ivLength} bytes contain the initialization vector (IV),
   *   and the rest contain the data encrypted using a {@linkplain #getCipher() cipher} initialized with this IV.
   * @return the decrypted data
   */
  @Override
  public byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException {
    AESParameterSpec params = getParametersToDecrypt(ciphertext);
    return initAndDoFinal(Cipher.DECRYPT_MODE, params, ciphertext, ivLength, ciphertext.length - ivLength);
  }

  /**
   * Creates the parameter object containing a random initialization vector (IV) to initialize a {@link Cipher}
   * for performing {@linkplain #encrypt(byte[]) encryption}.
   *
   * @return parameter object of the type required by the {@linkplain #mode operating mode} of this cipher
   * @see #encrypt(byte[])
   * @see Cipher#init(int, Key, AlgorithmParameters)
   */
  protected AESParameterSpec getParametersToEncrypt() {
    byte[] iv = secureRandomBytes(ivLength);
    return getParametersToEncrypt(iv);
  }

  /**
   * Creates the parameter object containing the given initialization vector (IV) to initialize a {@link Cipher}
   * for performing {@linkplain #encrypt(byte[]) encryption}.
   *
   * @return parameter object of the type required by the {@linkplain #mode operating mode} of this cipher
   * @see #getParametersToEncrypt()
   */
  protected AESParameterSpec getParametersToEncrypt(byte[] iv) {
    return mode.createAlgorithmParameterSpec(iv);
  }

  /**
   * Creates the parameter object to initialize a {@link Cipher} for performing {@linkplain #decrypt(byte[]) decryption}
   * of the given ciphertext using the initialization vector (IV) contained in its first {@link #ivLength} bytes.
   *
   * @param ciphertext: an array where the first {@link #ivLength} bytes contain the initialization vector (IV)
   * @return parameter object of the type required by the {@linkplain #mode operating mode} of this cipher
   * @see #decrypt(byte[])
   * @see Cipher#init(int, Key, AlgorithmParameters)
   */
  protected AESParameterSpec getParametersToDecrypt(byte[] ciphertext) {
    return mode.createAlgorithmParameterSpec(ciphertext, 0, ivLength);
  }

  @Override
  public byte[] secureRandomBytes(int n) {
    return RandomUtils.randBytes(rnd, n);
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


  /**
   * Invokes {@link #initAndDoFinal(Cipher, int, AlgorithmParameterSpec, byte[], int, int, byte[], int)}
   * using the {@link Cipher} instance returned by {@link #getCipher()}.
   * <p>
   * Subclasses can override this method to provide any needed synchronization or resource pooling.
   *
   * @return result of the {@link Cipher#doFinal(byte[], int, int, byte[], int)} invocation
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
   * @return the number of bytes stored in {@code output} (returned by the {@link Cipher#doFinal} invocation)
   * @throws GeneralSecurityException if the {@link Cipher} threw an exception
   */
  protected final int initAndDoFinal(Cipher cipher, int mode, AlgorithmParameterSpec params,
                               byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws GeneralSecurityException {
    cipher.init(mode, secretKey, params);
    return cipher.doFinal(input, inputOffset, inputLen, output, outputOffset);
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }

  protected MoreObjects.ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this)
        .add("mode", mode.getMode());
  }

}
