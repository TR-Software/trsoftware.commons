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

package solutions.trsoftware.commons.shared.util.stats;

import junit.framework.TestCase;

public class MeanTest extends TestCase {

  public void testMean() throws Exception {
    Mean<Integer> mean = new Mean<Integer>();
    assertEquals(0, mean.getNumSamples());
    assertEquals(0.0, mean.getMean());

    mean.update(1);
    assertEquals(1, mean.getNumSamples());
    assertEquals(1.0, mean.getMean());

    mean.update(2);
    assertEquals(2, mean.getNumSamples());
    assertEquals((1d + 2d) / 2, mean.getMean());

    mean.update(3);
    assertEquals(3, mean.getNumSamples());
    assertEquals((1d + 2d + 3d) / 3, mean.getMean());
  }

  /** Makes sure that a non-parametrized Mean instance can handle multiple kinds of Number inputs at once */
  public void testMeanWithDifferentDataTypes() throws Exception {
    Mean<Number> mean = new Mean<Number>();
    assertEquals(0, mean.getNumSamples());
    assertEquals(0.0, mean.getMean());

    mean.update(1);
    assertEquals(1, mean.getNumSamples());
    assertEquals(1.0, mean.getMean());

    mean.update(2L);
    assertEquals(2, mean.getNumSamples());
    assertEquals((1d + 2d) / 2, mean.getMean());

    mean.update(3f);
    assertEquals(3, mean.getNumSamples());
    assertEquals((1d + 2d + 3d) / 3, mean.getMean());

    mean.update(4d);
    assertEquals(4, mean.getNumSamples());
    assertEquals((1d + 2d + 3d + 4d) / 4, mean.getMean());
  }
}