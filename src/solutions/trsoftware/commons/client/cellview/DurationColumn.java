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

package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.user.cellview.client.TextColumn;
import solutions.trsoftware.commons.shared.util.text.DurationFormat;

/**
 * @author Alex, 9/19/2017
 */
public abstract class DurationColumn<T> extends TextColumn<T> {

  private static final DurationFormat DURATION_FORMATTER = new DurationFormat(DurationFormat.Component.MINUTES, 0);

  @Override
  public final String getValue(T object) {
    return DURATION_FORMATTER.format(getDuration(object));
  }

  /**
   * @return A value in milliseconds
   */
  public abstract int getDuration(T object);
}
