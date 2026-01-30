package solutions.trsoftware.commons.server.auth.token;

import javax.annotation.Nonnull;

/**
 * Handles serialization and authentication (or encryption) of {@link AuthToken} instances.
 *
 * @param <T> the token type
 * @param <P> the token's payload type
 */
public interface TokenEncoder<T extends AuthToken<P>, P> {
  /**
   * Computes a serialized string representation of the given token, which can be
   * used to authenticate and reconstruct the token via {@link #decode(String)}.
   *
   * @return a serialized string representation of the given token, which can be stored in a cookie
   * and used to authenticate and reconstruct the token via {@link #decode(String)}
   */
  String encode(@Nonnull T token);

  /**
   * Parses and authenticates a serialized token string produced by {@link #encode(AuthToken)}.
   *
   * @param tokenString serialized token string produced by {@link #encode(AuthToken)}
   * @return the token reconstructed from the given string
   * @throws RuntimeException if the token string is invalid or authentication failed
   */
  T decode(@Nonnull String tokenString);
}
