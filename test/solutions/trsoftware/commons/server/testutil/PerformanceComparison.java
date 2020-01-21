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

import java.util.List;
import java.util.concurrent.TimeUnit;

import static solutions.trsoftware.commons.shared.util.StringUtils.firstNotBlank;

/**
 * Compares the performance of a number of iterations of each Runnable task specified.
 *
 * @see BenchmarkRunner
 * @see BenchmarkingTestCase
 */
public abstract class PerformanceComparison {

  /**
   * @param iterations the number of iterations of each task to perform
   * <p style="color: #6495ed; font-weight: bold;">
   *   TODO: Instead of doing a fixed number of iterations for each task, do as many as it takes to go over
   *   some threshold value (e.g. 100ms), and a time-per-iteration metric instead of the total time.
   *   This could significantly speed up unit tests that do too many unneeded iterations
   * </p>
   * @return the multiplier of running time of {@code task1} compared to {@code task2}
   * (i.e. how many times is task1 faster than task2.
   */
  public static double compare(Runnable task1, String task1Name, Runnable task2, String task2Name, int iterations) {
    // warm up both tasks
    measureNanoTime(task1, iterations);
    measureNanoTime(task2, iterations);

    // TODO: interleave the tasks (so that neither gets the advantage or disadvantage of going first)
    long task1Ns = measureNanoTime(task1, iterations);
    System.out.printf("%s took %,d ns%n", task1Name, task1Ns);
    long task2Ns = measureNanoTime(task2, iterations);
    System.out.printf("%s took %,d ns%n", task2Name, task2Ns);

    // will have to subtract the overhead of running an empty task for that number of iterations
    long emptyTaskNs = measureNanoTime(new Runnable() {
      public void run() {
      }
    }, iterations);
    task1Ns -= emptyTaskNs;
    task2Ns -= emptyTaskNs;

    String label;
    double multiplier;
    if  (task1Ns <= task2Ns) {
      label = "faster";
      multiplier = (double)task2Ns / task1Ns;
    } else {
      label = "slower";
      multiplier = (double)task1Ns / task2Ns;
    }
    System.out.printf("%s is %.3f times %s than %s (%,d ns versus %,d ns)%n", task1Name, multiplier, label, task2Name, task1Ns, task2Ns);
    if (Math.min(task1Ns, task2Ns) < TimeUnit.MILLISECONDS.toNanos(100)) {
      System.out.println("WARNING: One of the tasks ran for less than 100 milliseconds - increase the number of iterations.");
    }
    return (double)task2Ns / task1Ns;
  }

  /**
   * @return the multiplier of running time of {@code task1} compared to {@code task2}
   * (i.e. how many times is task1 faster than task2.
   */
  public static double compare(Runnable task1, Runnable task2, int iterations) {
    return compare(task1, getTaskName(task1, "task1"),
        task2, getTaskName(task2, "task2"),
        iterations);
  }

  /**
   * @return the multiplier of running time of {@code task1} compared to {@code task2}
   * (i.e. how many times is task1 faster than task2.
   */
  public static double compare(NamedRunnable task1, NamedRunnable task2, int iterations) {
    return compare(task1, task1.getName(),
        task2, task2.getName(),
        iterations);
  }


  public static abstract class NamedRunnable implements Runnable {
    private String name;

    protected NamedRunnable(String name) {
      this.name = name;
    }

    public final String getName() {
      return name;
    }
  }

  private static String getTaskName(Runnable task, String defaultName) {
    return firstNotBlank(task.getClass().getSimpleName(), defaultName);
  }

//
//  /**
//   * @return the multiplier of running time of task 1 compared to task 2
//   * (i.e. how many times is task1 faster than task2.
//   */
//  public static double compare(Runnable task1, String task1Name, Runnable task2, String task2Name, int iterations) {
//    // warm up both tasks for just 1 iteration
//    runTask(task1, iterations / 2);
//    runTask(task2, iterations / 2);
//
//    // interleave the tasks (so that neither gets the advantage or disadvantage of going first)
//    long task1Ns = 0;
//    long task2Ns = 0;
//    int chunks = 20;
//    for (int i = 0; i < chunks; i++) {
//      if (i % 2 == 0) {
//        task1Ns += runTask(task1, iterations / chunks);
//        task2Ns += runTask(task2, iterations / chunks);
//      }
//      else {
//        task2Ns += runTask(task2, iterations / chunks);
//        task1Ns += runTask(task1, iterations / chunks);
//      }
//    }
//
//
//    // will have to subtract the overhead of running an empty task for that number of iterations
//    long emptyTaskNs = runTask(new Runnable() {
//      public void run() {
//      }
//    }, iterations);
//    task1Ns -= emptyTaskNs;
//    task2Ns -= emptyTaskNs;
//
//    String label;
//    double multiplier;
//    if  (task1Ns <= task2Ns) {
//      label = "faster";
//      multiplier = (double)task2Ns / task1Ns;
//    } else {
//      label = "slower";
//      multiplier = (double)task1Ns / task2Ns;
//    }
//    System.out.printf("%s is %s times %s than %s (%d ns versus %d ns)%n", task1Name, multiplier, label, task2Name, task1Ns, task2Ns);
//    if (Math.min(task1Ns, task2Ns) < TimeUnit.SECONDS.toNanos(2)) {
//      System.out.println("WARNING: One of the tasks ran for less than 2 seconds - increase the number of iterations.");
//    }
//    return multiplier;
//  }

  /**
   * Benchmarks a task
   * @param task the task to perform
   * @param iterations the number of times to repeat the task
   * @return the nanos elapsed after running the given number of iterations of the task
   */
  public static long measureNanoTime(Runnable task, int iterations) {
    long start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      task.run();
    }
    long end = System.nanoTime();
    return end - start;
  }

  /**
   * Can be used as an arg for {@link #compare(NamedRunnable, NamedRunnable, int)}: encapsulates a set of test inputs,
   * to be processed in each iteration.
   *
   * @author Alex
   * @since 7/20/2018
   */
  public abstract static class BenchmarkTask<E> extends NamedRunnable {

    protected List<E> inputs;

    public BenchmarkTask(String name, List<E> inputs) {
      super(name);
      this.inputs = inputs;
    }

    @Override
    public void run() {
      try {
        for (E arg : inputs) {
          doIteration(arg);
        }
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Implements the actual benchmark logic for an iteration.  Subclasses should override this method if they
     * don't override {@link #run()}.
     *
     * @param arg the next item from {@link #inputs}
     */
    protected void doIteration(E arg) throws Exception {
    }
  }
}
