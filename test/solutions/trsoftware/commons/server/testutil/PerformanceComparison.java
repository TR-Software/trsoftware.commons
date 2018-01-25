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

package solutions.trsoftware.commons.server.testutil;

import java.util.concurrent.TimeUnit;

import static solutions.trsoftware.commons.shared.util.StringUtils.firstNotBlank;

/**
 * Compares the performance of a number of iterations of each Runnable task specified.
 */
public abstract class PerformanceComparison {

  /**
   * @return the multiplier of running time of {@code task1} compared to {@code task2}
   * (i.e. how many times is task1 faster than task2.
   */
  public static double compare(Runnable task1, String task1Name, Runnable task2, String task2Name, int iterations) {
    // warm up both tasks
    runTask(task1, iterations);
    runTask(task2, iterations);

    // interleave the tasks (so that neither gets the advantage or disadvantage of going first)
    long task1Ns = runTask(task1, iterations);
    long task2Ns = runTask(task2, iterations);

    // will have to subtract the overhead of running an empty task for that number of iterations
    long emptyTaskNs = runTask(new Runnable() {
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
    System.out.printf("%s is %s times %s than %s (%,d ns versus %,d ns)%n", task1Name, multiplier, label, task2Name, task1Ns, task2Ns);
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

  /** Returns the nanos elapsed from running task for the given number of iterations */
  private static long runTask(Runnable task, int iterations) {
    long start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      task.run();
    }
    long end = System.nanoTime();
    return end - start;
  }

}
