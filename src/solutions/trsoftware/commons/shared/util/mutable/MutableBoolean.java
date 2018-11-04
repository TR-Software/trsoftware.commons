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

import solutions.trsoftware.commons.shared.util.callables.Condition;

/**
 * GWT-compatible replacement for {@link java.util.concurrent.atomic.AtomicBoolean AtomicBoolean}
 * and {@code org.apache.commons.lang3.mutable.MutableBoolean}.
 *
 * @author Alex
 * @since 11/28/2017
 */
public class MutableBoolean implements Condition {

  private volatile boolean value;

  public MutableBoolean() {
  }

  public MutableBoolean(boolean value) {
    this.value = value;
  }

  public boolean get() {
    return value;
  }

  @Override
  public boolean check() {
    return get();
  }

  public void set(boolean value) {
    this.value = value;
  }
}
