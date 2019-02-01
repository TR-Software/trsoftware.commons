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

package solutions.trsoftware.commons.server.testutil;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.shared.annotations.Slow;

public class PerformanceComparisonTest extends TestCase {

  private static class TimedTask implements Runnable {
    private int x = 0;  // a counter that will be incremented by the task
    private int timeLimit;
    private String name;
    private TimedTask(String name, int timeLimit) {
      this.name = name;
      this.timeLimit = timeLimit;
    }
    public void run() {
      Duration duration = new Duration(name);
      while (duration.elapsedMillis() < timeLimit)
        x++;  // this task will run for timeLimit millis
      System.out.println(duration);
    }
  }

  @Slow
  public void testPerformanceComparison() throws Exception {
    checkComparisons(1);
    checkComparisons(2);
    checkComparisons(3);
  }

  private void checkComparisons(int iterations) {
    {
      TimedTask slowTask = new TimedTask("slowTask", 150);
      TimedTask fastTask = new TimedTask("fastTask", 50);
      double result = compare(slowTask, fastTask, iterations);
      assertTrue(slowTask.x > 0); // first of all, assert that the tasks actually ran
      assertTrue(fastTask.x > 0);
      assertEquals(.33, result, .10); // slowTask is 3 times slower than fastTask (allow 10% accuracy margin)
    }
    {
      TimedTask slowTask = new TimedTask("slowTask", 150);
      TimedTask fastTask = new TimedTask("fastTask", 50);
      double result = compare(fastTask, slowTask, iterations);
      assertTrue(slowTask.x > 0);
      assertTrue(fastTask.x > 0);
      // fastTask is 3 times faster than slowTask
      assertEquals(3, result, .10);  // (allow 10% accuracy margin)
    }
  }

  private double compare(TimedTask t1, TimedTask t2, int iterations) {
    return PerformanceComparison.compare(t1, t1.name, t2, t2.name, iterations);
  }
}