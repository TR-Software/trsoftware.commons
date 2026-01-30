package solutions.trsoftware.commons.server.auth.token;

import com.google.common.base.Preconditions;
import solutions.trsoftware.commons.server.util.crypto.MacFunction;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Encodes/decodes tokens as a string consisting of 4 parts delimited by {@value #DEFAULT_DELIMITER}, e.g.
 *   "{@code prefix|payload|timestamp|signature}".
 * <p>
 * The delimiter can be customized using the {@link #SignedStringEncoder(String)} constructor
 * if the {@linkplain #encodePayload(Object) payload string} might contain {@value #DEFAULT_DELIMITER} characters.
 * <p>
 * <b>Note:</b> although this class provides authentication (using a MAC signature), it does not ensure
 * confidentiality because neither the payload nor timestamp are obfuscated.  If confidentiality is also needed,
 * use {@link EncryptedTokenEncoder} instead.
 *
 * @param <T> the token type
 * @param <P> the token's payload type
 */
public abstract class SignedStringEncoder<T extends AuthToken<P>, P> implements TokenEncoder<T, P> {

  /**
   * Default value for {@link #delimiter}
   */
  public static final String DEFAULT_DELIMITER = "|";

  /**
   * Separator for the distinct parts of the token string
   */
  private final String delimiter;

  protected SignedStringEncoder() {
    this(DEFAULT_DELIMITER);
  }

  /**
   * @param delimiter separator for the distinct parts of the encoded string;
   *   must be a char sequence that can never appears in the {@linkplain #encodePayload(Object) payload string},
   *   {@linkplain #getTypePrefix() prefix}, decimal encoding of a {@code long}.
   *
   */
  public SignedStringEncoder(String delimiter) {
    this.delimiter = requireNonNull(delimiter, "delimiter");
    // make sure this delimiter doesn't occur in prefix or timestamp
    Preconditions.checkArgument(!getTypePrefix().contains(delimiter) && delimiter.codePoints().noneMatch(Character::isDigit),
        "Ambiguous delimiter sequence ('%s')", delimiter);
    // Note: can't verify that delimiter won't appear in encodePayload until we actually use it
  }

  /**
   * Encodes the given token into a string consisting of 4 parts delimited by {@link #delimiter}:
   * (e.g. {@code prefix|payload|timestamp|signature} if {@link #delimiter} is {@code "|"}).
   *
   * @throws IllegalArgumentException if the result of {@link #encodePayload(Object)} contains the {@link #delimiter} sequence
   */
  @Override
  public String encode(@Nonnull T token) {
    P payload = token.getPayload();
    long timestamp = token.getTimestamp();
    StringJoiner joiner = getBaseStringJoiner(getPayloadString(payload), timestamp);

    // so far, joiner contains "prefix|payload|timestamp", which we use to compute the signature
    String baseString = joiner.toString();
    String signature = requireNonNull(computeSignature(baseString), "computeSignature");
    // join the signature to produce the full token string ("prefix|payload|timestamp|signature")
    joiner.add(signature);
    return joiner.toString();
  }

  /**
   * Invokes {@link #encodePayload(Object)} and verifies that the result doesn't contain the {@link #delimiter} sequence.
   *
   * @return result of {@link #encodePayload(Object)}
   * @throws IllegalArgumentException if the result of {@link #encodePayload(Object)} contains the {@link #delimiter}
   */
  private String getPayloadString(P payload) {
    String payloadString = encodePayload(payload);
    Preconditions.checkArgument(!payloadString.contains(delimiter),
        "encodePayload returned a string that contains the delimiter (\"%s\"): \"%s\"", delimiter, payloadString);
    return payloadString;
  }

  /**
   * Parses the string representation of a token.
   * <p>
   * Note: this method checks the token's signature but doesn't check whether the token is expired,
   * which should be done separately by checking the token's {@link AuthToken#timestamp timpestamp}
   *
   * @param tokenString token string consisting of 4 parts separated by {@link #delimiter}:
   *   (e.g. {@code prefix|payload|timestamp|signature} if {@link #delimiter} is {@code "|"})
   *
   * @throws IllegalArgumentException if the token string is malformatted
   * @throws SecurityException if the token data doesn't have the expected signature
   * @throws NullPointerException if token string is {@code null}
   */
  @Override
  public T decode(@Nonnull String tokenString) {
    // TODO: maybe define custom exception types for this class?
    requireNonNull(tokenString, "tokenString");
    String expectedPrefix = getTypePrefix();
    List<String> parts = StringUtils.split(tokenString, delimiter);
    if (parts.size() != 4 || !expectedPrefix.equals(parts.get(0))) {
      throw new IllegalArgumentException(format("Malformatted %s string (\"%s\")", getClass().getSimpleName(), tokenString));
    }
    // init fields from the parts
    String payloadString = parts.get(1);
    P payload = requireNonNull(parsePayload(payloadString), "parsePayload");
    long timestamp = Long.parseLong(parts.get(2));
    String signature = requireNonNull(parts.get(3), "signature");  // null check probably not needed, since StringUtils.split doesn't return null elements
      /* Note: an empty string is allowed for signature, b/c subclasses can impl computeSignature to return an empty string
         if they don't need authentication */

    // validate the signature
    String baseString = getBaseStringJoiner(getPayloadString(payload), timestamp).toString();
    String expectedSignature = requireNonNull(computeSignature(baseString), "computeSignature");
    if (!expectedSignature.equals(signature)) {
      throw new SecurityException("Token signature doesn't match the payload");
    }
    return createToken(payload, timestamp);
  }

  /**
   * Instantiates the appropriate {@link AuthToken} subclass with the given parameters.
   *
   * @param payload value for {@link AuthToken#payload}
   * @param timestamp value for {@link AuthToken#timestamp}
   */
  // TODO: maybe pull up createToken to a base class (and maybe allow passing a BiFunction<P, Long, T> for it to base class constructor)
  public abstract T createToken(@Nonnull P payload, long timestamp);

  /**
   * Subclasses must implement this method to parse the token string's payload component
   * into an object of the {@linkplain P expected type} for this class.
   * <p>
   * <b>Note:</b> implementors must ensure that this method is compatible with {@link #encodePayload(Object)}, i.e.
   * <nobr><code>{@link #parsePayload}({@link #encodePayload}(payload)).equals(payload)</code></nobr>.
   *
   * @param payload the payload component of the token string
   * @return the payload object parsed from the given string
   * @throws RuntimeException if the given string is malformatted
   */
  @Nonnull
  protected abstract P parsePayload(String payload);

  /**
   * Converts the given {@linkplain AuthToken#getPayload() payload} object into a string representation
   * that will appear in the complete {@linkplain #encode(AuthToken) token string}.
   * <p>
   * This implementation defaults to {@link Object#toString()}, but subclasses <em>must override this method
   * if needed to make it compatible with {@link #parsePayload(String)}</em> (i.e. must ensure that
   * <nobr><code>{@link #parsePayload}({@link #encodePayload}(payload)).equals(payload)</code></nobr>)
   * and to <em>ensure that the string does not contain the {@link #delimiter} sequence</em>.
   *
   * @param payload the token's {@linkplain AuthToken#getPayload() payload} object
   * @return a string encoding of the payload that can be parsed back to the equivalent object using {@link #parsePayload(String)}.
   */
  @Nonnull
  protected String encodePayload(P payload) {
    return payload.toString();  // subclasses can override to use something other than Object.toString
  }

  /**
   * Returns the value to use for the {@code prefix} component of the token string.
   * This implementation defaults to {@link Class#getSimpleName()}, but subclasses can override if needed.
   *
   * @return the prefix to indicate that a token string is of the expected type
   */
  public String getTypePrefix() {
    // subclasses can override if needed
    return getClass().getSimpleName();
  }

  /**
   * Computes a MAC signature for the given string (e.g. HmacSHA256)
   *
   * @param baseString the {@code prefix|payload|timestamp} part of the token string
   */
  @Nonnull
  protected String computeSignature(String baseString) {
    byte[] macBytes = getMacFunction().hashString(baseString);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(macBytes);
  }
  // TODO: maybe impl computeSignature using a MacFunction method like in SignedByteBufferEncoder

  /**
   * @return a MAC function for computing the token's signature
   */
  protected abstract MacFunction getMacFunction();

  /**
   * Returns the Base64 encoder to use for encoding the {@linkplain #computeSignature(String) signature}
   * component of the token string.
   *
   * @return URL-safe base64 encoder that omits '=' padding chars (to ensure that the string is safe for a cookie)
   */
  protected Base64.Encoder getBase64Encoder() {
    return ByteBufferEncoder.BASE64_ENCODER;
  }



  /**
   * @return a new string joiner initialized with {@code prefix|payload|timestamp}
   */
  private StringJoiner getBaseStringJoiner(String payloadString, long timestamp) {
    String typePrefix = getTypePrefix();
    return getBaseStringJoiner(delimiter, typePrefix, payloadString, timestamp);
  }

  private static StringJoiner getBaseStringJoiner(String delimiter, String typePrefix, String payloadString, long timestamp) {
    StringJoiner joiner = new StringJoiner(delimiter);
    joiner.add(typePrefix);
    joiner.add(payloadString);
    joiner.add(Long.toString(timestamp));
    return joiner;
  }
}
