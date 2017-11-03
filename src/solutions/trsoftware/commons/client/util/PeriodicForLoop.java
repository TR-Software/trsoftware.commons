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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Scheduler;

/**
 * An implementation of {@link Scheduler.RepeatingCommand} suitable for use with
 * {@link Scheduler#scheduleFixedDelay(Scheduler.RepeatingCommand, int)} and
 * {@link Scheduler#scheduleFixedPeriod(Scheduler.RepeatingCommand, int)}.
 *
 * Each invocation of {@link #execute()} updates an internal loop counter,
 * invokes {@link #onIteration(int)} (where the subclass should implement its loop body logic),
 * and returns {@code true} as long as the counter is less than {@link #limit}.
 *
 * Instances of this class can be reused as long as the instance methods {@link #scheduleFixedDelay(int)} and
 * {@link #scheduleFixedPeriod(int)} are used to run the loop instead of their counter-parts defined in {@link Scheduler}.
 *
 * @author Alex
 */
public abstract class PeriodicForLoop implements Scheduler.RepeatingCommand {
  private final int start;
  private final int limit;
  private final int step;
  /**
   * The loop variable.
   */
  private int i;

  private boolean running;

  /**
   * Equivalent to {@code for (int i = start; i < limit; i+=step)}
   */
  public PeriodicForLoop(int start, int limit, int step) {
    this.start = start;
    i = start;
    this.limit = limit;
    this.step = step;
  }

  /**
   * Equivalent to {@code for (int i = 0; i < limit; i++)}
   */
  public PeriodicForLoop(int limit) {
    this(0, limit, 1);
  }

  public final boolean execute() {
    running = true;
    onIteration(i);
    i+=step;
    if (i >= limit) {
      onFinished();
      running = false;
    }
    return running;
  }

  public boolean isRunning() {
    return running;
  }

  /**
   * Subclasses should override this method to implement the loop body logic.
   * @param i The value of the loop variable on this iteration.
   */
  protected abstract void onIteration(int i);


  /**
   * Subclasses could override this method to implement logic to be executed
   * once after the loop has terminated.
   */
  protected void onFinished() {

  }

  public void scheduleFixedDelay(int millis) {
    maybeStart();
    Scheduler.get().scheduleFixedDelay(this, millis);
  }

  public void scheduleFixedPeriod(int millis) {
    maybeStart();
    Scheduler.get().scheduleFixedPeriod(this, millis);
  }

  private void maybeStart() {
    if (running)
      throw new IllegalStateException(getClass().getName() + " already running");
    running = true;
    i = start;
  }
}
