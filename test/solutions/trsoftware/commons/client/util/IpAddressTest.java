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

package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.client.util.TestUtils.randomInts;

/**
 * @author Alex
 * @since Jun 29, 2013
 */
public class IpAddressTest extends TestCase {

  public void testAllConversions() throws Exception {
    for (int i : randomInts(1000)) {
      IpAddress ip = new IpAddress(i);
      assertEquals(ip, new IpAddress(ip.toString()));
      assertEquals(ip, new IpAddress(ip.toInt()));
      assertEquals(ip, new IpAddress(ip.toLong()));
    }
  }

}