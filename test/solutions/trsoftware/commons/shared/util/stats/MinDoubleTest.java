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

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

/**
 * @author Alex, 1/8/14
 */
public class MinDoubleTest extends TestCase {

  public void testCRUD() throws Exception {
    assertEquals(Double.POSITIVE_INFINITY, new MinDouble().get());
    assertEquals(0d, new MinDouble(0d).get());
    assertEquals(-1d, new MinDouble(-1d).get());
    assertEquals(1d, new MinDouble(1d).get());
    assertEquals(1d, new MinDouble(1d, 2d).get());
    assertEquals(-2d, new MinDouble(1d, 2d, -2d).get());
  }

  public void testEqualsAndHashCode() throws Exception {
    AssertUtils.assertEqualsAndHashCode(new MinDouble(), new MinDouble());
    AssertUtils.assertEqualsAndHashCode(new MinDouble(1d), new MinDouble(1d));
    AssertUtils.assertEqualsAndHashCode(new MinDouble(1d, 2d, -2d), new MinDouble(-2d));

    AssertUtils.assertNotEqualsAndHashCode(new MinDouble(), new MaxDouble());
    AssertUtils.assertNotEqualsAndHashCode(new MinDouble(1d), new MaxDouble(1d));
    AssertUtils.assertNotEqualsAndHashCode(new MinDouble(1d), new MinDouble(-1d));
    AssertUtils.assertNotEqualsAndHashCode(new MinDouble(1d, 2d, -2d), new MinDouble(1d, 2d, 2d));
  }

  public void testMerge() throws Exception {
    {
      MinDouble minDouble = new MinDouble(0d);
      minDouble.merge(new MinDouble(-1d));
      assertEquals(new MinDouble(0d, -1d), minDouble);
    }
    {
      MinDouble minDouble = new MinDouble();
      minDouble.merge(new MinDouble(1d));
      minDouble.merge(new MinDouble(2d, 3d));
      minDouble.merge(new MinDouble());
      assertEquals(new MinDouble(1d, 2d, 3d), minDouble);
    }
  }

}
