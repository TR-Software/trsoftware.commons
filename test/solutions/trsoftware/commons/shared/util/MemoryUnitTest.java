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

import static solutions.trsoftware.commons.shared.util.MemoryUnit.*;
import static solutions.trsoftware.commons.shared.util.RandomUtils.rnd;

/**
 * Dec 10, 2010
 *
 * @author Alex
 */
public class MemoryUnitTest extends TestCase {

  public void testFrom() throws Exception {
    // check that converting 0 from any unit yields zero
    for (MemoryUnit unit1 : values()) {
      for (MemoryUnit unit2 : values()) {
        assertEquals(0d, unit1.from(unit2, 0));
      }
    }
    // check that converting 1 from any unit to BYTES simply yields the number of bytes in that unit
    for (MemoryUnit unit : values()) {
      assertEquals(unit.bytes, BYTES.from(unit, 1));
    }
    // check that converting any value from any unit to itself yields the given value
    for (MemoryUnit unit1 : values()) {
      assertEquals(1.234d, unit1.from(unit1, 1.234d));
    }

    // do some manual tests as a sanity check
    assertEquals(2.1484375, KILOBYTES.from(BYTES, 2200), MathUtils.EPSILON);
    assertEquals(0.00209808349609375, MEGABYTES.from(KILOBYTES, 2.1484375), MathUtils.EPSILON*1000);
    assertEquals(0.000002048909664154052734375, GIGABYTES.from(MEGABYTES, 0.00209808349609375), MathUtils.EPSILON*1000*1000);
  }

  public void testTo() throws Exception {
    // check that converting 0 to any unit yields zero
    for (MemoryUnit unit1 : values()) {
      for (MemoryUnit unit2 : values()) {
        assertEquals(0d, unit1.to(unit2, 0));
      }
    }
    // check that converting 1 in any unit to MILLISECOND simply yields the number of milliseconds in that unit
    for (MemoryUnit unit : values()) {
      assertEquals(unit.bytes, unit.to(BYTES, 1));
    }
    // check that converting any value to any unit from itself yields the given value
    for (MemoryUnit unit1 : values()) {
      assertEquals(1.234d, unit1.to(unit1, 1.234d));
    }

    // do some manual tests as a sanity check
    assertEquals(2200, BYTES.from(KILOBYTES, 2.1484375), MathUtils.EPSILON);
    assertEquals(2.1484375, KILOBYTES.from(MEGABYTES, 0.00209808349609375), MathUtils.EPSILON*1000);
    assertEquals(0.00209808349609375, MEGABYTES.from(GIGABYTES, 0.000002048909664154052734375), MathUtils.EPSILON*1000*1000);
  }

  public void testToBytes() throws Exception {
    for (int i = 0; i < 1000; i++) {
      double value = rnd().nextDouble() * rnd().nextInt(20);
      for (MemoryUnit unit : values()) {
        assertEquals(unit.toBytes(value), unit.to(BYTES, value));
      }
    }
  }

  public void testFromBytes() throws Exception {
    for (int i = 0; i < 1000; i++) {
      double value = rnd().nextDouble() * rnd().nextInt(20);
      for (MemoryUnit unit : values()) {
        assertEquals(unit.fromBytes(value), unit.from(BYTES, value));
      }
    }
  }
}