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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;

/**
 * @author Alex, 1/8/14
 */
public class DurationTest extends TestCase {

  public void testFormatDurationAsMinSec() throws Exception {
    // NOTE: there's no rounding up of milliseconds in this implementation
    assertEquals("00:00", format(0, false));
    assertEquals("00:00", format(1, false));
    assertEquals("00:00.001", format(1, true));
    assertEquals("00:00", format(999, false));
    assertEquals("00:01", format(1000, false));
    assertEquals("00:01", format(1999, false));
    assertEquals("00:01.999", format(1999, true));
    assertEquals("00:02", format(2000, false));
    assertEquals("00:59", format(59999, false));
    assertEquals("01:00", format(60000, false));
    assertEquals("01:02", format(62000, false));
    assertEquals("59:59", format(TimeUnit.MINUTES.toMillis(59) + TimeUnit.SECONDS.toMillis(59), false));
    assertEquals("01:00:00", format(TimeUnit.MINUTES.toMillis(60), false));
    assertEquals("01:59:00", format(TimeUnit.MINUTES.toMillis(60) + TimeUnit.MINUTES.toMillis(59) + 999, false));
    assertEquals("01:59:00.999", format(TimeUnit.MINUTES.toMillis(60) + TimeUnit.MINUTES.toMillis(59) + 999, true));
    assertEquals("23:59:00.999", format(TimeUnit.HOURS.toMillis(23) + TimeUnit.MINUTES.toMillis(59) + 999, true));
    assertEquals("00:00:00", format(TimeUnit.HOURS.toMillis(24), false));
    assertEquals("00:00:00.999", format(TimeUnit.HOURS.toMillis(24) + 999, true));
  }

  private String format(long millis, boolean printMillis) {
    return Duration.formatAsClockTime(millis, printMillis);
  }

  public void testComputeSpeed() throws Exception {
    Duration duration = new Duration();
    Thread.sleep(500);
    System.out.println("Speed: ");
    for (solutions.trsoftware.commons.client.util.TimeUnit timeUnit : solutions.trsoftware.commons.client.util.TimeUnit.values()) {
      System.out.printf("  %s: %.8f%n", timeUnit, duration.computeSpeed(1000, timeUnit));
    }
  }
}
