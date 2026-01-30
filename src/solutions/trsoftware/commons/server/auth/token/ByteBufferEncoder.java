package solutions.trsoftware.commons.server.auth.token;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Base64;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Base class for token encoders that use a {@link ByteBuffer} to write/parse a token's timestamp
 * and payload.
 *
 * @author Alex
 * @since 11/3/2025
 */
public abstract class ByteBufferEncoder<T extends AuthToken<P>, P> implements TokenEncoder<T, P> {

  /**
   * URL-safe Base64 encoder without padding.
   * <p>
   * Note: the padding is not necessary for decoding the string with a {@link Base64.Decoder}.
   */
  public static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();  // without padding to make it safe for cookies
  public static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();


  /**
   * {@inheritDoc}
   *
   * @return url-safe base64-encoded string representation of the given token
   */
  @Override
  public String encode(@Nonnull T token) {
    ByteBuffer byteBuffer = encodeToByteBuffer(token);
    return encodeToString(byteBuffer);
  }

  /**
   * {@inheritDoc}
   *
   * @param tokenString url-safe base64 encoding of the token bytes, produced by {@link #encode(AuthToken)}
   *
   * @throws NullPointerException if token string is {@code null}
   * @throws IllegalArgumentException if the token string is malformatted
   * @throws RuntimeException if token authentication failed
   */
  @Override
  public T decode(@Nonnull String tokenString) {
    ByteBuffer buffer = decodeToByteBuffer(requireNonNull(tokenString, "tokenString"));
    // 1) read and validate the prefix
    byte[] expectedPrefix = getTypePrefix();
    if (expectedPrefix != null) {
      for (byte prefix : expectedPrefix) {
        if (prefix != buffer.get()) {
          // prefix mismatch
          throw new IllegalArgumentException(format("Malformatted %s string (\"%s\")", AuthToken.class.getSimpleName(), tokenString));
        }
      }
    }
    // 2) read expiration long
    long expiration = buffer.getLong();
    // 3) read and parse the payload
    assert buffer.hasRemaining();
    P payload = readPayload(buffer);
    // readPayload should've consumed all the bytes before the signature
    assert !buffer.hasRemaining();
    return createToken(payload, expiration);
  }

  /**
   * Performs the final transformation steps on the given byte buffer to produce the complete token string.
   * For example, this might involve either encryption or computing a MAC signature, followed by base64-encoding.
   *
   * @param byteBuffer the buffer created by {@link #encodeToByteBuffer(AuthToken)},
   *   containing the token's {@link #getTypePrefix() header}, {@link AuthToken#timestamp timestamp}, and {@link #writePayload payload} bytes
   * @return the final string encoding of the token, to be returned by {@link #encode(AuthToken)}
   */
  protected abstract String encodeToString(ByteBuffer byteBuffer);

  /**
   * Creates a new {@link ByteBuffer} containing the {@linkplain #getTypePrefix() type prefix},
   * {@linkplain AuthToken#getTimestamp() expiration}, and {@linkplain AuthToken#getPayload() payload}
   * of the given token.
   */
  protected ByteBuffer encodeToByteBuffer(T token) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(calcBufferSize(token));
    byte[] prefix = getTypePrefix();
    if (prefix != null) {
      byteBuffer.put(prefix);
    }
    byteBuffer.putLong(token.getTimestamp());
    writePayload(token.getPayload(), byteBuffer);
    return byteBuffer;
  }

  /**
   * Computes the {@link ByteBuffer} size required for {@linkplain #encode(AuthToken) encoding} the given token.
   *
   * @return arg for {@link ByteBuffer#allocate(int)}
   * @see #getPayloadSize(AuthToken)
   * @see #getTypePrefix()
   */
  protected int calcBufferSize(T token) {
    byte[] prefix = getTypePrefix();
    int prefixSize = prefix != null ? prefix.length : 0;
    int payloadSize = getPayloadSize(token);
    return prefixSize + payloadSize + Long.BYTES /* expiration long */;
  }

  /**
   * @return number of bytes needed to encode the token's payload, used to calculate the required byte buffer size
   * @see #calcBufferSize(AuthToken)
   */
  public abstract int getPayloadSize(T token);

  /**
   * Writes the given payload object into the given byte buffer
   * (at the buffer's current {@linkplain ByteBuffer#position() position}).
   * <p>
   * <b>Note:</b> implementors must ensure that this method is compatible with {@link #readPayload(ByteBuffer)},
   * i.e. must ensure that <nobr><code>{@link #readPayload}({@link #writePayload}(payload)).equals(payload)</code></nobr>.
   *
   * @param payload the payload object
   */
  protected abstract void writePayload(P payload, ByteBuffer byteBuffer);

  /**
   * Parses the token's payload data from the given byte buffer.
   * <p>
   * <b>Note:</b> implementors must ensure that this method is compatible with {@link #writePayload}.
   *
   * @param byteBuffer buffer containing the payload data in its {@linkplain ByteBuffer#remaining() bytes}
   */
  @Nonnull
  protected abstract P readPayload(ByteBuffer byteBuffer);  // TODO(12/11/2025): why declare Exception?


  /**
   * Instantiates the appropriate {@link AuthToken} subclass with the given parameters.
   *
   * @param payload value for {@link AuthToken#payload}
   * @param timestamp value for {@link AuthToken#timestamp}
   */
  // TODO: maybe pull up createToken to a base class (and maybe allow passing a BiFunction<P, Long, T> for it to that base class constructor)
  public abstract T createToken(@Nonnull P payload, long timestamp);

  /**
   * Returns the value to use for the leading bytes (i.e. header) of the token buffer to indicate the type of the token,
   * similar to the "magic number" of a file format.
   * <p>
   * The recommended number of bytes to use for the header is 3 or 6 if it's desirable that the prefix of the
   * base64-encoded string stands out from the rest of the token data
   * (e.g. {@code Base64.getUrlDecoder().decode("LoginTok")} results in a human-readable prefix of "LoginTok"
   * for all tokens with this header).
   * Note: this recommendation doesn't apply if the token is {@linkplain EncryptedTokenEncoder encrypted}, since the prefix
   * would be encrypted as well.
   *
   * @return the prefix to indicate that a token string is of the expected type, or
   *   {@code null} if prefix not needed
   */
  @Nullable
  public abstract byte[] getTypePrefix();

  protected abstract ByteBuffer decodeToByteBuffer(String tokenString);

  /**
   * @return URL-safe base64 encoder that omits '=' padding chars (to ensure that the string is safe for a cookie)
   */
  protected Base64.Encoder getBase64Encoder() {
    return BASE64_ENCODER;
  }

  /**
   * @return URL-safe base64 decoder that allows decoding base64 strings encoded with {@link #getBase64Encoder()}
   */
  protected Base64.Decoder getBase64Decoder() {
    return BASE64_DECODER;
  }
}
