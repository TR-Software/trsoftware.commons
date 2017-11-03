package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.ui.Anchor;

/**
 * These utility methods are useful for creating raw HTML elements with GWT,
 * for the HTML nad HTMLPanel widgets.
 *
 * @author Alex
 */
public class HtmlUtils {

  public static final String nbsp = "&nbsp;";

  /**
   * No-op value for the {@code href} attribute of an anchor element.
   * @see Anchor#DEFAULT_HREF
   */
  public static final String VOID_HREF = "javascript:;";

  /**
   * Returns a "<span id=...</span>" string
   * Warning - the body string is not escaped and allows valid HTML.
   */
  public static String span(String id, String cssClassName, String body) {
    return element("span", id, cssClassName, body);
  }

  /**
   * Returns a "<div id=...</div>" string
   * Warning - the body string is not escaped and allows valid HTML.
   */
  public static String div(String id, String cssClassName, String body) {
    return element("div", id, cssClassName, body);
  }

  /**
   * Returns a "<elementName id=...</elementName>" string
   * Warning - the body string is not escaped and allows valid HTML. 
   */
  public static String element(String elementName, String id, String cssClassName, String innerHtml) {
    if (innerHtml == null)
      innerHtml = "";
    return "<" + elementName + attr("id", id) + attr("class", cssClassName) + ">" + innerHtml + "</"+elementName+">";
  }

  /**
   * Returns a "<elementName id=...</elementName>" string
   * Warning - the body string is not escaped and allows valid HTML.
   */
  public static String element(String elementName, String innerHtml) {
    if (innerHtml == null)
      innerHtml = "";
    return "<" + elementName + ">" + innerHtml + "</"+elementName+">";
  }

  /**
   * Returns a name="value" string if the value arg is not null
   */
  private static String attr(String name, String value) {
    if (value == null)
      return "";
    return " " + name + "=\"" + value + "\"";
  }
}
