package solutions.trsoftware.commons.server.util.crypto.aes;

import solutions.trsoftware.commons.server.util.crypto.SimpleKeySpec;

import javax.crypto.spec.SecretKeySpec;

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
     * @see <a href="https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Cipher_block_chaining_(CBC)">Cipher block chaining (CBC)</a>
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
}
