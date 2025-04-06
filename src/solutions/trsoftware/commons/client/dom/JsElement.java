package solutions.trsoftware.commons.client.dom;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Extends {@link Element} with additional DOM API methods and property accessors that aren't exposed by GWT.
 *
 * @author Alex
 * @since 12/5/2024
 */
public class JsElement extends Element {

  protected JsElement() {
  }

  /**
   * Asserts that the given {@link Node} is an {@link Element} and automatically
   * typecasts it.
   */
  public static JsElement as(@Nonnull Node node) {
    assert is(node);
    return (JsElement) node;
  }

  /**
   * Returns an instance of this class corresponding to the DOM element having the given id.
   *
   * @param elementId the unique id value for an element
   * @return the matching element or {@code null} if no such element exists
   * @see Document#getElementById(String)
   */
  public static JsElement getById(String elementId) {
    return (JsElement)Document.get().getElementById(elementId);
  }

  /**
   * @return {@code true} if the given node is a descendant of this element
   * @@see <a href={@code "https://developer.mozilla.org/en-US/docs/Web/API/Node/contains"}>MDN</a>
   */
  public native final boolean contains(Node node) /*-{
    return this.contains && this.contains(node);
  }-*/;

  /**
   * Returns a {@code DOMRect} object providing information about the size of this element
   * and its position relative to the viewport.
   * <p>
   * The returned value is the smallest rectangle which contains the entire element, including its padding and
   * border-width. The left, top, right, bottom, x, y, width, and height properties describe the position and size of
   * the overall rectangle in pixels. Properties other than width and height are relative to the top-left of the
   * viewport.
   * <p>
   * <b>Note:</b> the returned x and y coordinates could be negative if the window is currently scrolled such
   * that the element is outside the currently-visible viewport area.
   * The absolute coordinates can be obtained by adjusting for the window scroll offset, e.g.
   * <code>x + {@link Window#getScrollLeft()}</code> and <code>y + {@link Window#getScrollLeft()}</code>.
   * @see <a href={@code "https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect"}>MDN</a>
   *
   * @return the value of {@code getBoundingClientRect()} or {@code null} if the method isn't supported by the browser
   */
  public native final DOMRect getBoundingClientRect() /*-{
    return this.getBoundingClientRect && this.getBoundingClientRect();
  }-*/;

  /**
   * Returns a live {@link DOMTokenList} collection of the {@code class} attributes of the element.
   * This can then be used to manipulate the class list.
   * @return the {@code Element.classList} property
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/classList">MDN</a>
   */
  public native final DOMTokenList getClassList() /*-{
    return this.classList;
  }-*/;

  /**
   * The {@code outerHTML} attribute of the Element DOM interface gets the serialized HTML fragment describing the element
   * including its descendants. It can also be set to replace the element with nodes parsed from the given string.
   * <p>
   * <em>Note:</em> this getter is equivalent to GWT's {@link Element#getString()}.
   *
   * @return the value of this elements {@code outerHTML} property
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/outerHTML">MDN</a>
   * @see #getString()
   */
  public native final String getOuterHTML() /*-{
    return this.outerHTML;
  }-*/;

  /**
   * Replaces the element and all of its descendants with a new DOM tree constructed by parsing the specified html string.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/outerHTML">MDN</a>
   * @throws JavaScriptException:
   *   {@code SyntaxError} if the HTML string is not valid;
   *   {@code NoModificationAllowedError} if the element is a direct child of a Document, such as {@code Document.documentElement}
   */
  public native final void setOuterHTML(String html) /*-{
    this.outerHTML = html;
  }-*/;

  /**
   * Inserts the given element node into the DOM tree at the specified position relative to this element.
   * The position parameter is a string specifying the position relative to the element, which must be one of the following:
   * <ul>
   * <li>{@code "beforebegin"}: Before the element.</li>
   * <li>{@code "afterbegin"}: Just inside the element, before its first child.</li>
   * <li>{@code "beforeend"}: Just inside the element, after its last child.</li>
   * <li>{@code "afterend"}: After the element.</li>
   * </ul>
   * @param position position relative to the element; must be one of the following strings:
   *   {@code "beforebegin"}, {@code "afterbegin"}, {@code "beforeend"}, or {@code "afterend"}
   * @param element the  element to be inserted into the tree.
   * @throws JavaScriptException
   *   {@code SyntaxError} if position is not one of the four listed values.
   *   {@code TypeError} if the element specified is not a valid element.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentElement">MDN</a>
   */
  private native void insertAdjacentElement(String position, Element element) /*-{
    this.insertAdjacentElement(position, element);
  }-*/;

  /**
   * Parses the specified text as HTML or XML and inserts the resulting nodes into the DOM tree at the specified position
   * relative to this element.
   * The position parameter is a string specifying the position relative to the element, which must be one of the following:
   * <ul>
   * <li>{@code "beforebegin"}: Before the element. Only valid if the element is in the DOM tree and has a parent element.</li>
   * <li>{@code "afterbegin"}: Just inside the element, before its first child.</li>
   * <li>{@code "beforeend"}: Just inside the element, after its last child.</li>
   * <li>{@code "afterend"}: After the element. Only valid if the element is in the DOM tree and has a parent element.</li>
   * </ul>
   * @param position position relative to the element; must be one of the following strings:
   *   {@code "beforebegin"}, {@code "afterbegin"}, {@code "beforeend"}, or {@code "afterend"}
   * @param text the string to be parsed as HTML or XML and inserted into the tree.
   * @throws JavaScriptException
   *   {@code NoModificationAllowedError} if position is "beforebegin" or "afterend" and the element either does not have a parent or its parent is the Document object.
   *   {@code SyntaxError} if position is not one of the four listed values.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentHTML">MDN</a>
   */
  private native void insertAdjacentHTML(String position, String text) /*-{
    this.insertAdjacentHTML(position, text);
  }-*/;

  /**
   * Inserts a new text node into the DOM tree at the specified position relative to this element.
   * The position parameter is a string specifying the position relative to the element, which must be one of the following:
   * <ul>
   * <li>{@code "beforebegin"}: Before the element.</li>
   * <li>{@code "afterbegin"}: Just inside the element, before its first child.</li>
   * <li>{@code "beforeend"}: Just inside the element, after its last child.</li>
   * <li>{@code "afterend"}: After the element.</li>
   * </ul>
   * @param position position relative to the element; must be one of the following strings:
   *   {@code "beforebegin"}, {@code "afterbegin"}, {@code "beforeend"}, or {@code "afterend"}
   * @param text the string to be inserted as a text node
   * @throws JavaScriptException
   *   {@code SyntaxError} if position is not one of the four listed values.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentText">MDN</a>
   */
  private native void insertAdjacentText(String position, String text) /*-{
    this.insertAdjacentText(position, text);
  }-*/;


  /**
   * Inserts the given element node into the DOM tree at the specified position relative to this element:
   * <ul>
   * <li>{@code "beforebegin"}: Before the element.</li>
   * <li>{@code "afterbegin"}: Just inside the element, before its first child.</li>
   * <li>{@code "beforeend"}: Just inside the element, after its last child.</li>
   * <li>{@code "afterend"}: After the element.</li>
   * </ul>
   *
   * @param position position relative to this element
   * @param element the  element to be inserted into the tree.
   * @throws JavaScriptException {@code TypeError} if the element specified is not a valid element.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentElement">MDN</a>
   */
  public final void insertAdjacentElement(InsertPosition position, Element element) {
    insertAdjacentElement(position.value, element);
  }

  /**
   * Parses the specified text as HTML or XML and inserts the resulting nodes into the DOM tree at the specified position
   * relative to this element:
   * <ul>
   * <li>{@code "beforebegin"}: Before the element. Only valid if the element is in the DOM tree and has a parent element.</li>
   * <li>{@code "afterbegin"}: Just inside the element, before its first child.</li>
   * <li>{@code "beforeend"}: Just inside the element, after its last child.</li>
   * <li>{@code "afterend"}: After the element. Only valid if the element is in the DOM tree and has a parent element.</li>
   * </ul>
   * @param position position relative to this element
   * @param text the string to be parsed as HTML or XML and inserted into the tree.
   * @throws JavaScriptException
   *   {@code NoModificationAllowedError} if position is "beforebegin" or "afterend"
   *   and the element either does not have a parent or its parent is the Document object.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentHTML">MDN</a>
   */
  public final void insertAdjacentHTML(InsertPosition position, String text) {
    insertAdjacentHTML(position.value, text);
  }

  /**
   * Inserts a new text node into the DOM tree at the specified position relative to this element:
   * <ul>
   * <li>{@code "beforebegin"}: Before the element.</li>
   * <li>{@code "afterbegin"}: Just inside the element, before its first child.</li>
   * <li>{@code "beforeend"}: Just inside the element, after its last child.</li>
   * <li>{@code "afterend"}: After the element.</li>
   * </ul>
   *
   * @param position position relative to this element
   * @param text the string to be inserted as a text node
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/insertAdjacentText">MDN</a>
   */
  public final void insertAdjacentText(InsertPosition position, String text) {
    insertAdjacentText(position.value, text);
  }

  /**
   * The possible values of the {@code position} argument to the "insertAdjacent" methods.
   * <ul>
   * <li>{@code "beforebegin"}: Before the element.</li>
   * <li>{@code "afterbegin"}: Just inside the element, before its first child.</li>
   * <li>{@code "beforeend"}: Just inside the element, after its last child.</li>
   * <li>{@code "afterend"}: After the element.</li>
   * </ul>
   *
   * @see #insertAdjacentElement
   * @see #insertAdjacentHTML
   * @see #insertAdjacentText
   */
  public enum InsertPosition {
    /** Before the element itself. */
    BEFORE_BEGIN("beforebegin"),
    /** Just inside the element, before its first child. */
    AFTER_BEGIN("afterbegin"),
    /** Just inside the element, after its last child. */
    BEFORE_END("beforeend"),
    /** After the element itself. */
    AFTER_END("afterend");

    public final String value;

    InsertPosition(String value) {
      this.value = value;
    }
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
   * @see <a href="https://web.archive.org/web/20210509083527/https://developer.mozilla.org/en-US/docs/Web/API/ParentNode/querySelectorAll">MDN Reference</a>
   * @see <a href="https://web.archive.org/web/20210509083527/https://developer.mozilla.org/en-US/docs/Web/API/ParentNode/querySelectorAll#Browser_compatibility">Browser compatibility</a>
   * @see <a href="https://caniuse.com/#feat=queryselector">Can I use querySelector/querySelectorAll?</a>
   * @see #querySelector(String)
   */
  @Nullable
  public final NodeList<Element> querySelectorAll(String selectors) {
    return ParentNode.as(this).querySelectorAll(selectors);
  }

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
   * @see <a href="https://web.archive.org/web/20210509083527/https://developer.mozilla.org/en-US/docs/Web/API/ParentNode/querySelector">MDN Reference</a>
   * @see <a href="https://web.archive.org/web/20210509083527/https://developer.mozilla.org/en-US/docs/Web/API/ParentNode/querySelector#Browser_compatibility">Browser compatibility</a>
   * @see <a href="https://caniuse.com/#feat=queryselector">Can I use querySelector/querySelectorAll?</a>
   */
  @Nullable
  public final Element querySelector(String selectors) {
    return ParentNode.as(this).querySelector(selectors);
  }
}
