package solutions.trsoftware.commons.client.dom.observer;

/**
 * Represents the width and height of an element based on the writing-mode,
 * which is used by the {@link ResizeObserverEntry} interface to access the box sizing properties of the element being observed.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Resize_Observer_API">ResizeObserver API (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverSize">ResizeObserverSize (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Styling_basics/Handling_different_text_directions">
 *   Handling different text directions (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/writing-mode"><code>writing-mode</code> (CSS property)</a>
 * @author Alex
 * @since 2/27/2025
 */
public interface ResizeObserverSize {
  /**
   * The length of the observed element's border box in the block dimension.
   * For boxes with a horizontal writing-mode, this is the vertical dimension, or height;
   * if the writing-mode is vertical, this is the horizontal dimension, or width.
   */
  double getBlockSize();

  /**
   * The length of the observed element's border box in the inline dimension.
   * For boxes with a horizontal writing-mode, this is the horizontal dimension, or width;
   * if the writing-mode is vertical, this is the vertical dimension, or height.
   */
  double getInlineSize();
}
