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

package solutions.trsoftware.commons.shared.util.stats;

import java.util.Arrays;
import java.util.List;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertEqualsAndHashCode;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertNotEqual;

/**
 * May 12, 2009
 *
 * @author Alex
 */
public class MinComparableTest extends CollectableStatsTestCase {

  public void testMin() throws Exception {
    MinComparable<Integer> m = new MinComparable<Integer>();
    assertNull(m.get());
    assertEquals(2, (int)m.updateAndGet(2));
    assertEquals(1, (int)m.updateAndGet(1));
    assertEquals(1, (int)m.updateAndGet(3));
    assertEquals(1, (int)m.get());
    assertEquals(-1, (int)m.updateAndGet(-1));
    assertEquals(-1, (int)m.get());
  }
  
  public void testUpdateFromCollection() throws Exception {
    MinComparable<Integer> m = new MinComparable<Integer>();
    m.updateAll(intList(1, 3, 2));
    assertEquals(1, (int)m.get());
  }

  public void testEval() throws Exception {
    assertEquals((Integer)1, MinComparable.eval(1, 3, 2));
  }

  public void testUpdateFromConstructor() throws Exception {
    MinComparable<Integer> m = new MinComparable<Integer>(intList(1, 3, 2));
    assertEquals(1, (int)m.get());
    assertEquals(0, (int)m.updateAndGet(0));
    assertEquals(0, (int)m.updateAndGet(4));
  }

  private List<Integer> intList(Integer... values) {
    return Arrays.asList(values);
  }

  public void testEqualsAndHashCode() throws Exception {
    MinComparable<Integer> m1 = new MinComparable<Integer>(intList(1, 3, 2));
    // these two lists have the same min
    MinComparable<Integer> m2 = new MinComparable<Integer>(intList(1, 3, 2, 1, 4));
    assertEqualsAndHashCode(m1, m2);
    // the next two do not
    assertNotEqual(m2, new MinComparable<Integer>(intList(1, 3, 2, 1, 0)));
    // the next object is not an instance of MinComparable
    assertNotEqual(m1, new MaxComparable<Integer>(intList(1, 3, 2)));
  }

  @Override
  public void testAsCollector() throws Exception {
    MinComparable<Integer> result = doTestAsCollector(new MinComparable<>(), null, 1, 3, 2, 4, 16);
    // sanity check
    assertEquals((Integer)1, result.getValue());
  }
}