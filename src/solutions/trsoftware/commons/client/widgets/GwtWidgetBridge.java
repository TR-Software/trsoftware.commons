package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.jso.JsObjectArray;

/**
 * Exposes an interface for external javascript in the hostpage to create
 * GWT widgets.
 *
 * Usage:
 *
 * GwtWidgetBridge.init("foo") creates an object called foo in the hostpage,
 * which containts the following functions:
 *   - newHTML(String): returns a new GWT HTML widget with the given HTML content rendered
 *   - newVerticalPanel(Widget[])
 *   - newHorizontalPanel(Widget[])
 *   - newFlowPanel(Widget[])
 * Feb 22, 2011
 *
 * @author Alex
 */
public class GwtWidgetBridge {

  // API methods:

  private static HTML newHTML(String html) {
    return new HTML(html);
  }

  private static VerticalPanel newVerticalPanel(JsObjectArray<Widget> widgets) {
    return addWidgetsToPanel(new VerticalPanel(), widgets);
  }

  private static HorizontalPanel newHorizontalPanel(JsObjectArray<Widget> widgets) {
    return addWidgetsToPanel(new HorizontalPanel(), widgets);
  }

  private static FlowPanel newFlowPanel(JsObjectArray<Widget> widgets) {
    return addWidgetsToPanel(new FlowPanel(), widgets);
  }

  // Helper methods:

  private static <T extends Panel> T addWidgetsToPanel(T pnl, JsObjectArray<Widget> widgets) {
    if (widgets != null) {
      for (int i = 0; i < widgets.length(); i++) {
        pnl.add(widgets.get(i));
      }
    }
    return pnl;
  }

  // Initializer:

  /**
   * Creates an object in the hostpage with the given name, which defines the
   * interface functions provided by this class.
   * @param namespace
   */
  public static native void init(String namespace) /*-{
    if (!$wnd[namespace]) {
      $wnd[namespace] = {};
    }
    var namespaceObject = $wnd[namespace];
    // assign our Java function to the native Javascript object
    namespaceObject.newHTML = @solutions.trsoftware.commons.client.widgets.GwtWidgetBridge::newHTML(Ljava/lang/String;);
    namespaceObject.newVerticalPanel = @solutions.trsoftware.commons.client.widgets.GwtWidgetBridge::newVerticalPanel(Lsolutions/trsoftware/commons/client/jso/JsObjectArray;);
    namespaceObject.newHorizontalPanel = @solutions.trsoftware.commons.client.widgets.GwtWidgetBridge::newHorizontalPanel(Lsolutions/trsoftware/commons/client/jso/JsObjectArray;);
    namespaceObject.newFlowPanel = @solutions.trsoftware.commons.client.widgets.GwtWidgetBridge::newFlowPanel(Lsolutions/trsoftware/commons/client/jso/JsObjectArray;);
    // can add more functions here
  }-*/;

}
