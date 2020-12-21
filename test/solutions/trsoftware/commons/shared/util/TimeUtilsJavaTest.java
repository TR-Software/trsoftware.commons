/*
 * Copyright 2020 TR Software Inc.
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

import com.google.gwt.core.shared.GwtIncompatible;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.util.Clock;
import solutions.trsoftware.commons.server.util.time.TemporalUnitImpl;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;

import static solutions.trsoftware.commons.shared.util.TimeUtils.*;

public class TimeUtilsJavaTest extends TestCase {

  public void testGenerateRelativeTimeElapsedString() throws Exception {

//    assertEquals("1 second ago", TimeUtils.generateRelativeTimeElapsedString(0));
//    assertEquals("1 second ago", TimeUtils.generateRelativeTimeElapsedString(20));
//    assertEquals("1 second ago", TimeUtils.generateRelativeTimeElapsedString(1000));
//    assertEquals("1 second ago", TimeUtils.generateRelativeTimeElapsedString(1999));
    assertEquals("moments ago", generateRelativeTimeElapsedString(0));
    assertEquals("moments ago", generateRelativeTimeElapsedString(20));
    assertEquals("moments ago", generateRelativeTimeElapsedString(1000));
    assertEquals("moments ago", generateRelativeTimeElapsedString(1999));
    assertEquals("moments ago", generateRelativeTimeElapsedString(9999));
    assertEquals("10 seconds ago", generateRelativeTimeElapsedString(10001));
    assertEquals("11 seconds ago", generateRelativeTimeElapsedString(11000));
    assertEquals("59 seconds ago", generateRelativeTimeElapsedString(1000*59+1));

    assertEquals("1 minute ago", generateRelativeTimeElapsedString(1000*60));
    assertEquals("1 minute ago", generateRelativeTimeElapsedString(1000*60+1));
    assertEquals("1 minute ago", generateRelativeTimeElapsedString(1000*60+59));
    assertEquals("2 minutes ago", generateRelativeTimeElapsedString(1000*60*2));
    assertEquals("58 minutes ago", generateRelativeTimeElapsedString(1000*60*59-1));
    assertEquals("59 minutes ago", generateRelativeTimeElapsedString(1000*60*59));

    assertEquals("1 hour ago", generateRelativeTimeElapsedString(1000*60*60));
    assertEquals("1 hour ago", generateRelativeTimeElapsedString(1000*60*60+1));
    assertEquals("1 hour ago", generateRelativeTimeElapsedString(1000*60*60+(59*60*1000)));
    assertEquals("2 hours ago", generateRelativeTimeElapsedString(1000*60*60*2));
    assertEquals("23 hours ago", generateRelativeTimeElapsedString(1000*60*60*24-1));

    assertEquals("1 day ago", generateRelativeTimeElapsedString(1000*60*60*24));
    assertEquals("1 day ago", generateRelativeTimeElapsedString(1000*60*60*24+1));
    assertEquals("1 day ago", generateRelativeTimeElapsedString(1000*60*60*24*2-1));
    assertEquals("2 days ago", generateRelativeTimeElapsedString(1000*60*60*24*2));
    assertEquals("10 days ago", generateRelativeTimeElapsedString(1000*60*60*24*10));

    assertEquals("29 days ago", generateRelativeTimeElapsedString(1000d*60*60*24*29));
    assertEquals("29 days ago", generateRelativeTimeElapsedString(1000d*60*60*24*29.9));
//    assertEquals("1 month ago", TimeUtils.generateRelativeTimeElapsedString(1000d*60*60*24*30));
//    assertEquals("1 month ago", TimeUtils.generateRelativeTimeElapsedString(1000d*60*60*24*31));
//    assertEquals("1 month ago", TimeUtils.generateRelativeTimeElapsedString(1000d*60*60*24*59));
//    assertEquals("2 months ago", TimeUtils.generateRelativeTimeElapsedString(1000d*60*60*24*60));
//    assertEquals("2 months ago", TimeUtils.generateRelativeTimeElapsedString(1000d*60*60*24*61));
    assertEquals("30 days ago", generateRelativeTimeElapsedString(1000d*60*60*24*30));
    assertEquals("31 days ago", generateRelativeTimeElapsedString(1000d*60*60*24*31));
    assertEquals("59 days ago", generateRelativeTimeElapsedString(1000d*60*60*24*59));
    // 30.4 days per month, so will switch to reporting "2 months" after 60.8 days
    assertEquals("60 days ago", generateRelativeTimeElapsedString(1000d*60*60*24*60));
    assertEquals("2 months ago", generateRelativeTimeElapsedString(1000d*60*60*24*61));
    // 30.4 days per month, so will switch to reporting "3 months" after 91.2 days
    assertEquals("2 months ago", generateRelativeTimeElapsedString(1000d*60*60*24*90));
    assertEquals("2 months ago", generateRelativeTimeElapsedString(1000d*60*60*24*91));
    assertEquals("3 months ago", generateRelativeTimeElapsedString(1000d*60*60*24*92));

    assertEquals("11 months ago", generateRelativeTimeElapsedString(1000d*60*60*24*364));
    assertEquals("11 months ago", generateRelativeTimeElapsedString(TimeUnit.YEARS.toMillis(.99)));
    assertEquals("1 year ago", generateRelativeTimeElapsedString(TimeUnit.YEARS.toMillis(1)));
    assertEquals("1 year ago", generateRelativeTimeElapsedString(TimeUnit.YEARS.toMillis(1.99)));
    assertEquals("2 years ago", generateRelativeTimeElapsedString(TimeUnit.YEARS.toMillis(2)));
    assertEquals("2 years ago", generateRelativeTimeElapsedString(TimeUnit.YEARS.toMillis(2.5)));
  }

  public void testMillisToSeconds() throws Exception {
    assertEquals(5.5, millisToSeconds(5500));
    assertEquals(0.001, millisToSeconds(1));
    long currentTime = System.currentTimeMillis();
    assertEquals(currentTime, secondsToMillisLong(millisToSeconds(currentTime)));
  }

  public void testSecondsToMillis() throws Exception {
    assertEquals(5500, secondsToMillisLong(5.5));
    assertEquals(1, secondsToMillisLong(0.001));
  }

  /**
   * Checks that timestamps can be represented as doubles (so they can be passed back from JSNI)
   */
  public void testRepresentingTimestampAsDouble() throws Exception {
    long time = System.currentTimeMillis();
    assertEquals(time, (long)((double)time));
  }

  public void testGetYear() throws Exception {
    assertEquals(2014, getCalendarYear(1404771167165d));
  }

  public void testIsElapsed() throws Exception {
    // we test both the long version and the double version of the method
    assertFalse(isElapsed(10, 0, 9));
    assertFalse(isElapsed(10d, 0d, 9d));
    assertFalse(isElapsed(10, 0, 10));
    assertFalse(isElapsed(10d, 0d, 10d));
    assertTrue(isElapsed(10, 0, 11));
    assertTrue(isElapsed(10d, 0d, 11d));
  }

  /**
   * Compares {@link TimeUtils#truncateTime(long, long)} to {@link LocalDateTime#truncatedTo(TemporalUnit)}
   * and {@link Instant#truncatedTo(TemporalUnit)}.
   */
  @GwtIncompatible
  public void testTruncateTime() {
    // test various truncation methods for rounding to the closest 15-minute time frame
    Clock clock = new Clock();
    final long startTime = clock.stopTime();
    final long endTime = startTime + java.util.concurrent.TimeUnit.HOURS.toMillis(2);
    final int increment = 59_123;
//    final int increment = 1_000;
    final TemporalUnit unit = new TemporalUnitImpl(Duration.ofMinutes(15));
    System.out.printf("%42s,%22s,%22s,%38s%n", "time", "LDT.truncateTo", "Instant.truncateTo", "truncateTime(long)");
    while (clock.millis() < endTime) {
      LocalDateTime localDateTime = LocalDateTime.now(clock);
      long timeMillis = clock.millis();
      Instant timeInstant = clock.instant();
      LocalDateTime ldtTrunc = localDateTime.truncatedTo(unit);
      Instant ldtTruncInstant = ldtTrunc.toInstant(ZoneOffset.UTC);
      Instant instantTrunc = timeInstant.truncatedTo(unit);
      long dur = unit.getDuration().toMillis();
      long longTrunc = TimeUtils.truncateTime(timeMillis, dur);
      Instant longTruncInstant = Instant.ofEpochMilli(longTrunc);
      System.out.printf("%42s,%22s,%22s,%38s%n",
          String.format("%d (%s)", timeMillis, timeInstant),
          ldtTruncInstant,
          instantTrunc,
          String.format("%d (%s)", longTrunc, longTruncInstant));
      AssertUtils.assertAllEqual(instantTrunc, ldtTruncInstant, longTruncInstant);
      clock.advanceTime(increment);
    }
  }

  @GwtIncompatible
  public void testSecondsToInstant() throws Exception {
    double seconds = 1606363944.125;
    Instant result = secondsToInstant(seconds);
    assertEquals(Instant.parse("2020-11-26T04:12:24.125Z"), result);
    // verify that it has both a whole seconds integer and a nanoseconds component
    assertEquals(1606363944, result.getEpochSecond());
    assertEquals(125000000, result.getNano());
  }
}