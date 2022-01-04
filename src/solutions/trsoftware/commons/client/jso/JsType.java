/*
 * Copyright 2022 TR Software Inc.
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

/**
 * Represents the native JS types.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/typeof">Javascript typeof operator</a>
 *
 * @author Alex
 * @since 11/17/2017
 */
public enum JsType {

  UNDEFINED,
  BOOLEAN,
  NUMBER,
  STRING,
  FUNCTION,
  /** Either an actual object or a {@code null} */
  OBJECT,
  /** This type is new in ECMAScript 2015 */
  SYMBOL,
  /** This type is new in ECMAScript 2020 */
  BIGINT,
  ;

  /**
   * @return the corresponding JavaScript {@code typeof} string
   * @see #parse(String)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/typeof">Javascript typeof operator</a>
   */
  public String getNativeName() {
    return name().toLowerCase();
  }

  /**
   * @param nativeName value obtained using a JS {@code typeof} expression
   * @return the {@code enum} constant corresponding to the given JS name
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/typeof">Javascript typeof operator</a>
   */
  public static JsType parse(String nativeName) {
    return valueOf(nativeName.toUpperCase());
  }

}
