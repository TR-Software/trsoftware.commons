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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.util.codec.UrlSafeBase64Alphabet;

import java.security.SecureRandom;

/**
 * @author Alex, 9/14/2017
 */
public abstract class SecureRandomUtils {

  public static final SecureRandom rnd = new SecureRandom();

  /**
   * @return A string of {@code length} chars chosen at random from {@link UrlSafeBase64Alphabet#CHARS}
   */
  public static String randString(int length) {
    if (length <= 0)
      return "";
    byte[] chars = UrlSafeBase64Alphabet.CHARS;
    StringBuilder buf = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buf.append((char)chars[rnd.nextInt(chars.length)]);
    }
    return buf.toString();
  }

  /**
   * Returns a url-safe base64-encoded string generated from {@code n} {@linkplain SecureRandom#nextBytes random bytes}.
   *
   * @param n the number of random bytes to use for generating the string
   * @return A url-safe base64-encoded string generated from {@code n} random bytes.
   */
  public static String randBytes(int n) {
    if (n <= 0)
      return "";
    byte[] bytes = new byte[n];
    rnd.nextBytes(bytes);
    return ServerStringUtils.urlSafeBase64Encode(bytes);
  }

  /**
   * Generates a random string that can be used for things like email verification tokens, etc.
   * For this purpose we use 12 random bytes because:
   * <ol>
   *   <li>it's long-enough to make collisions extremely rare</li>
   *   <li>it's short-enough to be used in a URL</li>
   *   <li>its base64 encoding doesn't contain any padding chars</li>
   * </ol>
   * @return a 16-char string generated from 12 random bytes encoded with url-safe base64
   * @see #randBytes(int)
   */
  public static String randomCode() {
    // we use 12 bytes because its base64 encoding is 16 chars long (and doesn't need any padding chars)
    return randBytes(12);
  }

  /**
   * Generates and prints some random values that can be used for auth codes, passwords, etc.
   */
  public static void main(String[] args) {
    System.out.println("randString(64): " + randString(64));
    for (int i = 12; i < 40; i++) {
      System.out.printf("randBytes(%d): %s%n", i, randBytes(i));
    }
  }
}
