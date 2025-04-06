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

package solutions.trsoftware.commons.client.util.geometry;

import com.google.common.base.MoreObjects;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.widgets.popups.EnhancedPopup;

/**
 * Specifies a list of preferences for choosing the position of an absolutely-positioned widget
 * (e.g. a {@link PopupPanel}) relative to another widget or element ({@link #pivot}).
 *
 * @see Alignment
 * @see EnhancedPopup#showRelativeTo(RelativePosition)
 * @author Alex, 11/17/2014
 */
public class RelativePosition {

  /**
   * The element relative to which the position will be computed
   */
  private final Element pivot;
  /**
   * Horizontal offset for the position computed by each {@link Alignment}
   */
  private final Offset offsetX;
  /**
   * Vertical offset for the position computed by each {@link Alignment}
   */
  private final Offset offsetY;
  /**
   * The alignments to consider when choosing the best position, ordered by preference
   */
  private final Alignment[] alignmentPrefs;

  /**
   * @param pivot element relative to which the position will be computed
   * @param offsetX horizontal offset for the position computed by each {@link Alignment}
   * @param offsetY vertical offset for the position computed by each {@link Alignment}
   * @param alignmentPrefs the alignments to consider when choosing the best position, ordered by preference
   */
  public RelativePosition(Element pivot, Offset offsetX, Offset offsetY,
                          Alignment... alignmentPrefs) {
    this.pivot = pivot;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.alignmentPrefs = alignmentPrefs;
  }

  /**
   * @param pivot element relative to which the position will be computed
   * @param offsetX horizontal offset (in pixels) for the position computed by each {@link Alignment}
   * @param offsetY vertical offset (in pixels) for the position computed by each {@link Alignment}
   * @param alignmentPrefs the alignments to consider when choosing the best position, ordered by preference
   */
  public RelativePosition(Element pivot, int offsetX, int offsetY,
                          Alignment... alignmentPrefs) {
    this(pivot, Offset.px(offsetX), Offset.px(offsetY), alignmentPrefs);
  }

  /**
   * @param pivot element relative to which the position will be computed
   * @param alignmentPrefs the alignments to consider when choosing the best position, ordered by preference
   */
  public RelativePosition(Element pivot,
                          Alignment... alignmentPrefs) {
    this(pivot, 0, 0, alignmentPrefs);
  }

  /**
   * @param pivot widget relative to which the position will be computed
   * @param offsetX horizontal offset (in pixels) for the position computed by each {@link Alignment}
   * @param offsetY vertical offset (in pixels) for the position computed by each {@link Alignment}
   * @param alignmentPrefs the alignments to consider when choosing the best position, ordered by preference
   */
  public RelativePosition(Widget pivot, int offsetX, int offsetY,
                          Alignment... alignmentPrefs) {
    this(pivot.getElement(), offsetX, offsetY, alignmentPrefs);
  }

  /**
   * @param pivot widget relative to which the position will be computed
   * @param alignmentPrefs the alignments to consider when choosing the best position, ordered by preference
   */
  public RelativePosition(Widget pivot,
                          Alignment... alignmentPrefs) {
    this(pivot, 0, 0, alignmentPrefs);
  }

  /**
   * @param pivot widget relative to which the position will be computed
   * @return an instance configured with {@link Alignment#NEXT_TO} preferences
   */
  public static RelativePosition nextTo(Widget pivot) {
    return nextTo(pivot.getElement());
  }

  /**
   * @param pivot element relative to which the position will be computed
   * @return an instance configured with {@link Alignment#NEXT_TO} preferences
   */
  public static RelativePosition nextTo(Element pivot) {
    return new RelativePosition(pivot, Alignment.NEXT_TO);
  }

  public Element getPivot() {
    return pivot;
  }

  public Offset getOffsetX() {
    return offsetX;
  }

  public Offset getOffsetY() {
    return offsetY;
  }

  public Alignment[] getAlignmentPrefs() {
    return alignmentPrefs;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("pivot", pivot)
        .add("offsetX", offsetX)
        .add("offsetY", offsetY)
        .add("alignmentPrefs", alignmentPrefs)
        .toString();
  }
}
