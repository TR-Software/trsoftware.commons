package solutions.trsoftware.commons.client.css;

import com.google.gwt.core.client.JavaScriptObject;

import javax.annotation.Nullable;

/**
 * The {@code CSSRule} interface represents a single CSS rule in a stylesheet.
 * <p>
 * There are several types of rules which inherit properties from {@code CSSRule}.
 * See the <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSRule">MDN web docs</a> for a complete listing
 * of the concrete sub-types.
 * <p>
 * <i>Note:</i> For now, this package provides an overlay only for the {@link CSSStyleRule} sub-type.
 * Overlays for other sub-classes (e.g. {@code CSSMediaRule}) may be implemented in the future using the same paradigm.
 *
 * @see CSSStyleSheet#getCssRules()
 * @see CSSRuleList
 * @see CSSStyleRule
 * @see <a href="https://caniuse.com/?search=CSSRule">Browser compatibility</a>
 */
public abstract class CSSRule extends JavaScriptObject {

  protected CSSRule() {
  }

  /**
   * Returns the textual representation of the rule, e.g. {@code "h1,h2 { font-size: 16pt }"}
   * or {@code "@import 'url'"}.
   * <p>
   * To access or modify parts of the rule (e.g. the value of "font-size" in the example) use the properties on
   * the specialized interface for the rule's type.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSRule/cssText">CSSRule.cssText</a>
   */
  public final native String getCssText() /*-{
    return this.cssText;
  }-*/;

  /**
   * Returns the containing rule, otherwise {@code null}.
   * E.g. if this rule is inside a {@code @media} block, the parent rule would be that {@code CSSMediaRule}.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSRule/parentRule">CSSRule.parentRule</a>
   */
  @Nullable
  public final native CSSRule getParentRule() /*-{
    return this.parentRule;
  }-*/;

  /**
   * Returns the style sheet that contains this rule.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSRule/parentStyleSheet">CSSRule.parentStyleSheet</a>
   */
  public final native CSSStyleSheet getParentStyleSheet() /*-{
    return this.parentStyleSheet;
  }-*/;

}
