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
public class MaxComparableTest extends CollectableStatsTestCase {

  public void testMax() throws Exception {
    MaxComparable<Integer> m = new MaxComparable<Integer>();
    assertNull(m.get());
    assertEquals(2, (int)m.updateAndGet(2));
    assertEquals(2, (int)m.updateAndGet(1));
    assertEquals(3, (int)m.updateAndGet(3));
    assertEquals(3, (int)m.get());
    assertEquals(3, (int)m.updateAndGet(2));
    assertEquals(3, (int)m.get());
  }

  public void testUpdateFromCollection() throws Exception {
    MaxComparable<Integer> m = new MaxComparable<Integer>();
    m.updateAll(intList(1, 3, 2));
    assertEquals(3, (int)m.get());
  }

  public void testEval() throws Exception {
    assertEquals((Integer)3, MaxComparable.eval(1, 3, 2));
  }

  public void testUpdateFromConstructor() throws Exception {
    MaxComparable<Integer> m = new MaxComparable<Integer>(intList(1, 3, 2));
    assertEquals(3, (int)m.get());
    assertEquals(3, (int)m.updateAndGet(0));
    assertEquals(4, (int)m.updateAndGet(4));
  }

  private List<Integer> intList(Integer... values) {
    return Arrays.asList(values);
  }

  public void testEqualsAndHashCode() throws Exception {
    MaxComparable<Integer> m1 = new MaxComparable<Integer>(intList(1, 3, 2));
    // these two lists have the same max
    MaxComparable<Integer> m2 = new MaxComparable<Integer>(intList(1, 3, 2, 1, 0));
    assertEqualsAndHashCode(m1, m2);
    // the next two do not
    assertNotEqual(m2, new MaxComparable<Integer>(intList(1, 3, 2, 1, 4)));
    // the next object is not an instance of MaxComparable
    assertNotEqual(m1, new MinComparable<Integer>(intList(1, 3, 2)));
  }

  @Override
  public void testAsCollector() throws Exception {
    MaxComparable<Integer> result = doTestAsCollector(new MaxComparable<>(), null, 1, 3, 2, 1, -5);
    // sanity check
    assertEquals((Integer)3, result.getValue());
  }
}