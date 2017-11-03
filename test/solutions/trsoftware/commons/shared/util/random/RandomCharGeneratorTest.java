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

package solutions.trsoftware.commons.shared.util.random;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Alex, 8/4/2017
 */
public class RandomCharGeneratorTest extends TestCase {

  public void testNext() throws Exception {
    String alphabet = "12345";
    RandomCharGenerator gen = new RandomCharGenerator(alphabet);
    int iterations = 100;
    for (int i = 0; i < iterations; i++) {
      assertTrue(alphabet.indexOf(gen.next()) >= 0);
    }
  }

  public void testNextWithExclusions() throws Exception {
    String alphabet = "12345";
    RandomCharGenerator gen = new RandomCharGenerator(alphabet);
    int iterations = 100;
    for (int nCharsToExclude = 1; nCharsToExclude < alphabet.length() - 1; nCharsToExclude++) {
      Set<Character> exclusionSet = new HashSet<Character>();
      for (int i = 0; i < nCharsToExclude; i++) {
         exclusionSet.add(alphabet.charAt(i));
      }
      for (int i = 0; i < iterations; i++) {
        assertFalse(exclusionSet.contains(gen.next(exclusionSet)));
      }
    }
  }
}