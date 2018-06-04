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

package solutions.trsoftware.commons.server.net;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.util.MathUtils.packUnsignedInt;

/**
 * Nov 6, 2009
 *
 * @author Alex
 */
public class IpRangeDatabaseTest extends TestCase {

  /** A simple, human-readable test which checks a few addresses to catch any blatant errors */
  public void testLookupCountry() throws Exception {
    IpRangeDatabase db = new IpRangeDatabase(100000);
    db.addRange(5, 10, "us");
    db.addRange(11, 20, "gb");
    db.addRange(55000, 100000, "es");
    db.addRange(110000, 220000, "ru");
    db.addRange(55000000, 100000000, "br");
    db.addRange(4123456789L, 4294967295L, "ca");  // 4 billion range (these take up the full 32 bits)

    // for every range we've defined, check the following values:
    // 1) the value just before lowest (should be null)
    // 2) the lowest value
    // 3) the second value
    // 4) a value in the middle
    // 5) the value just before highest
    // 6) the highest value
    // 7) the value just after highest (should be null, unless the next range starts right away)

            assertNull(db.lookupCountry(packUnsignedInt(4)));
    assertEquals("us", db.lookupCountry(packUnsignedInt(5)));
    assertEquals("us", db.lookupCountry(packUnsignedInt(6)));
    assertEquals("us", db.lookupCountry(packUnsignedInt(7)));
    assertEquals("us", db.lookupCountry(packUnsignedInt(9)));
    assertEquals("us", db.lookupCountry(packUnsignedInt(10)));

    assertEquals("gb", db.lookupCountry(packUnsignedInt(11)));
    assertEquals("gb", db.lookupCountry(packUnsignedInt(12)));
    assertEquals("gb", db.lookupCountry(packUnsignedInt(15)));
    assertEquals("gb", db.lookupCountry(packUnsignedInt(19)));
    assertEquals("gb", db.lookupCountry(packUnsignedInt(20)));
            assertNull(db.lookupCountry(packUnsignedInt(21)));

            assertNull(db.lookupCountry(packUnsignedInt(54999)));
    assertEquals("es", db.lookupCountry(packUnsignedInt(55000)));
    assertEquals("es", db.lookupCountry(packUnsignedInt(55001)));
    assertEquals("es", db.lookupCountry(packUnsignedInt(55123)));
    // this is a good test value: it falls into the next bucket
    // (i.e. 99999 >> 16 = 1, wheras 55123 >> 16 = 0)
    assertEquals("es", db.lookupCountry(packUnsignedInt(99999)));
    assertEquals("es", db.lookupCountry(packUnsignedInt(100000)));
            assertNull(db.lookupCountry(packUnsignedInt(100001)));

            assertNull(db.lookupCountry(packUnsignedInt(109999)));
    assertEquals("ru", db.lookupCountry(packUnsignedInt(110000)));
    assertEquals("ru", db.lookupCountry(packUnsignedInt(110001)));
    assertEquals("ru", db.lookupCountry(packUnsignedInt(111234)));
    assertEquals("ru", db.lookupCountry(packUnsignedInt(119999)));
    assertEquals("ru", db.lookupCountry(packUnsignedInt(220000)));
            assertNull(db.lookupCountry(packUnsignedInt(220001)));

            assertNull(db.lookupCountry(packUnsignedInt(54999999)));
    assertEquals("br", db.lookupCountry(packUnsignedInt(55000000)));
    assertEquals("br", db.lookupCountry(packUnsignedInt(55000001)));
    assertEquals("br", db.lookupCountry(packUnsignedInt(55123456)));
    assertEquals("br", db.lookupCountry(packUnsignedInt(99999999)));
    assertEquals("br", db.lookupCountry(packUnsignedInt(100000000)));
            assertNull(db.lookupCountry(packUnsignedInt(100000001)));

            assertNull(db.lookupCountry(packUnsignedInt(4123456788L)));
    assertEquals("ca", db.lookupCountry(packUnsignedInt(4123456789L)));
    assertEquals("ca", db.lookupCountry(packUnsignedInt(4123456790L)));
    assertEquals("ca", db.lookupCountry(packUnsignedInt(4155555555L)));
    assertEquals("ca", db.lookupCountry(packUnsignedInt(4294967295L)));
    assertEquals("ca", db.lookupCountry(packUnsignedInt(4294967295L))); // this is the highest possible value
  }


  /** Tests every single 32-bit IP address */
  public void testLookupCountryExhaustive() throws Exception {
    // TODO: impl
  }
}