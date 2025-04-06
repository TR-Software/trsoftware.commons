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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.widgets.popups.EnhancedPopup;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.geometry.Point;
import solutions.trsoftware.commons.shared.util.geometry.RealPoint;

import java.util.Objects;

import static solutions.trsoftware.commons.client.dom.DomUtils.*;
import static solutions.trsoftware.commons.client.util.geometry.Alignment.Horizontal.*;
import static solutions.trsoftware.commons.client.util.geometry.Alignment.Vertical.*;

/**
 * Specifies a preferred popup position relative to a DOM element or widget,
 * as an immutable ({@linkplain Horizontal horizontal}, {@linkplain Vertical vertical}) alignment pair.
 * 
 * @see RelativePosition
 * @see EnhancedPopup#showRelativeTo(Widget, Alignment...)
 * 
 * @author Alex, 2/16/2016
 */
public class Alignment {

  public static final Alignment ABOVE_RIGHT_EDGES = new Alignment(RIGHT_EDGES, ABOVE);
  public static final Alignment ABOVE_RIGHT = new Alignment(RIGHT_OF, ABOVE);
  public static final Alignment BELOW_RIGHT_EDGES = new Alignment(RIGHT_EDGES, BELOW);
  public static final Alignment BELOW_LEFT_EDGES = new Alignment(LEFT_EDGES, BELOW);
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
      BELOW_LEFT_EDGES,
      new Alignment(LEFT_EDGES, ABOVE),
      new Alignment(RIGHT_EDGES, BELOW),
      new Alignment(RIGHT_EDGES, ABOVE),
  };

  public enum Horizontal {
    LEFT_OF() {
      public double getX(Element element, double popupWidth, double popupHeight) {
        return getAbsoluteLeft(element) - popupWidth;
      }
    },
    RIGHT_OF() {
      public double getX(Element element, double popupWidth, double popupHeight) {
        return getAbsoluteRight(element);
      }
    },
    /** Right edges aligned */
    RIGHT_EDGES() {
      public double getX(Element element, double popupWidth, double popupHeight) {
        return getAbsoluteRight(element) - popupWidth;
      }
    },
    /** Left edges aligned */
    LEFT_EDGES() {
      public double getX(Element element, double popupWidth, double popupHeight) {
        return getAbsoluteLeft(element);
      }
    };

    /**
     * @return the {@code x}-coordinate for positioning a popup of the given dimensions next to the given element
     * according to this alignment
     */
    public int getX(Element element, int popupWidth, int popupHeight) {
      return (int)getX(element, (double)popupWidth, popupHeight);
    }

    /**
     * @return the {@code x}-coordinate for positioning a popup of the given dimensions next to the given element according
     * to this alignment
     */
    public abstract double getX(Element element, double popupWidth, double popupHeight);
  }

  public enum Vertical {
    ABOVE() {
      public double getY(Element element, double popupWidth, double popupHeight) {
        return getAbsoluteTop(element) - popupHeight;
      }
    },
    BELOW() {
      public double getY(Element element, double popupWidth, double popupHeight) {
        return getAbsoluteBottom(element);
      }
    },
    TOP_EDGES() {
      public double getY(Element element, double popupWidth, double popupHeight) {
        return getAbsoluteTop(element);
      }
    },
    /** Aligns the vertical midpoints of both rectangles */
    MIDDLE() {
      public double getY(Element element, double popupWidth, double popupHeight) {
        double elementMiddle = getAbsoluteTop(element) + getRenderedHeight(element) / 2;
        return elementMiddle - (popupHeight / 2);
      }
    };

    /**
     * @return the {@code y}-coordinate for positioning a popup of the given dimensions next to the given element
     * according to this alignment
     */
    public int getY(Element element, int popupWidth, int popupHeight) {
      return (int)getY(element, (double)popupWidth, popupHeight);
    }

    /**
     * @return the {@code y}-coordinate for positioning a popup of the given dimensions next to the given element
     * according to this alignment
     */
    public abstract double getY(Element element, double popupWidth, double popupHeight);
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

  /**
   * @return the desired position of a popup of the given dimensions when placed next to the given element
   * according to this alignment
   */
  public Point getPopupPosition(Element element, double popupWidth, double popupHeight) {
    return new RealPoint(
        getX(element, popupWidth, popupHeight),
        getY(element, popupWidth, popupHeight));
  }

  /**
   * @return the {@code x}-coordinate for positioning a popup of the given dimensions next to the given element
   * according to this alignment
   */
  public int getX(Element element, int popupWidth, int popupHeight) {
    return horizontal != null ? horizontal.getX(element, popupWidth, popupHeight) : 0;
  }

  /**
   * @return the {@code x}-coordinate for positioning a popup of the given dimensions next to the given element
   * according to this alignment
   */
  public double getX(Element element, double popupWidth, double popupHeight) {
    return horizontal != null ? horizontal.getX(element, popupWidth, popupHeight) : 0;
  }

  /**
   * @return the {@code y}-coordinate for positioning a popup of the given dimensions next to the given element
   * according to this alignment
   */
  public int getY(Element element, int popupWidth, int popupHeight) {
    return vertical != null ? vertical.getY(element, popupWidth, popupHeight): 0;
  }

  /**
   * @return the {@code y}-coordinate for positioning a popup of the given dimensions next to the given element
   * according to this alignment
   */
  public double getY(Element element, double popupWidth, double popupHeight) {
    return vertical != null ? vertical.getY(element, popupWidth, popupHeight): 0;
  }

  @Override
  public String toString() {
    return StringUtils.tupleToString(horizontal, vertical);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Alignment alignment = (Alignment)o;
    return horizontal == alignment.horizontal &&
        vertical == alignment.vertical;
  }

  @Override
  public int hashCode() {
    return Objects.hash(horizontal, vertical);
  }
}
