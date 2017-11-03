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

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicReference;

public class ServerRuntimeUtilsTest extends TestCase {

  public void testRunningInJUnit() throws Exception {
    // this thread is running in JUnit
    assertTrue(ServerRuntimeUtils.runningInJUnit());

    final AtomicReference<Boolean> innerThreadInJunit = new AtomicReference<>(null);

    // the inner thread is also consider "in JUnit" because it's enclosing class is a test case
    Thread innerThread = new Thread(() -> innerThreadInJunit.set(ServerRuntimeUtils.runningInJUnit()));
    innerThread.start();
    innerThread.join();
    assertNotNull(innerThreadInJunit.get());
    assertTrue(innerThreadInJunit.get()); // the inner thread was not launched by JUnit

    // this thread will not be considered as running in JUnit because it's not defined inside a test class.
    ServerRuntimeUtilsTestThread outerThread = new ServerRuntimeUtilsTestThread();
    outerThread.start();
    outerThread.join();
    assertNotNull(outerThread.getRanInJUnit());
    assertFalse(outerThread.getRanInJUnit()); // the outer thread was not launched by JUnit
  }

}