package solutions.trsoftware.commons.server.util.crypto.aes.benchmark;

import solutions.trsoftware.commons.server.util.crypto.aes.*;
import solutions.trsoftware.commons.shared.annotations.NotThreadSafe;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

/**
 * Contains a cached {@link Cipher} instance of a particular {@linkplain AESCipherMode#getTransformationSpec() AES mode}.
 * <p>
 * <b>Note:</b> this class is not thread-safe and must be synchronized externally or used with a {@link ThreadLocal}
 *
 * @see SynchronizedAESCipher
 * @see ConcurrentAESCipher
 * @author Alex
 * @since 12/5/2025
 */
@NotThreadSafe
public class LocalAESCipher2B extends AESCipherImpl {

  // TODO(8/17/2026): experimental copy of LocalAESCipher2, using fields instead of array for ivCache (same optimization as LocalAESCipher3B)

  protected final Cipher cipher;

  private final byte[] iv1 = new byte[ivLength];
  private final byte[] iv2 = new byte[ivLength];
  private boolean ivToggle;

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @throws NullPointerException     if any argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   * @throws RuntimeException         if {@link Cipher#getInstance(String)} threw an exception
   *                                  (i.e. if the current platform doesn't support the given AES mode)
   */
  public LocalAESCipher2B(byte[] key, Mode mode) {
    super(key, mode);
    try {
      cipher = createCipher();
    }
    catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Cipher getCipher() {
    return cipher;
  }

  @Override
  protected AESConstants.AESParameterSpec getParametersToEncrypt() {
    byte[] iv = (ivToggle ^= true) ? iv1 : iv2;  // using bitwise XOR to flip the boolean each time (see https://stackoverflow.com/a/224380)
    rnd.nextBytes(iv);
    return super.getParametersToEncrypt(iv);
  }

}
