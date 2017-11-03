package solutions.trsoftware.commons.client.validation;

import com.google.gwt.core.shared.GWT;

/**
 * Apr 6, 2011
 *
 * @author Alex
 */
public abstract class RegexValidationRuleGwtTestCase extends ValidationRuleGwtTestCase {

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    if (!GWT.isScript()) {
      System.err.println("WARNING: This test must be run in \"web\" mode to use Javascript's (not Java's) regex facilities (use -Dgwt.args=\"-web\" on the command line");
    }
  }
}
