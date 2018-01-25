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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;

/**
 * Encapsulates an {@link ImageButton} and an {@link Anchor}, both using the same {@link ClickHandler}.
 *
 * @author Alex
 */
public class ImageButtonWithText extends Composite implements HasClickHandlers {
  private ImageButton imgButton;
  private Anchor link;

  private ImageButtonWithText(AbstractImagePrototype img, boolean imgFirst, String linkText, boolean lnkTextAsHtml) {
    imgButton = new ImageButton(img);
    link = new Anchor(linkText, lnkTextAsHtml);
    FlowPanel panel = new FlowPanel();
    if (imgFirst) {
      panel.add(imgButton);
      panel.add(link);
    } else {
      panel.add(link);
      panel.add(imgButton);
    }
    initWidget(panel);
    setStyleName(CommonsClientBundleFactory.INSTANCE.getCommonsCss().ImageButtonWithText());
  }

  /** Simplified constructor, for convenience.  The image will be left of text. */
  public ImageButtonWithText(AbstractImagePrototype img, String text, boolean textAsHtml) {
    this(img, true, text, textAsHtml);
  }


  /** Simplified constructor, for convenience.  The image will be left of text. */
  public ImageButtonWithText(AbstractImagePrototype img, String text) {
    this(img, text, false);
  }

  /** Simplified constructor, for convenience. The image will be to the right of text. */
  public ImageButtonWithText(String text, AbstractImagePrototype img, boolean textAsHtml) {
    this(img, false, text, textAsHtml);
  }

  /** Simplified constructor, for convenience. The image will be to the right of text. */
  public ImageButtonWithText(String text, AbstractImagePrototype img) {
    this(text, img, false);
  }

  /**
   * Adds the given handler to both the {@link ImageButton} and the {@link Anchor}
   */
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return new MultiHandlerRegistration(
        imgButton.addClickHandler(handler),
        link.addClickHandler(handler));
  }

  /**
   * Allows adding the click handler using method chaining after the constructor.  This allows creating the widget
   * with a single expression, when the {@link HandlerRegistration} returned by {@link #addClickHandler(ClickHandler)}
   * is not needed.
   */
  public ImageButtonWithText withClickHandler(ClickHandler handler) {
    addClickHandler(handler);
    return this;
  }

  /**
   * Allows setting the title using method chaining after the constructor.
   * @return {@code this}, for method chaining
   * @see #setTitle(String)
   */
  public ImageButtonWithText withTitle(String title) {
    setTitle(title);
    return this;
  }


}