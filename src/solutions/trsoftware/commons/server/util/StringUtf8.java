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

package solutions.trsoftware.commons.server.util;

import java.util.Arrays;

/**
 * To save memory, stores a UTF-8 byte array representing a string instead
 * of a String instance which uses a Java char array and has several int fields.
 *
 * In practice, however, this doesn't offer much more than 40% space savings,
 * in the best case.
 * 
 * (The JVM also seems to internally cache strings, which goes in favor of
 * using java.lang.String)
 *
 * @since Oct 21, 2009
 * @author Alex
 */
public final class StringUtf8 {
  private final byte[] bytes;

  public StringUtf8(String str) {
    bytes = ServerStringUtils.stringToBytesUtf8(str);
  }

  @Override
  public String toString() {
    return ServerStringUtils.bytesToStringUtf8(bytes);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StringUtf8 that = (StringUtf8)o;

    if (!Arrays.equals(bytes, that.bytes)) return false;

    return true;
  }

  public int hashCode() {
    return (bytes != null ? Arrays.hashCode(bytes) : 0);
  }
}
