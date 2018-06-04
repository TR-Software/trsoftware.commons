/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.shared.util.ColorRGB;

/**
 * Gradually fades the color of the given component between the given start and end
 * color values.
 *
 * @author Alex
 */
public class BackgroundTweenAnimation extends Animation {

  private final Widget widget;

  private final ColorRGB endColor;

  private final int rDiff, gDiff, bDiff;

  // the components of the starting color
  private final int r;
  private final int g;
  private final int b;

  public BackgroundTweenAnimation(Widget widget, ColorRGB startColor, ColorRGB endColor) {
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
    ColorRGB newColor = new ColorRGB(
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

  private void setBackgroundColor(ColorRGB newColor) {
    DOM.setStyleAttribute(widget.getElement(), "backgroundColor", newColor.toString());  // dashes are replaced with camelCase in DOM style manipulation (e.g. background-color becomes backgroundColor)
  }

}
