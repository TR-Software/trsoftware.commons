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
 * Oct 11, 2012
 *
 * @author Alex
 */
public class MinAndMaxComparableTest extends CollectableStatsTestCase {

  public void testMinAndMax() throws Exception {
    MinAndMaxComparable<Integer> m = new MinAndMaxComparable<Integer>();
    assertNull(m.getMin());
    assertNull(m.getMax());

    m.update(2);
    assertEquals(2, (int)m.getMin());
    assertEquals(2, (int)m.getMax());

    m.update(1);
    assertEquals(1, (int)m.getMin());
    assertEquals(2, (int)m.getMax());

    m.update(3);
    m.update(2);
    assertEquals(1, (int)m.getMin());
    assertEquals(3, (int)m.getMax());
  }

  public void testUpdateFromCollection() throws Exception {
    MinAndMaxComparable<Integer> m = new MinAndMaxComparable<Integer>();
    m.updateAll(intList(1, 3, 2));
    assertEquals(1, (int)m.getMin());
    assertEquals(3, (int)m.getMax());
  }

  public void testUpdateFromConstructor() throws Exception {
    MinAndMaxComparable<Integer> m = new MinAndMaxComparable<Integer>(intList(1, 3, 2));
    assertEquals(1, (int)m.getMin());
    assertEquals(3, (int)m.getMax());
  }

  private List<Integer> intList(Integer... values) {
    return Arrays.asList(values);
  }

  public void testEqualsAndHashCode() throws Exception {
    MinAndMaxComparable<Integer> m1 = new MinAndMaxComparable<Integer>(intList(1, 3, 2));
    // these two lists have the same min and max
    MinAndMaxComparable<Integer> m2 = new MinAndMaxComparable<Integer>(intList(1, 3, 2, 1, 2, 3, 1));
    assertEqualsAndHashCode(m1, m2);
    // the next two do not
    MinAndMaxComparable<Integer> m3 = new MinAndMaxComparable<Integer>(intList(1, 3, 2, 1, 4));
    assertNotEqual(m1, m3);
    // the next object is not an instance of MinAndMaxComparable
    assertNotEqual(m1, new MinComparable<Integer>(intList(1, 3, 2)));
  }

  @Override
  public void testAsCollector() throws Exception {
    MinAndMaxComparable<Integer> result =
        doTestAsCollector(new MinAndMaxComparable<>(), null, 1, 3, 2, 1, 2, 3, 1);
    // sanity check
    assertEquals((Integer)1, result.getMin());
    assertEquals((Integer)3, result.getMax());
  }
}