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

package solutions.trsoftware.commons.server.util.codec;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.server.util.codec.CodecUtils.base64ToHex;
import static solutions.trsoftware.commons.server.util.codec.CodecUtils.hexToBase64;

/**
 * @author Alex
 * @since 11/11/2017
 */
public class CodecUtilsTest extends TestCase {

  public void testHexToBase64() throws Exception {
    assertEquals("ISa/o6oEGpscxKH3PrgWGw==", hexToBase64("2126bfa3aa041a9b1cc4a1f73eb8161b"));
    assertEquals("nyB4+iikQnfFgiwadgw+1A==", hexToBase64("9f2078fa28a44277c5822c1a760c3ed4"));
    assertEquals("", hexToBase64(""));
  }

  public void testBase64ToHex() throws Exception {
    assertEquals("2126bfa3aa041a9b1cc4a1f73eb8161b", base64ToHex("ISa/o6oEGpscxKH3PrgWGw=="));
    assertEquals("9f2078fa28a44277c5822c1a760c3ed4", base64ToHex("nyB4+iikQnfFgiwadgw+1A=="));
    assertEquals("", base64ToHex(""));
  }

}