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

import java.util.Random;

import static solutions.trsoftware.commons.shared.util.TimeUnit.*;

/**
 * May 19, 2009
 *
 * @author Alex
 */
public class TimeUnitTest extends TestCase {

  public static final double marginOfError = MathUtils.EPSILON;

  private double[] randomDoubles;

  public void setUp() throws Exception {
    super.setUp();
    randomDoubles = new double[1000];
    Random rnd = RandomUtils.rnd();
    for (int i = 0; i < randomDoubles.length; i++) {
      randomDoubles[i] = rnd.nextDouble() * rnd.nextInt();
    }
  }

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
    // check that converting from any unit to itself returns *exactly* the same value (i.e. no conversion involving lossy arithmetic)
    for (TimeUnit unit : TimeUnit.values()) {
      for (double value : randomDoubles) {
        assertEquals(value, unit.from(unit, value));
      }
    }

    // do some manual tests as a sanity check
    assertEquals(2_000_000, NANOSECONDS.from(MILLISECONDS, 2), marginOfError);
    assertEquals(2.2, SECONDS.from(MILLISECONDS, 2200), marginOfError);
    assertEquals(1.5, MINUTES.from(SECONDS, 90), marginOfError);
    assertEquals(1.5, HOURS.from(MINUTES, 90), marginOfError);
    assertEquals(4.5, DAYS.from(HOURS, 4.5 * 24), marginOfError);
    assertEquals(.5, MONTHS.from(DAYS, 15), .01);  // we allow a higher margin of error for this conversion, since a month isn't exactly 30 days
    assertEquals(1.25, YEARS.from(MONTHS, 15), marginOfError);
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
    // check that converting any unit to itself returns *exactly* the same value (i.e. no conversion involving lossy arithmetic)
    for (TimeUnit unit : TimeUnit.values()) {
      for (double value : randomDoubles) {
        assertEquals(value, unit.to(unit, value));
      }
    }

    // do some manual tests as a sanity check
    assertEquals(2, MILLISECONDS.from(NANOSECONDS, 2_000_000), marginOfError);
    assertEquals(2200, SECONDS.to(MILLISECONDS, 2.2), marginOfError);
    assertEquals(90, MINUTES.to(SECONDS, 1.5), marginOfError);
    assertEquals(90, HOURS.to(MINUTES, 1.5), marginOfError);
    assertEquals(4.5, DAYS.from(HOURS, 4.5 * 24), marginOfError);
    assertEquals(15.2184375, MONTHS.to(DAYS, .5), marginOfError);
    assertEquals(365.2425, YEARS.to(DAYS, 1), marginOfError);
    assertEquals(15, YEARS.to(MONTHS, 1.25), marginOfError);
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

  public void testGetPrettyName() throws Exception {
    assertEquals("hour", HOURS.getPrettyName(1));
    assertEquals("hours", HOURS.getPrettyName(0));
    assertEquals("hours", HOURS.getPrettyName(.9));
    assertEquals("hours", HOURS.getPrettyName(1.1));
    assertEquals("hours", HOURS.getPrettyName(2));
  }

  public void testChooseUnit() throws Exception {
    TimeUnit[] units = values();
    for (int i = units.length - 1; i >= 0; i--) {
      TimeUnit unit = units[i];
      // any nanos value >= 1 should map to the same unit (at most DAYS)
      assertEquals(units[MathUtils.restrict(i, 0, DAYS.ordinal())], chooseUnit(unit, 1));
      assertEquals(units[MathUtils.restrict(i, 0, DAYS.ordinal())], chooseUnit(unit, 1.1));
      // any nanos value < 1 should map to the next lower unit (at least NANOSECONDS)
      assertEquals(units[MathUtils.restrict(i-1, 0, DAYS.ordinal())], chooseUnit(unit, 0.9));
    }
  }

  private TimeUnit chooseUnit(TimeUnit sourceUnit, double duration) {
    double nanos = sourceUnit.toNanos(duration);
    TimeUnit result = TimeUnit.chooseUnit(nanos);
    // also print the result of format
    System.out.println(new TimeValue(duration, sourceUnit) + " -> " + format(nanos, 4));
    return result;
  }


}