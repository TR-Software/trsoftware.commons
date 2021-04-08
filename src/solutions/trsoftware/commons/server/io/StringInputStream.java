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

package solutions.trsoftware.commons.server.io;

import solutions.trsoftware.commons.server.util.ServerStringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
 * Extends {@link ByteArrayInputStream} to allow using a {@link String} as the byte source.
 *
 * @author Alex
 * @since 5/9/2018
 */
public class StringInputStream extends ByteArrayInputStream {

  /**
   * @param str the string to use as the byte source
   * @see String#getBytes()
   */
  public StringInputStream(String str) {
    super(str.getBytes());
  }

  /**
   * @param str the string to use as the byte source
   * @param charsetName the desired {@linkplain java.nio.charset.Charset charset} for encoding the bytes
   *
   * @see ServerStringUtils#stringToBytes(String, String)
   * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html">Supported Encodings</a>
   */
  public StringInputStream(String str, String charsetName) {
    super(ServerStringUtils.stringToBytes(str, charsetName));
  }


  public StringInputStream(String str, Charset charset) {
    super(str.getBytes(charset));
  }
}
