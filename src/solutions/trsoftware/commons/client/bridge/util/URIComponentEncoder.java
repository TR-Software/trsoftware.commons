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

package solutions.trsoftware.commons.client.bridge.util;

import solutions.trsoftware.commons.bridge.BridgeTypeFactory;

/**
 * Provides string escaping logic compatible with the JavaScript
 * {@code encodeURIComponent} and {@code decodeURIComponent} functions.
 * <p>
 * This class should always be used instead of {@link java.net.URLEncoder} when the encoded value needs to
 * be compatible with {@code decodeURIComponent} (e.g. a cookie value that might be read client-side
 * with {@link com.google.gwt.user.client.Cookies}).
 * <p>
 * <b>Example:</b> JSON strings are particularly sensitive to the  {@code URLEncoder.encode("{a: 1, b: 2}", "UTF-8")}
 * returns {@code "%7Ba%3A+1%2C+b%3A+2%7D"} (because it encodes spaces as {@code '+'}), but
 * {@code decodeURIComponent("%7Ba%3A+1%2C+b%3A+2%7D")} returns {@code "{a:+1,+b:+2}"}, which is invalid JSON!
 *
 * @see java.net.URLEncoder
 * @see java.net.URLDecoder
 *
 * @author Alex
 */
public abstract class URIComponentEncoder {

  /**
   * Encodes a value for use in a URI component. Produces the same output as the JavaScript {@code encodeURIComponent}
   * function.
   *
   * @see BridgeTypeFactory#getURIComponentEncoder()
   * @see solutions.trsoftware.tools.experiments.encoding.URLEncodingTester
   */
  public abstract String encode(String value);


  /**
   * The opposite of {@link #encode(String)}.
   */
  public abstract String decode(String value);

  /**
   * @return an instance of this class appropriate for the current execution environment.
   */
  public static URIComponentEncoder getInstance() {
    return BridgeTypeFactory.getURIComponentEncoder();
  }
}
