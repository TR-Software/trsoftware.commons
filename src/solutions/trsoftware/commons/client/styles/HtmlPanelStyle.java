package solutions.trsoftware.commons.client.styles;

import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Like CellPanelStyle, except also supports the cell padding property.
 *
 * @author Alex
 */
public class HtmlPanelStyle extends WidgetStyle {
  private int padding;
  private int spacing;

  public HtmlPanelStyle() {
  }

  public HtmlPanelStyle(String styleName) {
    super(styleName);
  }

  public Widget apply(Widget widget) {
    HTMLTable table = (HTMLTable) widget;

    // apply general widget styles
    super.apply(widget);

    // apply properties specific to a HTMLTable
    if (padding != 0)
      table.setCellPadding(padding);
    if (spacing != 0)
      table.setCellSpacing(spacing);

    return table;
  }

  public HtmlPanelStyle setPadding(int padding) {
    this.padding = padding;
    return this;
  }

  public HtmlPanelStyle setSpacing(int spacing) {
    this.spacing = spacing;
    return this;
  }
}
