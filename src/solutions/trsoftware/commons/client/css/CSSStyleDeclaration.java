package solutions.trsoftware.commons.client.css;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

import java.util.AbstractList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Extends the GWT {@link Style} class with some additional capabilities provided by the {@code CSSStyleDeclaration}
 * interface of the CSS Object Model.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration">MDN Reference</a>
 * @see Element#getStyle()
 *
 */
public class CSSStyleDeclaration extends Style {

  protected CSSStyleDeclaration() {
  }

  // property accessors:

  /**
   * The {@code CSSStyleDeclaration.cssText} property is a textual representation of the declaration block,
   * if and only if it is exposed via {@code HTMLElement.style}.  Setting this attribute changes the inline style.
   * If you want a text representation of a computed declaration block, you can get it with {@code JSON.stringify()}
   *
   * @return a string containing the text of the element's inline style declaration
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/cssText">MDN Reference</a>
   */
  public final native String getCssText() /*-{
    return this.cssText;
  }-*/;

  /**
   * The {@code CSSStyleDeclaration.cssText} property is a textual representation of the declaration block,
   * if and only if it is exposed via {@code HTMLElement.style}.  Setting this attribute changes the inline style.
   * If you want a text representation of a computed declaration block, you can get it with {@code JSON.stringify()}
   *
   * @param cssText a string containing the new text for the element's inline style declaration
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/cssText">MDN Reference</a>
   */
  public final native void setCssText(String cssText) /*-{
    return this.cssText = cssText;
  }-*/;


  /**
   * The {@code CSSStyleDeclaration.parentRule} read-only property returns a {@link CSSRule} that is the
   * parent of this style block, e.g. a {@link CSSStyleRule} representing the style for a CSS selector.
   *
   * @return the {@code CSSParentRuleDeclaration} object for the rule
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/parentRule">
   *   MDN Reference</a>
   */
  public final native CSSRule getParentRule() /*-{
    return this.parentRule;
  }-*/;


  // methods:

  /**
   * @return the number of properties
   * @see #item(int)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/length">MDN Reference</a>
   */
  public final native int length() /*-{
    return this.length;
  }-*/;

  /**
   * Returns the CSS property name at the specified index.
   *
   * @param index the element index, between {@code 0} and {@code length-1} (inclusive)
   * @return the name of the CSS property at the specified index, or an empty string if the index is invalid
   *   (<code>index &lt; 0</code> or <code>index &ge; length</code>)
   * @see #length() 
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/item">MDN Reference</a>
   */
  public final native String item(int index) /*-{
    return this.item(index);
  }-*/;

  /**
   * The {@code CSSStyleDeclaration.getPropertyPriority()} method returns a string that provides
   * all explicitly set priorities on the CSS property.
   *
   * @param name the property name to be checked (hyphen-case)
   * @return a string that represents the priority (e.g. {@code "important"}) if one exists.
   *   If none exists, returns the empty string.
   * @see #setProperty(String, String, String)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/getPropertyPriority">
   *   MDN Reference</a>
   */
  public final native String getPropertyPriority(String name) /*-{
    return this.getPropertyPriority(name);
  }-*/;

  /**
   * The {@code CSSStyleDeclaration.getPropertyValue()} method returns a string containing the
   * value of a specified CSS property (without the {@linkplain #getPropertyPriority(String) priority} suffix, if any).
   * <p>
   * <em>Note:</em> Unlike {@link Style#getProperty(String)}, this method requires the name to be <em>hyphen-case</em>
   * instead of camelCase (otherwise returns an empty string).
   *
   * @param name the property name to be checked (hyphen-case)
   * @return a string containing the value of the property. If not set, returns the empty string.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/getPropertyValue">
   *   MDN Reference</a>
   */
  public final native String getPropertyValue(String name) /*-{
    return this.getPropertyValue(name);
  }-*/;

  /**
   * The {@code CSSStyleDeclaration.setProperty()} method sets a new value for a property.
   * <p>
   * Notable differences from {@link Style#setProperty(String, String)}:
   * <ul>
   *   <li>
   *     allows setting the {@linkplain #getPropertyPriority(String) priority}
   *     (or removing the priority from an existing property)
   *     </li>
   *   <li>name must be hyphen-case instead of camelCase (otherwise has no effect)</li>
   * </ul>
   *
   * @param name the CSS property name (hyphen-case) to be modified
   * @param value the new property value
   * @param priority allows the "important" CSS priority to be set; the following values are accepted:
   *   {@code "important"}, {@code null}, or empty string
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/setProperty">
   *   MDN Reference</a>
   */
  public final native void setProperty(String name, String value, String priority) /*-{
    return this.setProperty(name, value, priority);
  }-*/;

  /**
   * The {@code CSSStyleDeclaration.removeProperty()} method removes a property from a CSS style declaration object.
   * <p>
   * This is equivalent to invoking {@link #setProperty(String, String, String)} with a {@code null} or empty string value.
   *
   * @param name the CSS property name (hyphen-case) to be removed
   * @return the value of the CSS property before it was removed
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleDeclaration/removeProperty">
   *   MDN Reference</a>
   */
  public final native String removeProperty(String name) /*-{
    return this.removeProperty(name);
  }-*/;

  /**
   * @return a list of all the property names comprising this style declaration
   */
  public final List<String> getPropertyNames() {
    /* TODO: unit test this; hosted mode throws:
        java.lang.IllegalAccessError: no such method: solutions.trsoftware.commons.client.css.CSSStyleDeclaration.item(int)String/invokeVirtual
        	at solutions.trsoftware.commons.client.css.CSSStyleDeclaration$.getPropertyNames$(CSSStyleDeclaration.java:160)
    */
//    return new ListAdapter<>(this::item, this::length);  // Note: can't use method refs here in hosted mode
    return new AbstractList<String>() {
      @Override
      public String get(int index) {
        return item(index);
      }

      @Override
      public int size() {
        return length();
      }
    };
  }

  /**
   * Parses a numeric string ending in {@code "px"}, such as a property value in a {@link CSSStyleDeclaration}
   *
   * @return the numeric value of the substring preceding the {@code "px"} suffix, or {@code 0} if the string
   * doesn't end in {@code "px"}
   *
   * @throws NullPointerException  if the string is null
   * @throws NumberFormatException if the string does not contain a parsable {@code double}.
   */
  public static double parsePx(String value) {
    requireNonNull(value, "value");
    if (value.endsWith("px")) {
      return Double.parseDouble(value.substring(0, value.length() - 2));
    }
    return 0d;  // TODO: maybe return NaN by default?
    /* TODO: in pure JS, this can be replaced by a simple parseFloat call (which automatically excludes any trailing non-numeric chars)
       (see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/parseFloat)
     */
  }

}
