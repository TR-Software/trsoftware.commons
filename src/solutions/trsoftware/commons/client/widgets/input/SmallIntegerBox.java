/*
 * Copyright 2024 TR Software Inc.
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
package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.dom.client.Document;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.client.IntegerParser;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ValueBox;
import solutions.trsoftware.commons.shared.util.text.StringRenderer;

/**
 * Functionally identical to {@link IntegerBox}, but uses a simple {@link Integer#toString()} renderer
 * instead of {@link NumberFormat}, in order to avoid digit grouping or any other unnecessary chars.
 */
public class SmallIntegerBox extends ValueBox<Integer> {
  public SmallIntegerBox() {
    super(Document.get().createTextInputElement(), StringRenderer.getInstance(),
        IntegerParser.instance());
  }
}
