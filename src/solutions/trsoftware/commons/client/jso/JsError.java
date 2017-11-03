package solutions.trsoftware.commons.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A JSNI overlay type for a native JS error.
 *
 * Supports a subset of the methods & properties provided by the various browser implementations.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error">Mozilla Error Object Reference</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/ie/dww52sbt(v=vs.94).aspx">MSDN Error Object Reference</a>
 * 
 * @since Mar 26, 2013
 * @author Alex
 */
public class JsError extends JavaScriptObject {

  // Overlay types always have protected, zero-arg constructors, because the object must have been instantiated in javascript
  protected JsError() { }

  public final native String getName() /*-{
    return this.name;
  }-*/;

  public final native String getMessage() /*-{
    return this.message;
  }-*/;

}
