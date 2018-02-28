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

package solutions.trsoftware.commons.server.util;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

/**
 * Date: Sep 17, 2008 Time: 6:32:44 PM
 *
 * @author Alex
 */
public class ServerRuntimeUtils {

  /**
   * We consider the code to be running in JUnit if somewhere on the stack
   * there a junit.framework.TestCase or one of its subclasses 
   */
  public static boolean runningInJUnit() {
    return isClassOnStack("junit.framework.TestCase");
  }

  /**
   * Checks the runtime stack of the current thread for presence of the given class
   * or its subclasses.
   *
   * This overload of the method takes a string instead of a Class object
   * to allow using it with classes that may not be loaded in the VM (e.g.
   * JUnit class won't be loaded in production).
   */
  public static boolean isClassOnStack(String targetClassName) {
    try {
      return isClassOnStack(Class.forName(targetClassName));
    }
    catch (ClassNotFoundException e) {
      return false; // the given class is not even in the VM, so it can't be on the stack
    }
  }

  /**
   * Checks the runtime stack of the current thread for presence of the given class
   * or its subclasses.
   */
  public static boolean isClassOnStack(Class targetClass) {
    StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
    for (StackTraceElement stackTraceElement : stackTraceElements) {
      String className = stackTraceElement.getClassName();
      // we check whether the code is was launched from the given class or one of its subclasses
      try {
        Class cls = Class.forName(className);
        if (targetClass.isAssignableFrom(cls))
          return true;
        else if (cls.getEnclosingClass() != null && targetClass.isAssignableFrom(cls.getEnclosingClass()))
          return true;
      }
      catch (ClassNotFoundException e) {
        continue;
      }
    }
    return false;
  }

  /**
   * Keeps retrying the given function until it either returns {@code true} or the given timeout is exceeded.
   * @param function a lambda expression that returns {@code true} when successful.
   * @throws TimeoutException if the given function never returned {@code true} before the given timeout elapsed
   */
  public static void retryWhileFalse(int timeoutMillis, BooleanSupplier function) throws TimeoutException {
    Duration duration = new Duration();
    while (!function.getAsBoolean()) {
      if (duration.elapsedMillis() > timeoutMillis)
        throw new TimeoutException(String.format("Condition not met within %d ms", timeoutMillis));
      try {
        Thread.sleep(100);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }
}
