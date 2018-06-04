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

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;


/**
 * @author Alex, 10/23/2014
 */
public class FlashingImage extends AbstractFlashingWidget<Image> {

  private AbstractImagePrototype imgOff;
  private AbstractImagePrototype imgOn;

  public FlashingImage(AbstractImagePrototype imgOff, AbstractImagePrototype imgOn, int flashingDelay) {
    super(imgOff.createImage(), flashingDelay);
    setFaces(imgOff, imgOn);
  }

  /**
   * Use this constructor to synchronize the flashing of this widget with another flashing widget so they both
   * flash at the same time.
   */
  public FlashingImage(AbstractImagePrototype imgOff, AbstractImagePrototype imgOn, PooledFlashingTimer flashingTimer) {
    super(imgOff.createImage(), flashingTimer);
    setFaces(imgOff, imgOn);
  }

  public void setFaces(AbstractImagePrototype imgOff, AbstractImagePrototype imgOn) {
    this.imgOff = imgOff;
    this.imgOn = imgOn;
  }

  @Override
  protected void setFlashOn(boolean flashOn) {
    if (flashOn)
      imgOn.applyTo(getImage());
    else
      imgOff.applyTo(getImage());
  }

  public Image getImage() {
    return (Image)getWidget();
  }
}
