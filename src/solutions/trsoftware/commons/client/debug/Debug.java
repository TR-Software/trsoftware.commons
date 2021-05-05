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

package solutions.trsoftware.commons.client.debug;

import com.google.gwt.core.shared.GWT;

/**
 * Main class of the {@code src/solutions/trsoftware/commons/Debug.gwt.xml} module.
 *
 * @author Alex
 * @since 5/1/2021
 */
public class Debug {

  public static final Debug INSTANCE = GWT.create(Debug.class);
  public static final boolean ENABLED = INSTANCE.isEnabled();

  public boolean isEnabled() {
    return false;
  }

  /**
   * Will be swapped in via deferred binding when the module property "debug" is "on".
   */
  public static class Impl extends Debug {
    @Override
    public boolean isEnabled() {
      return true;
    }
  }
}
