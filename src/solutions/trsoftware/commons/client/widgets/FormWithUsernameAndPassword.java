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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.event.CapsLockDetector;

import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.html;

/**
 * Uses a {@link CapsLockDetector} to show {@link #lblCapsLockWarning} whenever the contained {@link PasswordTextBox}
 * receives a keystroke while the "Caps Lock" key is on.  Also provides a convenient {@link #passwordInputWidget}
 * that contains both {@link #txtPassword} and {@link #lblCapsLockWarning}.
 *
 * @author Alex, 10/17/2017
 */
public abstract class FormWithUsernameAndPassword extends BasicInputForm implements HasFocusTarget {

  protected TextBox txtUsername = new TextBox();
  protected PasswordTextBox txtPassword = new PasswordTextBox();
  protected Label lblCapsLockWarning = html("Your <em>Caps Lock</em> key is on", FIELD_ERROR_STYLE);
  protected final FlowPanel passwordInputWidget = flowPanel(txtPassword, lblCapsLockWarning);

  public FormWithUsernameAndPassword(Layout layout) {
    super(layout);
    // show a "Caps Lock" warning when entering password
    txtPassword.addKeyPressHandler(new CapsLockDetector() {
      @Override
      protected void onCapsLockStatus(boolean on) {
        lblCapsLockWarning.setVisible(on);
      }
    });
    lblCapsLockWarning.setVisible(false);
  }


  /**
   * @return the widget that should be focused when this form is displayed.
   */
  @Override
  public FocusWidget getFocusTarget() {
    return txtUsername;
  }

}
