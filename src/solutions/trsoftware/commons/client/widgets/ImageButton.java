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
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.bundle.CommonsCss;

/**
 * Encapsulates an {@link Image} with a {@link ClickHandler}.  Uses the style defined by {@link CommonsCss#ImageButton()}
 * to allow adding a {@code :hover} effect only when an image logically represents a button.
 *
 * @author Alex
 */
public class ImageButton extends Composite implements HasClickHandlers {

  private Image img;

  public ImageButton(AbstractImagePrototype img) {
    this(img.createImage());
  }

  public ImageButton(ImageResource img) {
    this(AbstractImagePrototype.create(img));
  }

  public ImageButton(AbstractImagePrototype img, ClickHandler clickHandler) {
    this(img.createImage(), clickHandler);
  }

  public ImageButton(ImageResource img, ClickHandler clickHandler) {
    this(AbstractImagePrototype.create(img), clickHandler);
  }

  public ImageButton(AbstractImagePrototype img, String title, ClickHandler clickHandler) {
    this(img, clickHandler);
    setTitle(title);
  }

  public ImageButton(ImageResource img, String title, ClickHandler clickHandler) {
    this(AbstractImagePrototype.create(img), title, clickHandler);
  }

  public ImageButton(AbstractImagePrototype img, String title) {
    this(img, title, null);
  }

  public ImageButton(ImageResource img, String title) {
    this(AbstractImagePrototype.create(img), title);
  }

  public ImageButton(Image img, ClickHandler clickHandler) {
    this(img);
    if (clickHandler != null)
      addClickHandler(clickHandler);
  }

  public ImageButton(Image img) {
    initWidget(this.img = img);
    
    /*// must explicitly set the size on the widget in order for its opacity style to work in IE6
    // hence we propagate whatever CSS width/height properties are present on the child image
    String imageStyleWidth = DOM.getStyleAttribute(img.getElement(), "width");
    String imageStyleHeight = DOM.getStyleAttribute(img.getElement(), "height");
    if (StringUtils.notBlank(imageStyleWidth) && StringUtils.notBlank(imageStyleHeight)) {
      setWidth(imageStyleWidth);
      setHeight(imageStyleHeight);
    }*/

    setStyleName(CommonsClientBundleFactory.INSTANCE.getCss().ImageButton());
  }

  public Image getImage() {
    return img;
  }

  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return img.addClickHandler(handler);
  }
}
