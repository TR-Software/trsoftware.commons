/*
 * Copyright 2025 TR Software Inc.
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

import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.math.BigInteger;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;

/**
 * @author Alex
 * @since 5/21/2025
 */
public class BigIntRadixCodecTest extends BaseTestCase {

  public void testEncodeAndDecode() throws Exception {
    Alphabet[] alphabets = {
        BigRadixAlphabet.getInstance(),
        SmallRadixAlphabet.getInstance(),
        Base64Alphabet.getInstance(),
        UrlSafeBase64Alphabet.getInstance(),
    };
    for (Alphabet alphabet : alphabets) {
      for (int radix = 2; radix <= BigRadixAlphabet.MAX_RADIX; radix++) {
        BigIntRadixCodec codec = new BigIntRadixCodec(radix, alphabet);
        // test some basic examples manually

        // single-digit ints should be the same as Integer.toString (unless using base64)
        if (!(alphabet instanceof Base64Alphabet || alphabet instanceof UrlSafeBase64Alphabet)) {
          for (int i = -9; i < 10; i++) {
            BigInteger bigInt = BigInteger.valueOf(i);
            assertEquals(Integer.toString(i, radix), testEncodeAndDecode(codec, bigInt));
            // byte[] version
  //      assertEquals(encoded, codec.encode(bigInt.toByteArray()));  // TODO: this fails for negative ints
          }
        }
        // test with some random BigInteger instances
        for (int nBytes = 1; nBytes < 20; nBytes++) {
          // 10 random instances for each number of bytes
          for (int i = 0; i < 10; i++) {
            BigInteger bigInt = new BigInteger(RandomUtils.randBytes(nBytes));
            testEncodeAndDecode(codec, bigInt);
          }
        }
      }
    }
  }

  private String testEncodeAndDecode(BigIntRadixCodec codec, BigInteger value) {
    String encoded = codec.encode(value);
    // test decoding
    assertEquals(value, codec.decode(encoded));
    return encoded;
  }

  @Slow
  public void testEncodeAndDecodeUnsignedBytes() throws Exception {
    Alphabet[] alphabets = {
        BigRadixAlphabet.getInstance(),
        SmallRadixAlphabet.getInstance(),
        Base64Alphabet.getInstance(),
        UrlSafeBase64Alphabet.getInstance(),
    };
    int maxArrLength = 20;
    int nIterations = 100;
    for (Alphabet alphabet : alphabets) {
      for (int radix = 2; radix <= BigRadixAlphabet.MAX_RADIX; radix++) {
        BigIntRadixCodec codec = new BigIntRadixCodec(radix, alphabet);
        // test with some random byte[] instances
        for (int nBytes = 1; nBytes < maxArrLength; nBytes++) {
          // random instances for each number of bytes
          for (int i = 1; i <= nIterations; i++) {
            byte[] inputBytes = RandomUtils.randBytes(nBytes);
            // use the first few iterations to specifically test having multiple leading 0 bytes
            if (i < 4) {
              // zero out the leading i bytes
              for (int j = 0; j < i && j < inputBytes.length; j++) {
                inputBytes[j] = 0;
              }
            }
            testEncodeAndDecodeUnsignedBytes(codec, inputBytes);
          }
        }
      }
    }
  }

  private void testEncodeAndDecodeUnsignedBytes(BigIntRadixCodec codec, byte[] inputBytes) {
    String encoded = codec.encodeUnsignedBytes(inputBytes);
    byte[] decodedBytes = codec.decodeUnsignedBytes(encoded, inputBytes.length);
    assertArraysEqual(inputBytes, decodedBytes);
  }

}