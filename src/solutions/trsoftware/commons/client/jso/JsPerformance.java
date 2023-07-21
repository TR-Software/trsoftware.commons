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

package solutions.trsoftware.commons.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

import javax.annotation.Nullable;

/**
 * Overlay for the native {@code Performance} API ({@code window.performance}).
 *
 *
 * @author Alex
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Performance">Performance API (MDN)</a>
 * @since 5/3/2023
 *
 */
public class JsPerformance extends JavaScriptObject {

  // Overlay types always have protected, zero-arg constructors, because the object must have been instantiated in javascript
  protected JsPerformance() {
  }

  /**
   * @return the {@code window.performance} object, or {@code null} if not available (e.g. using incompatible browser)
   */
  @Nullable
  public static native JsPerformance get() /*-{
    return $wnd.performance;  // JSNI returns `undefined` as `null` (see https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html#:~:text=JavaScript%20undefined%20is%20also%20considered%20equal%20to%20null%20when%20passed%20into%20Java%20code)
  }-*/;

  /**
   * @return a high-res timestamp in milliseconds, with microsecond precision
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Performance/now">Performance.now() on MDN</a>
   */
  public final native double now() /*-{
    return this.now();
  }-*/;

  /**
   * @return {@code true} if the {@link #now()} method is supported by the current browser.
   */
  public static native boolean implementsNow() /*-{
    return Boolean($wnd.performance && $wnd.performance.now);
  }-*/;

}
