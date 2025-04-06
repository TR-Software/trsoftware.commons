package solutions.trsoftware.commons.client.util.geometry;

import com.google.common.base.MoreObjects;
import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.shared.util.geometry.Rectangle;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base class for {@link PopupPositioner} implementations that consider a set of elements not to obstruct when
 * choosing a suitable popup position.
 *
 * @author Alex
 * @since 2/16/2025
 */
public abstract class AbstractPopupPositioner implements PopupPositioner {

  /**
   * Popup will be positioned relative to this element
   */
  protected Element pivot;
  /**
   * The elements that shouldn't be covered by the popup.
   * The values in this map are meant to represent the percentage of each element that is okay to cover
   */
  protected Map<Element, Double> coverWeights;  // TODO: could make this a map of rects rather than Elements, for finer control of what portions can't be covered
  // NOTE: coverWeights values not currently used by any subclass

  /**
   * @param pivot element relative to which the popup position will be computed
   * @param elementsToNotObstruct the set of elements that shouldn't be covered by the popup
   */
  public AbstractPopupPositioner(Element pivot, Set<Element> elementsToNotObstruct) {
    this(pivot,
        elementsToNotObstruct.stream().collect(Collectors.toMap(Function.identity(), e -> 1d)));
  }

  /**
   * @param pivot element relative to which the popup position will be computed
   * @param elementsToNotObstruct mapping of the set of elements that shouldn't be covered by the popup, with
   *   values providing the "weights" of those elements (e.g. pct of the element that it's okay to cover)
   *   to consider when ranking the popup position possibilities
   */
  public AbstractPopupPositioner(Element pivot, Map<Element, Double> elementsToNotObstruct) {
    this.pivot = pivot;
    this.coverWeights = elementsToNotObstruct;
  }

  public static abstract class Result implements PopupPositioner.PositionResult, Comparable<Result> {
    protected Rectangle popupRect;
    /** Pct visible popup area not covering visible element areas */
    protected double score;
    /** Pct of total element area not covered by popup (without visibility considerations) */
    protected double score2;  // TODO: experimental

    public Result(Rectangle popupRect, double score) {
      this.popupRect = popupRect;
      this.score = score;
    }

    public Result(Rectangle popupRect, double score, double score2) {
      this(popupRect, score);
      this.score2 = score2;
    }

    public Rectangle getPopupRect() {
      return popupRect;
    }

    public double getScore() {
      return score;
    }

    public double getScore2() {
      return score2;
    }

    /**
     * Descending order by {@link #score}
     */
    @Override
    public int compareTo(Result o) {
      return Double.compare(o.score, score);  // descending order by score
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .addValue(popupRect)
          .add("score", score)
          .add("score2", score2)
          .toString();
    }
  }

}
