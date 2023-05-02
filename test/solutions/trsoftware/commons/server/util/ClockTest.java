/*
 * Copyright 2022 TR Software Inc.
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
 */

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.TestCaseCanStopClock;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.concurrent.TimeUnit;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

public class ClockTest extends TestCaseCanStopClock {

  @Slow
  public void testClock() throws Exception {
    // put the clock through its various states several times
    for (int i = 0; i < 3; i++) {
      // NOTE: the following line commented out on 9/26/2019 because it creates an arbitrary system-dependent timing restriction
      // assertApproximatelyEquals(System.currentTimeMillis(), Clock.currentTimeMillis(), 10);   // not more than 10 millis should have elapsed between those two calls

      // make sure that no operations are possible while the clock is ticking in normal mode
      assertThrows(IllegalStateException.class, (Runnable)() -> Clock.advance(100));
      assertThrows(IllegalStateException.class, (Runnable)() -> Clock.advance(100));
      assertThrows(IllegalStateException.class, (Runnable)() -> Clock.set(112934));
      assertThrows(IllegalStateException.class, (Runnable)Clock::startTicking);

      long stoppedTime = Clock.stop();
      assertEquals(stoppedTime, Clock.currentTimeMillis());
      Thread.sleep(1000);
      // make sure the clock is still stopped
      assertEquals(stoppedTime, Clock.currentTimeMillis());
      // the clock is now 1000 ms behind
      Clock.advance(2000);
      long time = Clock.currentTimeMillis();
      assertEquals(stoppedTime + 2000, time);
      // the clock is now 1000 ms ahead

      // the clock shouldn't change after we start it ticking in fake mode
      time = Clock.currentTimeMillis();
      Clock.startTicking();
      assertApproximatelyEquals(time, Clock.currentTimeMillis(), 5);  // allow some leeway due to potential delay caused by system load
      // the difference between the clock and real time should be preserved
      Thread.sleep(1000);
      assertApproximatelyEquals(time + 1000, Clock.currentTimeMillis(), 10);

      // make sure that no operations are possible while the clock is ticking in fake mode
      assertThrows(IllegalStateException.class, (Runnable)() -> Clock.advance(100));
      assertThrows(IllegalStateException.class, (Runnable)() -> Clock.set(112934));
      assertThrows(IllegalStateException.class, (Runnable)Clock::startTicking);

      // make sure that when the clock is stopped in fake mode it's still showing the fake time
      time = Clock.currentTimeMillis();
      Clock.stop();
      assertEquals(time, Clock.currentTimeMillis());
      Clock.advance(2000);
      assertEquals(time + 2000, Clock.currentTimeMillis());

      // resume normal operation
      Clock.resetToNormal();
    }
  }

  /**
   * Checks that time2 is within delta ms ahead of time1
   */
  private static void assertApproximatelyEquals(long time1, long time2, long delta) {
    assertTrue(time2 >= time1);
    assertTrue(time2 < time1 + delta);
  }

  public void testPerformance() throws Exception {
    int n = 1000000;
    benchmarkSystemCurrentTimeMillis(n);
    benchmarkSystemCurrentTimeMillis(n);
    benchmarkUninstrumentedClock(n);
    benchmarkUninstrumentedClock(n);
    benchmarkInstrumentedClock(n);
    benchmarkInstrumentedClock(n);
  }

  private void benchmarkUninstrumentedClock(int n) {
    long startTime = System.nanoTime();
    for (int i = 0; i < n; i++) {
      Clock.currentTimeMillis();
    }
    double timeElapsed = (double)(System.nanoTime() - startTime) / TimeUnit.MILLISECONDS.toNanos(1);
    System.out.printf("%d iterations of uninstrumented Clock took %f ms%n", n, timeElapsed);
  }

  private void benchmarkInstrumentedClock(int n) {
    long startTime = System.nanoTime();
    Clock.stop();
    for (int i = 0; i < n; i++) {
      Clock.currentTimeMillis();
    }
    double timeElapsed = (double)(System.nanoTime() - startTime) / TimeUnit.MILLISECONDS.toNanos(1);
    System.out.printf("%d iterations of instrumented Clock took %f ms%n", n, timeElapsed);
  }

  private void benchmarkSystemCurrentTimeMillis(int n) {
    long startTime = System.nanoTime();
    for (int i = 0; i < n; i++) {
      System.currentTimeMillis();
    }
    double timeElapsed = (double)(System.nanoTime() - startTime) / TimeUnit.MILLISECONDS.toNanos(1);
    System.out.printf("%d iterations of System.currentTimeMillis() Clock took %f ms%n", n, timeElapsed);
  }

  /**
	 * Asserts that two long are equal within the given margin of error
	 */
	static public void assertEquals(long expected, long actual, long margin) {
    assertTrue(StringUtils.template("$1 != $2 +/- $3",expected, actual, margin),
        expected - margin <= actual && expected + margin >= actual);
	}
}