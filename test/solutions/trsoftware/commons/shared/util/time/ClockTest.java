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
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.time.Clock.State;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThat;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.StringUtils.methodCallToString;

/**
 * @author Alex
 * @since 12/31/2022
 */
@SuppressWarnings("NonJREEmulationClassesInClientCode")
@GwtIncompatible
public class ClockTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Clock.resetToNormal();
  }

  @Override
  protected void tearDown() throws Exception {
    Clock.resetToNormal();
    super.tearDown();
  }

  public void testStop() throws Exception {
    // 1) from the NORMAL state
    {
      printStateSection("Starting from:");
      assertNormalTime();
      stopAndVerify();
      System.out.println();
    }
    // 2) from the STOPPED state
    {
      printStateSection("Starting from:");
      long millis = Clock.currentTimeMillis();
      long nanos = Clock.nanoTime();
      stopAndVerify();  // should have no effect
      assertEquals(millis, Clock.currentTimeMillis());  // same as before
      assertEquals(nanos, Clock.nanoTime());  // same as before
      System.out.println();
    }
    // 3) from the OFFSET state
    {
      resumeAndVerify();
      printStateSection("Starting from:");
      stopAndVerify();
      System.out.println();
    }
  }

  public void testSet() throws Exception {
    // 1) from the NORMAL state
    {
      assertNormalTime();
      printStateSection("Starting from:");
      long millis = 0, nanos = 0;
      Clock.set(millis, nanos);
      printState(methodCallToString("Clock.set", millis, nanos));
      assertSame(State.OFFSET, Clock.getState());
      assertRunning(true);
      System.out.println();
    }
    // 2) from the OFFSET state
    {
      printStateSection("Starting from:");
      long millis = 10_000, nanos = 10_000_000_000L;
      Clock.set(millis, nanos);
      printState(methodCallToString("Clock.set", millis, nanos));
      assertSame(State.OFFSET, Clock.getState());
      assertRunning(true);
      System.out.println();
    }
    // 3) from the STOPPED state
    {
      Clock.stop();
      assertStopped();
      printStateSection("Starting from:");
      long millis = 10_000, nanos = 10_000_000_000L;
      Clock.set(millis, nanos);
      printState(methodCallToString("Clock.set", millis, nanos));
      assertStopped(millis, nanos);
      System.out.println();
    }
  }

  public void testResume() throws Exception {
    // 1) from the NORMAL state
    {
      printStateSection("Starting from:");
      assertNormalTime();
      Clock.resume();
      printState("Clock.resume()");
      assertNormalTime();  // no changes
      System.out.println();
    }
    // 2) from the STOPPED state
    {
      Clock.stop();
      assertStopped();
      printStateSection("Starting from:");
      long millis = 10_000, nanos = 10_000_000_000L;
      Clock.set(millis, nanos);
      printState(methodCallToString("Clock.set", millis, nanos));
      resumeAndVerify(true);
      System.out.println();
    }
    // 2) from the OFFSET state
    {
      printStateSection("Starting from:");
      resumeAndVerify(true);  // no changes expected
      System.out.println();
    }
  }

  public void testAdvance() throws Exception {
    int duration = 1;
    TimeUnit unit = HOURS;
    // 1) from the STOPPED state
    {
      printStateSection("Starting from:");
      stopAndVerify();
      long millis0 = Clock.currentTimeMillis();
      long nanos0 = Clock.nanoTime();
      advance(duration, unit);
      assertStopped(millis0 + unit.toMillis(duration), nanos0 + unit.toNanos(duration));
      System.out.println();
    }
    Clock.resetToNormal();
    // 2) from the NORMAL state
    {
      printStateSection("Starting from:");
      assertNormalTime();
      advanceAndVerifyOffset(duration, unit);
      System.out.println();
    }
    // 3) from the OFFSET state
    {
      printStateSection("Starting from:");
      assertSame(State.OFFSET, Clock.getState());  // should already be in the OFFSET state
      advanceAndVerifyOffset(duration, unit);
      System.out.println();
    }
  }

  public void testTicker() throws Exception {
    // Clock.ticker() returns the same singleton instance regardless of clock state,
    // however this ticker's behavior varies depending on the state
    Ticker ticker = Clock.ticker();
    assertTrue(ticker instanceof SettableTicker);
    SettableTicker settableTicker = (SettableTicker)ticker;
    // 1) from the NORMAL state
    {
      assertNormalTime();
      printStateSection("Starting from:");
      assertSame(ticker, Clock.ticker());
      assertTickerNormal(ticker, true);
      assertNotSettable(settableTicker); // the ticker's value should not be modifiable in this state
      System.out.println();
    }
    // 2) from the OFFSET state
    {
      // place the clock in the OFFSET state
      advanceAndVerifyOffset(1, HOURS);
      printStateSection("Starting from:");
      assertSame(ticker, Clock.ticker());
      assertTickerRunning(ticker, true);
      assertNotSettable(settableTicker); // the ticker's value should not be modifiable in this state
      System.out.println();
    }
    // 3) from the STOPPED state
    {
      Clock.stop();
      assertStopped();
      printStateSection("Starting from:");
      assertSame(ticker, Clock.ticker());
      assertTickerStopped(ticker);
      long millis = Clock.currentTimeMillis(), nanos = Clock.nanoTime();
      // ticker.read() should match Clock.nanoTime() when stopped
      assertEquals(nanos, ticker.read());
      // when clock is stopped, we can use the SettableTicker methods to modify the time
      settableTicker.advance(10);
      printState("ticker.advance(10):");
      assertStopped(millis, nanos + 10);
      nanos += 10;
      int duration = 1;
      TimeUnit unit = HOURS;
      settableTicker.advance(duration, unit);
      printState(String.format("ticker.advance(%d, %s):", duration, unit));
      assertStopped(millis = millis + unit.toMillis(duration), nanos = nanos + unit.toNanos(duration));
      long targetTime = nanos - unit.toNanos(duration);
      settableTicker.setTime(targetTime);
      printState(String.format("ticker.setTime(%,d):", targetTime));
      // should've adjusted both nanos and millis accordingly
      assertStopped(millis - unit.toMillis(duration), targetTime);
      // TODO: test ticker.setTime()
      System.out.println();
    }
  }

  /**
   * Asserts that all {@link SettableTicker} methods on the given instance throw an {@link IllegalStateException}
   */
  private void assertNotSettable(SettableTicker settableTicker) {
    assertThrows(IllegalStateException.class, () -> settableTicker.setTime(1234));
    assertThrows(IllegalStateException.class, (Runnable)() -> settableTicker.advance(1234));
    assertThrows(IllegalStateException.class, (Runnable)() -> settableTicker.advance(12, MINUTES));
  }

  private void stopAndVerify() throws Exception {
    Clock.stop();
    printState("Clock.stop():");
    assertStopped();
  }

  private void advanceAndVerifyOffset(int duration, TimeUnit unit) throws Exception {
    long millis0 = Clock.currentTimeMillis();
    long nanos0 = Clock.nanoTime();
    advance(duration, unit);
    assertSame(State.OFFSET, Clock.getState());
    Thread.sleep(1);
    assertThat(Clock.currentTimeMillis()).isGreaterThan(millis0 + unit.toMillis(duration));
    assertThat(Clock.nanoTime()).isGreaterThan(nanos0 + unit.toNanos(duration));
    assertRunning(true);
  }

  private void advance(int duration, TimeUnit unit) {
    Clock.advance(duration, unit);
    printState(String.format("Clock.advance(%d, %s):", duration, unit));
  }

  private void resumeAndVerify() throws Exception {
    resumeAndVerify(false);
  }

  /**
   * Calls {@link Clock#resume()} and verifies that subsequent invocations of
   * {@link Clock#currentTimeMillis()} and {@link Clock#nanoTime()} return increasing values.
   * @param printTicks if {@code true}, will print the time values at each step
   */
  private void resumeAndVerify(boolean printTicks) throws Exception {
    Clock.resume();
    printState("Clock.resume()");
    assertSame(State.OFFSET, Clock.getState());
    Thread.sleep(10);
    printState("After Thread.sleep(10)");
    assertRunning(printTicks);
  }

  private void printState(String heading) {
    long currentTimeMillis = Clock.currentTimeMillis();
    System.out.printf("%s%n\tstate=%8s, currentTimeMillis=%,18d; nanoTime=%,24d%n\t%52s%n", heading,
        Clock.getState(), currentTimeMillis, Clock.nanoTime(),
        Instant.ofEpochMilli(currentTimeMillis));
  }

  private void printStateSection(String heading) {
    String hr = "================================================================================";
    System.out.println(hr);
    printState(heading);
    System.out.println(hr);
  }

  private void printTick(String indent, int i, long millis, long nanos) {
    System.out.printf("%sTick %2d: millis=%,18d; nanos=%,24d%n", indent, i, millis, nanos);
  }

  private void assertNormalTime() throws Exception {
    assertSame(State.NORMAL, Clock.getState());
    assertFalse(Clock.isStopped());
    long millisBefore = System.currentTimeMillis();
    long millis = Clock.currentTimeMillis();
    long millisAfter = System.currentTimeMillis();
    assertThat(millis).isBetween(millisBefore, millisAfter);
    long nanosBefore = System.nanoTime();
    long nanos = Clock.nanoTime();
    long nanosAfter = System.nanoTime();
    assertThat(nanos).isBetween(nanosBefore, nanosAfter);
    assertRunning(false);
  }

  /**
   * Verifies that subsequent invocations of {@link Clock#currentTimeMillis()} and {@link Clock#nanoTime()} return
   * increasing values.
   * @param printTicks if {@code true}, will print the time values at each step
   */
  private void assertRunning(boolean printTicks) throws Exception {
    assertTrue(Clock.isRunning());
    assertFalse(Clock.isStopped());
    assertNotSame(State.STOPPED, Clock.getState());
    AtomicLong millis = new AtomicLong(Clock.currentTimeMillis());
    AtomicLong nanos = new AtomicLong(Clock.nanoTime());
    for (int i = 0; i < 10; i++) {
      Thread.sleep(1);
      long nextMillis = Clock.currentTimeMillis();
      long nextNanos = Clock.nanoTime();
      if (printTicks)
        printTick("____ ", i + 1, nextMillis, nextNanos);
      assertThat(nextMillis).isGreaterThan(millis.getAndSet(nextMillis));
      assertThat(nextNanos).isGreaterThan(nanos.getAndSet(nextNanos));
    }
  }

  /**
   * Verifies that subsequent invocations of {@link Clock#currentTimeMillis()} and {@link Clock#nanoTime()} return
   * the same values.
   */
  private void assertStopped() throws Exception {
    assertTrue(Clock.isStopped());
    assertSame(State.STOPPED, Clock.getState());
    long millis0 = Clock.currentTimeMillis();
    long nanos0 = Clock.nanoTime();
    for (int i = 0; i < 10; i++) {
      Thread.sleep(1);
      assertEquals(millis0, Clock.currentTimeMillis());;
      assertEquals(nanos0, Clock.nanoTime());;
    }
  }

  /**
   * Verifies that subsequent invocations of {@link Clock#currentTimeMillis()} and {@link Clock#nanoTime()} return
   * the given values.
   */
  private void assertStopped(long expectedMillis, long expectedNanos) throws Exception {
    assertEquals(expectedMillis, Clock.currentTimeMillis());
    assertEquals(expectedNanos, Clock.nanoTime());
    assertEquals(expectedNanos, Clock.ticker().read());
    assertStopped();
  }

  /**
   * Verifies that subsequent invocations of {@link Ticker#read()} return the same value.
   * @return the value returned by {@link Ticker#read()}
   */
  private long assertTickerStopped(Ticker ticker) throws Exception {
    long value = ticker.read();
    for (int i = 0; i < 10; i++) {
      Thread.sleep(1);
      assertEquals(value, ticker.read());
    }
    return value;
  }

  /**
   * Verifies that subsequent invocations of {@link Ticker#read()} return increasing values.
   * @param printTicks if {@code true}, will print the readings at each step
   */
  private void assertTickerRunning(Ticker ticker, boolean printTicks) throws Exception {
    AtomicLong lastReading = new AtomicLong(ticker.read());
    for (int i = 0; i < 10; i++) {
      Thread.sleep(1);
      long nextReading = ticker.read();
      if (printTicks)
        System.out.printf("%sticker.read() %2d: %,d%n", "____ ", i + 1, nextReading);
      assertThat(nextReading).isGreaterThan(lastReading.getAndSet(nextReading));
    }
  }

  /**
   * Asserts that the given ticker more-or-less matches {@link System#nanoTime()}
   * @param printTicks if {@code true}, will print the readings at each step
   */
  private void assertTickerNormal(Ticker ticker, boolean printTicks) throws Exception {
    long nanosBefore = System.nanoTime();
    long nanos = ticker.read();
    long nanosAfter = System.nanoTime();
    assertThat(nanos).isBetween(nanosBefore, nanosAfter);
    assertTickerRunning(ticker, printTicks);
  }

  public void testNewStopwatch() throws Exception {
    Clock.stop();
    Stopwatch started = Clock.newStopwatch(true);
    Stopwatch unstarted = Clock.newStopwatch(false);
    assertTrue(started.isRunning());
    assertFalse(unstarted.isRunning());
    assertEquals(0, started.elapsed(NANOSECONDS));
    assertEquals(0, unstarted.elapsed(NANOSECONDS));
    Clock.advance(1, SECONDS);
    assertEquals(1, started.elapsed(SECONDS));
    assertEquals(0, unstarted.elapsed(NANOSECONDS));
    unstarted.start();
    Clock.advance(1, MILLISECONDS);
    assertEquals(1, unstarted.elapsed(MILLISECONDS));
    assertEquals(1001, started.elapsed(MILLISECONDS));
  }
}