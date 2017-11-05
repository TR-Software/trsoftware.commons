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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import solutions.trsoftware.commons.shared.util.LogicUtils;

import javax.annotation.Nonnull;

/**
 * A widget whose content is based on a single property of a {@link T} object.
 * @param <T> the type of the source object whose property is to be displayed
 * @param <V> the value type of the property
 */
public abstract class PropertyView<T, V> extends Composite {
  /** The value of the property currently being displayed */
  private V value;

  /**
   * @return The value of the property to be displayed by this widget for the given source object instance
   */
  protected abstract V getValue(@Nonnull T source);

  /**
   * Should update the view to display the given new property value.  This method will be invoked when the property
   * value changes.
   */
  protected abstract void updateView(V newValue);

  /** To be invoked externally whenever the property might have changed */
  public final void maybeUpdate(T source) {
    if (source != null) {
      V newValue = getValue(source);
      if (!LogicUtils.eq(value, newValue)) {
        updateView(newValue);
        value = newValue;
      }
    }
  }
}
