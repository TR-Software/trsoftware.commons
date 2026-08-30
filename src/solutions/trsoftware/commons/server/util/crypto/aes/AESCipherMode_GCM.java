package solutions.trsoftware.commons.server.util.crypto.aes;

import solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

import javax.crypto.Cipher;
import java.security.AlgorithmParameters;
import java.security.Key;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.*;
import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.AES_ALGORITHM;
import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.BLOCK_SIZE;

/**
 * Parameters for the AES encryption algorithm in {@link Mode#GCM GCM} (Galois/counter) mode
 * without padding ({@value #TRANSFORMATION_SPEC}).
 * <p>
 * <b>Note:</b> In contrast to {@link AESCipherMode_CBC CBC}, the GCM mode <i>ensures authentication</i>
 * in addition to confidentiality, making it the preferred choice mode for modern applications.
 * The only drawback is that it's slightly slower than CBC (~25% slower, benchmarked on Oracle JDK8/Windows 8/Intel i7)
 * and does not allow reusing the same initialization vector (IV) for multiple operations.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Advanced_Encryption_Standard">Advanced Encryption Standard (AES)</a>
 * @see <a href="https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Galois/counter_(GCM)">Galois/counter Mode (GCM)</a>
 * @see <a href="http://www.javamex.com/tutorials/cryptography/symmetric.shtml">Symmetric-key encryption in Java (tutorial)</a>
 * @author Alex
 * @since 12/5/2025
 */

public class AESCipherMode_GCM extends AESCipherMode {

  /**
   * Value of the {@link Cipher#getInstance(String)} argument for this mode.
   * <p>
   * <b>NOTE:</b> <i>"AES/GCM/NoPadding"</i> is technically not on the list of standard transformations
   * that are required for all JVM implementations (see {@link Cipher} documentation), but is almost certainly
   * supported by all major JDKs (e.g. Oracle's JDK definitely implements it).
   */
  public static final String TRANSFORMATION_SPEC = AES_ALGORITHM + "/GCM/NoPadding";
  /**
   * The default GCM IV length is 12 bytes (see {@link com.sun.crypto.provider.GaloisCounterMode}))
   * @see <a href="https://crypto.stackexchange.com/questions/41601/aes-gcm-recommended-iv-size-why-12-bytes">StackOverflow</a>
   */
  public static final int IV_LENGTH = 12;  // see com.sun.crypto.provider.GaloisCounterMode.DEFAULT_IV_LEN
  /**
   * Number of bytes used for the GCM auth tag.
   * The recommended tag length is the same as {@value AESConstants#BLOCK_SIZE} (see {@link com.sun.crypto.provider.GaloisCounterMode})
   */
  public static final int TAG_LENGTH = BLOCK_SIZE;  // this is the recommended value (128-bits); see com.sun.crypto.provider.GaloisCounterMode.DEFAULT_IV_LEN
  /** Number of bits used for the GCM auth tag */
  protected static final int TAG_LENGTH_BITS = TAG_LENGTH * Byte.SIZE;

  @Override
  public Mode getMode() {
    return Mode.GCM;
  }

  @Override
  public String getTransformationSpec() {
    return TRANSFORMATION_SPEC;
  }

  @Override
  public int getIvLength() {
    return IV_LENGTH;
  }

  @Override
  public boolean isPaddingUsed() {
    return false;
  }

  @Override
  public int getOutputSize(int inputLen, int opmode) {
    // see com.sun.crypto.provider.CipherCore.getOutputSizeByOperation
    // assuming empty buffer (since we're not doing any intermediate update ops before doFinal)
    return opmode != Cipher.DECRYPT_MODE
        ? inputLen + TAG_LENGTH
        : inputLen;
  }

  /**
   * Creates the appropriate parameter object for {@link Cipher#init(int, Key, AlgorithmParameters)} from
   * the given IV bytes.
   *
   * @param iv the buffer with the IV
   */
  @Override
  public AESParameterSpec createAlgorithmParameterSpec(byte[] iv) {
    return new FastGCMParameterSpec(iv);
  }

  /**
   * Creates the appropriate parameter object for {@link Cipher#init(int, Key, AlgorithmParameters)} from
   * the given IV bytes.
   *  @param iv the buffer with the IV.
   * @param offset the offset in {@code iv} where the IV starts.
   * @param len the number of IV bytes.
   */
  @Override
  public AESParameterSpec createAlgorithmParameterSpec(byte[] iv, int offset, int len) {
    return new FastGCMParameterSpec(iv, offset, len);
  }

}
