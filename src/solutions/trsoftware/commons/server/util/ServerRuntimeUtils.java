package solutions.trsoftware.commons.server.util;

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
   */
  public static void retryWhileFalse(int timeoutMillis, BooleanSupplier function) {
    Duration duration = new Duration();
    while (!function.getAsBoolean()) {
      if (duration.elapsedMillis() > timeoutMillis)
        throw new RuntimeException("Operation timed out");
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
