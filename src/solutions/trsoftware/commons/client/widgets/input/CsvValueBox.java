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

package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.dom.client.Document;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;
import solutions.trsoftware.commons.shared.util.text.CsvParser;
import solutions.trsoftware.commons.shared.util.text.CsvRenderer;

import java.util.List;

/**
 * Allows entering a list of values as a comma-separated string.  The values will be parsed using a given
 * {@link CsvParser}.
 *
 * @author Alex
 * @since 12/22/2017
 */
public class CsvValueBox<T> extends ValueBox<List<T>> {

  /**
   * This constructor may be used by subclasses to explicitly use an existing
   * element. This element must be an &lt;input&gt; element whose type is
   * 'text'.
   *
   */
  protected CsvValueBox(Renderer<T> renderer, Parser<T> parser) {
    super(Document.get().createTextInputElement(), new CsvRenderer<T>(renderer), new CsvParser<T>(parser));
  }
}
