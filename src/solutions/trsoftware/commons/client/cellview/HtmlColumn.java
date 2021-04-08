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

package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Alex, 9/19/2017
 */
public class HtmlColumn<T> extends Column<T, T> {

  public HtmlColumn(final SafeHtmlRenderer<T> renderer) {
    super(new AbstractSafeHtmlCell<T>(renderer) {
      @Override
      protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        // NOTE: it's unclear why AbstractSafeHtmlCell doesn't provide a default implementation of this method
        if (data != null) {
          sb.append(data);
        }
      }
    });
  }

  @Override
  public T getValue(T object) {
    return object;
  }

}
