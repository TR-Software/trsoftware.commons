package solutions.trsoftware.commons.server.util.crypto.aes;

import solutions.trsoftware.commons.server.util.crypto.SimpleKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * @author Alex
 * @since 12/5/2025
 */
public interface AESConstants {
  String AES_ALGORITHM = "AES";
  int BLOCK_SIZE = 16;  // AES block size always 16 bytes (same for GCM as CBC); see com.sun.crypto.provider.AESConstants.AES_BLOCK_SIZE

  /**
   * Ensures that the given array contains exactly 16, 24, or 32 bytes
   * (representing a 128, 192, or 256-bit AES key).
   *
   * @throws NullPointerException if the argument is null
   * @throws IllegalArgumentException if the argument does not contain the required number of bytes
   * @return the same array that was passed in
   */
  static byte[] checkKeySize(byte[] key) {
    int len = requireNonNull(key, "key").length;
    // Note: these are the same key size requirements checked by com.sun.crypto.provider.AESCrypt.isKeySizeValid (see com.sun.crypto.provider.AESConstants.AES_KEYSIZES)
    checkArgument(len == 16 || len == 24 || len == 32,
        "Key length must be 16, 24, or 32 bytes (given: %s)", len);
    return key;
  }

  /**
   * Generates a random secret key that can be used with the {@value AESConstants#AES_ALGORITHM} algorithm.
   * @return a 16-byte key
   * @see KeyGenerator
   */
  static byte[] generateKey() throws GeneralSecurityException {
    KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
    SecretKey secretKey = keyGen.generateKey();
    return secretKey.getEncoded();
  }

  /**
   * Enumerates the AES encryption modes (e.g. {@code CBC} or {@code GCM}) that can be used with an
   * {@link AESCipher} instance.
   *
   * @see AESCipherMode
   * @see AESCipherMode_CBC
   * @see AESCipherMode_GCM
   */
  enum Mode {
    /**
     * @see AESCipherMode_CBC
     * @see <a href="https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Cipher_block_chaining_(CBC)">Cipher block chaining (CBC)</a>
     */
    CBC(new AESCipherMode_CBC()),
    /**
     * @see AESCipherMode_GCM
     * @see  <a href="https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Galois/counter_(GCM)">Galois/counter Mode (GCM)</a>
     */
    GCM(new AESCipherMode_GCM()),
    ;

    private final AESCipherMode instance;

    Mode(AESCipherMode instance) {
      this.instance = instance;
    }

    public AESCipherMode getInstance() {
      return instance;
    }

    // delegated AESCipherMode methods:

    public String getTransformationSpec() {
      return instance.getTransformationSpec();
    }

    public boolean isPaddingUsed() {
      return instance.isPaddingUsed();
    }

    public int getIvLength() {
      return instance.getIvLength();
    }

    public int getOutputSize(int inputLen, int mode) {
      return instance.getOutputSize(inputLen, mode);
    }
  }

  /**
   * Same as {@link SecretKeySpec} but avoids cloning the byte array on every invocation of {@link #getEncoded()}
   */
  class AESKeySpec extends SimpleKeySpec {
    public AESKeySpec(byte[] key) {
      super(checkKeySize(key), AES_ALGORITHM);
    }
  }


  /**
   * Extends {@link AlgorithmParameterSpec} to add the {@link #getIV()} method, which is declared by
   * all AES parameter types (both {@link IvParameterSpec} and {@link GCMParameterSpec}).
   *
   * @see Cipher#init(int, Key, AlgorithmParameterSpec)
   * @see AESCipherMode#createAlgorithmParameterSpec(byte[])
   */
  interface AESParameterSpec extends AlgorithmParameterSpec {

    byte[] EMPTY_ARRAY = new byte[0];

    byte[] getIV();
  }
  
  /**
   * Extends {@link IvParameterSpec} to avoid unnecessarily copying the IV array in constructor and {@link #getIV()}.
   * This parameter type is used for {@linkplain AESCipherMode_CBC AES/CBC}.
   * 
   * @see AESCipherMode#createAlgorithmParameterSpec(byte[])
   */
  class FastIvParameterSpec extends IvParameterSpec implements AESParameterSpec {
    private final byte[] iv;

    public FastIvParameterSpec(byte[] iv) {
      // Note: passing empty array to super constructor, to minimize copying
      super(EMPTY_ARRAY);
      this.iv = iv;
    }

    public FastIvParameterSpec(byte[] src, int offset, int len) {
      /* Note: although we can't really avoid array copy here, since getIV() needs to return a complete array (not a sub-range),
         we're still avoiding an additional array copy in getIV() */
      this(Arrays.copyOfRange(src, offset, len));
    }

    @Override
    public byte[] getIV() {
      // overriding to avoid extra array clone performed by super
      return iv;
    }
  }

  /**
   * Extends {@link GCMParameterSpec} to avoid unnecessarily copying the IV array in constructor and {@link #getIV()}.
   * This parameter type is used for {@linkplain AESCipherMode_GCM AES/GCM}.
   * 
   * @see AESCipherMode#createAlgorithmParameterSpec(byte[])
   */
  class FastGCMParameterSpec extends GCMParameterSpec implements AESParameterSpec {
    private final byte[] iv;

    public FastGCMParameterSpec(byte[] iv) {
      // Note: passing empty array to super constructor, to minimize copying
      super(AESCipherMode_GCM.TAG_LENGTH_BITS, EMPTY_ARRAY);
      this.iv = iv;
    }

    public FastGCMParameterSpec(byte[] src, int offset, int len) {
      /* Note: although we can't really avoid array copy here, since getIV() needs to return a complete array (not a sub-range),
         we're still avoiding an additional array copy in getIV() */
      this(Arrays.copyOfRange(src, offset, len));
    }

    @Override
    public byte[] getIV() {
      // overriding to avoid extra array clone performed by super
      return iv;
    }
  }
}
