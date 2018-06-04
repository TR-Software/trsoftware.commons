/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.util;

import org.apache.commons.codec.binary.Hex;

/**
 * Date: Sep 22, 2008 Time: 1:50:50 PM
 *
 * @author Alex
 */
public class ServerArrayUtils {

  /**
   * Fills the given array with array.length new instances of the given class.
   * @return the array
   */
  public static <T> T[] fill(T[] array, Class<T> c) throws IllegalAccessException, InstantiationException {
    for (int i = 0; i < array.length; i++) {
      array[i] = c.newInstance();
    }
    return array;
  }

  public static String toHexString(byte[] bytes, int digitGrouping, String groupDelimiter) {
    if (bytes == null || bytes.length == 0)
      return "";
    char[] hexChars = Hex.encodeHex(bytes);
    StringBuilder buf = new StringBuilder(bytes.length*3);
    for (int i = 0; i < hexChars.length; i++) {
      buf.append(hexChars[i]);
      if (i < hexChars.length-1 && i % digitGrouping == (digitGrouping-1))
        buf.append(groupDelimiter);
    }
    return buf.toString();
  }
}
