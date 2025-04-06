package solutions.trsoftware.commons.server.testutil;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.server.testutil.MockScheduledExecutorService.ScheduledFutureTask;
import solutions.trsoftware.commons.server.testutil.MockScheduledExecutorService.TaskRunRecord;
import solutions.trsoftware.commons.server.testutil.MockScheduledExecutorService.TaskState;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;
import solutions.trsoftware.commons.shared.util.reflect.ClassNameParser;
import solutions.trsoftware.commons.shared.util.time.FakeTicker;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;

/**
 * @author Alex
 * @since 12/14/2022
 */
public class MockScheduledExecutorServiceTest extends BaseTestCase {

  private MockScheduledExecutorService scheduler;
  private FakeTicker ticker;
  private long startTime;
  private int nextTaskId, nextTaskRunId;
  private List<TaskRunRecord> expectedHistory;

  public void setUp() throws Exception {
    super.setUp();
    ticker = new FakeTicker();
    startTime = ticker.read();
    scheduler = new MockScheduledExecutorService(getName(), ticker);
    expectedHistory = new ArrayList<>();
  }

  public void tearDown() throws Exception {
    if (!expectedHistory.isEmpty())
      assertListsEqual(expectedHistory, scheduler.getHistory());
    scheduler = null;
    ticker = null;
    expectedHistory = null;
    super.tearDown();
  }

  public void testExecute() throws Exception {
    DummyRunnable command = new DummyRunnable();
    scheduler.execute(command);
    assertFalse(command.wasRun());
    printDebugInfo();
    TaskRunRecord runRecord = getOnlyElement(scheduler.runTasks());
    assertTrue(command.wasRun());
    printDebugInfo();
    System.out.println(runRecord);
    verifyRunRecord(runRecord, command, "execute", TaskState.NORMAL);
  }

  @SuppressWarnings("unchecked")
  public void testSubmit() throws Exception {
    // submit several tasks at once
    DummyTask[] tasks = new DummyTask[4];
    Future<?>[] futures = new Future[tasks.length];

    futures[0] = scheduler.submit((DummyCallable<Boolean>)(tasks[0] = new DummyCallable<Boolean>("Cancel next task", null) {
      @Override
      protected Boolean doCall() {
        // cancel the next task
        return futures[1].cancel(false);
      }
    }));
    futures[1] = scheduler.submit((Runnable)(tasks[1] = new DummyRunnable()));
    futures[2] = scheduler.submit((DummyCallable<Integer>)(tasks[2] = new DummyCallable<Integer>("Throw an exception", null) {
      @Override
      protected Integer doCall() throws Exception {
        throw new SimulatedException(2);
      }
    }));
    futures[3] = scheduler.submit((Runnable)(tasks[3] = new DummyRunnable()), "Done");


    // verify scheduler state after tasks submitted but before any executed
    printDebugInfo();
    for (int i = 0; i < tasks.length; i++) {
      DummyTask task = tasks[i];
      Future<?> future = futures[i];
      assertFalse(task.wasRun());
      assertFalse(future.isDone());
      assertFalse(future.isCancelled());
    }

    // execute the tasks and verify the post-conditions
    List<TaskRunRecord> runRecords = scheduler.runTasks();
    printDebugInfo();
    // only 3 of the submitted tasks should've been run (task[1] should've been cancelled)
    assertEquals(tasks.length-1, runRecords.size());

    // first task should've run successfully and cancelled the next task
    {
      assertTrue(tasks[0].wasRun());
      assertCompleted((Future<Boolean>)futures[0], true);
      verifyRunRecord(runRecords.get(0), tasks[0], "submit", TaskState.NORMAL, true);
    }
    // 2nd task should've been cancelled and never run
    {
      assertFalse(tasks[1].wasRun());
      assertCancelled(futures[1]);
      // NOTE: this task has no entry in runRecords because it never ran
    }
    // 3rd task should've thrown an exception
    {
      assertTrue(tasks[2].wasRun());
      assertExceptional(futures[2], SimulatedException.class);
      TaskRunRecord record = runRecords.get(1); // the run record index of this task is 1 b/c the previous task never ran
      verifyRunRecord(record, tasks[2], "submit", TaskState.EXCEPTIONAL, new SimulatedException(2));
    }
    // 4th task should've completed normally
    {
      assertTrue(tasks[3].wasRun());
      assertCompleted((Future<String>)futures[3], "Done");
      TaskRunRecord record = runRecords.get(2);
      verifyRunRecord(record, tasks[3], "submit", TaskState.NORMAL, "Done");
    }
  }

  @SuppressWarnings("unchecked")
  public void testSchedule() throws Exception {
    DummyTask[] tasks = new DummyTask[6];
    ScheduledFuture<?>[] futures = new ScheduledFuture[tasks.length];
    long[] delays = new long[tasks.length];
    int delayIncrement = 100;
    TimeUnit timeUnit = NANOSECONDS;

    // schedule some tasks
    {
      int i = 0;
      int nextDelay = delayIncrement;
      futures[i] = scheduler.schedule((DummyCallable<Boolean>)(tasks[i] = new DummyCallable<Boolean>("Cancel next task", null) {
        @Override
        protected Boolean doCall() {
          // cancel the next task
          return futures[1].cancel(false);
        }
      }), delays[i] = nextDelay, timeUnit);

      i++;
      nextDelay+= delayIncrement;
      futures[i] = scheduler.schedule((Runnable)(tasks[i] = new DummyRunnable()), delays[i] = nextDelay, timeUnit);

      // next 2 tasks will be scheduled for the same time
      i++;
      nextDelay+= delayIncrement;
      futures[i] = scheduler.schedule((DummyCallable<Integer>)(tasks[i] = new DummyCallable<Integer>("Throw an exception", null) {
        @Override
        protected Integer doCall() throws Exception {
          throw new SimulatedException(2);
        }
      }), delays[i] = nextDelay, timeUnit);
      i++;
      futures[i] = scheduler.schedule((Runnable)(tasks[i] = new DummyRunnable()), delays[i] = nextDelay, timeUnit);

      // next 2 tasks will be used to test Future.get()

      while (i < tasks.length-1) {
        i++;
        nextDelay+= delayIncrement;
        futures[i] = scheduler.schedule((DummyCallable<Long>)(tasks[i] = new DummyCallable<Long>("Return current time", null) {
          @Override
          protected Long doCall() throws Exception {
            return ticker.read();
          }
        }), delays[i] = nextDelay, timeUnit);
      }
    }
    printDebugInfo();

    String methodName = "schedule";
    // start executing the tasks
    {
      // advance time to that of 2nd task (which would've been cancelled by the first)
      long targetTime = delays[1];
      List<TaskRunRecord> runRecords = scheduler.advanceTo(targetTime);
      // only the 1st task should've been executed
      TaskRunRecord record = getOnlyElement(runRecords);
      assertTrue(tasks[0].wasRun());
      assertCompleted((Future<Boolean>)futures[0], true);
      verifyRunRecord(record, tasks[0], methodName, TaskState.NORMAL, true);
      // 2nd task should've been cancelled and never run
      assertFalse(tasks[1].wasRun());
      assertCancelled(futures[1]);
      assertEquals(targetTime, ticker.read());
      System.out.printf("Ran %d %s after advanceTo(%d)%n", runRecords.size(), StringUtils.pluralize("task", runRecords.size()), targetTime);
      printDebugInfo();
    }

    {
      // the next 2 tasks are scheduled for the same time, so should both run for
      List<TaskRunRecord> runRecords = scheduler.advanceToNext();
      assertEquals(2, runRecords.size());
      assertTrue(tasks[2].wasRun());
      assertExceptional(futures[2], SimulatedException.class);
      verifyRunRecord(runRecords.get(0), tasks[2], methodName, TaskState.EXCEPTIONAL);
      assertTrue(tasks[3].wasRun());
      assertCompleted(futures[3], null);
      verifyRunRecord(runRecords.get(1), tasks[3], methodName, TaskState.NORMAL);
      assertEquals(delays[2], delays[3]);
      assertEquals(delays[3], ticker.read());
      System.out.printf("Ran %d %s after advanceToNext%n", runRecords.size(), StringUtils.pluralize("task", runRecords.size()));
      printDebugInfo();
    }

    // the next 2 tasks will be completed by invoking Future.get, which should advance the ticker as needed
    {
      assertEquals(2, scheduler.getScheduledTaskCount());
      int i = 4;
      ScheduledFuture<Long> future = (ScheduledFuture<Long>)futures[i];
      assertFutureNotDone(future);
      assertEquals(delays[i], (long)future.get());
      assertCompleted(future, delays[i]);
      assertEquals(delays[i], ticker.read());
      System.out.printf("Ran %s via Future.get()%n", tasks[i]);
      printDebugInfo();
    }
    {
      assertEquals(1, scheduler.getScheduledTaskCount());
      int i = 5;
      long now = ticker.read();
      int timeout = delayIncrement / 2;
      ScheduledFuture<Long> future = (ScheduledFuture<Long>)futures[i];
      assertFutureNotDone(future);
      assertThrows(TimeoutException.class, (Function0_t)() -> future.get(timeout, timeUnit));
      assertFutureNotDone(future);
      // the above call to Future.get should've advanced the time up to the specified timeout
      assertEquals(now + timeout, ticker.read());
      // the next call to Future.get should complete the task
      assertEquals(delays[i], (long)future.get(timeout, timeUnit));
      assertCompleted(future, delays[i]);
      assertEquals(delays[i], ticker.read());
      System.out.printf("Ran %s via 2 invocations of Future.get(%d, %s)%n", tasks[i], timeout, timeUnit);
      printDebugInfo();
    }
  }

  public void testScheduleAtFixedRate() throws Exception {
    RepeatingTaskSpec<SlowRunnable> task0Spec, task1Spec;
    /*
     NOTE: we're using SlowRunnable tasks to verify fixed-rate execution:
           i.e. that the time to execute an iteration of the task doesn't affect the trigger time of the next iteration
    */
    List<RepeatingTaskSpec<SlowRunnable>> taskSpecs = scheduleRepeatingTasks(RepeatingTaskSpec::scheduleAtFixedRate,
        task0Spec = new RepeatingTaskSpec<>(2, 1, MILLISECONDS,
            new SlowRunnable(500, "Repeat every 1 ms after 2 ms")),
        task1Spec = new RepeatingTaskSpec<>(2000, 1000, NANOSECONDS,
            new SlowRunnable(50, "Repeat every 1000 ns after 2000 ns"))
    );
    for (RepeatingTaskSpec<SlowRunnable> spec : taskSpecs) {
      assertEquals(spec.getInitialDelayNanos(), spec.getFixedRateTriggerTime(0));
    }

    // run the nano task 4 times
    int task1RunCount;
    long now;
    {
      int n = task1RunCount = 4; // expected task1RunCount
      List<TaskRunRecord> runRecords = scheduler.runTasksUntil(() -> task1Spec.task.runCount == n);
      expectedHistory.addAll(runRecords);
      printDebugInfo();
      assertEquals(n, runRecords.size());
      assertEquals(n, task1Spec.task.runCount);
      for (int j = 0; j < runRecords.size(); j++) {
        TaskRunRecord record = runRecords.get(j);
        long expectedTime = task1Spec.getFixedRateTriggerTime(j);
        verifyRunRecord(record, task1Spec, TaskState.NEW, expectedTime, j+1);
      }
      assertEquals(
          task1Spec.getInitialDelayNanos() + task1Spec.getPeriodNanos() * (n - 1) + task1Spec.task.getRunDurationNanos(),
          // adding task.runDuration only for the last iteration that ran
          now = ticker.read());
      System.out.printf("Ran %s %d times%n", task1Spec.task, n);
      printDebugInfo();

      assertFutureNotDone(task1Spec.future);
      assertEquals(task1Spec.getFixedRateTriggerTime(n) - now, task1Spec.future.getDelay(NANOSECONDS));
    }
    // advance to the 1st iteration of the millis task
    {
      long task0Time = task0Spec.getInitialDelayNanos();
      assertEquals(task0Time, task0Spec.getFixedRateTriggerTime(0));
      List<TaskRunRecord> runRecords = scheduler.advanceTo(task0Time);
      expectedHistory.addAll(runRecords);
      printDebugInfo();
      assertEquals(1, task0Spec.task.runCount);
      // figure out how many times the nano task should've run in that time span
      int task1ExpectedRunCount = (int)((task0Time - task1Spec.getInitialDelayNanos()) / task1Spec.getPeriodNanos()) + 1;
      assertEquals(task1ExpectedRunCount, task1Spec.task.runCount);
      // the leading n-1 run records should be for the nano task
      int n = task1RunCount;  // number of runs already executed beforehand
      List<TaskRunRecord> expectedTask1Records = runRecords.subList(0, task1ExpectedRunCount - n - 1);
      for (int i = 0; i < expectedTask1Records.size(); i++) {
        TaskRunRecord task1Record = expectedTask1Records.get(i);
        verifyRunRecord(task1Record, task1Spec, TaskState.NEW,
            task1Spec.getFixedRateTriggerTime(n+i), n+i+1);
      }
      /*
      The next 2 run records should be for the 1st iteration of the millis task followed by the final iteration of the nano task
      (our executor uses the task's sequence number as the tie-breaker between tasks scheduled for the same time,
      so we expect task0, which was submitted earlier, to always run before task1 whenever an iteration of each
      is scheduled for the same time)
       */
      List<TaskRunRecord> last2Records = runRecords.subList(expectedTask1Records.size(), runRecords.size());
      assertEquals(2, last2Records.size());
      verifyRunRecord(last2Records.get(0), task0Spec, TaskState.NEW, task0Time, 1);
      verifyRunRecord(last2Records.get(1), task1Spec, TaskState.NEW, task0Time + task0Spec.task.runDuration, task1ExpectedRunCount);
    }

    now = ticker.read();
    // test cancellation of these periodic tasks
    {
      // cancel task1 now
      assertEquals(2, scheduler.getScheduledTaskCount());
      expectedHistory.add(cancelPeriodicTask(task1Spec));
      assertEquals(1, scheduler.getScheduledTaskCount());
    }
    {
      // run task0 for 2 more iterations and then cancel it as well
      long task0PeriodNanos = task0Spec.getPeriodNanos();
      long delay = task0PeriodNanos * 2 + task0PeriodNanos / 2;
      int runCount = task0Spec.task.runCount;
      List<TaskRunRecord> runRecords = scheduler.advance(delay);
      expectedHistory.addAll(runRecords);
      printDebugInfo();
      // task0 should've ran twice more as a result
      assertEquals(runCount + 2, task0Spec.task.runCount);
      assertEquals(2, runRecords.size());
      assertEquals(now + delay, now = ticker.read());
      for (int i = 0; i < runRecords.size(); i++) {
        TaskRunRecord record = runRecords.get(i);
        int iteration = 1 + i;
        verifyRunRecord(record, task0Spec, TaskState.NEW, task0Spec.getFixedRateTriggerTime(iteration), iteration+1);
      }

      expectedHistory.add(cancelPeriodicTask(task0Spec));
      assertEquals(0, scheduler.getScheduledTaskCount());
    }
    // nothing should be executed from this point on, since all tasks cancelled
    {
      assertEquals(0, scheduler.advanceToNext().size());
      // shouldn't have executed anything or changed the current time
      assertEquals(now, ticker.read());

      assertEquals(0, scheduler.advance(5, SECONDS).size());
      // should've advanced the current time but not executed anything
      assertEquals(now + SECONDS.toNanos(5), ticker.read());
    }
  }

  public void testScheduleWithFixedDelay() throws Exception {
    String methodName = "scheduleWithFixedDelay";
    RepeatingTaskSpec<SlowRunnable> task0Spec, task1Spec;
     /*
      NOTE: we're using SlowRunnable tasks to verify fixed-delay execution:
            i.e. that the time to execute an iteration affects the trigger time of the next iteration
     */
    List<RepeatingTaskSpec<SlowRunnable>> taskSpecs = scheduleRepeatingTasks(RepeatingTaskSpec::scheduleWithFixedDelay,
        task0Spec = new RepeatingTaskSpec<>(2, 1, MILLISECONDS,
            new SlowRunnable(500, "Repeat with delay 1 ms after 2 ms")),
        task1Spec = new RepeatingTaskSpec<>(2000, 1000, NANOSECONDS,
            new SlowRunnable(50, "Repeat with delay 1000 ns after 2000 ns"))
    );
    for (RepeatingTaskSpec<SlowRunnable> spec : taskSpecs) {
      assertEquals(spec.getInitialDelayNanos(), spec.getFixedDelayTriggerTime(0));
    }

    // run the nano task 4 times
    int task1RunCount;
    long now;
    {
      int n = task1RunCount = 4; // expected task1RunCount
      List<TaskRunRecord> runRecords = scheduler.runTasksUntil(() -> task1Spec.task.runCount == n);
      expectedHistory.addAll(runRecords);
      printDebugInfo();
      assertEquals(n, runRecords.size());
      assertEquals(n, task1Spec.task.runCount);
      for (int j = 0; j < runRecords.size(); j++) {
        TaskRunRecord record = runRecords.get(j);
        long expectedTime = task1Spec.getFixedDelayTriggerTime(j);
        verifyRunRecord(record, task1Spec, TaskState.NEW, expectedTime, j+1);
      }
      long runDuration = task1Spec.task.getRunDurationNanos();
      assertEquals(
          task1Spec.getInitialDelayNanos() + runDuration + (task1Spec.getPeriodNanos() + runDuration) * (n - 1),
          // adding task.runDuration only for the last iteration that ran
          now = ticker.read());

      assertFutureNotDone(task1Spec.future);
      assertEquals(task1Spec.getFixedDelayTriggerTime(n) - now, task1Spec.future.getDelay(NANOSECONDS));
    }
    // advance to the 1st iteration of the millis task
    {
      long task0Time = task0Spec.getInitialDelayNanos();
      assertEquals(task0Time, task0Spec.getFixedDelayTriggerTime(0));
      List<TaskRunRecord> runRecords = scheduler.advanceTo(task0Time);
      expectedHistory.addAll(runRecords);
      printDebugInfo();
      assertEquals(1, task0Spec.task.runCount);
      // figure out how many times the nano task should've run in that time span
      int task1ExpectedRunCount = (int)(
          (task0Time - task1Spec.getInitialDelayNanos()) / (task1Spec.getPeriodNanos() + task1Spec.task.getRunDurationNanos()))  + 1;
      int n = task1RunCount;  // number of runs already executed beforehand
      assertEquals(task1ExpectedRunCount, task1RunCount = task1Spec.task.runCount);
      int task1ExpectedRunRecords = task1ExpectedRunCount - n;
      assertEquals(task1ExpectedRunRecords + 1, runRecords.size());
      // the first task1ExpectedRunRecords run records should be for the nano task
      for (int i = 0; i < task1ExpectedRunRecords; i++) {
        TaskRunRecord task1Record = runRecords.get(i);
        verifyRunRecord(task1Record, task1Spec, TaskState.NEW,
            task1Spec.getFixedDelayTriggerTime(n+i), n+i+1);
      }
      // the last run record should be for the 1st iteration of the millis task;
      verifyRunRecord(ListUtils.last(runRecords), task0Spec, TaskState.NEW, task0Time, 1);
      assertEquals(task0Time + task0Spec.task.getRunDurationNanos(), now = ticker.read());
    }
    // test Future.get, which can never return normally: only with a cancellation or execution exception
    {
      // 1) if we call Future.get with a timeout, it should throw a TimeoutException
      /* Note: Computing the next trigger time of task1 requires special consideration,
               since task0 might have advanced the clock past the point when task1 was scheduled to run next.
       */
      long task1NextTriggerTime = Math.max(task1Spec.getFixedDelayTriggerTime(task1RunCount), now);
      assertThrows(TimeoutException.class, (Function0_t<Throwable>)() -> task1Spec.future.get(task1Spec.period, task1Spec.unit));
      printDebugInfo();
      assertEquals(now + task1Spec.getPeriodNanos(), now = ticker.read());
      // the above call to Future.get should have executed an additional iteration of the task before timing out
      assertEquals(task1RunCount + 1, task1RunCount = task1Spec.task.runCount);
      assertEquals(task1NextTriggerTime, task1Spec.task.lastRunTime);
      List<TaskRunRecord> updatedHistory = scheduler.getHistory();
      assertEquals(expectedHistory.size() + 1, updatedHistory.size());
      TaskRunRecord lastRecord = ListUtils.last(updatedHistory);
      verifyRunRecord(lastRecord, task1Spec, TaskState.NEW, task1NextTriggerTime, task1RunCount);
      expectedHistory.add(lastRecord);
    }
    {
      // 2) calling Future.get without a timeout will result in an infinite loop unless the task is cancelled or throws an exception
      //    so we'll schedule another task that cancels task1 just before its next run
      DummyCallable<Boolean> cancelTask1 = new DummyCallable<Boolean>("Cancel task " + task1Spec.task.id, null) {
        @Override
        protected Boolean doCall() {
          // cancel the next task
          return task1Spec.future.cancel(false);
        }
      };
      long cancellationDelay = task1Spec.future.getDelay(NANOSECONDS) - 1;
      ScheduledFuture<Boolean> cancelTask1Future = scheduler.schedule(
          cancelTask1, cancellationDelay, NANOSECONDS);
      assertEquals(cancellationDelay, cancelTask1Future.getDelay(NANOSECONDS));
      long expectedCancellationTime = now + cancellationDelay;
      printDebugInfo();

      // now try calling Future.get without a timeout
      assertThrows(CancellationException.class, (Function0_t<Throwable>)() -> task1Spec.future.get());
      printDebugInfo();
      List<TaskRunRecord> newRecords = getNewRunRecords();
      assertEquals(2, newRecords.size());
      verifyRunRecord(newRecords.get(0), task1Spec, TaskState.CANCELLED, expectedCancellationTime, task1RunCount);
      verifyRunRecord(newRecords.get(1), cancelTask1, "schedule", TaskState.NORMAL, expectedCancellationTime, 1);
      assertCompleted(cancelTask1Future, true);
      assertCancelled(task1Spec.future);
      expectedHistory.addAll(newRecords);
      assertEquals(expectedCancellationTime, now = ticker.read());
    }
    {
      // schedule another a task to make the millis task throw an exception after 2 more iterations
      DummyRunnable killTask0 = new DummyRunnable("Make task0 throw exception") {
        @Override
        protected void doRun() {
          task0Spec.task.runListeners.add(task -> {throw new SimulatedException(id);});
        }
      };
      int task0NewIterations = 2;
      int task0PreviousRunCount = task0Spec.task.runCount;
      int task0TargetRunCount = task0PreviousRunCount + task0NewIterations;
      long task0TargetTime = task0Spec.getFixedDelayTriggerTime(task0TargetRunCount - 1);  // the expected exec time of the 3rd iteration of task0
      long killTask0TargetTime = task0TargetTime - 1;
      long killTask0TargetDelay = killTask0TargetTime - now;
      ScheduledFuture<?> killTask0Future = scheduler.schedule(killTask0, killTask0TargetDelay, NANOSECONDS);
      printDebugInfo();
      assertEquals(killTask0TargetDelay, killTask0Future.getDelay(NANOSECONDS));
      assertEquals(new SimulatedException(killTask0.id), assertExceptional(task0Spec.future, SimulatedException.class));
      printDebugInfo();
      List<TaskRunRecord> newRecords = getNewRunRecords();
      assertEquals(3, newRecords.size());
      verifyRunRecord(newRecords.get(0), task0Spec, TaskState.NEW,
          task0Spec.getFixedDelayTriggerTime(task0PreviousRunCount), task0PreviousRunCount+1);
      verifyRunRecord(newRecords.get(1), killTask0, "schedule", TaskState.NORMAL,
          killTask0TargetTime, 1);
      verifyRunRecord(newRecords.get(2), task0Spec, TaskState.EXCEPTIONAL,
          task0TargetTime, task0TargetRunCount);
      expectedHistory.addAll(newRecords);
    }

    // at this point, there shouldn't be any scheduled tasks left
    assertEquals(0, scheduler.getScheduledTaskCount());
  }

  @Nonnull
  private List<TaskRunRecord> getNewRunRecords() {
    List<TaskRunRecord> updatedHistory = scheduler.getHistory();
    return updatedHistory.subList(expectedHistory.size(), updatedHistory.size());
  }

  private void printDebugInfo() {
    scheduler.printDebugInfo(5);
  }

  @SafeVarargs
  private final <T extends DummyRunnable> List<RepeatingTaskSpec<T>> scheduleRepeatingTasks(
      Function<RepeatingTaskSpec<T>, ScheduledFuture<?>> method, RepeatingTaskSpec<T>... tasks) {
    List<RepeatingTaskSpec<T>> taskSpecs = Arrays.asList(tasks);
    for (RepeatingTaskSpec<T> taskSpec : taskSpecs) {
      ScheduledFuture<?> future = method.apply(taskSpec);
      assertFutureNotDone(future);
      assertEquals(taskSpec.initialDelay, future.getDelay(taskSpec.unit));
    }
    printDebugInfo();
    // verify that the submitted tasks have been scheduled
    {
      ScheduledFutureTask<?>[] futureTasks = scheduler.examineTasks();
      assertEquals(taskSpecs.size(), futureTasks.length);
      assertComparablesOrdering(futureTasks);  // examineTasks should be sorted by delay
      List<RepeatingTaskSpec<T>> sortedTaskSpecs = CollectionUtils.sortedCopy(taskSpecs,
          Comparator.comparingLong(RepeatingTaskSpec::getInitialDelayNanos));
      for (int i = 0; i < sortedTaskSpecs.size(); i++) {
        RepeatingTaskSpec<T> taskSpec = sortedTaskSpecs.get(i);
        ScheduledFutureTask<?> futureTask = futureTasks[i];
        assertEquals(taskSpec.initialDelay, futureTask.getDelay(taskSpec.unit));
      }
    }

    return taskSpecs;
  }

  /**
   * Cancels the given task using its future
   * @return the cancellation record from the scheduler's history
   */
  private TaskRunRecord cancelPeriodicTask(RepeatingTaskSpec<SlowRunnable> taskSpec) throws Exception {
    long now = ticker.read();
    int expectedRunCount = taskSpec.task.runCount;
    System.out.println("Cancelling " + taskSpec);
    assertTrue(taskSpec.future.cancel(false));
    printDebugInfo();
    assertCancelled(taskSpec.future);
    assertEquals(now, ticker.read());  // the time shouldn't have changed
    TaskRunRecord lastHistoryItem = ListUtils.last(scheduler.getHistory());
    verifyRunRecord(lastHistoryItem, taskSpec, TaskState.CANCELLED, now, expectedRunCount);
    assertFalse(taskSpec.future.cancel(false));  // 2nd invocation of the same should have no effect
    assertCancelled(taskSpec.future);  // still cancelled
    assertSame(lastHistoryItem, ListUtils.last(scheduler.getHistory()));  // no new history entries should've been written
    return lastHistoryItem;
  }

  private void assertFutureNotDone(ScheduledFuture<?> future) {
    assertFalse(future.isDone());
    assertFalse(future.isCancelled());
  }

  private TaskRunRecord verifyRunRecord(TaskRunRecord record, DummyTask task, String methodName, TaskState outcomeState) {
    return verifyRunRecord(record, task, methodName, outcomeState, task.lastRunTime, task.runCount);
  }

  @Nonnull
  private TaskRunRecord verifyRunRecord(TaskRunRecord record, DummyTask task, String methodName, TaskState outcomeState, long expectedTime, int expectedRunCount) {
    assertEquals(task.toString(), record.getName());
    assertEquals(expectedTime, record.getTime());
    assertEquals(expectedRunCount, record.getRunCount());
    assertEquals(outcomeState, record.getOutcomeState());
    StackTraceElement stackFrame = record.getDebugTrace()[0];
    assertEquals(scheduler.getClass().getName(), stackFrame.getClassName());
    assertEquals(methodName, stackFrame.getMethodName());
    return record;
  }

  private <T extends RunnableTaskSpec<?, ?>> void verifyRunRecord(TaskRunRecord record, T taskSpec, TaskState expectedOutcomeState, long expectedTime, int expectedRunCount) {
    verifyRunRecord(record, taskSpec.task, taskSpec.methodName, expectedOutcomeState, expectedTime, expectedRunCount);
  }

  private <T extends RunnableTaskSpec<?, ?>> void verifyRunRecord(TaskRunRecord record, T taskSpec, TaskState expectedOutcomeState) {
    verifyRunRecord(record, taskSpec.task, taskSpec.methodName, expectedOutcomeState);
  }

  private TaskRunRecord verifyRunRecord(TaskRunRecord record, DummyTask task, String methodName, TaskState outcomeState, Object outcome) {
    verifyRunRecord(record, task, methodName, outcomeState);
    assertEquals(outcome, record.getOutcome());
    return record;
  }

  private <T> void assertCompleted(Future<T> future, T expectedResult) throws Exception {
    assertTrue(future.isDone());
    assertFalse(future.isCancelled());
    assertEquals(expectedResult, future.get());
  }

  private void assertCancelled(Future<?> future) throws Exception {
    assertTrue(future.isDone());
    assertTrue(future.isCancelled());
    assertThrows(CancellationException.class, (Function0_t)future::get);
  }

  @SuppressWarnings("unchecked")
  private <T extends Throwable> T assertExceptional(Future<?> future, Class<T> exceptionType) throws Exception {
    ExecutionException executionException = assertThrows(ExecutionException.class, (Function0_t)future::get);
    Throwable cause = executionException.getCause();
    assertEquals(exceptionType, cause.getClass());
    assertTrue(future.isDone());
    assertFalse(future.isCancelled());
    return (T)cause;
  }

  public void testShutdown() throws Exception {
    List<RepeatingTaskSpec<DummyRunnable>> repeatingTasks = scheduleRepeatingTasks(RepeatingTaskSpec::scheduleAtFixedRate,
        new RepeatingTaskSpec<>(2, 1, MILLISECONDS,
            new DummyRunnable("Repeat every 1 ms after 2 ms")),
        new RepeatingTaskSpec<>(2000, 1000, NANOSECONDS,
            new DummyRunnable("Repeat every 1000 ns after 2000 ns"))
    );
    RunnableTaskSpec<DummyRunnable, Future<?>> task0Spec, task1Spec;
    List<RunnableTaskSpec<DummyRunnable, Future<?>>> otherTasks = Arrays.asList(
        task0Spec = new RunnableTaskSpec<>(new DummyRunnable()).submit(),
        task1Spec = new RunnableTaskSpec<>(new DummyRunnable()).schedule(1, MILLISECONDS)
    );
    printDebugInfo();
    assertEquals(repeatingTasks.size() + otherTasks.size(), scheduler.getScheduledTaskCount());
    assertEquals(0, scheduler.getHistory().size());
    {
      // test shutdown
      assertFalse(scheduler.isShutdown());
      assertFalse(scheduler.isTerminated());
      System.out.printf("Invoking %s.shutdown()%n", scheduler.getClass().getSimpleName());
      scheduler.shutdown();
      assertTrue(scheduler.isShutdown());
      assertFalse(scheduler.isTerminated());
      printDebugInfo();
      // no new tasks can be submitted at this point
      assertThrows(RejectedExecutionException.class, () -> scheduler.submit(new DummyRunnable()));
      // the repeating tasks should've been cancelled
      List<TaskRunRecord> runRecords = scheduler.getHistory();
      ArrayList<RepeatingTaskSpec<DummyRunnable>> repeatingTasksSorted = CollectionUtils.sortedCopy(repeatingTasks, Comparator.comparingLong(RepeatingTaskSpec::getInitialDelayNanos));
      for (int i = 0; i < repeatingTasksSorted.size(); i++) {
        RepeatingTaskSpec<DummyRunnable> taskSpec = repeatingTasksSorted.get(i);
        assertCancelled(taskSpec.future);
        verifyRunRecord(runRecords.get(i), taskSpec, TaskState.CANCELLED);
      }
      expectedHistory.addAll(runRecords);
    }
    {
      // now test awaitTermination
      long task1Delay = task1Spec.getInitialDelayNanos();
      assertEquals(task1Delay, ((ScheduledFuture<?>)task1Spec.future).getDelay(NANOSECONDS));
      // we'll wait until just before task1 is due
      long terminationTimeout = task1Delay - 1;
      System.out.printf("Invoking %s.awaitTermination(%d, %s)%n", scheduler.getClass().getSimpleName(), terminationTimeout, NANOSECONDS);
      assertFalse(scheduler.awaitTermination(terminationTimeout, NANOSECONDS));
      printDebugInfo();
      assertFalse(scheduler.isTerminated());  // didn't wait long enough for task1 to run
      assertEquals(terminationTimeout, ticker.read());
      // at this point, task0 should've run, but not task1
      assertEquals(1, task0Spec.task.runCount);
      assertEquals(task0Spec.getInitialDelayNanos(), task0Spec.task.lastRunTime);
      TaskRunRecord task0RunRecord = getOnlyElement(getNewRunRecords());
      verifyRunRecord(task0RunRecord, task0Spec, TaskState.NORMAL);
      assertCompleted(task0Spec.future, null);
      expectedHistory.add(task0RunRecord);
      
      // at this point, only task1 should be scheduled
      assertEquals(1, scheduler.getScheduledTaskCount());
      ScheduledFutureTask<?> onlyRemainingTask = getOnlyElement(scheduler.examineTasks());
      assertEquals(task1Spec.task.toString(), onlyRemainingTask.getName());
      assertSame(task1Spec.future, onlyRemainingTask);
      assertEquals(1, ((ScheduledFuture<?>)task1Spec.future).getDelay(NANOSECONDS));
      
      // await termination again
      System.out.printf("Invoking %s.awaitTermination(%d, %s)%n", scheduler.getClass().getSimpleName(), 10, SECONDS);
      assertTrue(scheduler.awaitTermination(10, SECONDS));
      assertTrue(scheduler.isTerminated());
      printDebugInfo();
      // at this point, the last scheduled task (task1) should've run, and nothing should be scheduled
      assertEquals(1, task1Spec.task.runCount);
      assertEquals(task1Delay, task1Spec.task.lastRunTime);
      assertEquals(task1Delay, ticker.read());
      TaskRunRecord task1RunRecord = getOnlyElement(getNewRunRecords());
      verifyRunRecord(task1RunRecord, task1Spec, TaskState.NORMAL, task1Delay, 1);
      expectedHistory.add(task1RunRecord);
      assertEquals(0, scheduler.getScheduledTaskCount());
    }
  }

  public void testShutdownNow() throws Exception {
    List<RepeatingTaskSpec<DummyRunnable>> repeatingTasks = scheduleRepeatingTasks(RepeatingTaskSpec::scheduleAtFixedRate,
        new RepeatingTaskSpec<>(2, 1, MILLISECONDS,
            new DummyRunnable("Repeat every 1 ms after 2 ms")),
        new RepeatingTaskSpec<>(2000, 1000, NANOSECONDS,
            new DummyRunnable("Repeat every 1000 ns after 2000 ns"))
    );
    List<RunnableTaskSpec<DummyRunnable, Future<?>>> otherTasks = Arrays.asList(
        new RunnableTaskSpec<>(new DummyRunnable()).submit(),
        new RunnableTaskSpec<>(new DummyRunnable()).schedule(1, MILLISECONDS)
    );
    printDebugInfo();
    int nTasks = repeatingTasks.size() + otherTasks.size();
    assertEquals(nTasks, scheduler.getScheduledTaskCount());
    // run the task scheduled for current time
    System.out.printf("Invoking %s.runTasks()%n", scheduler.getClass().getSimpleName());
    TaskRunRecord onlyRunRecord = getOnlyElement(scheduler.runTasks());
    RunnableTaskSpec<DummyRunnable, Future<?>> taskSpec = otherTasks.get(0);
    taskSpec.task.assertWasRun(1, taskSpec.getInitialDelayNanos());
    assertCompleted(taskSpec.future, null);
    verifyRunRecord(onlyRunRecord, taskSpec, TaskState.NORMAL, taskSpec.getInitialDelayNanos(), 1);
    expectedHistory.add(onlyRunRecord);
    ScheduledFutureTask<?>[] scheduledTasks = scheduler.examineTasks();
    assertEquals(nTasks - 1, scheduledTasks.length);


    assertFalse(scheduler.isShutdown());
    assertFalse(scheduler.isTerminated());
    System.out.printf("Invoking %s.shutdownNow()%n", scheduler.getClass().getSimpleName());
    List<Runnable> unfinishedTasks = scheduler.shutdownNow();
    printDebugInfo();
    assertTrue(scheduler.isShutdown());
    assertTrue(scheduler.isTerminated());
    assertEmpty(getNewRunRecords());  // no tasks should've been run by the shutdownNow method
    assertEquals(0, scheduler.getScheduledTaskCount());  // and no tasks should still be scheduled
    // shutdownNow should've returned the tasks that never ran
    assertEquals(nTasks - 1, unfinishedTasks.size());
    assertEquals(SetUtils.newSet(scheduledTasks), SetUtils.newSet(unfinishedTasks));
  }

  @SuppressWarnings("WeakerAccess")
  private abstract class DummyTask {
    protected final int id = nextTaskId++;
    /** Short description of this task to be used in {@link #toString()} */
    protected final String msg;
    protected int runCount;
    protected long lastRunTime;
    protected long lastRunId;
    /**
     * Invoked at the end of the {@link #record()} method; can be used to modify the behavior of an already-submitted
     * tasks after construction.
     */
    protected List<Consumer<? super DummyTask>> runListeners = new ArrayList<>();

    private DummyTask(String msg) {
      this.msg = msg;
    }

    private DummyTask() {
      this(null);
    }

    protected void record() {
      runCount++;
      lastRunId = ++nextTaskRunId;
      lastRunTime = ticker.read();
      runListeners.forEach(listener -> listener.accept(this));
    }

    boolean wasRun() {
      return runCount > 0;
    }

    void assertWasRun(int expectedRunCount, long expectedLastRunTime) {
      assertEquals(expectedRunCount, runCount);
      assertEquals(expectedLastRunTime, lastRunTime);
    }

    @Override
    public String toString() {
      Class<? extends DummyTask> cls = getClass();
      ClassNameParser classNameParser = new ClassNameParser(cls);
      String className = StringUtils.firstNotBlank(
          cls.getSimpleName(), cls.getSuperclass().getSimpleName(),
          classNameParser.getComplexName(),
          DummyTask.class.getSimpleName());
      MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(className)
          .add("id", this.id);
      if (msg != null)
        helper.add("msg", StringUtils.quote(msg));
      return helper.toString();
    }
  }


  private class DummyCallable<V> extends DummyTask implements Callable<V> {
    private final V ret;

    DummyCallable() {
      this(null);
    }

    DummyCallable(V ret) {
      this.ret = ret;
    }

    DummyCallable(String msg, V ret) {
      super(msg);
      this.ret = ret;
    }

    @Override
    public final V call() throws Exception {
      record();
      return doCall();
    }

    protected V doCall() throws Exception {
      return ret;
    }
  }

  private class DummyRunnable extends DummyTask implements Runnable {

    public DummyRunnable(String msg) {
      super(msg);
    }

    public DummyRunnable() {
    }

    @Override
    public final void run() {
      record();
      doRun();
    }

    protected void doRun() {

    }

    /**
     * How long each execution of this task should take.
     * The {@link #run()} method will advance the ticker by this duration
     */
    public long getRunDurationNanos() {
      return 0;
    }
  }

  private class SlowRunnable extends DummyRunnable {
    /**
     * How long each execution of this task should take (in nanoseconds).
     * The {@link #run()} method will advance the ticker by this duration
     */
    private final long runDuration;

    public SlowRunnable(long runDuration, String msg) {
      super(String.format("%s; pause %,d ns", msg, runDuration));
      this.runDuration = runDuration;
    }

    private SlowRunnable(long runDuration) {
      this(runDuration, "");
    }

    @Override
    protected void doRun() {
      ticker.advance(runDuration);
    }

    /**
     * How long each execution of this task should take (in nanoseconds).
     * The {@link #run()} method will advance the ticker by this duration
     */
    @Override
    public long getRunDurationNanos() {
      return runDuration;
    }
  }

  private static class SimulatedException extends RuntimeException {
    private final int id;

    private SimulatedException(int id) {
      super(String.valueOf(id));
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      SimulatedException that = (SimulatedException)o;
      return id == that.id;
    }

    @Override
    public int hashCode() {
      return id;
    }
  }

  @SuppressWarnings("unchecked")
  private class RunnableTaskSpec<T extends DummyRunnable, F extends Future<?>> {

    protected final T task;
    protected long initialDelay;
    protected TimeUnit unit;
    protected F future;

    /**
     * Name of the {@link ScheduledExecutorService} method that returned the future
     */
    protected String methodName;

    public RunnableTaskSpec(T task) {
      this.task = task;
    }

    public RunnableTaskSpec(T task, long initialDelay, TimeUnit unit) {
      this(task);
      this.initialDelay = initialDelay;
      this.unit = unit;
    }

    RunnableTaskSpec<T, F> submit() {
      methodName = "submit";
      future = (F)scheduler.submit(task);
      initialDelay = 0;
      unit = NANOSECONDS;
      return this;
    }

    RunnableTaskSpec<T, F> schedule(long delay, TimeUnit unit) {
      methodName = "schedule";
      future = (F)scheduler.schedule(task, delay, unit);
      initialDelay = delay;
      this.unit = unit;
      return this;
    }

    long getInitialDelayNanos() {
      return unit.toNanos(initialDelay);
    }
  }
  
  private class RepeatingTaskSpec<T extends DummyRunnable> extends RunnableTaskSpec<T, ScheduledFuture<?>> {
    private final long period;

    RepeatingTaskSpec(long initialDelay, long period, TimeUnit unit, T task) {
      super(task, initialDelay, unit);
      this.period = period;
    }

    long getPeriodNanos() {
      return unit.toNanos(period);
    }

    ScheduledFuture<?> scheduleAtFixedRate() {
      methodName = "scheduleAtFixedRate";
      return future = scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     *
     * @param i iteration ordinal, starting with 0
     * @return the expected nano time when the i-th iteration of this task will be executed when submitted
     * using {@link ScheduledExecutorService#scheduleAtFixedRate scheduleAtFixedRate}
     */
    long getFixedRateTriggerTime(int i) {
      assertThat(i).isGreaterThanOrEqualTo(0);
      return unit.toNanos(initialDelay + period * i);
    }

    ScheduledFuture<?> scheduleWithFixedDelay() {
      methodName = "scheduleWithFixedDelay";
      return future = scheduler.scheduleWithFixedDelay(task, initialDelay, period, unit);
    }

    /**
     *
     * @param i iteration ordinal, starting with 0
     * @return the expected nano time when the i-th iteration of this task will be executed when submitted
     * using {@link ScheduledExecutorService#scheduleWithFixedDelay scheduleWithFixedDelay}
     */
    long getFixedDelayTriggerTime(int i) {
      assertThat(i).isGreaterThanOrEqualTo(0);
      long duration = task.getRunDurationNanos();
      long delay = getPeriodNanos() + duration;
      // TODO: this method might not be useful for testing if there are other scheduled tasks also changing the time
      return getInitialDelayNanos() + delay * i;
    }


    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("task", task)
          .add("period", period)
          .add("initialDelay", initialDelay)
          .add("unit", unit)
          .toString();
    }

  }

}