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

package solutions.trsoftware.commons.shared.util.time;

import com.google.gwt.core.client.GWT;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.TimeUnit;
import solutions.trsoftware.commons.shared.util.TimeUtils;
import solutions.trsoftware.commons.shared.util.function.ThrowingRunnable;

/**
 * Nov 13, 2010
 *
 * @author Alex
 */
public class RateLimiterTest extends CommonsGwtTestCase {

  private MockTime mockTime;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    mockTime = new MockTime(TimeUtils.currentTimeMillis());
  }

  public void testRateLimiter() throws Exception {
    // check that constructor validates its parameters
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        newRateLimiter(1, 0);
      }
    });
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        newRateLimiter(0, 1);
      }
    });
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        newRateLimiter(-1, 1);
      }
    });
    assertNotNull(newRateLimiter(1, 1));  // this is an acceptable value though

    // check a window of size 1
    {
      final RateLimiter r = newRateLimiter(1, 100);
      assertEquals(0d, r.millisUntilCanProceed());
      r.checkRateLimit();
      assertEquals(100d, r.millisUntilCanProceed());
      AssertUtils.assertThrows(RateLimiter.RateLimitException.class, (ThrowingRunnable)r::checkRateLimit);
      mockTime.advance(55);
      assertEquals(45d, r.millisUntilCanProceed());
      mockTime.advance(45);
      r.checkRateLimit();
      AssertUtils.assertThrows(RateLimiter.RateLimitException.class, (ThrowingRunnable)r::checkRateLimit);
    }

    // check a window of size 2
    {
      final RateLimiter r = newRateLimiter(2, 100);
      r.checkRateLimit();
      mockTime.advance(5);
      r.checkRateLimit();
      AssertUtils.assertThrows(RateLimiter.RateLimitException.class, (ThrowingRunnable)r::checkRateLimit);
      assertEquals(95d, r.millisUntilCanProceed());
      mockTime.advance(95);
      r.checkRateLimit();
      mockTime.advance(5);
      r.checkRateLimit();
      AssertUtils.assertThrows(RateLimiter.RateLimitException.class, (ThrowingRunnable)r::checkRateLimit);
    }
    
    // check a window of size 3
    {
      final RateLimiter r = new RateLimiter(3, .2, TimeUnit.SECONDS, mockTime, "foo");
      r.checkRateLimit();
      r.checkRateLimit();
      mockTime.advance(50);
      assertEquals(0d, r.millisUntilCanProceed());
      r.checkRateLimit();
      assertEquals(150d, r.millisUntilCanProceed());
      AssertUtils.assertThrows(RateLimiter.RateLimitException.class, (ThrowingRunnable)r::checkRateLimit);
      mockTime.advance(150);
      // window should now contain just the 1 event that happened after 50ms
      r.checkRateLimit();
      r.checkRateLimit();
      assertEquals(50d, r.millisUntilCanProceed());
      AssertUtils.assertThrows(RateLimiter.RateLimitException.class, (ThrowingRunnable)r::checkRateLimit);
    }
  }

  public RateLimiter newRateLimiter(int max, int timeWindowDurationMillis) {
    return new RateLimiter(max, timeWindowDurationMillis, TimeUnit.MILLISECONDS, mockTime, "foo");
  }

  public void testCheckRateLimitExceptionMessageFormatting() throws Exception {
    assertEquals("Exceeded the maximum rate of 1 foo per second",
        new RateLimiter(1, 1, TimeUnit.SECONDS, mockTime, "foo").new RateLimitException().getMessage());
    assertEquals("Exceeded the maximum rate of 2 foos per hour",
        new RateLimiter(2, 1, TimeUnit.HOURS, mockTime, "foo").new RateLimitException().getMessage());
    // NOTE: Java and JS render ""+2.0 differently (Java gives "2.0" and JS gives "2"), so we want the expected string
    // computed dynamically (otherwise hosted mode and web mode tests will produce different results)
    assertEquals("Exceeded the maximum rate of 1 event per " + (GWT.isScript() ? "2" : "2.0") + " minutes",
        new RateLimiter(1, 2, TimeUnit.MINUTES, mockTime, null).new RateLimitException().getMessage());
    assertEquals("Exceeded the maximum rate of 3 events per " + (GWT.isScript() ? "5" : "5.0") + " months",
        new RateLimiter(3, 5, TimeUnit.MONTHS, mockTime, null).new RateLimitException().getMessage());
  }
}