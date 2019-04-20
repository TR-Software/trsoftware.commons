package solutions.trsoftware.commons.client.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;

import javax.annotation.Nullable;

/**
 * Provides an implementation of the DOM <a href="https://developer.mozilla.org/en-US/docs/Web/API/ParentNode">ParentNode</a>
 * mixin, which is missing from {@link com.google.gwt.dom.client} package.
 * 
 * <blockquote cite="https://developer.mozilla.org/en-US/docs/Web/API/ParentNode">
 *   The ParentNode mixin contains methods and properties that are common to all types of Node objects
 *   that can have children.  It's implemented by Element, Document, and DocumentFragment objects.
 * </blockquote>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ParentNode">MDN Reference</a>
 * @author Alex
 * @since 4/12/2019
 */
public class ParentNode extends Node {

  protected ParentNode() {
  }

  /**
   * Assert that the given {@link JavaScriptObject} is a {@link ParentNode} and automatically typecast it.
   */
  public static ParentNode as(JavaScriptObject o) {
    assert is(o);
    return (ParentNode) o;
  }

  /**
   * Assert that the given {@link Node} is a {@link ParentNode} and automatically typecast it.
   */
  public static ParentNode as(Node node) {
    assert is(node);
    return (ParentNode)node;
  }
  
  /**
   * Determines whether the given {@link JavaScriptObject} can be cast to a {@link ParentNode}.
   * @return {@code true} iff the arg is not {@code null} and is either an Element or Document node.
   */
  public static boolean is(JavaScriptObject o) {
    return Node.is(o) && is((Node)o);
  }

  /**
   * Determines whether the given {@link Node} can be cast to a {@link ParentNode}.
   * @return {@code true} iff the arg is not {@code null} and is either an Element or Document node.
   */
  public static boolean is(Node node) {
    if (node != null) {
      short nodeType = node.getNodeType();
      if (nodeType == Node.ELEMENT_NODE || nodeType == Node.DOCUMENT_NODE)
        return true;
    }
    return false;
  }

  /**
   * Returns a {@link NodeList} of {@link Element Elements} matching the specified group of selectors
   * which are descendants of the object on which the method was called.
   * <p>
   * <b>Example:</b> {@code document.querySelectorAll("#main, p.warning, p.note")}
   *
   * @param selectors A DOMString containing one or more selectors to match against.
   * This string must be a valid CSS selector string; if it's not, a SyntaxError exception is thrown.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document_object_model/Locating_DOM_elements_using_selectors">
   *   Locating DOM elements using selectors</a> for more information about using selectors to identify elements.
   * Multiple selectors may be specified by separating them using commas.
   *
   * @return A non-live {@link NodeList} containing one {@link Element} object for each descendant element that matches
   * at least one of the specified selectors, or an empty {@link NodeList} in case of no matches.  Returns {@code null} if the method
   * is not supported by the current browser.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ParentNode/querySelectorAll">MDN Reference</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ParentNode/querySelectorAll#Browser_compatibility">Browser compatibility</a>
   * @see <a href="https://caniuse.com/#feat=queryselector">Can I use querySelector/querySelectorAll?</a>
   * @see #querySelector(String)
   */
  @Nullable
  public final native NodeList<Element> querySelectorAll(String selectors) /*-{
    if (typeof this.querySelectorAll === "function") {
      return this.querySelectorAll(selectors);
    }
    return null;
  }-*/;

  /**
   * Returns the first {@link Element} matching the specified group of selectors among the descendants of the object
   * on which the method was called.
   * If no matches are found, null is returned.
   * <p>
   * <b>Example:</b> {@code document.querySelector("#main, p.warning, p.note")}
   *
   * @param selectors A DOMString containing one or more selectors to match against.
   * This string must be a valid CSS selector string; if it's not, a SyntaxError exception is thrown.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document_object_model/Locating_DOM_elements_using_selectors">
   *   Locating DOM elements using selectors</a> for more information about using selectors to identify elements.
   * Multiple selectors may be specified by separating them using commas.
   *
   * @return the first {@link Element} that matches at least one of the specified selectors,
   * or {@code null} if no such element is found (or if the browser doesn't support this
   * method).
   *
   * @see #querySelectorAll(String)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ParentNode/querySelector">MDN Reference</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ParentNode/querySelector#Browser_compatibility">Browser compatibility</a>
   * @see <a href="https://caniuse.com/#feat=queryselector">Can I use querySelector/querySelectorAll?</a>
   */
  @Nullable
  public final native Element querySelector(String selectors) /*-{
    if (typeof this.querySelector === "function") {
      return this.querySelector(selectors);
    }
    return null;
  }-*/;

}
