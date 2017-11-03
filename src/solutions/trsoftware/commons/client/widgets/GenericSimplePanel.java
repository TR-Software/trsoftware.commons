package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows any {@link Element} to become a {@link SimplePanel}.
 *
 * @author Alex, 3/24/2015
 */
public class GenericSimplePanel extends SimplePanel {

  /**
   * @param elem the element to use for this widget.
   */
  public GenericSimplePanel(Element elem) {
    super(elem);
  }

  /**
   * @param elem the element to use for this widget.
   * @param child the child widget to add to it
   */
  public GenericSimplePanel(Element elem, Widget child) {
    this(elem);
    setWidget(child);
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
  public static GenericSimplePanel wrap(Element element) {
    // Assert that the element is attached.
    assert Document.get().getBody().isOrHasChild(element);

    GenericSimplePanel widget = new GenericSimplePanel(element);

    // Mark it attached and remember it for cleanup.
    widget.onAttach();
    RootPanel.detachOnWindowClose(widget);

    return widget;
  }

  /**
   * Shortcut for {@link #wrap(Element)}.
   * @param elementId the id of the element to be wrapped
   */
  public static GenericSimplePanel wrap(String elementId) {
    return wrap(DOM.getElementById(elementId));
  }

}
