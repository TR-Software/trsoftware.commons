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
