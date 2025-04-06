package solutions.trsoftware.commons.client.dom;

import solutions.trsoftware.commons.client.jso.ItemList;

/**
 * JSNI overlay for the native {@code DOMTokenList} interface, which represents a set of space-separated tokens.
 * Such a set is returned by {@code Element.classList} or {@code HTMLLinkElement.relList}, and many others.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMTokenList">MDN Reference</a>
 * @author Alex
 * @since 12/18/2024
 */
public class DOMTokenList extends ItemList<String> {

  protected DOMTokenList() {
  }

  /**
   * @return the value of the list as a space-delimited string
   */
  public native final String getValue() /*-{
    return this.value;
  }-*/;

  /**
   * Alias for {@link #getValue()}.
   * @return the value of the list as a space-delimited string
   */
  public final String getString() {
    return getValue();
  }

  /**
   * @return {@code true} iff the list contains the given token
   */
  public native final boolean contains(String token) /*-{
    return this.contains(token);
  }-*/;

  /**
   * Adds the given token to the list, if not already present.
   */
  public native final void add(String token) /*-{
    this.add(token);
  }-*/;

  /**
   * Adds the given tokens to the list, if not already present.
   */
  public final void add(String... tokens) {
    for (String token : tokens) {
      add(token);
    }
  }

  /**
   * Removes the given token from the list, if present.
   */
  public native final void remove(String token) /*-{
    this.remove(token);
  }-*/;

  /**
   * Removes the given tokens from the list, if present.
   */
  public final void remove(String... tokens) {
    for (String token : tokens) {
      remove(token);
    }
  }

  /**
   * Replaces an existing token with a new token. If the first token doesn't exist,
   * returns {@code false} immediately, without adding the new token to the token list.
   *
   * @return {@code true} iff token was successfully replaced
   */
  public native final boolean replace(String oldToken, String newToken) /*-{
    return this.replace(oldToken, newToken);
  }-*/;

  /**
   * Removes an existing token from the list and returns {@code false}.
   * If the token doesn't exist it's added and the function returns {@code true}.
   *
   * @return {@code true} if token was added; {@code false} if removed
   */
  public native final boolean toggle(String token) /*-{
    return this.toggle(token);
  }-*/;

}
