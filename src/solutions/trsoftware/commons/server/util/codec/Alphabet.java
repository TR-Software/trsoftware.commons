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

package solutions.trsoftware.commons.server.util.codec;

/**
 * Jan 28, 2009
 *
 * @author Alex
 */
public interface Alphabet {
  /**
   * @param plainInt Must be in range 0..radix (exclusive)
   * @return The character representing the int
   */
  byte encode(int plainInt);

  /**
   * @param codedByte A character representing an int in the range 0..radix (exclusive)
   * @return The int in the range 0..radix (exclusive)
   */
  int decode(byte codedByte);

  /**
   * @return The character used to encode a minus sign (for negative numbers)
   */
  byte sign();

  /** The base of the alphabet, e.g. 2, 10, 16, 36, 62, 64 */
  int base();
}
