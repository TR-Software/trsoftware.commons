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

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public interface CodingDictionary<V> {

  /** Returns a canonical code for the word */
  V encode(String word);

  /** Translates a canonical code back to the word */
  String decode(V code);

  /** @return the number of unique words in the dictionary */
  int size();

}
