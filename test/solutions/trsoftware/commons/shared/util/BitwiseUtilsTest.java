package solutions.trsoftware.commons.shared.util;

import com.google.common.primitives.Bytes;
import junit.framework.TestCase;

import java.util.Arrays;

import static com.google.common.base.Strings.lenientFormat;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.BitwiseUtils.*;

/**
 * @author Alex
 * @since 1/17/2019
 */
public class BitwiseUtilsTest extends TestCase {

  // TODO: create a GWTTestCase version

  public void testTestBit() throws Exception {
    // int version of method
    {
      // test some examples manually
      assertFalse(testBit(0b10110, 0));
      assertTrue(testBit(0b10110, 1));
      assertTrue(testBit(0b10110, 2));
      assertFalse(testBit(0b10110, 3));
      assertTrue(testBit(0b10110, 4));
      // now test some other examples in bulk
      int width = 32;
      for (int i = 0; i < width; i++) {
        assertTrue(testBit(-1, i));  // -1 has all 1-bits (in 2's complement binary)
        assertFalse(testBit(0, i));
        assertTrue(testBit(1 << i, i));
        assertFalse(testBit(0b10 << i, i));
        assertFalse(testBit(0b101 << (i - 1), i));
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> testBit(0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> testBit(0b10110, width));
    }
    // long version of method
    {
      // test some examples manually
      assertFalse(testBit(0b10110L, 0));
      assertTrue(testBit(0b10110L, 1));
      assertTrue(testBit(0b10110L, 2));
      assertFalse(testBit(0b10110L, 3));
      assertTrue(testBit(0b10110L, 4));
      // now test some other examples in bulk
      int width = 64;
      for (int i = 0; i < width; i++) {
        assertTrue(testBit(-1L, i));  // -1 has all 1-bits (in 2's complement binary)
        assertFalse(testBit(0L, i));
        assertTrue(testBit(1L << i, i));
        assertFalse(testBit(0b10L << i, i));
        assertFalse(testBit(0b101L << (i - 1), i));
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> testBit(0b10110L, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> testBit(0b10110L, width));
    }
    // short version of method
    {
      // test some examples manually
      assertFalse(testBit((short)0b10110, 0));
      assertTrue(testBit((short)0b10110, 1));
      assertTrue(testBit((short)0b10110, 2));
      assertFalse(testBit((short)0b10110, 3));
      assertTrue(testBit((short)0b10110, 4));
      // now test some other examples in bulk
      int width = 16;
      for (int i = 0; i < width; i++) {
        assertTrue(testBit((short)-1, i));  // -1 has all 1-bits (in 2's complement binary)
        assertFalse(testBit((short)0, i));
        assertTrue(testBit((short)1 << i, i));
        assertFalse(testBit((short)0b10 << i, i));
        assertFalse(testBit((short)0b101 << (i - 1), i));
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> testBit((short)0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> testBit((short)0b10110, width));
    }
    // byte version of method
    {
      // test some examples manually
      assertFalse(testBit((byte)0b10110, 0));
      assertTrue(testBit((byte)0b10110, 1));
      assertTrue(testBit((byte)0b10110, 2));
      assertFalse(testBit((byte)0b10110, 3));
      assertTrue(testBit((byte)0b10110, 4));
      // now test some other examples in bulk
      int width = 8;
      for (int i = 0; i < width; i++) {
        assertTrue(testBit((byte)-1, i));  // -1 has all 1-bits (in 2's complement binary)
        assertFalse(testBit((byte)0, i));
        assertTrue(testBit((byte)1 << i, i));
        assertFalse(testBit((byte)0b10 << i, i));
        assertFalse(testBit((byte)0b101 << (i - 1), i));
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> testBit((byte)0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> testBit((byte)0b10110, width));
    }
  }


  public void testClearBit() throws Exception {
    // int version of method
    {
      // test some examples manually
      assertEquals(0b10110, clearBit(0b10110, 0));
      assertEquals(0b10100, clearBit(0b10110, 1));
      assertEquals(0b00110, clearBit(0b10110, 4));

      // now test some other examples in bulk
      int width = Integer.SIZE;
      for (int i = 0; i < width; i++) {
        assertEquals(0, clearBit(0, i));
        assertEquals(~(1 << i), clearBit(-1, i)); // -1 has all 1-bits (in 2's complement binary)
        assertFalse(testBit(clearBit(-1, i), i));
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> clearBit(0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> clearBit(0b10110, width));
    }
    // long version of method
    {
      // test some examples manually
      assertEquals(0b10110L, clearBit(0b10110, 0));
      assertEquals(0b10100L, clearBit(0b10110, 1));
      assertEquals(0b00110L, clearBit(0b10110, 4));

      // now test some other examples in bulk
      int width = Long.SIZE;
      for (int i = 0; i < width; i++) {
        assertEquals(0L, clearBit(0L, i));
        assertEquals(~(1L << i), clearBit(-1L, i)); // -1 has all 1-bits (in 2's complement binary)
        assertFalse(testBit(clearBit(-1L, i), i));
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> clearBit(0b10110L, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> clearBit(0b10110L, width));
    }
    // short version of method
    {
      // test some examples manually
      assertEquals((short)0b10110, clearBit((short)0b10110, 0));
      assertEquals((short)0b10100, clearBit((short)0b10110, 1));
      assertEquals((short)0b00110, clearBit((short)0b10110, 4));

      // now test some other examples in bulk
      int width = Short.SIZE;
      for (int i = 0; i < width; i++) {
        assertEquals((short)0, clearBit((short)0, i));
        assertEquals((short)~(1 << i), clearBit((short)-1, i)); // -1 has all 1-bits (in 2's complement binary)
        assertFalse(testBit(clearBit((short)-1, i), i));
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> clearBit((short)0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> clearBit((short)0b10110, width));
    }
    // byte version of method
    {
      // test some examples manually
      assertEquals((byte)0b10110, clearBit((byte)0b10110, 0));
      assertEquals((byte)0b10100, clearBit((byte)0b10110, 1));
      assertEquals((byte)0b00110, clearBit((byte)0b10110, 4));

      // now test some other examples in bulk
      int width = Byte.SIZE;
      for (int i = 0; i < width; i++) {
        assertEquals((byte)0, clearBit((byte)0, i));
        assertEquals((byte)~(1 << i), clearBit((byte)-1, i)); // -1 has all 1-bits (in 2's complement binary)
        assertFalse(testBit(clearBit((byte)-1, i), i));
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> clearBit((byte)0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> clearBit((byte)0b10110, width));
    }
    
  }

  public void testSetBit() throws Exception {
    // int version of method
    {
      // test some examples manually
      assertEquals(0b10111, setBit(0b10110, 0));
      assertEquals(0b10110, setBit(0b10110, 1));
      assertEquals(0b10110, setBit(0b10110, 2));
      assertEquals(0b11110, setBit(0b10110, 3));

      // now test some other examples in bulk
      int width = Integer.SIZE;
      for (int i = 0; i < width; i++) {
        assertEquals(1 << i, setBit(0, i));
        assertEquals(-1, setBit(-1, i)); // -1 has all 1-bits (in 2's complement binary)
        // test some random numbers
        for (int j = 0; j < 10; j++) {
          int x = RandomUtils.rnd().nextInt();
          int xSi = setBit(x, i);
          assertTrue(testBit(xSi, i));
          assertTrue(testBit(setBit(clearBit(x, i), i), i));
        }
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> setBit(0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> setBit(0b10110, width));
    }
    // long version of method
    {
      // test some examples manually
      assertEquals(0b10111L, setBit(0b10110L, 0));
      assertEquals(0b10110L, setBit(0b10110L, 1));
      assertEquals(0b10110L, setBit(0b10110L, 2));
      assertEquals(0b11110L, setBit(0b10110L, 3));

      // now test some other examples in bulk
      int width = Long.SIZE;
      for (int i = 0; i < width; i++) {
        assertEquals(1L << i, setBit(0L, i));
        assertEquals(-1L, setBit(-1L, i)); // -1 has all 1-bits (in 2's complement binary)
        // test some random numbers
        for (int j = 0; j < 10; j++) {
          long x = RandomUtils.rnd().nextLong();
          long xSi = setBit(x, i);
          assertTrue(testBit(xSi, i));
          assertTrue(testBit(setBit(clearBit(x, i), i), i));
        }
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> setBit(0b10110L, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> setBit(0b10110L, width));
    }
    // short version of method
    {
      // test some examples manually
      assertEquals((short)0b10111, setBit((short)0b10110, 0));
      assertEquals((short)0b10110, setBit((short)0b10110, 1));
      assertEquals((short)0b10110, setBit((short)0b10110, 2));
      assertEquals((short)0b11110, setBit((short)0b10110, 3));

      // now test some other examples in bulk
      int width = Short.SIZE;
      for (int i = 0; i < width; i++) {
        assertEquals((short)(1 << i), setBit((short)0, i));
        assertEquals((short)-1, setBit((short)-1, i)); // -1 has all 1-bits (in 2's complement binary)
        // test some random numbers
        for (int j = 0; j < 10; j++) {
          short x = (short)RandomUtils.rnd().nextInt();
          short xSi = setBit(x, i);
          assertTrue(testBit(xSi, i));
          assertTrue(testBit(setBit(clearBit(x, i), i), i));
        }
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> setBit((short)0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> setBit((short)0b10110, width));
    }
    // byte version of method
    {
      // test some examples manually
      assertEquals((byte)0b10111, setBit((byte)0b10110, 0));
      assertEquals((byte)0b10110, setBit((byte)0b10110, 1));
      assertEquals((byte)0b10110, setBit((byte)0b10110, 2));
      assertEquals((byte)0b11110, setBit((byte)0b10110, 3));

      // now test some other examples in bulk
      int width = Byte.SIZE;
      for (int i = 0; i < width; i++) {
        assertEquals((byte)(1 << i), setBit((byte)0, i));
        assertEquals((byte)-1, setBit((byte)-1, i)); // -1 has all 1-bits (in 2's complement binary)
        // test some random numbers
        for (int j = 0; j < 10; j++) {
          byte x = (byte)RandomUtils.rnd().nextInt();
          byte xSi = setBit(x, i);
          assertTrue(testBit(xSi, i));
          assertTrue(testBit(setBit(clearBit(x, i), i), i));
        }
      }
      // lastly, test the bounds checking
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> setBit((byte)0b10110, -1));
      assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> setBit((byte)0b10110, width));
    }
  }

  public void testGetByte() {
    long x = 0xf7f6f5f4_73727170L;
    assertEquals((byte)0xf7, getByte(x, 7));
    assertEquals((byte)0xf6, getByte(x, 6));
    assertEquals((byte)0xf5, getByte(x, 5));
    assertEquals((byte)0xf4, getByte(x, 4));
    assertEquals((byte)0x73, getByte(x, 3));
    assertEquals((byte)0x72, getByte(x, 2));
    assertEquals((byte)0x71, getByte(x, 1));
    assertEquals((byte)0x70, getByte(x, 0));

    // TODO: test some invalid args
  }

  public void testLongToByteArray() {
    long x = 0xf7f6f5f4_73727170L;
    byte[] bigEndianBytes = {(byte)0xf7, (byte)0xf6, (byte)0xf5, (byte)0xf4, (byte)0x73, (byte)0x72, (byte)0x71, (byte)0x70};
    byte[] littleEndianBytes = reversedCopy(bigEndianBytes);

    // test longToByteArray(long): big-endian
    assertArraysEqual(bigEndianBytes, longToByteArray(x, true));

    // test longToByteArray(long, boolean): big-endian or little-endian
    assertArraysEqual(bigEndianBytes, longToByteArray(x, true));
    assertArraysEqual(littleEndianBytes, longToByteArray(x, false));

    // test longToByteArray(long, byte[], int, boolean): takes an output array and outputOffset
    byte[] outputB = filledByteArray(12, (byte)0xff);
    byte[] expectedB = Arrays.copyOf(outputB, outputB.length);
    System.arraycopy(bigEndianBytes, 0, expectedB, 2, bigEndianBytes.length);
    longToByteArray(x, outputB, 2, true);
    assertArraysEqual(expectedB, outputB);

    byte[] outputL = filledByteArray(12, (byte)0xff);
    byte[] expectedL = Arrays.copyOf(outputL, outputL.length);
    System.arraycopy(littleEndianBytes, 0, expectedL, 4, littleEndianBytes.length);
    longToByteArray(x, outputL, 4, false);
    assertArraysEqual(expectedL, outputL);

    // TODO: test some invalid args
  }

  public void testSetByte() throws Exception {
    long x = 0xF7F6F5F4_73727170L;
    byte b = (byte)0xee;
    int i = 6;
    long ret = setByte(x, b, i);
    System.out.println(lenientFormat("setByte(0x%s, 0x%s, %s) -> 0x%s", Long.toHexString(x), StringUtils.byteToHex(b), i, Long.toHexString(ret)));
    assertEquals(0xF7F6F5F4_737271EEL, setByte(x, (byte)0xEE, 0));
    assertEquals(0xF7F6F5F4_7372EE70L, setByte(x, (byte)0xEE, 1));
    assertEquals(0xF7F6F5F4_73EE7170L, setByte(x, (byte)0xEE, 2));
    assertEquals(0xF7F6F5F4_EE727170L, setByte(x, (byte)0xEE, 3));
    assertEquals(0xF7F6F5EE_73727170L, setByte(x, (byte)0xEE, 4));
    assertEquals(0xF7F6EEF4_73727170L, setByte(x, (byte)0xEE, 5));
    assertEquals(0xF7EEF5F4_73727170L, setByte(x, (byte)0xEE, 6));
    assertEquals(0xEEF6F5F4_73727170L, setByte(x, (byte)0xEE, 7));

    // TODO: test some invalid args
  }

  public void testByteArrayToLong() throws Exception {
    long x = 0xf7f6f5f4_73727170L;
    byte[] bigEndianBytes = {(byte)0xf7, (byte)0xf6, (byte)0xf5, (byte)0xf4, (byte)0x73, (byte)0x72, (byte)0x71, (byte)0x70};
    byte[] littleEndianBytes = reversedCopy(bigEndianBytes);
    // longFromByteArray(byte...)
    assertEquals(x, longFromByteArray(bigEndianBytes));
    // longFromByteArray(byte[], boolean)
    assertEquals(x, longFromByteArray(bigEndianBytes, true));
    assertEquals(x, longFromByteArray(littleEndianBytes, false));

    // longFromByteArray(byte[], int, int, boolean)
    // big-endian
    assertEquals(0xf7f6_f5f47372L,
        longFromByteArray(bigEndianBytes, 0, 6, true));
    assertEquals(0xf6f5f47372L,
        longFromByteArray(bigEndianBytes, 1, 5, true));
    // little-endian
    assertEquals(0xf5f4_73727170L,
        longFromByteArray(littleEndianBytes, 0, 6, false));
    assertEquals(0xf5f4_737271L,
        longFromByteArray(littleEndianBytes, 1, 5, false));

    // TODO: test some invalid args
  }

  /**
   * Returns a new byte array of the specified size filled with the specified byte.
   * @param len length of array to create
   * @param fill byte to fill with
   * @return new byte array containing {@code len} occurrences of {@code fill}
   */
  private static byte[] filledByteArray(int len, byte fill) {
    // TODO: extract method to ArrayUtils?
    byte[] ret = new byte[len];
    Arrays.fill(ret, fill);
    return ret;
  }

  private static byte[] reversedCopy(byte[] bytes) {
    // TODO: extract method to ArrayUtils?
    byte[] ret = Arrays.copyOf(bytes, bytes.length);
    Bytes.reverse(ret);
    return ret;
  }
}