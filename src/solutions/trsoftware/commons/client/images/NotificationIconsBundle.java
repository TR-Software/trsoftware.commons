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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

import javax.annotation.Nullable;

/**
 * Icons depicting numbers between {@value MIN_VALUE} and {@value MAX_VALUE} in a little red box.
 *
 * @author Alex
 */
public interface NotificationIconsBundle extends ImageBundle {
  AbstractImagePrototype redbox16_1();
  AbstractImagePrototype redbox16_2();
  AbstractImagePrototype redbox16_3();
  AbstractImagePrototype redbox16_4();
  AbstractImagePrototype redbox16_5();

  NotificationIconsBundle INSTANCE = GWT.create(NotificationIconsBundle.class);

  /**
   * Smallest number for which this bundle contains an icon.
   */
  int MIN_VALUE = 1;
  /**
   * Greatest number for which this bundle contains an icon.
   */
  int MAX_VALUE = 5;

  /**
   * @param value an integer between {@value MIN_VALUE} and {@value MAX_VALUE}
   * @return the icon corresponding to the given ordinal number, or {@code null} if
   * this bundle doesn't contain an image corresponding to that number.
   */
  @Nullable
  static AbstractImagePrototype getIconForNumber(int value) {
    NotificationIconsBundle imageBundle = INSTANCE;
    switch (value) {
      case 1:
        return imageBundle.redbox16_1();
      case 2:
        return imageBundle.redbox16_2();
      case 3:
        return imageBundle.redbox16_3();
      case 4:
        return imageBundle.redbox16_4();
      case 5:
        return imageBundle.redbox16_5();
      default:
        return null;
    }
  }
}