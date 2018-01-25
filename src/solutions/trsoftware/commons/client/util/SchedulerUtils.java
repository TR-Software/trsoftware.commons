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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Command;
import solutions.trsoftware.commons.shared.util.callables.Condition;

/**
 * Utility methods for performing certain common tasks with {@link Scheduler}
 *
 * @see #checkAndWait(Condition, int, Command)
 * @author Alex
 */
public final class SchedulerUtils {

  /**
   * Checks the given given {@link Condition} and if it's {@code true}, invokes the given command. If it's false,
   * will use {@link Scheduler#scheduleFixedDelay(RepeatingCommand, int)} to wait until it becomes {@code true}.
   * @param condition will wait for this to become {@code true}
   * @param delayMillis the interval between consecutive checks.
   * @param onConditionMet will execute this as soon as {@code condition} becomes {@code true}
   */
  public static void checkAndWait(final Condition condition, int delayMillis, final Command onConditionMet) {
    RepeatingCommand checkCondition = new RepeatingCommand() {
      @Override
      public boolean execute() {
        if (condition.check()) {
          onConditionMet.execute();
          return false;
        }
        return true;
      }
    };
    if (checkCondition.execute()) {
      Scheduler.get().scheduleFixedDelay(checkCondition, delayMillis);
    }
  }
  
}