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

package solutions.trsoftware.commons.server.testutil;

import solutions.trsoftware.commons.client.util.MemoryUnit;
import solutions.trsoftware.commons.client.util.callables.Function0;

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

  /** Prints out the memory still occupied after doing the given number of GCs */
  public static String printMemoryUsage(int gcCount) {
    return String.format("Memory used: %3.3f MB", bytesToMegs(calcSystemMemoryUsage(gcCount)));
  }

  public static double bytesToMegs(long bytes) {
    return MemoryUnit.MEGABYTES.fromBytes(bytes);
  }

  public static double bytesToKilobytes(long bytes) {
    return MemoryUnit.KILOBYTES.fromBytes(bytes);
  }

  /**
   * Returns the difference in memory usage after the object
   * is created the given factory.
   */
  public static long measureMemoryDelta(Function0 factory) {
    return measureMemoryDelta(1, factory);
  }

  /**
   * Returns the difference in memory usage after the object
   * is created the given factory. 
   * @param gcCount The number garbage collections to be run before and
   * after each measurement: 1 is fastest, but a larger number, like 10 will
   * give a better estimate of memory usage
   */
  public static long measureMemoryDelta(int gcCount, Function0 factory) {
    long memBefore = calcSystemMemoryUsage(gcCount);
    Object result = factory.call();
    long memAfter = calcSystemMemoryUsage(gcCount);
    // the following statement is mainly just to make sure the JIT doesn't clear the
    // reference to result before memAfter gets calculated
    if (result == null)
      System.err.println("WARNING: factory produced a null result.");
    return memAfter - memBefore;
  }

  /**
   * Prints the difference in memory usage after the object
   * is created the given factory (e.g. {prefix} used up x.y KB of memory)
   * @param factory
   */
  public static void printMemoryDelta(String prefix, Function0 factory) {
    printMemoryDelta(prefix, 2, factory);
  }


  /**
   * Prints the difference in memory usage after the object
   * is created the given factory (e.g. {prefix} used up x.y KB of memory)
   * @param gcCount The number garbage collections to be run before and
   * after each measurement: 1 is fastest, but a larger number, like 10 will
   * give a better estimate of memory usage
   */
  public static void printMemoryDelta(String prefix, int gcCount, Function0 factory) {
    System.out.printf(
        "%s used up %3.3f KB of memory%n",
        prefix,
        bytesToKilobytes(measureMemoryDelta(gcCount, factory)));
  }

  /**
   * Prints the difference in memory usage after the object
   * is created the given factory (e.g. {prefix} used up x.y KB of memory)
   * also measures and prints the time needed to construct the object.
   * @param factory
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
   * Measures current JVM memory usage as it is, without any GCs.
   * @return The number of bytes of memory in use right now.
   */
  private static long calcSystemMemoryUsage() {
    return calcSystemMemoryUsage(0);
  }

  /**
   * Measures current JVM memory usage.
   * @param gcCount The number of GCs to run prior to measuring the memory usage.
   * @return The number of bytes of used memory after running the desired GCs.
   */
  public static long calcSystemMemoryUsage(int gcCount) {
    Runtime rt = Runtime.getRuntime();
    for (int i = 0; i < gcCount; i++) {
      rt.gc();  // run enough GCs to get rid of temporary objects (like CSVReader) that were used for loading the object whose size we're interested in
    }
    return rt.totalMemory() - rt.freeMemory();
  }
}
