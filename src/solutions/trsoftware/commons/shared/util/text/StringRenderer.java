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
 * Uses {@link Object#toString()} to render values.  Renders {@code null} values as {@code ""} by default, but
 * can specify a different replacement string using the {@link #StringRenderer(String)} constructor.
 * <p>
 * This class is functionally identical to {@link com.google.gwt.text.shared.ToStringRenderer},
 * but allows using a generic type variable {@link T}.
 *
 * @author Alex
 * @since 12/2/2017
 */
@SuppressWarnings("rawtypes")
public class StringRenderer<T> extends AbstractRenderer<T> {

  private static StringRenderer instance;

  private final String textForNull;

  public StringRenderer() {
    this("");
  }

  public StringRenderer(String textForNull) {
    this.textForNull = textForNull;
  }

  @Override
  public String render(T object) {
    return object != null ? object.toString() : textForNull;
  }

  @SuppressWarnings("unchecked")
  public static <T> StringRenderer<T> getInstance() {
    if (instance == null)
      instance = new StringRenderer();
    return instance;
  }
}
