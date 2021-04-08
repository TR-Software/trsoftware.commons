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

import com.google.gwt.user.client.Timer;

import static com.google.gwt.core.client.Duration.currentTimeMillis;

/**
 * A subclass of {@link com.google.gwt.user.client.Timer} with the following embellishments:
 * <ol>
 *   <li>can tell you its current status (via {@link #isScheduled()}, {@link #isRepeating()}, and {@link #getPeriodMillis})</li>
 *   <li>can tell you when it will fire next (via {@link #getNextFiringTime()})</li>
 * </ol>
 * @author Alex
 */
public abstract class SmartTimer extends Timer {

  private enum State {NOT_SCHEDULED, SCHEDULED_ONCE, REPEATING, REPEATING_AFTER_DELAY}

  /** Approximate time when this timer will fire next */
  private double nextFiringTime;
  private int periodMillis;
  private State state = State.NOT_SCHEDULED;

  private void init(int nextFiringDelay, int periodMillis, State state) {
    setNextFiringTime(nextFiringDelay);
    this.periodMillis = periodMillis;
    this.state = state;
  }

  private void setNextFiringTime(int nextFiringDelay) {
    nextFiringTime = currentTimeMillis() + nextFiringDelay;
  }

  private void clear() {
    nextFiringTime = 0;
    periodMillis = 0;
    state = State.NOT_SCHEDULED;
  }

  @Override
  public void schedule(int delayMillis) {
    super.schedule(delayMillis);
    init(delayMillis, 0, State.SCHEDULED_ONCE);
  }

  @Override
  public void scheduleRepeating(int periodMillis) {
    super.scheduleRepeating(periodMillis);
    init(periodMillis, periodMillis, State.REPEATING);
  }

  /**
   * Schedules the timer to initially fire initialDelayMills from now, and then repeat at intervals of periodMillis.
   */
  public void scheduleRepeating(int initialDelayMills, int periodMillis) {
    super.schedule(initialDelayMills);
    init(initialDelayMills, periodMillis, State.REPEATING_AFTER_DELAY);
  }

  @Override
  public void cancel() {
    clear();
    super.cancel();
  }

  @Override
  public final void run() {
    switch (state) {
      case REPEATING_AFTER_DELAY:
        scheduleRepeating(periodMillis);  // this must be the firing after the initial delay
        break;
      case REPEATING:
        setNextFiringTime(periodMillis);  // update the next firing time
        break;
      default:
        clear(); // this timer will not fire again
        break;
    }
    doRun();
  }

  /**
   * This method will be called when a timer fires. Override it to implement the
   * timer's logic.
   */
  public abstract void doRun();

  /**
   * @return A timestamp that approximates when the timer is scheduled to run next, or 0 if this timer is not currently
   * scheduled to fire.
   */
  public double getNextFiringTime() {
    return nextFiringTime;
  }

  /**
   * @return true iff this time will fire at least once more.
   */
  public boolean isScheduled() {
    return nextFiringTime != 0;
  }

  /**
   * @return the delay between consecutive firings of this timer, or 0 if it's not scheduled to repeat.
   */
  public int getPeriodMillis() {
    return periodMillis;
  }

  /**
   * @return true iff this time will keep firing periodically.
   */
  public boolean isRepeating() {
    return periodMillis != 0;
  }
}
