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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Can be implemented by a composite widget to delegate all {@link Focusable} methods to a child widget,
 * as specified by {@link #getFocusTarget()}.
 *
 * @author Alex
 * @since 11/17/2017
 */
public interface HasFocusTarget extends IsWidget, Focusable {
  /**
   * @return the child widget that should be focused when this widget is displayed.
   */
  Focusable getFocusTarget();

  // delegate all Focusable methods to getFocusTarget():

  @Override
  default int getTabIndex() {
    return getFocusTarget().getTabIndex();
  }

  @Override
  default void setAccessKey(char key) {
    getFocusTarget().setAccessKey(key);
  }

  @Override
  default void setFocus(boolean focused) {
    getFocusTarget().setFocus(focused);
  }

  @Override
  default void setTabIndex(int index) {
    getFocusTarget().setTabIndex(index);
  }
}
