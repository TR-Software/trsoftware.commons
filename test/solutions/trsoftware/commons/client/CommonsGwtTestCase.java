package solutions.trsoftware.commons.client;

/**
 * A convenience base case that sets the module name, so that overriding
 * tests don't have to.
 *
 * @author Alex
 */
public abstract class CommonsGwtTestCase extends BaseGwtTestCase {

  public String getModuleName() {
    return "solutions.trsoftware.commons.TestCommons";
  }

}
