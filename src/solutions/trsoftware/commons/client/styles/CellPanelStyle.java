package solutions.trsoftware.commons.client.styles;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Defines style elements particular to a CellPanel, in addition
 * to those that apply to widgets in general.
 * 
 * Implements a builder pattern (using method chaining) for declaring a widget's
 * style properties.
 *
 * @author Alex
 */
public class CellPanelStyle extends WidgetStyle {
  private int spacing;

  public CellPanelStyle() {
    super();
  }

  public CellPanelStyle(String styleName) {
    super(styleName);
  }

  /** Applies the style to the given cell panel */
  public Widget apply(Widget widget) {
    CellPanel targetPanel = (CellPanel) widget;

    // apply general widget styles
    super.apply(widget);

    // apply properties specific to a CellPanel
    if (spacing != 0)
      targetPanel.setSpacing(spacing);

    return targetPanel;
  }

  public CellPanelStyle setSpacing(int spacing) {
    this.spacing = spacing;
    return this;
  }

  public CellPanelStyle setDimensions(String width, String height) {
    return (CellPanelStyle) super.setDimensions(width, height);
  }

  public CellPanelStyle setHeight(String height) {
    return (CellPanelStyle) super.setHeight(height);
  }

  public CellPanelStyle setStyleName(String styleName) {
    return (CellPanelStyle) super.setStyleName(styleName);
  }

  public CellPanelStyle setWidth(String width) {
    return (CellPanelStyle) super.setWidth(width);
  }
}
