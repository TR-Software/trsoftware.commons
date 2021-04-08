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

package solutions.trsoftware.tools.swing;

import solutions.trsoftware.commons.shared.util.NumberRange;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.function.Supplier;

/**
 * Convenient subclass of {@link JFormattedTextField} for {@link Integer} values.
 *
 * @author Alex
 * @since 1/10/2020
 */
public class IntegerInputField extends JFormattedTextField {

  private Supplier<NumberRange<Integer>> validRangeSupplier;

  private static final JFormattedTextField.AbstractFormatterFactory NUMBER_FF = new JFormattedTextField.AbstractFormatterFactory() {
    @Override
    public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
      NumberFormat numberFormat = NumberFormat.getNumberInstance();
      numberFormat.setMaximumFractionDigits(0);
      numberFormat.setGroupingUsed(false);
      return new NumberFormatter(numberFormat);
    }
  };


  public IntegerInputField() {
    super(NUMBER_FF);
  }

  public IntegerInputField(int initialValue) {
    super(NUMBER_FF, initialValue);
  }

  public Supplier<NumberRange<Integer>> getValidRangeSupplier() {
    return validRangeSupplier;
  }

  public IntegerInputField setValidRangeSupplier(Supplier<NumberRange<Integer>> validRangeSupplier) {
    this.validRangeSupplier = validRangeSupplier;
    return this;
  }

  public IntegerInputField setValidRange(NumberRange<Integer> validRange) {
    /*
      TODO: create an InputVerifier that checks that the input value is in this range
      (see https://stackoverflow.com/a/2749554)
     */
    this.validRangeSupplier = () -> validRange;
    return this;
  }

  public static AbstractFormatterFactory getNumberFf() {
    return NUMBER_FF;
  }

  /**
   * {@inheritDoc}
   * @param value {@inheritDoc} (should be an instance of {@link Integer})
   * @see #setIntValue(int)
   */
  @Override
  public void setValue(Object value) {
    if (!(value instanceof Integer))
      throw new IllegalArgumentException("value should be an Integer");
    super.setValue(value);
  }

  public void setIntValue(int value) {
    setValue(value);
  }

  @Override
  public Integer getValue() {
    // ensure the returned value is an Integer (JFormattedTextField might return a Long, which can't be cast to Integer)
    Number value = (Number)super.getValue();
    if (value != null && !(value instanceof Integer))
      return value.intValue();
    return (Integer)value;
  }

  /**
   * Attaches a {@link KeyListener} that increments the current value in response to the {@linkplain KeyEvent#VK_UP "up arrow" key} and decrements it in response to the {@linkplain KeyEvent#VK_DOWN "down arrow" key}.
   *
   * If a {@linkplain #setValidRangeSupplier(Supplier)} valid range supplier was provided}, the new value will be
   * {@linkplain NumberRange#coerce(Number)} restricted} to that range.
   *
   * @param amount the value will be incremented or decremented by this amount
   * @return self, for call chaining
   */
  public IntegerInputField incrementValueWithArrowKeys(final int amount) {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        int incr = 0;
        switch (e.getKeyCode()) {
          case KeyEvent.VK_UP:
            incr = amount;
            break;
          case KeyEvent.VK_DOWN:
            incr = -amount;
            break;
        }
        if (incr != 0) {
          Integer fieldValue = getValue();
          if (fieldValue != null) {
            int newValue = fieldValue + incr;
            if (validRangeSupplier != null) {
              NumberRange<Integer> validRange = validRangeSupplier.get();
              newValue = validRange.coerce(newValue);
            }
            int finalNewValue = newValue;  // must be "effectively final" to use inside lambda
            SwingUtilities.invokeLater(() -> {
              setIntValue(finalNewValue);
              // changing the value of a JFormattedTextField clears the selection and resets the caret position
              // so we're doing a selectAll after applying this change
              selectAll();
            });
          }
        }
      }
    });
    return this;
  }

  /**
   * Attaches a {@link FocusListener} that calls {@link JTextComponent#selectAll()} on the given
   * component when it {@linkplain FocusListener#focusGained(FocusEvent) gains focus}.
   *
   * @return self, for call chaining
   */
  public IntegerInputField selectAllWhenFocused() {
    TextComponentUtils.selectAllWhenFocused(this);
    return this;
  }
}
