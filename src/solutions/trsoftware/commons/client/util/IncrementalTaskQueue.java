package solutions.trsoftware.commons.client.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * {@linkplain Scheduler#scheduleIncremental Incrementally} executes a sequence of commands.
 * @author Alex
 * @since 8/23/2023
 */
public class IncrementalTaskQueue<T extends Command> implements Scheduler.RepeatingCommand {

  protected final Queue<T> queue;
  private boolean running;

  public IncrementalTaskQueue(Queue<T> queue) {
    // TODO: make this constructor private or protected, in favor of IncrementalTaskQueue(Supplier), to discourage leaking the queue
    this.queue = queue;
  }

  public IncrementalTaskQueue(Supplier<Queue<T>> queueSupplier) {
    this.queue = queueSupplier.get();
  }

  public IncrementalTaskQueue() {
    this(new ArrayDeque<>());
  }

  public IncrementalTaskQueue<T> add(@Nonnull T task) {
    queue.add(requireNonNull(task, "task"));
    startIfNotRunning();
    return this;
  }

  /**
   * Schedules this queue to run as an
   * {@linkplain Scheduler#scheduleIncremental(Scheduler.RepeatingCommand) incremental command}, if it's not empty
   * and not already running.
   * @return {@code true} if started by this method or was already running;
   *         {@code false} if unable to start because the queue is empty
   *
   */
  public boolean startIfNotRunning() {
    if (!running && !queue.isEmpty()) {
      getScheduler().scheduleIncremental(this);
      return running = true;
    }
    return running;
    /* TODO(10/9/2025): this revives the job even if handleFailedTask returned false on a previous iteration
         - is this the desired outcome?
     */
  }

  /**
   * Exposed for testing: can override to use a {@link com.google.gwt.core.client.testing.StubScheduler} instead
   * of the default {@link Scheduler#get()} implementation.
   */
  @VisibleForTesting
  Scheduler getScheduler() {
    return SchedulerUtils.getScheduler();
  }

  /**
   * Executes the next task in the queue.
   * <p>
   * <b>Caution:</b> this method should only be called by {@link Scheduler}, otherwise the {@linkplain #running internal state}
   * of this instance could become compromised, risking the possibility of a subsequent call to {@link #add(Command)}
   * scheduling this job again, even if it is already scheduled.
   *
   * @return {@code true} if the queue is not empty and {@link #handleFailedTask(Command, Throwable)} didn't return false
   *   if the task failed
   */
  @Override
  public boolean execute() {
    if (!queue.isEmpty()) {
      T task = queue.poll();
      assert task != null;
      if (!executeTask(task))
        return running = false;
    }
    return running = !queue.isEmpty();
  }

  /**
   * Executes the given task in a {@code try-catch} block.  If the task throws an exception,
   * will invoke {@link #handleFailedTask(Command, Throwable)} and return the result of that call.
   * Otherwise (if no exception thrown), will return {@code true}.
   *
   * @return {@code true} to continue executing tasks
   *           (if task succeeded or {@link #handleFailedTask} returned {@code true});
   *         {@code false} to stop the incremental job
   *           (if task failed and {@link #handleFailedTask} returned {@code false})
   */
  protected boolean executeTask(T task) {
    try {
      // NOTE: without this try/catch, an uncaught exception would cause this incremental job to be cancelled by SchedulerImpl
      task.execute();
      return true;
    }
    catch (Throwable ex) {
      return handleFailedTask(task, ex);
    }
  }

  /**
   * Invoked by the {@link #execute()} method when a task throws an exception.
   * This hook gives subclasses the opportunity to specify custom exception handling logic (e.g. retry the task,
   * modify the {@link #queue}, etc.) and decide whether the incremental job should keep running.
   *
   * @param task the failed task
   * @param ex the exception thrown by the task's {@link Command#execute()} method
   * @return {@code true} to continue executing tasks or {@code false} to stop the incremental job until the next invocation
   * of {@link #add(Command)} or {@link #startIfNotRunning()}
   */
  public boolean handleFailedTask(T task, Throwable ex) {
    GWT.reportUncaughtException(ex);
    return !queue.isEmpty();
  }

  /**
   * @return the number of pending tasks that have not yet been executed
   */
  public int size() {
    return queue.size();
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  public boolean isRunning() {
    return running;
  }

  @VisibleForTesting
  protected Queue<T> getQueue() {
    return queue;
  }

  /**
   * @return an immutable list of tasks currently awaiting execution
   */
  @VisibleForTesting
  public ImmutableList<T> examineTasks() {
    return ImmutableList.copyOf(queue);
  }
}
