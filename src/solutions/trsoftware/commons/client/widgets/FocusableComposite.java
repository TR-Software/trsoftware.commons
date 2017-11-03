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

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.jso.JsDocument;

/**
 * @author Alex, 9/22/2017
 */
public class FocusableComposite extends Composite {

  private FocusWidget focusWidget;

  public FocusWidget getFocusWidget() {
    return focusWidget;
  }

  public void setFocusWidget(FocusWidget focusWidget) {
    this.focusWidget = focusWidget;
  }

  public void setFocus(boolean focused) {
    focusWidget.setFocus(focused);
  }

  @Override
  protected void initWidget(Widget widget) {
    if (focusWidget == null)
      throw new IllegalStateException(getClass().getName() + " must call setFocusWidget before initWidget");
    super.initWidget(widget);
  }

  public boolean hasFocus() {
    Element activeElement = JsDocument.get().getActiveElement();
    return activeElement != null && activeElement == focusWidget.getElement();
  }
}
