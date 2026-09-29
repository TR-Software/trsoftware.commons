package solutions.trsoftware.commons.client.jso.history;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;

import javax.annotation.Nullable;

/**
 * Overlay for the native {@code History} object, and main interface for the JS History API.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API (MDN)</a>
 * @since 9/24/2026
 */
public class History extends JavaScriptObject {

  protected History() {
  }

  /**
   * @return the {@code window.history} object, or {@code null} if this API is not supported by the current browser
   */
  @Nullable
  public static native History get() /*-{
    return $wnd.history;
  }-*/;

  /**
   * The {@code length} read-only property of the History interface returns an integer representing
   * the number of entries in the session history, including the currently loaded page.
   *
   * For example, for a page loaded in a new tab this property returns 1.
   */
  public final native int length() /*-{
    return this.length;
  }-*/;

  /**
   * The {@code scrollRestoration} property of the History interface allows web applications
   * to explicitly set default scroll restoration behavior on history navigation.
   * <p>Values:
   * <ul>
   *   <li><i>auto</i>: The location on the page to which the user has scrolled will be restored.</li>
   *   <li><i>manual</i>: The location on the page is not restored. The user will have to scroll to the location manually.</li>
   * </ul>
   *
   * @return the current value of the property, or {@code null} if not supported by the current browser
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/History/scrollRestoration#browser_compatibility">
   *   Browser Compatibility</a>
   */
  public final native String scrollRestoration() /*-{
    return this.scrollRestoration;
  }-*/;

  /**
   * Setter for the {@link #scrollRestoration() scrollRestoration} property.
   *
   * @param scrollRestoration either {@code "auto"} or {@code "manual"}
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/History/scrollRestoration#browser_compatibility">
   *   Browser Compatibility</a>
   */
  public final native void scrollRestoration(String scrollRestoration) /*-{
    return this.scrollRestoration = scrollRestoration;
  }-*/;


  /**
   * The {@code pushState()} method of the History interface adds an entry to the browser's session history stack.
   *
   * @param state a JavaScript object which is associated with the new history entry created by {@code pushState()}.
   *   Whenever the user navigates to the new state, a {@code popstate} event is fired,
   *   and the state property of the event contains a copy of the history entry's state object.
   *   The state object can be anything that can be serialized.
   * @param url The new history entry's URL.
   *   Note that the browser won't attempt to load this URL after a call to {@code pushState()},
   *   but it may attempt to load the URL later, for instance, after the user restarts the browser.
   *   The new URL does not need to be absolute; if it's relative, it's resolved relative to the current URL.
   *   The new URL must be of the same origin as the current URL; otherwise, will throw an exception.
   *   If this parameter isn't specified, it's set to the document's current URL.
   * @throws JavaScriptException
   *   {@code SecurityError} is thrown if the associated document is not fully active,
   *   or if the provided url parameter is not a valid URL, or if the method is called too frequently.
   *   <p>{@code DataCloneError} is thrown if the provided state parameter is not serializable.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event"><tt>popstate</tt> event</a>
   */
  public final native void pushState(JavaScriptObject state, String url) /*-{
    this.pushState(state, "", url);
  }-*/;

  /**
   * Same as {@link #pushState(JavaScriptObject, String)} without specifying the optional URL parameter,
   * which defaults to to the document's current URL.
   *
   * @param state a JavaScript object which is associated with the new history entry created by {@code pushState()}.
   *   Whenever the user navigates to the new state, a {@code popstate} event is fired,
   *   and the state property of the event contains a copy of the history entry's state object.
   *   The state object can be anything that can be serialized.
   * @throws JavaScriptException
   *   {@code SecurityError} is thrown if the associated document is not fully active,
   *   or if the provided url parameter is not a valid URL, or if the method is called too frequently.
   *   <p>{@code DataCloneError} is thrown if the provided state parameter is not serializable.
   */
  public final native void pushState(JavaScriptObject state) /*-{
    this.pushState(state, "");
  }-*/;


  /**
   * The {@code replaceState()} method of the History interface modifies the current history entry,
   * replacing it with the state object and URL passed in the method parameters.
   * This method is particularly useful when you want to update the state object or URL
   * of the current history entry in response to some user action.
   *
   * @param state a new JavaScript object to be associated with the current history entry.
   *   The state object can be anything that can be serialized, or {@code null}
   * @param url the URL of the history entry.
   *   The new URL must be of the same origin as the current URL; otherwise this method throws an exception.
   * @throws JavaScriptException
   *   {@code SecurityError} is thrown if the associated document is not fully active,
   *   or if the provided url parameter is not a valid URL, or if the method is called too frequently.
   *   <p>{@code DataCloneError} is thrown if the provided state parameter is not serializable.
   *
   * @see #pushState(JavaScriptObject, String)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event"><tt>popstate</tt> event</a>
   */
  public final native void replaceState(@Nullable JavaScriptObject state, String url) /*-{
    this.replaceState(state, "", url);
  }-*/;

  /**
   * Same as {@link #replaceState(JavaScriptObject, String)} without specifying the optional {@code url} parameter.
   *
   * @param state a new JavaScript object to be associated with the current history entry.
   *   The state object can be anything that can be serialized, or {@code null}
   * @throws JavaScriptException
   *   {@code SecurityError} is thrown if the associated document is not fully active,
   *   or if the provided url parameter is not a valid URL, or if the method is called too frequently.
   *   <p>{@code DataCloneError} is thrown if the provided state parameter is not serializable.
   * @see #replaceState(JavaScriptObject, String)
   * @see #pushState(JavaScriptObject, String)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event"><tt>popstate</tt> event</a>
   */
  public final native void replaceState(@Nullable JavaScriptObject state) /*-{
    this.replaceState(state, "", url);
  }-*/;

  /**
   * The {@code go()} method of the History interface loads a specific page from the session history.
   * You can use it to move forwards and backwards through the history depending on the value of a parameter.
   * <p>
   * This method is asynchronous. Add a listener for the {@code popstate} event in order
   * to determine when the navigation has completed.
   *
   * @param delta The position in the history to which you want to move, relative to the current page.
   *   A negative value moves backwards, a positive value moves forwards.
   *   So, for example, {@code history.go(2)} moves forward two pages and {@code history.go(-2)} moves back two pages.
   *   If no value is passed or if delta equals 0, it has the same result as calling {@code location.reload()}
   * @throws JavaScriptException {@code SecurityError} is thrown if the associated document is not fully active.
   *   Browsers also throttle navigations and may throw this error, generate a warning,
   *   or ignore the call if it's called too frequently.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event"><tt>popstate</tt> event</a>
   */
  public final native void go(int delta) /*-{
    this.go(delta);
  }-*/;

  /**
   * The {@code back()} method of the History interface causes the browser to move back one page in the session history.
   * It has the same effect as calling {@code history.go(-1)}. If there is no previous page, this method call does nothing.
   * <p>
   * This method is asynchronous. Add a listener for the {@code popstate} event in order
   * to determine when the navigation has completed.
   *
   * @throws JavaScriptException {@code SecurityError} is thrown if the associated document is not fully active.
   *   Browsers also throttle navigations and may throw this error, generate a warning,
   *   or ignore the call if it's called too frequently.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event"><tt>popstate</tt> event</a>
   */
  public final native void back() /*-{
    this.back();
  }-*/;

  /**
   * The {@code forward()} method of the History interface causes the browser to move forward one page in the session history.
   * It has the same effect as calling {@code history.go(1)}.
   * <p>
   * This method is asynchronous. Add a listener for the {@code popstate} event in order
   * to determine when the navigation has completed.
   *
   * @throws JavaScriptException {@code SecurityError} is thrown if the associated document is not fully active.
   *   Browsers also throttle navigations and may throw this error, generate a warning,
   *   or ignore the call if it's called too frequently.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event"><tt>popstate</tt> event</a>
   */
  public final native void forward() /*-{
    this.forward();
  }-*/;
  
  




}
