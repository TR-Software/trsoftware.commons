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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Timer;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * May 20, 2009
 *
 * @author Alex
 */
public class RetryableCommandTest extends CommonsGwtTestCase {


  /** This test will run the command for 5 iterations but will not hit the max iterations limit */
  public void testWithFixedNumberOfIterations() throws Exception {
    final int maxAttempts = 10;
    final int nIterations = 5;
    final int delayMillis = 100;
    final int expectedRunTimeMillis = nIterations * delayMillis;  // we expect to be done after this time elapses
    final ArrayList<Double> times = new ArrayList<Double>();  // the time at which each iteration was executed

    delayTestFinish(expectedRunTimeMillis * 2);
    final Duration runningTime = new Duration();
    final RetryableCommand retriableCommand = new RetryableCommand(maxAttempts) {
      protected boolean executeIteration() {
        times.add(TimeUtils.currentTimeMillis());
        int i = times.size();
        System.out.println("Iteration " + i + " executing at +" + runningTime.elapsedMillis() + " ms.");
        return i < nIterations;
      }
    };

    assertFalse(retriableCommand.isStarted());
    assertFalse(retriableCommand.isStopped());
    assertEquals(maxAttempts, retriableCommand.getAttemptsRemaining());

    retriableCommand.start(delayMillis);

    assertTrue(retriableCommand.isStarted());
    assertFalse(retriableCommand.isStopped());
    assertEquals(maxAttempts, retriableCommand.getAttemptsRemaining());

    // check up on the task after 1.5 the expected time has passed
    new Timer() {
      public void run() {
        assertTrue(retriableCommand.isStarted());
        assertTrue(retriableCommand.isStopped());
        assertEquals(maxAttempts - nIterations, retriableCommand.getAttemptsRemaining());

        assertEquals(nIterations, times.size());
        assertNumbersIncreasingByConstant(times, delayMillis, delayMillis / 2);
        finishTest();
      }
    }.schedule((int)(expectedRunTimeMillis * 1.5));
  }

  /** This test will run the command for until the max iterations limit is reached */
  public void testIterationLimit() throws Exception {
    final int maxAttempts = 10;
    final int delayMillis = 75;
    final int expectedRunTimeMillis = maxAttempts * delayMillis;  // we expect to be done after this time elapses
    final ArrayList<Double> times = new ArrayList<Double>();  // the time at which each iteration was executed

    delayTestFinish(expectedRunTimeMillis * 2);

    final Duration runningTime = new Duration();
    final RetryableCommand retriableCommand = new RetryableCommand(maxAttempts) {
      protected boolean executeIteration() {
        times.add(TimeUtils.currentTimeMillis());
        int i = times.size();
        System.out.println("Iteration " + i + " executing at +" + runningTime.elapsedMillis() + " ms.");
        return true;  // always continue
      }
    };


    assertFalse(retriableCommand.isStarted());
    assertFalse(retriableCommand.isStopped());
    assertEquals(maxAttempts, retriableCommand.getAttemptsRemaining());

    retriableCommand.start(delayMillis);

    assertTrue(retriableCommand.isStarted());
    assertFalse(retriableCommand.isStopped());
    assertEquals(maxAttempts, retriableCommand.getAttemptsRemaining());

    // check up on the task after 1.5 the expected time has passed
    new Timer() {
      public void run() {
        assertTrue(retriableCommand.isStarted());
        assertTrue(retriableCommand.isStopped());
        assertEquals(0, retriableCommand.getAttemptsRemaining());

        assertEquals(maxAttempts, times.size());
        assertNumbersIncreasingByConstant(times, delayMillis, delayMillis / 5);
        finishTest();
      }
    }.schedule((int)(expectedRunTimeMillis * 1.5));
  }

  /**
   * Asserts that the numbers in the given list are in ascending order, with
   * b = a + n iff b follows a
   * The comparison will be done +/- margin
   */
  private void assertNumbersIncreasingByConstant(List<Double> numbers, double n, double margin) {
    for (int i = 1; i < numbers.size(); i++) {
      double a = numbers.get(i - 1) + n;
      double b = numbers.get(i);
      assertEquals("a - b = "+(a-b), a, b, margin);
    }
  }
}