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

package solutions.trsoftware.commons.shared.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Utilities for bitwise operations on binary numbers.
 *
 * @see java.util.BitSet
 * @author Alex
 * @since 1/17/2019
 */
public class BitwiseUtils {


  /**
   * Tests whether a particular bit is set ({@code == 1}) in the given {@code int} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to test (0-indexed; should be in range {@code [0, 31]})
   * @return {@code true} iff the {@code i}-th bit of {@code bitField} is set
   */
  public static boolean testBit(int bitField, int i) {
    checkBitIndex(i, Integer.SIZE);
    return (bitField & (1 << i)) != 0;
  }

  /**
   * Tests whether a particular bit is set ({@code == 1}) in the given {@code short} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to test (0-indexed; should be in range {@code [0, 15]})
   * @return {@code true} iff the {@code i}-th bit of {@code bitField} is set
   */
  public static boolean testBit(short bitField, int i) {
    checkBitIndex(i, Short.SIZE);
    return (bitField & (1 << i)) != 0;
  }

  /**
   * Tests whether a particular bit is set ({@code == 1}) in the given {@code byte} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to test (0-indexed; should be in range {@code [0, 7]})
   * @return {@code true} iff the {@code i}-th bit of {@code bitField} is set
   */
  public static boolean testBit(byte bitField, int i) {
    checkBitIndex(i, Byte.SIZE);
    return (bitField & (1 << i)) != 0;
  }

  /**
   * Tests whether a particular bit is set ({@code == 1}) in the given {@code long} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to test (0-indexed; should be in range {@code [0, 63]})
   * @return {@code true} iff the {@code i}-th bit of {@code bitField} is set
   */
  public static boolean testBit(long bitField, int i) {
    checkBitIndex(i, Long.SIZE);
    return (bitField & (1L << i)) != 0;
  }
  
  /**
   * Clears a particular bit (by setting it to {@code 0}) in the given {@code int} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to clear (0-indexed; should be in range {@code [0, 31]})
   * @return the value of {@code bitField} transformed by setting its {@code i}-th bit to {@code 0}
   */
  public static int clearBit(int bitField, int i) {
    checkBitIndex(i, Integer.SIZE);
    return bitField & ~(1 << i);
  }
  
  /**
   * Clears a particular bit (by setting it to {@code 0}) in the given {@code long} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to clear (0-indexed; should be in range {@code [0, 63]})
   * @return the value of {@code bitField} transformed by setting its {@code i}-th bit to {@code 0}
   */
  public static long clearBit(long bitField, int i) {
    checkBitIndex(i, Long.SIZE);
    return bitField & ~((long)1 << i);
  }
  
  /**
   * Clears a particular bit (by setting it to {@code 0}) in the given {@code short} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to clear (0-indexed; should be in range {@code [0, 15]})
   * @return the value of {@code bitField} transformed by setting its {@code i}-th bit to {@code 0}
   */
  public static short clearBit(short bitField, int i) {
    checkBitIndex(i, Short.SIZE);
    return (short)(bitField & ~(1 << i));
  }
  
  /**
   * Clears a particular bit (by setting it to {@code 0}) in the given {@code byte} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to clear (0-indexed; should be in range {@code [0, 7]})
   * @return the value of {@code bitField} transformed by setting its {@code i}-th bit to {@code 0}
   */
  public static byte clearBit(byte bitField, int i) {
    checkBitIndex(i, Byte.SIZE);
    return (byte)(bitField & ~(1 << i));
  }
  
  
  /**
   * Turns on a particular bit (by setting it to {@code 1}) in the given {@code int} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to set (0-indexed; should be in range {@code [0, 31]})
   * @return the value of {@code bitField} transformed by setting its {@code i}-th bit to {@code 1}
   */
  public static int setBit(int bitField, int i) {
    checkBitIndex(i, Integer.SIZE);
    return bitField | (1 << i);
  }
  
  /**
   * Turns on a particular bit (by setting it to {@code 1}) in the given {@code long} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to set (0-indexed; should be in range {@code [0, 63]})
   * @return the value of {@code bitField} transformed by setting its {@code i}-th bit to {@code 1}
   */
  public static long setBit(long bitField, int i) {
    checkBitIndex(i, Long.SIZE);
    return bitField | ((long)1 << i);
  }
  
  /**
   * Turns on a particular bit (by setting it to {@code 1}) in the given {@code short} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to set (0-indexed; should be in range {@code [0, 15]})
   * @return the value of {@code bitField} transformed by setting its {@code i}-th bit to {@code 1}
   */
  public static short setBit(short bitField, int i) {
    checkBitIndex(i, Short.SIZE);
    return (short)(bitField | ((short)1 << i));
  }
  
  /**
   * Turns on a particular bit (by setting it to {@code 1}) in the given {@code byte} field.
   *
   * @param bitField the source of the bits
   * @param i the bit index to set (0-indexed; should be in range {@code [0, 7]})
   * @return the value of {@code bitField} transformed by setting its {@code i}-th bit to {@code 1}
   */
  public static byte setBit(byte bitField, int i) {
    checkBitIndex(i, Byte.SIZE);
    return (byte)(bitField | ((byte)1 << i));
  }

  /**
   * @param width the number of bits in the bitfield's type
   * @throws IndexOutOfBoundsException if {@code idx} not in the given range (both endpoints inclusive)
   */
  private static void checkBitIndex(int idx, int width) {
    if (idx < 0 || idx >= width)
      throw new IndexOutOfBoundsException(idx + " (expected between 0 and " + (width - 1) + ")");
  }

  /**
   * Extracts the {@code i}<sup>th</sup> byte of the given {@code long},
   * such that {@code i=0} is the LSB (rightmost byte) and
   * {@code i=7} is the MSB (leftmost byte).
   *
   * @param x the source {@code long}
   * @param i the index of the byte to extract from {@code x}, between 0 and 7 (inclusive)
   * @return the  <i>{@code i}<sup>th</sup></i> byte of the given {@code long}
   * @throws AssertionError if {@code i} &notin; [0, 7] and assertions are enabled
   */
  public static byte getByte(long x, int i) {
    assert i >= 0 && i <= 7;  // using assert instead of Preconditions so that this doesn't slow down prod code
    // Note: this code is based on java.nio.Bits (methods long0 ... long7)
    return (byte)(x >> (i *8));
  }

  /**
   * Sets the {@code i}<sup>th</sup> byte of the given {@code long} to the specified value,
   * such that {@code i=0} is the LSB (rightmost byte) and
   * {@code i=7} is the MSB (leftmost byte).
   *
   * @param x the target {@code long}
   * @param b the byte value to assign
   * @param i the index of the byte to assign in {@code x}, between 0 and 7 (inclusive)
   * @return the new {@code long} value
   * @throws AssertionError if {@code i} &notin; [0, 7] and assertions are enabled
   */
  public static long setByte(long x, byte b, int i) {
    assert i >= 0 && i <= 7;  // using assert instead of Preconditions so that this doesn't slow down prod code
    // Note: this code is based on java.nio.Bits.makeLong
    // TODO: temp draft based on Google AI overview
    long new_byte = b & 0xffL; // convert byte to long, masking with 0xff to avoid sign-extension on left-shift, if the byte is negative

    // Calculate the number of bits to shift (position * 8)
    int shift_bits = i * 8;
    // 1. Create a mask of 0xFF shifted to the correct position
    long byte_mask = 0xffL << shift_bits;
    // 2. Clear the target byte's bits in the original data using AND with the NOT of the mask
    long ret = x & ~byte_mask;
    // 3. Create a mask with the new byte value shifted to the correct position
    long new_byte_shifted = new_byte << shift_bits;
    // 4. Set the new byte's bits using bitwise OR
    ret |= new_byte_shifted;
    return ret;

  }

  private static long makeLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
    // code borrowed from java.nio.Bits.makeLong
    return ((((long)b7) << 56) |
        (((long)b6 & 0xff) << 48) |
        (((long)b5 & 0xff) << 40) |
        (((long)b4 & 0xff) << 32) |
        (((long)b3 & 0xff) << 24) |
        (((long)b2 & 0xff) << 16) |
        (((long)b1 & 0xff) << 8) |
        (((long)b0 & 0xff)));
  }

  public static long longFromByteArray(byte... bytes) {
    // TODO: this is actually little-endian - maybe make it big-endian by default?
    return longFromByteArray(bytes, true);
  }

  public static long longFromByteArray(byte[] bytes, boolean bigEndian) {
    long ret = 0;
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      int bi = bigEndian ? (7 - i) : i;
      ret |= ((b & 0xffL) << (bi * 8));
    }
    return ret;
  }

  public static long longFromByteArray(byte[] bytes, int offset, int len, boolean bigEndian) {
    assert len >= 0 && len <= 8 && offset + len <= bytes.length;
    long ret = 0;
    int biLimit = len - 1;  // highest byte index (within the long) that we have given the number of bytes in the array range
    for (int i = 0; i < len; i++) {
      byte b = bytes[offset + i];
      int bi = bigEndian ? (biLimit - i) : i;
      ret |= ((b & 0xffL) << (bi * 8));
    }
    return ret;
  }


  /**
   * Encodes the given {@code long} value as an 8-byte array in big-endian order
   * (bytes ordered from most significant to least significant).
   *
   * @param x the {@code long} value to convert
   * @return the big-endian byte array representation of the {@code long}
   */
  public static byte[] longToByteArray(long x) {
    return longToByteArray(x, true);
  }

  /**
   * Encodes the given {@code long} value as an 8-byte array.
   *
   * @param x the {@code long} value to convert
   * @param bigEndian byte order for the result:
   *   {@code true} for big-endian (bytes ordered from most significant to least significant;
   *   {@code false} for little-endian (bytes ordered from least significant to most significant
   * @return the byte array representation of the {@code long} in the specified byte order
   */
  public static byte[] longToByteArray(long x, boolean bigEndian) {
    byte[] ret = new byte[8];
    longToByteArray(x, ret, 0, bigEndian);
    return ret;
  }

  /**
   * Encodes the given {@code long} value as an 8-byte sequence, storing the result in the specified {@code output} array.
   *
   * @param x the {@code long} value to convert
   * @param output the buffer for the result
   * @param outputOffset the offset in {@code output} where the result is stored
   * @param bigEndian byte order for the result:
   *   {@code true} for big-endian (bytes ordered from most significant to least significant;
   *   {@code false} for little-endian (bytes ordered from least significant to most significant
   * @return the array instance that was passed as the {@code output} arg (to facilitate call chaining)
   * @throws ArrayIndexOutOfBoundsException if {@code output} is too short or {@code outputOffset} is out-of-bounds
   */
  @CanIgnoreReturnValue
  public static byte[] longToByteArray(long x, byte[] output, int outputOffset, boolean bigEndian) {
    // Note: we assume that GWT honors Java's ArrayIndexOutOfBoundsException semantics, so we don't check the precondition explicitly
    int bi = outputOffset;
    if (bigEndian) {
      for (int i = 7; i >= 0; i--) {
        output[bi++] = getByte(x, i);
      }
    }
    else {  // little-endian
      for (int i = 0; i < 8; i++) {
        output[bi++] = getByte(x, i);
      }
    }
    return output;
  }

}
