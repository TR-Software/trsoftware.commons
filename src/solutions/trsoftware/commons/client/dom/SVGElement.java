package solutions.trsoftware.commons.client.dom;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a native {@code SVGElement}, which is the base interface for
 * all of the SVG DOM interfaces that correspond directly to elements in the SVG language
 * (such as {@code <svg>}, {@code <rect>}, {@code <path>}, etc.).
 * <p>
 * <b>Note:</b> Although this class inherits from {@link Element} for convenience,
 * there are some notable differences between {@code SVGElement} and {@code HTMLElement}
 * which break the following {@link Element} methods:
 * <ul>
 *   <li>{@link #addClassName(String)}, {@link #removeClassName(String)}, {@link #getClassName()}:
 *     because {@code SVGElement.className} is an {@code SVGAnimatedString} rather than a plain string.
 *     Authors are advised to use {@link #getClassList() Element.classList} instead.
 *   </li>
 * </ul>
 * Since JSNI methods are {@code final}, we cannot override the broken methods for this class,
 * so it's up to the developer to ensure that they are never invoked for SVG elements.
 * For the same reason, using an SVG element to implement a {@link Widget} is discouraged.
 *
 * @author Alex
 * @since 12/19/2024
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/SVGElement">SVGElement (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial/SVG_and_CSS">SVG and CSS (MDN)</a>
 */
public class SVGElement extends JsElement {

  protected SVGElement() {
  }

  /**
   * Creates a new {@code SVGElement} from the given SVG string.
   */
  public static SVGElement create(String svgString) {
    // temporarily render the SVG string into a div, to obtain the <svg> Element
    DivElement tempDiv = Document.get().createDivElement();
    tempDiv.setInnerHTML(svgString);
    return (SVGElement)tempDiv.getFirstChildElement();
  }
}
