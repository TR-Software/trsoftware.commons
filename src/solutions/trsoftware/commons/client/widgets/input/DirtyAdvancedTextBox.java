/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.user.client.Command;

import static solutions.trsoftware.commons.shared.util.StringUtils.isBlank;
import static solutions.trsoftware.commons.shared.util.StringUtils.notBlank;

/**
 * A text box which has an initial value, which can be edited by focusing the box.
 * getText() only returns a non-null value, however, if the box is dirty (i.e.
 * its current value is not equal to its initial value.
 *
 * The CSS class AdvancedTextBox-unfocused should be defined by the app's
 * stylesheet to specify the color to be used for the prompt text, if any.
 *
 * Jan 17, 2010
 * @author Alex
 */
public class DirtyAdvancedTextBox extends AdvancedTextBox implements DirtyInput {

  public DirtyAdvancedTextBox(Integer visibleChars, Integer maxChars, String initialText, Command onEnterKey, Command onEscapeKey, String styleName) {
    super(visibleChars, maxChars, initialText, notBlank(initialText), onEnterKey, onEscapeKey, styleName);
  }

  public DirtyAdvancedTextBox(Integer visibleChars, Integer maxChars, String initialText, String styleName) {
    this(visibleChars, maxChars, initialText, null, null, styleName);
  }

  public DirtyAdvancedTextBox(Integer visibleChars, Integer maxChars, String initialText) {
    this(visibleChars, maxChars, initialText, null);
  }

  @Override
  public void onFocus(FocusEvent event) {
    // don't call the superclass method (i.e. don't clear the text inside the box)
    removeStyleDependentName(UNFOCUSED_STYLE_DEPENDENT_NAME);
  }

  @Override
  public void onBlur(BlurEvent event) {
    if (!isDirty()) {
      addStyleDependentName(UNFOCUSED_STYLE_DEPENDENT_NAME);
    }
    // don't call the superclass method (we don't need to restore the prompt text because we never clear it in onFocus())
  }

  public boolean isDirty() {
    if (isBlank(getTextUnfiltered()))
      return notBlank(promptText);
    else
      return !getTextUnfiltered().equals(promptText);
  }
}
