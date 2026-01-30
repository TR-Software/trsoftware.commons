package solutions.trsoftware.commons.server.auth.token;

import solutions.trsoftware.commons.server.util.crypto.MacFunction;
import solutions.trsoftware.commons.server.util.crypto.aes.AESCipherMode_GCM;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Encodes/decodes tokens as a url-safe base64 string encoding of an encrypted byte array with the following format:
 * <code><nobr>
 *   [{@link #getTypePrefix() header} bytes]
 *   [{@link AuthToken#timestamp timestamp} (8 bytes)]
 *   [{@link #writePayload payload}]
 *   [{@link #getMacFunction() MAC signature}]
 * </nobr></code>
 * <p>
 * <b>Note:</b> although this class provides authentication (using a MAC signature), it does not ensure
 * confidentiality because the payload and timestamp are obfuscated only by the base64 encoding (which can easily be reversed).
 * If confidentiality is needed in addition to authentication, use {@link EncryptedTokenEncoder}
 * with a {@value AESCipherMode_GCM#TRANSFORMATION_SPEC} cipher.
 *
 * @param <T> the token type
 * @param <P> the token's payload type
 * @see EncryptedTokenEncoder
 */
public abstract class SignedByteBufferEncoder<T extends AuthToken<P>, P> extends ByteBufferEncoder<T, P> {

  /**
   * @return a MAC function for computing the token's signature
   */
  protected abstract MacFunction getMacFunction();

  @Override
  protected final String encodeToString(ByteBuffer byteBuffer) {
    /* Note: although the following code could be simplified by using Guava's BaseEncoding class, which allows encoding a range of an array,
       (e.g. BaseEncoding.base64Url().encode(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.position())),
       Java's Base64.Encoder class is actually much faster than Guava's BaseEncoding (since it doesn't use a StringBuilder)
       The only downside is that we might have to use Arrays.copyOfRange if the buffer has any extra bytes remaining,
       but that's very unlikely, assuming that our calcBufferSize method was accurate
     */
    byte[] toEncode = !byteBuffer.hasRemaining()
        ? byteBuffer.array()  // can just encode the full backing array
        // otherwise, have to trim the unused portion of the buffer
        : Arrays.copyOfRange(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.position());

    return getBase64Encoder().encodeToString(toEncode);
  }

  protected final ByteBuffer decodeToByteBuffer(String tokenString) {
    byte[] bytes = getBase64Decoder().decode(tokenString);
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    // validate and remove the signature
    int macLength = getMacLength();
    // the signature is in the last macLength bytes of the buffer; treat everything before that as the payload
    int payloadEnd = buffer.limit() - macLength;
    buffer.position(payloadEnd);
    validateSignature(buffer);
    // "discard" the signature by reverting the position to 0 and limiting it at the signature start position
    buffer.rewind().limit(payloadEnd);
    return buffer;
  }

  @Override
  protected final ByteBuffer encodeToByteBuffer(T token) {
    ByteBuffer buffer = super.encodeToByteBuffer(token);
    // generate and append the signature for the bytes that have been written up to the current position in the buffer
    appendSignature(buffer);
    return buffer;
  }

  @Override
  protected int calcBufferSize(T token) {
    // add the MAC signature length
    return super.calcBufferSize(token) + getMacLength();
  }

  /**
   * Returns the number of bytes produced by the {@linkplain #getMacFunction() MAC instance}.
   * This value is used to determine the required {@linkplain #calcBufferSize(AuthToken) buffer size}.
   *
   * @return the MAC length in bytes
   * @see MacFunction#getMacLength()
   */
  private int getMacLength() {
    return getMacFunction().getMacLength();
  }

  /**
   * Verifies that the bytes starting at the current {@link ByteBuffer#position() position} in the given
   * buffer match the expected MAC signature for all the preceding bytes.
   * <p>
   * This method returns normally if the signature is correct or throws a {@link SecurityException} otherwise.
   * The buffer's {@linkplain ByteBuffer#position() position} upon return will be somewhere between
   * its former position and {@link ByteBuffer#limit() limit}.
   *
   * @param buffer buffer with current {@linkplain ByteBuffer#position() position} indicating the beginning of the signature bytes
   * @throws SecurityException if the signature doesn't match the payload data
   */
  protected void validateSignature(ByteBuffer buffer) throws SecurityException {
    // compute the expected signature for the bytes that have been read up to this point
    byte[] expectedSignature = computeSignature(buffer);
    if (expectedSignature.length != buffer.remaining()) {
      throw new SecurityException("Token signature doesn't match the payload");
    }
    for (byte b : expectedSignature) {
      if (b != buffer.get())
        throw new SecurityException("Token signature doesn't match the payload");
    }

  }

  /**
   * Signs the bytes that have been written up to the current position in the buffer array and appends the signature to the buffer
   */
  private void appendSignature(ByteBuffer byteBuffer) {
    // NOTE: although it seems tempting to use Mac.doFinal(byte[], int) to generate the MAC directly into the buffer,
    // that doesn't actually avoid creating an intermediate array in Mac.doFinal
    byte[] signature = computeSignature(byteBuffer);
    byteBuffer.put(signature);
  }

  /**
   * Uses the {@linkplain #getMacFunction() MAC function} to generate a signature for the bytes up to the current position in the given buffer.
   * @return MAC hash code for the bytes in the given buffer
   */
  private byte[] computeSignature(ByteBuffer byteBuffer) {
    // sign the bytes that have been written up to the current position in the buffer array
    return getMacFunction().hash(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.position() - byteBuffer.arrayOffset());
  }

}
