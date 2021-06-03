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
import com.google.gwt.core.client.Scheduler;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @since Dec 4, 2008
 * @author Alex
 */
public class IncrementalForLoopTest extends CommonsGwtTestCase {
  private List<Integer> values;
  private IncrementalForLoop loop;


  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    values = new ArrayList<>();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    values = null;
    super.gwtTearDown();
  }

  /** Tests a loop similar to {@code for (int i = 0; i < 5; i++) } */
  public void testQuickIncrementalForLoopWithSimpleConstructor() throws Exception {
    delayTestFinish(1000);  // the assertListsEqual method will call finishTest()
    ArrayList<Integer> expected = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      expected.add(i);
    }
    Scheduler.get().scheduleIncremental(loop = new IncrementalForLoop(5) {
      protected void loopBody(int i) {
        values.add(i);
      }
      protected void loopFinished(boolean interrupted) {
        assertListsEqual(expected);
        assertTrue(isFinished());
        assertFalse(hasMoreWork());
      }
    });
  }


  /** Tests a loop similar to {@code for (int i = -1; i < 4; i += 2) } */
  public void testQuickIncrementalForLoopMinus1To4By2() throws Exception {
    delayTestFinish(1000);  // the assertListsEqual method will call finishTest()
    ArrayList<Integer> expected = new ArrayList<>();
    for (int i = -1; i < 4; i += 2) {
      expected.add(i);
    }
    Scheduler.get().scheduleIncremental(loop = new IncrementalForLoop(-1, 4, 2, 1000) {
      protected void loopBody(int i) {
        values.add(i);
      }
      protected void loopFinished(boolean interrupted) {
        assertListsEqual(expected);
        assertTrue(isFinished());
        assertFalse(hasMoreWork());
      }
    });
  }


  /** Tests a loop similar to {@code for (int i = 5; i > -5; i -= 2) } */
  public void testQuickIncrementalForLoopMinus1To5By2() throws Exception {
    delayTestFinish(1000);  // the assertListsEqual method will call finishTest()
    ArrayList<Integer> expected = new ArrayList<>();
    for (int i = 5; i > -5; i -= 2) {
      expected.add(i);
    }
    Scheduler.get().scheduleIncremental(loop = new IncrementalForLoop(5, -5, -2, 1000) {
      protected void loopBody(int i) {
        values.add(i);
      }
      protected void loopFinished(boolean interrupted) {
        assertListsEqual(expected);
        assertTrue(isFinished());
        assertFalse(hasMoreWork());
      }
    });
  }


  /** Tests a loop that will have to preempt itself and use up multiple increments */
  public void testLongIncrementalForLoop() throws Exception {
    delayTestFinish(10000); // give the loop enough time to finish
    // NOTE: the following test depends on CPU speed - it could fail on faster or slower systems
    final int maxIterations = 10000;  // do enough iterations to need more than one time increment
    final int timeLimit = 100;  // the time limit per increment in millis (should be long enough to give accurate results)
    final Duration duration = new Duration();
    Scheduler.get().scheduleIncremental(loop = new IncrementalForLoop(0, maxIterations, 1, timeLimit) {
      protected void loopBody(int i) {
        values.add(i);
      }
      protected void loopFinished(boolean interrupted) {
        int increments = loop.getIncrementCount();
        int iterations = loop.getIterationCount();
        assertEquals(values.size(), iterations);
        int elapsed = duration.elapsedMillis();
        String msg = "Loop executed " + increments + " increments and ran for " + iterations + " iterations (" + elapsed + " ms).";
        System.out.println(msg);
        assertEquals("Did not finish all iterations; " + msg, maxIterations, iterations); // should have had enough time to finish all iterations
        // approximately test that we're within 2 units from the expected number of increments
        assertTrue("Did not execute more than 1 increment; " + msg, increments > 1);
        finishTest();
      }
    });
  }

  /** Asserts that the expected List is equal to the actual list */
  private void assertListsEqual(List<Integer> expected) {
    assertEquals(expected, values);
    finishTest();
  }

  /**
   * Tests that the {@link IncrementalForLoop#stop()} method interrupts the loop.
   */
  public void testStop() throws Exception {
    delayTestFinish(10000);
    final Duration duration = new Duration();
    Scheduler.get().scheduleIncremental(loop = new IncrementalForLoop(0, 10000, 1, 100) {
      protected void loopBody(int i) {
        // stop the loop while executing the second increment
        if (getIncrementCount() == 1)
          stop();
        values.add(i);  // do some work
      }
      protected void loopFinished(boolean interrupted) {
        assertTrue(interrupted);
        assertTrue(isStopped());
        assertFalse(isFinished());
        assertTrue(hasMoreWork());
        int increments = getIncrementCount();
        assertEquals(2, increments);
        int iterations = values.size();
        int elapsed = duration.elapsedMillis();
        System.out.println("Loop executed " + increments + " increments and ran for " + iterations + " iterations (" + elapsed + " ms).");
        finishTest();
      }
    });
  }


}