package solutions.trsoftware.commons.server.util;
/**
 *
 * Date: Oct 23, 2008
 * Time: 5:06:13 PM
 * @author Alex
 */

import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.client.util.stats.NumberSample;
import solutions.trsoftware.commons.server.testutil.PerformanceComparison;
import junit.framework.TestCase;

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