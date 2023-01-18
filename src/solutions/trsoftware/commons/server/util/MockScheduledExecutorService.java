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

package solutions.trsoftware.commons.server.util;

import com.google.common.annotations.VisibleForTesting;
import solutions.trsoftware.commons.server.io.StringPrintStream;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link ScheduledExecutorService} that uses the mocked {@link Clock} class instead of the
 * System clock, so that the execution delays may be artificially manipulated
 * when running unit tests or simulations.
 *
 * Unlike typical scheduled executors, this class is event-driven (single-threaded)
 * and its event loop must be externally pumped by repeatedly calling the {@link #pumpEvents()} method.
 * The scheduled tasks will be executed inline by this method. To imitate the behavior
 * of a multithreaded executor you can call {@link #pumpEvents()} from an external timer.
 *
 * @see #advanceClockAndPump(long)
 * @see #advanceClockAndPumpIncrementally(long)
 * @see #advanceClockAndPumpIncrementallyTo(long)
 * @see com.google.common.util.concurrent.MoreExecutors#newDirectExecutorService()
 * @author Alex
 * @deprecated Superceded by {@link solutions.trsoftware.commons.server.testutil.MockScheduledExecutorService}
 */
public class MockScheduledExecutorService implements ScheduledExecutorService {
  // TODO(01/18/2023): remove this class from deployed production code
  /*
  TODO(11/13/2019): refactor this class:
    - extend java.util.concurrent.AbstractExecutorService
    - review the code in java.util.concurrent.ScheduledThreadPoolExecutor as an example of how to implement this
  */


  private NavigableSet<ScheduledTask> tasks = new ConcurrentSkipListSet<ScheduledTask>();
  private AtomicBoolean wasShutDown = new AtomicBoolean();

  public MockScheduledExecutorService() {

  }

  /** Initializes to contain the same tasks as the given executor */
  public MockScheduledExecutorService(MockScheduledExecutorService originator) {
    // make proper defensive copies of all the tasks
    for (ScheduledTask task : originator.tasks) {
      if (task instanceof RepeatingTask) {
        RepeatingTask oldTask = (RepeatingTask)task;
        scheduleRepeatingTask(new RepeatingTask(oldTask.runnable, oldTask.time, oldTask.period, oldTask.periodUnit));
      }
      else if (task instanceof ScheduledRunnableTask) {
        ScheduledRunnableTask runnableTask = (ScheduledRunnableTask)task;
        scheduleTask(new ScheduledRunnableTask(runnableTask.runnable, runnableTask.time));
      }
      else if (task instanceof ScheduledCallableTask) {
        ScheduledCallableTask callableTask = (ScheduledCallableTask)task;
        scheduleTask(new ScheduledCallableTask(callableTask.callable, callableTask.time));
      }
    }
  }


  /**
   * Advances the mock clock the given number of ms and then executes
   * all the events that are now eligible for execution.
   * @param millis
   */
  public void advanceClockAndPump(long millis) {
    Clock.advance(millis);
    pumpEvents();
  }


  /**
   * Unlike {@link #advanceClockAndPump}, which advances the clock in one go, this method
   * advances the mock clock incrementally.
   * <p>
   * As a performance optimization, the clock will be advanced by 1/64th of the remaining interval
   * on each iteration (or 1 millis, whichever is larger), invoking all the tasks scheduled up to that point.
   * The clock is guaranteed to stop exactly at the target time,
   * because we'll be pumping 1 ms at a time toward the end of the interval.
   * <p>
   * <strong>NOTE:</strong> although this algorithm attempts to execute the scheduled tasks as close to their target time
   * as possible, they may actually execute later than originally scheduled (because some ticks will be skipped over).
   * To ensure that tasks are executed at exactly the right time, use the {@link #advanceClockAndPumpIncrementally(long, long)}
   * method instead, with {@code tickMillis = 1} as the second argument.
   * 
   * @param millis the clock will be advanced by this duration
   */
  public void advanceClockAndPumpIncrementally(long millis) {
    advanceClockAndPumpIncrementallyTo(Clock.currentTimeMillis() + millis);
  }

  /**
   * Unlike {@link #advanceClockAndPump}, which advances the clock in one go, this method
   * advances the mock clock incrementally, {@code tickMillis} at a time, and calls {@link #pumpEvents()}
   * at the end of each iteration.
   * <p>
   * Passing {@code tickMillis = 1} ensures that tasks are executed exactly as scheduled; otherwise can improve speed
   * at the expense of precision.
   * For example: if argument {@code tickMillis < 1}, will advance the clock by 1/64th of the remaining interval on each
   * iteration (using the algorithm described in {@link #advanceClockAndPumpIncrementally(long)}).
   *
   * @param millis the clock will be advanced by this duration
   * @param tickMillis the clock will be advanced by this amount on each iteration; if less than 1,
   * will advance the clock by 1/64th of the remaining interval on each iteration (which is the default
   * behavior of the {@link #advanceClockAndPumpIncrementally(long)} method).
   */
  public void advanceClockAndPumpIncrementally(long millis, long tickMillis) {
    advanceClockAndPumpIncrementallyTo(Clock.currentTimeMillis() + millis, tickMillis);
  }

  /**
   * Similar to {@link #advanceClockAndPumpIncrementally(long, long)}, this method advances the mock clock incrementally,
   * {@code tickMillis} at a time, and calls {@link #pumpEvents()} at the end of each iteration.  The only difference
   * is that instead of specifying the offset, you specify the absolute time to advance the clock to.
   * <p>
   * Passing {@code tickMillis = 1} ensures that tasks are executed exactly as scheduled; otherwise can improve speed
   * at the expense of precision.
   * For example: if argument {@code tickMillis < 1}, will advance the clock by 1/64th of the remaining interval on each
   * iteration (using the algorithm described in {@link #advanceClockAndPumpIncrementallyTo(long)}).
   *
   * @param targetTime an absolute timestamp
   * @param tickMillis the clock will be advanced by this amount on each iteration; if less than 1,
   * will advance the clock by 1/64th of the remaining interval on each iteration (which is the default
   * behavior of the {@link #advanceClockAndPumpIncrementallyTo(long)} method).
   *
   * @param tickMillis the clock will be advanced by this amount on each iteration:
   *   1) a value of 1 ensures that events
   * if less than 1,
   * will advance the clock by 1/64th of the remaining interval on each iteration (which is the default
   * behavior of the {@link #advanceClockAndPumpIncrementally(long)} method).
   */
  public void advanceClockAndPumpIncrementallyTo(long targetTime, long tickMillis) {
    int tickCount = 0;
    long timeGap = targetTime - Clock.currentTimeMillis();
    long now;
    while ((now = Clock.currentTimeMillis()) < targetTime) {
      long gap = targetTime - now;  // millis remaining before targetTime
      if (tickMillis > 0) {
        // TODO: unit test this case
        Clock.advance(Math.min(gap, tickMillis));
      }
      else {
        // tick through the clock, 1/64 th of the interval at a time, until we reach the target date
        // this means that only the last 64 millis will be ticked through 1 millis at the time
        // (NOTE: can't tick one mills at a time because large gaps in the logs would take a really long time)
        long delta = Math.max(1, gap >> 6);
        if (now + delta*2 > targetTime) {
          // if we're less than 2 deltas away from the target time, then might as well just jump the clock to the target time (to avoid falling short)
          Clock.set(targetTime);
        }
        else {
          Clock.advance(delta);
        }
      }
      tickCount++;
      pumpEvents();
      // all the tasks scheduled up until this time should have finished
      assert oldestUnfinishedTaskTime() > Clock.currentTimeMillis(): "oldestUnfinishedTaskTime() > " + new Date(Clock.currentTimeMillis()) + "\n" + dumpTasks();
    }
    // debugging statnement
//    System.out.printf("Advanced clock over time gap of %d millis using %d ticks%n", timeGap, tickCount);
  }

  /**
   * Similar to {@link #advanceClockAndPumpIncrementally(long)}, this method advances the mock clock incrementally
   * and calls {@link #pumpEvents()} at the end of each iteration.  The only difference
   * is that instead of specifying the offset, you specify the absolute time to advance the clock to.
   * <p>
   * As a performance optimization, the clock will be advanced by 1/64th of the remaining interval
   * on each iteration (or 1 millis, whichever is larger), invoking all the tasks scheduled up to that point.
   * The clock is guaranteed to stop exactly at the target time,
   * because we'll be pumping 1 ms at a time toward the end of the interval.
   * <p>
   * <strong>NOTE:</strong> although this algorithm attempts to execute the scheduled tasks as close to their target time
   * as possible, they may actually execute later than originally scheduled (because some ticks will be skipped over).
   * To ensure that tasks are executed at exactly the right time, use the
   * {@link #advanceClockAndPumpIncrementallyTo(long, long)} method instead, with {@code tickMillis = 1}) as the second argument.
   *
   * @param targetTime an absolute timestamp
   */
  public void advanceClockAndPumpIncrementallyTo(long targetTime) {
    advanceClockAndPumpIncrementallyTo(targetTime, 0);
  }

  /** Runs all the tasks whose time (as determined by {@link Clock#currentTimeMillis()}) has come (or passed) */
  public void pumpEvents() {
    long now = Clock.currentTimeMillis();
    while (!tasks.isEmpty() && tasks.first().time <= now) {
      // while there is a task whose time comes before now, keep running tasks
      // (an executed task may schedule another task, that's why we use 2 loops)
      Iterator<ScheduledTask> iter = tasks.iterator();
      while (iter.hasNext()) {
        ScheduledTask task = iter.next();
        if (task.time > now)
          break; // this and all subsequent tasks aren't ready to be executed yet 
        if (!task.running && !task.finished) {
          task.running = true;
          // NOTE: this implementation is single-threaded, but if we wanted to run
          // the task in a separate thread, this would be the place to do it
          task.run();
          task.finished = true; // if we were running the task in a separate thread, we'd have to ask that thread if it's finished, but since we're single-threaded, we know it's finished now
          iter.remove();
        }
      }
    }
  }

  /** The time at which the oldest unfinished task was scheduled to run */
  public long oldestUnfinishedTaskTime() {
    try {
      return tasks.first().time;
    } catch (NoSuchElementException ex) {
      return Long.MAX_VALUE;  // a value useful for comparison (all times come before this)
    }
  }

  public MockScheduledFuture<?> schedule(final Runnable command, long delay, TimeUnit unit) {
    return scheduleTask(new ScheduledRunnableTask(command,  Clock.currentTimeMillis() + unit.toMillis(delay)));
  }

  public <V> MockScheduledFuture<V> schedule(final Callable<V> callable, long delay, TimeUnit unit) {
    return scheduleTask(new ScheduledCallableTask<>(callable, Clock.currentTimeMillis() + unit.toMillis(delay)));
  }

  public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, long initialDelay, final long period, final TimeUnit unit) {
    return scheduleRepeatingTask(new RepeatingTask(command, Clock.currentTimeMillis() + unit.toMillis(initialDelay), period, unit));
  }

  private <V> MockScheduledFuture<V> scheduleTask(ScheduledTask<V> task) {
    if (!wasShutDown.get()) {
      tasks.add(task);
      return new MockScheduledFuture<>(task);
    }
    throw new RejectedExecutionException(this + " has been shut down");
  }

  private ScheduledFuture<?> scheduleRepeatingTask(RepeatingTask task) {
    ScheduledFuture<?> taskFuture = scheduleTask(task);
    task.repeatingFuture = new RepeatingTaskFuture<>(taskFuture);
    return task.repeatingFuture;
  }

  // TODO(Jan 22, 2013): unit test this
  public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, long initialDelay, final long delay, final TimeUnit unit) {
    // wrap the command in a block that will re-schedule itself upon completion
    return schedule(new Runnable() {
      public void run() {
        try {
          command.run();
        }
        finally {
          // re-schedule itself
          schedule(this, unit.toMillis(delay), TimeUnit.MILLISECONDS);
        }
      }
    }, initialDelay, unit);

  }

  public void shutdown() {
    shutdownNow();
  }

  public List<Runnable> shutdownNow() {
    ArrayList<Runnable> ret = new ArrayList<>();
    if (wasShutDown.compareAndSet(false, true)) {
      for (Iterator<ScheduledTask> it = tasks.iterator(); it.hasNext(); ) {
        ret.add(it.next());
        it.remove();
      }
    }
    return ret;
  }

  public boolean isShutdown() {
    return wasShutDown.get();
  }

  public boolean isTerminated() {
    throw new UnsupportedOperationException("Method .isTerminated has not been fully implemented yet.");
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return true;
  }

  public <T> MockScheduledFuture<T> submit(Callable<T> task) {
    return schedule(task, 0, TimeUnit.MILLISECONDS);
  }

  public <T> MockScheduledFuture<T> submit(Runnable task, T result) {
    return submit(Executors.callable(task, result));
  }

  public Future<?> submit(Runnable task) {
    return schedule(task, 0, TimeUnit.MILLISECONDS);
  }

  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    throw new UnsupportedOperationException("Method .invokeAll has not been fully implemented yet.");
  }

  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    throw new UnsupportedOperationException("Method .invokeAll has not been fully implemented yet.");
  }

  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    throw new UnsupportedOperationException("Method .invokeAny has not been fully implemented yet.");
  }

  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    throw new UnsupportedOperationException("Method .invokeAny has not been fully implemented yet.");
  }

  public void execute(Runnable command) {
    submit(command);
  }

  /** @return a list of currently scheduled tasks */
  public List<ScheduledTask> examineTasks() {
    return new ArrayList<>(tasks);
  }

  public int getScheduledTaskCount() {
    return tasks.size();
  }

  /** Prints a list of currently scheduled tasks */
  public String dumpTasks() {
    PrintStream out = new StringPrintStream();
    out.printf("Tasks scheduled as of %s in %s%n", new Date(Clock.currentTimeMillis()), toString());
    out.printf("%5s, %30s, %s%n", "id", "time", "name");
    for (ScheduledTask task : tasks) {
      out.printf("%5d, %30s, %s%n", task.id, new Date(task.time), task.name);
    }
    return out.toString();
  }

  private static final AtomicLong nextSequenceNumber = new AtomicLong(0);

  @VisibleForTesting
  public abstract static class ScheduledTask<V> implements Runnable, Comparable<ScheduledTask<V>> {
    // TODO: rewrite this to be like java.util.concurrent.ScheduledThreadPoolExecutor.ScheduledFutureTask
    /** Used to deterministically resolve timing collisions (see compareTo) */
    protected final long id = nextSequenceNumber.getAndIncrement();
    protected final String name;
    protected final long time;
    private boolean running;
    private boolean finished;
    protected V computationResult;

    private ScheduledTask(long time, String name) {
      this.time = time;
      this.name = name;
    }

    @Override
    public int compareTo(@Nonnull ScheduledTask other) {
      int result = Long.compare(this.time, other.time);
      // make sure the result is nonzero if the two objects are not actually equal
      // (because the ConcurrentSkipListSet treats comparison=0 as the elements being equal, and will replace one with the other)
      if (result == 0 && !this.equals(other))
        return Long.compare(this.id, other.id); // use the unambiguous creation order to resolve conflict deterministically
      return result;
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

    public boolean isRunning() {
      return running;
    }

    public boolean isFinished() {
      return finished;
    }

    public V getComputationResult() {
      return computationResult;
    }
  }

  private class ScheduledRunnableTask extends ScheduledTask<Void> {
    protected final Runnable runnable;

    protected ScheduledRunnableTask(Runnable runnable, long time) {
      super(time, runnable.toString());
      this.runnable = runnable;
    }

    public void run() {
      runnable.run();
    }
  }

  private class ScheduledCallableTask<V> extends ScheduledTask<V> {
    protected final Callable<V> callable;

    protected ScheduledCallableTask(Callable<V> callable, long time) {
      super(time, callable.toString());
      this.callable = callable;
    }

    public void run() {
      try {
        computationResult = callable.call();
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

  private class RepeatingTask extends ScheduledRunnableTask {
    private long period;
    private TimeUnit periodUnit;
    /**
     * This is a special future that can be used to cancel the entire repeating
     * series of these tasks (it always refers to the latest task in the series)
     */
    private RepeatingTaskFuture repeatingFuture;

    private RepeatingTask(Runnable runnable, long nextExecutionTime, long period, TimeUnit periodUnit) {
      super(runnable, nextExecutionTime);
      this.period = period;
      this.periodUnit = periodUnit;
    }

    @Override
    public void run() {
      // first, run the task
      super.run();
      // now schedule the next instance of the task (if the above command
      // throws an exception, then the task will not be scheduled again,
      // which is in accordance to the ScheduledExecutorService interface spec
      long nextExecutionTime = time + periodUnit.toMillis(period);
      RepeatingTask task = new RepeatingTask(runnable, nextExecutionTime, period, periodUnit);
      ScheduledFuture<?> taskFuture = scheduleTask(task);
      task.repeatingFuture = repeatingFuture;
      // point the repeating future to the latest instance of the task
      repeatingFuture.delegate = taskFuture;
    }
  }


  @VisibleForTesting
  public class MockScheduledFuture<V> implements ScheduledFuture<V> {
    private boolean cancelled;
    private final ScheduledTask<V> task;

    public MockScheduledFuture(ScheduledTask<V> task) {
      this.task = task;
      cancelled = false;
    }

    public long getDelay(TimeUnit unit) {
      return unit.convert(task.time - Clock.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed o) {
      return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
      if (task.running && !mayInterruptIfRunning)
        return false;
      else
        return cancelled = tasks.remove(task);
    }

    public boolean isCancelled() {
      return cancelled;
    }

    public boolean isDone() {
      return task.finished;
    }

    public V get() throws InterruptedException, ExecutionException {
      try {
        return get(1, TimeUnit.MINUTES);
      }
      catch (TimeoutException e) {
        e.printStackTrace();
        throw new ExecutionException(e);
      }
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      // NOTE: This code is not tested
      if (isCancelled())
        throw new InterruptedException("The task was cancelled");
      long startedWaiting = Clock.currentTimeMillis();
      while (!task.finished) {
        if (Clock.currentTimeMillis() - startedWaiting > unit.toMillis(timeout))
          throw new TimeoutException();
        advanceClockAndPump(100);
      }
      return task.computationResult;
    }

    @VisibleForTesting
    public ScheduledTask<V> getTask() {
      return task;
    }
  }

  /** Delegates to the future for the latest instance of the task */
  private static class RepeatingTaskFuture<V> implements ScheduledFuture<V> {
    private ScheduledFuture<V> delegate;

    public RepeatingTaskFuture(ScheduledFuture<V> delegate) {
      this.delegate = delegate;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
      return delegate.cancel(mayInterruptIfRunning);
    }

    public int compareTo(Delayed o) {
      return delegate.compareTo(o);
    }

    public V get() throws InterruptedException, ExecutionException {
      return delegate.get();
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return delegate.get(timeout, unit);
    }

    public long getDelay(TimeUnit unit) {
      return delegate.getDelay(unit);
    }

    public boolean isCancelled() {
      return delegate.isCancelled();
    }

    public boolean isDone() {
      return delegate.isDone();
    }
  }



}

