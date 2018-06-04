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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * A delegate that facilitates other classes implementing the {@link Destroyable} interface.
 * Provides "mix-in" logic.
 * 
 * @author Alex
 */
public class Destroyer implements Destroyable {

  /** The actions to be performed when this view goes away (removing listeners, etc) */
  private List<Command> cleanupActions;

  private boolean destroyed = false;

  /** Cleans up everything created by this view.  This logic will be executed only once. */
  public void destroy() {
    if (destroyed)
      return;
    destroyed = true;
    if (cleanupActions != null) {
      for (Command cleanupAction : cleanupActions) {
        cleanupAction.execute();
      }
    }
  }

  public void addCleanupAction(Command command) {
    if (cleanupActions == null)
      cleanupActions = new ArrayList<Command>();
    cleanupActions.add(command);
  }
}