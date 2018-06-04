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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Image;
import solutions.trsoftware.commons.client.images.CommonsImages;

/**
 * Date: Dec 18, 2007
 * Time: 9:37:13 PM
 *
 * @author Alex
 */
public class LoadingImage extends Image {

  public LoadingImage() {
    super(CommonsImages.INSTANCE.loading_circle());
  }

}