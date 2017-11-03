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

package solutions.trsoftware.commons.client.util.geometry;

import com.google.gwt.user.client.ui.Widget;

import static solutions.trsoftware.commons.client.util.geometry.Alignment.Horizontal.*;
import static solutions.trsoftware.commons.client.util.geometry.Alignment.Vertical.*;

/**
 * @author Alex, 2/16/2016
 */
public class Alignment {

  public static final Alignment ABOVE_RIGHT_EDGES = new Alignment(RIGHT_EDGES, ABOVE);
  public static final Alignment ABOVE_RIGHT = new Alignment(RIGHT_OF, ABOVE);
  public static final Alignment BELOW_RIGHT_EDGES = new Alignment(RIGHT_EDGES, BELOW);
  public static final Alignment OVERLAY = new Alignment(LEFT_EDGES, TOP_EDGES);
  public static final Alignment RIGHTS_AND_TOPS = new Alignment(RIGHT_EDGES, TOP_EDGES);
  public static final Alignment[] NEXT_TO = {
      new Alignment(RIGHT_OF, TOP_EDGES),
      new Alignment(RIGHT_OF, ABOVE),
      new Alignment(RIGHT_OF, BELOW),
      new Alignment(RIGHT_OF, MIDDLE),
      new Alignment(LEFT_OF, TOP_EDGES),
      new Alignment(LEFT_OF, ABOVE),
      new Alignment(LEFT_OF, BELOW),
      new Alignment(LEFT_OF, MIDDLE),
      new Alignment(LEFT_EDGES, BELOW),
      new Alignment(LEFT_EDGES, ABOVE),
      new Alignment(RIGHT_EDGES, BELOW),
      new Alignment(RIGHT_EDGES, ABOVE),
  };

  public enum Horizontal {
    LEFT_OF() {
      public int getX(Widget widget, int popupWidth, int popupHeight) {
        return widget.getAbsoluteLeft() - popupWidth;
      }},
    RIGHT_OF() {
      public int getX(Widget widget, int popupWidth, int popupHeight) {
        return WindowGeometry.absoluteRight(widget);
      }},
    /** Right edges aligned */
    RIGHT_EDGES() {
      public int getX(Widget widget, int popupWidth, int popupHeight) {
        return WindowGeometry.absoluteRight(widget) - popupWidth;
      }},
    /** Left edges aligned */
    LEFT_EDGES() {
      public int getX(Widget widget, int popupWidth, int popupHeight) {
        return widget.getAbsoluteLeft();
      }};
    public abstract int getX(Widget widget, int popupWidth, int popupHeight);
  }

  public enum Vertical {
    ABOVE() {
      public int getY(Widget widget, int popupWidth, int popupHeight) {
        return widget.getAbsoluteTop() - popupHeight;
      }},
    BELOW() {
      public int getY(Widget widget, int popupWidth, int popupHeight) {
        return WindowGeometry.absoluteBottom(widget);
      }},
    TOP_EDGES() {
      public int getY(Widget widget, int popupWidth, int popupHeight) {
        return widget.getAbsoluteTop();
      }},
    /** Aligns the vertical midpoints of both rectangles */
    MIDDLE() {
      public int getY(Widget widget, int popupWidth, int popupHeight) {
        int widgetMiddle = widget.getAbsoluteTop() + widget.getOffsetHeight() / 2;
        return widgetMiddle - (popupHeight / 2);
      }};
    public abstract int getY(Widget widget, int popupWidth, int popupHeight);
  }

  private final Horizontal horizontal;
  private final Vertical vertical;

  public Alignment(Horizontal horizontal, Vertical vertical) {
    this.horizontal = horizontal;
    this.vertical = vertical;
  }

  public Horizontal getHorizontal() {
    return horizontal;
  }

  public Vertical getVertical() {
    return vertical;
  }
}
