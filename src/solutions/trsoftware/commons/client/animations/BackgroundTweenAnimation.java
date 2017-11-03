package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.util.Color;

/**
 * Gradually fades the color of the given component between the given start and end
 * color values.
 *
 * @author Alex
 */
public class BackgroundTweenAnimation extends Animation {

  private final Widget widget;

  private final Color endColor;

  private final int rDiff, gDiff, bDiff;

  // the components of the starting color
  private final int r;
  private final int g;
  private final int b;

  public BackgroundTweenAnimation(Widget widget, Color startColor, Color endColor) {
    this.widget = widget;
    this.endColor = endColor;

    r = startColor.r;
    g = startColor.g;
    b = startColor.b;

    rDiff = endColor.r - r;
    gDiff = endColor.g - g;
    bDiff = endColor.b - b;
  }


  protected void onUpdate(double progress) {
    Color newColor = new Color(
        r + (int)(progress * rDiff),
        g + (int)(progress * gDiff),
        b + (int)(progress * bDiff));
    setBackgroundColor(newColor);
  }

  /** Overriding this method in case animation terminates abnormally */
  @Override
  protected void onComplete() {
    super.onComplete();
    setBackgroundColor(endColor);
  }

  private void setBackgroundColor(Color newColor) {
    DOM.setStyleAttribute(widget.getElement(), "backgroundColor", newColor.toString());  // dashes are replaced with camelCase in DOM style manipulation (e.g. background-color becomes backgroundColor)
  }

}
