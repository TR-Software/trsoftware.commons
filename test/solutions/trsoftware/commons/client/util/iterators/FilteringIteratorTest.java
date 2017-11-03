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

package solutions.trsoftware.commons.client.util.iterators;

import solutions.trsoftware.commons.client.util.mutable.MutableInteger;

import java.util.Arrays;
import java.util.List;

public class FilteringIteratorTest extends IteratorTestCase {

  public void testFilteringIterator() throws Exception {
    assertIteratedElements(Arrays.asList(1, 3, 5, 7, 9),
        new FilteringIterator<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).iterator()) {
          @Override
          protected boolean filter(Integer x) {
            assert x != null;
            return x % 2 == 1;  // match only odd numbers
          }
        }
    );
  }

  public void testFilteringWithNullElements() throws Exception {
    // the 1-arg constructor excludes null input elements by default
    final List<Integer> inputs = Arrays.asList(null, 2, null, 4, 5, 6, 7, 8, 9, null);
    assertIteratedElements(Arrays.asList(5, 7, 9),
        new FilteringIterator<Integer>(inputs.iterator()) {
          @Override
          protected boolean filter(Integer x) {
            assert x != null;
            return x % 2 == 1;  // match only odd numbers
          }
        }
    );
    // using the the 2-arg constructor we can configure it to include null input elements
    final MutableInteger nullsSeen = new MutableInteger();
    assertIteratedElements(Arrays.asList(5, 7, 9),
        new FilteringIterator<Integer>(true, inputs.iterator()) {
          @Override
          protected boolean filter(Integer x) {
            if (x == null) {
              nullsSeen.increment();
              return false;
            }
            return x % 2 == 1;  // match only odd numbers
          }
        }
    );
    assertEquals(3, nullsSeen.get());
  }
}