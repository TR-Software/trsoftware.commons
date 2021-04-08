/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

/**
 * Gradually fades the opacity of the given widget between the given start and end values.
 *
 * @author Alex
 */
public class OpacityTweenAnimation extends Animation {

  private final Widget widget;
  private final double startOpacity;
  private final double endOpacity;
  private final double opacityDelta;

  // TODO: the superclass (Animation) should have a getter for this field instead of us re-creating it here (perhaps it does in GWT 2.0 - check)
  private boolean started;

  public OpacityTweenAnimation(Widget widget, double startOpacity, double endOpacity) {
    this.widget = widget;
    this.startOpacity = startOpacity;
    this.endOpacity = endOpacity;
    opacityDelta = endOpacity - startOpacity;
  }

  /** Called immediately before the animation starts. */
  @Override
  protected void onStart() {
    super.onStart();
    setOpacity(startOpacity);
    started = true;
  }

  protected void onUpdate(double progress) {
    setOpacity(startOpacity + progress*opacityDelta);
  }

  /** Overriding this method in case animation terminates abnormally */
  @Override
  protected void onComplete() {
    super.onComplete();
    setOpacity(endOpacity);
  }

  private void setOpacity(double newOpacity) {
    widget.getElement().getStyle().setOpacity(newOpacity);
  }

  public boolean isStarted() {
    return started;
  }
}