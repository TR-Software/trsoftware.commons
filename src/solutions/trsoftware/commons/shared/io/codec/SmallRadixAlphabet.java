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

package solutions.trsoftware.commons.shared.io.codec;

import java.util.Arrays;

/**
 * Any alphabet base 62 or less
 *
 * @author Alex
 */
public class SmallRadixAlphabet extends AlphabetAdapter {
  // TODO(3/3/2025): capital letters should come before lowercase to ensure lexicographic string comparison
  static final byte[] codingAlphabet = {
      '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' ,
      '8' , '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' ,
      'g' , 'h' , 'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
      'o' , 'p' , 'q' , 'r' , 's' , 't' , 'u' , 'v' ,
      'w' , 'x' , 'y' , 'z' , 'A' , 'B' , 'C' , 'D' ,
      'E' , 'F' , 'G' , 'H' , 'I' , 'J' , 'K' , 'L' ,
      'M' , 'N' , 'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
      'U' , 'V' , 'W' , 'X' , 'Y' , 'Z'
  };
  public SmallRadixAlphabet(int base) {
    super(base, Arrays.copyOfRange(codingAlphabet, 0, base), (byte)'-');
  }
}