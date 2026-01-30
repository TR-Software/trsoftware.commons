package solutions.trsoftware.commons.server.util.crypto.aes;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

/**
 * Synchronized thread-safe version of {@link LocalAESCipher}.
 *
 * @author Alex
 * @since 12/5/2025
 */
public class SynchronizedAESCipher extends LocalAESCipher {

  /**
   * @param key a 16, 24, or 32-byte array (representing a 128, 192, or 256-bit AES key)
   * @throws NullPointerException     if any argument is null
   * @throws IllegalArgumentException if the key does not contain the required number of bytes
   * @throws RuntimeException         if {@link Cipher#getInstance(String)} threw an exception
   *                                  (i.e. if the current platform doesn't support the given AES mode)
   */
  public SynchronizedAESCipher(byte[] key, Mode mode) {
    super(key, mode);
  }

  @Override // overriding to make synchronized
  protected synchronized byte[] initAndDoFinal(int mode, AlgorithmParameterSpec params, byte[] input, int inputOffset, int inputLen) throws GeneralSecurityException {
    return super.initAndDoFinal(cipher, mode, params, input, inputOffset, inputLen);
  }

  @Override // overriding to make synchronized
  protected synchronized int initAndDoFinal(int mode, AlgorithmParameterSpec params, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws GeneralSecurityException {
    return super.initAndDoFinal(cipher, mode, params, input, inputOffset, inputLen, output, outputOffset);
  }
}
