/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.util;

/**
 * Provides 2 custom radix encodings:
 * <ol>
 * <li>
 * <b>Base 64</b>:
 * A variant of the URL-safe Base 64 encoding standard, specifically tailored for encoding
 * 128-bit integers, using the smallest possible number of output characters.
 * In particular, this scheme avoids the need for any padding characters that you often see at
 * the end of traditional base64 strings.
 * <p>
 * <em>This is a lossy encoding</em>:
 * in order to avoid padding chars, it doesn't preserve the sign of integer
 * (leading 0-bits will be prepended to its binary representation such that the total number
 * of bits is divisible by 6).  It was designed for the purpose of representing
 * {@link java.util.UUID} values as url-safe strings of the shortest possible length, and
 * reversibility was not a requirement.
 * </li>
 * <li>
 * <b>Base 62</b>:
 * This is the largest radix that produces strings containing only Latin letters and decimal digits
 * using the the same encoding scheme as the {@linkplain Character#MAX_RADIX radix}-based
 * string conversions implemented by {@link Integer#toString(int, int)} and {@link Long#toString(long, int)}.
 * The characters ordering starts with [0-9], then [a-z], then [A-Z]. This is similar to hexadecimal,
 * but the opposite of Base64.
 * </li>
 * </ol>
 *
 * @author Alex
 */
public class NumberRadixEncoder {

  final static char[] b64Alphabet = new char[64];
  // Populate the lookup character array with the same characters as the UrlSafeBase64 class uses
  static {
    for (int i = 0; i <= 25; i++) {
      b64Alphabet[i] = (char) ('A' + i);
    }
    for (int i = 26, j = 0; i <= 51; i++, j++) {
      b64Alphabet[i] = (char) ('a' + j);
    }
    for (int i = 52, j = 0; i <= 61; i++, j++) {
      b64Alphabet[i] = (char) ('0' + j);
    }
    b64Alphabet[62] = '-';
    b64Alphabet[63] = '_';
  }

  final static char[] b62Alphabet = {
      '0' , '1' , '2' , '3' , '4' , '5' ,
      '6' , '7' , '8' , '9' , 'a' , 'b' ,
      'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
      'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
      'o' , 'p' , 'q' , 'r' , 's' , 't' ,
      'u' , 'v' , 'w' , 'x' , 'y' , 'z',
      'A' , 'B' ,
      'C' , 'D' , 'E' , 'F' , 'G' , 'H' ,
      'I' , 'J' , 'K' , 'L' , 'M' , 'N' ,
      'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
      'U' , 'V' , 'W' , 'X' , 'Y' , 'Z'
  };


  /**
   * Uses a version of the base 64 encoding customized for encoding a single number
   * rather than character data, and hence avoids the trailing padding characters
   * normally present when using standard base64 encoding on character data.
   *
   * <p>
   *   <em>This is a lossy encoding:</em>
   *   in order to avoid padding chars, it doesn't preserve the sign of integer
   *   (leading 0-bits will be prepended to its binary representation such that the total number
   *   of bits is divisible by 6).  It was designed for the purpose of representing
   *   {@link java.util.UUID} values as url-safe strings of the shortest possible length, and
   *   reversibility was not a requirement.
   * </p>
   *
   * @param i the value to encode
   * @return the number encoded in a url-safe base64 alphabet
   */
  public static String toStringBase64(long i) {
    return toStringBase64(0, i);
  }

  /**
   * Uses a version of the base 64 encoding customized for encoding a single number
   * rather than character data, and hence avoids the trailing padding characters
   * normally present when using standard base64 encoding on character data.
   *
   * <p>
   *   <em>This is a lossy encoding:</em>
   *   in order to avoid padding chars, it doesn't preserve the sign of integer
   *   (leading 0-bits will be prepended to its binary representation such that the total number
   *   of bits is divisible by 6).  It was designed for the purpose of representing
   *   {@link java.util.UUID} values as url-safe strings of the shortest possible length, and
   *   reversibility was not a requirement.
   * </p>
   *
   *
   * @param msb the most significant 64 bits of a 128-bit number
   * @param lsb the least significant 64 bits of a 128-bit number
   * @return the number encoded in a url-safe base64 alphabet
   *
   * @see SimpleUUID#randomUUID()
   */
  public static String toStringBase64(long msb, long lsb) {
    if (msb == 0 && lsb == 0)
      return "0";
    StringBuilder bits = new StringBuilder(Long.toBinaryString(msb)).append(Long.toBinaryString(lsb));
    // delete all the leading zeros
    for (int i = 0; i < bits.length(); i++) {
      if (bits.charAt(i) == '0')
        bits.deleteCharAt(i);
      else
        break;
    }
    // now pad with enough leading zeros to make the total number of bits divisible by 6
    // (one b64 alphabet character is used for every 6 bits)
    while (!(bits.length() % 6 == 0)) {
      bits.insert(0, '0');
    }
    int encodingLength = bits.length() / 6;
    StringBuilder result = new StringBuilder(encodingLength);
    for (int i = 0; i < bits.length(); i+=6) {
      byte val = Byte.parseByte(bits.substring(i, i + 6), 2);
      assert val < 64;
      result.append(b64Alphabet[val]);
    }
    return result.toString();
  }

  /**
   * Produces a string representation of the given {@code long} in base 62 using the alphabet {@code [0-9][a-z][A-Z]}.
   * <p>
   * This encoding algorithm works in the same manner as you might expect from {@link Long#toString(long, int) Long.toString(i, 62)}
   * (if 62 would have been
   * a {@linkplain Character#MAX_RADIX valid radix} for integer-string conversions in the {@link java.lang} classes).
   */
  public static String toStringBase62(long i) {
    // NOTE: this code was copied from Long.toString(i, radix), and slightly modified
    int radix = 62;
    char[] buf = new char[65];
    int charPos = 64;
    boolean negative = (i < 0);

    if (!negative) {
      i = -i;
    }

    while (i <= -radix) {
      buf[charPos--] = b62Alphabet[(int)(-(i % radix))];
      i = i / radix;
    }
    buf[charPos] = b62Alphabet[(int)(-i)];

    if (negative) {
      buf[--charPos] = '-';
    }

    return new String(buf, charPos, (65 - charPos));
  }

  // TODO: consider adding support encoding BigInteger in base62 (might be able to use an algorithm similar to Long.toString(i, radix))

}
