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
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.shared.util.ArrayUtils;

/**
 * A lightweight version of {@link DeckPanel}, that has the same support for showing one widget at the time, but doesn't
 * mess with the width, height, and display properties of the widgets (which sometimes create layout problems with
 * {@link DeckPanel}). Implements the one-at-a-time behaviour simply by attaching/detaching the children from the embedded
 * {@link SimplePanel}.
 *
 * @author Alex, 3/24/2015
 */
public class SimpleDeckPanel extends Composite {

  private final SimplePanel pnlContainer = new SimplePanel();
  private final Widget[] widgets;
  private int visibleWidgetIndex;

  public SimpleDeckPanel(Widget... widgets) {
    this.widgets = widgets;
    initWidget(pnlContainer);
    setWidget(0);
  }

  /**
   * Gets the index of the currently-visible widget.
   *
   * @return the visible widget's index
   */
  public int getVisibleWidgetIndex() {
    return visibleWidgetIndex;
  }

  /**
   * Gets the currently-visible widget.
   *
   * @return the visible widget's index
   */
  public Widget getVisibleWidget() {
    return widgets[getVisibleWidgetIndex()];
  }

  /**
   * Shows the widget at the specified index. This causes the currently-visible widget to be detached.
   *
   * @param index the index of the widget to be shown
   */
  public void setWidget(int index) {
    pnlContainer.setWidget(widgets[visibleWidgetIndex = index]);
  }

  /**
   * Shows the given widget. This causes the currently-visible widget to be detached.
   *
   * @param widget the widget to be shown, must have been present in the array passed to the constructor.
   */
  public void setWidget(Widget widget) {
    setWidget(ArrayUtils.linearSearch(widgets, widget));
  }



}
