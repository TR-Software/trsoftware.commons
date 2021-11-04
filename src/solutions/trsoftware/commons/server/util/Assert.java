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

package solutions.trsoftware.commons.server.util;

import com.google.common.base.Preconditions;
import solutions.trsoftware.commons.server.ServerConstants;

/**
 * Dec 30, 2009
 *
 * @author Alex
 */
public abstract class Assert extends solutions.trsoftware.commons.shared.util.Assert {

  public static void assertInUnitTestOrCanStopClock() {
    if (!ServerConstants.IN_UNIT_TEST && !ServerConstants.IS_CLOCK_STOPPABLE)
      throw new IllegalStateException("This code is intended for testing and should not be invoked from this context.  Did you forget to inherit CanStopClock?");
  }

  public static void assertNotInUnitTest() {
    if (ServerConstants.IN_UNIT_TEST)
      throw new IllegalStateException("This code is not intended for unit testing and should not be invoked from this context.");
  }

  /**
   * @throws IllegalArgumentException if the given object is not an instance of the given type
   *                                  (in the same sense as the {@code instanceof} operator)
   */
  public static void assertIsInstanceOf(Object obj, Class<?> expectedType) {
    Preconditions.checkArgument(expectedType.isInstance(obj),
        "The given argument (%s) must be an instance of %s", obj, expectedType);
  }
}
