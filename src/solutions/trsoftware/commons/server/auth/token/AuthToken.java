package solutions.trsoftware.commons.server.auth.token;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.shared.util.time.Clock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

import static java.util.Objects.requireNonNull;

/**
 * Represents an authentication token, which can be used to implement login cookies.
 * <p>
 * This class is used in conjunction with a {@link TokenEncoder} implementation,
 * which handles serialization and authentication (or encryption) of the token.
 *
 * @param <P> the payload type (e.g. Integer userId, or String username)
 *
 * @see EncryptedTokenEncoder
 * @see SignedByteBufferEncoder
 * @see SignedStringEncoder
 *
 * @author Alex
 * @since 12/12/2025
 */
public abstract class AuthToken<P> {

  /**
   * The data stored in this token, such as a userId, or a username.
   */
  @Nonnull
  protected final P payload;

  /**
   * A timestamp (in epoch millis) that can be used to check this token's freshness.
   * The value could be a creation timestamp (default), or an expiration time, depending on the application.
   */
  protected final long timestamp;

  /**
   * Creates a new token with the given {@link #payload} and the {@linkplain Clock#currentTimeMillis() current time}
   * as the {@link #timestamp}.
   *
   * @param payload the data stored in this token, such as a userId, or a username
   * @throws NullPointerException if {@code payload} is {@code null}
   * @see #AuthToken(Object, long)
   */
  public AuthToken(@Nonnull P payload) {
    this(payload, Clock.currentTimeMillis());
  }

  /**
   * Creates a new token with the given {@link #payload} and {@link #timestamp} value.
   *
   * @param payload the data stored in this token, such as a userId, or a username
   * @param timestamp the datetime (in epoch millis) to be associated with this token,
   *   such as the creation timestamp or expiration datetime
   *
   * @throws NullPointerException if {@code payload} is {@code null}
   */
  public AuthToken(@Nonnull P payload, long timestamp) {
    this.payload = requireNonNull(payload, "payload");
    this.timestamp = timestamp;
  }

  @Nonnull
  public P getPayload() {
    return payload;
  }

  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Parses the given token string using the specified encoder and returns the reconstructed token instance.
   * <p>
   * Returns {@code null} if {@link TokenEncoder#decode(String)} threw an exception (e.g. if the token string
   * is malformatted or authentication failed).
   *
   * @param tokenString token string to parse
   * @param parser the {@link TokenEncoder} implementation that was used to produce the given token string
   * @param <T> the token class
   * @return the parsed token or {@code null} if the token string could not be parsed
   *   (e.g. if the string is malformatted or authentication failed)
   */
  @Nullable
  public static <T extends AuthToken<?>> T parse(String tokenString, TokenEncoder<T, ?> parser) {
    // TODO: maybe delete this method?
    try {
      return parser.decode(tokenString);
    }
    catch (RuntimeException ex) {
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof AuthToken))
      return false;
    AuthToken<?> authToken = (AuthToken<?>)o;
    return timestamp == authToken.timestamp &&
        payload.equals(authToken.payload);
  }

  @Override
  public int hashCode() {
    return 31 * payload.hashCode() + Long.hashCode(timestamp);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("payload", payload)
        .add("timestamp", Instant.ofEpochMilli(timestamp))
        .toString();
  }

}
