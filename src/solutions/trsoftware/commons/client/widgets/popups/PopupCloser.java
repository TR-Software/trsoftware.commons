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

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * A simple widget used to hide a popup.
 *
 * @author Alex
 */
public abstract class PopupCloser<W extends Widget & HasClickHandlers> extends Composite implements ClickHandler {
  private final PopupPanel popup;

  public PopupCloser(final PopupPanel popup, W closerWidget) {
    this.popup = popup;
    closerWidget.addClickHandler(this);
    initWidget(closerWidget);
    setTitle("close this popup");
  }

  @Override
  public W getWidget() {
    return (W)super.getWidget();
  }

  public void onClick(ClickEvent event) {
    popup.hide();
  }
}
