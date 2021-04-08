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

package solutions.trsoftware.commons.shared.util.text;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Uses {@link Object#toString()} to render values.  Renders {@code null} values as {@code ""}.
 *
 * This class is mostly the same as {@link com.google.gwt.text.shared.ToStringRenderer}, but allows a generic type
 * variable {@code <T>}.
 *
 * @author Alex
 * @since 12/2/2017
 */
public class StringRenderer<T> extends AbstractRenderer<T> {

  private static StringRenderer instance;

  @Override
  public String render(T object) {
    return object != null ? object.toString() : "";
  }

  @SuppressWarnings("unchecked")
  public static <T> StringRenderer<T> getInstance() {
    if (instance == null)
      instance = new StringRenderer();
    return instance;
  }
}
