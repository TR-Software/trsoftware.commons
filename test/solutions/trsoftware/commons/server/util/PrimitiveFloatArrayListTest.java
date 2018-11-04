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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Oct 22, 2012
 *
 * @author Alex
 */
public class PrimitiveFloatArrayListTest extends TestCase {

  public void testPrimitiveFloatArrayList() throws Exception {
    // test the class versus ArrayList<Float>
    ArrayList<Float> expectedList = new ArrayList<>();
    PrimitiveFloatArrayList ourList = new PrimitiveFloatArrayList();
    // first, add 1000 random elements to each
    for (int i = 0; i < 1000; i++) {
      float f = RandomUtils.rnd.nextInt();
      expectedList.add(f);
      ourList.add(f);
      assertEquals(expectedList, ourList);
    }
    assertEquals(expectedList.size(), ourList.size());
    // now test in-place sorting
    Collections.sort(expectedList);
    Collections.sort(ourList);
    assertEquals(expectedList, ourList);
  }

}