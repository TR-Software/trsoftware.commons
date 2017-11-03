/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.util.codec;

/**
 * Jan 28, 2009
 *
 * @author Alex
 */
public abstract class AlphabetAdapter implements Alphabet {

  private int base;
  private final byte[] codingAlphabet;
  private final byte[] decodingAlphabet;
  private final byte sign;


  public AlphabetAdapter(int base, byte[] codingAlphabet, byte sign) {
    this.base = base;
    this.codingAlphabet = codingAlphabet;
    this.sign = sign;

    decodingAlphabet = new byte[Byte.MAX_VALUE];
    for (int j = 0; j < decodingAlphabet.length; j++) {
      if (j < codingAlphabet.length)
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

  /**
   * @return The character used to encode a minus sign (for negative numbers)
   */
  public byte sign() {
    return sign;
  }

  public int base() {
    return base;
  }
}