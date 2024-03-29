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

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import solutions.trsoftware.commons.client.widgets.LoadingMessage;

/**
 * @author Alex, 10/18/2017
 */
public class PleaseWaitPopup extends PopupDialog {
  public PleaseWaitPopup(String message, AbstractImagePrototype icon) {
    super(true, icon, "Please Wait", null,
        new LoadingMessage(message + "...", LoadingMessage.SpinnerPosition.RIGHT),
        null);
  }

  @Override
  public PopupBuilder<PleaseWaitPopup> configure() {
    return new PopupBuilder<PleaseWaitPopup>(this);
  }
}
