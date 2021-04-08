/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
 * which contains the following functions:
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
