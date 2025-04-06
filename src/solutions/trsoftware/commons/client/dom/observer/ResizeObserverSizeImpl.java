package solutions.trsoftware.commons.client.dom.observer;

/**
 * An immutable {@link ResizeObserverSize} implementation.
 *
 * @author Alex
 * @since 2/27/2025
 */
public class ResizeObserverSizeImpl implements ResizeObserverSize {
  private final double width;
  private final double height;
  /**
   * Writing mode of the element, used to determine how the element's width and height map to blockSize and inlineSize.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Styling_basics/Handling_different_text_directions">
   *   Handling different text directions</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/writing-mode"><code>writing-mode</code> (CSS)</a>
   */
  private final boolean vertical;

  public ResizeObserverSizeImpl(double width, double height, boolean vertical) {
    this.width = width;
    this.height = height;
    this.vertical = vertical;
  }

  @Override
  public double getBlockSize() {
    return vertical ? width : height;
  }

  @Override
  public double getInlineSize() {
    return vertical ? height : width;
  }
}
