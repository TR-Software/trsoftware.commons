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

import com.google.gwt.core.client.JsDate;

/**
 * @author Alex
 * @since 1/30/2019
 */
public class PolyfillIE8 extends Polyfill {

  @Override
  public native String toISOString(JsDate date) /*-{
    if (date.toISOString) {
      return date.toISOString();
    } else {
      function pad(number) {
        return number < 10 ? '0' + number : number;
      }

      return date.getUTCFullYear() +
          '-' + pad(date.getUTCMonth() + 1) +
          '-' + pad(date.getUTCDate()) +
          'T' + pad(date.getUTCHours()) +
          ':' + pad(date.getUTCMinutes()) +
          ':' + pad(date.getUTCSeconds()) +
          '.' + (date.getUTCMilliseconds() / 1000).toFixed(3).slice(2, 5) +
          'Z';
    }
  }-*/;
}
