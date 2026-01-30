package solutions.trsoftware.commons.server.util.crypto;

import solutions.trsoftware.commons.server.util.crypto.mac.ConcurrentMacFunction;
import solutions.trsoftware.commons.server.util.crypto.mac.GuavaHashFunction;
import solutions.trsoftware.commons.server.util.crypto.mac.MacFunctionPrototype;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;

/**
 * Provides a simple high-level interface for computing MAC hashes, hiding the complexity of working with
 * {@link javax.crypto.Mac} instances directly.
 *
 * @see MacFunctionPrototype
 * @see ConcurrentMacFunction
 * @see GuavaHashFunction
 * @see com.google.common.hash.HashFunction
 * @see com.google.common.hash.Hashing
 *
 * @author Alex
 * @since 12/16/2025
 *
 */
public interface MacFunction {

  /**
   * Returns the length of the MAC in bytes.
   *
   * @return the MAC length in bytes.
   */
  int getMacLength();

  /**
   * Computes the MAC for the given array of bytes.
   *
   * @param input the array of bytes to be processed
   * @return the computed MAC
   */
  default byte[] hash(byte[] input) {
    return hash(input, 0, input.length);
  }

  /**
   * Computes the MAC for the first {@code len} bytes in {@code input},
   * starting at {@code offset} inclusive.
   *
   * @param input the input buffer.
   * @param offset the offset in {@code input} where the input starts.
   * @param len the number of bytes to process.
   * @return the computed MAC
   */
  byte[] hash(byte[] input, int offset, int len);

  /**
   * Computes the MAC for the {@code UTF-8} encoding of the given string.
   *
   * @param input the input char sequence
   * @return the computed MAC
   */
  default byte[] hashString(CharSequence input) {
    return hash(input.toString().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Helper for {@link #hash(byte[], int, int)}: uses the given {@link Mac} instance to compute the hash using
   * the given input parameters.
   * @return the result of {@link Mac#doFinal()}
   */
  static byte[] hash(Mac mac, byte[] input, int offset, int len) {
    mac.update(input, offset, len);
    return mac.doFinal();
  }

}
