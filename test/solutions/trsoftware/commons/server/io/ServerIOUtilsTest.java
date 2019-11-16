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

package solutions.trsoftware.commons.server.io;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;
import solutions.trsoftware.commons.shared.util.function.ToLongBiFunctionThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.copyInputToOutput;
import static solutions.trsoftware.commons.server.io.ServerIOUtils.readCharactersIntoString;
import static solutions.trsoftware.commons.shared.util.RandomUtils.randBytes;

/**
 * Oct 18, 2009
 *
 * @author Alex
 */
public class ServerIOUtilsTest extends TestCase {

  public void testReadCharactersIntoString() throws Exception {
    int size = 50_000;
    byte[] inputSource = new byte[size];  // use a 1 meg size array filled with 'x'
    Arrays.fill(inputSource, (byte)'x');
    String expected = new String(inputSource);
    assertEquals(expected, readCharactersIntoString(new ByteArrayInputStream(inputSource)));
    // the overloaded versions of the method should produce the same result
    assertEquals(expected, readCharactersIntoString(new ByteArrayInputStream(inputSource), StandardCharsets.US_ASCII));
    assertEquals(expected, readCharactersIntoString(new StringReader(expected)));
  }

  public void testCopyInputToOutput() throws Exception {
    int inputLength = 100;  // the number of random bytes supplied by the input stream
    // 1) without explicit buffer size
    byte[] inputBytes = randBytes(inputLength);
    {
      doTestCopyInputToOutput(inputBytes, ServerIOUtils::copyInputToOutput);
    }
    // 2) with explicit buffer size
    for (int i = 0; i <= 10; i++) {  // will iterate powers of 2 up to 2^10 (1024)
      int bufferSize = 1 << i;
      doTestCopyInputToOutput(inputBytes, (in, out) -> copyInputToOutput(in, out, bufferSize));
    }
    // 3) with an input length limit (should throw exception if the input stream contains more than this number of bytes)
    // 3.a) if the limit >= the actual input length, make sure the method doesn't throw an exception
    for (int i = -1; i < 2; i++) {
      int bufferSize = inputLength + i;  // bufferSize within +/- 1 of the input length,
      for (int j = 0; j < 2; j++) {
        int inputLengthLimit = inputLength + j;  // limit same or +1 of the input length,
        doTestCopyInputToOutput(inputBytes,
            (in, out) -> copyInputToOutput(in, out, bufferSize, inputLengthLimit));
      }
    }
    // 3.b) if the limit is less than the buffer size, no bytes should be copied to output
    assertCopyInputToOutputThrowsException(inputBytes, 10, 11, 0);

    // 3.c) if the limit >= than the buffer size, some bytes will still be copied to output (in increments of bufferSize, until the limit has been exceeded)
    assertCopyInputToOutputThrowsException(inputBytes, 10, 10, 10);
  }

  private static void assertCopyInputToOutputThrowsException(byte[] inputBytes, int inputLengthLimit, int bufferSize, int expectedNumBytesCopied) {
    InputStreamTooLongException ex = AssertUtils.assertThrows(InputStreamTooLongException.class, (Function0_t<Throwable>)() ->
        doTestCopyInputToOutput(inputBytes,
            (in, out) -> copyInputToOutput(in, out, bufferSize, inputLengthLimit)));
    assertEquals(expectedNumBytesCopied, ex.getNumBytesCopied());
    assertEquals(inputLengthLimit, ex.getInputLengthLimit());
    assertEquals(bufferSize, ex.getNumExtraInputBytes());  // the next buffer-ful will have been read already
    assertTrue(Arrays.equals(Arrays.copyOfRange(inputBytes, expectedNumBytesCopied, expectedNumBytesCopied+bufferSize), ex.getExtraInputBytes()));
  }

  /**
   * Tests copying the specified number of random bytes using one of the {@link ServerIOUtils#copyInputToOutput} methods.
   * Unless the given operation throws {@link InputStreamTooLongException},
   * will verify that all the given bytes are copied to the output.
   *
   * @param inputBytes will use a {@link ByteArrayInputStream} initialized with these bytes.
   * @param copyFcn function that calls one of the overloads of {@link ServerIOUtils#copyInputToOutput}
   *
   * @throws InputStreamTooLongException if thrown by the given function
   */
  private static void doTestCopyInputToOutput(byte[] inputBytes, ToLongBiFunctionThrows<InputStream, OutputStream, Exception> copyFcn) throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(inputBytes);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    long bytesCopied = copyFcn.accept(in, out);
    assertEquals(inputBytes.length, bytesCopied);
    assertTrue(Arrays.equals(inputBytes, out.toByteArray()));
  }

}