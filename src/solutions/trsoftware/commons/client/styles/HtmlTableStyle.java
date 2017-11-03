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

package solutions.trsoftware.commons.client.styles;

import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Defines style elements particular to a CellPanel, in addition
 * to those that apply to widgets in general.
 *
 * @author Alex
 */
public class HtmlTableStyle extends WidgetStyle {
  private int spacing;

  public HtmlTableStyle() {
  }

  public HtmlTableStyle(String styleName) {
    super(styleName);
  }

  /** Applies the style to the given cell panel */
  public <T extends Widget> T apply(T widget) {
    HTMLTable targetTable = (HTMLTable) widget;

    // apply general widget style
    super.apply(widget);

    // apply spacing, if any
    if (spacing != 0)
      targetTable.setCellSpacing(spacing);

    return widget;
  }

  public HtmlTableStyle setSpacing(int spacing) {
    this.spacing = spacing;
    return this;
  }

  public HtmlTableStyle setDimensions(String width, String height) {
    return (HtmlTableStyle) super.setDimensions(width, height);
  }

  public HtmlTableStyle setHeight(String height) {
    return (HtmlTableStyle) super.setHeight(height);
  }

  public HtmlTableStyle setStyleName(String styleName) {
    return (HtmlTableStyle) super.setStyleName(styleName);
  }

  public HtmlTableStyle setWidth(String width) {
    return (HtmlTableStyle) super.setWidth(width);
  }
}