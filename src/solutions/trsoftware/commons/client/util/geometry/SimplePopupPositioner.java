package solutions.trsoftware.commons.client.util.geometry;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import solutions.trsoftware.commons.client.dom.DOMRect;
import solutions.trsoftware.commons.client.dom.JsElement;
import solutions.trsoftware.commons.shared.util.geometry.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static solutions.trsoftware.commons.shared.util.StringUtils.tupleToString;

/**
 * @author Alex
 * @since 1/19/2025
 * @see RelativePosition
 */
@Beta
public class SimplePopupPositioner extends AbstractPopupPositioner {
  // TODO: clean up & document this class; maybe use it to replace WindowGeometry.positionPopupNextToElement

  /* NOTE: this class is an experimental alternative to WindowGeometry.positionPopupNextToElement,
     specialized for the HintPopup styles.
     - if this works out, maybe refact EnhancedPopup to use this class instead of WindowGeometry and RelativePosition
     - in the future, might be able to replace with the new CSS "anchor positioning" capabilities when they get broader browser support
   */

  protected List<AlignmentPref> alignmentPrefs;

  public SimplePopupPositioner(Element pivot, Set<Element> elementsToNotObstruct, List<AlignmentPref> alignmentPrefs) {
    super(pivot, elementsToNotObstruct);
    this.alignmentPrefs = alignmentPrefs;
  }

  /**
   * Invokes {@link PopupPanel#setPopupPositionAndShow(PopupPanel.PositionCallback)} using the best position
   * returned by {@link #findBestPosition(PopupPanel)}
   * @param popup the popup to be shown
   */
  public void setPopupPositionAndShow(PopupPanel popup) {
    popup.setPopupPositionAndShow((offsetWidth, offsetHeight) -> {
      List<Result> results = findBestPosition(offsetWidth, offsetHeight);
      if (!results.isEmpty()) {
        Rectangle popupRect = results.get(0).getPopupRect();
        popup.setPopupPosition(popupRect.x, popupRect.y);
      }
    });
  }

  public List<Result> findBestPosition(int popupWidth, int popupHeight) {
    // adapted from WindowGeometry.positionPopupNextToElement
    Rectangle clientWindow = new Rectangle(Window.getScrollLeft(), Window.getScrollTop(), Window.getClientWidth(), Window.getClientHeight());

    // compute a "goodness" score for each AlignmentPref
    List<Result> results = new ArrayList<>();
    // TODO: maybe replace loop with Stream and .sorted()?
    for (AlignmentPref pref : alignmentPrefs) {
      Alignment alignment = pref.alignment;
      int x = alignment.getX(pivot, popupWidth, popupHeight);
      int y = alignment.getY(pivot, popupWidth, popupHeight);
      int offsetX = (int)(x + pref.calcOffsetX(pivot));
      int offsetY = (int)(y + pref.calcOffsetY(pivot));
      Rectangle popupRect = new Rectangle(offsetX, offsetY, popupWidth, popupHeight);
      // TODO: make sure x, y > 0?
      // compute the score of this rect
      // we want to maximize overlap with window (i.e. pctContainedInWindow) & minimize overlap with the other elements
      Rectangle popupRectVisible = clientWindow.intersection(popupRect);
      double elAreaTotal = 0;
      double elAreaCovered = 0;
      double visibleElAreaTotal = 0;
      double visibleElAreaCovered = 0;

//      coverWeights.keySet().stream().map(JsElement::as).map(JsElement::getBoundingClientRect).map(domRect -> new Rectangle(domRect.getAbsoluteX(), domRect.getAbsoluteY(), domRect.getWidth(), domRect.getHeight()))
      for (Element element : coverWeights.keySet()) {
        DOMRect domRect = JsElement.as(element).getBoundingClientRect();
        Rectangle elRect = new Rectangle(domRect);
        Rectangle elRectVisible = clientWindow.intersection(elRect);
        Rectangle elRectCovered = popupRect.intersection(elRect);
        Rectangle visibleElRectCovered = popupRectVisible.intersection(elRectVisible);
        visibleElAreaTotal += elRectVisible.area();
        visibleElAreaCovered += visibleElRectCovered.area();
        elAreaTotal += elRect.area();
        elAreaCovered += elRectCovered.area();
      }
      // TODO: compute how much of the popupRect is good (i.e. contained in window and not covering the elements)
      double visiblePopupArea = popupRectVisible.area();
      double goodArea = visiblePopupArea - visibleElAreaCovered;
      double pctGood = goodArea / popupRect.area();
      // TODO: maybe assign greater importance to visibleElAreaCovered, as long as the popupRect coords >= 0 (i.e. it's ok if it overflows to a window region that can be scrolled to)
      double score = pctGood;

      results.add(new Result(popupRect, pref, score));
    }
    results.sort(null);  // natural order sort when Comparator arg is null
    return results;
    // TODO: unit test
  }

  public List<PositionResult> findBestPosition(PopupPanel popup) {
    // TODO: experimental version of the other impl, using the StyleMutator to compute new (popupWidth, popupHeight) on each iteration
    Rectangle clientWindow = new Rectangle(Window.getScrollLeft(), Window.getScrollTop(), Window.getClientWidth(), Window.getClientHeight());

    // compute a "goodness" score for each AlignmentPref
    List<PositionResult> results = new ArrayList<>();
    // TODO: maybe replace loop with Stream and .sorted()?
    for (AlignmentPref pref : alignmentPrefs) {
      pref.applyStyle(popup);
      int popupWidth = popup.getOffsetWidth();
      int popupHeight = popup.getOffsetHeight();
      Alignment alignment = pref.alignment;
      int x = alignment.getX(pivot, popupWidth, popupHeight);
      int y = alignment.getY(pivot, popupWidth, popupHeight);
      int offsetX = (int)(x + pref.calcOffsetX(pivot));
      int offsetY = (int)(y + pref.calcOffsetY(pivot));
      Rectangle popupRect = new Rectangle(offsetX, offsetY, popupWidth, popupHeight);
      // TODO: make sure x, y > 0?
      // compute the score of this rect
      // we want to maximize overlap with window (i.e. pctContainedInWindow) & minimize overlap with the other elements
      Rectangle popupRectVisible = clientWindow.intersection(popupRect);
      double elAreaTotal = 0;
      double elAreaCovered = 0;
      double visibleElAreaTotal = 0;
      double visibleElAreaCovered = 0;

//      coverWeights.keySet().stream().map(JsElement::as).map(JsElement::getBoundingClientRect).map(domRect -> new Rectangle(domRect.getAbsoluteX(), domRect.getAbsoluteY(), domRect.getWidth(), domRect.getHeight()))
      for (Element element : coverWeights.keySet()) {
        DOMRect domRect = JsElement.as(element).getBoundingClientRect();
        Rectangle elRect = new Rectangle(domRect);
        Rectangle elRectVisible = clientWindow.intersection(elRect);
        Rectangle elRectCovered = popupRect.intersection(elRect);
        Rectangle visibleElRectCovered = popupRectVisible.intersection(elRectVisible);
        visibleElAreaTotal += elRectVisible.area();
        visibleElAreaCovered += visibleElRectCovered.area();
        elAreaTotal += elRect.area();
        elAreaCovered += elRectCovered.area();
      }
      // TODO: compute how much of the popupRect is good (i.e. contained in window and not covering the elements)
      double visiblePopupArea = popupRectVisible.area();
      double goodArea = visiblePopupArea - visibleElAreaCovered;
      double pctGood = goodArea / popupRect.area();
      // TODO: maybe assign greater importance to visibleElAreaCovered, as long as the popupRect coords >= 0 (i.e. it's ok if it overflows to a window region that can be scrolled to)
      double score = pctGood;

      results.add(new Result(popupRect, pref, score));
      pref.removeStyle(popup);  // clean up for next iteration
    }
    results.sort(null);  // natural order sort when Comparator arg is null
    return results;
    // TODO: unit test
  }

  public static class Result extends AbstractPopupPositioner.Result {
    private AlignmentPref alignmentPref;

    public Result(Rectangle popupRect, AlignmentPref alignmentPref, double score) {
      super(popupRect, score);
      this.alignmentPref = alignmentPref;
      this.score = score;
    }

    public Result(Rectangle popupRect, AlignmentPref alignmentPref, double score, double score2) {
      super(popupRect, score, score2);
      this.alignmentPref = alignmentPref;
    }

    @Override
    public void applyStyle(PopupPanel popup) {
      alignmentPref.applyStyle(popup);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .addValue(popupRect)
          .addValue(alignmentPref)
          .add("score", score)
          .add("score2", score2)
          .toString();
    }
  }

  public static class AlignmentPref implements StyleMutator {
    private final Alignment alignment;
    private final Offset offsetX, offsetY;
    private StyleMutator styleMutator;

    public AlignmentPref(Alignment alignment, Offset offsetX, Offset offsetY) {
      this.alignment = alignment;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
    }

    public AlignmentPref(Alignment alignment, Offset offsetX, Offset offsetY, StyleMutator styleMutator) {
      this.alignment = alignment;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.styleMutator = styleMutator;
    }

    public Alignment getAlignment() {
      return alignment;
    }

    public Offset getOffsetX() {
      return offsetX;
    }

    public Offset getOffsetY() {
      return offsetY;
    }

    public double calcOffsetX(Element pivot) {
      return offsetX.getX(pivot);
    }
    
    public double calcOffsetY(Element pivot) {
      return offsetY.getY(pivot);
    }

    public StyleMutator getStyleMutator() {
      return styleMutator;
    }

    public AlignmentPref setStyleMutator(StyleMutator styleMutator) {
      this.styleMutator = styleMutator;
      return this;
    }

    @Override
    public void applyStyle(PopupPanel popup) {
      if (styleMutator != null) {
        styleMutator.applyStyle(popup);
      }
    }

    @Override
    public void removeStyle(PopupPanel popup) {
      if (styleMutator != null) {
        styleMutator.removeStyle(popup);
      }
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("alignment", alignment)
          .add("offset", tupleToString(offsetX, offsetY))
          .toString();
    }
  }

  // TODO: experimental: allow each AlignmentPref to have an associated style, which is used to get an updated popup width/height on each iteration
  public interface StyleMutator {
    void applyStyle(PopupPanel popup);
    void removeStyle(PopupPanel popup);
  }

  public interface PopupRectangle {
    int getX();
    int getY();
    int getWidth();
    int getHeight();

    Rectangle intersection(Rectangle other);
  }
}
