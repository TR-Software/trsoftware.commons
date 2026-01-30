package solutions.trsoftware.commons.server.util.crypto.mac;

import com.google.common.base.MoreObjects;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import solutions.trsoftware.commons.server.util.crypto.MacFunction;
import solutions.trsoftware.commons.server.util.crypto.SimpleKeySpec;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Encapsulates the parameters needed to create {@link Mac} instances implementing a specified MAC algorithm
 * and initialized with a specified secret key.
 * <p>
 * The {@link #hash} operations are implemented using a clone of a pre-initialized {@link Mac} instance,
 * thereby replacing the runtime cost of a {@link Mac#getInstance(String)} and {@link Mac#init(Key)} with
 * a cheaper {@link Mac#clone()} invocation.
 * This is the same approach used by Guava's {@link com.google.common.hash.MacHashFunction MacHashFunction}
 * (e.g. {@link Hashing#hmacSha256(Key)}), but creates less garbage by skipping an intermediate {@link Hasher} object.
 *
 * @author Alex
 * @since 12/16/2025
 */
public class MacFunctionPrototype implements MacFunction {
  private final String algorithm;
  private final SecretKey secretKey;
  protected final Mac prototype;  // cloned before each use
  /**
   * Cached value of {@link Mac#getMacLength()}
   */
  private final int macLength;
  private volatile boolean supportsClone = true;

  /**
   * @param algorithm the standard name of the requested MAC algorithm (e.g. "HmacSHA256")
   *   See the Mac section in the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac">
   *   Java Cryptography Architecture Standard Algorithm Name Documentation</a>
   *   for information about standard algorithm names.
   * @param key the key material of the secret key
   * @throws RuntimeException if the algorithm is not supported or the key is inappropriate for initializing this MAC
   */
  public MacFunctionPrototype(String algorithm, byte[] key) {
    this(algorithm, new SimpleKeySpec(key, algorithm));
  }

  /**
   * @param algorithm the standard name of the requested MAC algorithm (e.g. "HmacSHA256")
   *   See the Mac section in the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac">
   *   Java Cryptography Architecture Standard Algorithm Name Documentation</a>
   *   for information about standard algorithm names.
   * @param key the secret key for {@link Mac#init(Key)}
   * @throws RuntimeException if the algorithm is not supported or the key is inappropriate for initializing this MAC
   */
  public MacFunctionPrototype(String algorithm, SecretKey key) {
    this.algorithm = algorithm;
    this.secretKey = key;
    /* Create a "prototype" Mac instance to fail early if the algorithm is not supported or the key is invalid
       This also allows caching getMacLength() and cloning the Mac in getMac()
     */
    prototype = getMac(algorithm, key);
    macLength = prototype.getMacLength();
  }


  /**
   * Creates a new {@link Mac} object initialized with the given key.
   *
   * @param algorithm the standard name of the requested MAC algorithm (e.g. "HmacSHA256")
   *   See the Mac section in the <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Mac">
   *   Java Cryptography Architecture Standard Algorithm Name Documentation</a>
   *   for information about standard algorithm names.
   * @param key the secret key for {@link Mac#init(Key)}
   * @return a new {@link Mac} instance initialized with the given key,
   *   ready to perform {@code update} and {@code doFinal} operations
   * @throws RuntimeException if {@link Mac#getInstance(String)} or {@link Mac#init(Key)} threw an exception
   *   (i.e. if the algorithm is not supported or the key is inappropriate for initializing the MAC)
   */
  public static Mac getMac(String algorithm, Key key) {
    try {
      Mac mac = Mac.getInstance(algorithm);
      mac.init(key);
      return mac;
    }
    catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a new {@link Mac} instance initialized with the {@link #secretKey}.
   *
   * @return a new {@link Mac} instance, initialized and ready to perform {@code update} and {@code doFinal} operations
   */
  public Mac getMac() {
    if (supportsClone) {
      try {
        return (Mac)prototype.clone();
      }
      catch (CloneNotSupportedException e) {
        // falls through to Mac.getInstance
        supportsClone = false;  // don't try it again next time
      }
    }
    return getMac(this.algorithm, this.secretKey);
  }

  @Override
  public byte[] hash(byte[] input, int offset, int len) {
    return MacFunction.hash(getMac(), input, offset, len);
  }

  @Override
  public int getMacLength() {
    return macLength;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .addValue(StringUtils.quote(algorithm))
        .toString();
  }
}
