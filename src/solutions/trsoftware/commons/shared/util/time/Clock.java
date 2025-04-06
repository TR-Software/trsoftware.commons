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

package solutions.trsoftware.commons.shared.util.time;

import com.google.common.base.Ticker;
import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.concurrent.AtomicUtils;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * A static class that can be used instead of {@link System#currentTimeMillis()} and {@link System#nanoTime()} to
 * allow altering their behavior for unit testing.
 *
 * @author Alex
 * @since 12/31/2022
 */
public class Clock {
  private static final NormalTime NORMAL_TIME = new NormalTime();
  private static final TickerImpl TICKER = new TickerImpl();

  private static final AtomicReference<TimeState> state = new AtomicReference<>(NORMAL_TIME);


  public static long currentTimeMillis() {
    return state.get().currentTimeMillis();
  }

  public static long nanoTime() {
    return state.get().nanoTime();
  }

  public static Ticker ticker() {
    return TICKER;
  }

  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  @GwtIncompatible
  public static Instant instant() {
    return Instant.ofEpochMilli(currentTimeMillis());
  }

  /**
   * Causes {@link #currentTimeMillis()} and {@link #nanoTime()} to start returning returning fixed values
   * that are initialized to their current real values, but can be adjusted with {@link #set(long, long)}
   * or {@link #advance(long, TimeUnit)}.
   * This method has no effect if {@linkplain #isStopped() already stopped}
   * <p>
   * NOTE: the requirement that this method is always called prior to
   * {@link #advance(long, TimeUnit)}, {@link #resume()}, etc., provides a convenient way to search
   * for all occurrences of code that stops the clock.
   */
  public static void stop() {
    updateState(TimeState::stop);
  }


  /**
   * Causes {@link #currentTimeMillis()} and {@link #nanoTime()} to resume returning the real values of
   * {@link System#currentTimeMillis()} and {@link System#nanoTime()}
   */
  public static void resetToNormal() {
    state.set(NORMAL_TIME);
  }

  /**
   * Resumes the clock from its current time values.
   * Similar to {@link java.time.Clock#offset(java.time.Clock, java.time.Duration)}.
   * <p>
   * This method has no effect if the clock is already {@linkplain #isRunning() running}
   *
   *
   * @throws IllegalStateException if not {@linkplain State#STOPPED stopped}
   */
  public static void resume() {
    updateState(TimeState::resume);
  }

  /**
   * Advances the instrumented time by the given duration.  The clock must be {@linkplain #stop() stopped}
   * before calling this method.
   * TODO: update doc
   *
   * @throws IllegalStateException if not supported in the current {@linkplain #getState() state}
   */
  public static void advance(long duration, TimeUnit unit) {
    updateState(timeState -> timeState.advanceAndGet(duration, unit));
  }

  /**
   * TODO: doc
   *
   * @param millis the value to be returned by the first invocation of {@link #currentTimeMillis()}
   * @param nanos the value to be returned by the first invocation of {@link #nanoTime()}
   */
  public static void set(long millis, long nanos) {
    updateState(timeState -> timeState.set(millis, nanos));
  }

  /**
   * @return the current state of this clock, indicating what kind of values are returned by {@link #currentTimeMillis()}
   * and {@link #nanoTime()}
   */
  public static State getState() {
    return state.get().getState();
  }

  /**
   * Returns {@code true} if the clock is not currently {@linkplain #stop() stopped}, which means that subsequent
   * invocations of {@link #currentTimeMillis()} and {@link #nanoTime()} are returning increasing values based on
   * the actual system time (with a potential offset).
   */
  public static boolean isRunning() {
    return !isStopped();
  }

  /**
   * Returns {@code true} if the clock is currently {@linkplain #stop() stopped}, which means that subsequent
   * invocations of {@link #currentTimeMillis()} and {@link #nanoTime()} are returning constant values.
   * Can be used to check whether it's safe to call mutating {@link SettableTicker} methods on the
   * global {@link #ticker()} instance.
   */
  public static boolean isStopped() {
    return getState() == State.STOPPED;
  }

  /**
   * Calls {@link Stopwatch#createStarted(Ticker)} or {@link Stopwatch#createUnstarted()}
   * using this clock's {@link #ticker()}.
   *
   * @param started whether to call {@link Stopwatch#createStarted(Ticker)} or {@link Stopwatch#createUnstarted(Ticker)}
   * @return the result of {@link Stopwatch#createStarted(Ticker)} if the argument is {@code true}, otherwise
   * {@link Stopwatch#createUnstarted(Ticker)}
   */
  public static Stopwatch newStopwatch(boolean started) {
    return started ? Stopwatch.createStarted(ticker()) : Stopwatch.createUnstarted(ticker());
  }


  // Utility methods:

  private static void updateState(UnaryOperator<TimeState> updateFunction) {
    AtomicUtils.updateAndGet(state, updateFunction);
  }


  // Helper classes:

  /**
   * Represents current state of the {@link Clock}, indicating what kind of values are being
   * returned by {@link #currentTimeMillis()} and {@link #nanoTime()}
   */
  public enum State {
    /**
     * The {@link Clock} is currently returning the unaltered values of
     * {@link System#currentTimeMillis()} and {@link System#nanoTime()}
     */
    NORMAL,
    /**
     * The {@link Clock} is currently returning fixed values for
     * {@link #currentTimeMillis()} and {@link #nanoTime()}
     */
    STOPPED,
    /**
     * The {@link Clock} is currently returning the values of
     * {@link System#currentTimeMillis()} and {@link System#nanoTime()} offset by a constant amount.
     */
    OFFSET;
  }

  /**
   * The singleton {@link Ticker}, delegating all ops to the current {@link TimeState}
   */
  static class TickerImpl extends Ticker implements SettableTicker {

    @Override
    public long read() {
      return state.get().read();
    }

    @Override
    public SettableTicker setTime(long nanos) {
      return state.get().setTime(nanos);
    }

    @Override
    public void advance(long deltaNanos) {
      state.get().advance(deltaNanos);
    }

    @Override
    public void advance(long duration, TimeUnit timeUnit) {
      state.get().advance(duration, timeUnit);
    }
  }


  abstract static class TimeState implements SettableTicker {
    protected abstract long currentTimeMillis();

    protected long nanoTime() {
      return read();
    }

    abstract State getState();

    /*
    ================================================================================
    State transition methods:
    ================================================================================
    */

    /**
     * @return an instance of the {@link StoppedTime} state initialized with the current time readings of the current state
     */
    @Nonnull
    protected StoppedTime stop() {
      return new StoppedTime(currentTimeMillis(), nanoTime());
    }

    /**
     * @return a new instance of the {@link OffsetTime} state based on this one.
     */
    @Nonnull
    protected abstract TimeState resume();

    /**
     * @param millis the value to be returned by the first invocation of {@link #currentTimeMillis()}
     * @param nanos the value to be returned by the first invocation of {@link #nanoTime()}
     */
    protected TimeState set(long millis, long nanos) {
      return new OffsetTime(
          millis - NORMAL_TIME.currentTimeMillis(),
          nanos - NORMAL_TIME.nanoTime());
    }

    protected abstract TimeState advanceAndGet(long duration, TimeUnit unit);


    /*
    ================================================================================
    SettableTicker methods:
    ================================================================================
     */
    @Override
    public SettableTicker setTime(long nanos) {
      // subclasses can override if they support this op
      throw new IllegalStateException(illegalStateMessage(State.STOPPED));
    }

    @Override
    public void advance(long deltaNanos) {
      // subclasses can override if they support this op
      throw new IllegalStateException(illegalStateMessage(State.STOPPED));
    }

    @Override
    public void advance(long duration, TimeUnit timeUnit) {
      // subclasses can override if they support this op
      throw new IllegalStateException(illegalStateMessage(State.STOPPED));
    }

    @Nonnull
    private String illegalStateMessage(State... legalStates) {
      StringBuilder msg = new StringBuilder("Clock state must be ");
      if (legalStates.length > 1)
        msg.append("either ");
      StringUtils.appendEnumerated(msg, ",", "or", Arrays.asList(legalStates));
      return msg.append(" (current state is ").append(getState()).append(")").toString();
    }
  }

  /**
   * Returns the unaltered values of {@link System#currentTimeMillis()} and {@link System#nanoTime()}
   */
  static class NormalTime extends TimeState {
    private NormalTime() {  // singleton
    }

    @Override
    protected long currentTimeMillis() {
      return System.currentTimeMillis();
    }

    @Override
    public long read() {
      // Note: System.nanoTime() requires GWT 2.10+
      return System.nanoTime();
    }

    @Override
    State getState() {
      return State.NORMAL;
    }

    @Nonnull
    @Override
    protected TimeState resume() {
      return this;  // already running, nothing to resume
    }

    @Override
    protected TimeState advanceAndGet(long duration, TimeUnit unit) {
      return new OffsetTime(unit.toMillis(duration), unit.toNanos(duration));
    }
  }

  /**
   * Uses fixed values for {@link #currentTimeMillis()} and {@link #nanoTime()}
   */
  static class StoppedTime extends TimeState {
    private final AtomicLong millis;
    private final AtomicLong nanos;

    /**
     * @param millis the fixed time to be returned by the first invocation of {@link #currentTimeMillis()}
     * @param nanos the fixed time to be returned by the first invocation of {@link #nanoTime()}
     */
    StoppedTime(long millis, long nanos) {
      this.millis = new AtomicLong(millis);
      this.nanos = new AtomicLong(nanos);
    }

    @Override
    protected synchronized long currentTimeMillis() {
      return millis.get();
    }

    @Override
    public synchronized long read() {
      return nanos.get();
    }

    @Override
    State getState() {
      return State.STOPPED;
    }

    @Override
    public synchronized void advance(long duration, TimeUnit unit) {
      millis.addAndGet(unit.toMillis(duration));
      nanos.addAndGet(unit.toNanos(duration));
    }

    @Override
    public void advance(long deltaNanos) {
      advance(deltaNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public synchronized SettableTicker setTime(long nanos) {
      long delta = nanos - this.nanos.getAndSet(nanos);
      millis.addAndGet(TimeUnit.NANOSECONDS.toMillis(delta));
      return this;
    }

    @Nonnull
    @Override
    protected StoppedTime stop() {
      return this;
    }

    @Nonnull
    @Override
    protected OffsetTime resume() {
      return new OffsetTime(
          currentTimeMillis() - NORMAL_TIME.currentTimeMillis(),
          nanoTime() - NORMAL_TIME.currentTimeMillis());
    }

    @Nonnull
    @Override
    protected synchronized StoppedTime set(long millis, long nanos) {
      this.millis.set(millis);
      this.nanos.set(nanos);
      return this;
    }

    @Override
    protected TimeState advanceAndGet(long duration, TimeUnit unit) {
      advance(duration, unit);
      return this;
    }
  }

  static class OffsetTime extends TimeState {
    private final long offsetMillis;
    private final long offsetNanos;

    OffsetTime(long offsetMillis, long offsetNanos) {
      this.offsetMillis = offsetMillis;
      this.offsetNanos = offsetNanos;
    }

    @Override
    protected long currentTimeMillis() {
      return NORMAL_TIME.currentTimeMillis() + offsetMillis;
    }

    @Override
    public long read() {
      return NORMAL_TIME.read() + offsetNanos;
    }

    @Override
    State getState() {
      return State.OFFSET;
    }

    @Override
    protected TimeState advanceAndGet(long duration, TimeUnit unit) {
      return new OffsetTime(
          offsetMillis + unit.toMillis(duration),
          offsetNanos + unit.toNanos(duration));
    }

    @Nonnull
    @Override
    protected TimeState resume() {
      return this;  // already running, nothing to resume
    }

  }


}
