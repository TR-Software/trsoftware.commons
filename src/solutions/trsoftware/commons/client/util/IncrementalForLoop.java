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

/**
 * Facilitates splitting a time-consuming task into multiple increments (in order to avoid "unresponsive script" warnings
 * in the browser) by using an abstraction similar to an indexed {@code for} loop.
 * <p>
 * The behavior of this {@linkplain Scheduler#scheduleIncremental(Scheduler.RepeatingCommand) incremental command}
 * is similar to the following loop:
 * <pre>
 *   {@link #loopStarted}();
 *   for (int i = start; (step > 0 && i < limit) || (step < 0 && i > limit); i+=step) {
 *     {@link #loopBody}(i);
 *   }
 *   {@link #loopFinished}(false);
 * </pre>
 * The main difference is that the body of the loop will be interrupted whenever the current increment exceeds
 * the time allotted to it, and will be continued on the next increment.
 * <p>
 * Subclasses are required only to implement {@link #loopBody(int)}, but can also override {@link #loopStarted()}
 * / {@link #loopFinished(boolean)} and {@link #incrementStarted()} / {@link #incrementFinished(Duration)},
 * to be notified of the loop lifecycle events.  Execution can be cancelled at any time by calling {@link #stop()}.
 * <p>
 * NOTE: We also have {@link PeriodicForLoop}, which is a similar class that runs only 1 iteration per time increment.
 *
 * @author Alex
 * @see IncrementalLoop
 * @see IncrementalJob
 */
public abstract class IncrementalForLoop extends IncrementalLoop {
  private final int start;
  private final int limit;
  private final int step;

  /**
   * Creates an {@linkplain Scheduler#scheduleIncremental(Scheduler.RepeatingCommand) incremental} version of the following loop:
   * <pre>
   *   for (int i = start; (step > 0 && i < limit) || (step < 0 && i > limit); i+=step) {
   *     {@link #loopBody}(i);
   *   }
   * </pre>
   * @param start initial value for the loop variable
   * @param limit limiting value for the loop variable
   * @param step will be added to the loop variable after each iteration
   * @param incrementMillis execution will be preempted after this duration has elapsed, and the loop will resume
   * on the next execution
   */
  public IncrementalForLoop(int start, int limit, int step, int incrementMillis) {
    super(incrementMillis);
    this.start = start;
    this.limit = limit;
    this.step = step;
    /*
      TODO: throw an exception if the given params would create an infinite loop?
        For example: if (distance(start, limit) + step) > distance(start, limit))  // i.e. moving farther away from limit with each step
     */
  }

  /**
   * Creates an {@linkplain Scheduler#scheduleIncremental(Scheduler.RepeatingCommand) incremental} version of the following loop:
   * <pre>
   *   for (int i = 0; i < limit; i++) {
   *     {@link #loopBody}(i);
   *   }
   * </pre>
   * The loop's execution will be paused every 1000 milliseconds.
   * @see #IncrementalForLoop(int, int, int, int)
   */
  public IncrementalForLoop(int limit) {
    this(0, limit, 1, 1000);
  }

  @Override
  public boolean hasMoreWork() {
    int i = computeLoopVariable();
    if (step > 0)
      return i < limit;
    if (step < 0)
      return i > limit;
    return false;
  }

  @Override
  protected int computeLoopVariable() {
    int i = getIterationCount();
    return start + i * step;
  }

}
