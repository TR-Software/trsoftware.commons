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

import com.google.gwt.user.client.ui.PasswordTextBox;
import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * Jan 18, 2010
 *
 * @author Alex
 */
public class DirtyPasswordTextBox extends PasswordTextBox implements DirtyInput {

  private final String initialValue;

  public DirtyPasswordTextBox(Integer visibleChars, Integer maxChars, String initialValue) {
    this(initialValue);
    if (visibleChars != null)
      setVisibleLength(visibleChars);
    if (maxChars != null)
      setMaxLength(maxChars);
  }

  /** Creates an empty password text box. */
  public DirtyPasswordTextBox(String initialValue) {
    if (!StringUtils.isBlank(initialValue)) {
      this.initialValue = initialValue;
      setText(initialValue);
    }
    else {
      this.initialValue = getText();
    }
  }

  public boolean isDirty() {
    return !getText().equals(initialValue);
  }

}
