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

public class ServerArrayUtilsTest extends TestCase {

  public void testToHexString() throws Exception {
    assertEquals("", ServerArrayUtils.toHexString(null, 2, " "));
    assertEquals("02 03", ServerArrayUtils.toHexString(new byte[]{2, 3}, 2, " "));
    assertEquals("020 304 05", ServerArrayUtils.toHexString(new byte[]{2, 3, 4, 5}, 3, " "));
    assertEquals("0203 0405", ServerArrayUtils.toHexString(new byte[]{2, 3, 4, 5}, 4, " "));
    assertEquals("0203_0405", ServerArrayUtils.toHexString(new byte[]{2, 3, 4, 5}, 4, "_"));
  }

}