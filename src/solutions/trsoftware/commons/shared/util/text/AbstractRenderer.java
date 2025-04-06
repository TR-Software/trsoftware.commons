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


import com.google.gwt.text.shared.Renderer;

import java.io.IOException;
import java.util.function.Function;

/**
 * Same as the {@link com.google.gwt.text.shared.AbstractRenderer} class (provides a default implementation of {@link
 * Renderer#render(Object, Appendable)}), but implemented as an {@code interface} (using a Java 1.8 {@code default}
 * method), thus allowing mixin-style multiple inheritance and lambdas.
 *
 * @author Alex
 * @since 7/27/2018
 */
public interface AbstractRenderer<T> extends Renderer<T> {

  @Override
  default void render(T object, Appendable appendable) throws IOException {
    appendable.append(render(object));
  }

  @SuppressWarnings("unchecked")
  static <T> Renderer<T> asRenderer(Function<T, String> toStringFunction) {
    return (AbstractRenderer<T>)toStringFunction;
  }
}
