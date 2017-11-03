package solutions.trsoftware.commons.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A JSNI overlay type for the window.performance.timing object. Supports a subset of the methods provided by that API.
 *
 * NOTE: {@link elemental.js.html.JsPerformanceTiming} is a more full-featured implementation of this concept and is
 * part of GWT's experimental new "Elemental" package. However, that class doesn't compensate for lack of functionality
 * of certain methods, and also Elemental only works with SuperDevMode (will produce a GWT compiler error when running
 * under the regular DevMode, see http://stackoverflow.com/questions/17428265/adding-elemental-to-gwt )
 *
 * @author Alex
 * @see <a href="http://msdn.microsoft.com/en-us/ff975075(v=vs.85)">MSDN Reference</a>
 * @see <a href="http://caniuse.com/#search=performance">List of browser versions that support this API</a>
 * @since 11/5/2014
 */
public class JsPerformanceTiming extends JavaScriptObject {

  // Overlay types always have protected, zero-arg constructors, because the object must have been instantiated in javascript
  protected JsPerformanceTiming() {
  }

  public static native JsPerformanceTiming get() /*-{
    var performance = $wnd.performance;
    return performance ? performance.timing : null;
  }-*/;

  public final native double getConnectEnd() /*-{
    return this.connectEnd;
  }-*/;

  public final native double getConnectStart() /*-{
    return this.connectStart;
  }-*/;

  public final native double getDomComplete() /*-{
    return this.domComplete;
  }-*/;

  public final native double getDomContentLoadedEventEnd() /*-{
    return this.domContentLoadedEventEnd;
  }-*/;

  public final native double getDomContentLoadedEventStart() /*-{
    return this.domContentLoadedEventStart;
  }-*/;

  public final native double getDomInteractive() /*-{
    return this.domInteractive;
  }-*/;

  public final native double getDomLoading() /*-{
    return this.domLoading;
  }-*/;

  public final native double getDomainLookupEnd() /*-{
    return this.domainLookupEnd;
  }-*/;

  public final native double getDomainLookupStart() /*-{
    return this.domainLookupStart;
  }-*/;

  public final native double getFetchStart() /*-{
    return this.fetchStart;
  }-*/;

  public final native double getLoadEventEnd() /*-{
    return this.loadEventEnd;
  }-*/;

  public final native double getLoadEventStart() /*-{
    return this.loadEventStart;
  }-*/;

  public final native double getNavigationStart() /*-{
    return this.navigationStart;
  }-*/;

  public final native double getRedirectEnd() /*-{
    return this.redirectEnd;
  }-*/;

  public final native double getRedirectStart() /*-{
    return this.redirectStart;
  }-*/;

  public final native double getRequestStart() /*-{
    return this.requestStart;
  }-*/;

  public final native double getResponseEnd() /*-{
    return this.responseEnd;
  }-*/;

  public final native double getResponseStart() /*-{
    return this.responseStart;
  }-*/;

  public final native double getSecureConnectionStart() /*-{
    return this.secureConnectionStart;
  }-*/;

  public final native double getUnloadEventEnd() /*-{
    return this.unloadEventEnd;
  }-*/;

  public final native double getUnloadEventStart() /*-{
    return this.unloadEventStart;
  }-*/;
}
