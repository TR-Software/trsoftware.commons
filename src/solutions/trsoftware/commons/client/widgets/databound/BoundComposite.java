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

package solutions.trsoftware.commons.client.widgets.databound;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.shared.util.TakesValue;

/**
 * A mix-in HasValue version of Composite.
 *
 * @author Alex
 */
public abstract class BoundComposite<W extends Widget, V> extends Composite implements TakesValue<V> {

  protected W widget;
  private V value;

  public BoundComposite(W widget) {
    this.widget = widget;
    initWidget(widget);
  }

  public final V getValue() {
    return value;
  }

  public final void setValue(V value) {
    this.value = value;
    customRender(value);
  }

  /**
   * Must be overridden by subclasses to provide rendering of the value within
   * the widget.
   */
  protected abstract void customRender(V value);
}