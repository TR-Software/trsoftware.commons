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
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import java.util.Random;

import static solutions.trsoftware.commons.client.util.TimeUnit.*;

/**
 * May 19, 2009
 *
 * @author Alex
 */
public class TimeUnitTest extends TestCase {

  public void testFrom() throws Exception {
    // check that converting 0 from any unit yields zero
    for (TimeUnit unit1 : values()) {
      for (TimeUnit unit2 : values()) {
        assertEquals(0d, unit1.from(unit2, 0));
      }
    }
    // check that converting 1 from any unit to MILLISECOND simply yields the number of milliseconds in that unit
    for (TimeUnit unit : values()) {
      assertEquals(unit.millis, MILLISECONDS.from(unit, 1));
    }

    // do some manual tests as a sanity check
    assertEquals(2_000_000, NANOSECONDS.from(MILLISECONDS, 2), AssertUtils.EPSILON);
    assertEquals(2.2, SECONDS.from(MILLISECONDS, 2200), AssertUtils.EPSILON);
    assertEquals(1.5, MINUTES.from(SECONDS, 90), AssertUtils.EPSILON);
    assertEquals(1.5, HOURS.from(MINUTES, 90), AssertUtils.EPSILON);
    assertEquals(4.5, DAYS.from(HOURS, 4.5 * 24), AssertUtils.EPSILON);
    assertEquals(.5, MONTHS.from(DAYS, 15), .01);  // we allow a higher margin of error for this conversion, since a month isn't exactly 30 days
    assertEquals(1.25, YEARS.from(MONTHS, 15), AssertUtils.EPSILON);
  }

  public void testTo() throws Exception {
    // check that converting 0 to any unit yields zero
    for (TimeUnit unit1 : values()) {
      for (TimeUnit unit2 : values()) {
        assertEquals(0d, unit1.to(unit2, 0));
      }
    }
    // check that converting 1 in any unit to MILLISECOND simply yields the number of milliseconds in that unit
    for (TimeUnit unit : values()) {
      assertEquals(unit.millis, unit.to(MILLISECONDS, 1));
    }

    // do some manual tests as a sanity check
    assertEquals(2, MILLISECONDS.from(NANOSECONDS, 2_000_000), AssertUtils.EPSILON);
    assertEquals(2200, SECONDS.to(MILLISECONDS, 2.2), AssertUtils.EPSILON);
    assertEquals(90, MINUTES.to(SECONDS, 1.5), AssertUtils.EPSILON);
    assertEquals(90, HOURS.to(MINUTES, 1.5), AssertUtils.EPSILON);
    assertEquals(4.5, DAYS.from(HOURS, 4.5 * 24), AssertUtils.EPSILON);
    assertEquals(15, MONTHS.to(DAYS, .5), .3);  // we allow a higher margin of error for this conversion, since a month isn't exactly 30 days
    assertEquals(15, YEARS.to(MONTHS, 1.25), AssertUtils.EPSILON);
  }

  public void testToMillis() throws Exception {
    Random rnd = new Random();
    for (int i = 0; i < 1000; i++) {
      double value = rnd.nextDouble() * rnd.nextInt(20);
      for (TimeUnit unit : values()) {
        assertEquals(unit.toMillis(value), unit.to(MILLISECONDS, value));
      }
    }
  }

  public void testFromMillis() throws Exception {
    Random rnd = new Random();
    for (int i = 0; i < 1000; i++) {
      double value = rnd.nextDouble() * rnd.nextInt(20);
      for (TimeUnit unit : values()) {
        assertEquals(unit.fromMillis(value), unit.from(MILLISECONDS, value));
      }
    }
  }
}