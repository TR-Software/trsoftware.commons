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

package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary;

import java.util.Arrays;

/**
 * Oct 26, 2009
 *
 * @author Alex
 */
class NgramStringState extends StringState {

  private String[] wordCodes;

  public NgramStringState(CodingDictionary<String> dict, String... words) {
    wordCodes = new String[words.length];
    for (int i = 0; i < words.length; i++) {
      wordCodes[i] = dict.encode(words[i]);
    }
  }

  @Override
  public String getWord(int index, CodingDictionary<String> dict) {
    return wordCodes[index];
  }

  public int wordCount() {
    return wordCodes.length;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NgramStringState that = (NgramStringState)o;

    if (!Arrays.equals(wordCodes, that.wordCodes)) return false;

    return true;
  }

  public int hashCode() {
    return wordCodes != null ? Arrays.hashCode(wordCodes) : 0;
  }
}
