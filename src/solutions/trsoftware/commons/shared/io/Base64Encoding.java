package solutions.trsoftware.commons.shared.io;

import com.google.common.base.MoreObjects;
import com.google.common.io.BaseEncoding;
import solutions.trsoftware.commons.shared.util.concurrent.AtomicUtils;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Unifies the functionality of {@link Base64.Encoder} and {@link Base64.Decoder} into a single object that performs
 * both encoding and decoding with a specified base64 variant (e.g. standard or url-safe, padding or no padding).
 * This could be used as a strategy pattern object that can be passed around or stored in a constant.
 * <p>
 * This class fulfills the same role as Guava's {@link BaseEncoding}, but offers better performance:
 * {@link Base64EncodingImpl} takes advantage of Java's newer {@link Base64} facility, which is almost twice as fast
 * as Guava's implementation.  For clientside (GWT) code, it is emulated using {@link BaseEncoding}.
 * In other words, it performs better than {@link BaseEncoding} in serverside code and no worse than {@link BaseEncoding}
 * in clientside code.
 *
 * @author Alex
 * @since 12/19/2025
 */
public abstract class Base64Encoding {
  /* See perf comparison of Java's Base64 vs. Guava's BaseEncoding in solutions.trsoftware.commons.shared.io.Base64Benchmark
     (BaseEncoding is ~69% slower than Base64)
     Benchmark results spreadsheet: test/solutions/trsoftware/commons/shared/io/Base64EncodingBenchmark.ods
   */

  /**
   * {@code true} if using the "URL and Filename safe" ("base64url") encoding
   * (<a href="http://tools.ietf.org/html/rfc4648#section-5">RFC 4648 section 5</a>);
   * {@code false} if using the standard "base64" encoding
   * (<a href="http://tools.ietf.org/html/rfc4648#section-4">RFC 4648 section 4</a>)
   */
  protected final boolean url;
  /**
   * {@code true} if encoding without any padding characters
   * (as specified by <a href="http://tools.ietf.org/html/rfc4648#section-3.2">RFC 4648 section 3.2: "Padding of Encoded
   * Data"</a>);
   * otherwise, will use the standard {@code '='} character for padding
   */
  protected final boolean withoutPadding;

  /**
   * @param url {@code true} if using the "URL and Filename safe" ("base64url") encoding
   *     (<a href="http://tools.ietf.org/html/rfc4648#section-5">RFC 4648 section 5: "URL and Filename Safe
   *     Alphabet"</a>);
   *     {@code false} if using the standard "base64" encoding
   *     (<a href="http://tools.ietf.org/html/rfc4648#section-4">RFC 4648 section 4</a>)
   * @param withoutPadding {@code true} if encoding without any padding characters
   *     (as specified by <a href="http://tools.ietf.org/html/rfc4648#section-3.2">RFC 4648 section 3.2: "Padding of
   *     Encoded Data"</a>);
   *     {@code false} to use the standard {@code '='} character for padding
   */
  protected Base64Encoding(boolean url, boolean withoutPadding) {
    this.url = url;
    this.withoutPadding = withoutPadding;
  }

  /**
   * Encodes the given byte array according to the encapsulated settings
   * (as configured by the {@link #url} and {@link #withoutPadding} flags)
   * @return base64 encoding of the given bytes
   */
  public abstract String encode(byte[] bytes);

  /**
   * Decodes the byte array represented by the given Base64 string.
   *
   * @param chars a base64 string matching the format recognized by this instance
   *   ({@link #isUrl()} and/or {@link #isWithoutPadding()})
   * @return the bytes decoded from the given string
   */
  public abstract byte[] decode(String chars);


  public boolean isUrl() {
    return url;
  }

  public boolean isWithoutPadding() {
    return withoutPadding;
  }

  /**
   * Returns the default implementation that uses the standard "base64" alphabet with the {@code '='} character for
   * padding.
   */
  public static Base64Encoding getInstance() {
    return getInstance(false, false);
  }

  /**
   * Returns the implementation with the specified parameters.
   *
   * @param url {@code true} if using the "URL and Filename safe" ("base64url") encoding
   *     (<a href="http://tools.ietf.org/html/rfc4648#section-5">RFC 4648 section 5: "URL and Filename Safe
   *     Alphabet"</a>);
   *     {@code false} if using the standard "base64" encoding
   *     (<a href="http://tools.ietf.org/html/rfc4648#section-4">RFC 4648 section 4</a>)
   * @param withoutPadding {@code true} to encode without any padding characters
   *     (as specified by <a href="http://tools.ietf.org/html/rfc4648#section-3.2">RFC 4648 section 3.2: "Padding of
   *     Encoded Data"</a>);
   *     {@code false} to use the standard {@code '='} character for padding
   */
  public static Base64Encoding getInstance(boolean url, boolean withoutPadding) {
//    return new Base64EncodingImpl(url, withoutPadding);
    // TODO: cache one of each type of instance as a global singleton? (perhaps using static "holder" classes?)
    return getCachedInstance(url, withoutPadding);
  }

  private static Base64Encoding getCachedInstance(boolean url, boolean withoutPadding) {
    int idx = (intValue(url) << 1) | intValue(withoutPadding);
    // using a pattern similar to lazy-init with double-checked locking, but using AtomicReferenceArray to avoid actually locking
    return AtomicUtils.computeIfAbsent(instances, idx, i -> new Base64EncodingImpl(url, withoutPadding));
  }

  private static int intValue(boolean bool) {
    return bool ? 1 : 0;
  }

  private static final AtomicReferenceArray<Base64Encoding> instances = new AtomicReferenceArray<>(4);

  /*
   NOTE: The following instance getter methods use an AtomicReferenceArray for caching, which offers performance
   that's roughly on-par with the more idiomatic holder class approach (see the commented-out code below),
   but avoids having 4 extra .class files
   */

  /**
   * @return cached instance for the standard base64 encoding scheme
   * @see <a href="http://tools.ietf.org/html/rfc4648#section-4">RFC 4648 section 4</a>
   */
  public static Base64Encoding base64() {
    return getInstance(false, false);
  }

  /**
   * @return cached instance for the standard base64 encoding scheme without padding
   * @see <a href="http://tools.ietf.org/html/rfc4648#section-3.2">RFC 4648 section 3.2: "Padding of Encoded Data"</a>
   */
  public static Base64Encoding base64NoPadding() {
    return getInstance(false, true);
  }

  /**
   * @return cached instance for the "base64url" encoding scheme
   * @see <a href="http://tools.ietf.org/html/rfc4648#section-5">RFC 4648 section 5: "URL and Filename Safe Alphabet"</a>
   */
  public static Base64Encoding base64Url() {
    return getInstance(true, false);
  }

  /**
   * @return cached instance for the "base64url" encoding scheme without padding
   * @see <a href="http://tools.ietf.org/html/rfc4648#section-5">RFC 4648 section 5: "URL and Filename Safe Alphabet"</a>
   * @see <a href="http://tools.ietf.org/html/rfc4648#section-3.2">RFC 4648 section 3.2: "Padding of Encoded Data"</a>
   */
  public static Base64Encoding base64UrlNoPadding() {
    return getInstance(true, true);
  }


  /*public static Base64Encoding base64() {
    return Base64Holder.INSTANCE;
  }

  private static class Base64Holder {
    static final Base64Encoding INSTANCE = new Base64EncodingImpl(false, false);
  }

  public static Base64Encoding base64NoPadding() {
    return Base64NoPaddingHolder.INSTANCE;
  }

  private static class Base64NoPaddingHolder {
    static final Base64Encoding INSTANCE = new Base64EncodingImpl(false, true);
  }

  public static Base64Encoding base64Url() {
    return Base64UrlHolder.INSTANCE;
  }


  private static class Base64UrlHolder {
    static final Base64Encoding INSTANCE = new Base64EncodingImpl(true, false);
  }

  public static Base64Encoding base64UrlNoPadding() {
    return Base64UrlNoPaddingHolder.INSTANCE;
  }

  private static class Base64UrlNoPaddingHolder {
    static final Base64Encoding INSTANCE = new Base64EncodingImpl(true, true);
  }*/
  

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("url", url)
        .add("withoutPadding", withoutPadding)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Base64Encoding))
      return false;
    Base64Encoding that = (Base64Encoding)o;
    return url == that.url &&
        withoutPadding == that.withoutPadding;
  }

  @Override
  public int hashCode() {
    return 31 * (url ? 1 : 0) + (withoutPadding ? 1 : 0);
  }
}
