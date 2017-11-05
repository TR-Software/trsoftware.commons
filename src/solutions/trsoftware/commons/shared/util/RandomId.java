/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.util;

import java.math.BigInteger;
import java.util.Random;

/**
 * Generates random ID strings by taking the base36-encoded version of a next 48-bit non-negative random integer.
 * The reason we chose base 36 as our string encoding format is because that's the largest base that produces a result
 * containing only chars [a-z0-9] (this is codified by {@link Character#MAX_RADIX}).  The reason we're using 48-bit
 * numbers is because (1) the class {@code Random} uses a seed with only 48 bits, and (2) the base-36 string encoding
 * of a 48-bit number has length in the range [8,10], with the median being 10.  Basically this makes for a large-enough
 * space to make collisions very unlikely but also small-enough to be communicated by humans.
 *
 *
 * @author Alex, 3/18/2016
 */
public class RandomId {

  public static final int BITS = 48;
  public static final int RADIX = 36;

  private RandomId() {
  }


  /**
   * @param rnd The pseudo-random number generator to use; in server-side code this could be an instance of
   * {@link java.security.SecureRandom} (GWT only emulates the standard {@link Random} class).
   * @return The base 36 encoding of the next 48-bit non-negative random integer from the given generator.
   */
  public static String nextString(Random rnd) {
    return new BigInteger(BITS, rnd).toString(RADIX);
  }

  /**
   * @param rnd The pseudo-random number generator to use; in server-side code this could be an instance of
   * {@link java.security.SecureRandom} (GWT only emulates the standard {@link Random} class).
   * @return The next 48-bit non-negative random integer from the given generator.
   */
  public static long nextLong(Random rnd) {
    byte[] bytes = new byte[(BITS+7)/8];
    rnd.nextBytes(bytes);
    long result = 0;
    // mask the bits into the result
    for (int i = 0; i < bytes.length; i++)
      result |= (long)MathUtils.unsignedByte(bytes[i]) << 8*i;
    // lastly mask out any extra bits (just in case the value of the BITS constant ever changes to a number that's not divisible by 8)
    result &= ((1L << BITS) - 1);
    return result;
  }
}
