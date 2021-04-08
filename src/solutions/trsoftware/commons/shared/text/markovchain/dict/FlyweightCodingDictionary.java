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

package solutions.trsoftware.commons.shared.text.markovchain.dict;

import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.HashMap;

/**
 * Implements functionality similar to String.intern, but allows the String
 * instances to be garbage collected when the instance of this class
 * is no longer referenced.
 *
 * String.intern() on the other hand, puts the strings in PermGen, not on the
 * heap, so they will likely never get GC'd.
 *
 * Oct 26, 2009
 *
 * @author Alex
 */
public class FlyweightCodingDictionary implements CodingDictionary<String> {

  private HashMap<String, String> flyweight = new HashMap<String, String>();

  /** Returns a canonical code for the word */
  public String encode(String word) {
    return MapUtils.getOrInsert(flyweight, word, word);
  }

  /** Translates a canonical code back to the word */
  public String decode(String code) {
    return flyweight.get(code);
  }

  /** @return the number of unique words in the dictionary */
  public int size() {
    return flyweight.size();
  }
}
