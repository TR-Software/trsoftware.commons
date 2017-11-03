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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.util.codec.Base64Alphabet;

import java.security.SecureRandom;

/**
 * @author Alex, 9/14/2017
 */
public abstract class SecureRandomUtils {

  public static final SecureRandom rnd = new SecureRandom();

  /**
   * @return A string of {@code length} chars chosen at random from {@link Base64Alphabet#CHARS}
   */
  public static String randString(int length) {
    byte[] chars = Base64Alphabet.CHARS;
    StringBuilder buf = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buf.append((char)chars[rnd.nextInt(chars.length)]);
    }
    return buf.toString();
  }

  public static void main(String[] args) {
    System.out.println(randString(64));
  }

}
