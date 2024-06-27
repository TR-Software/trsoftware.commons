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

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.testing.StubScheduler;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Command;
import solutions.trsoftware.commons.server.util.ThreadUtils;
import solutions.trsoftware.commons.shared.util.callables.Condition;

import javax.annotation.Nullable;

import static solutions.trsoftware.commons.shared.util.LogicUtils.nonNullOrElse;

/**
 * Utility methods for performing certain common tasks with {@link Scheduler}
 *
 * @see #checkAndWait(Condition, int, Command)
 * @author Alex
 */
public final class SchedulerUtils {

  /**
   * The global scheduler instance to be used everywhere.
   * Can be replaced with a {@link StubScheduler} for unit testing (via {@link #setScheduler(Scheduler)}).
   * Defaults to {@link Scheduler#get()} if {@link GWT#isClient()}, otherwise {@code null}.
   */
  @Nullable
  private static Scheduler scheduler = defaultScheduler();

  /**
   * Returns {@link Scheduler#get()} if {@link GWT#isClient()}, otherwise {@code null}.
   * <p>
   * Returning {@code null} when not in GWT allows using {@link #setScheduler(Scheduler)} in non-GWT unit tests
   * (otherwise {@link Scheduler#get()}, which uses {@link GWT#create(Class)}, would throw {@link UnsupportedOperationException}
   * @return
   */
  private static Scheduler defaultScheduler() {
    return GWT.isClient() ? Scheduler.get() : null;
  }
  // TODO: maybe move all mockable instances to a single class (e.g. com.typeracer.main.server.services.ServicesSnapshot)

  /**
   * @return the current value {@link #scheduler}, which could be {@code null} if not {@link GWT#isClient()}
   * @see #setScheduler(Scheduler)
   */
  @Nullable
  public static Scheduler getScheduler() {
    return scheduler;
  }

  /**
   * Allows overriding the {@linkplain #defaultScheduler() default scheduler} for unit testing.
   * Notably, this allows a plain Java test case to test scheduling behavior without extending {@link GWTTestCase}.
   * @param scheduler
   */
  public static void setScheduler(Scheduler scheduler) {
    SchedulerUtils.scheduler = nonNullOrElse(scheduler, SchedulerUtils::defaultScheduler);
  }

  /**
   * Checks the given given {@link Condition}, and if it's {@code true}, executes the given command right away
   * and returns {@code true}.
   *
   * Otherwise, will schedule a {@link RepeatingCommand} to keep checking the condition and execute the command as soon
   * as it's been met.
   *
   * @param condition will wait for this to become {@code true}
   * @param delayMillis the interval between consecutive checks.
   * @param timeoutMillis the background task will give up if the condition not met within this number of milliseconds
   * @param onConditionMet will execute this as soon as {@code condition} becomes {@code true}
   * @return {@code true} if the given command was executed right away, otherwise {@code false}, to indicate
   * that a background timer was started by this method
   *
   * @see ThreadUtils#waitFor(java.util.function.BooleanSupplier, long, long)
   */
  public static boolean checkAndWait(final Condition condition, int delayMillis, int timeoutMillis, final Command onConditionMet) {
    if (condition.check()) {
      onConditionMet.execute();
      return true;
    }
    Duration waitingDuration = new Duration();
    RepeatingCommand checkCondition = () -> {
      if (condition.check()) {
        onConditionMet.execute();
        return false;
      }
      return waitingDuration.elapsedMillis() <= timeoutMillis;
    };
    Scheduler.get().scheduleFixedDelay(checkCondition, delayMillis);
    return false;
  }

  /**
   * Checks the given given {@link Condition}, and if it's {@code true}, executes the given command right away
   * and returns {@code true}.
   *
   * Otherwise, will schedule a {@link RepeatingCommand} to keep checking the condition and execute the command as soon
   * as it's been met.
   *
   * @param condition will wait for this to become {@code true}
   * @param delayMillis the interval between consecutive checks.
   * @param onConditionMet will execute this as soon as {@code condition} becomes {@code true}
   * @return {@code true} if the given command was executed right away, otherwise {@code false}, to indicate
   * that a background timer was started by this method
   *
   * @see #checkAndWait(Condition, int, int, Command)
   */
  public static boolean checkAndWait(final Condition condition, int delayMillis, final Command onConditionMet) {
    return checkAndWait(condition, delayMillis, Integer.MAX_VALUE, onConditionMet);
  }
  
}