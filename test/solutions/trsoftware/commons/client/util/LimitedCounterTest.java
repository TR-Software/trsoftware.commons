package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import junit.framework.TestCase;

/**
 * Mar 26, 2010
 *
 * @author Alex
 */
public class LimitedCounterTest extends TestCase {

  public void testConstructor() throws Exception {
    // limit must be positive
    for (final int i : new int[]{-1, 0}) {
      AssertUtils.assertThrows(IllegalArgumentException.class,
          new Runnable() {
            public void run() {
              new LimitedCounter(i);
            }
          });
    }
    new LimitedCounter(1);  // limit == 1 is OK
  }

  public void testIncrementAndMet() throws Exception {
    {
      LimitedCounter c1 = new LimitedCounter(1);
      assertTrue(c1.increment()); // limit will be met on 1st incrementation
      verifyMetLimit(c1);
    }
    {
      LimitedCounter c2 = new LimitedCounter(2);
      assertFalse(c2.increment()); // limit will not be met on 1st incrementation
      assertTrue(c2.increment()); // limit will be met on 2nd incrementation
      verifyMetLimit(c2);
    }
    {
      LimitedCounter c100 = new LimitedCounter(100);
      for (int i = 0; i < 99; i++) {
        assertFalse(c100.increment()); // limit will not be met on the first 99 iterations
      }
      assertTrue(c100.increment()); // limit will be met on the 100th incrementation
      verifyMetLimit(c100);
    }
  }

  private void verifyMetLimit(LimitedCounter c) {
    // limit has alread been met; all subsequent calls to increment will return false
    for (int i = 0; i < 100; i++) {
      assertTrue(c.metLimit());
      assertFalse(c.increment());
    }
  }

}