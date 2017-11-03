package solutions.trsoftware.commons.server;

import junit.framework.Assert;

/**
 * Since Java doesn't have multiple inheritance, it's not possible
 * to mix mulple TestCase superclasses each with a different setUp and and tearDown
 * behavior.
 *
 * This class, in conjunction with SuperTestCase solves that problem.
 *
 * @author Alex
 */
public abstract class SetUpTearDownDelegate extends Assert {

  /** sublcasses should override to provide customized setUp logic */
  public abstract void setUp() throws Exception;

  /** sublcasses should override to provide customized tearDown logic */
  public abstract void tearDown() throws Exception;

}