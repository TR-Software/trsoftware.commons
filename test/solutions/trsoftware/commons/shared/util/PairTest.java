/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertEqualsAndHashCode;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertNotEqual;

/**
 * @author Alex
 * @since 8/23/2021
 */
public class PairTest extends TestCase {

  public void testTestEqualsAndHashCode() throws Exception {
    for (int i = 0; i < 5; i++) {
      Pair<Integer, Integer> p = new Pair<>(i, i);
      assertEqualsAndHashCode(p, new Pair<>(i, i));
      // the equals method should accept subclasses
      ImmutablePair<Integer, Integer> ip = new ImmutablePair<>(i, i);
      assertEqualsAndHashCode(p, ip);
      assertEqualsAndHashCode(ip, p);
      // test some non-equal cases
      assertNotEqual(p, new Pair<>(i, i+1));
      assertNotEqual(p, new ImmutablePair<>(i, i+1));
    }
    // test some cases with nulls
    assertEqualsAndHashCode(new Pair<>(null, null), new Pair<>(null, null));
    assertEqualsAndHashCode(new Pair<>("foo", null), new ImmutablePair<>("foo", null));
  }
}