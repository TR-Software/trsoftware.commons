package solutions.trsoftware.commons.client.validation;

import junit.framework.TestCase;

/**
 * This class uses RegExpValidationRuleTestBridge as a delegate (which provides
 * a way to call the same test methods from both Java and GWT test contexts).
 *
 * @author Alex
 */
public class RegexValidationRuleJavaTest extends TestCase {
  RegexValidationRuleGwtTest delegate = new RegexValidationRuleGwtTest();

  public void testRegexValidation() throws Exception {
    delegate.testRegexValidation();
  }
}