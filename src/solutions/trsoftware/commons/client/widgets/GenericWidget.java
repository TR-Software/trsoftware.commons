package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows any {@link Element} to become a {@link Widget}
 *
 * @author Alex
 * @since 8/22/2018
 */
public class GenericWidget extends Widget implements HasHTML {

  /**
   * Wraps an existing {@link Element}
   * @param element the HTML element to be wrapped.
   */
  public GenericWidget(Element element) {
    setElement(element);
  }

  /**
   * Creates a new {@link Element} with the given tag name.
   * @param tagName HTML tag name (e.g. {@code div}, {@code h1}, etc.)
   */
  public GenericWidget(String tagName) {
    this(Document.get().createElement(tagName));
  }

  /**
   * Creates a new {@link Element} with the given tag name, and sets its {@code innerHTML} to the given string.
   * @param tagName HTML tag name (e.g. {@code div}, {@code h1}, etc.)
   * @param html the value to assign to the element's {@code innerHTML} property
   * @see #GenericWidget(String)
   * @see #setHTML(String)
   */
  public GenericWidget(String tagName, String html) {
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
}
