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

/**
 * For encoding arbitrary-precision integers into any base.  Officially,
 * only supports a limited number of such bases - those that are represented
 * by constant fields in this class.  However, this class is, in theory,
 * infinitely extensible by providing alternate implementations of the
 * Alphabet interface.
 *
 * Instances of this class are immutable.
 *
 * @author Alex
 */
public enum BigIntRadixCodec {

  // the most useful bases are represented as constants

  BASE_2(new SmallRadixAlphabet(2)),
  BASE_8(new SmallRadixAlphabet(8)),
  BASE_16(new SmallRadixAlphabet(16)),
  BASE_32(new SmallRadixAlphabet(32)),
  BASE_36(new SmallRadixAlphabet(36)),  // base 36 is useful because it uses the alphabet [0-9a-z]
  BASE_62(new BigRadixAlphabet(62)),  // base 62 is useful because it uses the alphabet [0-9a-zA-Z]
  BASE_64(new Base64Alphabet());

  private final Alphabet alphabet;
  private final int bitsPerDigit;
  private final int mask;

  private static final BigInteger NEGATIVE_ONE = BigInteger.valueOf(-1);
  private final BigInteger radixBigInt;
  private final int radix;

  private BigIntRadixCodec(Alphabet alphabet) {
    this.alphabet = alphabet;
    radix = alphabet.base();
    bitsPerDigit = log2(radix -1);
    mask = (1 << bitsPerDigit) - 1;
    radixBigInt = BigInteger.valueOf(radix);
  }

  /** Computes the base 2 logarithm of the given value */
  private static int log2(int value) {
    if (value <= 0)
      throw new IllegalArgumentException("Value must be positive.");
    for (int i = 0; i < Integer.MAX_VALUE; i++) {
      if ((value >> i) == 0)
        return i;
    }
    throw new IllegalStateException("Should never get to this point.");
  }

  public String encode(BigInteger input) {
    // uses repeated division by the base to obtain the output
    BigInteger remainder = input.abs();
    StringBuilder out = new StringBuilder();
    do {
      BigInteger[] quotientAndMod = remainder.divideAndRemainder(radixBigInt);
      BigInteger quotient = quotientAndMod[0];
      BigInteger mod = quotientAndMod[1];
      out.append((char)alphabet.encode(mod.intValue()));
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
      input = input.substring(1);
    }

    // The algorithm follows this example for base 16, generalized to any base
    // abcd = d*16^0 + c*16^1 + b*16^2 + a*16^3

    BigInteger result = BigInteger.ZERO;
    for (int i = 0; i < input.length(); i++) {
      char lastChar = input.charAt(input.length() - 1 - i);
      int lastCharValue = alphabet.decode((byte)lastChar);
      result = result.add(BigInteger.valueOf(lastCharValue).multiply(radixBigInt.pow(i)));
    }
    if (negative)
      result = result.multiply(NEGATIVE_ONE);
    return result;
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
