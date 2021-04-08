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

package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary;

import java.util.Arrays;

/**
 * A state representing N consecutive words.
 *
 * Supports dictionaries with up to Short.MAX_VALUE = 32767 entries (because
 * the internal representation of the state is pair of shorts (to save memory).
 *
 * @author Alex
 */
class NgramShortState extends ShortState {

  private short[] wordCodes;

  NgramShortState(CodingDictionary<Short> dict, String... words) {
    wordCodes = new short[words.length];
    for (int i = 0; i < words.length; i++) {
      int code = dict.encode(words[i]);
      assert code <= Short.MAX_VALUE;
      wordCodes[i] = (short)code;
    }
  }

  @Override
  public String getWord(int index, CodingDictionary<Short> dict) {
    return dict.decode(wordCodes[index]);
  }

  public int wordCount() {
    return wordCodes.length;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NgramShortState that = (NgramShortState)o;

    if (!Arrays.equals(wordCodes, that.wordCodes)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return wordCodes != null ? Arrays.hashCode(wordCodes) : 0;
  }
}