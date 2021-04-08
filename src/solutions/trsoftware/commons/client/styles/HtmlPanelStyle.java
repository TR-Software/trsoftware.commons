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

import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Like CellPanelStyle, except also supports the cell padding property.
 *
 * @author Alex
 */
public class HtmlPanelStyle extends WidgetStyle {
  private int padding;
  private int spacing;

  public HtmlPanelStyle() {
  }

  public HtmlPanelStyle(String styleName) {
    super(styleName);
  }

  public <T extends Widget> T apply(T widget) {
    HTMLTable table = (HTMLTable) widget;

    // apply general widget styles
    super.apply(widget);

    // apply properties specific to a HTMLTable
    if (padding != 0)
      table.setCellPadding(padding);
    if (spacing != 0)
      table.setCellSpacing(spacing);

    return widget;
  }

  public HtmlPanelStyle setPadding(int padding) {
    this.padding = padding;
    return this;
  }

  public HtmlPanelStyle setSpacing(int spacing) {
    this.spacing = spacing;
    return this;
  }
}
