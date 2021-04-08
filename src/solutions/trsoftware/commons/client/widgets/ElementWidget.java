package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows any existing {@link Element} to become a {@link Widget}
 *
 * @author Alex
 * @since 8/22/2018
 */
public class ElementWidget extends Widget implements HasHTML {

  /**
   * Wraps an existing {@link Element}
   * @param element the HTML element to be wrapped.
   */
  protected ElementWidget(Element element) {
    setElement(element);
  }

  /**
   * Creates a new {@link Element} with the given tag name.
   * @param tagName HTML tag name (e.g. {@code div}, {@code h1}, etc.)
   */
  public ElementWidget(String tagName) {
    this(Document.get().createElement(tagName));
  }

  /**
   * Creates a new {@link Element} with the given tag name, and sets its {@code innerHTML} to the given string.
   * @param tagName HTML tag name (e.g. {@code div}, {@code h1}, etc.)
   * @param html the value to assign to the element's {@code innerHTML} property
   * @see #ElementWidget(String)
   * @see #setHTML(String)
   */
  public ElementWidget(String tagName, String html) {
    this(tagName);
    setHTML(html);
  }

  @Override
  public String getHTML() {
    return getElement().getInnerHTML();
  }

  @Override
  public void setHTML(String html) {
    getElement().setInnerHTML(html);
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }


  /**
   * Creates an instance that wraps an existing element.
   *
   * This element must already be attached to the document. If the element is
   * removed from the document, you must call {@link RootPanel#detachNow(Widget)}.
   *
   * This method is implemented the same way as other {@code wrap} methods provided by GWT, such as
   * {@link HTML#wrap(Element)}.
   *
   * @param element the element to be wrapped
   */
  public static ElementWidget wrap(Element element) {
    // Assert that the element is attached.
    assert Document.get().getBody().isOrHasChild(element);

    ElementWidget widget = new ElementWidget(element);

    // Mark it attached and remember it for cleanup.
    widget.onAttach();
    RootPanel.detachOnWindowClose(widget);

    return widget;
  }

  /**
   * Shortcut for {@link #wrap(Element)}.
   *
   * @param elementId the id of the element to be wrapped
   */
  public static ElementWidget wrap(String elementId) {
    return wrap(Document.get().getElementById(elementId));
  }
}
