package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.util.ArrayUtils;

/**
 * A lightweight version of {@link DeckPanel}, that has the same support for showing one widget at the time, but doesn't
 * mess with the width, height, and display properties of the widgets (which sometimes create layout problems with
 * {@link DeckPanel}). Implements the one-at-a-time behaviour simply by attaching/detaching the children from the embedded
 * {@link SimplePanel}.
 *
 * @author Alex, 3/24/2015
 */
public class SimpleDeckPanel extends Composite {

  private final SimplePanel pnlContainer = new SimplePanel();
  private final Widget[] widgets;
  private int visibleWidgetIndex;

  public SimpleDeckPanel(Widget... widgets) {
    this.widgets = widgets;
    initWidget(pnlContainer);
    setWidget(0);
  }

  /**
   * Gets the index of the currently-visible widget.
   *
   * @return the visible widget's index
   */
  public int getVisibleWidgetIndex() {
    return visibleWidgetIndex;
  }

  /**
   * Gets the currently-visible widget.
   *
   * @return the visible widget's index
   */
  public Widget getVisibleWidget() {
    return widgets[getVisibleWidgetIndex()];
  }

  /**
   * Shows the widget at the specified index. This causes the currently-visible widget to be detached.
   *
   * @param index the index of the widget to be shown
   */
  public void setWidget(int index) {
    pnlContainer.setWidget(widgets[visibleWidgetIndex = index]);
  }

  /**
   * Shows the given widget. This causes the currently-visible widget to be detached.
   *
   * @param widget the widget to be shown, must have been present in the array passed to the constructor.
   */
  public void setWidget(Widget widget) {
    setWidget(ArrayUtils.linearSearch(widgets, widget));
  }



}
