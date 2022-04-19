/*
 * Copyright 2022 TR Software Inc.
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

import javax.swing.*;
import java.awt.*;

/**
 * Panel that displays a {@link JLabel} and a {@link JFormattedTextField}
 *
 * @author Alex, 1/1/14
 * @see IntegerInputField
 */
public class TextInputPanel<V, F extends JFormattedTextField> extends JPanel {
  private final F txtField;

  public TextInputPanel(String promptText, F txtField) {
    this(promptText, txtField, new FlowLayout(FlowLayout.LEFT), null, null);
  }

  public TextInputPanel(String promptText, F txtField, LayoutManager layout, Object... layoutConstraints) {
    setLayout(layout);
    add(new JLabel(promptText), layoutConstraints[0]);
    this.txtField = txtField;
    add(txtField, layoutConstraints[1]);
  }

  /**
   * Sets the "the number of columns to use to calculate the preferred width" of the text field.
   * @see JFormattedTextField#setColumns(int)
   *
   * @param columns the number of columns &gt;= 0
   * @return self, for call chaining
   */
  public TextInputPanel<V, F> setTextFieldWidth(int columns) {
    txtField.setColumns(columns);
    return this;
  }

  public F getTextField() {
    return txtField;
  }

  public V getValue() {
    return (V)txtField.getValue();
  }
}
