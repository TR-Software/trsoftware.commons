package solutions.trsoftware.commons.client.css;

import solutions.trsoftware.commons.client.jso.ItemList;

/**
 * A {@link CSSRuleList} represents an ordered collection of read-only {@link CSSRule} objects.
 * <p>
 * While this object is read-only, and cannot be directly modified, it is considered a live object, as the
 * content can change over time.
 * <p>
 * To edit the underlying rules returned by {@link CSSRule} objects, use {@link CSSStyleSheet#insertRuleAtEnd(String)} and
 * {@link CSSStyleSheet#deleteRule(int)}, which are methods of CSSStyleSheet.
 * <p>
 * The interface has no constructor. An instance of CSSRuleList is returned by {@link CSSStyleSheet# CSSStyleSheet.cssRules} and
 * CSSKeyframesRule.cssRules.
 *
 * @author Alex
 * @since 5/24/2023
 */
public class CSSRuleList extends ItemList<CSSRule> {

  protected CSSRuleList() {
  }
}
