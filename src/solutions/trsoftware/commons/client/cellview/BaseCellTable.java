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

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.NoSelectionModel;

/**
 * @author Alex, 9/19/2017
 */
public class BaseCellTable<T> extends CellTable<T> {

  protected void addColumn(String heading, Column<T, ?> col) {
    addColumn(col, heading);
  }

  protected void addColumn(Header<?> header, Column<T, ?> col) {
    addColumn(col, header);
  }

  protected void preventSelection() {
    setSelectionModel(new NoSelectionModel<T>());
    setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
  }
}
