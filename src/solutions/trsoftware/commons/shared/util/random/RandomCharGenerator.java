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

package solutions.trsoftware.commons.shared.util.random;

import java.util.HashSet;
import java.util.Set;

import static solutions.trsoftware.commons.shared.util.RandomUtils.rnd;

/**
 * Generates random chars drawn from the given alphabet.
 *
 * @author Alex, 8/4/2017
 */
public class RandomCharGenerator {

  private String alphabet;

  public RandomCharGenerator(String alphabet) {
    this.alphabet = alphabet;
  }

  public char next() {
    return alphabet.charAt(rnd.nextInt(alphabet.length()));
  }

  public char next(Set<Character> exclusions) {
    Set<Character> exclusionSet = new HashSet<Character>(exclusions);
    char next;
    do {
      next = next();
    }
    while (!exclusionSet.add(next));
    return next;
  }

}
