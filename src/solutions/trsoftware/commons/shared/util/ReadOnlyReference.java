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

package solutions.trsoftware.commons.shared.util;

/**
 * A simple immutable container (i.e. box) for any object reference that cannot be modified
 * after being set.  Similar to a {@code final} field, with the only difference being
 * that the value can be initialized at any time.
 *
 *
 * @author Alex
 */
public final class ReadOnlyReference<T> {

  private volatile T referent;

  public ReadOnlyReference() {
  }

  public ReadOnlyReference(T referent) {
    this.referent = referent;
  }

  /**
   * @throws IllegalStateException if the value has already been set (!= null)
   */
  public synchronized void set(T newReferent) throws IllegalStateException {
    if (referent != null)
      throw new IllegalStateException("Attempt to modify a ReadOnlyReference");
    referent = newReferent;
  }

  public T get() {
    return referent;
  }
}