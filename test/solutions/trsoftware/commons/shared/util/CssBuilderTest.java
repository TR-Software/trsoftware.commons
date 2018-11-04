package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

/**
 * @author Alex
 * @since 8/19/2018
 */
public class CssBuilderTest extends TestCase {

  public void testAppend() throws Exception {
    assertEquals("background-color: red; font-size: 2em;", new CssBuilder()
        .append("background-color", "red")
        .append("font-size", "2em")
        .toString()
    );
  }
}