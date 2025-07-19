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
 * The standardized "base64" alphabet.
 * <p>
 * It differs from typical radix encodings (like hex) because it doesn't start with '0' - '9'
 *
 * @author Alex
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc4648#section-4">RFC 4648</a>
 */
public class Base64Alphabet extends AlphabetAdapter {

  public static final int MAX_RADIX = 64;
  /**
   * The standard character set for the "base64" encoding specified in RFC 4648
   */
  public static final byte[] CHARS = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };
  // TODO: maybe make CHARS private and pass a defensive copy to super constructor



  /* TODO(3/3/2025):
       - this encoding not safe for URL/filename [+/] not valid in filenames; might want to to use the java.util.Base64.Encoder.toBase64URL alphabet
       - additionally, the results aren't lexicographically comparable:
         - might want to use something more like SmallRadixAlphabet(62), ordered [0-9A-Za-z],
           with chars 62/63 being safe chars greater than 'z'
           (Unfortunately there aren't any filename-safe ASCII chars greater than 'z')
   */

  private static final Base64Alphabet INSTANCE = new Base64Alphabet();

  public static Base64Alphabet getInstance() {
    return INSTANCE;
  }

  private Base64Alphabet() {
    super(CHARS);

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
  // TODO: document the mutability of the array returned by this method
  public byte[] getChars() {
    return CHARS;
  }
}
