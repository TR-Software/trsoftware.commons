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

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Alex, 9/19/2017
 */
public class ActionColumn<T> extends Column<T, T> {

  public ActionColumn(String label, ActionCell.Delegate<T> delegate) {
    super(new ActionCell<T>(label, delegate));
  }

  public ActionColumn(Cell<T> cell) {
    super(cell);
  }

  @Override
  public T getValue(T object) {
    return object;
  }
}
