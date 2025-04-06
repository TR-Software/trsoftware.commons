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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.event.Events;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;

/**
 * Encapsulates an {@link ImageButton} and an {@link Anchor}, both using the same {@link ClickHandler}.
 *
 * @author Alex
 */
public class ImageButtonWithText extends Composite implements HasClickHandlers {
  private final ImageButton imageButton;
  private final Anchor link;

  private ImageButtonWithText(AbstractImagePrototype img, boolean imgFirst, Anchor link) {
    imageButton = new ImageButton(img);
    this.link = link;
    FlowPanel panel = new FlowPanel();
    if (imgFirst) {
      panel.add(imageButton);
      panel.add(link);
    } else {
      panel.add(link);
      panel.add(imageButton);
    }
    initWidget(panel);
    setStyleName(CommonsClientBundleFactory.INSTANCE.getCss().ImageButtonWithText());
    // click the link when the button is clicked
    // TODO: make this behavior user-configurable (can be undesirable in some cases)
    imageButton.addClickHandler(event -> Events.click(link.getElement()));
  }

  private ImageButtonWithText(AbstractImagePrototype img, boolean imgFirst, String linkText, boolean lnkTextAsHtml) {
    this(img, imgFirst, new Anchor(linkText, lnkTextAsHtml));
  }

  /**
   * Combines an image button with a scripting {@link Anchor} that displays the given text to the right of the image.
   *
   * @param text the anchor text
   * @param textAsHtml {@code true} to treat the specified text as html
   *
   * @see Anchor#Anchor(String, boolean)
   */
  public ImageButtonWithText(AbstractImagePrototype img, String text, boolean textAsHtml) {
    this(img, true, new Anchor(text, textAsHtml));
  }

  /**
   * Combines an image button with a scripting {@link Anchor} that displays the given text to the right of the image.
   *
   * @param text the anchor text
   * @see Anchor#Anchor(String)
   */
  public ImageButtonWithText(AbstractImagePrototype img, String text) {
    this(img, true, new Anchor(text));
  }

  /**
   * Combines an image button with the given pre-initialized {@link Anchor} on its right side.
   *
   * @param link a new instance of {@link Anchor}
   */
  public ImageButtonWithText(AbstractImagePrototype img, Anchor link) {
    this(img, true, link);
  }

  /**
   * Combines an image button with a scripting {@link Anchor} that displays the given text to the left of the image.
   *
   * @param text the anchor text
   * @param textAsHtml {@code true} to treat the specified text as html
   *
   * @see Anchor#Anchor(String, boolean)
   */
  public ImageButtonWithText(String text, AbstractImagePrototype img, boolean textAsHtml) {
    this(img, false, text, textAsHtml);
  }

  /**
   * Combines an image button with a scripting {@link Anchor} that displays the given text to the left of the image.
   *
   * @param text the anchor text
   * @see Anchor#Anchor(String)
   */
  public ImageButtonWithText(String text, AbstractImagePrototype img) {
    this(text, img, false);
  }

  /**
   * Combines an image button with the given pre-initialized {@link Anchor} on its left side.
   *
   * @param link a new instance of an {@link Anchor}
   */
  public ImageButtonWithText(Anchor link, AbstractImagePrototype img) {
    this(img, false, link);
  }

  /**
   * Adds the given handler to both the {@link ImageButton} and the {@link Anchor}
   * @see #onClick(ClickHandler)
   */
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return new MultiHandlerRegistration(
        imageButton.addClickHandler(handler),
        link.addClickHandler(handler)
    );
  }

  /**
   * Allows adding the click handler using method chaining after the constructor.  This allows creating the widget
   * with a single expression, when the {@link HandlerRegistration} returned by {@link #addClickHandler(ClickHandler)}
   * is not needed.
   * @see #addClickHandler(ClickHandler)
   */
  public ImageButtonWithText onClick(ClickHandler handler) {
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

  public ImageButton getImageButton() {
    return imageButton;
  }

  public Anchor getLink() {
    return link;
  }
}