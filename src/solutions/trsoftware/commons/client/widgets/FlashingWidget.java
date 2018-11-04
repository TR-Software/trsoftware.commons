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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;


/**
 * Can be used to decorate any widget with a flashing effect.  This class accomplishes that by repeatedly
 * adding and removing a given suffix to the widget's primary style name, but other subclasses of
 * {@link solutions.trsoftware.commons.client.widgets.AbstractFlashingWidget} subclasses may use a different strategy
 * in their {@link #setFlashOn(boolean)} method implementation.
 *
 * @author Alex
 */
public class FlashingWidget<T extends Widget> extends AbstractFlashingWidget<T> {

  private String flashOnStyle = CommonsClientBundleFactory.INSTANCE.getCss().flashOn();

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
