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

package solutions.trsoftware.commons.shared.util.callables;

/**
 * A function &empty; &rarr; {@link R} that might throw exception {@link E}.
 *
 * Basically the same as {@link java.util.concurrent.Callable}, but allows the exception type to be parametrized
 *
 * @author Alex
 */
@FunctionalInterface
public interface Function0t<R, E extends Throwable> {
  R call() throws E;
}