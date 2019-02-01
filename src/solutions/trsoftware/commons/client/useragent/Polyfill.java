package solutions.trsoftware.commons.client.useragent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsDate;
import solutions.trsoftware.commons.shared.util.LazyReference;

/**
 * Can be replaced via deferred binding for older browsers (e.g. IE8) to provide implementations
 * of various newer JavaScript functions based on the polyfills from MDN (e.g.
 * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString#Polyfill">
 *   Date.toISOString()</a>)
 *
 * @author Alex
 * @since 1/30/2019
 */
public class Polyfill {

  private static final LazyReference<Polyfill> instance = new LazyReference<Polyfill>() {
    @Override
    protected Polyfill create() {
      return GWT.create(Polyfill.class);
    }
  };

  public static Polyfill get() {
    return instance.get();
  }

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString">
   *   MDN: Date.prototype.toISOString()</a>
   * @see solutions.trsoftware.commons.client.jso.JsDate#toISOString()
   */
  public native String toISOString(JsDate date) /*-{
    return date.toISOString();
  }-*/;
}
