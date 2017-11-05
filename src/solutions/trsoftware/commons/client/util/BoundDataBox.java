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

  /** These display widgets will be updated whenever the boxed value changes */
  private List<TakesValue<V>> boundWidgets = new ArrayList<TakesValue<V>>();

  public BoundDataBox() {
  }

  public BoundDataBox(V value) {
    super(value);
  }

  @Override
  public void setValue(V value) {
    super.setValue(value);
    for (TakesValue<V> boundWidget : boundWidgets) {
      boundWidget.setValue(value);
    }
  }

  public void addBoundDisplayWidget(TakesValue<V> displayWidget) {
    boundWidgets.add(displayWidget);
    displayWidget.setValue(getValue());
  }

}
