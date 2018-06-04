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

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.testutil.SetUpTearDownDelegate;
import solutions.trsoftware.commons.server.testutil.SetUpTearDownDelegateList;
import solutions.trsoftware.commons.server.util.CanStopClock;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A base {@link TestCase} that provides some additional functionality:
 *
 * <ul>
 *   <li>
 *     Allows specifying custom set-up and tear-down behavior with command objects, as an alternative to inheritance
 *     of the {@link #setUp()} / {@link #tearDown()} methods. This is useful when something similar to multiple
 *     inheritance is desired (e.g. to mix multiple test classes with different {@link #setUp()} / {@link #tearDown()}
 *     behavior.
 *     See {@link #addSetupTearDownDelegate(SetUpTearDownDelegate)}
 *   </li>
 *   <li>
 *     Nulls out all instance fields upon {@link #tearDown()}, and prints a warning for any field that wasn't {@code null}
 *     at the end of the test.
 *     See <a href="https://stackoverflow.com/a/3653734">StackOverflow discussion on this subject</a>
 *   </li>
 * </ul>
 *
 * @author Alex
 */
public abstract class SuperTestCase extends TestCase implements CanStopClock {

  private SetUpTearDownDelegateList delegates;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (delegates != null)
      delegates.setUpAll();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      if (delegates != null) {
        delegates.tearDownAll();
        delegates = null;
      }
      super.tearDown();
    }
    finally {
      checkForNonNullFields();
    }
  }

  /**
   * @return The same instance that was passed in, to allow method chaining.
   */
  public <T extends SetUpTearDownDelegate> T addSetupTearDownDelegate(T delegate) {
    if (delegates == null)
      delegates = new SetUpTearDownDelegateList();
    delegates.add(delegate);
    return delegate;
  }

  /**
   * Nulls out all reference fields of the subclass (and prints a warning for any fields that are found to be not null)
   * after the test case's execution.  In practice, this doesn't make much of a difference,
   * but according to the linked StackOverflow question, the instances will not be GC'd while a suite is running.
   * So if we have some {@link TestCase} subclass or a custom suite that defines a lot of test methods, it's probably
   * best to null out those fields.
   * @throws Exception
   * @see <a href="https://stackoverflow.com/a/3653734">StackOverflow discussion on this subject</a>
   */
  private void checkForNonNullFields() throws Exception {
    // see http://stackoverflow.com/questions/3653589/junit-should-i-assign-null-to-resources-in-teardown-that-were-instantiated-in
    Class cls = getClass();
    while (!cls.equals(TestCase.class)) {
      for (Field field : cls.getDeclaredFields()) {
        Class<?> fieldType = field.getType();
        if (!Modifier.isStatic(field.getModifiers()) && !fieldType.isPrimitive() && !fieldType.isEnum()) {
          field.setAccessible(true);
          if (field.get(this) != null) {
            System.err.println("WARNING: field not null after tearDown: " + field.getDeclaringClass().getSimpleName() + "#" + field.getName() + " - it will be nulled by reflection in SuperTestCase.checkForNonNullFields");
            field.set(this, null);
          }
        }
      }
      cls = cls.getSuperclass();
    }
  }

}