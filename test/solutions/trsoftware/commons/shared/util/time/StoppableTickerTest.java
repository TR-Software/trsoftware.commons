package solutions.trsoftware.commons.shared.util.time;

import solutions.trsoftware.commons.shared.BaseTestCase;

/**
 * @author Alex
 * @since 9/25/2024
 */
public class StoppableTickerTest extends BaseTestCase {

  public void testWithoutInitialValue() throws Exception {
    FakeTicker source = new FakeTicker();
    assertEquals(0L, source.read());
    StoppableTicker ticker = new StoppableTicker(source);
    assertEquals(0L, ticker.read());
    assertTrue(ticker.isRunning());
    source.setTime(1_000);
    long expected = 1_000;
    assertEquals(expected, ticker.read());

    ticker.stop();
    assertFalse(ticker.isRunning());
    source.setTime(2_000);  // should not be reflected in StoppableTicker until resumed
    assertEquals(expected, ticker.read());  // still the old value, while stopped
    ticker.resume();
    assertTrue(ticker.isRunning());
    assertEquals(expected, ticker.read());
    // at this point we expect our ticker to be 1_000ns behind the source
    long expectedOffset = 1_000;

    // TODO: this is partly duplicated in doTest

    for (int i = 1; i <= 10; i++) {
      int deltaNanos = i * 1000;
      source.advance(deltaNanos);
      assertEquals(expected = (source.read() - expectedOffset), ticker.read());
    }
    // if we stop our ticker again, the lag offset should accumulate
    ticker.stop();
    long deltaOffset = 4_000;
    source.advance(deltaOffset);
    assertEquals(expected, ticker.read());  // should still return the old value while stopped
    expectedOffset += deltaOffset;
    ticker.resume();
    assertEquals(expected = (source.read() - expectedOffset), ticker.read());  // should still return the old value while stopped
  }


  public void testWithInitialValue() throws Exception {
    {
      long initialValue = 0;
      FakeTicker source = new FakeTicker(10);
      assertEquals(10L, source.read());
      StoppableTicker ticker = new StoppableTicker(source, initialValue);
      assertEquals(initialValue, ticker.read());
      doTest(source, ticker, 10);
    }
    {
      // try an initial value greater than the source value (negative offset)
      long initialValue = 20;
      FakeTicker source = new FakeTicker(10);
      assertEquals(10L, source.read());
      StoppableTicker ticker = new StoppableTicker(source, initialValue);
      assertEquals(initialValue, ticker.read());
      doTest(source, ticker, -10);
    }
  }


  private void doTest(FakeTicker source, StoppableTicker ticker, long expectedOffset) {
    assertTrue(ticker.isRunning());
    long currentValue = readAndVerifyOffset(ticker, source, expectedOffset);
    for (int stopCount = 0; stopCount < 5; stopCount++) {
      for (int deltaNanos = 1; deltaNanos <= 10; deltaNanos++) {
        source.advance(deltaNanos);
        currentValue = readAndVerifyOffset(ticker, source, expectedOffset);
      }
      // stop the ticker while advancing the source, to increase the offset
      ticker.stop();
      assertFalse(ticker.isRunning());
      source.advance(10);
      expectedOffset += 10;  // the lag offset should accumulate while stopped
      assertEquals(currentValue, ticker.read());  // value shouldn't change while stopped
      ticker.resume();
      assertTrue(ticker.isRunning());
      currentValue = readAndVerifyOffset(ticker, source, expectedOffset);
    }
  }

  private long readAndVerifyOffset(StoppableTicker ticker, FakeTicker source, long expectedOffset) {
    long value = ticker.read();
    assertEquals(source.read() - expectedOffset, value);
    return value;
  }
}