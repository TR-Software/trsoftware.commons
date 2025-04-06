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

  static CommonsImages get() {
    return INSTANCE;
  }

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
  @Source("reloadIconDoubleArrows.svg")
  SvgImageResource reloadIconDoubleArrows();
  
  // copied from IconsBundle:
  ImageResource user32();
  ImageResource user_group();
  ImageResource user_group_car();
  //  ImageResource red_car();
//  ImageResource user_chat();
  ImageResource user_chat32();
  //  ImageResource user_chat24();
  //  ImageResource user_arrow();
  ImageResource user_plus24();
  ImageResource help24();
//  /** Original: http://commons.wikimedia.org/wiki/File:Pin.JPG */
//  ImageResource pin16();
  /** Original: http://commons.wikimedia.org/wiki/File:Pin.JPG */
  ImageResource pin24();
//  /** Original: http://commons.wikimedia.org/wiki/File:Pin.JPG */
//  ImageResource pin32();
  /** Source: http://commons.wikimedia.org/wiki/File:Flag_icon_red_4.svg */
  ImageResource red_flag24();
  ImageResource globe24();
  ImageResource excel_file16();
  /** Source: http://code.mayanportal.org/browser/trunk/media/templates/default/img/gnome-icon-theme-2.20.0/24x24/devices/media-floppy.png?rev=29 */
  ImageResource disk24();
  /** Source: http://commons.wikimedia.org/wiki/File:User-invisible.svg */
  ImageResource ghost24();
  ImageResource triangleDown();
  ImageResource triangleLeft();
  ImageResource triangleRight();
  
}
