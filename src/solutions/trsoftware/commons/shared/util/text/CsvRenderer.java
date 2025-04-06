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

package solutions.trsoftware.commons.shared.util.text;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;

import java.util.Iterator;
import java.util.List;

/**
 * Renders a list of values as a comma-separated string. Renders {@code null} and empty lists as {@code ""}.
 *
 * @author Alex
 * @since 12/2/2017
 */
public class CsvRenderer<T> extends AbstractRenderer<List<T>> {

  private Renderer<T> valueRenderer;

  public CsvRenderer(Renderer<T> valueRenderer) {
    this.valueRenderer = valueRenderer;
  }

  public CsvRenderer() {
    this(StringRenderer.getInstance());
  }

  @Override
  public String render(List<T> values) {
    // TODO(8/6/2024): implement quoting (see solutions.trsoftware.commons.server.io.csv.CSVWriter.writeNextElement)
    if (values == null || values.isEmpty())
      return "";
    StringBuilder out = new StringBuilder();
    for (Iterator<T> it = values.iterator(); it.hasNext(); ) {
      T value = it.next();
      out.append(valueRenderer.render(value));
      if (it.hasNext())
        out.append(',');
    }
    return out.toString();
  }
}
