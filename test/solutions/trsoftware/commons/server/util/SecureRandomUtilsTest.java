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

import junit.framework.TestCase;

import static solutions.trsoftware.commons.server.util.SecureRandomUtils.randBytes;
import static solutions.trsoftware.commons.server.util.SecureRandomUtils.randString;

/**
 * @author Alex
 * @since 11/10/2017
 */
public class SecureRandomUtilsTest extends TestCase {


  public void testRandString() throws Exception {
    for (int n = -2; n <= 20; n++) {
      String str = randString(n);
      if (n <= 0)
        assertEquals("", str);
      else {
        assertEquals(n, str.length());
        assertTrue(ServerStringUtils.isUrlSafe(str));
      }
    }
  }

  public void testRandBytes() throws Exception {
    for (int n = -2; n <= 32; n++) {
      String str = randBytes(n);
      if (n <= 0)
        assertEquals("", str);
      else {
        assertEquals(((int)(4 * Math.ceil((double)n / 3))), str.length());
        System.out.printf("randBytes(%d) -> %s (length = %d)%n", n, str, str.length());
        assertTrue(ServerStringUtils.isUrlSafe(str));
      }
    }
  }

}