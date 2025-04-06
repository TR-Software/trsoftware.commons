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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import solutions.trsoftware.commons.client.dom.DomUtils;
import solutions.trsoftware.commons.shared.util.geometry.Rectangle;

import java.util.Arrays;

/**
 * Utils for positioning a {@link PopupPanel} in accordance to a list of preferences specified by a {@link RelativePosition}
 *
 * @author Alex, Nov 2, 2008
 * @see PopupPanel#showRelativeTo(UIObject)
 */
public class WindowGeometry {
  // TODO(12/5/2024): document this class
  // TODO(3/28/2025): maybe replace this class with a PopupPositioner subclass

  /**
   * Sets the position of the popup relative to another widget (the "pivot") using the prefs defined by the argument.
   * Does nothing if that the pivot widget is not attached to the DOM (when a widget is not attached, it's top and left
   * coordinates are both 0).
   */
  public static void positionPopupNextToWidget(PopupPanel popup, RelativePosition pos) {
    positionPopupNextToElement(popup, pos);  // TODO: remove duplicate method with same param types
  }

  /**
   * Sets the position of the popup relative to another element (the "pivot") using the prefs defined by the argument.
   * Does nothing if that the pivot element is not attached to the DOM (when element is not attached, it's top and left
   * coordinates are both 0).
   */
  public static void positionPopupNextToElement(PopupPanel popup, RelativePosition pos) {
    positionPopupNextToElement(pos.getPivot(), popup,
        popup.getOffsetWidth(), popup.getOffsetHeight(), pos.getOffsetX(), pos.getOffsetY(),
        pos.getAlignmentPrefs());
  }

  private static void positionPopupNextToElement(Element element, PopupPanel popup,
                                                 int popupWidth, int popupHeight,
                                                 Offset xOffset, Offset yOffset,
                                                 Alignment... alignmentPrefs) {
    if (!DomUtils.isAttached(element)) {
      return;  // when a element is not attached, it's top and left coordinates are both 0, so this method doesn't make sense
      // TODO: maybe throw ISE or use some default position (e.g. centered in window, or (0, 0)) in this case instead of not showing the popup at all
    }
    int xOffsetPx = (int)Math.round(xOffset.getX(element));
    int yOffsetPx = (int)Math.round(yOffset.getY(element));

    Rectangle clientWindow = new Rectangle(Window.getScrollLeft(), Window.getScrollTop(), Window.getClientWidth(), Window.getClientHeight());
    
    // try each given alignment, checking to make sure that it fits the screen
    Rectangle best = null;
    Rectangle bestOverlap = null;
    for (Alignment alignment : alignmentPrefs) {
      int x = alignment.getX(element, popupWidth, popupHeight);
      int y = alignment.getY(element, popupWidth, popupHeight);
      Rectangle popupRectangle = new Rectangle(x, y, popupWidth, popupHeight);
      // if this rectangle fits within the client window, use it;
      Rectangle overlapRectangle = clientWindow.intersection(popupRectangle);
      if (popupRectangle.equals(overlapRectangle)) {
        // this rectangle is fully contained within the client window; use it
        best = overlapRectangle;
//        System.out.println("Pefect match: " + orientationPair);
        break;
      }
      else if (best == null || overlapRectangle.area() > bestOverlap.area()) {
//        System.out.println("Best so far: " + orientationPair);
        best = popupRectangle;
        bestOverlap = overlapRectangle;
      }
    }
    if (best == null)
      best = new Rectangle(0, 0, 0, 0);
//    System.out.println("Drawing popup at " + best.toString());
    // TODO(12/29/2024): maybe consider the offset args in the above loop (when looking for the best rectangle)?
    popup.setPopupPosition(best.x + xOffsetPx, best.y + yOffsetPx);
  }

  /** Debugging method */
  public static String printPositioningInfo(UIObject widget, String name) {
    Document doc = Document.get();
    StringBuilder msg = new StringBuilder(name)
        .append(" widget position (absolute[Left|Top]): ").append(Arrays.asList(widget.getAbsoluteLeft(), widget.getAbsoluteTop()))
        .append(";\n  widget size (offset[Width/Height]): ").append(Arrays.asList(widget.getOffsetWidth(), widget.getOffsetHeight()))
        .append(";\n  document scroll position (scroll[Left/Top]): ").append(Arrays.asList(doc.getScrollLeft(), doc.getScrollTop()))
        .append(";\n  document scroll size (scroll[Left/Top]): ").append(Arrays.asList(doc.getScrollWidth(), doc.getScrollHeight()))
        .append(";\n  document client size (client[Width/Height]): ").append(Arrays.asList(doc.getClientWidth(), doc.getClientHeight()))
        .append(";\n  document coordinate system (bodyOffset[Left/Top]): ").append(Arrays.asList(doc.getBodyOffsetLeft(), doc.getBodyOffsetTop()))
        .append(";\n  document compatMode: ").append(doc.getCompatMode())
        .append(";\n  document.[documentElement|body].scrollTop: ").append(Arrays.asList(doc.getDocumentElement().getScrollTop(), doc.getBody().getScrollTop()))
        ;
    return msg.toString();
  }
}
