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

package solutions.trsoftware.commons.server.util.text;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import java.util.ArrayList;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.server.util.text.AdaptiveHuffmanCoder.*;

/**
 * Oct 22, 2009
 *
 * @author Alex
 */
public class AdaptiveHuffmanCoderTest extends TestCase {


  public void testEncodeDecode() throws Exception {
    AdaptiveHuffmanCoder coder = new AdaptiveHuffmanCoder();
    String[] input = {"Alice", "in", "wonderland"};
    ArrayList<byte[]> encodings = new ArrayList<byte[]>();
    byte[] bytes = coder.encode(input);
    encodings.add(bytes);
    assertEquals(3 * input.length, bytes.length);  // at first we expect to get 3 bytes per word
    String[] decoded = coder.decode(bytes);
    AssertUtils.assertArraysEqual(input, decoded);

    // but if the code has seen the same words over and over again enough times
    // it will adapt and place them on the first level, result in 1 byte per word for these words
    for (int i = 0; i < 1000; i++) {
      bytes = coder.encode(input);
      encodings.add(bytes);
      assertArraysEqual(input, coder.decode(bytes));
    }

    bytes = coder.encode(input);
    encodings.add(bytes);

    // now we should be down to just 1 byte per word
    assertEquals(input.length, bytes.length);

    // make sure that after the adaptive process all prior encodings can still be decoded
    for (byte[] encoding : encodings) {
      assertArraysEqual(input, coder.decode(encoding));
    }

    System.out.println(coder.dumpTree());
  }

  public void testAddToTree() throws Exception {
    // do a very simple tree
    AdaptiveHuffmanCoder coder = new AdaptiveHuffmanCoder();
    assertArraysEqual(new byte[]{(byte)128}, pathToByteArray(coder.addToTree(1, "a")));
    assertArraysEqual(new byte[]{0, (byte)128}, pathToByteArray(coder.addToTree(2, "aa")));
    assertArraysEqual(new byte[]{(byte)129}, pathToByteArray(coder.addToTree(1, "b")));
    assertArraysEqual(new byte[]{0, (byte)129}, pathToByteArray(coder.addToTree(2, "bb")));
    assertArraysEqual(new byte[]{0, 0, (byte)128}, pathToByteArray(coder.addToTree(3, "aaa")));
    System.out.println(coder.dumpTree());
  }

  // this test relies on visual inspection of output for now
  public void testLevelFilling() throws Exception {
    // test level filling (that the next level starts getting filled when you run out of space on a level
    AdaptiveHuffmanCoder coder = new AdaptiveHuffmanCoder();
    for (int i = 0; i < 129; i++) {  // add 1 more node than level 1 can hold
      coder.addToTree(1, "" + i);
    }
    System.out.println(coder.dumpTree());
  }

  // this test relies on visual inspection of output for now
  public void TODO_testLevel2Filling() throws Exception {
    // test level filling (that the next level starts getting filled when you run out of space on a level
    AdaptiveHuffmanCoder coder = new AdaptiveHuffmanCoder();
    int limit = 128 * 128 + 128 + 1000;  // this should fill levels 1 and 2, and place 1000 remaining leaves on level 3
    for (int i = 0; i < limit; i++) {  // add 1 more node than level 1 can hold
      coder.addToTree(1, "" + i);
    }
    System.out.println(coder.dumpTree());
  }

  public void testPathToByteArray() throws Exception {
    assertArraysEqual(new byte[]{0, 0, 0}, pathToByteArray(0x01000000));
    assertArraysEqual(new byte[]{0, 0}, pathToByteArray(0x00010000));
    assertArraysEqual(new byte[]{0}, pathToByteArray(0x00000100));
    assertArraysEqual(new byte[]{0x11, 0x22, 0x33}, pathToByteArray(0x01112233));
    assertArraysEqual(new byte[]{0x22, 0x33}, pathToByteArray(0x00012233));
    assertArraysEqual(new byte[]{0x33}, pathToByteArray(0x00000133));
    assertPathIntIllegal(0x00000001);
    assertPathIntIllegal(0x00000011);
    assertPathIntIllegal(0x00002200);
    assertPathIntIllegal(0x11000000);
    assertPathIntIllegal(0x00112233);
    assertPathIntIllegal(0);
    assertPathIntIllegal(Integer.MIN_VALUE);
    assertPathIntIllegal(Integer.MAX_VALUE);
  }

  private void assertPathIntIllegal(final int pathInt) {
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        pathToByteArray(pathInt);
      }
    });
  }

  public void testByteValueToIndex() throws Exception {
    for (int i = 0; i < 256; i++) {
      assertEquals(i, byteValueToIndex((byte)i));
    }
  }

  public void testIndexToByteValue() throws Exception {
    for (int i = 0; i < 256; i++) {
      assertEquals(i, byteValueToIndex(indexToByteValue(i)));
    }
  }

}