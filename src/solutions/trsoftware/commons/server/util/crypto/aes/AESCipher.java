package solutions.trsoftware.commons.server.util.crypto.aes;

import com.google.common.annotations.VisibleForTesting;
import solutions.trsoftware.commons.server.util.ServerStringUtils;
import solutions.trsoftware.commons.server.util.crypto.CryptoCipher;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

import static java.util.Objects.requireNonNull;
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

  /**
   * Generates the specified number of bytes using this implementation's secure random generator.
   *
   * @param n the number of bytes to generate
   * @return the specified number of bytes from this implementation's secure random generator
   */
  public abstract byte[] secureRandomBytes(int n);

  /** Generates a random key that can be used for AES encryption */
  public static void main(String[] args) throws GeneralSecurityException {
    byte[] key = generateKey();
    System.out.println("Random key:");
    System.out.printf("byte[%d]: %s%n", key.length, Arrays.toString(key));
    System.out.println("ServerStringUtils.urlSafeBase64Encode(key): " + ServerStringUtils.urlSafeBase64Encode(key));
    System.out.println("Base64.getEncoder().encodeToString(key): " + Base64.getEncoder().encodeToString(key));
  }

}
