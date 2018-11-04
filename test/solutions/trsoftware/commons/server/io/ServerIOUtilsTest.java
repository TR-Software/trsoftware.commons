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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Arrays;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.readCharactersIntoString;

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
    assertEquals(expected, readCharactersIntoString(new ByteArrayInputStream(inputSource), "ASCII"));
    assertEquals(expected, readCharactersIntoString(new StringReader(expected)));
  }

}