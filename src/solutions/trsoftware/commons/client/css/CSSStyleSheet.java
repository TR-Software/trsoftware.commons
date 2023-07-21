package solutions.trsoftware.commons.client.css;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.StyleElement;

/**
 * Overlay for the native {@code CSSStyleSheet} API.
 * <p>
 * An instance corresponding to a {@code <style>} or {@code <link>} element can be obtained
 * using {@link #getInstance(StyleElement)} or {@link #getInstance(LinkElement)}.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet">MDN Reference</a>
 * @see <a href="https://caniuse.com/?search=CSSStyleSheet">Browser Compatibility</a>
 */
public class CSSStyleSheet extends JavaScriptObject {

  protected CSSStyleSheet() {
  }

  /**
   * Retrieves the {@link CSSStyleSheet} instance associated with the given element.
   *
   * @param element a {@code <style>} element
   * @return the {@code CSSStyleSheet} instance obtained from the element's {@code .sheet} property
   */
  public static native CSSStyleSheet getInstance(StyleElement element) /*-{
    return element.sheet;
  }-*/;


  /**
   * Retrieves the {@link CSSStyleSheet} instance associated with the given element.
   *
   * @param element a {@code <link>} element
   * @return the {@code CSSStyleSheet} instance obtained from the element's {@code .sheet} property
   */
  public static native CSSStyleSheet getInstance(LinkElement element) /*-{
    return element.sheet;
  }-*/;

  // property getters:

  /**
   * Returns a live {@link CSSRuleList} which maintains an up-to-date list of the {@link CSSRule} objects
   * that comprise the stylesheet.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet/cssRules">MDN Reference</a>
   */
  public final native CSSRuleList getCssRules() /*-{
    return this.cssRules;
  }-*/;

  // instance methods:

  /**
   * Inserts a new CSS rule into the stylesheet without specifying the optional {@code index} parameter
   * (which defaults to {@code 0}).
   * <p>
   * <b>Note</b>: the {@code index} parameter was not optional in older browser versions
   * (see <a href="https://caniuse.com/mdn-api_cssstylesheet_insertrule_index_parameter_optional">compatibility table</a>).
   * To ensure maximum compatibility use {@link #insertRule(String, int)}.
   *
   * @param rule the full string of the rule to be inserted, including the selector and style declaration
   *     (e.g. {@code "#blanc {color: white}"} for rule-sets, or {@code @charset "utf-8"} for at-rules)
   * @return the newly inserted rule's index within the stylesheet's rule-list
   * @see #insertRuleAtEnd(String)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet/insertRule">MDN Reference</a>
   */
  public final native int insertRule(String rule) /*-{
    return this.insertRule(rule);
  }-*/;

  /**
   * Inserts a new CSS rule into the stylesheet.
   *
   * @param rule the full string of the rule to be inserted, including the selector and style declaration
   *     (e.g. {@code "#blanc {color: white}"} for rule-sets, or {@code @charset "utf-8"} for at-rules)
   * @param index positive integer less than or equal to {@code stylesheet.cssRules.length}, representing the newly
   *     inserted rule's position in CSSStyleSheet.cssRules
   * @return the newly inserted rule's index within the stylesheet's rule-list
   * @see #insertRule(String)
   * @see #insertRuleAtEnd(String)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet/insertRule">MDN Reference</a>
   */
  public final native int insertRule(String rule, int index) /*-{
    return this.insertRule(rule, index);
  }-*/;

  /**
   * Inserts a new CSS rule at the end of the stylesheet.
   * This is equivalent to invoking {@code insertRule(rule, getCssRules().length())}
   *
   * @param rule the full string of the rule to be inserted, including the selector and style declaration
   *     (e.g. {@code "#blanc {color: white}"} for rule-sets, or {@code @charset "utf-8"} for at-rules)
   * @return the newly inserted rule's index within the stylesheet's rule-list
   * @see #insertRule(String)
   * @see #insertRule(String, int)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet/insertRule">MDN Reference</a>
   */
  public final native int insertRuleAtEnd(String rule) /*-{
    return this.insertRule(rule, this.cssRules.length);
  }-*/;

  /**
   * Removes a rule from the stylesheet object.
   * <p>
   * <em>Caution:</em> the index returned from {@link #insertRule} may no longer point to the same rule
   * if the stylesheet has been modified after the insertion.
   *
   * @param index the index into the stylesheet's {@link #getCssRules() CSSRuleList} indicating the rule to be removed.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet/deleteRule">MDN Reference</a>
   */
  public final native void deleteRule(int index) /*-{
    this.deleteRule(index);
  }-*/;

  /**
   * Appends a new rule to the stylesheet.
   * <p>
   * <b>NOTE</b>: this is considered a <em>legacy method</em>, superceded by {@link #insertRule(String, int)}.
   *
   * @param selector the selector portion of the CSS rule
   * @param styleBlock the style block to apply to elements matching the selector
   * @param index An optional index into the stylesheet's CSSRuleList at which to insert the new rule.
   *     If index is not specified (see {@link #addRule(String, String)}), the next index after the last item
   *     currently in the list is used (that is, the value of cssStyleSheet.cssRules.length).
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet/addRule">MDN Reference</a>
   * @deprecated use {@link #insertRule(String, int)} or {@link #insertRuleAtEnd(String)}
   *   for compliance with current web standards
   */
  public final native void addRule(String selector, String styleBlock, int index) /*-{
    this.addRule(selector, styleBlock, index);
  }-*/;

  /**
   * Appends a new rule to the stylesheet.
   * <p>
   * <b>NOTE</b>: this is considered a <em>legacy method</em>, superceded by {@link #insertRule(String, int)}.
   *
   * @param selector the selector portion of the CSS rule
   * @param styleBlock the style block to apply to elements matching the selector
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet/addRule">MDN Reference</a>
   * @deprecated use {@link #insertRule(String, int)} or {@link #insertRuleAtEnd(String)}
   *   for compliance with current web standards
   */
  public final native void addRule(String selector, String styleBlock) /*-{
    this.addRule(selector, styleBlock);
  }-*/;
}
