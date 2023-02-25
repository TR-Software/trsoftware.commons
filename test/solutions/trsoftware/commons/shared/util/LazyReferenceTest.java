/*
 * Copyright 2023 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import solutions.trsoftware.commons.shared.BaseTestCase;

/**
 * @author Alex
 * @since 1/22/2023
 */
public class LazyReferenceTest extends BaseTestCase {

  public void testGet() throws Exception {
    Object toCreate = new Object();
    MockLazyReference<Object> ref = new MockLazyReference<>(toCreate);
    assertFalse(ref.created);
    assertFalse(ref.hasValue());
    assertNull(ref.get(false));
    assertFalse(ref.created);
    assertFalse(ref.hasValue());
    assertSame(toCreate, ref.get());
    assertTrue(ref.created);
    assertTrue(ref.hasValue());
    // subsequent calls to get should keep returning the same object
    assertSame(toCreate, ref.get());
    assertSame(toCreate, ref.get(false));
    assertSame(toCreate, ref.get(true));


  }

  private static class MockLazyReference<T> extends LazyReference<T> {
    boolean created;
    T toCreate;

    public MockLazyReference(T toCreate) {
      this.toCreate = toCreate;
    }

    @Override
    protected T create() {
      if (created)
        fail("Already created");
      created = true;
      return toCreate;
    }
  }
}