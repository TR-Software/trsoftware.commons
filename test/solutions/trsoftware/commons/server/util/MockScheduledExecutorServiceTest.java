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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.TestCaseCanStopClock;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Apr 8, 2009
 *
 * @author Alex
 */
public class MockScheduledExecutorServiceTest extends TestCaseCanStopClock {
  private MockScheduledExecutorService scheduler;

  protected void setUp() throws Exception {
    super.setUp();
    scheduler = new MockScheduledExecutorService();
  }

  @Override
  public void tearDown() throws Exception {
    scheduler = null;
    super.tearDown();
  }

  public void testSchedule() throws Exception {
    // using a nested single-threaded executor for testing so we can check the deterministic execution order
    Clock.stop();

    // test basic scheduling
    {
      DummyTask task1 = new DummyTask();
      scheduler.schedule(task1, 5, TimeUnit.SECONDS);
      // make sure the task doesn't get executed until it's time
      assertFalse(task1.wasRun());
      scheduler.advanceClockAndPump(1000);
      assertFalse(task1.wasRun());
      scheduler.advanceClockAndPump(3999);
      assertFalse(task1.wasRun());
      scheduler.advanceClockAndPump(1);  // this should put it over the tipping point
      assertTrue(task1.wasRun());
      assertEquals(1, task1.getRunCount());
    }

    // test future control (that the task can be cancelled)
    {
      DummyTask task1 = new DummyTask();
      ScheduledFuture scheduledFuture1 = scheduler.schedule(task1, 5, TimeUnit.SECONDS);
      // make sure the task doesn't get executed until it's time
      assertFalse(task1.wasRun());
      assertFalse(scheduledFuture1.isDone());
      assertFalse(scheduledFuture1.isCancelled());
      scheduler.advanceClockAndPump(1000);
      assertFalse(task1.wasRun());
      assertFalse(scheduledFuture1.isDone());
      assertFalse(scheduledFuture1.isCancelled());
      // make sure we're now 4 seconds away from execution
      assertEquals(4, scheduledFuture1.getDelay(TimeUnit.SECONDS));

      // try to cancel the task before it has been run
      assertEquals(1, scheduler.getScheduledTaskCount()); // we have one task scheduled
      assertTrue(scheduledFuture1.cancel(false));
      assertEquals(0, scheduler.getScheduledTaskCount()); // the task is no longer scheduled

      assertFalse(scheduledFuture1.isDone());
      assertTrue(scheduledFuture1.isCancelled());

      // make sure the task never gets run, even after its time has come and gone
      scheduler.advanceClockAndPump(5000); // this should have put it over the tipping point
      assertFalse(task1.wasRun());
      assertEquals(0, task1.getRunCount());
    }

    // now schedule two tasks simultaneously (make sure one isn't replaced with the other)
    {
      DummyTask task1 = new DummyTask();
      DummyTask task2 = new DummyTask();
      scheduler.schedule(task1, 5, TimeUnit.SECONDS);
      scheduler.schedule(task2, 5, TimeUnit.SECONDS);
      // make sure the tasks don't get executed until it's time
      assertFalse(task1.wasRun());
      assertFalse(task2.wasRun());
      scheduler.advanceClockAndPump(1000);
      assertFalse(task1.wasRun());
      assertFalse(task2.wasRun());
      scheduler.advanceClockAndPump(3999);
      assertFalse(task1.wasRun());
      assertFalse(task2.wasRun());
      scheduler.advanceClockAndPump(1);  // this should put it over the tipping point
      assertTrue(task1.wasRun());
      assertEquals(1, task1.getRunCount());
      assertTrue(task2.wasRun());
      assertEquals(1, task2.getRunCount());
    }

    // now schedule two tasks a small unit of time apart (make sure the execution order reflects the small time difference)
    {
      DummyTask task1 = new DummyTask();
      DummyTask task2 = new DummyTask();
      scheduler.schedule(task1, 5001, TimeUnit.MILLISECONDS);
      scheduler.schedule(task2, 5000, TimeUnit.MILLISECONDS);
      // make sure the tasks don't get executed until it's time
      assertFalse(task1.wasRun());
      assertFalse(task2.wasRun());
      scheduler.advanceClockAndPump(1000);
      assertFalse(task1.wasRun());
      assertFalse(task2.wasRun());
      scheduler.advanceClockAndPump(4001); // this should put both tasks over the tipping point
      assertTrue(task1.wasRun());
      assertEquals(1, task1.getRunCount());
      assertTrue(task2.wasRun());
      assertEquals(1, task2.getRunCount());
      // make sure task 2 got run before task 1
      assertTrue(task2.getLastRunSeqNum() < task1.getLastRunSeqNum());
    }
  }

  public void testScheduleAtFixedRate() throws Exception {
    Clock.stop();
    assertEquals(0, scheduler.getScheduledTaskCount());
    // 1) test basic scheduling
    DummyTask task1 = new DummyTask() {
      @Override
      public void run() {
        super.run();
        System.out.println(toString() + " ran on " + new Date(Clock.currentTimeMillis()) + " (artificial time)");
      }
    };
    // schedule the task to run 5 seconds from now and then repeat every 20 seconds
    ScheduledFuture<?> task1Future = scheduler.scheduleAtFixedRate(task1, 5, 20, TimeUnit.SECONDS);
    assertEquals(1, scheduler.getScheduledTaskCount());
    System.out.println(task1.toString() + " scheduled on " + new Date(Clock.currentTimeMillis()) + " (artificial time)");
    // make sure the task doesn't get executed until it's time
    assertFalse(task1.wasRun());
    scheduler.advanceClockAndPump(1000);
    assertFalse(task1.wasRun());
    scheduler.advanceClockAndPump(3999);
    assertFalse(task1.wasRun());
    scheduler.advanceClockAndPump(1);  // this should put it over the tipping point
    assertTrue(task1.wasRun());
    assertEquals(1, scheduler.getScheduledTaskCount()); // the task should have rescheduled itself
    assertEquals(1, task1.getRunCount());
    // now make sure this task runs every 20 seconds (check 10 iterations of the task)
    for (int i = 1; i <= 10; i++) {
      System.out.println(scheduler.dumpTasks());
      scheduler.advanceClockAndPumpIncrementally(15000);
      assertEquals(i, task1.getRunCount());  // shouldn't have executed yet
      scheduler.advanceClockAndPumpIncrementally(5000);  // another 5 seconds will put over the edge
      assertEquals(i + 1, task1.getRunCount());
    }

    // 2) test future control (that the task can be cancelled)
    assertEquals(1, scheduler.getScheduledTaskCount()); // the task is still scheduled
    int lastRunCount = task1.getRunCount();
    task1Future.cancel(false);
    assertEquals(0, scheduler.getScheduledTaskCount()); // the task is no longer scheduled
    scheduler.advanceClockAndPumpIncrementally(25000);
    assertEquals(lastRunCount, task1.getRunCount()); // the task has not been run again, even after it's scheduled time
  }


  public void testAdvanceClockAndPumpIncrementally() throws Exception {
    long startTime = Clock.stop();
    // schedule a bunch of tasks at 1 millis intervals, and make sure the tasks
    // will be executed exactly at their scheduled time
    DummyTask[] tasks = new DummyTask[64];  // 64 millis is the max interval on which the step size is 1ms
    for (int i = 0; i < tasks.length; i++) {
      DummyTask task = new DummyTask();
      tasks[i] = task;
      scheduler.schedule(task, i, TimeUnit.MILLISECONDS);
    }
    scheduler.advanceClockAndPumpIncrementally(64);
    // make sure the clock stopped at the right time
    assertEquals(startTime + 64, Clock.currentTimeMillis());
    // make sure all the tasks have been executed at their scheduled time
    assertAllTasksExecutedOnTime(startTime, tasks);
  }

  private void assertAllTasksExecutedOnTime(long startTime, DummyTask[] tasks) {
    for (int i = 0; i < tasks.length; i++) {
      DummyTask task = tasks[i];
      assertEquals(1, task.getRunCount());
      if (i == 0)  // special case - the advanceClockAndPumpIncrementally method will execute a task with delay 0 one millisecond late
        assertEquals(startTime + 1, task.getLastRunTime());
      else
        assertEquals(startTime + i, task.getLastRunTime());
    }
  }

  public void testAdvanceClockAndPumpIncrementallyTo() throws Exception {
    Clock.stop();
    long startTime = 0;
    Clock.set(startTime);
    // shedule a bunch of tasks at 1 millis intervals, and make sure the tasks
    // will be executed exactly at their scheduled time
    DummyTask[] tasks = new DummyTask[64];  // 64 millis is the max interval on which the step size is 1ms
    for (int i = 0; i < tasks.length; i++) {
      DummyTask task = new DummyTask();
      tasks[i] = task;
      scheduler.schedule(task, i, TimeUnit.MILLISECONDS);
    }
    scheduler.advanceClockAndPumpIncrementallyTo(64);
    // make sure the clock stopped at the right time
    assertEquals(startTime + 64, Clock.currentTimeMillis());
    // make sure all the tasks have been executed at their scheduled time
    assertAllTasksExecutedOnTime(startTime, tasks);
  }

  /** A block of code that remembers whether and when it was run */
  private static class DummyTask implements Runnable {
    private static final AtomicLong nextSeqNum = new AtomicLong(0);

    private AtomicInteger runCount = new AtomicInteger(0);
    private AtomicLong lastRunTime = new AtomicLong(0);
    /**
     * Used to resolve the run order of tasks (i.e. whether one was run before
     * the other)
     */
    private AtomicLong lastRunSeqNum = new AtomicLong(-1);

    public void run() {
      lastRunSeqNum.set(nextSeqNum.getAndIncrement());
      runCount.incrementAndGet();
      lastRunTime.set(Clock.currentTimeMillis());
    }

    public long getLastRunTime() {
      return lastRunTime.get();
    }

    public int getRunCount() {
      return runCount.get();
    }

    public boolean wasRun() {
      return runCount.get() > 0;
    }

    public long getLastRunSeqNum() {
      if (wasRun())
        return lastRunSeqNum.get();
      throw new IllegalStateException("Cannont get run sequence number before task was run");
    }

    @Override
    public String toString() {
      return super.toString().replace(getClass().getName(), "DummyTask");
    }
  }
}