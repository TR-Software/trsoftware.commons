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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Combines multiple sequential {@linkplain Task tasks} (that can each have multiple steps)
 * into a single {@linkplain Scheduler#scheduleIncremental(Scheduler.RepeatingCommand) incremental command}.
 * <p>
 * During each execution, the job executes as many {@linkplain Task#next() chunks of work} as it can in
 * the allotted time, and will resume with the next step on the next increment.
 * <p>
 * This abstraction is similar to processing all the elements in a sequence of iterators.
 *
 * @author Alex
 * @since 5/26/2021
 * @see IncrementalLoop
 * @see Scheduler#scheduleIncremental(Scheduler.RepeatingCommand)
 */
public class IncrementalJob extends IncrementalLoop {

  /**
   * The task sequence
   */
  private List<Task> tasks;

  private Iterator<Task> taskIterator;
  private Task currentTask;

  /**
   * We use this state variable to minimize the number of times <code>currentTask.{@link Task#hasNext() hasNext()}</code>
   * needs to be called during the job execution and to avoid executing any more iterations than necessary.
   * <p>
   * This idea was borrowed from {@link com.google.common.collect.AbstractIterator}
   */
  private State state = State.NOT_READY;

  private enum State {
    /** We don't yet know what chunk to execute next or have already executed it */
    NOT_READY,
    /** We have determined what chunk to execute next but haven't executed it yet */
    READY,
    /** We have reached the end of the tasks and are finished */
    DONE,
  }

  /**
   * This constructor should be used when the list of tasks is already available.
   * Otherwise, subclasses may use the {@link #IncrementalJob(int)} constructor and call {@link #initTasks(List)} later.
   *
   * @param tasks the sequence of tasks to be executed
   * @param incrementMillis execution will be preempted after this duration has elapsed, and the job will resume
   * with the next {@linkplain Task#next() chunk of work} on the next increment
   */
  public IncrementalJob(int incrementMillis, List<Task> tasks) {
    super(incrementMillis);
    initTasks(tasks);
  }

  /**
   * This constructor is made available for subclassing.
   * When when using this constructor, the subclass must call {@link #initTasks(List)} from its own constructor.
   *
   * @param incrementMillis execution will be preempted after this duration has elapsed, and the job will resume
   * with the next {@linkplain Task#next() chunk of work} on the next increment
   * @see #IncrementalJob(int, List)
   */
  protected IncrementalJob(int incrementMillis) {
    super(incrementMillis);
  }

  @Override
  protected final void loopStarted() {
    checkInit();
    jobStarted();
  }

  protected void initTasks(List<Task> tasks) {
    this.tasks = tasks;
    taskIterator = tasks.iterator();
    currentTask = taskIterator.next(); // will throw exception if the task list is empty
    state = State.NOT_READY;
  }

  private void checkInit() {
    if (tasks == null)
      throw new IllegalStateException("initTasks() hasn't been called yet");
  }

  /**
   * Subclasses may override this method to implement logic to be executed
   * when the job starts.
   */
  protected void jobStarted() {  }

  /**
   * Subclasses may override this method to implement logic to be executed
   * after the loop has terminated.
   *
   * @param interrupted {@code true} if the loop was interrupted (via the {@link #stop()} method) before finishing
   *     all iterations
   */
  @Override
  protected final void loopFinished(boolean interrupted) {
    jobFinished(interrupted);
  }

  /**
   * Subclasses may override this method to implement logic to be executed
   * after the job has terminated.
   *
   * @param interrupted {@code true} if the loop was interrupted (via the {@link #stop()} method) before finishing
   *     all iterations
   */
  protected void jobFinished(boolean interrupted) {  }

  @Override
  public final boolean hasMoreWork() {
    switch (state) {
      case DONE:
        return false;
      case READY:
        return true;
    }
    return maybeAdvance();
  }

  /**
   * Ensures that {@link #currentTask} is a task that has more work to do, advancing {@link #taskIterator} if needed.
   * The {@link #state} value at the end of this method call will be either {@link State#READY READY} or {@link State#DONE DONE}.
   *
   * @return {@code true} if successful ({@link #currentTask} has more work to do) or {@code false} if all tasks finished.
   */
  private boolean maybeAdvance() {
    checkInit();
    // NOTE: this code was designed to minimize the number of times currentTask.hasNext() needs to be called during the job execution
    if (currentTask.hasNext()) {
      state = State.READY;
      return true;
    }
    while (taskIterator.hasNext()) {
      currentTask = taskIterator.next();
      if (currentTask.hasNext()) {
        state = State.READY;
        return true;
      }
    }
    state = State.DONE;
    return false;
  }

  @Override
  protected final void loopBody(int i) {
    if (hasMoreWork()) {
      state = State.NOT_READY;
      currentTask.next();
    }
  }

  public Task getCurrentTask() {
    return currentTask;
  }

  public List<Task> getTasks() {
    return Collections.unmodifiableList(tasks);
  }

  /**
   * An iterator-like interface for a task that can be split into one or more sequential steps.
   */
  public interface Task {
    /**
     * @return {@code true} if there's more work to do
     */
    boolean hasNext();

    /**
     * Perform the next chuck of work
     */
    void next();
  }

  /**
   * Implements a {@linkplain Task task} with only a single chunk of work.
   * <p>
   * Subclasses just have to implement {@link #execute()}.
   */
  public static abstract class SingletonTask implements Task, Command {
    private boolean executed;
    @Override
    public final boolean hasNext() {
      return !executed;
    }
    @Override
    public final void next() {
      execute();
      executed = true;
    }
  }

  /**
   * Implements a {@linkplain Task task} that processes the elements returned by an iterator.
   * <p>
   * Subclasses just have to implement {@link #accept(Object)}.
   */
  public static abstract class IteratorTask<T> implements Task, Consumer<T> {
    private final Iterator<T> it;

    public IteratorTask(@Nonnull Iterator<T> iterator) {
      this.it = iterator;
    }

    public IteratorTask(@Nonnull Iterable<T> iterable) {
      this(iterable.iterator());
    }

    @Override
    public final boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public void next() {
      accept(it.next());
    }
  }
}
