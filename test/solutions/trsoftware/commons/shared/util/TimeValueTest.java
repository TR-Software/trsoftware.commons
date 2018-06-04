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

import java.util.Arrays;
import java.util.List;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;
import static solutions.trsoftware.commons.shared.util.TimeUnit.*;

/**
 * @author Alex
 * @since 11/10/2017
 */
public class TimeValueTest extends TestCase {

  private final TimeValue s15 = new TimeValue(15, SECONDS);
  private final TimeValue m15 = new TimeValue(15, MINUTES);
  private final TimeValue h24 = new TimeValue(24, HOURS);
  private final List<TimeValue> values = Arrays.asList(s15, m15, h24);

  public void testTo() throws Exception {
    for (TimeValue value : values) {
      for (TimeUnit toUnit : values()) {
        assertEquals(new TimeValue(toUnit.from(value.getUnit(), value.getValue()), toUnit), value.to(toUnit));
      }
    }
  }

  public void testToMillis() throws Exception {
    for (TimeValue value : values) {
      assertEquals(value.getUnit().toMillis(value.getValue()), value.toMillis());
    }
  }

  /** Tests the methods inherited from {@link Number} */
  public void testNumber() throws Exception {
    for (TimeValue value : values) {
      double millis = value.toMillis();
      assertEquals(millis, value.doubleValue());;
      assertEquals((int)millis, value.intValue());;
      assertEquals((long)millis, value.longValue());;
      assertEquals((float)millis, value.floatValue());;
    }
  }

  public void testToString() throws Exception {
    assertEquals("15 seconds", s15.toString());
    assertEquals("15 minutes", m15.toString());
    assertEquals("24 hours", h24.toString());
    assertEquals("1.235 days", new TimeValue(1.2345678, DAYS).toString());
  }

  public void testCompareTo() throws Exception {
    assertThat(s15).isLessThan(m15).isLessThan(h24).isLessThanOrEqualTo(m15).isLessThanOrEqualTo(h24).isEqualTo(s15);
    assertThat(h24).isGreaterThan(m15).isGreaterThan(s15).isGreaterThanOrEqualTo(m15).isGreaterThanOrEqualTo(s15).isEqualTo(h24);
  }

  public void testEqualsAndHashCode() throws Exception {
    assertEqualsAndHashCode(s15, new TimeValue(15, SECONDS));
    assertEqualsAndHashCode(m15, new TimeValue(15, MINUTES));
    assertEqualsAndHashCode(h24, new TimeValue(24, HOURS));

    assertNotEqualsAndHashCode(s15, new TimeValue(15.0001, SECONDS));
    assertNotEqualsAndHashCode(m15, new TimeValue(15.0001, MINUTES));
    assertNotEqualsAndHashCode(h24, new TimeValue(24.0001, HOURS));

    // two instances should not be equal if their units are not equal, even if they represent the same duration in millis
    assertNotEqualsAndHashCode(s15, new TimeValue(15_000, MILLISECONDS));
  }

  public void testIsElapsed() throws Exception {
    // we test both the long version and the double version of the method
    TimeValue duration = new TimeValue(10, SECONDS);
    assertFalse(duration.isElapsed(0, 9000));
    assertFalse(duration.isElapsed(0d, 9000d));
    assertFalse(duration.isElapsed(0, 10000));
    assertFalse(duration.isElapsed(0d, 10000d));
    assertTrue(duration.isElapsed(0, 11000));
    assertTrue(duration.isElapsed(0d, 11000d));
  }

  public void testAdd() throws Exception {
    assertEquals(new TimeValue(15.25, MINUTES), m15.add(s15));
    assertEquals(new TimeValue(15.25, SECONDS), s15.add(new TimeValue(250, MILLISECONDS)));
    assertEquals(new TimeValue(24.25, HOURS), h24.add(m15));
    // now test the millis version of the method
    assertEquals(new TimeValue(15.25, MINUTES), m15.add(15_000));
    assertEquals(new TimeValue(24.25, HOURS), h24.add(15_000*60));
    // now test adding a negative value
    assertEquals(new TimeValue(14.75, MINUTES), m15.add(-15_000));
    assertEquals(new TimeValue(-15, SECONDS), s15.add(-30_000));
  }

  public void testSubtract() throws Exception {
    assertEquals(new TimeValue(14.75, MINUTES), m15.subtract(s15));
    assertEquals(new TimeValue(23.75, HOURS), h24.subtract(m15));
    assertEquals(new TimeValue(-1, SECONDS), s15.subtract(new TimeValue(16, SECONDS)));
  }

}