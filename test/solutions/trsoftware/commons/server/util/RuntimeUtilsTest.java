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

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicReference;

import static solutions.trsoftware.commons.server.util.RuntimeUtils.*;

public class RuntimeUtilsTest extends TestCase {

  public void testRunningInJUnit() throws Exception {
    // this thread is running in JUnit
    assertTrue(isRunningInJUnit());

    final AtomicReference<Boolean> innerThreadInJunit = new AtomicReference<>(null);

    // the inner thread is also consider "in JUnit" because it's enclosing class is a test case
    Thread innerThread = new Thread(() -> innerThreadInJunit.set(isRunningInJUnit()));
    innerThread.start();
    innerThread.join();
    assertNotNull(innerThreadInJunit.get());
    assertTrue(innerThreadInJunit.get()); // the inner thread was not launched by JUnit

    // this thread will not be considered as running in JUnit because it's not defined inside a test class.
    RuntimeUtilsTestThread outerThread = new RuntimeUtilsTestThread();
    outerThread.start();
    outerThread.join();
    assertNotNull(outerThread.getRanInJUnit());
    assertFalse(outerThread.getRanInJUnit()); // the outer thread was not launched by JUnit
  }

  public void testBuildNewJavaProcess() throws Exception {
    ProcessBuilder processBuilder = buildNewJavaProcess();
    processBuilder.command().add(PrintClassPath.class.getName());
    Process process = processBuilder.start();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      assertEquals(getClassPath(), br.readLine());
      assertEquals(0, process.waitFor());
    }
  }

  public void testGetClassPath() throws Exception {
    assertEquals(System.getProperty("java.class.path"), getClassPath());
    assertEquals(ManagementFactory.getRuntimeMXBean().getClassPath(), getClassPath());
  }
}