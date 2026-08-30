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
public class LocalAESCipher3A extends AESCipherImpl {

  // TODO(8/22/2026): experimental copy of LocalAESCipher3, with optimized cache indexing

  protected final Cipher cipher;

  // reusing the same 2 instances of FastGCMParameterSpec, directly overwriting their iv fields
  private final AESParameterSpec[] ivCache = new AESParameterSpec[2];
  private int ivCount;

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @throws NullPointerException     if any argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   * @throws RuntimeException         if {@link Cipher#getInstance(String)} threw an exception
   *                                  (i.e. if the current platform doesn't support the given AES mode)
   */
  public LocalAESCipher3A(byte[] key, Mode mode) {
    super(key, mode);
    try {
      cipher = createCipher();
    }
    catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
    for (int i = 0; i < ivCache.length; i++) {
      ivCache[i] = getParametersToEncrypt(new byte[ivLength]);
    }
  }

  @Override
  public Cipher getCipher() {
    return cipher;
  }

  @Override
  protected AESParameterSpec getParametersToEncrypt() {
    int i = ivCount++ & 1;  // bitwise `& 1` is same as `% 2`, but never negative, so int overflow is safe
    AESParameterSpec nextParam = ivCache[i];
    // overwrite the cached iv
    rnd.nextBytes(nextParam.getIV());
    return nextParam;
  }

}
