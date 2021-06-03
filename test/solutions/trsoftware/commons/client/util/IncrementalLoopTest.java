/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.client.util;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.junit.client.GWTTestCase;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.TestUtils;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;

import java.util.ArrayList;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThat;

/**
 * @author Alex
 * @since 6/1/2021
 */
public class IncrementalLoopTest extends CommonsGwtTestCase {


  /**
   * Tests a quick loop that should terminate within a single increment
   */
  public void testSingleIncrementLoop() throws Exception {
    delayTestFinish(2000);

    // computes n! for ints between 0 and 20 (should be able to finish within a single increment of 1000 ms)
    Scheduler.get().scheduleIncremental(new ComputeFactorialsLoop(20, 1000) {
      @Override
      protected void loopFinished(boolean interrupted) {
        printStats();
        // check expected method call counts
        assertEquals(1, methodCounts.get("loopStarted"));
        assertEquals(1, getIncrementCount());
        assertEquals(1, methodCounts.get("incrementStarted"));
        assertEquals(1, methodCounts.get("incrementFinished"));
        assertEquals(n, getIterationCount());
        assertEquals(n, methodCounts.get("loopBody"));
        assertEquals(n + 1, methodCounts.get("hasMoreWork"));  // we expect 1 extra call when the loop condition breaks
        // check the loop state (assert that it finished normally)
        assertFalse(interrupted);
        assertFalse(isStopped());
        assertTrue(isStarted());
        assertTrue(isFinished());
        assertFalse(hasMoreWork());
        // check that the expected result was computed
        assertFactorialsComputed();
        finishTest();
      }
    });
  }

  /** Tests a loop that will have to preempt itself and require multiple increments */
  public void testMultiIncrementLoop() throws Exception {
    delayTestFinish(2000);

    // computes n! for ints between 0 and 20, busy-waiting after each iteration in order to test the execution spanning multiple increments
    int incrementMillis = 100;
    Scheduler.get().scheduleIncremental(new ComputeFactorialsLoop(20, incrementMillis) {
      @Override
      protected void loopBodyImpl(int i) {
        super.loopBodyImpl(i);
        TestUtils.busyWait(incrementMillis / 5);
      }
      @Override
      protected void loopFinished(boolean interrupted) {
        printStats();
        // check expected method call counts
        assertEquals(1, methodCounts.get("loopStarted"));
        int nIncrements = getIncrementCount();
        // the incrementStarted/Finished methods should have been called exactly once for each increment
        assertEquals(nIncrements, methodCounts.get("incrementStarted"));
        assertEquals(nIncrements, methodCounts.get("incrementFinished"));
        // we expect this loop to have required approx. n/5 increments, since it waits 1/5th of an increment duration between iterations
        // but in order to not make this test too sensitive to system load, we give it some leeway in the following assertions (especially for the upper-bound)
        int expectedIncrements = n / 5;
        assertThat(nIncrements).isBetween(expectedIncrements - 1, expectedIncrements * 2);
        assertEquals(n, getIterationCount());
        assertEquals(n, methodCounts.get("loopBody"));
        assertEquals(n + nIncrements, methodCounts.get("hasMoreWork"));  // we expect 1 extra call when the loop condition breaks and 1 more for each increment that times out
        // check the loop state (assert that it finished normally)
        assertFalse(interrupted);
        assertFalse(isStopped());
        assertTrue(isStarted());
        assertTrue(isFinished());
        assertFalse(hasMoreWork());
        // check that the expected result was computed
        assertFactorialsComputed();
        finishTest();
      }
    });
  }

  /**
   * Tests that at least 1 iteration is executed per increment, even if the iteration duration exceeds the max
   * increment duration.  This ensures that the loop always makes progress.
   */
  public void testThatLoopAlwaysMakesProgress() throws Exception {
    delayTestFinish(2000);

    // computes n! for ints between 0 and 20, busy-waiting longer than the max increment duration after each iteration,
    // so that we can check that at least 1 iteration is executed in each increment
    int incrementMillis = 20;
    Scheduler.get().scheduleIncremental(new ComputeFactorialsLoop(20, incrementMillis) {
      @Override
      protected void loopBodyImpl(int i) {
        super.loopBodyImpl(i);
        TestUtils.busyWait(incrementMillis + 1);  // wait long enough to ensure that only 1 iteration is able to run in each increment
      }
      @Override
      protected void loopFinished(boolean interrupted) {
        printStats();
        // check expected method call counts
        assertEquals(1, methodCounts.get("loopStarted"));
        int nIncrements = getIncrementCount();
        assertEquals(n, nIncrements);  // the busy-wait in loop body should've ensured that only 1 iteration was able to run in each increment
        // the incrementStarted/Finished methods should have been called exactly once for each increment
        assertEquals(nIncrements, methodCounts.get("incrementStarted"));
        assertEquals(nIncrements, methodCounts.get("incrementFinished"));
        assertEquals(n, getIterationCount());
        assertEquals(n, methodCounts.get("loopBody"));
        assertEquals(n + nIncrements, methodCounts.get("hasMoreWork"));  // we expect 1 extra call when the loop condition breaks and 1 more for each increment that times out
        // check the loop state (assert that it finished normally)
        assertFalse(interrupted);
        assertFalse(isStopped());
        assertTrue(isStarted());
        assertTrue(isFinished());
        assertFalse(hasMoreWork());
        // check that the expected result was computed
        assertFactorialsComputed();
        finishTest();
      }
    });
  }

  /**
   * Tests that the {@link IncrementalLoop#stop()} method interrupts the loop.
   */
  public void testStop() throws Exception {
    delayTestFinish(1000);
    final Duration duration = new Duration();
    Scheduler.get().scheduleIncremental(new LoopTester(10) {
      @Override
      protected boolean hasMoreWorkImpl() {
        return true; // keep going until the stop method is invoked by the loop body
      }
      @Override
      protected void loopBodyImpl(int i) {
        // stop the loop while executing the second increment
        if (getIncrementCount() == 1)
          stop();
        TestUtils.busyWait(1);  // do some trivial work in each iteration
      }
      protected void loopFinished(boolean interrupted) {
        printStats();
        // check expected method call counts
        assertEquals(1, methodCounts.get("loopStarted"));
        int nIncrements = getIncrementCount();
        assertEquals(2, nIncrements);
        // the incrementStarted/Finished methods should have been called exactly once for each increment
        assertEquals(nIncrements, methodCounts.get("incrementStarted"));
        assertEquals(nIncrements, methodCounts.get("incrementFinished"));
        int nIterations = getIterationCount();
        assertEquals(nIterations, methodCounts.get("loopBody"));
        assertEquals(nIterations + nIncrements, methodCounts.get("hasMoreWork"));  // we expect 1 extra call when the loop condition breaks and 1 more for each increment that times out
        // check the loop state (assert that it was interrupted)
        assertTrue(isStarted());
        assertTrue(interrupted);
        assertTrue(isStopped());
        assertFalse(isFinished());
        assertTrue(hasMoreWork());
        finishTest();
      }
    });
  }

  /**
   * Computes {@code n!} for integers between 0 and 20.
   *
   * Subclasses must implement {@link #loopFinished(boolean)}
   */
  private abstract class ComputeFactorialsLoop extends LoopTester {
    protected final ArrayList<Long> factorials;
    protected final int n;

    public ComputeFactorialsLoop(int n, int incrementMillis) {
      super(incrementMillis);
      this.n = n;
      factorials = new ArrayList<>();
    }

    @Override
    protected boolean hasMoreWorkImpl() {
      return factorials.size() < n;
    }

    @Override
    protected void loopBodyImpl(int i) {
      if (i == 0)
        factorials.add(1L);
      else
        factorials.add(factorials.get(i - 1) * i);
    }

    /** Asserts that {@link #factorials} contains 0! .. (n-1)! */
    protected void assertFactorialsComputed() {
      assertEquals(n, factorials.size());
      for (int i = 0; i < factorials.size(); i++) {
        assertEquals(MathUtils.factorial(i), (long)factorials.get(i));
      }
    }
  }


  private abstract class LoopTester extends IncrementalLoop {
    protected final HashCounter<String> methodCounts = new HashCounter<>();
    private Duration duration;

    public LoopTester(int incrementMillis) {
      super(incrementMillis);
    }

    @Override
    protected void loopStarted() {
      duration = new Duration();
      methodCounts.increment("loopStarted");
    }

    @Override
    protected final boolean hasMoreWork() {
      methodCounts.increment("hasMoreWork");
      return hasMoreWorkImpl();
    }

    protected abstract boolean hasMoreWorkImpl();

    @Override
    protected final void loopBody(int i) {
      methodCounts.increment("loopBody");
      loopBodyImpl(i);
    }

    protected abstract void loopBodyImpl(int i);

    @Override
    protected void incrementStarted() {
      methodCounts.increment("incrementStarted");
    }

    @Override
    protected void incrementFinished(Duration incrementDuration) {
      methodCounts.increment("incrementFinished");
    }

    /**
     * Should implement this method to verify assertions and call {@link GWTTestCase#finishTest()}
     */
    @Override
    protected abstract void loopFinished(boolean interrupted);

    public void printStats() {
      getLogger().info(Strings.lenientFormat("Loop executed %s increments and ran for %s iterations (%s ms)",
          getIncrementCount(),
          getIterationCount(),
          duration.elapsedMillis()
      ));
    }
  }

}