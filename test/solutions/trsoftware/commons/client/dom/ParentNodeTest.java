package solutions.trsoftware.commons.client.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.util.Arrays;

import static solutions.trsoftware.commons.client.jso.JsDocumentTest.assertEmpty;

/**
 * @author Alex
 * @since 4/17/2019
 */
public class ParentNodeTest extends CommonsGwtTestCase {

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    RootPanel.get().add(new HTML(
        "<div id='foo' class='foo'>Foo<span id='bar' class='foo'>Bar</span></div><p class='baz'>Baz</p>"
    ));
  }
  
  public void testAs() throws Exception {
    // should fail for all JS objects except a Document or Element node 
    AssertUtils.assertThrows(AssertionError.class, (Runnable)() -> ParentNode.as(null));
    AssertUtils.assertThrows(AssertionError.class, (Runnable)() -> ParentNode.as(JavaScriptObject.createArray()));
    AssertUtils.assertThrows(AssertionError.class, (Runnable)() -> ParentNode.as(JavaScriptObject.createObject()));
    AssertUtils.assertThrows(AssertionError.class, (Runnable)() -> ParentNode.as(JavaScriptObject.createFunction()));
    AssertUtils.assertThrows(AssertionError.class, (Runnable)() -> ParentNode.as(Document.get().createTextNode("foo")));
    ParentNode.as(Document.get());
    ParentNode.as(Document.get().createSpanElement());
  }

  public void testIs() throws Exception {
    // should return false for all JS objects except a Document or Element node 
    assertFalse(ParentNode.is(null));
    assertFalse(ParentNode.is(JavaScriptObject.createArray()));
    assertFalse(ParentNode.is(JavaScriptObject.createObject()));
    assertFalse(ParentNode.is(JavaScriptObject.createFunction()));
    assertFalse(ParentNode.is(Document.get().createTextNode("foo")));
    assertTrue(ParentNode.is(Document.get()));
    assertTrue(ParentNode.is(Document.get().createSpanElement()));
  }

  public void testQuerySelectorAll() throws Exception {
    // 1) test selector with no matches
    for (Node node : Arrays.asList(Document.get(), RootPanel.get().getElement())) {
      assertEmpty(ParentNode.as(node).querySelectorAll(".asdfqwer"));
    }
    // 2) test with a single class selector
    for (Node node : Arrays.asList(Document.get(), RootPanel.get().getElement())) {
      ParentNode parentNode = ParentNode.as(node);
      NodeList<Element> nodeList = parentNode.querySelectorAll(".foo");
      assertNotNull(nodeList);
      assertEquals(2, nodeList.getLength());
      DivElement item0 = DivElement.as(nodeList.getItem(0));
      assertEquals("FooBar", item0.getInnerText());
      SpanElement item1 = SpanElement.as(nodeList.getItem(1));
      assertEquals("bar", item1.getId());
      assertEquals("Bar", item1.getInnerText());
    }
    {
      // query just the children of <div id="foo">
      ParentNode parentNode = ParentNode.as(Document.get().getElementById("foo"));
      NodeList<Element> nodeList = parentNode.querySelectorAll(".foo");
      assertNotNull(nodeList);
      assertEquals(1, nodeList.getLength());
      SpanElement item0 = SpanElement.as(nodeList.getItem(0));
      assertEquals("bar", item0.getId());
      assertEquals("Bar", item0.getInnerText());
    }
    // 3) test with multiple selectors
    for (Node node : Arrays.asList(Document.get(), RootPanel.get().getElement())) {
      ParentNode parentNode = ParentNode.as(node);
      NodeList<Element> nodeList = parentNode.querySelectorAll("#bar, .baz");
      assertNotNull(nodeList);
      assertEquals(2, nodeList.getLength());
      SpanElement item0 = SpanElement.as(nodeList.getItem(0));
      assertEquals("bar", item0.getId());
      assertEquals("Bar", item0.getInnerText());
      ParagraphElement item1 = ParagraphElement.as(nodeList.getItem(1));
      assertEquals("Baz", item1.getInnerText());
    }
    {
      // query just the children of <div id="foo">
      ParentNode parentNode = ParentNode.as(Document.get().getElementById("foo"));
      NodeList<Element> nodeList = parentNode.querySelectorAll("#bar, .baz");
      assertNotNull(nodeList);
      assertEquals(1, nodeList.getLength());
      SpanElement item0 = SpanElement.as(nodeList.getItem(0));
      assertEquals("bar", item0.getId());
      assertEquals("Bar", item0.getInnerText());
    }
  }

  /**
   * Same as {@code querySelectorAll}, but returns only the first match.
   */
  public void testQuerySelector() throws Exception {
    // 1) test selector with no matches
    for (Node node : Arrays.asList(Document.get(), RootPanel.get().getElement())) {
      assertEmpty(ParentNode.as(node).querySelectorAll(".asdfqwer"));
    }
    // 2) test with a single class selector
    for (Node node : Arrays.asList(Document.get(), RootPanel.get().getElement())) {
      ParentNode parentNode = ParentNode.as(node);
      Element result = parentNode.querySelector(".foo");
      assertNotNull(result);
      assertEquals("FooBar", DivElement.as(result).getInnerText());
    }
    {
      // query just the children of <div id="foo">
      ParentNode parentNode = ParentNode.as(Document.get().getElementById("foo"));
      Element result = parentNode.querySelector(".foo");
      assertNotNull(result);
      SpanElement item0 = SpanElement.as(result);
      assertEquals("bar", item0.getId());
      assertEquals("Bar", item0.getInnerText());
    }
    // 3) test with multiple selectors
    for (Node node : Arrays.asList(Document.get(), RootPanel.get().getElement())) {
      ParentNode parentNode = ParentNode.as(node);
      Element result = parentNode.querySelector("#bar, .baz");
      assertNotNull(result);
      SpanElement spanElement = SpanElement.as(result);
      assertEquals("bar", spanElement.getId());
      assertEquals("Bar", spanElement.getInnerText());
    }
  }

  
}