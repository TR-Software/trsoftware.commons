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

package solutions.trsoftware.commons.server;

import solutions.trsoftware.commons.server.util.CanStopClock;
import solutions.trsoftware.commons.server.util.RuntimeUtils;

/**
 * Jun 30, 2009
 *
 * @author Alex
 */
public interface ServerConstants {
  // save the value of this method as a constant so that the code relying on this check can be optimized by the compiler
  boolean IN_UNIT_TEST = RuntimeUtils.isRunningInJUnit();
  /** requires each unit test that needs to stop the clock to explicitly inherit from CanStopClock */
  boolean IS_CLOCK_STOPPABLE = RuntimeUtils.isClassOnStack(CanStopClock.class);
}
