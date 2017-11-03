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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.io.StringPrintStream;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A ScheduledExecutorService that uses the mocked Clock class instead of the
 * System clock, so that the execution delays may be artificially manipulated
 * when running unit tests or simulations.
 *
 * Unlike typical scheduled executors, this class is event-driven, and
 * (single-threaded) and its event loop must be externally pumped
 * by repeatedly calling the pumpEvents() method.  The scheduled
 * tasks will be executed inline by this method. To immitate the behavior
 * of a multithreaded executor you can call pumpEvents() from an external timer.
 *
 * @author Alex
 */
public class MockScheduledExecutorService implements ScheduledExecutorService {
  private NavigableSet<ScheduledTask> tasks = new ConcurrentSkipListSet<ScheduledTask>();

  public MockScheduledExecutorService() {

  }

  /** Inititializes to contain the same tasks as the given executor */
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
   * Unlike advanceClockAndPump, which advances the clock in one go, this method
   * advances the mock clock incrementally, 1/64 th of the remaining interval
   * on each iteration (or 1 millis whichever is larger),
   * invoking all the tasks scheduled up to that point.
   * The clock is guaranteed to stop exactly at the target time,
   * because we'll be pumping 1 ms at a time toward the end of the iterval.
   *
   * This ensures that the scheduled events execute as close to their target time as possible.
   * 
   * @param millis
   */
  public void advanceClockAndPumpIncrementally(long millis) {
    advanceClockAndPumpIncrementallyTo(Clock.currentTimeMillis() + millis);
  }

  /**
   * Unlike similar to advanceClockAndPumpIncrementally, except instead
   * of specifying the offset, you specify the target time to advance the clock
   * to.
   *
   * This ensures that the scheduled events execute as close to their target time as possible.
   * @param targetTime
   */
  public void advanceClockAndPumpIncrementallyTo(long targetTime) {
    // tick through the clock, 1/64 th of the interval at a time, until we reach the target date
    // this means that only the last 64 millis will be ticked through 1 millis at the time
    // (NOTE: can't tick one mills at a time because large gaps in the logs would take a really long time)
    int tickCount = 0;
    long timeGap = targetTime - Clock.currentTimeMillis();
    long now;
    while ((now = Clock.currentTimeMillis()) < targetTime) {
      long delta = Math.max(1, (targetTime - now) >> 6);
      if (now + delta*2 > targetTime) {
        // if we're less than 2 deltas away from the target time, then might as well just jump the clock to the target time (to avoid falling short)
        Clock.set(targetTime);
      }
      else {
        Clock.advance(delta);
      }
      tickCount++;
      pumpEvents();
      // all the tasks scheduled up until this time should have finished
      assert oldestUnfinishedTaskTime() > Clock.currentTimeMillis(): "oldestUnfinishedTaskTime() > " + new Date(Clock.currentTimeMillis()) + "\n" + dumpTasks();
    }
    // debugging statnement
//    System.out.printf("Advanced clock over time gap of %d millis using %d ticks%n", timeGap, tickCount);
  }

  public void pumpEvents() {
    // run all the tasks whose time has come (or passed)
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

  public ScheduledFuture<?> schedule(final Runnable command, long delay, TimeUnit unit) {
    return scheduleTask(new ScheduledRunnableTask(command,  Clock.currentTimeMillis() + unit.toMillis(delay)));
  }

  public <V> ScheduledFuture<V> schedule(final Callable<V> callable, long delay, TimeUnit unit) {
    return scheduleTask(new ScheduledCallableTask(callable, Clock.currentTimeMillis() + unit.toMillis(delay)));
  }

  public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, long initialDelay, final long period, final TimeUnit unit) {
    return scheduleRepeatingTask(new RepeatingTask(command, Clock.currentTimeMillis() + unit.toMillis(initialDelay), period, unit));
  }

  private <V> ScheduledFuture<V> scheduleTask(ScheduledTask task) {
    tasks.add(task);
    return new MockScheduledFuture<V>(task);
  }

  private ScheduledFuture<?> scheduleRepeatingTask(RepeatingTask task) {
    ScheduledFuture<?> taskFuture = scheduleTask(task);
    task.repeatingFuture = new RepeatingTaskFuture(taskFuture);
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

  }

  public List<Runnable> shutdownNow() {
    return Collections.emptyList();
  }

  public boolean isShutdown() {
    throw new UnsupportedOperationException("Method .isShutdown has not been fully implemented yet.");
  }

  public boolean isTerminated() {
    throw new UnsupportedOperationException("Method .isTerminated has not been fully implemented yet.");
  }

  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return true;
  }

  public <T> Future<T> submit(Callable<T> task) {
    throw new UnsupportedOperationException("Method .submit has not been fully implemented yet.");
  }

  public <T> Future<T> submit(Runnable task, T result) {
    throw new UnsupportedOperationException("Method .submit has not been fully implemented yet.");
  }

  public Future<?> submit(Runnable task) {
    throw new UnsupportedOperationException("Method .submit has not been fully implemented yet.");
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
    throw new UnsupportedOperationException("Method .execute has not been fully implemented yet.");
  }

  /** @returns a list of currently scheduled tasks */
  public List<ScheduledTask> examineTasks() {
    return new ArrayList<ScheduledTask>(tasks);
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

  private abstract class ScheduledTask<V> implements Runnable, Comparable {
    /** Used to deterministically resolve timing collisions (see compareTo) */
    protected final Long id = nextSequenceNumber.getAndIncrement();
    protected final String name;
    protected final Long time;
    private boolean running;
    private boolean finished;
    protected V computationResult;

    private ScheduledTask(long time, String name) {
      this.time = time;
      this.name = name;
    }

    public int compareTo(Object o) {
      ScheduledTask otherTask = (ScheduledTask)o;
      int result = this.time.compareTo(otherTask.time);
      // make sure the result is nonzero if the two objects are not acually equal
      // (because the ConcurrentSkipListSet treats comparison=0 as the elements being equal, and will replace one with the other)
      if (result == 0 && !this.equals(o))
        return id.compareTo(otherTask.id);  // use the unambiguous creation order to resolve conflict deterministically
      return result;
    }
  }

  private class ScheduledRunnableTask extends ScheduledTask {
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
      ScheduledFuture<Object> taskFuture = scheduleTask(task);
      task.repeatingFuture = repeatingFuture;
      // point the repeating future to the latest instance of the task
      repeatingFuture.delegate = taskFuture;
    }
  }


  private class MockScheduledFuture<V> implements ScheduledFuture<V> {
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
      return new Long(getDelay(TimeUnit.MILLISECONDS)).compareTo(o.getDelay(TimeUnit.MILLISECONDS));
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
  }

  /** Delegates to the future for the latest instance of the task */
  private class RepeatingTaskFuture<V> implements ScheduledFuture<V> {
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

