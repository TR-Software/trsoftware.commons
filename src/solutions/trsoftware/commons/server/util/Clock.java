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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.ServerConstants;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

/**
 * Can be used instead of {@link System#currentTimeMillis()} to allow testing time-based code.
 * <p>
 * This mock clock can be in one of the following states:
 * <ol>
 *   <li id="running">
 *     <b>Running</b>: returning normal system time. <br>
 *     Instances always start in this state and can return to it by calling {@link #reset()}.
 *   </li>
 *   <li>
 *     <b>Stopped</b>: returning a fixed (instrumented) time value. <br>
 *     First, call {@link #stopTime()} to enter this state, then can use the {@link #setTime(long)} / {@link #advanceTime(long)}
 *     methods to control the fixed time value.
 *   </li>
 *   <li>
 *     <b>Resumed</b>: returning an offset from normal system time. <br>
 *     This state can be entered from the "Stopped" state by calling {@link #resume()}, at which point the
 *     offset is derived automatically from the current instrumented time value.
 *   </li>
 * </ol>
 * @deprecated Superceded by {@link solutions.trsoftware.commons.shared.util.time.Clock}
 */
public class Clock extends java.time.Clock {

  @FunctionalInterface
  public interface TimeSupplier extends LongSupplier {
    long currentTimeMillis();

    @Override
    default long getAsLong() {
      return currentTimeMillis();
    }
  }

  /**
   * Provides the time returned by {@link System#currentTimeMillis()}
   * @see SystemTime#INSTANCE
   */
  public static final TimeSupplier SYSTEM_TIME_SUPPLIER = System::currentTimeMillis;

  /**
   * Provides the time returned by {@link Clock#currentTimeMillis()}
   */
  public static final TimeSupplier INSTRUMENTED_TIME_SUPPLIER = Clock::currentTimeMillis;

  /**
   * The singleton instance.
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: can use a dependency injection framework like Guice with 2 impls of this class:
   *   one instrumentable for tests, and the other not, for production
   *  </p>
   */
  // TODO: perhaps make this a ThreadLocal and remove all synchronization from class?
  private static final AtomicReference<Clock> instance = new AtomicReference<>(new Clock());

  /**
   * This fields exists only for compliance with the {@link java.time.Clock} interface; we don't actually use it.
   */
  private ZoneId zone;

  /**
   * Represents the current state of the clock, either:
   * <ol>
   *   <li>{@link SystemTime}: returning normal system time</li>
   *   <li>{@link InstrumentedTime}: returning an instrumented time value independent of the system time</li>
   *   <li>{@link OffsetTime}: returning an offset from normal system time</li>
   * </ol>
   * See the {@linkplain Clock class doc} for a complete description of these states.
   */
  private volatile State state = SystemTime.INSTANCE;

  /**
   * @return the singleton instance of this class
   */
  public static Clock getInstance() {
    return instance.get();
  }

  public Clock() {
    this(ZoneOffset.UTC);
  }

  public Clock(ZoneId zone) {
    this.zone = zone;
  }

  private Clock(ZoneId zone, State state) {
    this.zone = zone;
    this.state = state;
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    if (zone.equals(this.zone)) {  // intentional NPE
      return this;
    }
    return new Clock(zone, this.state);
  }

  /**
   * Gets the current millisecond instant of the clock.
   * <p>
   * This returns the millisecond-based instant, measured from 1970-01-01T00:00Z (UTC).
   * This is equivalent to the definition of {@link System#currentTimeMillis()}.
   * <p>
   * The return value can be instrumented for testing purposes by calling the {@link #stop()}, {@link #set(long)},
   * and {@link #advance(long)} methods.
   *
   * @return the current millisecond instant from this clock, measured from
   *  the Java epoch of 1970-01-01T00:00Z (UTC)
   */
  @Override
  public long millis() {
    return state.currentTimeMillis();
  }

  @Override
  public Instant instant() {
    return Instant.ofEpochMilli(millis());
  }

  /**
   * Should be used instead of {@link System#currentTimeMillis()}, can be instrumented
   * for testing purposes by calling the {@link #stop()} and {@link #set(long)} methods.
   *
   * @return the current millisecond instant from this clock, measured from
   *  the Java epoch of 1970-01-01T00:00Z (UTC)
   * @see #millis()
   */
  public static long currentTimeMillis() {
    return getInstance().millis();
  }

  /**
   * Causes {@link #millis()} to return a fixed (instrumented) value of time,
   * which will be fixed to the last real time value by this method,
   * but can be adjusted with {@link #setTime(long)} or {@link #advanceTime(long)}.
   * <p>
   * NOTE: the requirement that this method is always called prior to
   * {@link #advanceTime(long)}, {@link #resume()}, etc., gives us a convenient way to search
   * for all occurrences of code that stops the clock.
   *
   * @return the current time as fixed by this call
   */
  public long stopTime() {
    // ensure that the global (singleton) Clock instance can be stopped only while unit testing
    if (this == getInstance() && !ServerConstants.IS_CLOCK_STOPPABLE)
      throw new IllegalStateException("Clock.stop() cannot be called from this context.  Did you forget to inherit CanStopClock?");
    synchronized (this) {
      long value = millis();
      state = new InstrumentedTime(value);
      return value;
    }
  }

  // TODO: replace all the static methods with their instance method equivalents (via getInstance) and rename the instance methods to use the original static names

  /**
   * Causes {@link #currentTimeMillis()} to return a fixed (instrumented) value of time,
   * which is initialized to the current time value by this method,
   * but can be adjusted with {@link #set(long)} or {@link #advance(long)}.
   * <p>
   * NOTE: the requirement that this method is always called prior to
   * {@link #advance(long)}, {@link #startTicking()}, etc., gives us a convenient way to search
   * for all occurrences of code that stops the clock.
   *
   * @return the current time as fixed by this call
   */
  public static long stop() {
    return getInstance().stopTime();
  }

  /**
   * Causes {@link #millis()} to resume returning the real time.
   * @see #resetToNormal()
   */
  public void reset() {
    state = SystemTime.INSTANCE;
  }

  /** Causes {@link #currentTimeMillis()} to resume returning the real time */
  public static void resetToNormal() {
    getInstance().reset();
  }

  /**
   * Resumes the clock from the current instrumented time value.
   * Similar to {@link #offset(java.time.Clock, Duration)}.
   * @see #startTicking()
   */
  public synchronized void resume() {
    assertClockStopped();
    state = new OffsetTime(millis() - System.currentTimeMillis());
  }

  /**
   * Resumes the clock from the current value of instrumentedTime.
   * Similar to {@link #offset(java.time.Clock, Duration)}.
   */
  public static void startTicking() {
    getInstance().resume();
  }

  /**
   * Advances the instrumented time by the given number of milliseconds.
   * @see #advance(long)
   */
  public synchronized void advanceTime(long offsetMillis) {
    assertClockStopped();
    state = new InstrumentedTime(millis() + offsetMillis);
  }

  /** Advances the instrumented time by the given number of milliseconds */
  public static void advance(long offsetMillis) {
    getInstance().advanceTime(offsetMillis);
  }

  /**
   * Sets a mock time value to be returned by {@link #millis()} after {@link #stopTime()} was called
   * @see #set(long)
   */
  public synchronized void setTime(long millis) {
    assertClockStopped();
    state = new InstrumentedTime(millis);
  }

  /**
   * Sets a fake time value to be returned by {@link #currentTimeMillis()} after {@link #stop()} was called.
   *
   * @param millis the value to be returned by all subsequent invocations of {@link #currentTimeMillis()}
   */
  public static void set(long millis) {
    getInstance().setTime(millis);
  }

  /**
   * Sets a fake time value to be returned by {@link #currentTimeMillis()}
   * by first calling {@link #stop()} then {@link #set(long)}.
   *
   * @param millis the value to be returned by all subsequent invocations of {@link #currentTimeMillis()}
   */
  public static void stopAndSet(long millis) {
    stop();
    set(millis);
  }

  /**
   * @return {@code true} iff {@link #state} is an instance of {@link InstrumentedTime} (which would be the case
   * after {@link #stopTime()} has been called)
   */
  public boolean isTimeStopped() {
    return state instanceof InstrumentedTime;
  }

  public static boolean isStopped() {
    return getInstance().isTimeStopped();
  }

  /**
   * This assertion ensures that the {@link #stopTime()} method has already verified that the clock
   * {@linkplain ServerConstants#IS_CLOCK_STOPPABLE can be stopped in the current context}.
   * @throws IllegalStateException if the {@linkplain #state current state} is not an instance of {@link InstrumentedTime}
   */
  private void assertClockStopped() {
    // TODO(1/14/2020): can we get rid of this assertion?
    if (!isTimeStopped())
      throw new IllegalStateException("Clock must be stopped before calling this method");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;

    Clock clock = (Clock)o;

    return zone.equals(clock.zone) && state.equals(clock.state);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + zone.hashCode();
    result = 31 * result + state.hashCode();
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Clock{");
    sb.append("zone=").append(zone);
    sb.append(", state=").append(state);
    sb.append('}');
    return sb.toString();
  }

  /**
   * Represents the current state of the clock, either:
   * <ol>
   *   <li>returning normal system time (equivalent to {@link #systemUTC()})</li>
   *   <li>returning an offset from normal system time (equivalent to {@link #offset(java.time.Clock, Duration)})</li>
   *   <li>returning an instrumented time value independent of the system time (equivalent to {@link #fixed(Instant, ZoneId)})</li>
   * </ol>
   * @implNote Implementations should be immutable
   */
  interface State extends TimeSupplier {
  }

  /**
   * Returns the normal system time.
   * <p>
   * Immutable {@linkplain #INSTANCE singleton}.
   *
   * @see java.time.Clock#systemUTC()
   */
  static final class SystemTime implements State {

    /**
     * Singleton instance
     */
    static final SystemTime INSTANCE = new SystemTime();

    private SystemTime() {
      // private constructor enforces singleton pattern
    }

    @Override
    public long currentTimeMillis() {
      return System.currentTimeMillis();
    }

    @Override
    public String toString() {
      return "SystemTime";
    }

  }

  /**
   * Returns an offset from the normal system time.
   * <p>
   * Immutable.
   *
   * @see java.time.Clock#offset(java.time.Clock, Duration)
   */
  static final class OffsetTime implements State {
    private final long offset;

    OffsetTime(long offset) {
      this.offset = offset;
    }

    @Override
    public long currentTimeMillis() {
      return System.currentTimeMillis() + offset;
    }

    @Override
    public String toString() {
      return "OffsetTime(" + offset + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      OffsetTime that = (OffsetTime)o;

      return offset == that.offset;
    }

    @Override
    public int hashCode() {
      return (int)(offset ^ (offset >>> 32));
    }
  }

  /**
   * Returns an instrumented time value independent of the system time.
   * <p>
   * Immutable.
   *
   * @see java.time.Clock#fixed(Instant, ZoneId)
   */
  static final class InstrumentedTime implements State {
    private final long value;

    /**
     * @param value the fixed time to be returned by every invocation of {@link #currentTimeMillis()}
     */
    InstrumentedTime(long value) {
      this.value = value;
    }

    @Override
    public long currentTimeMillis() {
      return value;
    }

    @Override
    public String toString() {
      return "InstrumentedTime(" + value + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      InstrumentedTime that = (InstrumentedTime)o;

      return value == that.value;
    }

    @Override
    public int hashCode() {
      return (int)(value ^ (value >>> 32));
    }
  }


}
