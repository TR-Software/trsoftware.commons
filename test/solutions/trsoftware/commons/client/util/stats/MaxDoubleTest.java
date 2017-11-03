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

package solutions.trsoftware.commons.client.util.stats;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

/**
 * @author Alex, 1/8/14
 */
public class MaxDoubleTest extends TestCase {

  public void testCRUD() throws Exception {
    assertEquals(Double.NEGATIVE_INFINITY, new MaxDouble().get());
    assertEquals(0d, new MaxDouble(0d).get());
    assertEquals(-1d, new MaxDouble(-1d).get());
    assertEquals(1d, new MaxDouble(1d).get());
    assertEquals(2d, new MaxDouble(1d, 2d).get());
    assertEquals(2d, new MaxDouble(1d, 2d, -2d).get());
  }

  public void testEqualsAndHashCode() throws Exception {
    AssertUtils.assertEqualsAndHashCode(new MaxDouble(), new MaxDouble());
    AssertUtils.assertEqualsAndHashCode(new MaxDouble(1d), new MaxDouble(1d));
    AssertUtils.assertEqualsAndHashCode(new MaxDouble(1d, 2d, -2d), new MaxDouble(2d));

    AssertUtils.assertNotEqualsAndHashCode(new MaxDouble(), new MinDouble());
    AssertUtils.assertNotEqualsAndHashCode(new MaxDouble(1d), new MinDouble(1d));
    AssertUtils.assertNotEqualsAndHashCode(new MaxDouble(1d), new MaxDouble(-1d));
    AssertUtils.assertNotEqualsAndHashCode(new MaxDouble(1d, 2d, -2d), new MaxDouble(1d));
  }

  public void testMerge() throws Exception {
    {
      MaxDouble maxDouble = new MaxDouble(0d);
      maxDouble.merge(new MaxDouble(-1d));
      assertEquals(new MaxDouble(0d, -1d), maxDouble);
    }
    {
      MaxDouble maxDouble = new MaxDouble();
      maxDouble.merge(new MaxDouble(1d));
      maxDouble.merge(new MaxDouble(2d, 3d));
      maxDouble.merge(new MaxDouble());
      assertEquals(new MaxDouble(1d, 2d, 3d), maxDouble);
    }
  }

}
