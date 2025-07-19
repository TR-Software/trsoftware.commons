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

/**
 * Optimal alphabet for encoding an integer in a base greater than {@value Character#MAX_RADIX}.
 * <p>
 * The primary use-case of this class is base {@value #MAX_RADIX}, which allows representing an integer with the
 * shortest possible strings using only ASCII digits and letters. Specifically, we use the characters {@code [0-9A-Za-z]},
 * such that the uppercase letters precede lowercase to preserve the lexicographic comparison semantics of the encoded
 * strings.
 * <p>
 * <b>Note:</b> This alphabet is not compatible with Java's standard {@link Integer#toString(int, int)} encoding,
 * which uses only lowercase letters.  For example, the standard encoding of {@code 255} in Java is {@code "ff"},
 * whereas here it's {@code "47"} and {@code "ff"} actually represents {@code 2583}.
 *
 *
 * @author Alex
 */
public class BigRadixAlphabet extends AlphabetAdapter {
  // TODO: experimental: replacement for SmallRadixAlphabet, s.t. capital letters come before lowercase to ensure lexicographic string comparison
  static final byte[] CHARS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
  };
  public static final int MAX_RADIX = 62;

  private static final BigRadixAlphabet INSTANCE = new BigRadixAlphabet();

  public static BigRadixAlphabet getInstance() {
    return INSTANCE;
  }

  private BigRadixAlphabet() {

  }

  @Override
  public byte sign() {
    return (byte)'-';
  }

  @Override
  public int maxRadix() {
    return MAX_RADIX;
  }

  @Override
  public byte[] getChars() {
    return CHARS;
  }
}