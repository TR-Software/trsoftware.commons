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
    checkIndex(i, Integer.SIZE);
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
    checkIndex(i, Short.SIZE);
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
    checkIndex(i, Byte.SIZE);
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
    checkIndex(i, Long.SIZE);
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
    checkIndex(i, Integer.SIZE);
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
    checkIndex(i, Long.SIZE);
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
    checkIndex(i, Short.SIZE);
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
    checkIndex(i, Byte.SIZE);
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
    checkIndex(i, Integer.SIZE);
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
    checkIndex(i, Long.SIZE);
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
    checkIndex(i, Short.SIZE);
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
    checkIndex(i, Byte.SIZE);
    return (byte)(bitField | ((byte)1 << i));
  }

  /**
   * @param width the max number of bits in the bitfield's type
   * @throws IndexOutOfBoundsException if {@code idx} not in the given range (both endpoints inclusive)
   */
  private static void checkIndex(int idx, int width) {
    if (idx < 0 || idx >= width)
      throw new IndexOutOfBoundsException(String.valueOf(idx) + " (expected between 0 and " + (width - 1) + ")");
  }

}
