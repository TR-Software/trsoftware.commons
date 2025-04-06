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

import java.util.function.IntFunction;

/**
 * An overlay type similar to {@link com.google.gwt.core.client.JsArray}
 * except its elements are not restricted to types derived from {@link JavaScriptObject}.
 * <p>
 * Java objects are stored as "opaque values" through JSNI.
 *
 * @param <T> the concrete type of object contained in this array
 * @see com.google.gwt.core.client.JsArray
 * @see <a href="https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html#sharing">
 *   Sharing objects between Java source and JavaScript</a>
 */
public class JsObjectArray<T> extends JavaScriptObject {

  protected JsObjectArray() {
  }

  /**
   * Factory method.
   * @return a new (empty) array
   */
  public static <T> JsObjectArray<T> create() {
    return JavaScriptObject.createArray().cast();
  }

  /**
   * Returns the instance of this class assigned to the given property name in the global {@code window} object
   * (i.e. {@code window[name]}), or assigns and returns a new instance of this class if the property
   * doesn't have a value yet.
   * <p>
   * <i>Caution: this method doesn't check the type of the value previously assigned to this property before returning it.</i>
   * @param name global variable name (property name in the global {@code window} object)
   * @return the current value of {@code window[name]}
   */
  public static native <T> JsObjectArray<T> getInstance(String name) /*-{
    return $wnd[name] || ($wnd[name] = []);
  }-*/;

  /**
   * Gets the object at a given index.
   *
   * @param index the index to be retrieved
   * @return the object at the given index, or <code>null</code> if none
   *         exists
   */
  public final native T get(int index) /*-{
    return this[index];
  }-*/;

  /**
   * Gets the length of the array.
   *
   * @return the array length
   */
  public final native int length() /*-{
    return this.length;
  }-*/;

  /**
   * Sets the object value at a given index.
   *
   * If the index is out of bounds, the value will still be set. The array's
   * length will be updated to encompass the bounds implied by the added object.
   *
   * @param index the index to be set
   * @param value the object to be stored
   */
  public final native void set(int index, T value) /*-{
    this[index] = value;
  }-*/;

  /**
   * Adds the specified element to the end of an array and returns the new length of the array.
   * @return the new length of the array
   * @see #add(Object)
   */
  public final native int push(T value) /*-{
    return this.push(value);
  }-*/;

  /**
   * Appends an element to the end of the array.
   * @return this array, for call chaining.
   */
  public final JsObjectArray<T> add(T value) {
    push(value);
    return this;
  }

  /**
   * Appends the given elements to the end of the array.
   * @return this array, for call chaining.
   */
  @SafeVarargs
  public final JsObjectArray<T> add(T... values) {
    for (T value : values) {
      push(value);
    }
    return this;
  }

  /**
   * @param arrayGenerator constructs a new array of the desired type
   * @return a plain Java array containing the same elements as this native array
   */
  public final T[] toArray(IntFunction<T[]> arrayGenerator) {
    T[] ret = arrayGenerator.apply(length());
    for (int i = 0; i < ret.length; i++) {
      ret[i] = get(i);
    }
    return ret;
  }

}