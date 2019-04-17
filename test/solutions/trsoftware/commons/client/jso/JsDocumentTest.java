package solutions.trsoftware.commons.client.jso;

import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * @author Alex
 * @since 4/17/2019
 */
public class JsDocumentTest extends CommonsGwtTestCase {

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    RootPanel.get().add(new HTML(
        "<div class='foo'>Foo<span id='bar' class='foo'>Bar</span></div><p class='baz'>Baz</p>"
    ));
  }

  public void testGet() throws Exception {
    assertEquals(JsDocument.get(), Document.get());
  }

  public void testQuerySelectorAll() throws Exception {
    JsDocument doc = JsDocument.get();
    // 1) test selector with no matches
    assertEmpty(doc.querySelectorAll(".asdfqwer"));
    // 2) test with a single class selector
    {
      NodeList<Element> nodeList = doc.querySelectorAll(".foo");
      assertNotNull(nodeList);
      assertEquals(2, nodeList.getLength());
      DivElement item0 = DivElement.as(nodeList.getItem(0));
      assertEquals("FooBar", item0.getInnerText());
      SpanElement item1 = SpanElement.as(nodeList.getItem(1));
      assertEquals("bar", item1.getId());
      assertEquals("Bar", item1.getInnerText());
    }
    // 3) test with multiple selectors
    {
      NodeList<Element> nodeList = doc.querySelectorAll("#bar, .baz");
      assertNotNull(nodeList);
      assertEquals(2, nodeList.getLength());
      SpanElement item0 = SpanElement.as(nodeList.getItem(0));
      assertEquals("bar", item0.getId());
      assertEquals("Bar", item0.getInnerText());
      ParagraphElement item1 = ParagraphElement.as(nodeList.getItem(1));
      assertEquals("Baz", item1.getInnerText());
    }
  }

  /**
   * Same as {@code querySelectorAll}, but returns only the first match.
   */
  public void testQuerySelector() throws Exception {
    JsDocument doc = JsDocument.get();
    // 1) test selector with no matches
    assertNull(doc.querySelector(".asdfqwer"));
    // 2) test with a single class selector
    {
      Element result = doc.querySelector(".foo");
      assertNotNull(result);
      assertEquals("FooBar", DivElement.as(result).getInnerText());
    }
    // 3) test with multiple selectors
    {
      Element result = doc.querySelector("#bar, .baz");
      assertNotNull(result);
      SpanElement spanElement = SpanElement.as(result);
      assertEquals("bar", spanElement.getId());
      assertEquals("Bar", spanElement.getInnerText());
    }
  }

  public static void assertEmpty(NodeList nodeList) {
    assertTrue(nodeList == null || nodeList.getLength() == 0);
  }
}