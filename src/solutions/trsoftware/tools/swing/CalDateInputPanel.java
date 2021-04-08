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

import solutions.trsoftware.commons.server.util.CalDate;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;

/**
 * A panel that combines 3 integer inputs (year, month, day) to construct an instance of {@link CalDate}
 *
 * @author Alex, 1/1/14
 */
public class CalDateInputPanel extends JPanel {
  private final IntegerInputField txtYear;
  private final IntegerInputField txtMonth;
  private final IntegerInputField txtDay;

  /*
    TODO(1/10/2020): consider replacing the 3 separate text fields with a single JFormattedTextField
    that uses javax.swing.text.DateFormatter.  This would make it easier to ensure that the inputs
    represent a valid date.
   */

  public CalDateInputPanel(String promptText, CalDate init) {
    add(new JLabel(promptText));
    txtYear = addField(init.getYear(), 4, CalDate.Field.YEAR);
    txtMonth = addField(init.getMonth(), 2, CalDate.Field.MONTH);
    txtDay = addField(init.getDayOfMonth(), 2, CalDate.Field.DAY);
  }

  private IntegerInputField addField(int initialValue, int cols, CalDate.Field calDateField) {
    // TODO: add validation logic to the text field (could use field.setInputVerifier(InputVerifier))
    IntegerInputField field = new IntegerInputField(initialValue);
    field.setColumns(cols);
    // select the entire text when field is focused
    TextComponentUtils.selectAllWhenFocused(field);
    // increment or decrement the field value in response to up arrow and down arrow keys
    field.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        int incr = 0;
        switch (e.getKeyCode()) {
          case KeyEvent.VK_UP:
            incr = 1;
            break;
          case KeyEvent.VK_DOWN:
            incr = -1;
            break;
        }
        if (incr != 0) {
          Integer fieldValue = field.getValue();
          if (fieldValue != null) {
            CalDate calValue = getValue();
            Calendar cal = calValue.getCal();
            int newValue = fieldValue + incr;
            field.setIntValue(calDateField.getValueRange(calValue).coerce(newValue));
            // changing the value of a JFormattedTextField clears the selection and resets the caret position
            // so we're doing a selectAll after applying this change
            field.selectAll();
          }
        }
      }
    });
    add(field);
    return field;
  }

  public CalDate getValue() {
    return new CalDate(
        txtYear.getValue(),
        txtMonth.getValue(),
        txtDay.getValue());
  }
}
