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
 * The standardized "base64url" alphabet.
 * <p>
 * It differs from typical radix encodings (like hex) because it doesn't start with '0' - '9'
 *
 * @author Alex
 * @see BigIntRadixCodec
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc4648#section-5">RFC 4648</a>
 */
public class UrlSafeBase64Alphabet extends AlphabetAdapter {

  public static final int MAX_RADIX = 64;

  /**
   * The standard character set for the "base64url" encoding specified in RFC 4648
   */
  public static final byte[] CHARS = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
  };

  private static final UrlSafeBase64Alphabet INSTANCE = new UrlSafeBase64Alphabet();

  public static UrlSafeBase64Alphabet getInstance() {
    return INSTANCE;
  }

  private UrlSafeBase64Alphabet() {

  }

  @Override
  public byte sign() {
    /* Note: can't use '-' for sign b/c it's part of the base64url alphabet, so using tilde ('~') here instead
       b/c it's one of the 2 remaining unreserved in URL (From RFC 3986: unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~")
       (see https://webmasters.stackexchange.com/a/130694)
       TODO: however tilde not really filename-safe on Unix-based systems (see https://superuser.com/a/748264/1109425)
         - maybe replace '-' with '~' in the alphabet and use '-' for sign? but that wouldn't really solve the filename problem
     */

    return (byte)'~';
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
