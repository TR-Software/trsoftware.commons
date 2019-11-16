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

package solutions.trsoftware.commons.shared.util.callables;

import java.util.function.BiFunction;

/**
 * A function {@link A} &times; {@link B} &rarr; {@link R}.
 *
 * @deprecated made obsolete by {@link BiFunction} in Java 1.8+
 * @author Alex
 */
public interface Function2<A, B, R> extends BiFunction<A, B, R> {
  R call(A a, B b);

  @Override
  default R apply(A a, B b) {
    return call(a, b);
  }
}