package solutions.trsoftware.commons.server.testutil;

import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.server.util.NanoDuration;
import junit.framework.TestCase;

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
      NanoDuration duration = new NanoDuration(name);
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
      assertEquals(.33, result, .05); // slowTask is 3 times slower than fastTask (allow 5% accuracy margin)
    }
    {
      TimedTask slowTask = new TimedTask("slowTask", 150);
      TimedTask fastTask = new TimedTask("fastTask", 50);
      double result = compare(fastTask, slowTask, iterations);
      assertTrue(slowTask.x > 0);
      assertTrue(fastTask.x > 0);
      // fastTask is 3 times faster than slowTask
      assertEquals(3, result, .05);  // (allow 5% accuracy margin)
    }
  }

  private double compare(TimedTask t1, TimedTask t2, int iterations) {
    return PerformanceComparison.compare(t1, t1.name, t2, t2.name, iterations);
  }
}