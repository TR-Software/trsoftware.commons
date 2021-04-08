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

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Alex
 * @since 1/11/2020
 */
public class TextComponentUtils {

  /**
   * Attaches a {@link FocusListener} that calls {@link JTextComponent#selectAll()} on the given
   * component when it {@linkplain FocusListener#focusGained(FocusEvent) gains focus}.
   *
   * @return the given component, for call chaining
   */
  public static JTextComponent selectAllWhenFocused(JTextComponent field) {
    field.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if (!e.isTemporary()) {
//          field.selectAll();
          SwingUtilities.invokeLater(field::selectAll);
        }
      }
    });
    return field;
  }
}
