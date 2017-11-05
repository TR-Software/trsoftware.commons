/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.shared.util.callables.Function0;

/**
 * Creates a DiscolosurePanel which instantiates its body widget only when opened.
 *
 * Nov 5, 2009
 *
 * @author Alex
 */
public class DeferredDisclosurePanelBuilder {

  public static DisclosurePanel addDeferredBehavior(final DisclosurePanel dp, final Function0<Widget> contentWidgetFactory) {
    dp.addOpenHandler(new OpenHandler<DisclosurePanel>() {
      Widget contentWidget = null;
      public void onOpen(OpenEvent<DisclosurePanel> event) {
        if (contentWidget == null)
          dp.setContent(contentWidget = contentWidgetFactory.call());
      }
    });
    return dp;
  }

}