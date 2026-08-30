package solutions.trsoftware.commons.server.util.crypto.aes;

import solutions.trsoftware.commons.server.util.ResourcePool;
import solutions.trsoftware.commons.shared.annotations.ThreadSafe;

import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

/**
 * Uses a {@link ResourcePool} of cached {@link LocalAESCipher} instances to achieve maximal concurrency
 * for encryption and decryption.
 * <p>
 * An instance of this class can be safely and efficiently used as a global singleton shared by multiple threads,
 * and can also be used as fast source of {@linkplain #secureRandomBytes(int) secure random bytes}.
 *
 * @author Alex
 * @since 12/5/2025
 */
@ThreadSafe
public class ConcurrentAESCipher extends AESCipherPool<LocalAESCipher> {

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @param mode the AES mode (e.g. CBC or GCM)
   * @throws NullPointerException     if an argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   */
  public ConcurrentAESCipher(byte[] key, Mode mode) {
    super(key, mode);
  }

  @Override
  protected LocalAESCipher createWorker() {
    return new LocalAESCipher(secretKey.getEncoded(), mode.getMode());
  }
}
