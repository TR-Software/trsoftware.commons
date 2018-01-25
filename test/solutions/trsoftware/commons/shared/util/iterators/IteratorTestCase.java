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

package solutions.trsoftware.commons.shared.util.iterators;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Alex, 4/9/2017
 */
public abstract class IteratorTestCase extends TestCase {

  /**
   * Asserts that the given iterator returns all of the elements in the given list,
   * and throws {@link NoSuchElementException} when there are no more elements left
   */
  public static <T> void assertIteratedElements(List<T> expectedElements, final Iterator<T> testIter) {
    assertEquals(expectedElements, CollectionUtils.asList(testIter));
    assertFalse(testIter.hasNext());
    AssertUtils.assertThrows(NoSuchElementException.class, new Runnable() {
      @Override
      public void run() {
        testIter.next();
      }
    });
  }
}
