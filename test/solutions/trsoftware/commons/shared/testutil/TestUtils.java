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

package solutions.trsoftware.commons.shared.testutil;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwtmockito.GwtMockito;
import junit.framework.Assert;
import junit.framework.TestCase;
import solutions.trsoftware.commons.client.debug.Debug;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

/**
 * @author Alex, 10/23/2017
 * @see solutions.trsoftware.commons.shared.testutil.TestData
 */
public abstract class TestUtils {

  protected TestUtils() {
  }

  /**
   * Busy waits until the condition evaluates to true or the timeout has
   * elapsed.
   *
   * @return true if the condition was met within timeoutMs, false otherwise.
   */
  public static boolean waitFor(BooleanSupplier endCondition, long timeoutMs) throws Exception {
    long startTime = System.currentTimeMillis();
    while (!endCondition.getAsBoolean()) {
      if (System.currentTimeMillis() > startTime + timeoutMs) {
        return false;
      }
    }
    return true;
  }

  /**
   * Busy waits for the given number of milliseconds, using {@link System#currentTimeMillis()}
   */
  public static void busyWait(long millis) {
    long target = System.currentTimeMillis() + millis;
    while (System.currentTimeMillis() < target);
  }

  /**
   * Busy waits for the given number of nanoseconds, using {@link System#nanoTime()}
   */
  public static void busyWaitNanos(long nanos) {
    long target = System.nanoTime() + nanos;
    while (System.nanoTime() < target);
  }

  /**
   * Prints the given message surrounded by dashed lines of {@code '-'} chars of equal length
   *
   * @param msg the header text
   * @return the resulting width of the header lines in the output
   */
  public static int printSectionHeader(String msg) {
    return printSectionHeader(msg, '-');
  }

  /**
   * Prints the given message surrounded by 2 lines filled with {@code width} repetitions of the given char.
   * 
   * @param msg the header text
   * @param lineChar the filler character for the surrounding lines
   * @return the resulting width of the header lines in the output
   * @see #printSectionHeader(String)
   */
  public static int printSectionHeader(String msg, char lineChar) {
    int width = msg != null ? msg.length() : 0;
    printSectionHeader(msg, lineChar, width);
    return width;
  }

  /**
   * Prints the given message surrounded by 2 lines filled with {@code width} repetitions of the given char.
   * 
   * @param msg the header text
   * @param lineChar the filler character for the surrounding lines
   * @param width the length of the surrounding lines
   * @see #printSectionHeader(String)
   */
  public static void printSectionHeader(String msg, char lineChar, int width) {
    if (StringUtils.notBlank(msg)) {
      String hr = StringUtils.repeat(lineChar, width);
      System.out.println(hr);
      System.out.println(msg);
      System.out.println(hr);
    }
  }

  /**
   * Prints the elements of the given collection on separate lines, each indented by 2 spaces.
   */
  public static void printIndented(Collection<?> collection) {
    printIndented(collection, 2);
  }

  /**
   * Prints the given message followed by the elements of the given collection on separate lines, each indented by 2 spaces.
   */
  public static void printIndented(String message, Collection<?> collection) {
    printIndented(message, collection.stream());
  }

  /**
   * Prints the elements of the given collection on separate lines, each indented by the given number of spaces.
   */
  public static void printIndented(Collection<?> collection, int nSpaces) {
    printIndented(collection.stream(), nSpaces);
  }

  /**
   * Prints the elements of the given collection on separate lines, each indented by the given number of spaces.
   * @param out then print destination
   */
  public static void printIndented(Collection<?> collection, int nSpaces, PrintStream out) {
    printIndented(collection.stream(), nSpaces, out);
  }

  /**
   * Prints the elements of the given stream on separate lines, each indented by 2 spaces.
   */
  public static void printIndented(Stream<?> stream) {
    printIndented(stream, 2);
  }

  /**
   * Prints the given message followed by the elements of the given stream on separate lines, each indented by 2 spaces.
   */
  public static void printIndented(String message, Stream<?> stream) {
    System.out.println(message);
    printIndented(stream, 2);
  }

  /**
   * Prints the elements of the given stream on separate lines, each indented by the given number of spaces.
   */
  public static void printIndented(Stream<?> stream, int nSpaces) {
    printIndented(stream, nSpaces, System.out);
  }

  /**
   * Prints the elements of the given stream on separate lines, each indented by the given number of spaces.
   * @param out the print destination
   */
  public static void printIndented(Stream<?> stream, int nSpaces, PrintStream out) {
    stream.map(Objects::toString).map(StringUtils.indenting(nSpaces))
        .forEach(out::println);
  }

  /**
   * Temporarily sets <code>{@link Debug#ENABLED} = true</code> (if needed) and runs the given code.
   * <p>
   * This allows testing logic that requires that requires {@link Debug#ENABLED} to be {@code true} in a plain Java
   * {@link TestCase} (without using a {@code GWTTestCase} that overrides the {@code Debug.gwt.xml} module).
   * <p>
   * <b>Note:</b> since the {@link Debug} class relies on {@link GWT#create(Class)}, any code outside a {@code GWTTestCase}
   * should first invoke {@link GwtMockito#initMocks(Object)} to make this work.
   *
   * @param code logic that requires {@link Debug#ENABLED} to be {@code true}
   * @see Injections#setFieldValue(Field, Object)
   * @see FieldValueReplacement#setFieldValue(Field, Object)
   * @see <a href="https://github.com/google/gwtmockito">GwtMockito project</a>
   */
  @GwtIncompatible("Reflection")
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public static void withDebugEnabled(Runnable code) throws Exception {
    // TODO: extract this method to a util class
    // Note: we have to modify the Debug.ENABLED value by reflection, to allow testing this case
    if (!Debug.ENABLED) {
      Field debugEnabledField = Debug.class.getDeclaredField("ENABLED");
      try (FieldValueReplacement<Boolean> temp = FieldValueReplacement.setFieldValue(debugEnabledField, true)) {
        //noinspection ConstantConditions - just making sure the value was replaced
        Assert.assertTrue(Debug.ENABLED);
        code.run();
      }
      Assert.assertFalse(Debug.ENABLED);  // make sure OG value was restored
    }
    else {  // Debug.ENABLED is already true
      code.run();
    }
  }
}
