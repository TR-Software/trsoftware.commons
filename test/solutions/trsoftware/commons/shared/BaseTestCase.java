package solutions.trsoftware.commons.shared;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.time.Clock;

import java.util.logging.Logger;

/**
 * @author Alex
 * @since 1/9/2023
 */
public class BaseTestCase extends TestCase {

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

  /**
   * @return a logger named after the currently-running test
   * @see Logger#getLogger(String)
   */
  protected Logger getLogger() {
    return Logger.getLogger(getName());
  }
}
