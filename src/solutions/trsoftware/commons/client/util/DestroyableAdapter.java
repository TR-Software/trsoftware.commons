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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Command;

/**
 * Date: Oct 9, 2008 Time: 8:55:24 PM
 *
 * @author Alex
 */
public class DestroyableAdapter implements Destroyable {
  private Destroyer delegate = new Destroyer();

  public void destroy() {
    delegate.destroy();
  }

  public void addCleanupAction(Command command) {
    delegate.addCleanupAction(command);
  }
}
