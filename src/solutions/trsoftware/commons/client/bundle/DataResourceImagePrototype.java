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

package solutions.trsoftware.commons.client.bundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;

/**
 * Adapter that allows a {@link DataResource} to be used as an {@link AbstractImagePrototype}.
 * A typical use-case would be for for SVG images in a {@link ClientBundle}.
 *
 * @see SvgImageResource
 * @see SvgImageResourcePrototype
 *
 * @author Alex
 * @since 11/2/2021
 */
public class DataResourceImagePrototype extends AbstractImagePrototype {

  private final SafeUri safeUri;

  public DataResourceImagePrototype(SafeUri safeUri) {
    this.safeUri = safeUri;
  }

  public DataResourceImagePrototype(DataResource dataResource) {
    this(dataResource.getSafeUri());
  }

  public SafeUri getSafeUri() {
    return safeUri;
  }

  @Override
  public void applyTo(Image image) {
    image.setUrl(safeUri);
  }

  @Override
  public Image createImage() {
    return new Image(safeUri);
  }

  @Override
  public ImagePrototypeElement createElement() {
    ImageElement img = Document.get().createImageElement();
    img.setSrc(safeUri.asString());
    return img.cast();
  }

  @Override
  public SafeHtml getSafeHtml() {
    return getImgTemplate().image(safeUri);
  }

  private static HtmlTemplate imgTemplate;

  private static HtmlTemplate getImgTemplate() {
    if (imgTemplate == null)
      imgTemplate = GWT.create(HtmlTemplate.class);
    return imgTemplate;
  }

  interface HtmlTemplate extends SafeHtmlTemplates {
    @Template("<img src='{0}'/>")
    SafeHtml image(SafeUri safeUri);
  }

}
