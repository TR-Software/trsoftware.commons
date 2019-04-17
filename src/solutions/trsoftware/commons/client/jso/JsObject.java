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

/**
 * An overlay type providing access to properties of a {@link JavaScriptObject}.
 *
 * @author Alex
 */
public class JsObject extends JavaScriptObject {

  // Overlay types always have protected, zero-arg constructors, because the object must have been instantiated in javascript
  protected JsObject() { }

  /**
   * Factory method.
   * @return a new (empty) object
   */
  public static JsObject create() {
    return JavaScriptObject.createObject().cast();
  }


  /**
   * @return A string property value of this native object or {@code null} if the property is missing or its value is
   * actually {@code null}. Use {@link #hasKey(String)} to disambiguate.
   */
  public final native String getString(String key)/*-{
    return this[key] || null
  }-*/;

  /**
   * @return An object property value of this native objector or {@code null} if the property is missing or its value is
   * actually {@code null}. Use {@link #hasKey(String)} to disambiguate.
   */
  public final native <T extends JavaScriptObject> T getObject(String key)/*-{
    return this[key] || null
  }-*/;

  /**
   * Evaluates the JS expression {@code Boolean(this[key])}
   * @return The boolean property value of this native object, or {@code false} if the object doesn't
   * have the given property. If the property is defined but not a boolean, might return either {@code true} or {@code false},
   * depending on how JS converts the value to a boolean, e.g.
   * <nobr>{@code Boolean("foo") == true}</nobr> while <nobr>{@code Boolean("") == false}</nobr>.
   * Use {@link #hasKey(String)} and {@link #nativeTypeOf(String)} to disambiguate.
   */
  public final native boolean getBoolean(String key)/*-{
    return Boolean(this[key])
  }-*/;


  /**
   * Evaluates the JS expression {@code Number(this[key])}
   * @return The numeric property value of this native object.  Will return {@link Double#NaN} if the object doesn't
   * have the given property or if its value is actually {@code NaN}.  Might even return an actual number
   * if {@code Number(this[key])} evaluates to something other than {@code NaN}, e.g.
   * <nobr> {@code Number("foo") == NaN}</nobr> while <nobr>{@code Number("") == 0}</nobr>.
   * Use {@link #hasKey(String)} and {@link #nativeTypeOf(String)} to disambiguate.
   */
  public final native double getNumber(String key) /*-{
    return Number(this[key]);
  }-*/;

  /**
   * @return {@code true} iff this native object has the given property.
   */
  public final native boolean hasKey(String key) /*-{
    return key in this;
  }-*/;

  /**
   * Evaluates the JS expression {@code typeof this[key]}
   * @return the JS type string for the value type of this property (e.g. {@code "number", "function", "object", "undefined"}, etc.),
   * or {@code null} if this object doesn't contain a property with the given name.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/typeof">typeof reference</a>
   */
  public final native String nativeTypeOf(String key) /*-{
    if (key in this)
      return typeof this[key];
    return null;
  }-*/;

  /**
   * @return the {@link JsType} constant corresponding to the result of the JS expression "{@code typeof this[key]}",
   * or {@code null} if this object doesn't contain a property with the given name.
   */
  public final JsType typeOf(String key) {
    String type = nativeTypeOf(key);
    if (type != null)
      return JsType.parse(type);
    return null;
  }

  public final native void set(String key, String value) /*-{
    this[key] = value;
  }-*/;

  public final native void set(String key, JavaScriptObject value) /*-{
    this[key] = value;
  }-*/;

  public final native void set(String key, boolean value) /*-{
    this[key] = value;
  }-*/;

  public final native void set(String key, double value) /*-{
    this[key] = value;
  }-*/;

  /**
   * Removes the given property from this native object.
   * @param key the name of the property to be deleted, the call will succeed even if this property doesn't exist.
   */
  public final native void delete(String key) /*-{
    delete this[key];
  }-*/;

}