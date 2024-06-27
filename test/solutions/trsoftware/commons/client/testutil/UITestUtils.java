package solutions.trsoftware.commons.client.testutil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.css.CSSStyleDeclaration;
import solutions.trsoftware.commons.client.jso.JsWindow;
import solutions.trsoftware.commons.shared.graphics.ColorRGB;

/**
 * @author Alex
 * @since 5/7/2024
 */
public abstract class UITestUtils {

  /**
   * Returns the given color value formatted like it would appear in a {@link CSSStyleDeclaration}
   * obtained from {@link JsWindow#getComputedStyle(Element) window.getComputedStyle(element)},
   * which might be in a different format than what was specified when setting a color property in JavaScript.
   * <p>
   * For example, the following assertion in a {@link GWTTestCase} is likely to fail because the browser might natively
   * represent the hex color string {@code "#b1c0e6"} as {@code "rgb(177, 192, 230)"}:
   * <pre>
   *   element.getStyle().setBackgroundColor("#b1c0e6");
   *   assertEquals("#b1c0e6", JsWindow.get().getComputedStyle(element).getBackgroundColor());  // fails
   * </pre>
   * It can be fixed with the help of this method by rewriting the above assertion as
   * <pre>
   *   assertEquals({@link #getComputedColorValue}("#b1c0e6"), JsWindow.get().getComputedStyle(element).getBackgroundColor());
   * </pre>
   * @param colorValue a color string being assigned to a {@link Style} property
   * @return the same color formatted as it would appear in {@code window.getComputedStyle(element)} after making that assignment
   */
  public static String getComputedColorValue(String colorValue) {
    Label dummyWidget = new Label();
    Element element = dummyWidget.getElement();
    element.getStyle().setBackgroundColor(colorValue);
    // attach to the DOM in order to get computed style
    RootPanel.get().add(dummyWidget);
    try {
      Style computedStyle = JsWindow.get().getComputedStyle(element);
      return computedStyle.getBackgroundColor();
    }
    finally {
      dummyWidget.removeFromParent();
    }
  }

  /**
   * Shortcut for <code>{@link #getComputedColorValue(String) getComputedColorValue}(colorValue.toString())</code>
   *
   * @param colorValue a color being assigned to a {@link Style} property
   * @return the same color formatted as it would appear in {@code window.getComputedStyle(element)} after making that assignment
   */
  public static String getComputedColorValue(ColorRGB colorValue) {
    return getComputedColorValue(colorValue.toString());
  }
}
