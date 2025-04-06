/*
 * Copyright 2024 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.callables;

import solutions.trsoftware.commons.client.testutil.SimulatedException;
import solutions.trsoftware.commons.shared.BaseTestCase;

import javax.annotation.Nullable;
import java.util.logging.Logger;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 9/2/2023
 */
public class SafeRunnableTest extends BaseTestCase {

  public void testRun() throws Exception {
    int nextId = 1;
    {
      // with default impl of onError
      ThrowingSafeRunnable r = new ThrowingSafeRunnable(++nextId);
      r.run();  // exception should not propagate
      assertTrue(r.ran);
      assertTrue(r.onErrorInvoked);
    }
    {
      // overriding onError to return false
      ThrowingSafeRunnable r = new ThrowingSafeRunnable(++nextId) {
        @Override
        public boolean onError(Throwable e) {
          super.onError(e);
          return false;
        }
      };
      assertThrows(new SimulatedException(nextId), r);  // exception should propagate if onError returns false
      assertTrue(r.ran);
      assertTrue(r.onErrorInvoked);
    }
    {
      // overriding getLogger
      ThrowingSafeRunnable r = new ThrowingSafeRunnable(++nextId) {
        @Nullable
        @Override
        public Logger getLogger() {
          return SafeRunnableTest.this.getLogger();
        }
      };
      r.run();  // exception should not propagate
      assertTrue(r.ran);
      assertTrue(r.onErrorInvoked);
    }
    {
      // not throwing any exceptions
      ThrowingSafeRunnable r = new ThrowingSafeRunnable(++nextId) {
        @Override
        protected void throwException() {
          // NO-OP
        }
      };
      r.run();
      assertTrue(r.ran);
      assertFalse(r.onErrorInvoked);
    }

  }

  class ThrowingSafeRunnable implements SafeRunnable {
    protected final int id;
    protected boolean ran;
    protected boolean onErrorInvoked;

    public ThrowingSafeRunnable(int id) {
      this.id = id;
    }

    @Override
    public void doRun() {
      ran = true;
      throwException();
    }

    protected void throwException() {
      throw new SimulatedException(id);
    }

    @Override
    public boolean onError(Throwable e) {
      onErrorInvoked = true;
      assertEquals(new SimulatedException(id), e);
      return SafeRunnable.super.onError(e);
    }
  }
}