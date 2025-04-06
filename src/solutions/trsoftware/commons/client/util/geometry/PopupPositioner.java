package solutions.trsoftware.commons.client.util.geometry;

import com.google.gwt.user.client.ui.PopupPanel;
import solutions.trsoftware.commons.shared.util.geometry.Rectangle;

import java.util.List;

/**
 * Finds the best position for a {@link PopupPanel} according to some criteria.
 *
 * @author Alex
 * @since 2/16/2025
 * @see WindowGeometry#positionPopupNextToWidget(PopupPanel, RelativePosition)
 */
public interface PopupPositioner {

  /**
   * Computes a list of possibilities for the position of given popup, sorted by best score based on
   * the criteria encapsulated by this instance.
   */
  List<PositionResult> findBestPosition(PopupPanel popup);

  /**
   * A result returned by {@link #findBestPosition(PopupPanel)}, which can be used to set the popup's position
   * via {@link PopupPanel#setPopupPositionAndShow} or {@link PopupPanel#setPopupPosition}
   */
  interface PositionResult {
    /**
     * @return the resulting popup position rectangle
     */
    Rectangle getPopupRect();

    /**
     * @return the primary score computed for this position by {@link #findBestPosition(PopupPanel)}
     */
    double getScore();

    /**
     * @return optional secondary score
     *   (can be used as a tie-breaker for equal {@linkplain #getScore() primary score} values)
     */
    double getScore2();

    /**
     * Adjusts the popup's style based on what's best for this position
     */
    void applyStyle(PopupPanel popup);
  }
}
