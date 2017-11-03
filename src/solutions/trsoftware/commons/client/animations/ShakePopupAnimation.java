/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.animations;

import com.google.gwt.user.client.ui.PopupPanel;

/**
 * This class can be used as a field of a PopupPanel subclass, and implements
 * an animation that will shake that popup.
 *
 * Date: Sep 18, 2008 Time: 5:40:41 PM
 *
 * @author Alex
 */
public class ShakePopupAnimation extends SmartAnimation {
  private int startLeft;
  private int startTop;
  private PopupPanel popup;

  public ShakePopupAnimation(PopupPanel popup) {
    this.popup = popup;
  }

  @Override
  protected void onStart() {
    startLeft = popup.getPopupLeft();
    startTop = popup.getPopupTop();
    super.onStart();
  }

  @Override
  protected void onComplete() {
    super.onComplete();
    // always set the initial position at the end of the animation because
    // it could've been cancelled in the middle
    popup.setPopupPosition(startLeft, startTop);
  }

  @Override
  protected void onUpdate(double progress) {
    // animation starts and ends at 0 oscillating twice in range +/- 3px (sin(2pi) = 0)
    int newLeft = (int)(startLeft + 3 * (float)Math.sin(4 * Math.PI * progress));
    popup.setPopupPosition(newLeft, startTop);
  }

}
