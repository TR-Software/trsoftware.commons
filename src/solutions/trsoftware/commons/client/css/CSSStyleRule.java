package solutions.trsoftware.commons.client.css;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.function.Predicate;

/**
 * The CSSStyleRule interface represents a single CSS style rule.
 *
 * @see CSSStyleSheet#getCssRules()
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleRule">MDN Reference</a>
 */
public class CSSStyleRule extends CSSRule {

  protected CSSStyleRule() {
  }

  // property getters:

  /**
   * @return the textual representation of the selector for this rule, e.g. {@code "h1,h2"}
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleRule/selectorText">
   *   CSSStyleRule.selectorText property</a>
   */
  public final native String getSelectorText() /*-{
    return this.selectorText;
  }-*/;

  /**
   * @return the {@code CSSStyleDeclaration} object for the rule
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleRule/style">
   *   CSSStyleRule.style property</a>
   */
  public final native CSSStyleDeclaration getStyle() /*-{
    return this.style;
  }-*/;

  // casting / reflection:

  /**
   * Assert that the given {@link CSSRule} is compatible with this class and automatically typecast it.
   * <p>
   * Note: this method (in conjunction with {@link #is(JavaScriptObject)} can be used with a stream obtained from
   * {@link CSSRuleList#asList()} to get only the rules that instances of {@link CSSStyleRule}.
   *
   * @see #is(JavaScriptObject)
   */
  public static CSSStyleRule as(CSSRule rule) {
    assert is(rule);
    return (CSSStyleRule) rule;
  }

  /**
   * Determines whether the given {@link JavaScriptObject} can be cast to this class.
   * A {@code null} object will cause this method to return {@code false}.
   * <p>
   * Note: this method can be used with a stream obtained from {@link CSSRuleList#asList()}
   * to {@linkplain java.util.stream.Stream#filter(Predicate) filter} for instances of this class.
   * @see #as(CSSRule)
   */
  public static native boolean is(JavaScriptObject o) /*-{
    return o instanceof CSSStyleRule;
  }-*/;

}
