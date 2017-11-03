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

package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import java.util.HashSet;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class RandomIdTest extends TestCase {

  public void testNextString() throws Exception {
    // generate a bunch of random IDs
    Random rnd = new Random(123);
    int nValues = 100;
    HashSet<String> values = new HashSet<String>();
    boolean allBitsUsed = false;
    for (int i = 0; i < nValues; i++) {
      String resultString = RandomId.nextString(rnd);
      AssertUtils.assertThat(resultString).isNotNull().matchesRegex("[0-9a-z]+");
      assertTrue(values.add(resultString));  // collisions should be extremely unlikely with such a low number of random values
      // the result should be a base36-encoded positive integer that's up to 48 bits long
      long resultLong = Long.parseLong(resultString, RandomId.RADIX);
      // the result should be a base36-encoded positive integer that's up to 48 bits long
      assertTrue(resultLong < (1L << RandomId.BITS));
      // furthermore, we expect at least one of the results to use all the 6 bytes of the 48-bit value space
      allBitsUsed |= ((resultLong & 0xffffffffffffL) != 0);
    }
    assertTrue(allBitsUsed);
    System.out.println(values);
  }

  public void testNextLong() throws Exception {
    Random rnd = new Random(123);
    int nValues = 10000;
    SortedSet<Long> values = new TreeSet<Long>();
    boolean allBitsUsed = false;
    for (int i = 0; i < nValues; i++) {
      long resultLong = RandomId.nextLong(rnd);
      assertTrue(values.add(resultLong));  // collisions should be extremely unlikely
      assertTrue(resultLong > 0);  // getting a value of 0 should be extremely unlikely
      // the result should be a base36-encoded positive integer that's up to 48 bits long
      assertTrue(resultLong < (1L << RandomId.BITS));
      // furthermore, we expect at least one of the results to use all the 6 bytes of the 48-bit value space
      allBitsUsed |= ((resultLong & 0xffffffffffffL) != 0);
    }
    assertTrue(allBitsUsed);
    System.out.println(values);
  }
}