package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Widget;


/**
 * Can be used to decorate any widget with a flashing effect.  This class accomplishes that by repeatedly
 * adding and removing a given suffix to the widget's primary style name, but other subclasses of
 * {@link solutions.trsoftware.commons.client.widgets.AbstractFlashingWidget} subclasses may use a different strategy
 * in their {@link #setFlashOn(boolean)} method implementation.
 *
 * @author Alex
 */
public class FlashingWidget<T extends Widget> extends AbstractFlashingWidget<T> {

  private String flashOnStyle = "flashOn";

  public FlashingWidget(T widget, int flashingDelay) {
    super(widget, flashingDelay);
  }

  /**
   * Use this constructor to synchronize the flashing of this widget with another flashing widget so they both
   * flash at the same time.
   */
  public FlashingWidget(T widget, PooledFlashingTimer flashingTimer) {
    super(widget, flashingTimer);
  }

  public FlashingWidget<T> setFlashOnStyle(String flashOnStyle) {
    this.flashOnStyle = flashOnStyle;
    return this;
  }

  public String getFlashOnStyle() {
    return flashOnStyle;
  }

  /** Applies or removes the flash effect */
  @Override
  protected void setFlashOn(boolean flashOn) {
    if (flashOn)
      getWidget().addStyleDependentName(flashOnStyle);
    else
      getWidget().removeStyleDependentName(flashOnStyle);
  }

}
