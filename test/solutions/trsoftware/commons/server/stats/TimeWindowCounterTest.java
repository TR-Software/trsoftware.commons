package solutions.trsoftware.commons.server.stats;

import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.callables.Function0_t;
import solutions.trsoftware.commons.server.TestCaseCanStopClock;
import solutions.trsoftware.commons.server.testutil.MultithreadedTestHarness;
import solutions.trsoftware.commons.server.util.Clock;

import java.util.Collection;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mar 22, 2011
 *
 * @author Alex
 */
public class TimeWindowCounterTest extends TestCaseCanStopClock {
  private long maxAgeMillis = TimeUnit.HOURS.toMillis(1);


  public void testFixedTimeCounter() throws Exception {
    long startTime = Clock.stop();
    TimeWindowCounter counter = new TimeWindowCounter(maxAgeMillis);
    // count a few things
    counter.incr();
    Clock.advance(1);
    counter.incr();
    assertEquals(2, counter.getCount());
    Clock.advance(1);
    counter.add(5);
    Clock.advance(1);
    assertEquals(7, counter.getCount());
    counter.add(-3);
    final int sum1 = counter.getCount();
    assertEquals(4, sum1);  // during this time interval, we have net value 4
    assertEquals(4, counter.size());  // since the default granularity is 1 ms and the clock ticked 3 times between ops, we should have 4 entries recorded

    // add some values later in time
    Clock.advance(TimeUnit.MINUTES.toMillis(5));
    // count a few things
    counter.incr();
    counter.add(-2);
    final int sum2 = -1;  // net -1 during this interval
    assertEquals(sum1+sum2, counter.getCount());
    assertEquals(5, counter.size());  // one more entry recorded now

    // advance the clock so that each value in the first batch of entries expires
    Clock.set(startTime + maxAgeMillis + 1);
    assertEquals(sum1+sum2-1, counter.getCount());  // the very first entry should have expired
    assertEquals(4, counter.size());
    Clock.advance(1);
    assertEquals(sum1+sum2-1-1, counter.getCount());  // the second entry should have expired
    assertEquals(3, counter.size());
    Clock.advance(1);
    assertEquals(sum1+sum2-1-1-5, counter.getCount());  // the third entry should have expired
    assertEquals(2, counter.size());
    Clock.advance(1);
    assertEquals(sum1+sum2-1-1-5+3, counter.getCount());  // the fourth entry should have expired
    assertEquals(1, counter.size());

    // now advance the clock so that everything expires
    Clock.advance(TimeUnit.MINUTES.toMillis(5));
    assertEquals(0, counter.getCount());
    assertEquals(0, counter.size());
  }


  public void testGranularity() throws Exception {
    doGranularityTest(TimeUnit.SECONDS.toMillis(1));
    doGranularityTest(TimeUnit.SECONDS.toMillis(2));
    doGranularityTest(TimeUnit.MINUTES.toMillis(1));
    doGranularityTest(TimeUnit.MINUTES.toMillis(2));
    doGranularityTest(1);
    doGranularityTest(2);
    doGranularityTest(15);
    assertInvalidGranularity(11); // 11 ms doesn't evenly divide 1 hour
    assertInvalidGranularity(-1); // negative and zero values not allowed
    assertInvalidGranularity(0);
    assertInvalidGranularity(maxAgeMillis); // granularity must be smaller than maxAgeMillis
    assertInvalidGranularity(maxAgeMillis + 1); // negative and zero values not allowed
  }

  private void assertInvalidGranularity(final long granularity) {
    AssertUtils.assertThrows(IllegalArgumentException.class, new Function0_t<Exception>() {
      public void call() throws Exception {
        doGranularityTest(granularity);
      }
    });
  }

  public void doGranularityTest(final long granularity) throws Exception {
    System.out.println("Testing with granularity = " + granularity);
    Clock.stop();
    TimeWindowCounter counter = new TimeWindowCounter("Test", maxAgeMillis, granularity);
    // align the clock to the next granularity boundary
    while (Clock.currentTimeMillis() % granularity != 0)
      Clock.advance(1);
    long startTime = Clock.currentTimeMillis();
    
    // count a few things
    counter.incr();
    counter.incr();
    assertEquals(2, counter.getCount());
    Clock.advance(granularity / 5);
    counter.add(5);
    assertEquals(7, counter.getCount());
    Clock.advance(granularity / 2);
    counter.add(-3);
    final int sum1 = counter.getCount();
    assertEquals(4, sum1);  // during this time interval, we have net value 4
    assertEquals(1, counter.size());  // they're all contained in one entry though, since we still haven't left the boundaries of the current granularity

    // add some values later in time
    Clock.set(startTime + granularity + 1);
    // count a few things
    counter.incr();
    Clock.advance(granularity / 3);
    counter.add(-2);
    final int sum2 = -1;  // net -1 during this interval
    assertEquals(sum1+sum2, counter.getCount());
    assertEquals(2, counter.size());  // now we have 2 entries

    // advance the clock so that the first batch of entries expires
    Clock.set(startTime + maxAgeMillis + 1);
    assertEquals(sum2, counter.getCount());  // only the value from the second batch should remain
    assertEquals(1, counter.size());

    // now advance the clock so that everything expires
    Clock.advance(granularity + 1);
    assertEquals(0, counter.getCount());
    assertEquals(0, counter.size());
  }

  public void testGetters() throws Exception {
    TimeWindowCounter counter = new TimeWindowCounter("TestCounter", 100, 20);
    assertEquals("TestCounter", counter.getName());
    assertEquals(100, counter.getMaxAgeMillis());
    assertEquals(20, counter.getGranularityMillis());
  }

  public void testToString() throws Exception {
    Clock.stop();
    TimeWindowCounter counter = new TimeWindowCounter("TestCounter", 3);
    assertEquals("{\"TestCounter\": 0}", counter.toString());
    counter.incr();
    counter.incr();
    assertEquals("{\"TestCounter\": 2}", counter.toString());
    Clock.advance(1);
    counter.incr();
    assertEquals("{\"TestCounter\": 3}", counter.toString());
    Clock.advance(3);
    assertEquals("{\"TestCounter\": 1}", counter.toString());
  }

  @Slow
  public void testMultithreaded() throws Exception {
    doMultithreadedTest(1);
    doMultithreadedTest(2);
    doMultithreadedTest(TimeUnit.SECONDS.toMillis(1));
    doMultithreadedTest(TimeUnit.SECONDS.toMillis(2));
    doMultithreadedTest(TimeUnit.MINUTES.toMillis(1));
    doMultithreadedTest(TimeUnit.MINUTES.toMillis(2));
  }

  private void doMultithreadedTest(long granularity) throws BrokenBarrierException, InterruptedException {
    int nThreads = 100;
    final TimeWindowCounter counter = new TimeWindowCounter("Test", maxAgeMillis, granularity);
    final AtomicInteger ourCounter = new AtomicInteger();

    Collection<Throwable> errors = new MultithreadedTestHarness(new Runnable() {
      public void run() {
        ourCounter.incrementAndGet();
        counter.incr();
      }
    }).run(nThreads, 10000);
    // verify that our reference counter matches the TimeWindowCounter value
    assertEquals(ourCounter.get(), counter.getCount());
    assertTrue(errors.isEmpty());
  }
}