package solutions.trsoftware.commons.client.util.template;

import junit.framework.TestCase;

/**
 * This class uses StringTemplateParserGwtTest as a delegate (which provides a
 * way to call the same test methods from both Java and GWT test contexts).
 *
 * The one restriction is that StringTemplateParserGwtTest must not use any set
 * up code (must not override the gwtSetUp() method.
 *
 * @author Alex
 */
public class SimpleTemplateParserJavaTest extends TestCase {
  SimpleTemplateParserGwtTest delegate = new SimpleTemplateParserGwtTest();

  public void testStringTemplate() throws Exception {
    delegate.testStringTemplate();
  }
}