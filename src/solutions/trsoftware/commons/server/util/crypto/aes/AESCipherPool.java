package solutions.trsoftware.commons.server.util.crypto.aes;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.server.util.ResourcePool;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

/**
 * Uses a {@link ResourcePool} of cached {@linkplain AESCipherImpl AES cipher} instances to achieve maximal concurrency
 * for encryption and decryption.
 * <p>
 * The pool grows as needed, depending on thread contention, ultimately reaching a {@linkplain #getPoolSize() size}
 * equal to the highest number of threads concurrently operating on it.
 *
 * @author Alex
 * @since 12/3/2025
 */
public abstract class AESCipherPool<T extends AESCipherImpl> extends AESCipher {

  protected final ResourcePool<T> ciphers = new ResourcePool<>(this::createWorker);

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @param mode the AES mode (e.g. CBC or GCM)
   * @throws NullPointerException if an argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   */
  public AESCipherPool(byte[] key, Mode mode) {
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
    return ciphers.apply(cipher -> cipher.encrypt(input, inputOffset, inputLen));
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
    return ciphers.apply(cipher -> cipher.decrypt(ciphertext));
  }

  @Override
  public byte[] secureRandomBytes(int n) {
    return ciphers.apply(cipher -> cipher.secureRandomBytes(n));
  }

  /**
   * Create a new instance of {@link T} for the {@linkplain #ciphers worker pool}.
   *
   * @implNote this default implementation returns a new instance of {@link LocalAESCipher};
   *   subclasses must override if {@link T} is a different {@link AESCipherImpl} subclass
   */
  protected abstract T createWorker();

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
