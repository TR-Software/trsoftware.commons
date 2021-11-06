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

package solutions.trsoftware.commons.client.widgets.text;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueLabel;
import solutions.trsoftware.commons.shared.text.TypingSpeed;
import solutions.trsoftware.commons.shared.util.Box;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

/**
 * @author Alex, 10/30/2017
 */
public class TypingSpeedLabel extends ValueLabel<TypingSpeed> {

  /**
   * We have to encapsulate the unit in a box so that it can be accessed from {@link TypingSpeedRenderer},
   * which has to be a static class because our constructor can't call the
   * {@link ValueLabel#ValueLabel(Renderer) superclass constructor} with a non-static inner class.
   * Encapsulating the unit like this is the only way to have our {@link TypingSpeedRenderer} instance pick up changes
   * made by {@link #setUnit(TypingSpeed.Unit)}.
   */
  private Box<TypingSpeed.Unit> unitBox;

  public TypingSpeedLabel(final TypingSpeed.Unit unit, final int maxFractionalDigits) {
    this(maxFractionalDigits, new Box<TypingSpeed.Unit>(unit));
    if (unit == null)
      throw new NullPointerException();
  }

  private TypingSpeedLabel(final int maxFractionalDigits, Box<TypingSpeed.Unit> unitBox) {
    super(new TypingSpeedRenderer(maxFractionalDigits, unitBox));
    this.unitBox = unitBox;
  }

  @Override
  public void setValue(TypingSpeed value) {
    super.setValue(value);
    // display the full-precision value as "hover" text
    TypingSpeed.Unit unit = getUnit();
    setTitle("" + TypingSpeed.getDefaultFormatter().format(value.getSpeed(unit)) + " " + unit.name());
  }

  public TypingSpeed.Unit getUnit() {
    return unitBox.getValue();
  }

  public void setUnit(TypingSpeed.Unit unit) {
    if (unit == null)
      throw new NullPointerException();
    if (unitBox.getAndSet(unit) != unit) {
      // update the rendered value when changing to a different unit
      TypingSpeed value = getValue();
      // avoid NPE when the value hasn't already been set
      if (value != null)
        setValue(value);  // trigger re-rendering of the value
    }
  }

  private static class TypingSpeedRenderer extends AbstractRenderer<TypingSpeed> {
    final SharedNumberFormat numberFormat;
    private final Box<TypingSpeed.Unit> unitBox;

    public TypingSpeedRenderer(int maxFractionalDigits, Box<TypingSpeed.Unit> unitBox) {
      numberFormat = new SharedNumberFormat(maxFractionalDigits);
      this.unitBox = unitBox;
    }

    @Override
    public String render(TypingSpeed typingSpeed) {
      if (typingSpeed == null)
        return "";
      TypingSpeed.Unit unit = unitBox.getValue();
      return numberFormat.format(typingSpeed.getSpeed(unit)) + " " + unit.name();
    }
  }
}
