package solutions.trsoftware.commons.client;

import com.google.gwt.core.shared.GWT;

/**
 * Dec 4, 2008
 *
 * @author Alex
 */
public abstract class CommonsGwtWebTestCase extends CommonsGwtTestCase {

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    assertTrue(
        "This test needs to be run in web mode to properly exercise" +
            " emulated classes like solutions.trsoftware.commons.bridge.BridgeTypeFactory" +
            " and the JRE emulation library.  Give the -Dgwt.args=\"-web\" VM parameter" +
            " to the test runner to run the test in web mode.",
        GWT.isScript());
  }
}
