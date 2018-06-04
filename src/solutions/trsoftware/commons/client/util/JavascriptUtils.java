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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.shared.GWT;

/**
 * Date: Apr 16, 2008 Time: 4:09:43 PM
 *
 * @author Alex
 */
public class JavascriptUtils {

  /**
   * Unescapes the URL-encoded characters in the given string.
   * @return The unescaped version of the string, or if the string
   * doesn't pose a valid URL (the browser decodeURIComponent call will
   * throw an exception), returns the argument unmodified, suppressing
   * the exception.  The arguments that can fail contain a "%" symbol
   * without being followed by a two-digit hex code (e.g. "%2B")
   */
  public static String safeDecodeURIComponent(String encodedURLComponent) {
    try {
      return unsafeDecodeURIComponentImpl(encodedURLComponent);
    } catch (Throwable ex) {
      GWT.log("decodeURIComponent(\"" + encodedURLComponent + "\") failed, returning argument without decoding", ex);
      return encodedURLComponent;
    }
  }

  public static native String unsafeDecodeURIComponentImpl(String encodedURLComponent) /*-{
    return $wnd.decodeURIComponent(encodedURLComponent);
  }-*/;

  public static native String encodeURIComponent(String str) /*-{
    return $wnd.encodeURIComponent(str);
  }-*/;
}
