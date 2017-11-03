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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextArea;

/**
 * A {@link TextArea} that dynamically adjusts its size to accommodate its value.
 *
 * @author Alex, 9/22/2017
 */
public class DynamicSizeTextArea extends TextArea implements ValueChangeHandler<String> {

  private int charWidth = 20;
  private int minLines = 1;

  public DynamicSizeTextArea() {
    super();
    addValueChangeHandler(this);
  }

  public DynamicSizeTextArea(Element element) {
    super(element);
    addValueChangeHandler(this);
  }

  @Override
  public void onValueChange(ValueChangeEvent<String> event) {
    String value = event.getValue();
    int requiredLines = (int)Math.ceil((double)value.length() / charWidth);
    if (requiredLines >= minLines) {
      setVisibleLines(requiredLines);
    }
  }
}
