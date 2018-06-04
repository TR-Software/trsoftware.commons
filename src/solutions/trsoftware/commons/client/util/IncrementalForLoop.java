/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;

/**
 * Runs a for loop in one or more increments as needed to avoid
 * "unresponsive script" warnings in the browser.
 *
 * Subclasses should implement the loop body logic in {@link #loopBody(int)}
 *
 * NOTE: We also have {@link PeriodicForLoop}, which is a similar class that runs only 1 iteration per time increment.
 *
 * @author Alex
 */
public abstract class IncrementalForLoop implements Scheduler.RepeatingCommand {
  private int limit;
  private int step;
  /**
   * The currently-executing loop increment will be pre-empted after this
   * many milliseconds have elapsed.
   */
  private int incrementMillis;

  /**
   * The last value of the loop variable before the loop was interrupted
   * i.e. if lastValue=5, then the iteration of the loop with i=5 has
   * already been executed.
   */
  private int lastValue;

  /** For reporting and testing how the loop performed */
  private int incrementsExecuted = 0;
  private boolean finished = false;

  /**
   * Equivalent to for (int i = start; i < limit; i+=step)
   * @param limit
   */
  public IncrementalForLoop(int start, int limit, int step, int incrementMillis) {
    this.limit = limit;
    this.step = step;
    this.incrementMillis = incrementMillis;
    lastValue = start-step;
  }

  /**
   * Equivalent to for (int i = 0; i < limit; i++) which will be pre-empted
   * every 1000 milliseconds;
   * @param limit
   */
  public IncrementalForLoop(int limit) {
    this(0, limit, 1, 1000);
  }

  public final boolean execute() {
    incrementsExecuted++;
    Duration incrementDuration = new Duration();
    for (int i = lastValue+step; (step > 0 && i < limit) || (step < 0 && i > limit); i+=step) {
      // invoking the body before checking the time ensures that the
      // loop makes progress on each iteration (otherwise it could run forever)
      loopBody(i);
      if (incrementDuration.elapsedMillis() > incrementMillis) {
        // this increment has been running too long and needs to be pre-empted
        lastValue = i;  // save the counter value
        return true;
      }
    }
    // the loop finished without having been pre-empted - no more work left to do
    loopFinished();
    finished = true;
    return false;
  }

  public int getIncrementsExecuted() {
    return incrementsExecuted;
  }

  public boolean isFinished() {
    return finished;
  }

  /**
   * Subclasses should override this method to implement the loop body logic.
   * @param i The value of the loop variable on this iteration.
   */
  protected abstract void loopBody(int i);

  /**
   * Subclasses should override this method to implement logic to be executed
   * once after the loop has terminated.
   */
  protected abstract void loopFinished();
}
