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
 * Jan 28, 2009
 *
 * @author Alex
 */
public interface Alphabet {
  // TODO: rename to RadixAlphabet
  /**
   * @param digit the digit to encode;  must be in range <code>[0, radix)</code>
   * @return the character representing the given digit
   * @see #decode(char)
   */
  byte encode(int digit);  // TODO: maybe change sig to: char encode(int digit)

  /**
   * @param codedDigit a character representing an {@code int} in the range <code>[0, radix)</code>
   * @return the {@code int} corresponding to the given digit character
   * @see #encode(int)
   */
  int decode(byte codedDigit);  // TODO: maybe change sig to: int decode(char codedDigit)

  /**
   * @return the maximum radix supported by this alphabet (i.e. the number of unique chars it contains)
   */
  int maxRadix();

  /**
   * @return the characters in this alphabet, such that {@code chars[i]} is the byte used to encode
   * a digit <code>i &isin; [0, {@link #maxRadix})</code>)
   */
  byte[] getChars();

  /**
   * @return The character used to encode a minus sign (for negative numbers)
   */
  byte sign();
}
