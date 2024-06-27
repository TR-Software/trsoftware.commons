/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.server.testutil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.compare.RichComparable;
import solutions.trsoftware.commons.shared.util.time.SettableTicker;
import solutions.trsoftware.tools.util.TablePrinter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;


/**
 * A {@link ScheduledExecutorService} that uses a mock {@link Ticker} instead of {@link System#nanoTime()}
 * so that the execution delays may be artificially manipulated when running unit tests or simulations.
 * <p>
 * Unlike typical scheduled executors, this implementation executes scheduled tasks on the main thread, and
 * no task is executed until triggered by calling {@link #runTasks()}, which runs all the tasks scheduled
 * up to the current point in time as defined by the mocked {@link Ticker}, or one of the following methods
 * which automatically advance the ticker as needed:
 * <ul>
 *   <li>{@link #runTasksUntil(long)} / {@link #runTasksUntil(long, BooleanSupplier)}</li> / {@link #runTasksUntil(BooleanSupplier)}
 *   <li>{@link #advanceTo(long)} / {@link #advanceToNext()}</li>
 *   <li>{@link #advance(long)} / {@link #advance(long, TimeUnit)}</li>
 * </ul>
 *
 * @see com.google.common.util.concurrent.MoreExecutors#newDirectExecutorService()
 * @author Alex
 * @since 12/5/2022
 */
public class MockScheduledExecutorService implements ScheduledExecutorService {
  /*
  TODO:
    - consolidate with the older solutions.trsoftware.commons.server.util.MockScheduledExecutorService version; ideas:
      1) extract superclass with abstract now/advance/setTime methods that can be implemented with either Clock or Ticker
      2) create a combined ClockTicker object that serves both functions at the same time
    Or just get rid of the old solutions.trsoftware.commons.server.util.MockScheduledExecutorService
  */

  protected enum State {RUNNING, SHUTTING_DOWN, TERMINATED};
  protected volatile State state = State.RUNNING;

  private final PriorityQueue<ScheduledFutureTask<?>> tasks = new PriorityQueue<>();
  private final ArrayList<TaskRunRecord> history = new ArrayList<>();
  private final long startTime;

  private final String name;
  @Nonnull
  private final SettableTicker ticker;


  public MockScheduledExecutorService(SettableTicker ticker) {
    this(null, ticker);
  }

  public MockScheduledExecutorService(String name, SettableTicker ticker) {
    this.name = name;
    this.ticker = Objects.requireNonNull(ticker);
    startTime = now();
  }

  protected long now() {
    return ticker.read();
  }

  protected void incrTime(long timeDelta) {
    ticker.advance(timeDelta);
  }

  protected void setTime(long time) {
    ticker.setTime(time);
  }


  /**
   * Runs all the tasks whose time (as determined by {@link #now()}) has come (or passed)
   * @return a summary of the tasks that were run by this method invocation
   */
  public List<TaskRunRecord> runTasks() {
    long now = now();
    ArrayList<TaskRunRecord> ran = new ArrayList<>();
    while (!tasks.isEmpty() && tasks.element().time <= now) {
      // while there is a task whose time comes before now, keep running tasks
      // (an executed task may schedule another task, that's why we use 2 loops)
      ScheduledFutureTask<?> task = tasks.remove();
      assert task.time <= now;
      if (!task.isDone()) {
        ran.add(runTask(task));
      }
    }
    return ran;
  }

  private TaskRunRecord runTask(ScheduledFutureTask<?> task) {
    task.run();
    TaskRunRecord record = task.getRunRecord();
    history.add(record);
    return record;
  }

  private void cancelTask(ScheduledFutureTask<?> task) {
    tasks.remove(task);
    history.add(task.getRunRecord());
  }

  /**
   * Advances the mock clock to the time of the next scheduled task, and calls {@link #runTasks()}.
   * This will result in the execution of that task as well as any others that are scheduled for that same instant.
   * <p>
   * Upon completion, the {@linkplain #now() current time} will be the time of the task that was executed.
   * <p>
   * This method does nothing and returns an empty list if there are no scheduled tasks.
   *
   * @return a summary of the tasks that were run by this method invocation
   */
  public List<TaskRunRecord> advanceToNext() {
    ScheduledFutureTask<?> nextTask = tasks.peek();
    if (nextTask != null) {
      setTime(nextTask.time);
      return runTasks();
    }
    return Collections.emptyList();
  }

  /**
   * Runs the {@linkplain #now() mock clock} up given absolute time value, incrementally executing eligible tasks
   * along the way.
   * <p>
   * Upon completion, the {@linkplain #now() current time} will be at the specified instant, unless one of the executed
   * tasks changed the time to a different value (e.g. to simulate a delay)
   *
   * @param targetTime the absolute time endpoint
   * @return a summary of the tasks that were run by this method invocation
   * @see #runTasksUntil(long)
   */
  public List<TaskRunRecord> advanceTo(long targetTime) {
    List<TaskRunRecord> ran = runTasksUntil(targetTime);
    // verify postconditions
    assert tasks.isEmpty() || tasks.element().time > targetTime;
    maybeSetTime(targetTime);
    return ran;
  }

  /**
   * Runs the {@linkplain #now() mock clock} forward by the given time offset, incrementally executing eligible tasks
   * along the way.
   * <p>
   * Upon completion, the {@linkplain #now() current time} will be {@code now + delay}, where {@code now} was the
   * current time when this method was invoked.
   *
   * @param delay the time offset (nanos)
   * @return a summary of the tasks that were run by this method invocation
   */
  public List<TaskRunRecord> advance(long delay) {
    return advanceTo(offsetTime(delay));
  }

  /**
   * Runs the {@linkplain #now() mock clock} forward by the given time offset, incrementally executing eligible tasks
   * along the way.
   * <p>
   * Upon completion, the {@linkplain #now() current time} will be {@code now + delay}, where {@code now} was the
   * current time when this method was invoked.
   *
   * @param delay the time offset
   * @param unit unit of the offset
   * @return a summary of the tasks that were run by this method invocation
   */
  public List<TaskRunRecord> advance(long delay, TimeUnit unit) {
    return advance(unit.toNanos(delay));
  }

  /**
   * Runs all queued tasks that are scheduled up to (and including) the specified {@linkplain #now() absolute time}.
   * <p>
   * Upon completion, the {@linkplain #now() current time} will be that of the last task that was executed,
   * (which may be earlier than the specified argument).
   *
   * @return a summary of the tasks that were run by this method invocation
   * @see #advanceTo(long)
   */
  public List<TaskRunRecord> runTasksUntil(long time) {
    return runTasksUntil(time, () -> false);
  }

  /**
   * Runs the clock forward, incrementally executing scheduled tasks until the given condition is met.
   * <p>
   * Upon completion, the {@linkplain #now() current time} will be that of the last task that was executed,
   * unless one of the executed tasks changed the time to a different value (e.g. to simulate a delay)
   *
   * @param stopCondition will stop running tasks when this supplier returns {@code true}
   * @return a summary of the tasks that were run by this method invocation
   */
  public List<TaskRunRecord> runTasksUntil(@Nonnull BooleanSupplier stopCondition) {
    return runTasksUntil(Long.MAX_VALUE, stopCondition);
  }

  /**
   * Runs all queued tasks that are scheduled up to (and including) the specified {@linkplain #now() absolute time},
   * or until the given condition is met, whichever comes first.
   * <p>
   * Upon completion, the {@linkplain #now() current time} will be that of the last task that was executed,
   * (which may be earlier than the specified argument).
   *
   * @param stopCondition will stop running tasks when this supplier returns {@code true}
   * @return a summary of the tasks that were run by this method invocation
   */
  public List<TaskRunRecord> runTasksUntil(long time, @Nonnull BooleanSupplier stopCondition) {
    ArrayList<TaskRunRecord> ran = new ArrayList<>();
    while (!stopCondition.getAsBoolean() && !tasks.isEmpty()) {
      ScheduledFutureTask<?> nextTask = tasks.element();
      if (nextTask.time > time) {
        // timeout
        maybeSetTime(time);
        return ran;
      }
      maybeSetTime(nextTask.time);
      ran.addAll(runTasks());
    }
    return ran;
  }

  /**
   * Sets the {@linkplain #now() current time} to the given value unless it's in the past.  This ensures
   * that the "arrow of time" is preserved (i.e. the clock can't run backwards).
   *
   * @param targetTime the desired absolute time
   * @return {@code true} if the time was changed as a result of this invocation
   */
  private boolean maybeSetTime(long targetTime) {
    if (now() < targetTime) {
      setTime(targetTime);
      return true;
    }
    return false;
  }

  private <V> ScheduledFutureTask<V> scheduleTask(ScheduledFutureTask<V> task) {
    State state = this.state;
    if (state == State.RUNNING) {
      tasks.add(task);
      return task;
    }
    throw new RejectedExecutionException(this + (state == State.SHUTTING_DOWN ? " is being" : " has been") + " shut down");
  }

  // Immediate Tasks:

  @Override
  public void execute(Runnable command) {
    scheduleImmediate(command, new Exception().getStackTrace());
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return scheduleTask(new ScheduledFutureTask<>(task, now(), new Exception().getStackTrace()));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return scheduleTask(new ScheduledFutureTask<>(task, result, now(), new Exception().getStackTrace()));
  }

  @Override
  public Future<?> submit(Runnable task) {
    return scheduleImmediate(task, new Exception().getStackTrace());
  }

  @Nonnull
  private ScheduledFuture<?> scheduleImmediate(Runnable task, StackTraceElement[] stackTrace) {
    return scheduleTask(new ScheduledFutureTask<Void>(task,  null, now(), stackTrace));
  }

  // Delayed Tasks:

  public ScheduledFuture<?> schedule(final Runnable command, long delay, TimeUnit unit) {
    return scheduleTask(new ScheduledFutureTask<Void>(command,  null, offsetTime(delay, unit), new Exception().getStackTrace()));
  }

  public <V> ScheduledFuture<V> schedule(final Callable<V> callable, long delay, TimeUnit unit) {
    return scheduleTask(new ScheduledFutureTask<>(callable, offsetTime(delay, unit), new Exception().getStackTrace()));
  }

  // Periodic Tasks:

  public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, long initialDelay, final long period, final TimeUnit unit) {
    return scheduleTask(new ScheduledFutureTask<Void>(command, null, offsetTime(initialDelay, unit), unit.toNanos(period), new Exception().getStackTrace()));
  }

  public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, long initialDelay, final long delay, final TimeUnit unit) {
    return scheduleTask(new ScheduledFutureTask<Void>(command, null, offsetTime(initialDelay, unit), unit.toNanos(-delay), new Exception().getStackTrace()));
  }

  // Other ExecutorService methods

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.testutil.MockScheduledExecutorService.invokeAll has not been fully implemented yet.");
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.testutil.MockScheduledExecutorService.invokeAll has not been fully implemented yet.");
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.testutil.MockScheduledExecutorService.invokeAny has not been fully implemented yet.");
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.testutil.MockScheduledExecutorService.invokeAny has not been fully implemented yet.");
  }


  // TODO: unit test the following methods
  /**
   * Stops accepting new tasks and cancels any existing periodic tasks.
   * To execute all existing delayed tasks, call {@link #awaitTermination(long, TimeUnit)}
   */
  public void shutdown() {
    if (state == State.RUNNING) {
      state = State.SHUTTING_DOWN;
      // cancel all periodic tasks
      for (ScheduledFutureTask<?> task : tasks.toArray(new ScheduledFutureTask[0])) {
        if (task.isPeriodic())
          task.cancel(false);
      }
    }
  }

  public List<Runnable> shutdownNow() {
    state = State.TERMINATED;
    ArrayList<Runnable> notStarted = new ArrayList<>();
    while (!tasks.isEmpty()) {
      ScheduledFutureTask<?> task = tasks.remove();
      if (task.state.get() == TaskState.NEW)
        notStarted.add(task);
    }
    return notStarted;
  }

  public boolean isShutdown() {
    return state != State.RUNNING;
  }

  public boolean isTerminated() {
    return state == State.TERMINATED;
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) {
    if (state == State.RUNNING)
      shutdown();
    runTasksUntil(offsetTime(timeout, unit));
    if (tasks.isEmpty())
      state = State.TERMINATED;
    return state == State.TERMINATED;
  }

  @Override
  public String toString() {
    return name != null ? String.format("%s(\"%s\")", getClass().getSimpleName(), name) : StringUtils.idToString(this);
  }

  /** @return a copy of the currently scheduled tasks, sorted by delay */
  @VisibleForTesting
  public ScheduledFutureTask<?>[] examineTasks() {
    ScheduledFutureTask<?>[] ret = tasks.toArray(new ScheduledFutureTask[0]);
    Arrays.sort(ret);
    return ret;
  }

  /**
   * @return the number of tasks that have not yet completed
   */
  public int getScheduledTaskCount() {
    return tasks.size();
  }

  public List<TaskRunRecord> getHistory() {
    return ImmutableList.copyOf(history);
  }

  /**
   * @param startTime earliest task execution time for the history subset to return
   */
  @Nonnull
  public List<TaskRunRecord> getHistorySince(long startTime) {
    return history.stream().filter(record -> record.time >= startTime).collect(Collectors.toList());
  }

  /**
   * @return the stack trace starting with the first element that's not in this class
   */
  private StackTraceElement[] getCallerStackTrace() {
    // TODO: consider extracting this method to RuntimeUtils
    StackTraceElement[] stackTrace = new Exception().getStackTrace();
    int i;
    for (i = 0; i < stackTrace.length; i++) {
      StackTraceElement element = stackTrace[i];
      if (!element.getClassName().equals(getClass().getName()))
        break;
    }
    return Arrays.copyOfRange(stackTrace, i, stackTrace.length);
  }

  public void printDebugInfo() {
    printDebugInfo(System.out);
  }

  public void printDebugInfo(PrintStream out) {
    printDebugInfo(out, false, getHistory());
  }

  public void printDebugInfo(boolean verbose) {
    printDebugInfo(System.out, verbose, getHistory());
  }

  /**
   * @param startTime will print only tasks executed after this time
   */
  public void printDebugInfo(long startTime) {
    printDebugInfo(System.out, false, getHistorySince(startTime));
  }

  /**
   * @param nRecords will limit the output to this number of history items
   */
  public void printDebugInfo(int nRecords) {
    printDebugInfo(System.out, false, history.subList(Math.max(0, history.size()-nRecords), history.size()));
  }

  /**
   * @param selectedRange will include only this history subset in the output
   */
  public void printDebugInfo(PrintStream out, boolean verbose, List<TaskRunRecord> selectedRange) {
    ScheduledFutureTask<?>[] tasks = examineTasks();
    String[] headerLines = new String[] {
        String.format("%s state at ticker time %,d:", toString(), now()),
        "\tat " + getCallerStackTrace()[0]
    };
    int headerWidth = Arrays.stream(headerLines).mapToInt(String::length).max().getAsInt();
    String headerText = StringUtils.join(System.lineSeparator(), headerLines);
    out.printf("%s%n%s%n%1$s%n",
        StringUtils.repeat('=', headerWidth), headerText);
    out.printf("%d Scheduled tasks:%n", tasks.length);
    if (tasks.length > 0) {
      TablePrinter tp = new TablePrinter();
      for (ScheduledFutureTask<?> task : tasks) {
        tp.newRow()
            .addCol("id", task.id)
            .addCol("eta / delay", etaToString(task.time))
            .addCol("name", printTaskName(task, verbose));
      }
      tp.printTable(out);
    }
    out.printf("%d Completed tasks", history.size());
    if (!selectedRange.isEmpty()) {
      out.printf(" (displaying time range [%,d, %,d]):%n", selectedRange.get(0).time, now());
      TablePrinter tp = new TablePrinter();
      for (TaskRunRecord task : selectedRange) {
        tp.newRow()
            .addCol("id", task.id)
            .addCol("time / offset", timeToString(task.time))
            .addCol("runCount", "%,d", task.runCount)
            .addCol("outcome", task.outcomeState)
            .addCol("name", printTaskName(task, verbose));
      }
      tp.printTable(out);
    }
    out.println();
    out.println(StringUtils.repeat('=', headerWidth));
  }

  private String printTaskName(TaskInfo task, boolean verbose) {
    StringBuilder ret = new StringBuilder(task.getName());
    if (verbose)
      ret.append(" // ").append(task.getDebugTrace()[1]);
    return ret.toString();
  }

  private String etaToString(long time) {
    return String.format("%,d ns (%+,d ns)", time, time - now());
  }

  private String timeToString(long time) {
    if (time < 0)
      return "N/A";
    String ret = String.format("%,d ns", time);
    if (startTime != 0) {
      ret += String.format(" (%+,d ns)", time - startTime);
    }
    return ret;
  }


  /**
   * Computes the trigger time of a delayed action (i.e. now + delay)
   */
  private long offsetTime(long delay) {
    return now() + delay;
  }

  /**
   * Computes the trigger time of a delayed action (i.e. now + delay)
   */
  private long offsetTime(long delay, TimeUnit unit) {
    return offsetTime(unit.toNanos(delay));
  }

  private void reExecutePeriodic(ScheduledFutureTask<?> task) {
    if (!isShutdown())
      tasks.add(task);
  }

  private static final AtomicLong nextSequenceNumber = new AtomicLong(0);

  public enum TaskState implements RichComparable<TaskState> {
    NEW, NORMAL, EXCEPTIONAL, CANCELLED;
  }

  /**
   *
   * @see ScheduledThreadPoolExecutor.ScheduledFutureTask
   */
  @SuppressWarnings("rawtypes")
  @VisibleForTesting
  public class ScheduledFutureTask<V> implements RunnableScheduledFuture<V>, TaskInfo {
    /** Used to deterministically resolve timing collisions (see compareTo) */
    protected final long id = nextSequenceNumber.getAndIncrement();
    /**
     * The {@link Object#toString() toString} representation of the underlying {@link Runnable} or {@link Callable};
     * retained for debugging.
     */
    protected final String name;
    /**
     * Nano time when this task is scheduled to be run
     */
    protected long time;
    /**
     * Period in nanoseconds for repeating tasks.  A positive
     * value indicates fixed-rate execution.  A negative value
     * indicates fixed-delay execution.  A value of 0 indicates a
     * non-repeating task.
     */
    private final long period;

    // copied from java.util.concurrent.FutureTask:
    private AtomicReference<TaskState> state = new AtomicReference<>(TaskState.NEW);

    /**
     * The underlying callable that will be executed; nulled out after running.
     * Note: this could be either the original {@link Callable} submitted to the executor or
     * a {@link Runnable} wrapped with {@link Executors#callable(Runnable, Object)}, if it was submitted
     * using a method like {@link #scheduleAtFixedRate}.
     * @see #runnable
     */
    private Callable<V> callable;

    /**
     * The original {@link Runnable} submitted to the executor, prior to being wrapped with
     * {@link Executors#callable(Runnable, Object)}. Retained for debugging.
     */
    @Nullable
    private Runnable runnable;

    /** The result to return or exception to throw from get() */
    private Object outcome;

    /**
     * The stack snapshot when this task was created; retained for debugging.
     */
    private final StackTraceElement[] debugTrace;
    /**
     * Number of times this task has been run so far (greater than 1 only if periodic); retained for debugging.
     */
    private int runCount;
    /**
     * Time when this task was last run; -1 indicates that it hasn't been run yet.
     */
    private long lastRunTime = -1;
    /**
     * Time when this task was last run; -1 indicates that it hasn't been cancelled.
     */
    private long cancellationTime = -1;

    /**
     * @param time scheduled execution {@linkplain #now() time} (nanos)
     * @param debugTrace stack snapshot from the method which created this task (retained for debugging)
     * @see #schedule(Callable, long, TimeUnit)
     */
    ScheduledFutureTask(@Nonnull Callable<V> callable, long time, StackTraceElement[] debugTrace) {
      this.callable = Objects.requireNonNull(callable);
      this.time = time;
      this.name = callable.toString();
      this.debugTrace = debugTrace;
      this.period = 0;
    }

    /**
     * @param result fixed value to be returned by the {@link Future} upon completion
     * @param time scheduled execution {@linkplain #now() time} (nanos)
     * @param debugTrace stack snapshot from the method which created this task (retained for debugging)
     * @see #schedule(Runnable, long, TimeUnit)
     */
    ScheduledFutureTask(@Nonnull Runnable runnable, V result, long time, StackTraceElement[] debugTrace) {
      this(runnable, result, time, 0, debugTrace);
    }

    /**
     * Creates a repeating task.
     *
     * @param result fixed value to be returned by the {@link Future} upon completion
     * @param time scheduled {@linkplain #now() time} (nanos) of first execution
     * @param period the period in nanoseconds for the repeating task:
     *     A positive value indicates fixed-rate execution.
     *     A negative value indicates fixed-delay execution.
     *     A value of 0 indicates a non-repeating task.
     * @param debugTrace stack snapshot from the method which created this task (retained for debugging)
     * @see #scheduleAtFixedRate(Runnable, long, long, TimeUnit)
     * @see #scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
     */
    public ScheduledFutureTask(@Nonnull Runnable runnable, V result, long time, long period, StackTraceElement[] debugTrace) {
      this.callable = Executors.callable(runnable, result);
      this.time = time;
      this.name = runnable.toString();
      this.debugTrace = debugTrace;
      this.period = period;
    }

    @Override
    public int compareTo(@Nonnull Delayed other) {
      if (other == this)
        return 0;
      if (other instanceof ScheduledFutureTask) {
        ScheduledFutureTask otherTask = (ScheduledFutureTask)other;
        int result = Long.compare(this.time, otherTask.time);
        // make sure the result is nonzero if the two objects are not actually equal
        // (because the ConcurrentSkipListSet treats comparison=0 as the elements being equal, and will replace one with the other)
        if (result == 0)
          return Long.compare(this.id, otherTask.id); // use the unambiguous creation order to resolve conflict deterministically
        return result;
      }
      return Long.compare(getDelay(TimeUnit.NANOSECONDS), other.getDelay(TimeUnit.NANOSECONDS));
    }

    public long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public long getTime() {
      return time;
    }

    @Override
    public long getPeriod() {
      return period;
    }

    @Override
    public StackTraceElement[] getDebugTrace() {
      return debugTrace;
    }

    @Override
    public boolean isPeriodic() {
      return period != 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
      return unit.convert(time - now(), TimeUnit.NANOSECONDS);
    }

    /**
     * Sets the next time to run for a periodic task.
     */
    private void setNextRunTime() {
      long p = period;
      if (p > 0)
        time += p;  // fixed rate
      else
        time = offsetTime(-p);  // fixed delay
    }

    @Override
    public void run() {
      if (state.get() == TaskState.NEW) {
        runCount++;
        lastRunTime = now();
        V result;
        boolean success;
        try {
          result = callable.call();
          success = true;
        }
        catch (Throwable ex) {
          result = null;
          success = false;
          setException(ex);
        }
        if (success) {
          if (!isPeriodic()) {
            set(result);
            callable = null;  // release memory
          }
          else {
            // periodic tasks don't return a result
            if (state.compareAndSet(TaskState.NEW, TaskState.NEW)) {
              // this compareAndSet ensures that the task didn't cancel itself when callable.call was executed
              setNextRunTime();
              state.set(TaskState.NEW);  // reset the state to make the task eligible for re-execution
              reExecutePeriodic(this);
            }
          }
        }
      }
    }

    public TaskState getState() {
      return state.get();
    }

    public Callable<V> getCallable() {
      return callable;
    }

    @Nullable
    public Runnable getRunnable() {
      return runnable;
    }

    public Object getOutcome() {
      return outcome;
    }

    public int getRunCount() {
      return runCount;
    }

    public long getLastRunTime() {
      return lastRunTime;
    }

    public long getCancellationTime() {
      return cancellationTime;
    }

    /**
     * @return a summary of the last run (or cancellation) of this task
     */
    private TaskRunRecord getRunRecord() {
      return new TaskRunRecord(id, name, cancellationTime < 0 ? lastRunTime : cancellationTime, time, runCount, period, outcome, state.get(), debugTrace);
    }

    /**
     * Sets the result of this future to the given value unless
     * this future has already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon successful completion of the computation.
     *
     * @param v the value
     */
    protected void set(V v) {
      if (state.compareAndSet(TaskState.NEW, TaskState.NORMAL)) {
        outcome = v;
        done();
      }
    }

    /**
     * Causes this future to report an {@link ExecutionException}
     * with the given throwable as its cause, unless this future has
     * already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon failure of the computation.
     *
     * @param t the cause of failure
     */
    protected void setException(Throwable t) {
      if (state.compareAndSet(TaskState.NEW, TaskState.EXCEPTIONAL)) {
        outcome = t;
        done();
      }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      // NOTE: not handling interrupts, since our impl is designed to run in main thread
      if (state.compareAndSet(TaskState.NEW, TaskState.CANCELLED)) {
        cancellationTime = now();
        cancelTask(this);
        return true;
      }
      return false;
    }

    @Override
    public boolean isCancelled() {
      return state.get() == TaskState.CANCELLED;
    }

    @Override
    public boolean isDone() {
      return state.get().isGreaterThan(TaskState.NEW);
    }

    /**
     * Returns result or throws exception for completed task.
     *
     * @param state completed state value
     */
    @SuppressWarnings("unchecked")
    private V getResult(TaskState state) throws ExecutionException {
      Object x = outcome;
      switch (state) {
        case NORMAL:
          return (V)x;
        case CANCELLED:
          throw new CancellationException();
        case EXCEPTIONAL:
          throw new ExecutionException((Throwable)x);
      }
      // should never happen:
      throw new IllegalStateException("Task state = " + state);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
      TaskState state = this.state.get();
      if (state.isGreaterThan(TaskState.NEW)) {
        return getResult(state);
      }
      // have to wait for result
      return getResult(awaitDone(false, 0));
    }


    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      TaskState state = this.state.get();
      if (state.isGreaterThan(TaskState.NEW)) {
        return getResult(state);
      }
      // have to wait for result
      state = awaitDone(true, unit.toNanos(timeout));
      if (state == TaskState.NEW)
        throw new TimeoutException();
      return getResult(state);
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     *
     * @param timed true if use timed waits
     * @param nanos time to wait, if timed
     * @return state upon completion
     */
    private TaskState awaitDone(boolean timed, long nanos) throws InterruptedException {
      if (isPeriodic()) {
        System.err.println("WARNING: A periodic future's get() method can only return by throwing an exception");
        // can only throw an exception upon task cancellation or abnormal termination of a task execution; see https://download.java.net/java/early_access/loom/docs/api/java.base/java/util/concurrent/ScheduledExecutorService.html#scheduleAtFixedRate(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)
      }
      assert tasks.contains(this);  // sanity check (this task can never complete if not on queue)

      long deadline = timed ? now() + nanos : Long.MAX_VALUE;
      runTasksUntil(deadline, this::isDone);
      return state.get();
    }

    /**
     * Optional hook for reporting task completion
     */
    protected void done() {

    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("name", name)
          .add("time", time)
          .add("period", period)
          .add("state", state)
          .add("outcome", outcome)
          .add("debugTrace", debugTrace[1])
          .add("runCount", runCount)
          .add("lastRunTime", lastRunTime)
          .toString();
    }
  }


  public static class TaskRunRecord implements TaskInfo {
    /**
     * The sequence number of the task when it was submitted.
     */
    private final long id;
    /**
     * The {@link Object#toString() toString} representation of the underlying {@link Runnable} or {@link Callable};
     * retained for debugging.
     */
    private final String name;
    /**
     * Nano time when this task was run or cancelled; -1 indicates that it hasn't been run or cancelled
     */
    private final long time;
    /**
     * Nano time when this task is scheduled to be run next (if periodic); -1 indicates that will not run again.
     */
    private final long nextRunTime;
    /**
     * Number of times this task has been run (greater than 1 only if periodic)
     */
    private final int runCount;
    /**
     * Period in nanoseconds for repeating tasks.  A positive
     * value indicates fixed-rate execution.  A negative value
     * indicates fixed-delay execution.  A value of 0 indicates a
     * non-repeating task.
     */
    private final long period;
    /** The result to return or exception to throw from get() */
    private final Object outcome;

    private final TaskState outcomeState;

    /**
     * The stack snapshot when this task was created, retained for debugging.
     */
    private final StackTraceElement[] debugTrace;

    TaskRunRecord(long id, String name, long time, long nextRunTime, int runCount, long period, Object outcome, TaskState outcomeState, StackTraceElement[] debugTrace) {
      this.id = id;
      this.name = name;
      this.time = time;
      this.nextRunTime = outcomeState == TaskState.NEW ? nextRunTime : -1;
      this.runCount = runCount;
      this.period = period;
      this.outcome = outcome;
      this.outcomeState = outcomeState;
      this.debugTrace = debugTrace;
    }


    @Override
    public long getId() {
      return id;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public long getTime() {
      return time;
    }

    public long getNextRunTime() {
      return nextRunTime;
    }

    @Override
    public long getPeriod() {
      return period;
    }

    public Object getOutcome() {
      return outcome;
    }

    public TaskState getOutcomeState() {
      return outcomeState;
    }

    @Override
    public StackTraceElement[] getDebugTrace() {
      return debugTrace;
    }

    public int getRunCount() {
      return runCount;
    }

    @Override
    public boolean isDone() {
      return outcomeState.isGreaterThan(TaskState.NEW);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("name", name)
          .add("time", time)
          .add("runCount", runCount)
          .add("nextRunTime", nextRunTime)
          .add("period", period)
          .add("outcome", outcome)
          .add("outcomeState", outcomeState)
          .add("debugTrace[1]", debugTrace[1])
          .toString();
    }
  }


  public interface TaskInfo {
    long getId();

    String getName();

    long getTime();

    long getPeriod();

    StackTraceElement[] getDebugTrace();

    boolean isDone();

    default boolean isPeriodic() {
      return getPeriod() != 0;
    };
  }
}

