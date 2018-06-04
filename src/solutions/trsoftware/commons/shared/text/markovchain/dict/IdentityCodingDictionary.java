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

package solutions.trsoftware.commons.shared.text.markovchain.dict;

/**
 * This coder does nothing.  The output is the same instance as the input.
 *
 * Oct 26, 2009
 *
 * @author Alex
 */
public class IdentityCodingDictionary implements CodingDictionary<String> {

  /** Returns a canonical code for the word */
  public String encode(String word) {
    return word;
  }

  /** Translates a canonical code back to the word */
  public String decode(String code) {
    return code;
  }

  /** @return the number of unique words in the dictionary */
  public int size() {
    return 0;
  }
}