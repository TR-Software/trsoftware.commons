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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;

/**
 * A button that encapsulates an on/off state and changes its image accordingly.
 * Mirrors the functionality of {@link ToggleAnchor}
 *
 * This is a much simpler implementation than {@link com.google.gwt.user.client.ui.ToggleButton}, which is a focus
 * widget and a form element. Our implementation is just an image with a click handler.
 *
 * @author Alex, 9/17/2014
 */
public class ImageToggleButton extends Composite implements ClickHandler {

  private ImageButton btn;
  /** The current state of the toggle */
  protected boolean on;

  private AbstractImagePrototype offImg;
  private AbstractImagePrototype onImg;

  private String offTitle;
  private String onTitle;

  public ImageToggleButton(AbstractImagePrototype offImg, String offTitle, AbstractImagePrototype onImg, String onTitle, boolean startOn) {
    this.offImg = offImg;
    this.onImg = onImg;
    this.offTitle = offTitle;
    this.onTitle = onTitle;
    initWidget(btn = new ImageButton(offImg, this));
    toggle(startOn);
  }

  /** Sets the toggle {@link #on} state to the given value */
  public void toggle(boolean toggleOn) {
    on = toggleOn;
    (on ? onImg : offImg).applyTo(btn.getImage());
    setTitle(on ? onTitle : offTitle);
  }

  /** Flips the toggle state */
  public final void toggle() {
    toggle(!on);
  }

  @Override
  public void onClick(ClickEvent event) {
    toggle();
  }

  /**
   * @return the current state of the toggle
   */
  public boolean isOn() {
    return on;
  }

}
