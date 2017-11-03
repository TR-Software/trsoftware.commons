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

package solutions.trsoftware.commons.client.gchart;

import junit.framework.TestCase;

public class GoogleChartTest extends TestCase {

  public void testGChartExtendedEncode() throws Exception {
    assertEquals("AA", GoogleChart.extendedEncode(0));
    assertEquals("AH", GoogleChart.extendedEncode(7));
    assertEquals("CF", GoogleChart.extendedEncode(133));
    assertEquals("-H", GoogleChart.extendedEncode(3975));
    assertEquals(".F", GoogleChart.extendedEncode(4037));
    assertEquals("..", GoogleChart.extendedEncode(4095));
  }

  public void testGChartExtendedEncodePercentage() throws Exception {
    assertEquals("AA", GoogleChart.extendedEncodePercentage(0.0));
    assertEquals("AH", GoogleChart.extendedEncodePercentage(0.0017094017094017094017094017094017));
    assertEquals("CF", GoogleChart.extendedEncodePercentage(0.032478632478632478632478632478632));
    assertEquals("-H", GoogleChart.extendedEncodePercentage(0.97069597069597069597069597069597));
    assertEquals(".F", GoogleChart.extendedEncodePercentage(0.98583638583638583638583638583639));
    assertEquals("..", GoogleChart.extendedEncodePercentage(1.0));
  }
}