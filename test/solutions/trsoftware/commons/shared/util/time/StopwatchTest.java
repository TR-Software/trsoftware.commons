/*
 * Copyright 2025 TR Software Inc.
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
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.io.TablePrinter;
import solutions.trsoftware.commons.shared.testutil.TestUtils;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.util.concurrent.TimeUnit;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;
import static solutions.trsoftware.commons.shared.util.time.Stopwatch.*;

/**
 * @author Alex
 * @since 4/3/2025
 */
public class StopwatchTest extends BaseTestCase {

  /**
   * Tests all the lifecycle methods of {@link Stopwatch}: measuring time, copying, start/stop/reset/restart, etc.
   */
  public void testStopwatch() {
    FakeTicker ticker = new FakeTicker(100);
    Stopwatch sw = createStarted(ticker);
    assertTrue(sw.isRunning());
    // test running/stopped state transitions
    assertFalse(sw.startIfNotRunning());
    assertThrows(IllegalStateException.class, sw::start);
    assertTrue(sw.isRunning());  // unchanged
    sw.stop();  // shouldn't throw ISE
    assertFalse(sw.isRunning());  // changed
    sw.start();
    assertTrue(sw.isRunning());  // changed

    assertEquals(0, sw.elapsedNanos());
    ticker.advance(10);
    assertEquals(10, sw.elapsedNanos());
    ticker.advance(10);
    assertEquals(20, sw.elapsedNanos());
    assertEquals(20, sw.elapsedNanos());  // unchanged if ticker still returns same value

    // test copying
    Stopwatch copy = createCopyOf(sw);
    assertNotSame(sw, copy);
    assertEqualsAndHashCode(sw, copy);
    assertEquals(20, copy.elapsedNanos());  // same value

    // stop the original but not the copy
    sw.stop();
    ticker.advance(10);
    assertEquals(20, sw.elapsedNanos());  // unchanged b/c stopped
    assertEquals(30, copy.elapsedNanos());  // copy updated b/c still running
    assertNotEqual(sw, copy);  // out-of-sync

    // test reset/restart
    sw.reset();
    assertEquals(0, sw.elapsedNanos());
    assertFalse(sw.isRunning());

    assertTrue(copy.isRunning());
    copy.stopIfRunning();
    assertFalse(copy.isRunning());
    copy.restart();
    assertTrue(copy.isRunning());
    assertEquals(0, copy.elapsedNanos());

    ticker.advance(10);
    assertEquals(0, sw.elapsedNanos());  // stopped
    assertEquals(10, copy.elapsedNanos());  // running

    assertThrows(IllegalStateException.class, sw::stop);  // already stopped
    assertThrows(IllegalStateException.class, copy::start);  // already running

    System.out.println("sw = " + sw);
    System.out.println("copy = " + copy);
  }

  public void testCreateUnstarted() {
    // createUnstarted with a FakeTicker and default system ticker
    {
      FakeTicker fakeTicker = new FakeTicker();
      Stopwatch sw = createUnstarted(fakeTicker);
      assertFalse(sw.isRunning());
      assertEquals(0, sw.elapsedNanos());
      assertSame(fakeTicker, sw.getTicker());
      sw.start();
      assertTrue(sw.isRunning());
      assertEquals(0, sw.elapsedNanos());  // ticker value not changed
      fakeTicker.advance(10);
      assertEquals(10, sw.elapsedNanos());
      sw.stop();
      assertFalse(sw.isRunning());
      assertEquals(10, sw.elapsedNanos());
      System.out.println("fakeTickerStopwatch: " + sw);
    }
    // createUnstarted with a default system ticker
    {
      Stopwatch sw = createUnstarted();
      assertFalse(sw.isRunning());
      assertEquals(0, sw.elapsedNanos());
      Ticker sysTicker = sw.getTicker();  // defaults to System.nanoTime
      assertTrue(sysTicker.read() > 0);
      long t1 = System.nanoTime();
      sw.start();
      assertTrue(sw.isRunning());
      long swNanos = sw.elapsedNanos();
      long t2 = System.nanoTime();
      assertThat(swNanos).isGreaterThan(0L).isLessThan(t2 - t1);
      sw.stop();
      long stoppedNanos = sw.elapsedNanos();
      TestUtils.busyWaitNanos(10);
      assertEquals(stoppedNanos, sw.elapsedNanos());  // value shouldn't change after stopping
      assertFalse(sw.isRunning());
      sw.start();
      TestUtils.busyWaitNanos(10);
      long resumedNanos = sw.elapsedNanos();
      assertTrue(resumedNanos > stoppedNanos);
      System.out.println("sysTickerStopwatch: " + sw);
    }
  }

  public void testCreateStarted() {
    // createStarted with a FakeTicker and default system ticker; also test startIfNotRunning/stopIfRunning
    {
      FakeTicker fakeTicker = new FakeTicker();
      Stopwatch sw = createStarted(fakeTicker);
      assertTrue(sw.isRunning());
      assertSame(fakeTicker, sw.getTicker());
      assertFalse(sw.startIfNotRunning());  // already running
      assertEquals(0, sw.elapsedNanos());  // ticker value not changed
      fakeTicker.advance(10, TimeUnit.MICROSECONDS);
      long expectedNanos = TimeUnit.MICROSECONDS.toNanos(10);
      assertEquals(expectedNanos, sw.elapsedNanos());
      assertTrue(sw.stopIfRunning());;
      assertFalse(sw.isRunning());
      assertEquals(expectedNanos, sw.elapsedNanos());
      System.out.println("fakeTickerStopwatch: " + sw);
    }
    // createStarted with a default system ticker; also test start/stop IllegalStateException
    {
      long t1 = System.nanoTime();
      Stopwatch sw = createStarted();
      assertTrue(sw.isRunning());
      Ticker sysTicker = sw.getTicker();  // defaults to System.nanoTime
      assertTrue(sysTicker.read() > 0);
      assertTrue(sw.isRunning());
      long swNanos = sw.elapsedNanos();
      long t2 = System.nanoTime();
      assertThat(swNanos).isGreaterThan(0L).isLessThan(t2 - t1);
      sw.stop();
      assertFalse(sw.isRunning());
      long stoppedNanos = sw.elapsedNanos();
      TestUtils.busyWaitNanos(10);
      assertEquals(stoppedNanos, sw.elapsedNanos());  // value shouldn't change after stopping
      assertFalse(sw.isRunning());
      sw.start();
      long resumedNanos = sw.elapsedNanos();
      assertTrue(resumedNanos > stoppedNanos);
      System.out.println("sysTickerStopwatch: " + sw);
    }
  }

  /**
   * Tests that {@link Stopwatch#start()} and {@link Stopwatch#stop()} throw {@link IllegalStateException} if already
   * in the desired state, whereas {@link Stopwatch#startIfNotRunning()} and {@link Stopwatch#stopIfRunning()} do not.
   */
  public void testStartStop() {
    Stopwatch sw = createUnstarted();
    assertFalse(sw.isRunning());
    // 1) start/stop should throw if in the wrong state
    assertThrows(IllegalStateException.class, sw::stop);  // already stopped
    sw.start();  // should succeed
    assertTrue(sw.isRunning());
    assertThrows(IllegalStateException.class, sw::start);  // already started
    // 2) startIfNotRunning/stopIfRunning should return false instead of throwing
    assertFalse(sw.startIfNotRunning());
    assertTrue(sw.stopIfRunning());
    assertFalse(sw.isRunning());
    assertFalse(sw.stopIfRunning());
    assertTrue(sw.startIfNotRunning());
    assertTrue(sw.isRunning());
  }

  public void testTestToString() {
    FakeTicker ticker = new FakeTicker();
    // for now just testing output visually TODO: maybe add some assertions?
    TablePrinter out = new TablePrinter();
    SharedNumberFormat nanoFmt = new SharedNumberFormat("#,###");
    Stopwatch sw = createStarted(ticker);
    out.newRow()
        .addCol("elapsedNanos", nanoFmt.format(sw.elapsedNanos()))
        .addCol("toString", sw);
    long incr = 0;
    for (TimeUnit unit : TimeUnit.values()) {
      ticker.advance((++incr) * 100, unit);
      out.newRow()
          .addCol("elapsedNanos", nanoFmt.format(sw.elapsedNanos()))
          .addCol("toString", sw);
    }
    out.printTable();
  }

  /**
   * Tests {@link Stopwatch#reset()} and {@link Stopwatch#restart()}
   */
  public void testResetRestart() {
    FakeTicker ticker = new FakeTicker();
    Stopwatch sw = createStarted(ticker);
    assertTrue(sw.isRunning());
    assertEquals(0, sw.elapsedNanos());
    ticker.advance(10);
    assertEquals(10, sw.elapsedNanos());
    // restart should set elapsed to 0 but still running
    sw.restart();
    assertEquals(0, sw.elapsedNanos());
    assertTrue(sw.isRunning());
    ticker.advance(100);
    assertEquals(100, sw.elapsedNanos());
    // reset should set elapsed to 0 and as well as stop running
    sw.reset();
    assertEquals(0, sw.elapsedNanos());
    assertFalse(sw.isRunning());
  }
  
  /**
   * Tests {@link Stopwatch#pauseStopwatch(Stopwatch)} and {@link Stopwatch#resumeStopwatch(Stopwatch)}
   */
  public void testPauseResume() {
    // 1) should accept null args (do nothing and return false)
    assertFalse(pauseStopwatch(null));
    assertFalse(resumeStopwatch(null));

    // 2) should transition state if not null and not already in the desired state
    Stopwatch sw = createUnstarted();
    assertFalse(pauseStopwatch(sw));  // already stopped
    assertTrue(resumeStopwatch(sw));  // should be started
    assertTrue(sw.isRunning());
    assertFalse(resumeStopwatch(createStarted()));  // already started
    assertTrue(sw.isRunning());  // still running
    assertTrue(pauseStopwatch(sw));  // should stop
    assertFalse(sw.isRunning());
  }
}