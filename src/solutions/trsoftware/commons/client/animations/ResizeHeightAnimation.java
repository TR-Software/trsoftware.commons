package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;

/**
 * Gradually fades the color of the given component between the given start and end
 * color values.
 *
 * @author Alex
 */
public class ResizeHeightAnimation extends Animation {  // TODO: extract a general superclass, ResizeAnimation, also read initial size directly from the element

  private final Widget widget;
  private int endSize;
  private int sizeDelta;


  public ResizeHeightAnimation(Widget widget, int startSize, int endSize) {
    this.widget = widget;
    this.endSize = endSize;
    this.sizeDelta = endSize - startSize;
  }


  protected void onUpdate(double progress) {
    double newValue = progress * sizeDelta;
    setSize(newValue);
  }

  protected void setSize(double newValue) {
    widget.getElement().getStyle().setHeight(newValue, Style.Unit.PX);
  }

  /** Overriding this method in case animation terminates abnormally */
  @Override
  protected void onComplete() {
    super.onComplete();
    setSize(endSize);
  }

}
