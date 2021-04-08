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

package solutions.trsoftware.commons.client.useragent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsDate;
import solutions.trsoftware.commons.shared.util.LazyReference;

/**
 * Can be replaced via deferred binding for older browsers (e.g. IE8) to provide implementations
 * of various newer JavaScript functions based on the polyfills from MDN (e.g.
 * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString#Polyfill">
 *   Date.toISOString()</a>)
 *
 * @author Alex
 * @since 1/30/2019
 */
public class Polyfill {

  private static final LazyReference<Polyfill> instance = new LazyReference<Polyfill>() {
    @Override
    protected Polyfill create() {
      return GWT.create(Polyfill.class);
    }
  };

  public static Polyfill get() {
    return instance.get();
  }

  /**
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString">
   *   MDN: Date.prototype.toISOString()</a>
   * @see solutions.trsoftware.commons.client.jso.JsDate#toISOString()
   */
  public native String toISOString(JsDate date) /*-{
    return date.toISOString();
  }-*/;
}
