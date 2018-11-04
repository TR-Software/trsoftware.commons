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
 * Provides 2 encodings:
 *
 * Base 64:
 * A variant of b64 specifically taylored for encoding numbers as opposed
 * to bytes of character data.  The result using a url-safe
 * encoding in base64.  The base 64 algorithm is customized for encoding
 * a 128-bit number, and hence avoids the trailing padding characters normally
 * present when using the standard base64 encoding on character data.
 *
 * WARNING: this encoding doesn't preserve the sign of number, because it
 * pads the bitstring with zeros. If you want to decode this encoding safely,
 * you have to count the number of zero bits that were added.  But we don't
 * really care about decoding for generating UUIDs and stuff like that.
 *
 *
 * Base 62:
 * This is the largest radix that can be used to produce only ASCII characters
 * and digits. The ordering is totally different from b64, in that this codec
 * starts with 0-1, then a-f, then A-Z (similar to hexidecimal, but more characters)
 *
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
   * @param i the value to encode
   *
   * @return the number encoded in a url-safe base64 alphabet
   *
   * WARNING: this encoding doesn't preserve the sign of number, because it
   * pads the bitstring with zeros. If you want to decode this encoding safely,
   * you have to count the number of zero bits that were added.  But we don't
   * really care about decoding for generating UUIDs and stuff like that.
   */
  public static String toStringBase64(long i) {
    return toStringBase64(0, i);
  }

  /**
   * Uses a version of the base 64 encoding customized for encoding a single number
   * rather than character data, and hence avoids the trailing padding characters
   * normally present when using standard base64 encoding on character data.
   *
   * @param msb the most significant 64 bits of a 128-bit number
   * @param msb the least significant 64 bits of a 128-bit number
   *
   * @return the number encoded in a url-safe base64 alphabet
   *
   * WARNING: this encoding doesn't preserve the sign of number, because it
   * pads the bitstring with zeros. If you want to decode this encoding safely,
   * you have to count the number of zero bits that were added.  But we don't
   * really care about decoding for generating UUIDs and stuff like that.
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

  /** Copied from Long.toString(i, radix), and slightly modified */
  public static String toStringBase62(long i) {
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

}
