package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.dom.WidgetQuery;

/**
 * A {@link Label} that dynamically changes its font size to fit within the dimensions of its explicitly-sized
 * parent container.
 *
 * NOTE: this class extends {@link InlineLabel} rather than {@link Label} because if it were a block-rendered element,
 * it would be stretched to fit the entire parent container's width, making the font resizing features implemented
 * by this class useless.
 *
 * @author Alex, 2/25/2016
 */
public class DynamicFontSizeLabel extends InlineLabel {
  private final int minFontSizePct;
  private boolean attemptingAdjustment;
  // TODO: implement this POC


  public DynamicFontSizeLabel(String text, int minFontSizePct) {
    super(text);
    this.minFontSizePct = minFontSizePct;
  }


  public void refresh() {
    // the width could be 0 if the widget is not attached to the DOM or has no content, in which case there's nothing to do
    if (!attemptingAdjustment && needsReduction()) {
      attemptingAdjustment = true;  // prevents infinite recursion if the subclass implementation of makeAdjustments() does something that causes this method to be called again
      makeAdjustments();
      attemptingAdjustment = false;
      maybeResizeFont();
    }
  }

  // TODO: cont here: increase the font size if it's too small (up to 100%)

  private void maybeResizeFont() {
    double fontSizePct = 100d;
    while (needsReduction() && fontSizePct > minFontSizePct) {
      fontSizePct -= 5d;
      getElement().getStyle().setFontSize(fontSizePct, Style.Unit.PCT);
      // TODO: cont here: break the loop if the adjustment stops helping
      System.out.println("SizedLabel: font size reduced to " + fontSizePct + "%");  // TODO: temp
    }
  }

  private boolean needsReduction() {
    Widget parent = WidgetQuery.getDomParent(this);
    return parent != null && (getOffsetWidth() > parent.getOffsetWidth() || getOffsetHeight() > parent.getOffsetHeight());
  }

  /**
   * Gives the subclass a chance to reduce the size of this {@link Label} (by abbreviating the text or some CSS or DOM manipulation).
   */
  protected void makeAdjustments() {
    // subclasses may override
  }

  @Override
  public void setText(String text) {
    super.setText(text);
    refresh();
  }

  @Override
  public void setText(String text, Direction dir) {
    super.setText(text, dir);
    refresh();
  }

  @Override
  public void setWordWrap(boolean wrap) {
    super.setWordWrap(wrap);
    refresh();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    refresh();
  }
}
