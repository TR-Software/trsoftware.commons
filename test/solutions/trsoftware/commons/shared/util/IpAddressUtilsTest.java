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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.util.IpAddressUtils.*;

/**
 * @author Alex
 * @since Jun 21, 2013
 */
public class IpAddressUtilsTest extends TestCase {

  /** Meta test (tests the functionality of this test class) */
  public void testIp4LongToString() throws Exception {
    // test a few values manually to be sure
    assertEquals("0.0.0.0", ip4LongToString(0));
    assertEquals("0.0.0.1", ip4LongToString(1));
    assertEquals("0.0.0.7", ip4LongToString(7));
    assertEquals("0.0.0.127", ip4LongToString(127));
    assertEquals("0.0.0.255", ip4LongToString(255));
    assertEquals("0.0.1.0", ip4LongToString(256));
    assertEquals("127.127.127.127", ip4LongToString(0x7f7f7f7fL));
    assertEquals("127.255.127.255", ip4LongToString(0x7fff7fffL));
    assertEquals("255.255.255.255", ip4LongToString(0xffffffffL));

    // now test a 100K random values (it takes too long to test the whole range of ints)
    for (int i = 0; i < 100000; i++) {
      long value = randomIpLong();  // use the lo 32-bit part
      assertEquals(value, ip4StringToLong(ip4LongToString(value)));
    }
  }

  public void testIp4StringToLong() throws Exception {
    assertEquals(0L, ip4StringToLong(null));
    assertEquals(0L, ip4StringToLong("0.0.0.0"));
    assertEquals(1L, ip4StringToLong("0.0.0.1"));
    assertEquals(0x7f7f7f7fL, ip4StringToLong("127.127.127.127"));
    assertEquals(0x7fff7fffL, ip4StringToLong("127.255.127.255"));
    assertEquals(0xffffffffL, ip4StringToLong("255.255.255.255"));
  }

  public void testIp4IntToString() throws Exception {
     // test a few values manually to be sure
    assertEquals("0.0.0.0", ip4IntToString(ip4LongToInt(0)));
    assertEquals("0.0.0.1", ip4IntToString(ip4LongToInt(1)));
    assertEquals("0.0.0.7", ip4IntToString(ip4LongToInt(7)));
    assertEquals("0.0.0.127", ip4IntToString(ip4LongToInt(127)));
    assertEquals("0.0.0.255", ip4IntToString(ip4LongToInt(255)));
    assertEquals("0.0.1.0", ip4IntToString(ip4LongToInt(256)));
    assertEquals("127.127.127.127", ip4IntToString(ip4LongToInt(0x7f7f7f7fL)));
    assertEquals("127.255.127.255", ip4IntToString(ip4LongToInt(0x7fff7fffL)));
    assertEquals("255.255.255.255", ip4IntToString(ip4LongToInt(0xffffffffL)));

    // now test a 100K random values (it takes too int to test the whole range of ints)
    for (int i = 0; i < 100000; i++) {
      int value = RandomUtils.rnd.nextInt();
      assertEquals(value, ip4StringToInt(ip4IntToString(value)));
    }
  }

  public void testIp4StringToInt() throws Exception {
    assertEquals(ip4LongToInt(0), ip4StringToInt(null));
    assertEquals(ip4LongToInt(0L), ip4StringToInt("0.0.0.0"));
    assertEquals(ip4LongToInt(1L), ip4StringToInt("0.0.0.1"));
    assertEquals(ip4LongToInt(0x7f7f7f7fL), ip4StringToInt("127.127.127.127"));
    assertEquals(ip4LongToInt(0x7fff7fffL), ip4StringToInt("127.255.127.255"));
    assertEquals(ip4LongToInt(0xffffffffL), ip4StringToInt("255.255.255.255"));
  }

  public static long randomIpLong() {
    return RandomUtils.rnd.nextLong() & 0xffffffffL;
  }
}