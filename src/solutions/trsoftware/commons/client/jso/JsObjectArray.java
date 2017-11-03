/*
 * Copyright 2008 Google Inc.
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

/**
 * An overlay type similar to com.google.gwt.core.client.JsArray
 * except its elements are not restricted to types derived from JavaScriptObject
 *
 * @param <T> the concrete type of object contained in this array
 * @see com.google.gwt.core.client.JsArray
 */
public class JsObjectArray<T> extends JavaScriptObject {

  protected JsObjectArray() {
  }

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
}