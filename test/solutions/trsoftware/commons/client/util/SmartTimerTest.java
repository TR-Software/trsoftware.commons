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

import com.google.gwt.user.client.Timer;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import java.util.ArrayList;

import static com.google.gwt.core.client.Duration.currentTimeMillis;
import static solutions.trsoftware.commons.client.util.ListUtils.last;

public class SmartTimerTest extends CommonsGwtTestCase {

  private final int periodMillis = 200;
  private final int partialPeriodMillis = periodMillis / 2;
  private final double leeway = partialPeriodMillis  / 2;   // allow some leeway for all time comparisons because we're using the wall clock time
  private final int fullPeriodsToWait = 3;
  private final int verificationDelay = periodMillis * fullPeriodsToWait + partialPeriodMillis + (int)leeway;

  private ArrayList<Double> firingTimes;
  private SmartTimer smartTimer;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    firingTimes = new ArrayList<Double>();
    smartTimer = new SmartTimer() {
      @Override
      public void doRun() {
        firingTimes.add(currentTimeMillis());
      }
    };
  }

  public void testSchedule() throws Exception {
    assertNotScheduled();
    smartTimer.schedule(periodMillis);
    final double startedTime = currentTimeMillis();
    assertScheduled(startedTime + periodMillis, false);

    new Timer() {
      @Override
      public void run() {
        // make sure smartTimer only fired once
        assertEquals(1, firingTimes.size());
        assertEquals(startedTime + periodMillis, last(firingTimes), leeway);
        // make sure smartTimer doesn't think it will fire again
        assertNotScheduled();
        finishTest();
      }
    }.schedule(verificationDelay);
    delayTestFinish(verificationDelay + 1);  // delay the test long enough to verify smartTimer will only fire once
  }

  public void testScheduleRepeating() throws Exception {
    assertNotScheduled();
    smartTimer.scheduleRepeating(periodMillis);
    final double startedTime = currentTimeMillis();
    assertScheduled(startedTime + periodMillis, true);

    new Timer() {
      @Override
      public void run() {
        assertEquals(fullPeriodsToWait, firingTimes.size()); // make sure smartTimer fired once during every period
        Double lastFiringTime = last(firingTimes);
        //428020498160E12
        //428020498171E12
        assertEquals(startedTime + periodMillis * fullPeriodsToWait, lastFiringTime, leeway);
        assertEquals(lastFiringTime + periodMillis, smartTimer.getNextFiringTime(), leeway);
        assertEquals(periodMillis, smartTimer.getPeriodMillis());
        assertTrue(smartTimer.isRepeating());
        // now cancel the timer and make sure smartTimer doesn't think it will fire again
        smartTimer.cancel();
        assertNotScheduled();
        finishTest();
      }
    }.schedule(verificationDelay);
    delayTestFinish(verificationDelay + 1);  // delay the test long enough to verify smartTimer will only fire once
  }

  public void testScheduleRepeatingAfterInitialDelay() throws Exception {
    assertNotScheduled();
    smartTimer.scheduleRepeating(partialPeriodMillis, periodMillis);
    final double startTime = currentTimeMillis();
    assertScheduled(startTime + partialPeriodMillis, true);

    new Timer() {
      @Override
      public void run() {
        // verify the recorded firing times
        assertEquals(fullPeriodsToWait + 1, firingTimes.size()); // make sure smartTimer fired once for the initial delay and the once during every full period
        for (int i = 0; i < firingTimes.size(); i++)
          assertFiringTimeEquals(i, startTime + partialPeriodMillis + i * periodMillis);
        // verify that the timer settings are still as expected
        assertFiringTimeEquals(firingTimes.size()-1, smartTimer.getNextFiringTime() - periodMillis);
        assertEquals(periodMillis, smartTimer.getPeriodMillis());
        assertTrue(smartTimer.isRepeating());
        // now cancel the timer and make sure smartTimer doesn't think it will fire again
        smartTimer.cancel();
        assertNotScheduled();
        finishTest();
      }
    }.schedule(verificationDelay);
    delayTestFinish(verificationDelay + 1);  // delay the test long enough to verify smartTimer will only fire once
  }

  private void assertFiringTimeEquals(int i, double expected) {
    assertEquals(expected, firingTimes.get(i), leeway);
  }

  private void assertScheduled(double expectedFiringTime, boolean repeating) {
    assertTrue(smartTimer.isScheduled());
    assertEquals(expectedFiringTime, smartTimer.getNextFiringTime(), leeway);
    assertEquals(repeating ? periodMillis : 0, smartTimer.getPeriodMillis());
    assertEquals(repeating, smartTimer.isRepeating());
  }

  private void assertNotScheduled() {
    assertFalse(smartTimer.isScheduled());
    assertFalse(smartTimer.isRepeating());
    assertEquals(0d, smartTimer.getNextFiringTime());
    assertEquals(0, smartTimer.getPeriodMillis());
  }
}