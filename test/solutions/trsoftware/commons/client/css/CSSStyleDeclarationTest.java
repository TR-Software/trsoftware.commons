package solutions.trsoftware.commons.client.css;

import com.google.gwt.dom.client.Element;
import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.lenientFormat;

/**
 * @author Alex
 * @since 8/4/2024
 */
public class CSSStyleDeclarationTest extends CommonsGwtTestCase {

  @DoNotRunWith({Platform.HtmlUnitBug})  // CSSStyleDeclaration.item(int) always returns null in HtmlUnit
  public void testGetPropertyNames() throws Exception {
    Widget widget = new Label(getName());
    RootPanel.get().add(widget);
    Element element = widget.getElement();
    CSSStyleDeclaration style = element.getStyle().cast();
    style.setBackgroundColor("red");
    style.setProperty("font-style", "italic", "important");
    getLogger().info("style.getCssText() = " + style.getCssText());

    int length = style.length();
    getLogger().info("length = " + length);
    for (int i = 0; i < length; i++) {
      getLogger().info(lenientFormat("style.item(%s) = %s", i, style.item(i)));
    }

    List<String> propertyNames = style.getPropertyNames();
    getLogger().info("propertyNames = " + propertyNames);
    assertEquals(Arrays.asList("background-color", "font-style"), propertyNames);
  }
}