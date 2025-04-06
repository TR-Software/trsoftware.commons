package solutions.trsoftware.commons.client.widgets.popups;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.client.bundle.CommonsCss;
import solutions.trsoftware.commons.client.dom.DOMRect;
import solutions.trsoftware.commons.client.dom.DomUtils;
import solutions.trsoftware.commons.client.dom.JsElement;
import solutions.trsoftware.commons.client.dom.SVGElement;
import solutions.trsoftware.commons.client.dom.observer.ResizeObserver;
import solutions.trsoftware.commons.client.dom.observer.ResizeObserverEntry;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;
import solutions.trsoftware.commons.client.images.CommonsImages;
import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.jso.JsDocument;
import solutions.trsoftware.commons.client.jso.JsObjectArray;
import solutions.trsoftware.commons.client.templates.CommonTemplates;
import solutions.trsoftware.commons.client.widgets.ListPanel;
import solutions.trsoftware.commons.client.widgets.RadioButtonGroup;
import solutions.trsoftware.commons.shared.util.HtmlBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.lenientFormat;
import static java.lang.Double.parseDouble;
import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.client.dom.JsElement.InsertPosition.BEFORE_END;
import static solutions.trsoftware.commons.client.widgets.WidgetDecorator.applyInlineStyles;
import static solutions.trsoftware.commons.client.widgets.Widgets.*;
import static solutions.trsoftware.commons.shared.util.StringUtils.*;
import static solutions.trsoftware.commons.shared.util.function.FunctionalUtils.applyIfNotNull;

/**
 * An alternative implementation of a {@linkplain PopupPanel#glass "glass" element}
 * that can be displayed under a {@link PopupPanel} to obscure the underlying document while the popup is showing.
 * <p>
 * This implementation uses an {@code <svg>} element that allows adding transparent regions in order to highlight
 * certain page elements while leaving the rest of the page obscured.
 * <p>
 * Example:
 * <pre>{@code 
 *   <svg class="glassSvg" style="position: absolute;left: 0px;top: 0px;display: block;width: 1726px;height: 1382px;">
 *     <mask id="holeMask">
 *       <!-- Everything under a white pixel in mask will be visible (i.e. the glass) -->
 *       <rect width="100%" height="100%" fill="white"/>
 *       <!-- Rectangle overlaying an element to cut out of the glass
 *            (anything under a black pixel will be invisible, i.e. a hole through the glass) -->
 *       <rect x="757" y="261" width="61" height="32" fill="black"/>
 *     </mask>
 *     <!-- The glass background: -->
 *     <rect width="100%" height="100%" fill="#0008" mask="url(#holeMask)"/>
 *   </svg>
 * }</pre>
 * Transparent cutouts over page elements (i.e. "holes") can be added with {@link #addMaskHole(Element)} or
 * {@link #addMaskHole(Element, String)}.
 * <p>
 * The glass element is automatically resized and mask regions updated in response to window resize and scroll events,
 * as well as any changes to the size of the underlying {@linkplain #addMaskHole(Element) transparency "hole" elements}.
 * Additional {@link ResizeObserver} targets can be added via {@link #addResizeObserver(Element)} if changes to
 * any other page element could affect the hole positions (e.g. resize of a parent container of a hole element).
 *
 * @author Alex
 * @since 12/4/2024
 * @see CommonTemplates#popup_glass_svg()
 */
public class PopupGlassSvg {
  /**
   * Fill color of mask rects for transparency (black pixels in mask will be transparent)
   */
  private static final String MASK_RECT_COLOR = "black";

  /** The main {@code <svg>} element */
  private final SVGElement svgElement;
  /** The {@code <mask>} element inside the {@code <svg>} element */
  private final SVGElement maskElement;

  /**
   * Mapping of current mask holes by {@code <rect>} element id.
   */
  private final Map<String, MaskRect> maskRects = new LinkedHashMap<>();
  /**
   * Mapping of elements that should be observed for changes while glass {@linkplain #isShowing() is showing}
   * @see #maybeCreateResizeObserver()
   */
  // TODO: can replace Map with a simple Set<Element> b/c don't need to save the HandlerRegistration
  private final Map<Element, HandlerRegistration> resizeObserverTargets;
  private ResizeObserver resizeObserver;

  /**
   * Handlers/observers are added on {@link #show()} and removed on {@link #hide()}
   */
  private MultiHandlerRegistration handlerRegistrations;

  public PopupGlassSvg() {
    String maskId = HTMLPanel.createUniqueId();
    // TODO: move the popup_glass template to an internal TemplateBundle embedded in this class
    String svg = CommonTemplates.INSTANCE.popup_glass_svg().render(
        "svgClass", CommonsCss.get().PopupGlassSvg(),
        "maskId", maskId,
        "backgroundClass", CommonsCss.get().glassBackground()
    );
    svgElement = SVGElement.create(svg);
    Style style = svgElement.getStyle();
    // TODO: maybe move these to stylesheet
    style.setPosition(Style.Position.ABSOLUTE);
    style.setLeft(0, Style.Unit.PX);
    style.setTop(0, Style.Unit.PX);

    maskElement = svgElement.getFirstChildElement().cast();
    assert "mask".equalsIgnoreCase(maskElement.getTagName());

    resizeObserverTargets = new LinkedHashMap<>();  // TODO: maybe lazy init
  }

  public PopupGlassSvg(Element seeThroughElement) {
    this();
    addMaskHole(seeThroughElement);
    // TODO: maybe remove this constructor in favor of chained addMaskHole methods
  }

  /**
   * Adds a transparent region over the given element.
   * <p>
   * <b>Note:</b> although the element will appear unobscured by the glass, it will not be able to receive any
   * mouse or touch events.
   *
   * @param element an element to keep unobscured by the glass
   * @return id of the created mask {@code <rect>}
   */
  public String addMaskHole(Element element) {
    return addMaskHole(element, (SVGRectStyle)null);
  }

  /**
   * Adds a transparent region over the given element.
   * <p>
   * <b>Note:</b> although the element will appear unobscured by the glass, it will not be able to receive any
   * mouse or touch events.
   *
   * @param element an element to keep unobscured by the glass
   * @param styleName optional CSS {@code class} name for the mask {@code <rect>} corresponding to the element
   * @return id of the created mask {@code <rect>}
   */
  public String addMaskHole(Element element, String styleName) {
    return addMaskHole(element, new SVGRectStyle(styleName));
  }

  /**
   * Adds a transparent region over the given element.
   * <p>
   * <b>Note:</b> although the element will appear unobscured by the glass, it will not be able to receive any
   * mouse or touch events.
   *
   * @param element an element to keep unobscured by the glass
   * @param rectStyle (optional) additional parameters for the mask {@code <rect>} corresponding to the element
   * @return id of the created mask {@code <rect>}
   */
  public String addMaskHole(Element element, @Nullable SVGRectStyle rectStyle) {
    requireNonNull(element, "element");
    String rectId = addMaskRect(new MaskRectOverElement(element, rectStyle));
    // observe layout changes for the hole elements
    addResizeObserver(element);
    return rectId;
  }

  /**
   * Adds a rectangular transparent region to the glass.
   * <p>
   * <b>Note:</b> although the element will appear unobscured by the glass, it will not be able to receive any
   * mouse or touch events.
   *
   * @param rect the parameters of the rectangle
   * @param rectStyle (optional) additional parameters for the mask {@code <rect>} corresponding to the element
   * @return id of the created mask {@code <rect>}
   */
  public String addMaskHole(SVGRect rect, @Nullable SVGRectStyle rectStyle) {
    requireNonNull(rect, "rect");
    rect.setFill(MASK_RECT_COLOR);
    return addMaskRect(new CustomMaskRect(rect, rectStyle));
  }

  private String addMaskRect(MaskRect maskRect) {
    String rectId = maskRect.getId();
    maskRects.put(rectId, maskRect);
    SVGRect svgRect = maskRect.getSVGRect();
    String svgString = svgRect.toHtml();
    maskElement.insertAdjacentHTML(BEFORE_END, svgString);
    return rectId;
  }

  /**
   * Removes a transparent region added by {@link #addMaskHole}.
   * @param rectId an id returned by {@link #addMaskHole}
   * @return {@code true} if removed, or {@code false} if not found
   */
  public boolean removeMaskHole(String rectId) {
    MaskRect removed = maskRects.remove(rectId);
    JsElement maskEl;
    if (removed != null && (maskEl = removed.getRectElement()) != null) {
      maskEl.removeFromParent();
      applyIfNotNull(removed.getElement(), this::removeResizeObserver);
      return true;
    }
    return false;
  }

  /**
   * Returns the ids of the {@code mask} that was created via {@link #addMaskHole} for the given element
   * @param element the element that was used to create the hole
   * @return list of {@code mask rect} ids or an empty list if not found
   */
  @VisibleForTesting
  public List<String> getMaskHoleId(Element element) {
    return maskRects.values().stream()
        .filter(maskRect -> maskRect.getElement() == element)
        .map(MaskRect::getId).collect(Collectors.toList());
  }

  // TODO: maybe support removal of mask holes?


  /**
   * Registers a {@link ResizeObserver} target.  This should be invoked for any elements that could change in
   * a way that shifts the hole positions (e.g. a parent container of a {@linkplain #addMaskHole(Element) hole elements}).
   *
   * @param elementToObserve an element that might change size in a way that affects any of the defined hole positions.
   * @return a remover to unsubscribe the given element from observation
   */
  public HandlerRegistration addResizeObserver(Element elementToObserve) {
    requireNonNull(elementToObserve, "elementToObserve");
    assert resizeObserverTargets != null;  // TODO: maybe lazy init
    if (resizeObserver != null && isShowing()) {
      // if the glass is already showing, start observing this element right away (otherwise this will be deferred until show())
      resizeObserver.observe(elementToObserve);
      // TODO: make sure not already observing this element?
    }
    return resizeObserverTargets.computeIfAbsent(elementToObserve, element ->
        () -> removeResizeObserver(element));
  }

  /**
   * Removes a {@link ResizeObserver} target added by {@link #addResizeObserver(Element)}.
   * @param element the element to stop observing
   */
  public void removeResizeObserver(Element element) {
    if (resizeObserver != null)
      resizeObserver.unobserve(element); // stop observing this element right away
    resizeObserverTargets.remove(element);
  }

  private HandlerRegistration maybeCreateResizeObserver() {
    if (resizeObserver == null && (!maskRects.isEmpty() || !resizeObserverTargets.isEmpty())) {
      resizeObserver = ResizeObserver.Impl.create(this::onElementResize);
    }
    if (resizeObserver != null) {
      resizeObserverTargets.keySet().forEach(resizeObserver::observe);
    }
    return () -> {  // remover:
      if (resizeObserver != null) {
        resizeObserver.disconnect();
        resizeObserver = null;  // allow GC on the ResizeObserver (not much overhead to re-create it again on next show, which is unlikely to be invoked more than once)
      }
    };
  }

  /**
   * Invokes {@link #adjustSize()} in response to resize notifications from {@link #resizeObserver}.
   *
   * @param entries
   * @param observer
   */
  private void onElementResize(ResizeObserverEntry[] entries, ResizeObserver observer) {
    // TODO: temp debug logging
    JsConsole.get().logVarArgs(JsConsole.Level.DEBUG, JsObjectArray.create()
        .add(lenientFormat("%s.resizeObserver", getClass().getSimpleName()))
        .add(entries).add(observer));

    if (isShowing()) {
      // only need to invoke adjustMaskRects (don't need to resize the full glass unless the full window is resized or scrolled)
      adjustMaskRects();
    }
  }

  /**
   * Updates the size of the glass element and mask rectangles, to keep it covering the entire viewport and
   * keep the transparent regions positioned over their corresponding "hole" elements.
   * This should be called in response to changes in window size, scroll offset, or sizes of other elements.
   */
  private void adjustSize() {
    // copied from com.google.gwt.user.client.ui.PopupPanel.glassResizer
    Style style = svgElement.getStyle();

    int winWidth = Window.getClientWidth();
    int winHeight = Window.getClientHeight();

    // Hide the glass while checking the document size. Otherwise it would
    // interfere with the measurement.
    style.setDisplay(Style.Display.NONE);
    style.setWidth(0, Style.Unit.PX);
    style.setHeight(0, Style.Unit.PX);

    int width = Document.get().getScrollWidth();
    int height = Document.get().getScrollHeight();

    // Set the glass size to the larger of the window's client size or the
    // document's scroll size.
    style.setWidth(Math.max(width, winWidth), Style.Unit.PX);
    style.setHeight(Math.max(height, winHeight), Style.Unit.PX);

    // The size is set. Show the glass again.
    style.setDisplay(Style.Display.BLOCK);

    adjustMaskRects();
  }

  /**
   * Updates the mask rectangles to keep them correctly positioned over their corresponding "hole" elements.
   * This should be called in response to changes in window size, scroll offset, or sizes of other elements.
   */
  private void adjustMaskRects() {
    maskRects.values().forEach(MaskRect::maybeUpdateMask);
  }

  /**
   * Attaches the SVG element to the Document's body.
   */
  public void show() {
    Document.get().getBody().appendChild(svgElement);
    adjustSize();
    if (handlerRegistrations == null) {
      handlerRegistrations = new MultiHandlerRegistration(
          Window.addResizeHandler(event -> adjustSize()),
          Window.addWindowScrollHandler(event -> adjustSize()),
          maybeCreateResizeObserver()
      );
    }
  }

  /**
   * Removes the SVG element from the Document's body.
   */
  public void hide() {
    if (isShowing()) {  // make sure it's actually attached, to avoid a NotFoundError from Node.removeChild
      Document.get().getBody().removeChild(svgElement);
      if (handlerRegistrations != null) {
        handlerRegistrations.removeHandler();
        handlerRegistrations = null;
      }
    }
  }

  public boolean isShowing() {
    return Document.get().getBody().isOrHasChild(svgElement);
  }

  // style manipulation methods similar to those provided by UIObject:

  /**
   * Adds a secondary CSS {@code class} name to the {@code SVG} element.
   *
   * @param style the secondary style name to be added
   * @see #removeStyleName(String)
   */
  public void addStyleName(String style) {
    // Note: can't use GWT's Element.addClassName method b/c the className property of an SVG element is an instance of SVGAnimatedString rather than String
    svgElement.getClassList().add(style);
  }

  /**
   * Gets all of the {@code SVG} element's {@code class} names, as a space-separated list.
   *
   * @return the objects's space-separated style names
   */
  public String getStyleName() {
    // Note: can't use GWT's Element.getClassName method b/c the className property of an SVG element is an instance of SVGAnimatedString rather than String
    return svgElement.getClassList().getValue();
  }


  /**
   * Removes a {@code class} name from the {@code SVG} element.
   *
   * @param style the secondary style name to be removed
   * @see #addStyleName(String)
   * @see #setStyleName(String, boolean)
   */
  public void removeStyleName(String style) {
    // Note: can't use GWT's Element.removeClassName method b/c the className property of an SVG element is an instance of SVGAnimatedString rather than String
    svgElement.getClassList().remove(style);
  }

  /**
   * Assigns a new value for the {@code SVG} element's {@code class} attribute.
   * This clears all of its style names and sets it to the given style.
   *
   * @param style the new style name
   */
  public void setStyleName(String style) {
    svgElement.setAttribute("class", style);
  }


  /**
   * Encapsulates the data associated with a {@code <mask> <rect>}
   */
  public static abstract class MaskRect {
    @Nonnull
    protected final String id;
    @Nullable
    protected SVGRectStyle rectStyle;

    protected MaskRect() {
      id = HTMLPanel.createUniqueId();
    }

    protected MaskRect(@Nullable SVGRectStyle rectStyle) {
      this();
      this.rectStyle = rectStyle;
    }

    /**
     * @return id of the {@code <rect>} element
     */
    @Nonnull
    public String getId() {
      return id;
    }

    /**
     * @return style of the
     */
    @Nullable
    public SVGRectStyle getRectStyle() {
      return rectStyle;
    }

    /**
     * @return the underlying page element covered by this mask rect,
     *     or {@code null} if this mask rect isn't associated with a specific element
     */
    @Nullable
    public Element getElement() {
      return null;  // subclasses can override
    }

    JsElement getRectElement() {
      return JsElement.getById(id);
    }

    /**
     * @return the parameters of the rectangle
     */
    public abstract SVGRect getSVGRect();

    /**
     * Updates the {@code <rect>} element, if needed, to keep it in sync with an underlying page element.
     */
    void maybeUpdateMask() {
      // subclasses can override if needed
    }
  }

  /**
   * Specifies a mask "hole" cutout over an underlying page element.
   *
   * @see #addMaskHole(Element, SVGRectStyle) 
   */
  static class MaskRectOverElement extends MaskRect {
    @Nonnull private final Element element;

    MaskRectOverElement(@Nonnull Element element) {
      this(element, null);
    }

    MaskRectOverElement(@Nonnull Element element, @Nullable SVGRectStyle rectStyle) {
      super(rectStyle);
      this.element = requireNonNull(element, "element");
    }

    @Override
    @Nonnull
    public Element getElement() {
      return element;
    }

    @Override
    public SVGRect getSVGRect() {
      return toSVGRect(DomUtils.getBoundingClientRect(element));
    }

    private SVGRect toSVGRect(@Nonnull DOMRect domRect) {
      SVGRect svgRect = new SVGRect(requireNonNull(domRect, "elRect"))
          .setFill(MASK_RECT_COLOR)
          .setId(getId());
      applyIfNotNull(rectStyle, svgRect::applyStyle);
      return svgRect;
    }

    @Override
    public void maybeUpdateMask() {
      DOMRect elRect = DomUtils.getBoundingClientRect(element);
      JsElement maskEl = getRectElement();
      checkNotNull(maskEl, "Mask rect %s", id);
      DOMRect maskRect = maskEl.getBoundingClientRect();
      if (!DOMRect.equals(elRect, maskRect)) {
        SVGRect svgRect = toSVGRect(elRect);
        svgRect.updateElement(maskEl.cast());
      }
    }
  }

  /**
   * Specifies a mask "hole" cutout over an underlying page element.
   *
   * @see #addMaskHole(Element, SVGRectStyle) 
   */
  static class CustomMaskRect extends MaskRect {
    @Nonnull private final SVGRect svgRect;

    CustomMaskRect(@Nonnull SVGRect svgRect) {
      this(svgRect, null);
    }

    CustomMaskRect(@Nonnull SVGRect svgRect, @Nullable SVGRectStyle rectStyle) {
      super(rectStyle);
      this.svgRect = requireNonNull(svgRect, "svgRect");
      svgRect.setFill(MASK_RECT_COLOR).setId(getId());
      applyIfNotNull(rectStyle, svgRect::applyStyle);
    }

    @Override
    public SVGRect getSVGRect() {
      // TODO: apply the SVGRectStyle
      return svgRect;
    }
  }


  /**
   * Defines the parameters for an SVG {@code <rect>} element, and provides the ability to generate an SVG string
   * ({@link #toHtml()}) to create a the element or to update an existing element ({@link #updateElement(SVGElement)}).
   */
  public static class SVGRect {
    /**
     * Required attributes
     */
    private double x, y, width, height;
    private String fill;
    @Nullable
    private String id, className;
    /**
     * Optional border radius
     */
    @Nullable
    private Double rx, ry;

    public SVGRect(double x, double y, double width, double height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }

    public SVGRect(DOMRect domRect) {
      this(domRect.getAbsoluteX(), domRect.getAbsoluteY(), domRect.getWidth(), domRect.getHeight());
      fill = MASK_RECT_COLOR;
    }

    public SVGRect expand(double top, double right, double bottom, double left) {
      padLeft(left);
      padTop(top);
      padRight(right);
      padBottom(bottom);
      return this;
    }

    public SVGRect padTop(double top) {
      y -= top;
      height += top;
      return this;
    }

    public SVGRect padLeft(double left) {
      x -= left;
      width += left;
      return this;
    }

    public SVGRect padRight(double right) {
      width += right;
      return this;
    }

    public SVGRect padBottom(double bottom) {
      height += bottom;
      return this;
    }

    public SVGRect applyStyle(SVGRectStyle style) {
      className = style.getClassName();
      rx = style.getRx();
      ry = style.getRy();
      applyIfNotNull(style.getPaddingTop(), this::padTop);
      applyIfNotNull(style.getPaddingLeft(), this::padLeft);
      applyIfNotNull(style.getPaddingRight(), this::padRight);
      applyIfNotNull(style.getPaddingBottom(), this::padBottom);
      return this;
    }

    private Map<String, String> getAttributeMap() {
      Map<String, String> attrs = new LinkedHashMap<>();
      attrs.put("x", valueOf(x));
      attrs.put("y", valueOf(y));
      attrs.put("width", valueOf(width));
      attrs.put("height", valueOf(height));
      if (fill != null)
        attrs.put("fill", fill);
      if (id != null)
        attrs.put("id", id);
      if (className != null)
        attrs.put("class", className);
      if (rx != null)
        attrs.put("rx", rx.toString());
      if (ry != null)
        attrs.put("ry", ry.toString());
      
      return attrs;
    }

    public String toHtml() {
      return new HtmlBuilder().openTag("rect").attrs(getAttributeMap()).closeTag().toString();
    }

    /**
     * Modifies the given {@code <rect>} element to match the attributes of this instance.
     *
     * @param rectElement a {@code <rect>} element
     */
    public void updateElement(SVGElement rectElement) {
      //noinspection Convert2MethodRef - can't use method ref here in classic DevMode
      getAttributeMap().forEach((name, value) -> rectElement.setAttribute(name, value));
    }

    public double getX() {
      return x;
    }

    public SVGRect setX(double x) {
      this.x = x;
      return this;
    }

    public double getY() {
      return y;
    }

    public SVGRect setY(double y) {
      this.y = y;
      return this;
    }

    public double getWidth() {
      return width;
    }

    public SVGRect setWidth(double width) {
      this.width = width;
      return this;
    }

    public double getHeight() {
      return height;
    }

    public SVGRect setHeight(double height) {
      this.height = height;
      return this;
    }

    public String getFill() {
      return fill;
    }

    public SVGRect setFill(String fill) {
      this.fill = fill;
      return this;
    }

    @Nullable
    public String getId() {
      return id;
    }

    public SVGRect setId(@Nullable String id) {
      this.id = id;
      return this;
    }

    @Nullable
    public String getClassName() {
      return className;
    }

    public SVGRect setClassName(@Nullable String className) {
      this.className = className;
      return this;
    }

    @Nullable
    public Double getRx() {
      return rx;
    }

    public SVGRect setRx(@Nullable Double rx) {
      this.rx = rx;
      return this;
    }

    @Nullable
    public Double getRy() {
      return ry;
    }

    public SVGRect setRy(@Nullable Double ry) {
      this.ry = ry;
      return this;
    }
  }

  /**
   * Defines additional parameters for an {@link SVGRect}, such as CSS class name, corner radius, and padding
   * (which adjusts the rectangle's size and position).
   */
  public static class SVGRectStyle {
    @Nullable
    private String className;
    /**
     * Optional padding
     */
    @Nullable
    private Double paddingTop, paddingRight, paddingBottom, paddingLeft;
    /**
     * Optional corner radius
     */
    @Nullable
    private Double rx, ry;

    public SVGRectStyle() {
    }

    public SVGRectStyle(String className) {
      this.className = className;
    }

    public String getClassName() {
      return className;
    }

    public SVGRectStyle setClassName(String className) {
      this.className = className;
      return this;
    }

    @Nullable
    public Double getPaddingTop() {
      return paddingTop;
    }

    public SVGRectStyle setPaddingTop(@Nullable Double paddingTop) {
      this.paddingTop = paddingTop;
      return this;
    }

    @Nullable
    public Double getPaddingRight() {
      return paddingRight;
    }

    public SVGRectStyle setPaddingRight(@Nullable Double paddingRight) {
      this.paddingRight = paddingRight;
      return this;
    }

    @Nullable
    public Double getPaddingBottom() {
      return paddingBottom;
    }

    public SVGRectStyle setPaddingBottom(@Nullable Double paddingBottom) {
      this.paddingBottom = paddingBottom;
      return this;
    }

    @Nullable
    public Double getPaddingLeft() {
      return paddingLeft;
    }

    public SVGRectStyle setPaddingLeft(@Nullable Double paddingLeft) {
      this.paddingLeft = paddingLeft;
      return this;
    }

    public SVGRectStyle setPadding(Double top, Double right, Double bottom, Double left) {
      this.paddingTop = top;
      this.paddingRight = right;
      this.paddingBottom = bottom;
      this.paddingLeft = left;
      return this;
    }

    /**
     * Sets equal padding for top and bottom, as well as left and right.
     *
     * @param topBottom padding top and bottom
     * @param leftRight padding left and right
     */
    public SVGRectStyle setPadding(Double topBottom, Double leftRight) {
      return setPadding(topBottom, leftRight, topBottom, leftRight);
    }

    /**
     * Sets equal padding on all 4 sides to the given value.
     * 
     * @param padding value for {@link #paddingTop}, {@link #paddingRight}, {@link #paddingBottom}, and {@link #paddingLeft}
     */
    public SVGRectStyle setPadding(@Nullable Double padding) {
      return setPadding(padding, padding, padding, padding);
    }

    @Nullable
    public Double getRx() {
      return rx;
    }

    public SVGRectStyle setRx(@Nullable Double rx) {
      this.rx = rx;
      return this;
    }

    @Nullable
    public Double getRy() {
      return ry;
    }

    public SVGRectStyle setRy(@Nullable Double ry) {
      this.ry = ry;
      return this;
    }

    /**
     * Sets the horizontal and vertical corner radius to the given value
     * @param value {@link #rx} and {@link #ry}
     */
    public SVGRectStyle setCornerRadius(@Nullable Double value) {
      this.rx = value;
      this.ry = value;
      return this;
    }

    /**
     * Sets the horizontal and vertical corner radius to the given values
     * @param value {@link #rx} and {@link #ry}
     */
    public SVGRectStyle setCornerRadius(@Nullable Double rx, @Nullable Double ry) {
      this.rx = rx;
      this.ry = ry;
      return this;
    }
  }


  /*
  ================================================================================
  Testing utils:
  ================================================================================
  */

  public static class Tester extends PopupDialog {
    // TODO: remove this class after testing completed (or move to debug pkg)

    private final PopupGlassSvg glass;
    private final SimplePanel pnlConfigHolder;
    private ConfigForm configForm;
    private final ListPanel pnlMaskHoleList;
    private final DisclosurePanel pnlMaskHoleListContainer;

    public Tester() {
      super(false, AbstractImagePrototype.create(CommonsImages.get().help24()),
          PopupGlassSvg.class.getSimpleName() + " Tester",
          PopupGlassSvg.class.getSimpleName() + "TesterPopup");
      glass = new PopupGlassSvg();
      setCustomGlass(glass);
      configForm = new ConfigForm();
      pnlConfigHolder = new SimplePanel(configForm);
      pnlConfigHolder.setStyleName(CommonsCss.get().contentSection());
      // controls for adding mask holes:
      setBodyWidget(flowPanel(
          label("Mask Hole Spec:"),
          pnlConfigHolder,
          new Button("Add to mask", (ClickHandler)event -> addMaskHole(configForm)),
          pnlMaskHoleListContainer = disclosurePanel("Current Holes",
              pnlMaskHoleList = new ListPanel(ListPanel.Type.OL))
      ));
    }

    /**
     * Shows alert with the given message and returns {@code null}.
     */
    static <T> T errorMessage(String msg) {
      Window.alert(msg);
      return null;
    }

    private void addMaskHole(ConfigForm config) {
      HoleType holeType = config.getHoleType();
      String createdHoleId = null;
      if (holeType == HoleType.ELEMENT) {
        String selector = config.getSelector();
        Element element = getElement(selector);
        if (element != null)
          createdHoleId = glass.addMaskHole(element, config.getSVGRectStyle());
      }
      else if (holeType == HoleType.RECT) {
        SVGRect rect = config.getSVGRect();
        if (rect != null)
          createdHoleId = glass.addMaskHole(rect, config.getSVGRectStyle());
      }
      if (createdHoleId != null) {
        pnlMaskHoleList.add(new MaskHoleInfo(createdHoleId, config));
        pnlMaskHoleListContainer.setOpen(true);
      }
    }

    @Nullable
    public static Element promptForElementSelector() {
      String selector = Window.prompt("Glass hole selector:", ".tblActivePlayers tr:nth-child(2) td.playerScore");
      return getElement(selector);
    }

    @Nullable
    public static Element getElement(String selector) {
      if (notBlank(selector)) {
        selector = selector.trim();
        Element element = JsDocument.get().querySelector(selector);
        if (element != null)
          return element;
        else
          return errorMessage(lenientFormat("The selector (%s) did not match any elements", selector));
      }
      return errorMessage("Element selector not specified");
    }

    enum HoleType {
      ELEMENT("Selector"), RECT("Rectangle");
      private final String label;
      HoleType(String label) {
        this.label = label;
      }
      @Override
      public String toString() {
        return label;
      }
    }

    private class MaskHoleInfo extends Composite {
      private final String rectId;

      public MaskHoleInfo(String rectId, ConfigForm config) {
        this.rectId = rectId;
        initWidget(inlineFlowPanel(
            applyInlineStyles(
                inlineLabel(config.toString()),
                style -> style.setProperty("fontFamily", "monospace")),
            new Button("Remove", (ClickHandler)event -> {
              boolean removed = glass.removeMaskHole(rectId);
              if (removed)
                removeFromParent();
            })
        ));
      }
    }

    private static class ConfigForm extends Composite {
      // TODO: temp for testing
      private final TextBox txtSelector = new TextBox();
      private final TextBox txtClassName = new TextBox();
      private final TextBox txtPadding = new TextBox();
      private final TextBox txtRadius = new TextBox();
      private final RadioButtonGroup<HoleType> holeTypeRadio;

      public ConfigForm() {
        txtSelector.setVisibleLength(30);
        holeTypeRadio = new RadioButtonGroup<>(EnumSet.allOf(HoleType.class));
        holeTypeRadio.addValueChangeHandler(new ValueChangeHandler<HoleType>() {
          @Override
          public void onValueChange(ValueChangeEvent<HoleType> event) {
            txtSelector.setText("");
            switch (event.getValue()) {
              case ELEMENT:
                txtSelector.getElement().setAttribute("placeholder", "CSS selector");
                break;
              case RECT:
                txtSelector.getElement().setAttribute("placeholder", "x y width height");
                break;
            }
          }
        });
        /*holeTypeRadio.addValueChangeHandler(event -> {
          txtSelector.setText("");
          switch (event.getValue()) {
            case ELEMENT:
              txtSelector.getElement().setAttribute("placeholder", "CSS selector");
              break;
            case RECT:
              txtSelector.getElement().setAttribute("placeholder", "x y width height");
              break;
          }
        });*/
        holeTypeRadio.setValue(HoleType.ELEMENT);
        FlexTable tblStyle = new FlexTable();

        initWidget(flowPanel(
            flowPanel(holeTypeRadio.getButtons().values()),
            txtSelector,
            disclosurePanel(SVGRectStyle.class.getSimpleName(), false,
                tblStyle)
        ));
        int row = -1;
        tblStyle.setText(++row, 0, "className");
        tblStyle.setWidget(row, 1, txtClassName);
        tblStyle.setText(++row, 0, "padding");
        tblStyle.setWidget(row, 1, txtPadding);
        tblStyle.setText(++row, 0, "radius");
        tblStyle.setWidget(row, 1, txtRadius);
      }

      @Nullable
      public HoleType getHoleType() {
        return holeTypeRadio.getValue();
      }

      private void assertHoleType(HoleType expected) {
        checkState(getHoleType() == expected, "Selected hole type is not %s", expected);
      }

      public String getSelector() {
        assertHoleType(HoleType.ELEMENT);
        return txtSelector.getText();
      }

      @Nullable
      public SVGRect getSVGRect() {
        assertHoleType(HoleType.RECT);
        String text = txtSelector.getText();
        List<String> parts = splitAndTrim(text, " ");
        if (parts.size() != 4) {
          return errorMessage("Need 4 numbers for rectangle: x y width height");
        }
        try {
          double[] values = parts.stream()
              .mapToDouble(Double::parseDouble).toArray();
          return new SVGRect(values[0], values[1], values[2], values[3]);
        }
        catch (NumberFormatException ex) {
          return errorMessage("Need 4 numbers for rectangle: x y width height");
        }
      }

      public String getRadius() {
        return txtRadius.getText();
      }

      public String getPadding() {
        return txtPadding.getText();
      }

      public String getClassName() {
        return txtClassName.getText();
      }

      @Nullable
      public SVGRectStyle getSVGRectStyle() {
        String className = getClassName();
        String padding = getPadding();
        String radius = getRadius();
        SVGRectStyle style = new SVGRectStyle();
        boolean hasData = false;
        if (notBlank(className)) {
          style.setClassName(className.trim());
          hasData = true;
        }
        if (notBlank(padding)) {
          // TODO: extract the number arr parsing & error handling code shared with getSvgRect
          List<String> parts = splitAndTrim(padding, " ");
          switch (parts.size()) {
            case 1: /* apply to all four sides */
              style.setPadding(parseDouble(parts.get(0)));
              break;
            case 2: /* top and bottom | left and right */
              style.setPadding(
                  parseDouble(parts.get(0)),
                  parseDouble(parts.get(1)));
              break;
            case 4: /* top | right | bottom | left */
              style.setPadding(
                  parseDouble(parts.get(0)),
                  parseDouble(parts.get(1)),
                  parseDouble(parts.get(2)),
                  parseDouble(parts.get(3)));
              break;
            default:
              errorMessage("Padding must contain 1, 2, or 4 numbers");
          }
          hasData = true;
        }
        if (notBlank(radius)) {
          // TODO: extract the number arr parsing & error handling code shared with getSvgRect
          List<String> parts = splitAndTrim(radius, " ");
          if (parts.size() == 1) { /* same rx and ry */
            style.setCornerRadius(parseDouble(parts.get(0)));
          }
          else if (parts.size() == 2) { /* separate rx and ry */
            style.setCornerRadius(
                parseDouble(parts.get(0)),
                parseDouble(parts.get(1)));
          }
          else {
            throw new IllegalArgumentException("Radius must contain 1 or 2 numbers");
          }
          hasData = true;
        }
        return hasData ? style : null;
      }

      @Override
      public String toString() {
        String selText = txtSelector.getText();
        String objName = getHoleType() == HoleType.RECT
            ? lenientFormat("[%s]", selText)
            : selText;
        return appendStyle(MoreObjects.toStringHelper(objName)).toString();
      }

      private ToStringHelper appendStyle(ToStringHelper str) {
        applyIfNotBlank(getClassName(), v -> str.add("cls", quote(v)));
        applyIfNotBlank(getPadding(), v -> str.add("pad", quote(v)));
        applyIfNotBlank(getRadius(), v -> str.add("rad", quote(v)));
        return str;
      }
    }
    
  }

}
