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

package solutions.trsoftware.commons.shared.util.mutable;

import solutions.trsoftware.commons.shared.util.stats.Mergeable;

/**
 * Superclass for GWT-compatible imitations of {@link java.util.concurrent.atomic}'s {@code Atomic*} classes
 * and {@link org.apache.commons.lang3.mutable}'s {@code Mutable*} classes.
 *
 * @author Alex
 */
public abstract class MutableNumber extends Number implements Mergeable<MutableNumber>, Comparable<Number> {

  /** @return the value of this number as a primitive wrapper type */
  public abstract Number numberValue();

  @Override
  public final String toString() {
    // WARNING: do not change this - it's important that the toString method
    // match that of other Number subclasses, otherwise bizarre things could happen,
    // like issues JSON serialization and the like
    return numberValue().toString();
  }

}
