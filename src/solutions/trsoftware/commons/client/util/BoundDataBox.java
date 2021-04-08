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

package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.shared.util.Box;
import solutions.trsoftware.commons.shared.util.TakesValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: Nov 14, 2008 Time: 2:33:34 PM
 *
 * @author Alex
 */
public class BoundDataBox<V> extends Box<V> {

  // TODO: rewrite this using event handlers?

  /** These display widgets will be updated whenever the boxed value changes */
  private List<TakesValue<V>> boundWidgets;

  public BoundDataBox() {
  }

  public BoundDataBox(V value) {
    super(value);
  }

  @Override
  public void setValue(V value) {
    super.setValue(value);
    for (TakesValue<V> boundWidget : getBoundWidgets()) {
      boundWidget.setValue(value);
    }
  }

  public void addBoundDisplayWidget(TakesValue<V> displayWidget) {
    getBoundWidgets().add(displayWidget);
    displayWidget.setValue(getValue());
  }

  public List<TakesValue<V>> getBoundWidgets() {
    if (boundWidgets == null)
      boundWidgets = new ArrayList<TakesValue<V>>();  // lazy init
    return boundWidgets;
  }
}
