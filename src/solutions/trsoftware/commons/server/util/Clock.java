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

import solutions.trsoftware.commons.server.ServerConstants;

import java.util.concurrent.atomic.AtomicReference;

// TODO: use Guice or something with 2 impls of this class - one instrumentable for tests, and the other not, for production
public class Clock {


  public interface TimeFunction {
    long currentTimeMillis();
  }

  public static final TimeFunction SYSTEM_TIME_FCN = System::currentTimeMillis;

  public static final TimeFunction INSTRUMENTED_TIME_FCN = Clock::currentTimeMillis;

  /** Overrides the actual time for testing purposes */
  private static final AtomicReference<Long> instrumentedTime = new AtomicReference<>();

  /** If not null, returns real time + offset instead of instrumentedTime */
  private static final AtomicReference<Long> offset = new AtomicReference<>();

  /**
   * Should be used instead of Clock.currentTimeMillis(), can be instrumented
   * for testing purposes by calling the stop() and set() methods.
   */
  public static long currentTimeMillis() {
    if (offset.get() != null)
      return System.currentTimeMillis() + offset.get();
    else {
      Long instrumentedValue = instrumentedTime.get();
      if (instrumentedValue != null)
        return instrumentedValue;
    }
    return System.currentTimeMillis();
  }

  /**
   * Causes currentTimeMillis() to return an instrumented value of time,
   * for the current thread, which is initialized to the last real time value
   * by this method but can be adjusted with set() or advance.
   *
   * NOTE: the requirement that this method is always called prior to
   * advance, startTicking, etc., gives us a convenient way to search
   * for all occurrences of code that stops the clock.
   *
   * @return the current time as fixed by this call
   */
  public static long stop() {
    // assert that the Clock should only ever be stopped while unit testing
    if (!ServerConstants.IS_CLOCK_STOPPABLE)
      throw new IllegalStateException("Clock.stop() cannot be called from this context.  Did you forget to inherit CanStopClock?");
    instrumentedTime.set(Clock.currentTimeMillis());
    offset.set(null);
    return instrumentedTime.get();
  }

  /** Causes currentTimeMillis() to resume returning the real time */
  public static void resetToNormal() {
    instrumentedTime.set(null);
    offset.set(null);
  }

  /** Resumes the clock from the current value of instrumentedTime */
  public static void startTicking() {
    if (instrumentedTime.get() == null)
      throw new IllegalStateException("Clock must be stopped before calling startTicking()");
    offset.set(instrumentedTime.get() - System.currentTimeMillis());
    instrumentedTime.set(null);
  }

  /** Advances the clock by the given number of milliseconds */
  public static void advance(long millis) {
    if (instrumentedTime.get() == null)
      throw new IllegalStateException("Clock must be stopped before calling advance()");
    instrumentedTime.set(instrumentedTime.get() + millis);
  }

  /** Sets a fake time value to be returned by currentTimeMillis after stop() was called */
  public static void set(long millis) {
    if (instrumentedTime.get() == null)
      throw new IllegalStateException("Clock must be stopped before calling set()");
    instrumentedTime.set(millis);
  }
}
