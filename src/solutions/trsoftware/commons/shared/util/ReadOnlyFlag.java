/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
 * A simple immutable container (i.e. box) for a {@code boolean} that cannot be modified
 * after being set.  Similar to a {@code final} field, with the only difference being
 * that the value can be initialized at any time.
 *
 * This class makes it easy to ensure that something happens only once:
 *
 * <pre>
 * if (readOnlyFlag.set()) {
 *   // do something that should only happen once
 * }
 * </pre>
 * @author Alex
 */
public final class ReadOnlyFlag {

  private volatile boolean set = false;

  public ReadOnlyFlag() {
  }

  /**
   * Sets the flag if it has never been set before.
   *
   * @return true if successful. False return indicates that the value has already been set.
   */
  public synchronized boolean set() {
    return !set && (set = true);
  }

  public boolean isSet() {
    return set;
  }
}