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

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.styles.CellPanelStyle;
import solutions.trsoftware.commons.client.styles.WidgetStyle;

import java.util.ArrayList;

/**
 * This wrapper can be used for adding widgets to a cell panel using the panel factory methods
 * in {@link Widgets} (e.g. {@link Widgets#horizontalPanel(CellPanelStyle, Widget...)}) in order
 * to specify the cell styles inline using a builder pattern / method chaining.
 *
 * This class only extends {@link Widget} in order to adapt it for an argument to the {@link Widgets} factory methods,
 * but it's not a real widget - it cannot be added directly to the DOM.  The {@link #addTo(CellPanel)} method
 * is the proper way to add the wrapped widget to a panel.
 *
 * @author Alex
 */
public class CellPanelEntry extends Widget {

  /*
   NOTE: This class used to be a Composite with fields for each modified cell style property.  However, we changed
   it to be only a container for deferred CellStyleApplicator functions for 2 reasons: (1) to save memory:
   this class now be garbage collected right after it's added to a panel - there's no need to hang on to those
   settings beyond that point, and (2) to work around the GWT compiler bug described in 2015_03_30_GWT_compiler_bug.txt
   */

  private final Widget widget;

  interface CellStyleApplicator {
    void applyCellStyle(CellPanel cellPanel);
  }

  private ArrayList<CellStyleApplicator> styleApplicators = new ArrayList<CellStyleApplicator>();

  public CellPanelEntry(Widget widget) {
    this.widget = widget;
  }

  /**
   * Adds the widget to the given panel and runs all the style applicators.
   */
  void addTo(CellPanel targetPanel) {
    targetPanel.add(widget);
    for (CellStyleApplicator s : styleApplicators) {
      s.applyCellStyle(targetPanel);
    }
  }

  public CellPanelEntry setCellHeight(final String height) {
    styleApplicators.add(new CellStyleApplicator() {
      @Override
      public void applyCellStyle(CellPanel cellPanel) {
        cellPanel.setCellHeight(widget, height);
      }
    });
    return this;
  }

  public CellPanelEntry setCellWidth(final String width) {
    styleApplicators.add(new CellStyleApplicator() {
      @Override
      public void applyCellStyle(CellPanel cellPanel) {
        cellPanel.setCellWidth(widget, width);
      }
    });
    return this;
  }

  public CellPanelEntry setAlignment(final HasHorizontalAlignment.HorizontalAlignmentConstant horizontalAlignment) {
    styleApplicators.add(new CellStyleApplicator() {
      @Override
      public void applyCellStyle(CellPanel cellPanel) {
        cellPanel.setCellHorizontalAlignment(widget, horizontalAlignment);
      }
    });
    return this;
  }

  public CellPanelEntry setAlignment(final HasVerticalAlignment.VerticalAlignmentConstant verticalAlignment) {
    styleApplicators.add(new CellStyleApplicator() {
      @Override
      public void applyCellStyle(CellPanel cellPanel) {
        cellPanel.setCellVerticalAlignment(widget, verticalAlignment);
      }
    });
    return this;
  }

  public CellPanelEntry applyStyleToWidget(WidgetStyle style) {
    style.apply(widget);
    return this;
  }

  @Override
  public String toString() {
    return widget.toString();
  }
}
