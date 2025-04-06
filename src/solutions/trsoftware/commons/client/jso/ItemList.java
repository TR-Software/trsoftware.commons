package solutions.trsoftware.commons.client.jso;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NodeList;
import solutions.trsoftware.commons.client.css.CSSRuleList;
import solutions.trsoftware.commons.client.css.CSSStyleDeclaration;
import solutions.trsoftware.commons.shared.util.collections.ListAdapter;

import java.util.List;

/**
 * Provides a common interface for the various read-only or "live" array-like collection types
 * that have a {@code length} property and implement element access via an {@code item(index)} method.
 * <p>
 * There is a multitude of such classes defined in the various Web APIs, e.g.
 * {@link NodeList}, {@link CSSRuleList}, {@code TouchList}, {@link CSSStyleDeclaration}, etc.
 *
 * @see #asList()
 * @see <a href="https://dontcallmedom.github.io/webdex/i.html#item()%40%40TouchList%40method">
 *   Glossary of Web platform classes with an <code>item(index)</code> method</a>
 * @see <a href="https://stackoverflow.com/a/74641156">Discussion on StackOverflow</a>
 */
public abstract class ItemList<T> extends JavaScriptObject {

  protected ItemList() {
  }

  /**
   * @return the number of items in the collection
   */
  public final native int length() /*-{
    return this.length;
  }-*/;

  /**
   * Returns the element at the specified index.
   * <p>
   * <b>Note</b>: If given an invalid index (<code>index &lt; 0</code> or <code>index &ge; length</code>),
   * most implementations return {@code null} (e.g. {@code NodeList}, {@code TouchList}, {@code CSSRuleList})
   * but others could return something else (e.g. {@code CSSStyleDeclaration} returns an empty string).
   * Generally, this method doesn't throw an exception in such cases.
   *
   * @param index the element index, between {@code 0} and {@code length-1} (inclusive)
   * @return the element at the given index, or {@code null} or empty string if the index is not valid
   */
  public final native T item(int index) /*-{
    return this.item(index);
  }-*/;

  // convenience methods:

  /**
   * Returns an unmodifiable {@link List java.util.List} view of this native {@link ItemList} object.
   */
  public final List<T> asList() {
    return new ListAdapter<>(this::item, this::length);
  }

}
