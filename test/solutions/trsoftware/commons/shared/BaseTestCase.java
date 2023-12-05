package solutions.trsoftware.commons.shared;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.Injections;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.time.Clock;

import java.util.Random;
import java.util.logging.Logger;

/**
 * @author Alex
 * @since 1/9/2023
 */
public class BaseTestCase extends TestCase {

  /**
   * Will have the same seed for each test to ensure repeatable results.
   */
  protected Random rnd;

  protected Injections injections;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Clock.resetToNormal();
    RandomUtils.setRnd(rnd = new Random(1));
    injections = new Injections();
  }

  @Override
  protected void tearDown() throws Exception {
    Clock.resetToNormal();
    rnd = null;
    injections.restoreAll();
    injections = null;
    super.tearDown();
  }

  /**
   * @return a logger named after the currently-running test
   * @see Logger#getLogger(String)
   */
  protected Logger getLogger() {
    return Logger.getLogger(getName());
  }
}
