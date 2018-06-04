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
package solutions.trsoftware.commons.client.jso;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;

/**
 * Extends {@link JsArrayMixed} to provide ability to construct an array of values via method chaining.
 *
 * Example:
 * <pre>
 *   {@link JsMixedArray#create() JsMixedArray.create()}.add("Event object: ").add(event)
 * </pre>
 */
public class JsMixedArray extends JsArrayMixed {

  protected JsMixedArray() {
  }

  /**
   * Factory method
   * @return a new array
   */
  public static JsMixedArray create() {
    return (JsMixedArray)JavaScriptObject.createArray();
  }

  /**
   * Appends an element to the end of the array.
   * @return this array, for method chaining.
   */
  public final JsMixedArray add(boolean value) {
    push(value);
    return this;
  }

  /**
   * Appends an element to the end of the array.
   * @return this array, for method chaining.
   */
  public final JsMixedArray add(double value) {
    push(value);
    return this;
  }

  /**
   * Appends an element to the end of the array.
   * @return this array, for method chaining.
   */
  public final JsMixedArray add(String value) {
    push(value);
    return this;
  }

  /**
   * Appends an element to the end of the array.
   * @return this array, for method chaining.
   */
  public final JsMixedArray add(JavaScriptObject value) {
    push(value);
    return this;
  }

}
