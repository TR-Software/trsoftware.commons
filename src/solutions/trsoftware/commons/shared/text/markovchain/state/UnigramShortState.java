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
 * A state representing only 1 words.  Uses less memory than
 * NgramShortState when N == 1.
 *
 * Supports dictionaries with up to Short.MAX_VALUE = 32767 entries (because
 * the internal representation of the state is pair of shorts (to save memory).
 *
 * @author Alex
 */
class UnigramShortState extends ShortState {

  private short wordCode;

  UnigramShortState(String word, CodingDictionary<Short> dict) {
    int code = dict.encode(word);
    assert code <= Short.MAX_VALUE;
    wordCode = (short)code; 
  }

  @Override
  public String getWord(int index, CodingDictionary<Short> dict) {
    if (index == 0)
      return dict.decode(wordCode);
    else
      throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  public int wordCount() {
    return 1;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UnigramShortState that = (UnigramShortState)o;

    if (wordCode != that.wordCode) return false;

    return true;
  }

  public int hashCode() {
    return (int)wordCode;
  }
}