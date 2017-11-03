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

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.util.geometry.Alignment;
import solutions.trsoftware.commons.client.util.geometry.RelativePosition;

/**
 * Convenience class to save code when the popup already exists at construction time and that same instance is always
 * the one to show (cleaner than having to create an anonymous inner class).
 *
 * @author Alex, 2/17/2016
 */
public class ConcretePopupOpener<W extends Widget, P extends EnhancedPopup> extends PopupOpener<W, P> {

  // TODO: get rid of this class now that we have the following capability:
  {
    setReusePopup(true);
  }

  /**
   * Same as {@link PopupOpener#PopupOpener(Widget, int, RelativePosition)}.
   *
   * @param popup The singleton instance to always show.
   */
  public ConcretePopupOpener(W opener, P popup, int eventBits, RelativePosition position) {
    super(opener, eventBits, position);
    this.popup = popup;
  }

  /**
   * Same as {@link PopupOpener#PopupOpener(Widget, int, Alignment...)}.
   *
   * @param popup The singleton instance to always show.
   */
  public ConcretePopupOpener(W opener, P popup, int eventBits, Alignment... alignmentPrefs) {
    super(opener, eventBits, alignmentPrefs);
    this.popup = popup;
  }

  @Override
  protected P createPopup() {
    throw new IllegalStateException("ConcretePopupOpener.createPopup should never be called.");  // because popup != null
  }
}
