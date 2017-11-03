package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

/**
 * @author Alex, 10/16/2017
 */
public class HtmlElementBuilderTest extends TestCase {

  public void testSelfClosingTag() throws Exception {
    assertEquals("<foo id=\"1\" class=\"bar\" />", new HtmlElementBuilder("foo")
        .setAttribute("id", "1")
        .setAttribute("class", "bar").selfClosingTag());
  }

  public void testOpenTag() throws Exception {
    assertEquals("<foo id=\"1\" class=\"bar\">", new HtmlElementBuilder("foo")
        .setAttribute("id", "1")
        .setAttribute("class", "bar").openTag());
  }

  public void testCloseTag() throws Exception {
    assertEquals("</foo>", new HtmlElementBuilder("foo")
        .setAttribute("id", "1")
        .setAttribute("class", "bar").closeTag());
  }

}