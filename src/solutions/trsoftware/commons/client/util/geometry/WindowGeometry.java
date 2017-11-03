package solutions.trsoftware.commons.client.util.geometry;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import java.util.Arrays;

/**
 * Date: Nov 2, 2008 Time: 5:03:32 PM
 *
 * @author Alex
 */
public class WindowGeometry {


  public static int absoluteRight(Widget widget) {
    return widget.getAbsoluteLeft() + widget.getOffsetWidth();
  }

  public static int absoluteBottom(Widget widget) {
    return widget.getAbsoluteTop() + widget.getOffsetHeight();
  }

  /**
   * Sets the position of the popup relative to another widget (the "pivot") using the prefs defined by the argument.
   * Does nothing if that the pivot widget is not attached to the DOM (when a widget is not attached, it's top and left
   * coordinates are both 0).
   */
  public static void positionPopupNextToWidget(PopupPanel popup, RelativePosition pos) {
    positionPopupNextToWidget(pos.getPivot(), popup, popup.getOffsetWidth(), popup.getOffsetHeight(), pos.getOffsetX(), pos.getOffsetY(), pos.getAlignmentPrefs());
  }

  private static void positionPopupNextToWidget(Widget widget, PopupPanel popup,
                                               int popupWidth, int popupHeight,
                                               int xOffset, int yOffset,
                                               Alignment... alignmentPrefs) {
    if (!widget.isAttached())
      return;  // when a widget is not attached, it's top and left coordinates are both 0, so this method doesn't make sense
    Rectangle clientWindow = new Rectangle(Window.getScrollLeft(), Window.getScrollTop(), Window.getClientWidth(), Window.getClientHeight());
    
    // try each given alignment, checking to make sure that it fits the screen
    Rectangle best = null;
    Rectangle bestOverlap = null;
    for (Alignment alignment : alignmentPrefs) {
      int x = alignment.getHorizontal().getX(widget, popupWidth, popupHeight);
      int y = alignment.getVertical().getY(widget, popupWidth, popupHeight);
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
    popup.setPopupPosition(best.x + xOffset, best.y + yOffset);
  }

  /** Debugging method */
  public static String printPositioningInfo(UIObject widget, String name) {
    Document doc = Document.get();
    StringBuilder msg = new StringBuilder(name)
        .append(" widget position (absolute[Left|Top]): ").append(Arrays.asList(widget.getAbsoluteLeft(), widget.getAbsoluteTop()))
        .append("; widget size (offset[Width/Height]): ").append(Arrays.asList(widget.getOffsetWidth(), widget.getOffsetHeight()))
        .append("; document scroll position (scroll[Left/Top]): ").append(Arrays.asList(doc.getScrollLeft(), doc.getScrollTop()))
        .append("; document scroll size (scroll[Left/Top]): ").append(Arrays.asList(doc.getScrollWidth(), doc.getScrollHeight()))
        .append("; document client size (client[Width/Height]): ").append(Arrays.asList(doc.getClientWidth(), doc.getClientHeight()))
        .append("; document coordinate system (bodyOffset[Left/Top]): ").append(Arrays.asList(doc.getBodyOffsetLeft(), doc.getBodyOffsetTop()))
        ;
    return msg.toString();
  }
}
