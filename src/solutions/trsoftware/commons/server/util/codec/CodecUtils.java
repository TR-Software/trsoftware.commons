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

package solutions.trsoftware.commons.server.util.codec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.Base64;

/**
 * @author Alex
 * @since 11/11/2017
 */
public class CodecUtils {

  public static String hexToBase64(String hexString) {
    if (hexString.isEmpty())
      return "";
    byte[] hexBytes;
    try {
      hexBytes = Hex.decodeHex(hexString.toCharArray());
    }
    catch (DecoderException e) {
      throw new IllegalArgumentException(e);
    }
    return Base64.getEncoder().encodeToString(hexBytes);
  }

  public static String base64ToHex(String b64String) {
    if (b64String.isEmpty())
      return "";
    return new String(Hex.encodeHex(Base64.getDecoder().decode(b64String)));
  }


}
