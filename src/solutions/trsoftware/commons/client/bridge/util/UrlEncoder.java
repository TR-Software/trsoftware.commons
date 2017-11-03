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

package solutions.trsoftware.commons.client.bridge.util;

import solutions.trsoftware.commons.bridge.BridgeTypeFactory;

/**
 * Jun 30, 2012
 *
 * @author Alex
 */
public abstract class UrlEncoder {

  /**
   * Encodes a value for use in a URI component.  Same as Javascript's {@code encodeURIComponent} function and Java's
   * {@link java.net.URLEncoder#encode(String, String)}. NOTE: it is not guaranteed that the aforementioned functions,
   * used to implement this class on the client and server respectively, will produce the same results for the same
   * inputs. The encoding used on the server will be UTF-8.
   */
  public abstract String encode(String value);


  /**
   * The opposite of {@link #encode(String)}.
   */
  public abstract String decode(String value);

  /**
   * Returns an instance of this class appropriate for the current execution environment.
   */
  public static UrlEncoder get() {
    return BridgeTypeFactory.getUrlEncoder();
  }
}
