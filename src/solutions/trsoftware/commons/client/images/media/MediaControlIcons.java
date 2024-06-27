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

package solutions.trsoftware.commons.client.images.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import solutions.trsoftware.commons.client.bundle.SvgImageResource;

/**
 * @author Alex, 9/17/2014
 */
public interface MediaControlIcons extends ClientBundle {
  
  MediaControlIcons INSTANCE = GWT.create(MediaControlIcons.class);

  static MediaControlIcons get() {
    return INSTANCE;
  }

  // PNGs copied from solutions.trsoftware.typinglog.client.replay.icons.PlayerIconsBundle:

  ImageResource play();
  ImageResource pause();

  ImageResource step_back();
  ImageResource step_forward();
  
  ImageResource ff();
  ImageResource ff_full();
  
  ImageResource rew();
  ImageResource rew_full();

  // SVG images taken from the Google Icons font (https://fonts.google.com/icons):
  
  @Source("play.svg")
  SvgImageResource play_svg();
  @Source("pause.svg")
  SvgImageResource pause_svg();

  @Source("play_circle.svg")
  SvgImageResource play_circle_svg();
  @Source("pause_circle.svg")
  SvgImageResource pause_circle_svg();

  /* The following 2 images were derived from the above Google Icons
     using Inskscape to make the icon fill the entire viewbox,
     and minified using https://www.svgminify.com/
   */
  @Source("play_circle_large.svg")
  SvgImageResource play_circle_svg_large();
  @Source("pause_circle_large.svg")
  SvgImageResource pause_circle_svg_large();
}
