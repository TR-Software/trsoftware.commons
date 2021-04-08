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

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.view.client.ProvidesKey;

/**
 * Exposes the underlying {@link ListBox} and its methods inherited from {@link Focusable}
 * @author Alex
 * @since 12/2/2017
 */
public class FocusableValueListBox<T> extends ValueListBox<T> implements Focusable {

  public FocusableValueListBox(Renderer<T> renderer) {
    super(renderer);
  }

  public FocusableValueListBox(Renderer<T> renderer, ProvidesKey<T> keyProvider) {
    super(renderer, keyProvider);
  }

  public ListBox getListBox() {
    return (ListBox)getWidget();
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return getListBox().addBlurHandler(handler);
  }

  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return getListBox().addFocusHandler(handler);
  }

  @Override
  public int getTabIndex() {
    return getListBox().getTabIndex();
  }

  @Override
  public void setAccessKey(char key) {
    getListBox().setAccessKey(key);
  }

  @Override
  public void setFocus(boolean focused) {
    getListBox().setFocus(focused);
  }

  @Override
  public void setTabIndex(int index) {
    getListBox().setTabIndex(index);
  }

}
