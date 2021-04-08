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

/**
 * A state representing 2 consecutive words.  Uses less memory than
 * NgramShortState when N == 2.
 *
 * Supports dictionaries with up to Short.MAX_VALUE = 32767 entries (because
 * the internal representation of the state is pair of shorts (to save memory).
 *
 * @author Alex
 */
class BigramShortState extends ShortState {

  private short word1Code;
  private short word2Code;

  BigramShortState(String word1, String word2, CodingDictionary<Short> dict) {
    int word1Code = dict.encode(word1);
    assert word1Code <= Short.MAX_VALUE;
    int word2Code = dict.encode(word2);
    assert word2Code <= Short.MAX_VALUE;

    this.word1Code = (short)word1Code;
    this.word2Code = (short)word2Code;
  }

  @Override
  public String getWord(int index, CodingDictionary<Short> dict) {
    if (index == 0)
      return dict.decode(word1Code);
    else if (index == 1)
      return dict.decode(word2Code);
    else
      throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  public int wordCount() {
    return 2;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BigramShortState that = (BigramShortState)o;

    if (word1Code != that.word1Code) return false;
    if (word2Code != that.word2Code) return false;

    return true;
  }

  public int hashCode() {
    int result = 0;
    result = 31 * result + (int)word1Code;
    result = 31 * result + (int)word2Code;
    return result;
  }
}
