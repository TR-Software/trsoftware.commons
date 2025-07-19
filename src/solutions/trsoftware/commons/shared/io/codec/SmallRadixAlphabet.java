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

/**
 * Any alphabet base 62 or less
 *
 * @author Alex
 */
public class SmallRadixAlphabet extends AlphabetAdapter {
  // TODO(3/3/2025): capital letters should come before lowercase to ensure lexicographic string comparison

  /* TODO(5/21/2025): maybe delete this class
       - for radix <= 36 can just use BigInteger.toString(int) and BigInteger(String, int) constructor
         for radix > 36 it wouldn't be compatible with the standard Java encoding anyways
       - the only argument in favor of keeping this class is it allows using the BigIntRadixCodec API for radix <= 36
         - in that case, reduce the MAX_RADIX of this class to 36
  */
  public static final byte[] CHARS = {
      '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' ,
      '8' , '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' ,
      'g' , 'h' , 'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
      'o' , 'p' , 'q' , 'r' , 's' , 't' , 'u' , 'v' ,
      'w' , 'x' , 'y' , 'z' , 'A' , 'B' , 'C' , 'D' ,
      'E' , 'F' , 'G' , 'H' , 'I' , 'J' , 'K' , 'L' ,
      'M' , 'N' , 'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
      'U' , 'V' , 'W' , 'X' , 'Y' , 'Z'
  };
  public static final int MAX_RADIX = 62;  // TODO: reduce to 36

  private static final SmallRadixAlphabet INSTANCE = new SmallRadixAlphabet();

  public static SmallRadixAlphabet getInstance() {
    return INSTANCE;
  }

  private SmallRadixAlphabet() {

  }

  @Override
  public byte sign() {
    return (byte)'-';
  }

  @Override
  public int maxRadix() {
    return MAX_RADIX;
  }

  @Override
  public byte[] getChars() {
    return CHARS;
  }
}