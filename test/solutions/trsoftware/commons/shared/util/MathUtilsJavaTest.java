/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.shared.GwtIncompatible;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.util.ServerArrayUtils;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static solutions.trsoftware.commons.shared.util.MathUtils.*;

@GwtIncompatible
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class MathUtilsJavaTest extends TestCase {

  public void testInt128ToByteArray() throws Exception {
    final int iterations = 100;
    for (int i = 0; i < iterations; i++) {
      UUID uuid = UUID.randomUUID();
      long msb = uuid.getMostSignificantBits();
      long lsb = uuid.getLeastSignificantBits();
      BigInteger bigInt = new BigInteger(Long.toHexString(msb) + Long.toHexString(lsb), 16);
      // compare BigInt's implementation to the one under test
      byte[] bigIntBytes = bigInt.toByteArray();
      System.out.println("bigIntBytes = " + ServerArrayUtils.toHexString(bigIntBytes, 1, ""));
      // BigInt pads the array with a 0 byte in front to preserve the sign, if the number is positive
      // we do not need this padding
      if (bigIntBytes.length == 17) {
        byte[] bigIntBytesStripped = new byte[bigIntBytes.length - 1];
        System.arraycopy(bigIntBytes, 1, bigIntBytesStripped, 0, bigIntBytesStripped.length);
        bigIntBytes = bigIntBytesStripped;
      }
      // also, BigInt's implementation will not return leading zero bytes, but ours will
      // for the sake of comparison, we pad the BigInt array with leading zeros if needed
      if (bigIntBytes.length < 16) {
        byte[] bigIntBytesPadded = new byte[16];
        System.arraycopy(bigIntBytes, 0, bigIntBytesPadded, 16 - bigIntBytes.length, bigIntBytes.length);
        bigIntBytes = bigIntBytesPadded;
      }
      byte[] ourBytes = int128ToByteArray(msb, lsb);
      System.out.println("ourBytes = " + ServerArrayUtils.toHexString(ourBytes, 1, ""));
      assertTrue(Arrays.equals(bigIntBytes, ourBytes));
    }
  }

  public void testPackUnsignedInt32() throws Exception {
    // check that the values come out in ascending order
    assertEquals(Integer.MIN_VALUE, packUnsignedInt(0));
    assertEquals(Integer.MIN_VALUE + 1, packUnsignedInt(1));
    assertEquals(Integer.MIN_VALUE + 2, packUnsignedInt(2));
    assertEquals(Integer.MAX_VALUE - 2, packUnsignedInt(0xfffffffdL));
    assertEquals(Integer.MAX_VALUE - 1, packUnsignedInt(0xfffffffeL));
    assertEquals(Integer.MAX_VALUE, packUnsignedInt(0xffffffffL));

    // it takes too long to test every 32-bit value,
    // so just we check the endpoints and a million random values
    for (long i : new long[]{0, 1, 2, 3, 0xfffffffcL, 0xfffffffeL, 0xfffffffeL, 0xffffffffL}) {
      assertEquals(i, unsignedInt(packUnsignedInt(i)));
    }

    Random rnd = new Random();
    for (int i = 0; i < 1000000; i++) {
      long value = Math.abs(rnd.nextLong()) % 0xffffffffL;  // put the long in the 32-bit range
      assertEquals(value, unsignedInt(packUnsignedInt(value)));
    }

    // make sure that only args in range 0..0xffffffffL are accepted
    int illegalValuesChecked = 0;
    for (long i = -1000; i < 0xffffffffL + 1000; i++) {
      if (i < 0 || i > 0xffffffffL) {
        final long value = i;
        AssertUtils.assertThrows(IllegalArgumentException.class,
            new Runnable() {
              public void run() {
                packUnsignedInt(value);
              }
            });
        illegalValuesChecked++;
      }
      if (i == 0)
        i = 0xffffffffL;  // skip the valid range
    }
    System.out.println("illegalValuesChecked = " + illegalValuesChecked);
  }

  public void testUnpackUnsignedInt32() throws Exception {
    // check that the values come out in ascending order
    assertEquals(0, unsignedInt(Integer.MIN_VALUE));
    assertEquals(1, unsignedInt(Integer.MIN_VALUE + 1));
    assertEquals(2, unsignedInt(Integer.MIN_VALUE + 2));
    assertEquals(0xfffffffdL, unsignedInt(Integer.MAX_VALUE - 2));
    assertEquals(0xfffffffeL, unsignedInt(Integer.MAX_VALUE - 1));
    assertEquals(0xffffffffL, unsignedInt(Integer.MAX_VALUE));

    // it takes too long to test every 32-bit value,
    // so just we check the endpoints and a million random values
    for (int i : new int[]{Integer.MIN_VALUE, 0, Integer.MAX_VALUE}) {
      assertEquals(i, packUnsignedInt(unsignedInt(i)));
    }
    assertEquals(Integer.MIN_VALUE, packUnsignedInt(unsignedInt(Integer.MIN_VALUE)));
    assertEquals(Integer.MAX_VALUE, packUnsignedInt(unsignedInt(Integer.MAX_VALUE)));

    Random rnd = new Random();
    for (int i = 0; i < 1000000; i++) {
      int value = rnd.nextInt();
      assertEquals(value, packUnsignedInt(unsignedInt(value)));
    }
  }

  public void testPackUnsignedInt8() throws Exception {
    assertEquals(Byte.MIN_VALUE, packUnsignedByte(0));
    assertEquals(Byte.MIN_VALUE + 1, packUnsignedByte(1));
    assertEquals(Byte.MIN_VALUE + 2, packUnsignedByte(2));
    assertEquals(Byte.MAX_VALUE - 2, packUnsignedByte(0xfd));
    assertEquals(Byte.MAX_VALUE - 1, packUnsignedByte(0xfe));
    assertEquals(Byte.MAX_VALUE, packUnsignedByte(0xff));


    int expected = Byte.MIN_VALUE;
    for (int i = 0; i < 256; i++) {
      assertEquals(expected++, packUnsignedByte(i));
    }

    // make sure that only args in range 0..255 are accepted
    int illegalValuesChecked = 0;
    for (int i = -1000; i < 1000; i++) {
      if (i < 0 || i > 255) {
        final int value = i;
        AssertUtils.assertThrows(IllegalArgumentException.class,
            new Runnable() {
              public void run() {
                packUnsignedByte(value);
              }
            });
        illegalValuesChecked++;
      }
    }
    System.out.println("illegalValuesChecked = " + illegalValuesChecked);
  }

  public void testUnpackUnsignedInt8() throws Exception {
    assertEquals(0, unsignedByte(Byte.MIN_VALUE));
    assertEquals(1, unsignedByte((byte)(Byte.MIN_VALUE + 1)));
    assertEquals(2, unsignedByte((byte)(Byte.MIN_VALUE + 2)));
    assertEquals(0xfd, unsignedByte((byte)(Byte.MAX_VALUE - 2)));
    assertEquals(0xfe, unsignedByte((byte)(Byte.MAX_VALUE - 1)));
    assertEquals(0xff, unsignedByte(Byte.MAX_VALUE));

    int expected = 0;
    for (int i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
      assertEquals(expected++, unsignedByte((byte)i));
    }
    for (int i = 0; i < 256; i++) {
      assertEquals(i, unsignedByte(packUnsignedByte(i)));
    }
  }

  public void testFactorial() throws Exception {
    assertEquals(1, factorial(0));
    assertEquals(1, factorial(1));
    assertEquals(2, factorial(2));
    assertEquals(6, factorial(3));
    assertEquals(24, factorial(4));
    assertEquals(120, factorial(5));
    assertEquals(1307674368000L, factorial(15));
    // 20! is the largest factorial that can be represented by a long
    assertEquals(2432902008176640000L, factorial(20));

    // factorial is undefined for negative numbers
    AssertUtils.assertThrows(IllegalArgumentException.class, (Runnable)() -> factorial(-1));
    AssertUtils.assertThrows(IllegalArgumentException.class, (Runnable)() -> factorial(-12345));
    // 21! overflows a 64-bit long
    AssertUtils.assertThrows(ArithmeticException.class, (Runnable)() -> factorial(21));
  }

  public void testNPr() throws Exception {
    // first check a few values manually, then automate the rest using the formula
    assertEquals(1, nPr(0, 0));
    assertEquals(1, nPr(1, 0));
    assertEquals(1, nPr(1, 1));
    assertEquals(2, nPr(2, 1));
    assertEquals(2, nPr(2, 2));

    assertEquals(1, nPr(5, 0));
    assertEquals(5, nPr(5, 1));
    assertEquals(20, nPr(5, 2));
    assertEquals(60, nPr(5, 3));
    assertEquals(120, nPr(5, 4));
    assertEquals(120, nPr(5, 5));

    for (int n = 0; n < 20; n++)
      for (int r = 0; r <= n; r++)
        assertEquals("nPr(" + n + ", " + r + ")", factorial(n) / factorial(n - r), nPr(n, r));
  }

  public void testNCr() throws Exception {
    // first check a few values manually, then automate the rest using the formula
    assertEquals(1, nCr(0, 0));
    assertEquals(1, nCr(1, 0));
    assertEquals(1, nCr(1, 1));
    assertEquals(2, nCr(2, 1));
    assertEquals(1, nCr(2, 2));

    assertEquals(1, nCr(5, 0));
    assertEquals(5, nCr(5, 1));
    assertEquals(10, nCr(5, 2));
    assertEquals(10, nCr(5, 3));
    assertEquals(5, nCr(5, 4));
    assertEquals(1, nCr(5, 5));


    for (int n = 0; n < 20; n++)
      for (int r = 0; r <= n; r++)
        assertEquals("nCr(" + n + ", " + r + ")", factorial(n) / (factorial(r) * factorial(n - r)), nCr(n, r));
  }

  public void testFibonacci() throws Exception {
    // from http://oeis.org/A000045
    int[] expectedSequence = new int[]{0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, 17711, 28657, 46368, 75025, 121393, 196418, 317811, 514229, 832040, 1346269, 2178309, 3524578, 5702887, 9227465, 14930352, 24157817, 39088169};
    for (int i = 0; i < expectedSequence.length; i++) {
      checkFibonacci(i, expectedSequence[i]);
    }
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        fibonacci(-1);
      }
    });
  }

  private void checkFibonacci(int i, int expected) {
    System.out.println("Checking fibonacci(" + i + ") == " + expected);
    assertEquals(expected, fibonacci(i));
  }

  public void testDecimalCharCount() throws Exception {
    // we just need to test a few corner cases to make sure this method words
    int[] cases = new int[]{
        Integer.MIN_VALUE,Integer.MIN_VALUE+1,-1001,-1000,-999,-101,-100,-99,-11,-10,-9,-1,
        0,1,9,10,11,99,100,101,999,1000,1001,Integer.MAX_VALUE-1,Integer.MAX_VALUE};
    for (int n : cases) {
      String str = Integer.toString(n);
      assertEquals(str, str.length(), decimalCharCount(n));
    }
  }

  public void testGetFractionalPart() throws Exception {
    assertEquals(.456, getFractionalPart(3.456), .001);
    assertEquals(0d, getFractionalPart(3.0));
    assertEquals(0d, getFractionalPart(Integer.MAX_VALUE));
    assertEquals(.12345, getFractionalPart(Integer.MAX_VALUE + .12345), .00001);
  }

  public void testEqual() throws Exception {
    // 1) test some values that are not finite
    assertTrue(equal(0, 0, Double.MIN_VALUE));
    doTestEqualOnNonFiniteValues(Double.MIN_VALUE);
    for (double delta = 0; delta < 1; delta+=.000001) {
      doTestEqualOnNonFiniteValues(delta);
      assertTrue(equal(0, 0, delta));
    }
    double delta = .000001;
    // 2) test the example given in the method javadoc: 32450.0 / 3.75 * 3.75 resulting in 32450.000000000004, whereas we would expect 32450.0
    assertTrue(equal(32450.0, 32450.0 / 3.75 * 3.75, delta));
    // test using a negative delta (method should use the absolute value of the delta)
    assertTrue(equal(32450.0, 32450.0 / 3.75 * 3.75, -delta));
    // 3) now test a million random finite values
    double[] nonFiniteValues = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN};
    Random rnd = new Random();
    for (int i = 0; i < 1_000_000; i++) {
      double x = rnd.nextDouble();
      assertTrue(equal(x, x, delta));
      double y = rnd.nextDouble();
      if (y != 0)
        assertTrue(equal(x, x / y * y, delta));
      for (double nonFiniteValue : nonFiniteValues) {
        assertFalse(equal(x, nonFiniteValue, delta));
        assertFalse(equal(nonFiniteValue, x, delta));
      }
    }
  }

  private static void doTestEqualOnNonFiniteValues(double delta) {
    assertTrue(equal(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, delta));
    assertTrue(equal(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, delta));
    assertFalse(equal(Double.NaN, Double.NaN, delta));  // NaN != x for any value of x, including NaN
    assertFalse(equal(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, delta));
    assertFalse(equal(Double.POSITIVE_INFINITY, Double.NaN, delta));
    assertFalse(equal(Double.NaN, Double.POSITIVE_INFINITY, delta));
    assertFalse(equal(Double.NEGATIVE_INFINITY, Double.NaN, delta));
    assertFalse(equal(Double.NaN, Double.NEGATIVE_INFINITY, delta));
  }

  public void testFloorMod() throws Exception {
    // 1) compare against some manually-generated results of using Python's % operator
    {
      // test some positive values of the divisor
      {
        int divisor = 10;
        assertEquals(3, floorMod(3, divisor));
        assertEquals(1, floorMod(11, divisor));
        assertEquals(7, floorMod(-3, divisor));
        assertEquals(9, floorMod(-11, divisor));
      }
      // test some negative values of the divisor
      {
        int divisor = -10;
        assertEquals(-7, floorMod(3, divisor));
        assertEquals(-9, floorMod(11, divisor));
        assertEquals(-3, floorMod(-3, divisor));
        assertEquals(-1, floorMod(-11, divisor));
      }
    }
    // 2) compare against Java 8's new Math.floorMod function
    {
      int n = 500;
      for (int x = -n; x < n; x++) {
        for (int divisor = -n; divisor < n; divisor++) {
          if (divisor != 0) {
            // if using an older GWT version, this code won't compile client-side,
            // since Math.floorMod didn't get added to JRE emulation until recently
            assertEquals(Math.floorMod(x, divisor), floorMod(x, divisor));
          }
          else {
            int finalX = x;
            int finalDivisor = divisor;
            AssertUtils.assertThrows(ArithmeticException.class, (Runnable)() -> {
              int resultMod0 = floorMod(finalX, finalDivisor);
            });
          }
        }
      }
    }
  }

  public void testRestrict() throws Exception {
    // double version
    assertEquals(5d, restrict(5d, 0d, 10d));
    assertEquals(0d, restrict(-.01d, 0d, 10d));
    assertEquals(10d, restrict(10.01d, 0d, 10d));
    // float version
    assertEquals(5f, restrict(5f, 0f, 10f));
    assertEquals(0f, restrict(-.01f, 0f, 10f));
    assertEquals(10f, restrict(10.01f, 0f, 10f));
    // int version
    assertEquals(5, restrict(5, -10, 10));
    assertEquals(-10, restrict(-11, -10, 10));
    assertEquals(10, restrict(11, -10, 10));
    // long version
    assertEquals(5L, restrict(5L, -10L, 10L));
    assertEquals(-10L, restrict(-11L, -10L, 10L));
    assertEquals(10L, restrict(11L, -10L, 10L));
  }


  public void testSignum() throws Exception {
    Map<Integer, int[]> testCases = new MapDecorator<Integer, int[]>(new LinkedHashMap<Integer, int[]>())
        .put(0, new int[]{0})
        .put(-1, new int[]{-123, -1})
        .put(1, new int[]{123, 1})
        .getMap();
    for (Integer expected : testCases.keySet()) {
      int[] args = testCases.get(expected);
      for (int arg : args) {
        assertEquals((int)expected, signum(arg));
        assertEquals((int)expected, signum((long)arg));
      }
    }
  }

  public void testIsPowerOf2() throws Exception {
    // 1) compute all the actual powers of 2 that can be represented as int
    Set<Integer> powersOf2 = new HashSet<>();
    for (long i = 1; i <= Integer.MAX_VALUE; i*=2) {  // using long to avoid overflow
      // test all ints between i and the next power of 2
      int p = (int)i;
      assertTrue(isPowerOf2(p));
      powersOf2.add(p);
    }
    // 2) test some specific negative cases manually
    assertFalse(isPowerOf2(0));
    assertFalse(isPowerOf2(-1));
    assertFalse(isPowerOf2(-2));
    assertFalse(isPowerOf2(-3));
    assertFalse(isPowerOf2(-4));
    assertFalse(isPowerOf2(Integer.MIN_VALUE));
    // 3) now test some random values (too slow to iterate over possible ints)
    for (int i = 0; i < 100_000; i++) {
      int x = RandomUtils.rnd().nextInt();
      assertEquals(powersOf2.contains(x), isPowerOf2(x));
    }
  }

  @GwtIncompatible
  public void testRound() throws Exception {
    // very basic test
    assertEquals(1.0, round(0.5, 0));
    assertEquals(-1.0, round(-0.5, 0));
    assertEquals(2.67, round(2.675, 2));  // example of float representation limitation (from https://docs.python.org/2.7/library/functions.html#round)
    assertEquals(BigDecimal.valueOf(2.68), round(BigDecimal.valueOf(2.675), 2));  // same input produces a more-accurate result with BigDecimal instead of double

    // some random values
    Random rnd = new Random(1);
    int n = 1000;
//    int n = 100;
    double[] inputs = new double[n];
    for (int i = 0; i < inputs.length; i++) {
      inputs[i] = RandomUtils.nextDoubleInRange(rnd,0, 300);
    }


    Multimap<String, String> warnings = LinkedHashMultimap.create();
    int iMax = 5;
//    SharedNumberFormat[] formats = ArrayUtils.fill(new SharedNumberFormat[iMax], i -> new SharedNumberFormat(0, 1, i, false));
    SharedNumberFormat[] formats = ArrayUtils.fill(new SharedNumberFormat[iMax], SharedNumberFormat::new);

    System.out.println("---- Testing round(double, int) ----");
    for (int i = 0; i < iMax; i++) {
      SharedNumberFormat fmt = formats[i];
      for (double x : inputs) {
        double rx = round(x, i);
        String fx = fmt.format(x);
        String msg = String.format("round(double, %d): %20s -> %s", i, x, rx);
        if (!fx.equals(fmt.format(rx)) || !MathUtils.equal(fmt.parse(fx), rx, Math.pow(10, -i))) {
          warnings.put("double", msg);
          msg += " -- WARNING";
        }
        System.out.println(msg);
      }
    }

    System.out.println("\n\n---- Testing round(BigDecimal, int) ----");
    // repeat the same test with BigDecimals
    for (int i = 0; i < iMax; i++) {
      SharedNumberFormat fmt = formats[i];
      for (double input : inputs) {
        BigDecimal x = BigDecimal.valueOf(input);
        BigDecimal rx = round(x, i);
        String fx = fmt.format(x);
        String msg = String.format("round(BigDecimal, %d): %20s -> %s", i, x, rx);
        if (!fx.equals(fmt.format(rx))) {
          warnings.put("BigDecimal", msg);
          msg += " -- WARNING";
        }
        System.out.println(msg);
      }
    }

    assertEquals(warnings.toString(), 0, warnings.size());
  }
}