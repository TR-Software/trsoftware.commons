package solutions.trsoftware.commons.server.auth.token;

import solutions.trsoftware.commons.server.util.crypto.CryptoCipher;
import solutions.trsoftware.commons.server.util.crypto.aes.AESCipher;
import solutions.trsoftware.commons.server.util.crypto.aes.AESCipherMode_GCM;
import solutions.trsoftware.commons.server.util.crypto.aes.AESConstants.Mode;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

/**
 * Encodes/decodes tokens as a url-safe base64 string encoding of an encrypted byte array with the following format:
 * <code><nobr>
 *   [{@link #getTypePrefix() header} bytes]
 *   [{@link AuthToken#timestamp timestamp} (8 bytes)]
 *   [{@link #writePayload payload} bytes]
 * </nobr></code>
 * <p>
 * The recommended encryption scheme is {@value AESCipherMode_GCM#TRANSFORMATION_SPEC}, which guarantees both
 * confidentiality and authentication.  This can be achieved by implementing {@link #getCipher()} to return
 * an instance of {@link AESCipher} configured in {@link Mode#GCM GCM} mode.
 *
 * @param <T> the token type
 * @param <P> the token's payload type
 */
public abstract class EncryptedTokenEncoder<T extends AuthToken<P>, P> extends ByteBufferEncoder<T, P> {

  /**
   * Returns the cipher to use for encryption/authentication of the token.
   * <p>
   * The recommended encryption scheme {@value AESCipherMode_GCM#TRANSFORMATION_SPEC}, which guarantees both
   * confidentiality and authentication.  Therefore we suggest supplying an instance of
   * {@link AESCipher} configured in {@link Mode#GCM GCM} mode.
   *
   * @return the cipher to use for encryption
   */
  protected abstract CryptoCipher getCipher();


  @Override
  protected final String encodeToString(ByteBuffer byteBuffer) {
    try {
      return getCipher().encrypt(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.position(), getBase64Encoder());
    }
    catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected final ByteBuffer decodeToByteBuffer(String tokenString) {
    byte[] decrypted;
    try {
      decrypted = getCipher().decrypt(tokenString, getBase64Decoder());
    }
    catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
    return ByteBuffer.wrap(decrypted);
  }


}
