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

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import solutions.trsoftware.commons.shared.util.stats.NumberSample;

/**
 * Facilitates splitting a time-consuming task into multiple increments (in order to avoid "unresponsive script" warnings
 * in the browser) by using an abstraction similar to an iterator.
 * <p>
 * The behavior of this {@linkplain Scheduler#scheduleIncremental(Scheduler.RepeatingCommand) incremental command}
 * is similar to the following loop:
 * <pre>
 *   {@link #loopStarted}();
 *   int i = 0;
 *   while ({@link #hasMoreWork}()) {
 *     {@link #loopBody}(i++);
 *   }
 *   {@link #loopFinished}(false);
 * </pre>
 * The main difference is that the body of the loop is interrupted whenever the current increment exceeds
 * the time allotted to it, and will be continued on the next increment.
 * <p>
 * Subclasses are required only to implement {@link #loopBody(int)} and {@link #hasMoreWork()}, but can also override
 * {@link #loopStarted()} / {@link #loopFinished(boolean)} and {@link #incrementStarted()} / {@link #incrementFinished(Duration)},
 * to be notified of the loop lifecycle events.  Execution can be cancelled at any time by calling {@link #stop()}.
 *
 * @author Alex
 * @see Scheduler#scheduleIncremental(Scheduler.RepeatingCommand)
 * @see IncrementalForLoop
 * @see IncrementalJob
 */
public abstract class IncrementalLoop implements Scheduler.RepeatingCommand {
  /**
   * The currently-executing loop increment will be preempted after this
   * number of milliseconds have elapsed.
   */
  private final int incrementMillis;

  /**
   * Incremented every time the loop body is executed, like the index variable in a {@code for} loop.
   */
  private int count;

  /**
   * For reporting and testing how the loop performed.
   * This number sample will contain the millis duration of each executed increment.
   */
  private NumberSample<Integer> incrementDurations = new NumberSample<>();

  private boolean started;

  /**
   * Can be used to stop all future executions of the loop.
   */
  private boolean stopped;

  /**
   * @param incrementMillis execution will be preempted after this duration has elapsed, and the loop will resume
   * on the next execution
   * @see IncrementalLoop
   */
  public IncrementalLoop(int incrementMillis) {
    this.incrementMillis = incrementMillis;
  }

  public final boolean execute() {
    if (stopped) {
      // interrupted externally
      if (started)
        loopFinished(true);
      return false;
    }
    if (!started) {
      loopStarted();
      started = true;
    }
    incrementStarted();
    Duration incrementDuration = new Duration();
    while (hasMoreWork()) {
      /*
       NOTE: we execute the body before checking the elapsed time in order to ensure that the
       loop makes progress on each iteration (otherwise it could run forever).
      */
      loopBody(computeLoopVariable());
      count++;
      if (incrementDuration.elapsedMillis() > incrementMillis) {
        // this increment has been running too long and needs to be preempted
        finishIncrement(incrementDuration);
        if (hasMoreWork())
          return true;  // to be continued on the next increment
        else {
          loopFinished(false);
          return false;
        }
      }
    }
    // the loop finished without having been preempted - no more work left to do
    finishIncrement(incrementDuration);
    loopFinished(false);
    return false;  // done
  }

  /**
   * Optionally transforms the ordinal number of the current iteration into the argument for {@link #loopBody(int)}
   */
  protected int computeLoopVariable() {
    return count;
  }

  /**
   * Checks whether the loop has finished processing all iterations.
   * <p>
   * NOTE: a return value of {@code false} doesn't imply that the loop is still running, because, for example,
   * it might have been interrupted prematurely with {@link #stop()}.  To check if it's still scheduled for execution,
   * you could evaluate the following expression:
   * <pre>{@link #isStarted}() && !({@link #isFinished}() || {@link #isStopped}())</pre>
   *
   * @return {@code true} if the loop finished processing all iterations without having been {@linkplain #stop() interrupted}
   * (or if it never had {@linkplain #hasMoreWork() any work to do} in the first place)
   */
  public final boolean isFinished() {
    return !hasMoreWork();
  }

  /**
   * The loop expression: will keep executing while this evaluates to {@code true}
   *
   * @return {@code true} if the loop finished processing all iterations
   */
  protected abstract boolean hasMoreWork();

  /**
   * Subclasses may override this method to be notified every time a new increment has started executing.
   */
  protected void incrementStarted() {  }

  private void finishIncrement(Duration incrementDuration) {
    incrementDurations.update(incrementDuration.elapsedMillis());
    incrementFinished(incrementDuration);
  }

  /**
   * Subclasses may override this method to be notified every time an increment has finished executing,
   * either because the loop is finished or its allotted time was exceeded.
   */
  protected void incrementFinished(Duration incrementDuration) {
  }

  /**
   * Execute the next iteration of the loop.
   *
   * @param i the value of the loop variable on this iteration.
   */
  protected abstract void loopBody(int i);

  /**
   * Subclasses may override this method to implement logic to be executed before the loop has started.
   */
  protected void loopStarted() {  }

  /**
   * Subclasses may override this method to implement logic to be executed after the loop has terminated.
   *
   * @param interrupted {@code true} if the loop was interrupted (via the {@link #stop()} method) before finishing
   * all iterations
   */
  protected void loopFinished(boolean interrupted) {  }

  /**
   * @return {@code true} if the {@link #execute()} method was invoked at least once
   */
  public boolean isStarted() {
    return started;
  }

  /**
   * @return {@code true} iff the task was interrupted prematurely (via the {@link #stop()} method) before the loop has finished
   * @see #stop()
   */
  public boolean isStopped() {
    return stopped;
  }

  /**
   * Stop all future executions of the loop, even if it's not {@linkplain #isFinished() finished}.
   */
  public void stop() {
    stopped = true;
  }

  /**
   * @return the number of iterations that have been executed so far
   */
  public int getIterationCount() {
    return count;
  }

  /**
   * @return the number of increments that have been executed
   */
  public int getIncrementCount() {
    return incrementDurations.size();
  }

  public NumberSample<Integer> getIncrementDurations() {
    return incrementDurations;
  }

}
