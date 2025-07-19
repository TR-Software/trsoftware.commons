/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.io.codec;

import java.math.BigInteger;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * For encoding arbitrary-precision integers into any base.  Officially,
 * only supports a limited number of such bases - those that are represented
 * by constant fields in this class.  However, this class is, in theory,
 * infinitely extensible by providing alternate implementations of the
 * {@link Alphabet} interface.
 *
 * Instances of this class are immutable.
 *
 * @author Alex
 */
public class BigIntRadixCodec {

  // the most useful bases are represented as constants

  public static final BigIntRadixCodec BASE_2 = new BigIntRadixCodec(2, SmallRadixAlphabet.getInstance());
  public static final BigIntRadixCodec BASE_8 = new BigIntRadixCodec(8, SmallRadixAlphabet.getInstance());
  public static final BigIntRadixCodec BASE_16 = new BigIntRadixCodec(16, SmallRadixAlphabet.getInstance());
  public static final BigIntRadixCodec BASE_32 = new BigIntRadixCodec(32, SmallRadixAlphabet.getInstance());
  public static final BigIntRadixCodec BASE_36 = new BigIntRadixCodec(36, SmallRadixAlphabet.getInstance());  // base 36 is useful because it uses the alphabet [0-9a-z]
  public static final BigIntRadixCodec BASE_62 = new BigIntRadixCodec(62, BigRadixAlphabet.getInstance());  // base 62 is useful because it uses the alphabet [0-9a-zA-Z]
  public static final BigIntRadixCodec BASE_64 = new BigIntRadixCodec(64, Base64Alphabet.getInstance());

  /* TODO(5/21/2025): refactor this class:
      - add factory methods getting singleton BigRadixAlphabet-based and SmallRadixAlphabet-based instances
      - what's the point of SmallRadixAlphabet radix less than 36? Can just use BigInteger.toString(int) and BigInteger(String, int) constructor
      - maybe inline the AlphabetAdapter encode/decode logic and base field into this class (there's no real point for the Alphabet abstraction other than providing an array of bytes)
   */

  private final Alphabet alphabet;
  private final BigInteger radix;

  public BigIntRadixCodec(int radix, Alphabet alphabet) {
    requireNonNull(alphabet, "alphabet");
    checkArgument(radix >= 2 && radix <= alphabet.maxRadix(),
        "Radix (%s) must be in range [2, %s] for the specified alphabet", radix, alphabet.maxRadix());
    this.alphabet = alphabet;
    this.radix = BigInteger.valueOf(radix);
  }

  public int getRadix() {
    return radix.intValue();
  }

  public String encode(BigInteger input) {
    // uses repeated division by the base to obtain the output
    BigInteger remainder = input.abs();
    StringBuilder out = new StringBuilder();
    do {
      BigInteger[] quotientAndMod = remainder.divideAndRemainder(radix);
      BigInteger quotient = quotientAndMod[0];
      BigInteger mod = quotientAndMod[1];
      out.append(encode(mod.intValue()));
      remainder = quotient;
    } while (!remainder.equals(BigInteger.ZERO));

    if (input.signum() < 0)
      out.append((char)alphabet.sign());

    // the output is backwards - reverse it to restore endianness
    return out.reverse().toString();
  }

  public BigInteger decode(String input) {
    boolean negative = false;
    if (input.charAt(0) == (char)alphabet.sign()) {
      negative = true;
      input = input.substring(1);  // remove the sign char
    }

    // The algorithm follows this example for base 16, generalized to any base
    // abcd = d*16^0 + c*16^1 + b*16^2 + a*16^3

    BigInteger result = BigInteger.ZERO;
    for (int i = 0; i < input.length(); i++) {
      char lastChar = input.charAt(input.length() - 1 - i);
      int lastCharValue = decode(lastChar);
      result = result.add(BigInteger.valueOf(lastCharValue).multiply(radix.pow(i)));
    }
    if (negative)
      result = result.negate();
    return result;
  }

  /**
   * @param digit the digit to encode;  must be in range <code>[0, {@link #radix})</code>
   * @return the character representing the given digit
   * @see #decode(char)
   */
  private char encode(int digit) {
    return (char)alphabet.encode(digit);
  }

  /**
   * @param codedDigit a character representing an {@code int} in the range <code>[0, {@link #radix})</code>
   * @return the {@code int} corresponding to the given digit
   * @see #encode(int)
   */
  private int decode(char codedDigit) {
    return alphabet.decode((byte)codedDigit);
  }

  // TODO(5/18/2025): document methods

  /**
   * Encodes an unsigned binary number represented by the given byte array.
   *
   * @param bytes big-endian binary representation of the number to encode
   */
  public String encodeUnsignedBytes(byte[] bytes) {
    // TODO: this only works for unsigned numbers: document this fact (maybe rename method to encodeUnsignedBytes)
    return encode(new BigInteger(1, bytes));
  }

  /**
   * Reverse of {@link #encodeUnsignedBytes(byte[])}
   *
   * @param encoded the encoding produced by {@link #encodeUnsignedBytes(byte[])}
   * @param nBytes the expected number of bytes 
   *   (required for correct handling of the leading {@code 0} byte returned by {@link BigInteger#toByteArray()}) 
   * @return big-endian binary representation of the number encoded with {@link #encodeUnsignedBytes(byte[])},
   *   i.e. the argument passed to that method to produce the given string
   */
  public byte[] decodeUnsignedBytes(String encoded, int nBytes) {
    // TODO: this only works for unsigned numbers: document this fact
    // @see https://stackoverflow.com/a/79631341
    // TODO: reconcile code dup in MathUtils.bigIntToUnsignedByteArray
    byte[] bytes = decode(encoded).toByteArray();
    if (bytes.length > nBytes) {
      // length not what we expected: remove the extra leading 0 sign byte
      // (i.e. return the last nBytes bytes of array)
      int nExtraBytes = bytes.length - nBytes;
      // TODO: assert that only 1 extra byte?
      return Arrays.copyOfRange(bytes, nExtraBytes, bytes.length);
    }
    else if (bytes.length < nBytes) {
      // original array must've had multiple leading 0 bytes, which were dropped when BigInteger was constructed,
      // so prepend those leading 0 bytes
      int nMissingBytes = nBytes - bytes.length;
      byte[] padded = new byte[nBytes];
      System.arraycopy(bytes, 0, padded, nMissingBytes, bytes.length);
      return padded;
    }
    return bytes;
  }



//  public String encode(BigInteger input) {
//    // an integer 0..63 is represented by exactly 6 bits
//    BigInteger remainder = input.abs();
//    StringBuilder out = new StringBuilder();
//    do {
//      int lsb = remainder.intValue();
//      out.append((char)alphabet.encode(lsb & mask));
//      remainder = remainder.shiftRight(bitsPerDigit);
//    } while (!remainder.equals(BigInteger.ZERO));
//
//    if (input.signum() < 0)
//      out.append((char)alphabet.sign());
//
//    // the output is backwards - reverse it to restore endianness
//    return out.reverse().toString();
//  }
//
//  public BigInteger decode(String input) {
//    boolean negative = false;
//    if (input.charAt(0) == (char)alphabet.sign()) {
//      negative = true;
//      input = input.substring(1);
//    }
//    // an integer 0..63 is represented by exactly 6 bits
//    BigInteger result = BigInteger.ZERO;
//    for (int i = input.length()-1; i >= 0; i--) {
//      int part = alphabet.decode((byte)input.charAt(i));
//      result = result.add(BigInteger.valueOf(part).shiftLeft((input.length()-1-i)*bitsPerDigit));
//    }
//    if (negative)
//      result = result.multiply(NEGATIVE_ONE);
//    return result;
//  }



}
