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

package solutions.trsoftware.commons.client.widgets.databound;

import com.google.gwt.user.client.ui.Label;
import solutions.trsoftware.commons.shared.util.TakesValue;

/**
 * A mix-in HasValue version of Label.
 *
 * @author Alex
 */
public class BoundLabel<V> extends Label implements TakesValue<V> {

  private V value;

  public final V getValue() {
    return value;
  }

  public final void setValue(V value) {
    this.value = value;
    if (!customRender(value)) {
      setText(String.valueOf(value));
    }
  }

  /**
   * Can be overridden by subclasses to provide rendering that differs
   * from setText(value.toString).
   * @return Whether this method was overridden (custom rendering was used
   * and setText() should not be called for the value).
   */
  protected boolean customRender(V value) {
    return false;
  }
}
