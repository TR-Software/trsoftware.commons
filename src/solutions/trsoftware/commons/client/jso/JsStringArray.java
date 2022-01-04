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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Extends {@link JsArrayString} to provide ability to construct an array of values via method chaining.
 * <p>
 * <b>Example</b>:
 * <pre>
 *   {@link JsStringArray#create() JsMixedArray.create()}.add("foo").add("bar")
 * </pre>
 */
public class JsStringArray extends JsArrayString {

  protected JsStringArray() {
  }

  /**
   * @return a new empty array
   */
  public static JsStringArray create() {
    return JavaScriptObject.createArray().cast();
  }

  /**
   * @return a new empty array with the given size
   * @see JavaScriptObject#createArray(int)
   */
  public static JsStringArray create(int size) {
    return JavaScriptObject.createArray(size).cast();
  }

  /**
   * @return a new instance containing the same elements as the given Java array
   */
  public static JsStringArray create(String[] arr) {
    return JsUtils.toJsArray(arr).cast();
  }

  /**
   * Appends an element to the end of the array.
   * @return this array, for method chaining.
   */
  public final JsStringArray add(String value) {
    push(value);
    return this;
  }

  public final String[] toJavaArray() {
    return JsUtils.toJavaArray(this);
  }

}
