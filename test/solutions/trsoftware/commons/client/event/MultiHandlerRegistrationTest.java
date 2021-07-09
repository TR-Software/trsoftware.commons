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

package solutions.trsoftware.commons.client.event;

import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.UmbrellaException;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Strings.lenientFormat;

/**
 * @author Alex
 * @since 7/8/2021
 */
public class MultiHandlerRegistrationTest extends TestCase {

  public void testRemoveHandler() throws Exception {
    // 1) test that it doesn't throw a NPE for any null handler registrations
    {
      List<MockRegistration> components = Arrays.asList(
          new MockRegistration(0), null, new MockRegistration(1));
      MultiHandlerRegistration reg = new MultiHandlerRegistration(components.toArray(new HandlerRegistration[0]));
      reg.removeHandler();
      assertAllHandlersRemoved(components);
      System.out.println("================================================================================");
    }
    // 2) test that removeHandler is invoked for all components, regardless of whether they throw an exception
    {
      List<MockRegistration> components = Arrays.asList(
          new MockRegistration(0), new BadRegistration(1),
          new MockRegistration(2), new BadRegistration(3),
          new MockRegistration(4));
      MultiHandlerRegistration reg = new MultiHandlerRegistration(components.toArray(new HandlerRegistration[0]));
      UmbrellaException umbrellaException = AssertUtils.assertThrows(UmbrellaException.class, (Runnable)() -> reg.removeHandler());
      Set<Throwable> causes = umbrellaException.getCauses();
      assertEquals(Sets.newHashSet(new SimulatedException(1), new SimulatedException(3)), causes);
      assertAllHandlersRemoved(components);
    }
  }

  private void assertAllHandlersRemoved(List<MockRegistration> components) {
    for (MockRegistration reg : components) {
      if (reg != null) {
        assertEquals(1, reg.removeCount);
      }
    }
  }

  /**
   * Simply prints a message when {@link #removeHandler()} is invoked.
   */
  private static class MockRegistration implements HandlerRegistration  {
    protected final int id;
    /** Number of times the {@link #removeHandler()} method has been invoked */
    private int removeCount;

    private MockRegistration(int id) {
      this.id = id;
    }

    @Override
    public void removeHandler() {
      removeCount++;
      System.out.println(lenientFormat("%s(%s).removeHandler() invocation #%s",
          getClass().getSimpleName(), id, removeCount));
    }
  }

  /**
   * Throws an exception from {@link #removeHandler()}
   */
  private static class BadRegistration extends MockRegistration {

    private BadRegistration(int id) {
      super(id);
    }

    @Override
    public void removeHandler() {
      super.removeHandler();
      throw new SimulatedException(id);
    }
  }

  private static class SimulatedException extends RuntimeException {
    private int id;

    private SimulatedException(int id) {
      super("Simulated exception #" + id);
      this.id = id;
    }


    private SimulatedException() {
      // empty constructor to suppress warnings about Serializable
    }

    public int getId() {
      return id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      SimulatedException that = (SimulatedException)o;

      return id == that.id;
    }

    @Override
    public int hashCode() {
      return id;
    }
  }
}