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

package solutions.trsoftware.commons.client.styles;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Defines style elements particular to a CellPanel, in addition
 * to those that apply to widgets in general.
 * 
 * Implements a builder pattern (using method chaining) for declaring a widget's
 * style properties.
 *
 * @author Alex
 */
public class CellPanelStyle extends WidgetStyle {
  private int spacing;

  public CellPanelStyle() {
    super();
  }

  public CellPanelStyle(String styleName) {
    super(styleName);
  }

  /** Applies the style to the given cell panel */
  public <T extends Widget> T apply(T widget) {
    CellPanel targetPanel = (CellPanel) widget;

    // apply general widget styles
    super.apply(widget);

    // apply properties specific to a CellPanel
    if (spacing != 0)
      targetPanel.setSpacing(spacing);

    return widget;
  }

  public CellPanelStyle setSpacing(int spacing) {
    this.spacing = spacing;
    return this;
  }

  public CellPanelStyle setDimensions(String width, String height) {
    return (CellPanelStyle) super.setDimensions(width, height);
  }

  public CellPanelStyle setHeight(String height) {
    return (CellPanelStyle) super.setHeight(height);
  }

  public CellPanelStyle setStyleName(String styleName) {
    return (CellPanelStyle) super.setStyleName(styleName);
  }

  public CellPanelStyle setWidth(String width) {
    return (CellPanelStyle) super.setWidth(width);
  }
}
