package solutions.trsoftware.commons.client.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Alex, 10/16/2017
 */
public class HtmlElementBuilder {

  private String tagName;
  private Map<String, String> attrs = new LinkedHashMap<String, String>();

  public HtmlElementBuilder(String tagName) {
    this.tagName = tagName;
  }

  public HtmlElementBuilder setAttribute(String key, String value) {
    attrs.put(key, value);
    return this;
  }

  /**
   * @return HTML string for the opening tag of this element.
   */
  public String openTag() {
    return toHtml(false);
  }

  /**
   * @return HTML string for the closing tag of this element.
   */
  public String closeTag() {
    return "</" + tagName + ">";
  }

  /**
   * @return HTML string for the full tag without a body. (e.g. {@code <img src="foo.png" />})
   */
  public String selfClosingTag() {
    return toHtml(true);
  }



  private String toHtml(boolean selfClosing) {
    StringBuilder out = new StringBuilder();
    out.append("<").append(tagName);
    for (Map.Entry<String, String> attr : attrs.entrySet()) {
      out.append(' ').append(attr.getKey()).append("=\"").append(attr.getValue()).append('"');
    }
    if (selfClosing)
      out.append(" /");
    out.append(">");
    return out.toString();
  }

}
