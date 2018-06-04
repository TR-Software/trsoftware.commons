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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

/**
 * @author Alex
 * @since 1/2/2018
 */
public class BitSetJavaTest extends TestCase {

  BitSetGwtTest delegate = new BitSetGwtTest();

  public void testConstructor() {
    delegate.testConstructor();
  }

  public void testAnd() {
    delegate.testAnd();
  }

  public void testAndNot() {
    delegate.testAndNot();
  }

  public void testCardinality() {
    delegate.testCardinality();
  }

  public void testClear() {
    delegate.testClear();
  }

  public void testClearInt() {
    delegate.testClearInt();
  }

  public void testClearIntIntAndSetIntInt() {
    delegate.testClearIntIntAndSetIntInt();
  }

  public void testClone() {
    delegate.testClone();
  }

  public void testEquals() {
    delegate.testEquals();
  }

  public void testFlipInt() {
    delegate.testFlipInt();
  }

  public void testFlipIntInt() {
    delegate.testFlipIntInt();
  }

  public void testGetIntAndSetInt() {
    delegate.testGetIntAndSetInt();
  }

  public void testGetIntInt() {
    delegate.testGetIntInt();
  }

  public void testHashCode() {
    delegate.testHashCode();
  }

  public void testIntersects() {
    delegate.testIntersects();
  }

  public void testIsEmpty() {
    delegate.testIsEmpty();
  }

  public void testLength() {
    delegate.testLength();
  }

  public void testNextClearBit() {
    delegate.testNextClearBit();
  }

  public void testNextSetBit() {
    delegate.testNextSetBit();
  }

  public void testPreviousClearBit() {
    delegate.testPreviousClearBit();
  }

  public void testPreviousSetBit() {
    delegate.testPreviousSetBit();
  }

  public void testOr() {
    delegate.testOr();
  }

  public void testSetIntBoolean() {
    delegate.testSetIntBoolean();
  }

  public void testSetIntIntBoolean() {
    delegate.testSetIntIntBoolean();
  }

  public void testSize() {
    delegate.testSize();
  }

  public void testToString() {
    delegate.testToString();
  }

  public void testXor() {
    delegate.testXor();
  }

  public void testToByteArray() {
    delegate.testToByteArray();
  }

  public void testToLongArray() {
    delegate.testToLongArray();
  }

  public void testValueOfBytes() {
    delegate.testValueOfBytes();
  }

  public void testValueOfLongs() {
    delegate.testValueOfLongs();
  }
}
