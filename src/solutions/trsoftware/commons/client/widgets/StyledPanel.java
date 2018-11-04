/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A Panel that supports custom HTML (e.g. table-based) styling that is hard
 * to replicate with CSS and GWT/Java.
 */
public abstract class StyledPanel extends Composite {

  // TODO(2/17/2016): can use GWT's new DecoratorPanel class to replace this class

  private String contentElementId;

  /** This panel holds the html that provides the styling for the panel's background and borders */
  private HTMLPanel pnlWrapper;

  /**
   * @param html The html to be used for the panel
   * @param contentElementId The id of the element within html which will
   * hold the content of this panel (e.g. a div element)
   */
  protected StyledPanel(String html, String contentElementId) {
    this.contentElementId = contentElementId;
    pnlWrapper = new HTMLPanel(html);
    initWidget(pnlWrapper);
  }

  public void add(Widget widget) {
    pnlWrapper.add(widget, contentElementId);
  }

  public boolean remove(Widget widget) {
    return pnlWrapper.remove(widget);
  }

  public void clear() {
    pnlWrapper.clear();
  }
}
