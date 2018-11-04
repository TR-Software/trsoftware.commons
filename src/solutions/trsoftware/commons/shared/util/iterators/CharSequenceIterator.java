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

package solutions.trsoftware.commons.shared.util.iterators;

/**
* @author Alex, 4/27/2016
*/
public class CharSequenceIterator extends IndexedIterator<Character> {

  private final CharSequence charSequence;

  public CharSequenceIterator(CharSequence charSequence) {
    super(charSequence.length());
    this.charSequence = charSequence;
  }

  public CharSequenceIterator(CharSequence charSequence, int start) {
    super(start, charSequence.length());
    this.charSequence = charSequence;
    get(start); // trigger IndexOutOfBoundsException if the starting index isn't valid
  }

  @Override
  protected Character get(int idx) {
    return charSequence.charAt(idx);
  }

}
