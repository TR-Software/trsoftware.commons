/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 *
 * @deprecated use the newer {@link CommonsImages} bundle to avoid downloading a composite image of all the sprites
 * defined in this interface.
 *
 * @since Dec 18, 2007
 * @author Alex
 */
public interface IconsBundle extends ImageBundle {

//  TODO: move all of the images not used by Commons back to TypeRacer (and replace with ClientBundle)

//  AbstractImagePrototype user();
  AbstractImagePrototype user32();
  AbstractImagePrototype user24();
  AbstractImagePrototype user_group();
  AbstractImagePrototype user_group_car();
//  AbstractImagePrototype red_car();
//  AbstractImagePrototype user_chat();
  AbstractImagePrototype user_chat32();
//  AbstractImagePrototype user_chat24();
  AbstractImagePrototype mail24();
//  AbstractImagePrototype user_arrow();
  AbstractImagePrototype user_arrow24();
  AbstractImagePrototype user_plus24();
  AbstractImagePrototype user_edit24();
  AbstractImagePrototype info24();
  AbstractImagePrototype clipboard24();
  AbstractImagePrototype warn24();
  AbstractImagePrototype help24();
//  /** Original: http://commons.wikimedia.org/wiki/File:Pin.JPG */
//  AbstractImagePrototype pin16();
  /** Original: http://commons.wikimedia.org/wiki/File:Pin.JPG */
  AbstractImagePrototype pin24();
//  /** Original: http://commons.wikimedia.org/wiki/File:Pin.JPG */
//  AbstractImagePrototype pin32();
  /** Source: http://commons.wikimedia.org/wiki/File:Flag_icon_red_4.svg */
  AbstractImagePrototype red_flag24();
  AbstractImagePrototype globe24();
  AbstractImagePrototype excel_file16();
  /** Source: http://code.mayanportal.org/browser/trunk/media/templates/default/img/gnome-icon-theme-2.20.0/24x24/devices/media-floppy.png?rev=29 */
  AbstractImagePrototype disk24();
  /** Source: http://commons.wikimedia.org/wiki/File:User-invisible.svg */
  AbstractImagePrototype ghost24();
  AbstractImagePrototype triangleDown();
  AbstractImagePrototype triangleLeft();
  AbstractImagePrototype triangleRight();


  class Instance {
    private static final IconsBundle instance = GWT.create(IconsBundle.class);

    public static IconsBundle get() {
      return instance;
    }
  }

}
