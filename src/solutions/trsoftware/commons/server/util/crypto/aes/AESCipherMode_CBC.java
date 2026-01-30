package solutions.trsoftware.commons.server.util.crypto.aes;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import static solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.AES_ALGORITHM;

/**
 * Parameters for the AES encryption algorithm in {@link AESConstants.Mode#CBC CBC} mode
 * with {@code PKC5Padding} ({@value #TRANSFORMATION_SPEC}).
 * <p>
 * <b>Note:</b> In contrast to {@link AESCipherMode_GCM GCM}, the CBC mode ensures only confidentiality but
 * <i>does not provide authentication</i>, therefore GCM is the generally recommended mode for modern applications.
 * The main advantage of CBC is that it's slightly faster than GCM (~25% faster, benchmarked on Oracle JDK8/Windows 8/Intel i7),
 * and allows reusing the same initialization vector (IV) without sacrificing confidentiality, which can provide
 * an additional speed boost (since {@link SecureRandom} is fairly slow).
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
  protected String getTransformationSpec() {
    return TRANSFORMATION_SPEC;
  }

  @Override
  protected boolean isPaddingUsed() {
    return true;
  }

  @Override
  protected AlgorithmParameterSpec createAlgorithmParameterSpec(byte[] iv) {
    return new FastIvParameterSpec(iv);
  }

  @Override
  protected AlgorithmParameterSpec createAlgorithmParameterSpec(byte[] iv, int offset, int len) {
    return new FastIvParameterSpec(iv, offset, len);
  }
  

  /**
   * Extends {@link IvParameterSpec} to avoid unnecessarily copying the IV array in constructor and {@link #getIV()}
   */
  public static class FastIvParameterSpec extends IvParameterSpec {
    private static final byte[] EMPTY_ARRAY = new byte[0];
    private final byte[] iv;

    public FastIvParameterSpec(byte[] iv) {
      // Note: passing empty array to super constructor, to minimize copying
      super(EMPTY_ARRAY);
      this.iv = iv;
    }

    public FastIvParameterSpec(byte[] src, int offset, int len) {
      /* Note: although we can't really avoid array copy here, since getIV() needs to return a complete array (not a sub-range),
       *  we're still avoiding an additional array copy in getIV()
       */
      this(Arrays.copyOfRange(src, offset, len));
    }

    @Override
    public byte[] getIV() {
      // overriding to avoid extra array clone performed by super
      return iv;
    }
  }
}
