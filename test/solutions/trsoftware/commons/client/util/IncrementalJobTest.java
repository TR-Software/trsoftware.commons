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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.TestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static solutions.trsoftware.commons.client.util.IncrementalJob.SingletonTask;
import static solutions.trsoftware.commons.client.util.IncrementalJob.Task;

/**
 * @author Alex
 * @since 5/27/2021
 */
public class IncrementalJobTest extends CommonsGwtTestCase {

  /**
   * A {@link Task} that doesn't do anything
   */
  public static final Task EMPTY_TASK = new Task() {
    @Override
    public boolean hasNext() {
      return false;
    }
    @Override
    public void next() {
    }
  };

  public void testIncrementalJob() throws Exception {
    // TODO: reduce timeout after finished debugging
    delayTestFinish(2000);
    ArrayList<Integer> numbers = new ArrayList<>();
    ArrayList<Long> factorials = new ArrayList<>();
    final int n = 20;  // 20! is the largest factorial that can be represented by a long integer

    TaskWrapper initNumbersTask = new TaskWrapper("initNumbersTask", new SingletonTask() {
      // executes a single iteration that fills the numbers list with ints 1..n, busy-waiting 4ms at the end
      @Override
      public void execute() {
        for (int i = 1; i <= n; i++) {
          numbers.add(i);
        }
        TestUtils.busyWait(4);
      }
    });
    TaskWrapper computeFactorialsTask = new TaskWrapper("computeFactorialsTask", new Task() {
      // executes n iterations, computing the factorials of each int in the numbers list, busy-waiting 1ms after each iteration
      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < numbers.size();
      }

      @Override
      public void next() {
        factorials.add(MathUtils.factorial(numbers.get(i++)));
        TestUtils.busyWait(1);
      }
    });
    TaskWrapper emptyTask = new TaskWrapper("emptyTask", EMPTY_TASK);  // this task should be skipped
    TaskWrapper printResultsTask = new TaskWrapper("printResultsTask", new SingletonTask() {
      // executes a single iteration that prints the two lists generated above
      @Override
      public void execute() {
        getLogger().info("numbers    = " + numbers);
        getLogger().info("factorials = " + factorials);
      }
    });
    List<Task> tasks = Arrays.asList(
        initNumbersTask,
        computeFactorialsTask,
        emptyTask,
        printResultsTask
    );

    IncrementalJob job = new IncrementalJob(5, tasks) {
      private Duration duration;

      @Override
      protected void jobStarted() {
        super.jobStarted();
        duration = new Duration();
      }

      @Override
      protected void jobFinished(boolean interrupted) {
        getLogger().info(Strings.lenientFormat("Job finished processing %s iterations in %s ms;\n  increment durations: %s\n  wrappers: %s",
            getIterationCount(),
            duration.elapsedMillis(),
            getIncrementDurations().summarize(),
            tasks));
        // check that the job computed the desired result
        assertEquals(n, numbers.size());
        assertEquals(n, factorials.size());
        // check the following assertions:
        // 1) the job and all tasks have finished
        assertTrue(isFinished());
        assertFalse(hasMoreWork());
        for (Task task : tasks) {
          // NOTE: we invoke the hasNext method on the wrapped object, in order to not mess with hasNextCount
          TaskWrapper wrapper = (TaskWrapper)task;
          assertFalse(wrapper.task.hasNext());
        }
        // 2) the job didn't do any more work than needed:
        //   a) only the expected number of iterations were executed for each task (i.e. 1 for each singleton task + n to compute the factorials)
        assertEquals(2 + n, getIterationCount());
        assertEquals(1, initNumbersTask.nextCount);
        assertEquals(n, computeFactorialsTask.nextCount);
        assertEquals(0, emptyTask.nextCount);
        assertEquals(1, printResultsTask.nextCount);
        //   b) the number of hasNext() invocations of each task should be at most 1 greater than the number of next() invocations
        for (Task task : tasks) {
          // NOTE: we invoke the hasNext method on the wrapped object, in order to not mess with hasNextCount
          TaskWrapper wrapper = (TaskWrapper)task;
          AssertUtils.assertThat(wrapper.hasNextCount).isLessThanOrEqualTo(wrapper.nextCount + 1);
        }
        finishTest();
      }
    };
    assertFalse(job.isStarted());
    assertFalse(job.isFinished());
    assertTrue(job.hasMoreWork());
    assertFalse(job.isStopped());

    Scheduler.get().scheduleIncremental(job);
  }


  /**
   * Wraps a {@link Task} in order to count the number of times its methods have been invoked
   */
  private static class TaskWrapper implements Task {
    private final String name;
    private final Task task;
    /**
     * Number of times {@link #hasNext()} was invoked.
     */
    private int hasNextCount;
    /**
     * Number of times {@link #next()} was invoked.
     */
    private int nextCount;

    public TaskWrapper(String name, Task task) {
      this.name = name;
      this.task = task;
    }

    @Override
    public boolean hasNext() {
      hasNextCount++;
      return task.hasNext();
    }

    @Override
    public void next() {
      nextCount++;
      task.next();
    }

    public Task getTask() {
      return task;
    }

    public int getHasNextCount() {
      return hasNextCount;
    }

    public int getNextCount() {
      return nextCount;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("hasNextCount", hasNextCount)
          .add("nextCount", nextCount)
          .toString();
    }
  }

}