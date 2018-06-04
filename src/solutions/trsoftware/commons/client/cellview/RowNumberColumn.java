/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;

/**
 * Renders the index (starting with 1) for a row in a {@link CellTable}.
 *
 * Borrowed from <a href="https://stackoverflow.com/questions/4347224/adding-a-row-number-column-to-gwt-celltable">StackOverflow</a>
 *
 * @author Alex, 10/16/2017
 */
public class RowNumberColumn<T> extends Column<T, Number> {

  public RowNumberColumn() {
    super(new NumberCell() {
      @Override
      public void render(Context context, Number value, SafeHtmlBuilder sb) {
        super.render(context, context.getIndex()+1, sb);
      }
    });
  }

  @Override
  public Number getValue(T object) {
    return null;
  }
}
