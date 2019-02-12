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

package solutions.trsoftware.commons.server.util;

import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

/**
 * Date: Sep 17, 2008 Time: 6:32:44 PM
 *
 * @author Alex
 */
public class RuntimeUtils {

  /**
   * We consider the code to be running in JUnit if somewhere on the stack
   * there is a {@link junit.framework.TestCase} or one of its subclasses
   */
  public static boolean isRunningInJUnit() {
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
  public static boolean isClassOnStack(Class<?> targetClass) {
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
   *
   * @see ThreadUtils#waitFor(BooleanSupplier, long)
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

  /**
   * Creates a {@link ProcessBuilder} for starting a new JVM process with the same classpath as the current java process.
   * <p>
   * The resultant builder will be pre-configured with the command line
   * <pre>
   *   {java.home}/bin/java -classpath {classpath}
   * </pre>
   * Other command-line arguments can be added directly to {@link ProcessBuilder#command()}.
   * <h3>Example:</h3>
   * <pre>
   *   ProcessBuilder processBuilder = buildNewJavaProcess();
   *   processBuilder.command().add("com.example.Foo");
   *   processBuilder.command().add("arg1");
   *   processBuilder.command().add("arg2");
   *   Process subprocess = processBuilder.start();
   * </pre>
   * Will start a process with the command line
   * <pre>
   *   {java.home}/bin/java -classpath {classpath} com.example.Foo arg1 arg2
   * </pre>
   */
  public static ProcessBuilder buildNewJavaProcess() {
    Path javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java");
    return new ProcessBuilder(
        javaExecutable.toString(),
        "-classpath",
        getClassPath()
    );
  }

  private static final String JAVA_CLASS_PATH_PROP = "java.class.path";

  /**
   * @return the value of the system property {@value #JAVA_CLASS_PATH_PROP}.
   * @see RuntimeMXBean#getClassPath()
   */
  public static String getClassPath() {
    return System.getProperty(JAVA_CLASS_PATH_PROP);
  }
}
