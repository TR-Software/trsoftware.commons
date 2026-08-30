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
public class LocalAESCipher1 extends AESCipherImpl {

  protected final Cipher cipher;

  /* TODO(8/17/2026): experimental: reusing same array for generateIV
      - even better, reuse the same instance of FastGCMParameterSpec, directly overwriting its iv field
  */
  protected byte[] iv = new byte[ivLength];

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @throws NullPointerException     if any argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   * @throws RuntimeException         if {@link Cipher#getInstance(String)} threw an exception
   *                                  (i.e. if the current platform doesn't support the given AES mode)
   */
  public LocalAESCipher1(byte[] key, Mode mode) {
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
    rnd.nextBytes(iv);
    /* TODO: cloning iv b/c CipherCore.init (CipherCore.java:577) requires new array instance each time
             - maybe use 2 different iv arrays and alternate between the 2 each time
               (see experimental LocalAESCipher2 class)
     */
    return super.getParametersToEncrypt(iv.clone());
  }

}
