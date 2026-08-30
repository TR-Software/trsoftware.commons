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
public class LocalAESCipher2 extends AESCipherImpl {

  // TODO(8/17/2026): experimental copy of LocalAESCipher, using alternating iv arrays instead of iv.clone()

  protected final Cipher cipher;

  private static final int ivCacheSize = 2;
  private long ivCount;
  // TODO(8/18/2026): even better, reuse the same 2 instances of FastGCMParameterSpec, directly overwriting their iv fields
  protected final byte[][] ivCache = new byte[ivCacheSize][ivLength];

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @throws NullPointerException     if any argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   * @throws RuntimeException         if {@link Cipher#getInstance(String)} threw an exception
   *                                  (i.e. if the current platform doesn't support the given AES mode)
   */
  public LocalAESCipher2(byte[] key, Mode mode) {
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
    int i = (int)(ivCount++ % ivCacheSize);
    byte[] iv = ivCache[i];
    rnd.nextBytes(iv);
    /* alternating iv arrays b/c CipherCore.init (CipherCore.java:577) requires a different array instance each time */
    return super.getParametersToEncrypt(iv);
  }

}
