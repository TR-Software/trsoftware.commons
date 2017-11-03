package solutions.trsoftware.commons.client.styles;

import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Defines style elements particular to a CellPanel, in addition
 * to those that apply to widgets in general.
 *
 * @author Alex
 */
public class HtmlTableStyle extends WidgetStyle {
  private int spacing;

  public HtmlTableStyle() {
  }

  public HtmlTableStyle(String styleName) {
    super(styleName);
  }

  /** Applies the style to the given cell panel */
  public Widget apply(Widget widget) {
    HTMLTable targetTable = (HTMLTable) widget;

    // apply general widget style
    super.apply(widget);

    // apply spacing, if any
    if (spacing != 0)
      targetTable.setCellSpacing(spacing);

    return targetTable;
  }

  public HtmlTableStyle setSpacing(int spacing) {
    this.spacing = spacing;
    return this;
  }

  public HtmlTableStyle setDimensions(String width, String height) {
    return (HtmlTableStyle) super.setDimensions(width, height);
  }

  public HtmlTableStyle setHeight(String height) {
    return (HtmlTableStyle) super.setHeight(height);
  }

  public HtmlTableStyle setStyleName(String styleName) {
    return (HtmlTableStyle) super.setStyleName(styleName);
  }

  public HtmlTableStyle setWidth(String width) {
    return (HtmlTableStyle) super.setWidth(width);
  }
}