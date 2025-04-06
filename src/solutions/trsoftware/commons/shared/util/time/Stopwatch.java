/*
 * Copyright (C) 2008 The Guava Authors and (C) 2025 TR Software Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.shared.util.time;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Ticker;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.*;

/**
 * A richer version of Guava's {@link com.google.common.base.Stopwatch}, offering greater flexibility by
 * allowing instances to be copied, and providing additional utility methods:
 * <ul>
 *   <li>{@link #createCopyOf(Stopwatch)}</li>
 *   <li>{@link #startIfNotRunning()}</li>
 *   <li>{@link #stopIfRunning()}</li>
 * </ul>
 * <p>
 * This class is fully API-compatible with Guava's Stopwatch, and can be easily substituted for that class
 * (as a drop-in replacement) simply by changing the {@code import} statement.
 */
public final class Stopwatch {
  /*
  Note: this source code is mostly an exact copy of com.google.common.base.Stopwatch from Guava 28.2
   */

  private final Ticker ticker;
  private final AtomicBoolean isRunning;
  private long elapsedNanos;
  private long startTick;

  /**
   * Creates (but does not start) a new stopwatch using {@link System#nanoTime} as its time source.
   */
  public static Stopwatch createUnstarted() {
    return new Stopwatch();
  }

  /**
   * Creates (but does not start) a new stopwatch, using the specified time source.
   */
  public static Stopwatch createUnstarted(Ticker ticker) {
    return new Stopwatch(ticker);
  }

  /**
   * Creates (and starts) a new stopwatch using {@link System#nanoTime} as its time source.
   */
  public static Stopwatch createStarted() {
    return new Stopwatch().start();
  }

  /**
   * Creates (and starts) a new stopwatch, using the specified time source.
   */
  public static Stopwatch createStarted(Ticker ticker) {
    return new Stopwatch(ticker).start();
  }

  /**
   * Creates a copy of the given stopwatch, using the same {@link #ticker}, current running/stopped state,
   * and elapsed time value.
   * The new stopwatch will initially return the same {@linkplain #elapsed(TimeUnit) elapsed time}
   * as the given stopwatch, but may diverge over time, based on any independent changes to their
   * started/stopped states.
   */
  public static Stopwatch createCopyOf(@Nonnull Stopwatch other) {
    return new Stopwatch(other);
  }

  Stopwatch() {
    this(Ticker.systemTicker());
  }

  Stopwatch(Ticker ticker) {
    this.ticker = checkNotNull(ticker, "ticker");
    isRunning = new AtomicBoolean();
  }

  /**
   * Copy constructor
   * @param other instance whose fields will be copied
   */
  Stopwatch(Stopwatch other) {
    requireNonNull(other, "other");
    ticker = other.ticker;
    isRunning = new AtomicBoolean(other.isRunning.get());
    elapsedNanos = other.elapsedNanos;
    startTick = other.startTick;
  }

  public Ticker getTicker() {
    return ticker;
  }

  /**
   * @return {@code true} if {@link #start()} has been called on this stopwatch, and {@link #stop()}
   * has not been called since the last call to {@link #start()}.
   */
  public boolean isRunning() {
    return isRunning.get();
  }

  /**
   * Starts the stopwatch.
   *
   * @return this {@code Stopwatch} instance
   * @throws IllegalStateException if the stopwatch is already running.
   * @see #startIfNotRunning()
   */
  @CanIgnoreReturnValue
  public Stopwatch start() {
    checkState(startIfNotRunning(), "This stopwatch is already running.");
    /* NOTE: not sure why Guava's implementation throws ISE if already running
       but we're preserving that behavior to ensure API-compatibility with Guava's Stopwatch
       (so that our class can be used as a drop-in replacement for Guava's)
     */
    return this;
  }

  /**
   * {@linkplain #start() Starts} the stopwatch if it's not already {@linkplain #isRunning() running}.
   * <p>
   * Unlike {@link #start()}, this method never throws {@link IllegalStateException}.
   *
   * @return {@code true} if the stopwatch state changed as a result of this call (i.e. wasn't already running)
   */
  @CanIgnoreReturnValue
  public boolean startIfNotRunning() {
    if (isRunning.compareAndSet(false, true)) {
      startTick = ticker.read();
      return true;
    }
    return false;
  }

  /**
   * Stops the stopwatch. Future reads will return the fixed duration that had elapsed up to this point.
   *
   * @return this {@code Stopwatch} instance
   * @throws IllegalStateException if the stopwatch is already stopped.
   * @see #stopIfRunning()
   */
  @CanIgnoreReturnValue
  public Stopwatch stop() {
    checkState(stopIfRunning(), "This stopwatch is already stopped.");
    /* NOTE: not sure why Guava's implementation throws ISE if already stopped
       but we're preserving that behavior to ensure API-compatibility with Guava's Stopwatch
       (so that our class can be used as a drop-in replacement for Guava's)
     */
    return this;
  }

  /**
   * {@linkplain #stop() Stops} the stopwatch if it's not already {@linkplain #isRunning() stopped}.
   * <p>
   * Unlike {@link #stop()}, this method never throws {@link IllegalStateException}.
   *
   * @return {@code true} if the stopwatch state changed as a result of this call (i.e. wasn't already stopped)
   */
  @CanIgnoreReturnValue
  public boolean stopIfRunning() {
    if (isRunning.compareAndSet(true, false)) {
      long tick = ticker.read();
      elapsedNanos += tick - startTick;
      return true;
    }
    return false;
  }

  /**
   * Sets the elapsed time for this stopwatch to zero, and places it in a stopped state.
   *
   * @return this {@code Stopwatch} instance
   */
  @CanIgnoreReturnValue
  public Stopwatch reset() {
    elapsedNanos = 0;
    isRunning.set(false);
    return this;
  }

  /**
   * Invokes {@link #reset()} and {@link #start()}, thereby setting the elapsed time for this stopwatch to zero,
   * and restarting the {@linkplain #elapsed(TimeUnit) elapsed time} counter from zero.
   *
   * @return this {@code Stopwatch} instance
   */
  @CanIgnoreReturnValue
  public Stopwatch restart() {
    reset();
    start();
    return this;
  }

  public long elapsedNanos() {
    return isRunning.get() ? ticker.read() - startTick + elapsedNanos : elapsedNanos;
  }

  /**
   * Returns the current elapsed time shown on this stopwatch, expressed in the desired time unit,
   * with any fraction rounded down.
   *
   * <p><b>Note:</b> the overhead of measurement can be more than a microsecond, so it is generally
   * not useful to specify {@link TimeUnit#NANOSECONDS} precision here.
   *
   * <p>It is generally not a good idea to use an ambiguous, unitless {@code long} to represent
   * elapsed time. Therefore, we recommend using {@link #elapsed()} instead, which returns a
   * strongly-typed {@link Duration} instance.
   */
  public long elapsed(TimeUnit desiredUnit) {
    return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
  }

  /**
   * Returns the current elapsed time shown on this stopwatch as a {@link Duration}. Unlike {@link
   * #elapsed(TimeUnit)}, this method does not lose any precision due to rounding.
   *
   * @since 22.0
   */
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  @GwtIncompatible
  public Duration elapsed() {
    return Duration.ofNanos(elapsedNanos());
  }

  /** Returns a string representation of the current elapsed time. */
  @Override
  public String toString() {
    // NOTE: code duplicated in solutions.trsoftware.commons.shared.util.TimeUnit.format,
    // which in turn, is a copy of the code in Guava's Stopwatch
    long nanos = elapsedNanos();

    TimeUnit unit = chooseUnit(nanos);
    double value = (double) nanos / NANOSECONDS.convert(1, unit);

    // Too bad this functionality is not exposed as a regular method call
    return formatCompact4Digits(value) + " " + abbreviate(unit);
  }

  static String formatCompact4Digits(double value) {
    // corresponds to Guava's package-private Platform.formatCompact4Digits(value) method
    return new SharedNumberFormat(4).format(value);
  }

  private static TimeUnit chooseUnit(long nanos) {
    if (DAYS.convert(nanos, NANOSECONDS) > 0) {
      return DAYS;
    }
    if (HOURS.convert(nanos, NANOSECONDS) > 0) {
      return HOURS;
    }
    if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
      return MINUTES;
    }
    if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
      return SECONDS;
    }
    if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
      return MILLISECONDS;
    }
    if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
      return MICROSECONDS;
    }
    return NANOSECONDS;
  }

  private static String abbreviate(TimeUnit unit) {
    switch (unit) {
      case NANOSECONDS:
        return "ns";
      case MICROSECONDS:
        return "\u03bcs"; // μs
      case MILLISECONDS:
        return "ms";
      case SECONDS:
        return "s";
      case MINUTES:
        return "min";
      case HOURS:
        return "h";
      case DAYS:
        return "d";
      default:
        throw new AssertionError();
    }
  }

  /**
   * Returns {@code true} iff this stopwatch has the same value, ticker, and {@linkplain #isRunning() state}
   * as the argument.
   * <p><b>Note:</b> this behavior is incompatible with Guava's {@link com.google.common.base.Stopwatch}, which
   * doesn't override {@link Object#equals(Object)}.
   * @param o another {@link Stopwatch} instance
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Stopwatch stopwatch = (Stopwatch)o;
    return elapsedNanos == stopwatch.elapsedNanos &&
        startTick == stopwatch.startTick &&
        ticker.equals(stopwatch.ticker) &&
        isRunning() == stopwatch.isRunning();
  }

  @Override
  public int hashCode() {
    return Objects.hash(elapsedNanos, startTick, ticker, isRunning());
  }

  /**
   * Invokes {@link Stopwatch#stop()} if the given instance is not null and is actually running
   * (otherwise {@link Stopwatch#stop()} would throw an {@link IllegalStateException}).
   *
   * @return {@code true} if the stopwatch state changed as a result of this call
   * @see #stopIfRunning()
   */
  public static boolean pauseStopwatch(Stopwatch stopwatch) {
    return stopwatch != null && stopwatch.stopIfRunning();
  }

  /**
   * Invokes {@link Stopwatch#start()} if the given instance is not null and is not already running
   * (otherwise {@link Stopwatch#start()} would throw an {@link IllegalStateException}).
   *
   * @return {@code true} if the stopwatch state changed as a result of this call
   * @see #startIfNotRunning()
   */
  public static boolean resumeStopwatch(Stopwatch stopwatch) {
    return stopwatch != null && stopwatch.startIfNotRunning();
  }
}
