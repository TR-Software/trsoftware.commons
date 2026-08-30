package solutions.trsoftware.commons.server.util.crypto.aes.benchmark;

import solutions.trsoftware.commons.server.util.crypto.aes.AESCipherImpl;
import solutions.trsoftware.commons.server.util.crypto.aes.AESCipherMode;
import solutions.trsoftware.commons.server.util.crypto.aes.ConcurrentAESCipher;
import solutions.trsoftware.commons.shared.annotations.NotThreadSafe;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.AESParameterSpec;
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
public class LocalAESCipher3B extends AESCipherImpl {

  // TODO(8/22/2026): experimental copy of LocalAESCipher3, using fields instead of array for ivCache

  protected final Cipher cipher;

  // reusing the same 2 instances of FastGCMParameterSpec, directly overwriting their iv fields
  private final AESParameterSpec iv1 = getParametersToEncrypt(new byte[ivLength]);
  private final AESParameterSpec iv2 = getParametersToEncrypt(new byte[ivLength]);
  private boolean ivToggle;

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @throws NullPointerException     if any argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   * @throws RuntimeException         if {@link Cipher#getInstance(String)} threw an exception
   *                                  (i.e. if the current platform doesn't support the given AES mode)
   */
  public LocalAESCipher3B(byte[] key, Mode mode) {
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
  protected AESParameterSpec getParametersToEncrypt() {
    AESParameterSpec nextParam = (ivToggle ^= true) ? iv1 : iv2;  // using bitwise XOR to flip the boolean each time (see https://stackoverflow.com/a/224380)
    // overwrite the cached iv
    rnd.nextBytes(nextParam.getIV());
    return nextParam;
  }

}
