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

import com.google.gwt.core.client.*;

import java.util.function.Consumer;

/**
 * Utility methods for working with {@link JavaScriptObject} overlays.
 *
 * @author Alex
 * @since 11/28/2021
 *
 * @see JsArrayUtils
 */
public class JsUtils {

  private JsUtils() {
  }

  // TODO: merge this with solutions.trsoftware.commons.client.util.JavascriptUtils


  /**
   * Creates a new Java {@code String[]} containing the same strings as the given native array.
   *
   * @see #toJsArray(String[])
   */
  public static String[] toJavaArray(JsArrayString jsArray) {
    String[] ret = new String[jsArray.length()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = jsArray.get(i);
    }
    return ret;
  }

  /**
   * Creates a new JavaScript array containing the same strings as the given Java array.
   */
  public static JsArrayString toJsArray(String[] arr) {
    JsArrayString ret = JsArrayString.createArray(arr.length).cast();
    for (int i = 0; i < arr.length; i++) {
      ret.set(i, arr[i]);
    }
    return ret;
  }

  /**
   * Creates a new Java {@code double[]} containing the same numbers as the given native array.
   *
   * @see JsArrayUtils#readOnlyJsArray(double[])
   */
  public static double[] toJavaArray(JsArrayNumber jsArray) {
    double[] ret = new double[jsArray.length()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = jsArray.get(i);
    }
    return ret;
  }

  /**
   * Creates a new Java {@code int[]} containing the same integers as the given native array.
   *
   * @see JsArrayUtils#readOnlyJsArray(int[])
   */
  public static int[] toJavaArray(JsArrayInteger jsArray) {
    int[] ret = new int[jsArray.length()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = jsArray.get(i);
    }
    return ret;
  }

  /**
   * Creates a new Java {@code boolean[]} containing the same boolean values as the given native array.
   *
   * @see #toJsArray(boolean[])
   */
  public static boolean[] toJavaArray(JsArrayBoolean jsArray) {
    boolean[] ret = new boolean[jsArray.length()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = jsArray.get(i);
    }
    return ret;
  }

  /**
   * Creates a new JavaScript array containing the same boolean values as the given Java array.
   *
   * @see #toJavaArray(JsArrayBoolean)
   */
  public static JsArrayBoolean toJsArray(boolean[] arr) {
    JsArrayBoolean ret = JsArrayBoolean.createArray(arr.length).cast();
    for (int i = 0; i < arr.length; i++) {
      ret.set(i, arr[i]);
    }
    return ret;
  }

  // TODO(1/4/2024): document and test the new methods below

  /**
   * Strict shallow equality test for 2 JS objects, applying the {@code ===} operator to each property value
   * @param x
   * @param y
   * @return
   */
  public static native boolean equals(JavaScriptObject x, JavaScriptObject y) /*-{
    if (x === y)
      return true;
    if ((typeof x) !== (typeof y))
      return false;
    if (x == null || y == null)  // "== null" matches both null and undefined
      return false;
    // TODO: test the above null/undefined logic
    // Note: Object.keys should work for objects and arrays;
    // for all other types the above code (combined with the below try/catch) should suffice
    // TODO: make sure the above statement is true, e.g. for "number", "function", etc.
    try {
      var xKeys = Object.keys(x);
      var yKeys = Object.keys(y);
      if (xKeys.length !== yKeys.length)
        return false;
      for (var i in xKeys) {
        var key = xKeys[i];
        if (x[key] !== y[key])
          return false;
      }
      return true;
    }
    catch (e) {
      // probably Object.keys threw the exception b/c either x or y wasn't an object or array
      console.warn("JsUtils.equals(", x, y, ") threw an exception: ", e);
      return false;
    }
  }-*/;

  /**
   * Returns a native {@code function} (wrapped with {@code $entry}) that passes its arg to the given consumer.
   *
   * @param <T> a type that's safe to pass back from JavaScript, such as a {@link JavaScriptObject} or an {@link Object}
   *   that originated in Java code
   *   (see <a href="https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html#passing-javascript">JSNI reference</a>)
   */
  public static native <T> JavaScriptObject toJsFunction(Consumer<T> consumer) /*-{
    return $entry(function (arg) {
      consumer.@java.util.function.Consumer::accept(*)(arg);
    });
  }-*/;

}
