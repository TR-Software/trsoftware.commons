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

package solutions.trsoftware.commons.client.debug;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.widgets.SmartComposite;

/**
 * Provides an optional facility for rendering widgets to assist with debugging.
 *
 * To take advantage of this, simply add a deferred binding rule to the module XML and provide a subclass with
 * a constructor that creates the necessary debugging widgets and calls {@link #initWidget(Widget)}.
 *
 * @author Alex
 * @since 3/6/2018
 */
public class DebugPanel extends SmartComposite {


  /**
   * Creates an instance via deferred binding and, if its constructor provided a widget (via {@link #initWidget(Widget)}),
   * adds it to the default {@link RootPanel} (i.e. {@code <body>})  
   */
  public static void create() {
    create(null);
  }

  /**
   * Creates an instance via deferred binding and, if its constructor provided a widget (via {@link #initWidget(Widget)}),
   * adds it to a {@link RootPanel}.
   * @param hostElemId argument to {@link RootPanel#get(String)}
   */
  public static void create(String hostElemId) {
    DebugPanel instance = GWT.create(DebugPanel.class);
    if (instance.isInitialized()) {
      RootPanel.get(hostElemId).add(instance);
    }
  }

}
