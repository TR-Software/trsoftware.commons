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

import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.client.widgets.input.EnumValueListBox;
import solutions.trsoftware.commons.shared.util.ColorHSL;

/**
 * Gradually changes one of the HSL components of an element's {@code background-color} style between 2 values at a given rate.
 *
 * @author Alex
 * @see <a href="http://hslpicker.com">Online HSL color picker</a> to tune the settings
 */
public class OscillatingBackgroundColorAnimation extends OscillatingAnimation {

  private final Element element;
  private ColorHSL.Component component;
  private ColorHSL color;


  public OscillatingBackgroundColorAnimation(Element element, ColorHSL baseColor, ColorHSL.Component component, double minValue, double maxValue, double wavelength) {
    super(minValue, maxValue, wavelength);
    this.element = element;
    this.component = component;
    color = baseColor.copy();
  }

  @Override
  protected void setValue(double value) {
    color.set(component, value);
    element.getStyle().setBackgroundColor(color.toString());
  }

  /**
   * A scaffolding widget that can be used to test this animation with various parameters.
   */
  public static class Tester extends OscillatingAnimation.Tester<OscillatingBackgroundColorAnimation> {
    final ColorHSL baseColor = new ColorHSL(40, .80, .80);
    final ColorHSL.InputBox colorInput = new ColorHSL.InputBox();
    final EnumValueListBox<ColorHSL.Component> componentInput = new EnumValueListBox<ColorHSL.Component>(ColorHSL.Component.class);

    public Tester() {
      colorInput.setValue(baseColor);
      componentInput.setValue(ColorHSL.Component.LIGHTNESS);
      pnlControls.insert(registerInputWidget(colorInput, "Color"), 0);
      pnlControls.insert(registerInputWidget(componentInput, "Var"), 1);
      updateBackgroundColor();
    }

    private void updateBackgroundColor() {
      targetElement.getStyle().setBackgroundColor(colorInput.getValue().toString());
    }

    @Override
    protected OscillatingBackgroundColorAnimation createAnimation() {
      updateBackgroundColor();
      return new OscillatingBackgroundColorAnimation(targetElement, colorInput.getValue(), componentInput.getValue(), getMinValue(), getMaxValue(), getWavelength());
    }
  }

}