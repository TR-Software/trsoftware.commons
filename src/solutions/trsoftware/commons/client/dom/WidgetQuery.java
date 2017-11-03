package solutions.trsoftware.commons.client.dom;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.util.GwtUtils;

/**
 * Performs lookups on the widget hierarchy (i.e. DOM).
 * 
 * Dec 27, 2009
 *
 * @author Alex
 */
public class WidgetQuery {
  /**
   * Finds the first ancestor of the given widget which is an instance of the
   * given concrete class (or a subclass of it).
   * @return an instance of the given class which is an ancestor of w or null
   * if not found.
   */
  public static <T extends Widget> T ancestorOf(Widget w, Class<T> parentClass) {
    Widget next = w;
    while ((next = next.getParent()) != null) {
      if (GwtUtils.isAssignableFrom(parentClass, next.getClass()))
        return (T)next;
    }
    return null;
  }

  /** If w is contained by a popup, hide that popup */
  public static void hideParentPopupPanel(Widget w) {
    // if this widget is shown inside a popup, hide that popup
    PopupPanel parentPopup = ancestorOf(w,  PopupPanel.class);
    if (parentPopup != null)
      parentPopup.hide();
  }

  /** @return the first parent whose element is different from this one (excludes {@link Composite}). */
  public static Widget getDomParent(Widget w) {
    Element elt = w.getElement();
    for (Widget parent = w.getParent(); parent != null; parent = parent.getParent()) {
      Element parentElt = parent.getElement();
      if (parentElt != null && parentElt != elt)
        return parent;
    }
    return null;
  }
}
