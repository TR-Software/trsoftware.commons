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

import com.google.gwt.dom.client.Element;

/**
 * Fades an element in & out at a specified rate.
 *
 * @author Alex
 */
public class OscillatingOpacityAnimation extends OscillatingAnimation {

  private final Element element;

  public OscillatingOpacityAnimation(Element element, double minOpacity, double maxOpacity, double wavelength) {
    super(minOpacity, maxOpacity, wavelength);
    this.element = element;
  }

  @Override
  protected void setValue(double value) {
    element.getStyle().setOpacity(value);
  }

  @Override
  protected void onComplete() {
    super.onComplete();
    // clear the inline opacity style set by this animation in setValue
    element.getStyle().clearOpacity();
  }

  /**
   * A scaffolding widget that can be used to test this animation with various parameters.
   */
  public static class Tester extends OscillatingAnimation.Tester<OscillatingOpacityAnimation> {

    public Tester() {
      targetElement.getStyle().setBackgroundColor("red");
    }

    @Override
    protected OscillatingOpacityAnimation createAnimation() {
      return new OscillatingOpacityAnimation(targetElement, getMinValue(), getMaxValue(), getWavelength());
    }
  }

}