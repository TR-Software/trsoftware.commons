package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.testing.StubScheduler;
import com.google.gwt.user.client.Command;
import solutions.trsoftware.commons.client.testutil.SimulatedException;
import solutions.trsoftware.commons.shared.BaseTestCase;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static java.util.Collections.*;
import static solutions.trsoftware.commons.shared.util.MapUtils.linkedHashMap;

/**
 * @author Alex
 * @since 8/24/2023
 */
public class IncrementalTaskQueueTest extends BaseTestCase {

  public void testAdd() throws Exception {
    TestableTaskQueue taskQueue = new TestableTaskQueue();
    StubScheduler scheduler = taskQueue.scheduler;
    assertFalse(taskQueue.isRunning());
    assertTrue(scheduler.getRepeatingCommands().isEmpty());

    ArrayList<Task> tasks = new ArrayList<>();
    ArrayList<Task> executedTasks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Task task = new Task() {
        @Override
        public void execute() {
          super.execute();
          executedTasks.add(this);
        }
      };
      tasks.add(task);
      taskQueue.add(task);
      assertTrue(taskQueue.isRunning());
      assertEquals(i+1, taskQueue.size());
      assertEquals(singletonList(taskQueue), scheduler.getRepeatingCommands());
    }
    assertEquals(tasks.size(), taskQueue.size());
    // run N-1 iterations of the incremental command
    for (int i = 0; i < tasks.size() - 1; i++) {
      assertEquals(i, executedTasks.size());
      Task task = tasks.get(i);
      assertFalse(task.isExecuted());
      // execute one iteration of the incremental command scheduled by our taskQueue
      assertTrue(scheduler.executeRepeatingCommands());  // should return true because incremental command not finished yet
      assertTrue(task.isExecuted());
      int nExecuted = i + 1;
      assertEquals(nExecuted, executedTasks.size());
      assertEquals(tasks.subList(0, nExecuted), executedTasks);
      assertEquals(taskQueue.size(), tasks.size() - nExecuted);
      assertTrue(taskQueue.isRunning());
    }
    // at this point, only 1 task should still be queued
    assertEquals(1, taskQueue.size());
    assertTrue(taskQueue.isRunning());
    // if we execute that, the taskQueue should no longer be scheduled as an incremental command
    assertFalse(scheduler.executeRepeatingCommands());  // the taskQueue should no longer be scheduled as a repeating command
    assertTrue(scheduler.getRepeatingCommands().isEmpty());
    // all tasks in the queue should have been executed
    assertFalse(taskQueue.isRunning());
    assertEquals(0, taskQueue.size());
    assertEquals(tasks, executedTasks);
    // if we add more tasks to the queue, it should once again schedule itself as an incremental command
    for (int i = 0; i < 10; i++) {
      Task task = new Task() {
        @Override
        public void execute() {
          super.execute();
          executedTasks.add(this);
        }
      };
      tasks.add(task);
      taskQueue.add(task);
      assertTrue(taskQueue.isRunning());
      assertEquals(i+1, taskQueue.size());
      assertEquals(singletonList(taskQueue), scheduler.getRepeatingCommands());
    }
  }

  public void testHandleFailedTask() throws Exception {
    LinkedHashMap<Task, Throwable> failedTasks = new LinkedHashMap<>();
    TestableTaskQueue taskQueue = new TestableTaskQueue() {
      @Override
      public boolean handleFailedTask(Task task, Throwable ex) {
        assertFalse(failedTasks.containsKey(task));
        failedTasks.put(task, ex);
        assertTrue(super.handleFailedTask(task, ex));  // the superclass impl of this method should always return true
        // but we actually return the custom value specified by the ThrowingTask instance
        return ((ThrowingTask)task).canContinue;
      }
    };
    StubScheduler scheduler = taskQueue.scheduler;
    Task throwingTask1 = new ThrowingTask(true);
    Task normalTask1 = new Task();
    taskQueue.add(throwingTask1).add(normalTask1);
    assertEquals(2, taskQueue.size());
    assertTrue(taskQueue.isRunning());
    assertEquals(singletonList(taskQueue), scheduler.getRepeatingCommands());

    // 1) execute throwingTask1: will throw an exception, which should be handled
    assertTrue(scheduler.executeRepeatingCommands());  // should still be scheduled, because handleFailedTask returned true
    assertEquals(
        singletonMap(throwingTask1, new SimulatedException(throwingTask1.getId())),
        failedTasks);
    assertEquals(1, taskQueue.size());
    assertTrue(taskQueue.isRunning());
    assertEquals(singletonList(taskQueue), scheduler.getRepeatingCommands());

    // add a couple more tasks such that the 2nd ThrowingTask will cause handleFailedTask to return false, thereby stopping the taskQueue
    Task throwingTask2 = new ThrowingTask(false);
    Task normalTask2 = new Task();
    taskQueue.add(throwingTask2).add(normalTask2);
    assertEquals(3, taskQueue.size());
    assertTrue(taskQueue.isRunning());
    assertEquals(singletonList(taskQueue), scheduler.getRepeatingCommands());

    // 2) execute normalTask1
    assertFalse(normalTask1.isExecuted());
    assertTrue(scheduler.executeRepeatingCommands());
    assertTrue(normalTask1.isExecuted());
    assertEquals(2, taskQueue.size());
    assertTrue(taskQueue.isRunning());
    assertEquals(singletonList(taskQueue), scheduler.getRepeatingCommands());

    // 3) execute throwingTask2: will cause handleFailedTask, thereby stopping the taskQueue
    assertFalse(scheduler.executeRepeatingCommands());  // should be cancelled, because handleFailedTask returned false
    assertEquals(emptyList(), scheduler.getRepeatingCommands());
    assertEquals(1, taskQueue.size());
    assertFalse(taskQueue.isRunning());
    assertEquals(linkedHashMap(
        throwingTask1, new SimulatedException(throwingTask1.getId()),
        throwingTask2, new SimulatedException(throwingTask2.getId())),
        failedTasks);

    // 4) normalTask2 should still be in the queue, despite the queue not currently running it will not be executed
    assertEquals(singletonList(normalTask2), taskQueue.examineTasks());
    // restart the queue
    assertTrue(taskQueue.startIfNotRunning());
    assertTrue(taskQueue.isRunning());
    assertEquals(singletonList(taskQueue), scheduler.getRepeatingCommands());
    assertEquals(1, taskQueue.size());
    assertFalse(normalTask2.isExecuted());
    // the next run should execute normalTask2, finishing the job
    assertFalse(scheduler.executeRepeatingCommands());  // should be stopped, because no more tasks remain
    assertEquals(emptyList(), scheduler.getRepeatingCommands());
    assertEquals(0, taskQueue.size());
    assertFalse(taskQueue.isRunning());
  }


  static class TestableTaskQueue extends IncrementalTaskQueue<Task> {
    StubScheduler scheduler = new StubScheduler();
    @Override
    Scheduler getScheduler() {
      return scheduler;
    }
  }

  static class Task implements Command {
    static int nextId;
    final int id = nextId++;
    private boolean executed;

    @Override
    public void execute() {
      executed = true;
    }

    public int getId() {
      return id;
    }

    public boolean isExecuted() {
      return executed;
    }
  }

  static class ThrowingTask extends Task {
    /**
     * The value to be returned by {@link IncrementalTaskQueue#handleFailedTask(Command, Throwable)} for this task.
     */
    final boolean canContinue;

    /**
     * @param canContinue The value to be returned by {@link IncrementalTaskQueue#handleFailedTask(Command, Throwable)} for this task.
     */
    ThrowingTask(boolean canContinue) {
      this.canContinue = canContinue;
    }

    @Override
    public void execute() {
      throw new SimulatedException(id);
    }
  }
}