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

import static com.google.common.base.Strings.lenientFormat;

/**
 * Jan 28, 2009
 *
 * @author Alex
 */
public abstract class AlphabetAdapter implements Alphabet {
  // TODO: rename to BaseAlphabet or RadixAlphabetBase

  private final byte[] codingAlphabet;
  private final byte[] decodingAlphabet;  // reverse mapping of codingAlphabet

  public AlphabetAdapter(byte[] codingAlphabet) {
    int maxRadix = maxRadix();
    int nChars = codingAlphabet.length;
    // codingAlphabet must have enough chars to encode max digit in radix and cannot be longer than max byte value
    if (nChars < maxRadix || nChars > 128) {
      throw new IllegalArgumentException(lenientFormat(
          "codingAlphabet length (%s) for radix %s must be in range [%s, 128]", nChars, maxRadix, maxRadix));
    }
//    this.codingAlphabet = codingAlphabet;
    this.codingAlphabet = getChars();
    // compute the reverse mapping
    decodingAlphabet = new byte[Byte.MAX_VALUE];
    for (int j = 0; j < decodingAlphabet.length; j++) {
      if (j < maxRadix)
        decodingAlphabet[codingAlphabet[j]] = (byte)j;
    }
  }

  public AlphabetAdapter() {
    int maxRadix = maxRadix();
    this.codingAlphabet = getChars();
    int nChars = codingAlphabet.length;
    // codingAlphabet must have enough chars to encode max digit in radix and cannot be longer than max byte value
    if (nChars < maxRadix || nChars > 128) {
      throw new IllegalArgumentException(lenientFormat(
          "codingAlphabet length (%s) for radix %s must be in range [%s, 128]", nChars, maxRadix, maxRadix));
    }
//    this.codingAlphabet = codingAlphabet;
    // compute the reverse mapping
    decodingAlphabet = new byte[Byte.MAX_VALUE];
    for (int j = 0; j < decodingAlphabet.length; j++) {
      if (j < maxRadix)
        decodingAlphabet[codingAlphabet[j]] = (byte)j;
    }
  }

  /**
   * @param plainInt Must be in range 0..radix (exclusive)
   * @return The character representing the int
   */
  public byte encode(int plainInt) {
    return codingAlphabet[plainInt];
  }

  /**
   * @param codedByte A character representing an int in the range 0..radix (exclusive)
   * @return The int in the range 0..radix (exclusive)
   */
  public int decode(byte codedByte) {
    return decodingAlphabet[codedByte];
  }

  @Override
  // TODO: document the mutability of the array returned by this method
  // TODO: maybe make this method abstract or remove the subclass overrides (i.e. pass it to constructor)
  public byte[] getChars() {
    return codingAlphabet;
  }
}