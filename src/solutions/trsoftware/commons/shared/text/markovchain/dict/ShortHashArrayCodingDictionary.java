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
 * Maps words in the dictionary to unique shorts and back again.
 *
 * Supports dictionaries with up to Short.MAX_VALUE = 32767 entries.
 *
 * Oct 20, 2009
 *
 * @author Alex
 */
public class ShortHashArrayCodingDictionary extends HashArrayCodingDictionary<String, Short> {

  protected String encodeKey(String key) {
    return key;
  }

  protected String decodeKey(String key) {
    return key;
  }

  protected Short makeValue(int value) {
    return (short)value;
  }

  public short[] encodeAll(String[] words) {
    short[] result = new short[words.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = encode(words[i]);
    }
    return result;
  }

  public String[] decodeAll(short[] words) {
    String[] result = new String[words.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = decode(words[i]);
    }
    return result;
  }
}