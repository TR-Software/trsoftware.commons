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
 * A simple way to ensure that some code only runs once.
 * <p>
 * <b>Example</b>:
 * <pre>
 *   RunOnlyOnceGuard guard = new RunOnlyOnceGuard();
 *   guard.check();  // no problem
 *   guard.check();  // throws IllegalStateException
 * </pre>
 * </p>
 * @author Alex
 */
public class RunOnlyOnceGuard {
  private boolean locked = false;

  /**
   * @throws IllegalStateException if this method has already been called.
   */
  public void check(String failureMessage) throws IllegalStateException {
    if (locked)  // this method should only be called once per lifetime of this instance
      throw new IllegalStateException(failureMessage);
    locked = true;
  }

  public boolean isLocked() {
    return locked;
  }
}
