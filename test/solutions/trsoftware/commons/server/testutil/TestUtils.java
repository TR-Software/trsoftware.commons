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

package solutions.trsoftware.commons.server.testutil;

import com.google.common.testing.GcFinalization;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.MemoryUnit;
import solutions.trsoftware.commons.shared.util.callables.Function0;

import java.io.PrintStream;
import java.util.Collection;

/**
 * Jul 29, 2009
 *
 * @author Alex
 */
public class TestUtils {

  public static String printMemoryStats() {
    Runtime runtime = Runtime.getRuntime();
    return String.format("Memory (MB): %3.3f total, %3.3f free, %3.3f max, %3.3f used",
        bytesToMegs(runtime.totalMemory()), bytesToMegs(runtime.freeMemory()), bytesToMegs(runtime.maxMemory()),
        bytesToMegs(runtime.totalMemory() - runtime.freeMemory()));
  }

  public static double bytesToMegs(long bytes) {
    return MemoryUnit.MEGABYTES.fromBytes(bytes);
  }

  public static double bytesToKilobytes(long bytes) {
    return MemoryUnit.KILOBYTES.fromBytes(bytes);
  }

  /**
   * Attempts to measure the difference in memory usage before and after an object is created by the given factory.
   * Will attempt to wait for a full-heap GC prior to making both measurements.
   * @return the memory usage difference, in bytes
   */
  public static long measureMemoryDelta(Function0 factory) {
    return measureMemoryDelta(true, factory);
  }

  /**
   * Attempts to measure the difference in memory usage before and after an object is created by the given factory.
   * @param awaitFullGc if {@code true}, will attempt to wait for a full-heap GC prior to making the measurements
   * @return the memory usage difference, in bytes
   */
  public static long measureMemoryDelta(boolean awaitFullGc, Function0 factory) {
    long memBefore = calcSystemMemoryUsage(awaitFullGc);
    Object result = factory.call();
    long memAfter = calcSystemMemoryUsage(awaitFullGc);
    // the following statement is mainly just to make sure the JIT doesn't clear the
    // reference to result before memAfter gets calculated
    if (result == null)
      System.err.println("WARNING: factory produced a null result.");
    return memAfter - memBefore;
  }

  /**
   * Prints the difference in memory usage before and after an object is created by the given factory.
   * Will attempt to wait for a full-heap GC prior to making both measurements.
   * <p style="font-style: italic;">
   *   Output example:
   *   <pre>{prefix} used up 36.133 KB of memory and 0.18 ms avg. time</pre>
   * </p>
   */
  public static void printMemoryDelta(String prefix, Function0 factory) {
    System.out.printf(
        "%s used up %,3.3f KB of memory%n",
        prefix,
        bytesToKilobytes(measureMemoryDelta(factory)));
  }

  /**
   * Prints the difference in time and memory usage before and after an object is created by the given factory.
   *
   * <p style="font-style: italic;">
   *   Output example:
   *   <pre>{prefix} used up 36.133 KB of memory and 0.18 ms avg. time</pre>
   * </p>
   * @param factory the code to be benchmarked (should produce a new object whose memory usage is to be measured)
   * @param prefix a string describing the object being benchmarked, will be used in the
   */
  public static void printMemoryAndTimeUsage(String prefix, Function0 factory) {
    // first measure the memory usage (we don't time this because the GCs will use a lot of time)
    double kilobytesUsed = TestUtils.bytesToKilobytes(measureMemoryDelta(factory));
    // now do enough iterations of the factory to take up a full second and determine the average running time
    long startTime = System.currentTimeMillis();
    long desiredEndTime = startTime + 1000;
    int iterations = 0;
    while (System.currentTimeMillis() < desiredEndTime) {
      Object result = factory.call();
      iterations++;
    }
    long actualEndTime = System.currentTimeMillis();
    double avgTime = (double)(actualEndTime - startTime) / (double)iterations;

    System.out.printf(
        "%s used up %,.3f KB of memory and %,.2f ms avg. time%n",
        prefix, kilobytesUsed, avgTime);
  }

  /**
   * Measures current JVM memory usage as it is, without performing any GC beforehand
   * @return The number of bytes of memory in use right now.
   */
  private static long calcSystemMemoryUsage() {
    return calcSystemMemoryUsage(false);
  }

  /**
   * Measures the current memory usage of the JVM, optionally waiting for a full-heap GC before making the measurement.
   * @param awaitFullGc if {@code true}, will invoke {@link GcFinalization#awaitFullGc()} prior to the measurement
   * @return The current memory usage in bytes
   * @see GcFinalization#awaitFullGc()
   * @see <a href="https://stackoverflow.com/a/27831908">StackOverflow discussion about Java memory benchmarking</a>
   */
  public static long calcSystemMemoryUsage(boolean awaitFullGc) {
    System.gc();
    if (awaitFullGc)
      GcFinalization.awaitFullGc(); // see https://stackoverflow.com/a/27831908
    Runtime rt = Runtime.getRuntime();
    return rt.totalMemory() - rt.freeMemory();
  }

  /**
   * Prefixes a test name with the full name of its class.
   * @param testCase a test instance
   * @return {@code "<className>.<testName>"}; example: {@code "com.example.FooTest.testFoo"}
   */
  public static String qualifiedTestName(TestCase testCase) {
    return qualifiedTestName(testCase, false);
  }

  /**
   * Prefixes a test name with the name of its class.
   * @param testCase a test instance
   * @param useSimpleClassName {@code true} to use {@link Class#getSimpleName()} else {@link Class#getName()}
   * @return {@code "<className>.<testName>"}; example: {@code "FooTest.testFoo"} or {@code "com.example.FooTest.testFoo"}
   */
  public static String qualifiedTestName(TestCase testCase, boolean useSimpleClassName) {
    Class<? extends TestCase> testClass = testCase.getClass();
    String className = useSimpleClassName ? testClass.getSimpleName() : testClass.getName();
    return className + "." + testCase.getName();
  }

  /**
   * Prints the given collection to {@link System#out} such that every element is on a new line.
   * Elements are printed using their default {@link Object#toString()} representation.
   */
  public static void printCollection(String name, Collection collection) {
    PrintStream out = System.out;
    if (name != null)
      out.print(name + " = ");
    if (collection.isEmpty()) {
      out.println("[]");
      return;
    }
    out.println("[");
    for (Object elt : collection) {
      out.printf("  %s%n", elt);
    }
    out.println("]");
  }

  public static String toPrettyJson(Object o) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(o);
  }
}
