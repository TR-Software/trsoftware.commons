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

package solutions.trsoftware.commons.shared.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple way to ensure that some code only runs once.
 * <p>
 * <b>Example</b>:
 * <pre>
 *   RunOnlyOnceGuard guard = new RunOnlyOnceGuard();
 *   guard.check();  // no problem
 *   guard.check();  // throws IllegalStateException
 * </pre>
 * <p>
 * <b>NOTE:</b> unless the exception-throwing behavior provided by this class is necessary, it is simpler
 * to use {@link AtomicBoolean} to ensure that something happens only once
 * (see {@link AtomicBoolean#compareAndSet(boolean, boolean)})
 *
 * @author Alex
 * @see AtomicBoolean#compareAndSet(boolean, boolean)
 */
public class RunOnlyOnceGuard {
  private final AtomicBoolean locked = new AtomicBoolean();

  /**
   * @throws IllegalStateException if this method has already been called on this instance.
   */
  public void check(String failureMessage) throws IllegalStateException {
    if (!locked.compareAndSet(false, true))  // this method should only be called once per lifetime of this instance
      throw new IllegalStateException(failureMessage);
  }

  public boolean isLocked() {
    return locked.get();
  }
}
