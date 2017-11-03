package solutions.trsoftware.commons.client.util;
/**
 *
 * Date: May 4, 2008
 * Time: 8:14:28 PM
 * @author Alex
 */

import junit.framework.TestCase;

public class HtmlUtilsTest extends TestCase {

  public void testSpan() {
    assertEquals("<span id=\"foo\" class=\"bar\"></span>", HtmlUtils.span("foo", "bar", ""));
    assertEquals("<span class=\"bar\"></span>", HtmlUtils.span(null, "bar", null));
    assertEquals("<span></span>", HtmlUtils.span(null, null, null));
  }

  public void testDiv() {
    assertEquals("<div id=\"foo\" class=\"bar\"></div>", HtmlUtils.div("foo", "bar", ""));
    assertEquals("<div class=\"bar\"></div>", HtmlUtils.div(null, "bar", null));
    assertEquals("<div></div>", HtmlUtils.div(null, null, null));
  }

  public void testArbitraryElement() {
    assertEquals("<hr id=\"foo\" class=\"bar\"></hr>", HtmlUtils.element("hr", "foo", "bar", ""));
    assertEquals("<hr class=\"bar\"></hr>", HtmlUtils.element("hr", null, "bar", null));
    assertEquals("<hr></hr>", HtmlUtils.element("hr", null, null, null));
  }
}