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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.server.testutil.PerformanceComparison;
import solutions.trsoftware.commons.shared.util.stats.NumberSample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class NumberRadixEncoderTest extends TestCase {

  public void testCompareLengthsByRadix() throws Exception {
    // displays the stats for lengths of non-negative numbers encoded to strings by various radixes
    int iterations = 1000;
    Random rnd = new Random();
    for (int bits : new int[]{32, 48, 64, 128}) {
      for (int radix : new int[]{10, 16, Character.MAX_RADIX, 62, 64}) {
        NumberSample<Integer> lengths = new NumberSample<Integer>(iterations);
        ArrayList<String> examples = new ArrayList<String>();
        for (int i = 0; i < iterations; i++) {
          BigInteger value = new BigInteger(bits, rnd);
          String encodedValue;
          if (radix <= Character.MAX_RADIX)
            encodedValue = value.toString(radix);
          else if (radix == 62 && bits < 64)
            encodedValue = NumberRadixEncoder.toStringBase62(value.longValue());
          else if (radix == 64 && bits < 64)
            encodedValue = NumberRadixEncoder.toStringBase64(value.longValue());
          else
            encodedValue = "N/A";
          if (examples.size() < 10)
            examples.add(encodedValue);
          lengths.update(encodedValue.length());
        }
        System.out.printf("Base-%d encoded strings of %d-bit random numbers:%n  examples: %s%n  stats: %s%n%n",
            radix, bits, examples, lengths.summarize());
      }
    }

  }

  /** Compares the speed of b64 vs. b62 encoding */
  @Slow
  public void testEncodingSpeedFor128BitInts() throws Exception {
    final Random rnd = new Random();

    PerformanceComparison.compare(
        new Runnable() {
          public void run() {
            NumberRadixEncoder.toStringBase62(rnd.nextLong());
            NumberRadixEncoder.toStringBase62(rnd.nextLong());
          }
        }, "b62 encoding",
        new Runnable() {
          public void run() {
            NumberRadixEncoder.toStringBase64(rnd.nextLong(), rnd.nextLong());
          }
        }, "b64 encoding", 1000000);
  }

  /** Compares the speed of b64 vs. b62 encoding */
  @Slow
  public void testEncodingSpeedFor64BitInts() throws Exception {
    final Random rnd = new Random();

    PerformanceComparison.compare(
        new Runnable() {
          public void run() {
            NumberRadixEncoder.toStringBase62(rnd.nextLong());
          }
        }, "b62 encoding",
        new Runnable() {
          public void run() {
            NumberRadixEncoder.toStringBase64(rnd.nextLong());
          }
        }, "b64 encoding", 1000000);
  }
}