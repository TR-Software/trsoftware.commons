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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a {@code public} {@link #isInitialized()} method because {@link Composite#getWidget()} has {@code protected} access.
 * @author Alex
 * @since 3/6/2018
 */
public class SmartComposite extends Composite {

  /**
   * @return {@code true} iff this {@link Composite} has a widget (i.e. {@link #initWidget(Widget)} has already been called)
   */
  public boolean isInitialized() {
    return super.getWidget() != null;
  }
}
