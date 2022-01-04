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

import javax.annotation.Nullable;

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
   * Casts an existing {@link JavaScriptObject} to this class.
   */
  public static JsObject as(JavaScriptObject obj) {
    return obj.cast();
  }


  /**
   * @return A string property value of this native object or {@code null} if the property is missing or its value is
   * actually {@code null}. Use {@link #hasKey(String)} to disambiguate.
   * @throws ClassCastException in hosted mode if the property value is not a string
   */
  public final native String getString(String key)/*-{
    return this[key]
  }-*/;

  /**
   * @return An object property value of this native objector or {@code null} if the property is missing or its value is
   * actually {@code null}. Use {@link #hasKey(String)} to disambiguate.
   * @throws ClassCastException in hosted mode if the property value is not an object
   */
  public final native <T extends JavaScriptObject> T getObject(String key)/*-{
    return this[key]
  }-*/;

  /**
   * Evaluates the JS expression {@code Boolean(this[key])}
   * @return The boolean property value of this native object, or {@code false} if the object doesn't
   * have the given property. If the property is defined but not a boolean, might return either {@code true} or {@code false},
   * depending on how JS converts the value to a boolean, e.g.
   * <nobr>{@code Boolean("foo") == true}</nobr> while <nobr>{@code Boolean("") == false}</nobr>.
   * Use {@link #hasKey(String)} and {@link #nativeTypeOf(String)} to disambiguate.
   * @see #getBoxedBoolean(String)
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
   * @see #getBoxedNumber(String)
   */
  public final native double getNumber(String key) /*-{
    return Number(this[key]);
  }-*/;

  /**
   * @return The boolean property value of this native object boxed as a Java {@link Boolean}, or {@code null}
   * if the object either doesn't have the given property or its value is not a boolean.
   * Use {@link #hasKey(String)} and {@link #nativeTypeOf(String)} to disambiguate.
   */
  @Nullable
  public final native Boolean getBoxedBoolean(String key)/*-{
    return (typeof this[key] === "boolean") ? @java.lang.Boolean::valueOf(Z)(this[key]) : null;
  }-*/;

  /**
   * Provides a safer alternative to {@link #getNumber(String)}, avoiding type conversion when the property value is
   * not a number.
   * <p>
   * The result (if not {@code null}) can be used to cast the numeric value of the property to any other
   * Java number type (e.g. {@link Double#longValue()}, {@link Double#intValue()}, etc.),
   * although potentially losing precision.
   *
   * @return The numeric property value of this native object boxed as a Java {@link Double}, or {@code null}
   * if the object either doesn't have the given property or its value is not a number.
   * Use {@link #hasKey(String)} and {@link #nativeTypeOf(String)} to disambiguate.
   */
  @Nullable
  public final native Double getBoxedNumber(String key)/*-{
    return (typeof this[key] === "number") ? @java.lang.Double::valueOf(D)(this[key]) : null;
  }-*/;

  /**
   * @return {@code true} iff the given property is {@code in} this object or its prototype chain
   */
  public final native boolean hasKey(String key) /*-{
    return key in this;
  }-*/;

  /**
   * @return {@code true} iff the object has the specified property as its own property (as opposed to inheriting it)
   */
  public final native boolean hasOwnProperty(String key) /*-{
    return this.hasOwnProperty(key);
  }-*/;

  /**
   * Evaluates the JS expression {@code typeof this[key]}
   * @return the JS type string for the value type of this property (e.g. {@code "number", "function", "object", "undefined"}, etc.),
   * or {@code null} if this object doesn't contain a property with the given name.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/typeof">typeof reference</a>
   */
  @Nullable
  public final native String nativeTypeOf(String key) /*-{
    if (key in this)
      return typeof this[key];
    return null;
  }-*/;

  /**
   * @return the {@link JsType} constant corresponding to the result of the JS expression "{@code typeof this[key]}",
   * or {@code null} if this object doesn't contain a property with the given name.
   */
  @Nullable
  public final JsType typeOf(String key) {
    String type = nativeTypeOf(key);
    if (type != null)
      return JsType.parse(type);
    return null;
  }

  /**
   * Assigns a new value for a property ({@code this[key] = value}).
   * @param key name of the property
   * @param value new value for the property
   * @return self (for chaining)
   */
  public final native JsObject set(String key, String value) /*-{
    this[key] = value;
    return this;
  }-*/;

  /**
   * Assigns a new value for a property ({@code this[key] = value}).
   * @param key name of the property
   * @param value new value for the property
   * @return self (for chaining)
   */
  public final native JsObject set(String key, JavaScriptObject value) /*-{
    this[key] = value;
    return this;
  }-*/;

  /**
   * Assigns a new value for a property ({@code this[key] = value}).
   * @param key name of the property
   * @param value new value for the property
   * @return self (for chaining)
   */
  public final native JsObject set(String key, boolean value) /*-{
    this[key] = value;
    return this;
  }-*/;

  /**
   * Assigns a new value for a property ({@code this[key] = value}).
   * @param key name of the property
   * @param value new value for the property
   * @return self (for chaining)
   */
  public final native JsObject set(String key, double value) /*-{
    this[key] = value;
    return this;
  }-*/;

  /**
   * Removes the given property from this native object.
   * @param key the name of the property to be deleted, the call will succeed even if this property doesn't exist.
   * @return self (for chaining)
   */
  public final native JsObject delete(String key) /*-{
    delete this[key];
    return this;
  }-*/;

  /**
   * Assigns the special JavaScript value {@code undefined} for the given property.
   * @param key name of the property
   * @return self (for chaining)
   */
  public final native JsObject setUndefined(String key) /*-{
    this[key] = undefined;
    return this;
  }-*/;

  /**
   * Assigns {@code null} for the given property.
   * @param key name of the property
   * @return self (for chaining)
   */
  public final native JsObject setNull(String key) /*-{
    this[key] = null;
    return this;
  }-*/;

  /**
   * @return an array of a this object's own enumerable property names, iterated in the same order that a normal loop would.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/keys">Object.keys</a>
   * @see JsStringArray#toJavaArray()
   */
  public final native JsStringArray keys() /*-{
    var ret = [];
    for (var key in this) {
      if (this.hasOwnProperty(key)) {
        ret.push(key);
      }
    }
    return ret;
  }-*/;

  /**
   * Copies all enumerable own properties from the given object into {@code this} object. The properties in this
   * object are overwritten by properties in the {@code source} object if they have the same key.
   *
   * @param source the object from which properties will be copied into this object.
   * @return self (for chaining)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/assign">Object.assign()</a>
   */
  public final native JsObject merge(JavaScriptObject source) /*-{
    for (var key in source) {
      if (source.hasOwnProperty(key)) {
        this[key] = source[key];
      }
    }
    return this;
  }-*/;

}