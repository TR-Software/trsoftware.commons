package solutions.trsoftware.commons.server.util.crypto.aes;

import solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

import javax.crypto.Cipher;
import java.security.SecureRandom;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.*;
import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.AES_ALGORITHM;

/**
 * Parameters for the AES encryption algorithm in {@link Mode#CBC CBC} mode with {@code PKC5Padding}
 * ({@value #TRANSFORMATION_SPEC}).
 * <p>
 * <b>Note:</b> In contrast to {@link AESCipherMode_GCM GCM}, the CBC mode ensures only confidentiality but
 * <i>does not provide authentication</i>. Therefore, GCM is the generally recommended mode for modern applications.
 * The main advantage of CBC is that it's slightly faster than GCM (~25% faster, benchmarked on Oracle JDK8/Windows 8/Intel i7),
 * and allows reusing the same initialization vector (IV) without sacrificing confidentiality, which can provide
 * an additional speed boost (since {@link SecureRandom#nextBytes} is fairly slow).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Advanced_Encryption_Standard">Advanced Encryption Standard (AES)</a>
 * @see <a href="https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Cipher_block_chaining_(CBC)">Cipher block chaining (CBC)</a>
 * @see <a href="http://www.javamex.com/tutorials/cryptography/symmetric.shtml">Symmetric-key encryption in Java (tutorial)</a>
 * @author Alex
 * @since 12/5/2025
 */
public class AESCipherMode_CBC extends AESCipherMode {

  /**
   * Value of the {@link Cipher#getInstance(String)} argument for this mode.
   * <p>
   * <i>Note:</i> every JVM implementation is required to support <i>AES/CBC/PKCS5Padding</i> and <i>AES/CBC/NoPadding</i>,
   * but <i>NoPadding</i> doesn't really make sense for general usage because that only works for input lengths
   * that are multiples of block size (i.e. divisible by {@value AESConstants#BLOCK_SIZE})
   */
  public static final String TRANSFORMATION_SPEC = AES_ALGORITHM + "/CBC/PKCS5Padding";

  @Override
  public Mode getMode() {
    return Mode.CBC;
  }

  @Override
  public String getTransformationSpec() {
    return TRANSFORMATION_SPEC;
  }

  @Override
  public boolean isPaddingUsed() {
    return true;
  }

  @Override
  public AESParameterSpec createAlgorithmParameterSpec(byte[] iv) {
    return new FastIvParameterSpec(iv);
  }

  @Override
  public AESParameterSpec createAlgorithmParameterSpec(byte[] iv, int offset, int len) {
    return new FastIvParameterSpec(iv, offset, len);
  }

}
