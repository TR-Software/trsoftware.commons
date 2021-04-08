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
import java.util.Map;

/**
 * Speeds up insertion into ArrayCodingDictionary by maintaing a HashMap of
 * all the words at the expense of using up more memory.
 *
 * Oct 20, 2009
 *
 * @author Alex
 */
public abstract class HashArrayCodingDictionary<K, V extends Number> extends ArrayCodingDictionary<K, V> {

  /** Maps words to their code (ensures the uniqueness of word->code mappings) */
  private Map<K, V> mapping = new HashMap<K, V>();

  /**
   * Looks up the given word and returns its existing index.  If not found,
   * returns the provided nextIndex and adds it to the lookup table if
   * applicable.
   */
  @Override
  protected final int lookup(K word, int nextIndex) {
    return MapUtils.getOrInsert(mapping, word, makeValue(nextIndex)).intValue();
  }

}