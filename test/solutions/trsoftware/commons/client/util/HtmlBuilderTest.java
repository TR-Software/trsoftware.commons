package solutions.trsoftware.commons.client.util;
/**
 *
 * Date: Nov 4, 2008
 * Time: 4:05:59 PM
 * @author Alex
 */

import junit.framework.TestCase;

public class HtmlBuilderTest extends TestCase {


  public void testHtmlBuilder() throws Exception {
    HtmlBuilder builder = new HtmlBuilder()
        .openTag("a").attr("href", "http://example.com").attr("title", "Example link")
          .openTag("img").attr("src", "foo.jpg").closeTag()
          .innerHtml("Test link")
        .closeTag();
    assertEquals("<a href=\"http://example.com\" title=\"Example link\"><img src=\"foo.jpg\"/>Test link</a>",
        builder.toString());
  }

  public void testFlatStructure() throws Exception {
    // test the builder with no nesting of elements
    HtmlBuilder builder = new HtmlBuilder()
        .openTag("a").attr("href", "http://example.com").attr("title", "Example link").innerHtml("link 1").closeTag()
        .openTag("a").attr("href", "http://example2.com").innerHtml("link 2").closeTag();
    assertEquals(
        "<a href=\"http://example.com\" title=\"Example link\">link 1</a><a href=\"http://example2.com\">link 2</a>",
        builder.toString());
  }
}