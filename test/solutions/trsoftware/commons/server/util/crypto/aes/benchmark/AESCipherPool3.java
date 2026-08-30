package solutions.trsoftware.commons.server.util.crypto.aes.benchmark;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.server.util.ResourcePool;
import solutions.trsoftware.commons.server.util.crypto.aes.AESCipher;
import solutions.trsoftware.commons.server.util.crypto.aes.AESCipherImpl;
import solutions.trsoftware.commons.server.util.crypto.aes.LocalAESCipher;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

/**
 * Uses a {@link ResourcePool} of cached {@linkplain AESCipherImpl AES cipher} instances to achieve maximal concurrency
 * for encryption and decryption.
 *
 * @author Alex
 * @since 12/3/2025
 */
public class AESCipherPool3<T extends AESCipherImpl> extends AESCipher {
  /* TODO(8/24/2026): experimental version of AESCipherPool using ResourcePool.checkOut/checkIn instead of apply,
       to remove memory overhead of passing lambdas to ResourcePool.apply, which requires a new lambda allocation for each call (to capture the params)
       until the lambda gets inlined by the JIT
       (see CryptoCipherBenchmark2/result_04 profiler .jfr dumps)
   */

  protected final ResourcePool<T> ciphers = new ResourcePool<>(this::createWorker);

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @param mode the AES mode (e.g. CBC or GCM)
   * @throws NullPointerException if an argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   */
  public AESCipherPool3(byte[] key, Mode mode) {
    super(key, mode);
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
    T cipher = ciphers.checkOut();
    byte[] ret = cipher.encrypt(input, inputOffset, inputLen);
    ciphers.checkIn(cipher);  // done with this instance, return it to the pool
    return ret;
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
    T cipher = ciphers.checkOut();
    byte[] ret = cipher.decrypt(ciphertext);
    ciphers.checkIn(cipher);  // done with this instance, return it to the pool
    return ret;
  }

  @Override
  public byte[] secureRandomBytes(int n) {
    T cipher = ciphers.checkOut();
    byte[] ret = cipher.secureRandomBytes(n);
    ciphers.checkIn(cipher);  // done with this instance, return it to the pool
    return ret;
  }

  /**
   * Create a new instance of {@link T} for the {@linkplain #ciphers worker pool}.
   *
   * @implNote this default implementation returns a new instance of {@link LocalAESCipher};
   *   subclasses must override if {@link T} is a different {@link AESCipherImpl} subclass
   */
  protected T createWorker() {
    //noinspection unchecked
    return (T)new LocalAESCipher(secretKey.getEncoded(), mode.getMode());
  }

  /**
   * @return the number of worker instances currently contained by the pool
   * @see #createWorker()
   */
  public int getPoolSize() {
    return ciphers.size();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("mode", mode.getMode())
        .add("poolSize", getPoolSize())
        .toString();
  }

}
