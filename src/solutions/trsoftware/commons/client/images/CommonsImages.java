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

package solutions.trsoftware.commons.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import solutions.trsoftware.commons.client.bundle.SvgImageResource;

/**
 * Provides icons used by this module.
 *
 * @author Alex
 * @since 12/29/2017
 */
public interface CommonsImages extends ClientBundle {

  CommonsImages INSTANCE = GWT.create(CommonsImages.class);

  /*
    NOTE: don't rely on GWT's ClientBundle generator to scale the images
    (e.g. with @ImageResource.ImageOptions(width = 22, height = 30))
    because the result (generated using Java's ImageIO) will probably look much worse than what you can get using a
    real image editor (like GIMP)
   */

  ImageResource user24();
  ImageResource user_arrow24();
  ImageResource user_edit24();
  ImageResource info24();
  ImageResource clipboard24();
  ImageResource warn24();
  ImageResource loading_circle();
  ImageResource mail24();

  @Source("reloadIconSingleArrow.svg")  // https://commons.wikimedia.org/wiki/File:OOjs_UI_icon_reload-progressive.svg
  SvgImageResource reloadIconSingleArrow();
}
