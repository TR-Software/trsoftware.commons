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
import solutions.trsoftware.commons.client.useragent.Polyfill;
import solutions.trsoftware.commons.shared.util.TimeUnit;

import java.util.Comparator;
import java.util.Date;

/**
 * JSNI overlay for the JavaScript
 * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date">Date</a> object.
 * <p>
 * NOTE: this class extends {@link com.google.gwt.core.client.JsDate} to provide some of the missing methods
 *
 * @author Alex
 * @since 7/15/2018
 */
public class JsDate extends com.google.gwt.core.client.JsDate {
  /*
  NOTE: don't be tempted to implement Comparable<JsDate>: it will fail in hosted mode due to the
  "Overlay types cannot implement interfaces that define methods" limitation
  (see https://github.com/gwtproject/old_google_code_wiki/blob/master/OverlayTypes.wiki.md)
  */

  static {
    // enable Object.equals and Object.hashCode for all instances
    addEqualsAndHashCodeToPrototype();
  }

  protected JsDate() {
  }

  // NOTE: we're shadowing the static create(*) factory methods from superclass to avoid having to cast the new instance with JSO.cast()

  /**
   * Creates a new date with the current time.
   */
  public static JsDate create() {
    return com.google.gwt.core.client.JsDate.create().cast();
  }

  /**
   * Creates a new date with the specified internal representation, which is the
   * number of milliseconds since midnight on January 1st, 1970. This is the
   * same representation returned by {@link #getTime()}.
   */
  public static JsDate create(double milliseconds) {
    return com.google.gwt.core.client.JsDate.create(milliseconds).cast();
  }

  /**
   * Creates a new instance from a formatted date string.
   * Any string returned by another instance's {@link #toISOString()} method should work here.
   *
   * <blockquote cite="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date">
   * <em>WARNING</em>: parsing of date strings with the {@code Date} constructor (and {@code Date.parse},
   * they are equivalent) is strongly discouraged due to browser differences and inconsistencies. Support for <a
   * href="http://tools.ietf.org/html/rfc2822#section-3.3">RFC 2822</a> format strings is by convention only.
   * Support for ISO 8601 formats differs in that date-only strings (e.g. "1970-01-01") are treated as UTC, not local.
   * </blockquote>
   *
   * @param dateString String value representing a date. The string should be in a format recognized by the
   *     {@link #parse(String)} method (IETF-compliant RFC 2822 timestamps and also a version of ISO8601).
   * @return a new {@code Date} object parsed from the given formatted string.
   * @see #parse(String)
   * @see #toISOString()
   */
  public static JsDate create(String dateString) {
    return com.google.gwt.core.client.JsDate.create(dateString).cast();
  }

  /**
   * Creates a new date using the specified values.
   */
  public static JsDate create(int year, int month) {
    return com.google.gwt.core.client.JsDate.create(year, month).cast();
  }

  /**
   * Creates a new date using the specified values.
   */
  public static JsDate create(int year, int month, int dayOfMonth) {
    return com.google.gwt.core.client.JsDate.create(year, month, dayOfMonth).cast();
  }

  /**
   * Creates a new date using the specified values.
   */
  public static JsDate create(int year, int month, int dayOfMonth, int hours) {
    return com.google.gwt.core.client.JsDate.create(year, month, dayOfMonth, hours).cast();
  }

  /**
   * Creates a new date using the specified values.
   */
  public static JsDate create(int year, int month, int dayOfMonth, int hours, int minutes) {
    return com.google.gwt.core.client.JsDate.create(year, month, dayOfMonth, hours, minutes).cast();
  }

  /**
   * Creates a new date using the specified values.
   */
  public static JsDate create(int year, int month, int dayOfMonth, int hours, int minutes, int seconds) {
    return com.google.gwt.core.client.JsDate.create(year, month, dayOfMonth, hours, minutes, seconds).cast();
  }

  /**
   * Creates a new date using the specified values.
   */
  public static JsDate create(int year, int month, int dayOfMonth, int hours, int minutes, int seconds, int millis) {
    return com.google.gwt.core.client.JsDate.create(year, month, dayOfMonth, hours, minutes, seconds, millis).cast();
  }


  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getDay">{@code getDay()}</a> method.
   * <p>
   * NOTE: this method simply delegates to {@link #getDay()}; we provide it just for clarification (using a less-ambiguous name).
   *
   * @return the day of the week for the specified date according to local time (0 for Sunday, 1 for Monday, 2 for Tuesday, and so on).
   */
  public final int getDayOfWeek() {
    return getDay();
  }

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getUTCDay">{@code getUTCDay()}</a> method.
   * <p>
   * NOTE: this method simply delegates to {@link #getDay()}; we provide it just for clarification (using a less-ambiguous name).
   * @return the day of the week for the specified date according to universal time (0 for Sunday, 1 for Monday, 2 for Tuesday, and so on).
   */
  public final int getUTCDayOfWeek() {
    return this.getUTCDay();
  };


  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/setMilliseconds">{@code
   * setMilliseconds()}</a> method, which sets the milliseconds for a specified date according to local time.
   *
   * @param millis A number between 0 and 999, representing the milliseconds.
   * @return The number of milliseconds between [<u>1 January 1970 00:00:00 UTC</u> and <u>the updated date</u>] (the
   *     Date object is also changed in place).
   */
  public final native double setMilliseconds(int millis) /*-{
    return this.setMilliseconds(millis);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString">{@code
   * toISOString()}</a> method, which returns a string in simplified extended ISO format (ISO 8601), which is always 24
   * or 27 characters long (YYYY-MM-DDTHH:mm:ss.sssZ or ±YYYYYY-MM-DDTHH:mm:ss.sssZ, respectively). The timezone is
   * always zero UTC offset, as denoted by the suffix "Z".
   * <p><b>Example</b>:
   * <pre>
   *   var today = new Date('05 October 2011 14:48 UTC');
   *   console.log(today.toISOString()); // Returns 2011-10-05T14:48:00.000Z
   * </pre>
   *
   * @return A string representing the given date in the ISO 8601 format according to universal time.
   */
  public final String toISOString() {
    return Polyfill.get().toISOString(this);
  }

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toJSON">{@code
   * toJSON()}</a> method, which  returns a string representation of the Date object.
   * <p>
   * Calling {@code toJSON()} returns a string (using {@link #toISOString()}) representing the {@code Date} object's
   * value. This method is generally intended to, by default, usefully serialize Date objects during JSON
   * serialization.
   * <p><b>Example</b>:
   * <pre>
   *   var jsonDate = (new Date()).toJSON();
   *   var backToDate = new Date(jsonDate);
   *   console.log(jsonDate); //2015-10-26T07:46:36.611Z
   * </pre>
   *
   * @return A string representation of the given date (identical to {@link #toISOString()})
   */
  public final native String toJSON() /*-{
    return this.toJSON();
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleDateString">{@code
   * toLocaleDateString()}</a> method, which returns a string with a language sensitive representation of the date
   * portion of this date. The new locales and options arguments let applications specify the language whose formatting
   * conventions should be used and allow to customize the behavior of the function. In older implementations, which
   * ignore the locales and options arguments, the locale used and the form of the string returned are entirely
   * implementation dependent.
   * <p>
   * <b>Example</b>:
   * <pre>
   *   // NOTE: this example was tested using Chrome 67.0.3396.99 (on Windows); results might differ in other browsers
   *   var dt = new Date("2018-07-16T01:47:13.842Z");
   *   dt.toLocaleDateString("us");  // returns "7/15/2018" (US/English locale)
   *   dt.toLocaleDateString("es");  // returns "15/7/2018" (Spanish locale)
   *   dt.toLocaleDateString("fr");  // returns "15/7/2018" (French locale)
   *   dt.toLocaleDateString("fr-CA");  // returns "2018-07-15" (French-Canadian locale)
   *   dt.toLocaleDateString("ru");  // returns "15.07.2018" (Russian locale)
   *   dt.toLocaleDateString("zh");  // returns "2018/7/15" (Chinese locale)
   * </pre>
   *
   * @param locale A string with a BCP 47 language tag (refer to the MDN docs for possible values)
   * @return A string representing the date portion of the given Date instance according to language-specific conventions.
   */
  public final native String toLocaleDateString(String locale) /*-{
    return this.toLocaleDateString(locale);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleDateString">{@code
   * toLocaleDateString()}</a> method, which returns a string with a language sensitive representation of the date
   * portion of this date. The new locales and options arguments let applications specify the language whose formatting
   * conventions should be used and allow to customize the behavior of the function. In older implementations, which
   * ignore the locales and options arguments, the locale used and the form of the string returned are entirely
   * implementation dependent.
   *
   * @param locales an array of BCP 47 language tag strings (refer to the MDN docs for possible values)
   * @return A string representing the date portion of the given Date instance according to language-specific conventions.
   * @see #toLocaleDateString(String)
   */
  public final native String toLocaleDateString(JsStringArray locales) /*-{
    return this.toLocaleDateString(locales);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleDateString">{@code
   * toLocaleDateString()}</a> method, which returns a string with a language sensitive representation of the date
   * portion of this date. The new locales and options arguments let applications specify the language whose formatting
   * conventions should be used and allow to customize the behavior of the function. In older implementations, which
   * ignore the locales and options arguments, the locale used and the form of the string returned are entirely
   * implementation dependent.
   *
   * @param locales an array of BCP 47 language tag strings (refer to the MDN docs for possible values)
   * @param options optional object containing additional options (refer to the MDN docs for possible values)
   * @return A string representing the date portion of the given Date instance according to language-specific conventions.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleDateString">MDN
   *     Reference</a>
   */
  public final native String toLocaleDateString(JsStringArray locales, JsObject options) /*-{
    return this.toLocaleDateString(locales, options);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleString">{@code
   * toLocaleString()}</a> method, which returns a string with a language sensitive representation of this date. The new
   * locales and options arguments let applications specify the language whose formatting conventions should be used and
   * customize the behavior of the function. In older implementations, which ignore the locales and options arguments,
   * the locale used and the form of the string returned are entirely implementation dependent.
   * <p>
   * This version of the method uses the specified locale
   * <p>
   * <b>Example</b>:
   * <pre>
   *   // NOTE: this example was tested using Chrome 67.0.3396.99 (on Windows); results might differ in other browsers
   *   var dt = new Date("2018-07-16T01:47:13.842Z");
   *   dt.toLocaleString("us");  // returns "7/15/2018, 9:47:13 PM" (US locale)
   *   dt.toLocaleString("en-US");  // returns "7/15/2018, 9:47:13 PM" (US/English locale)
   *   dt.toLocaleString("es-US");  // returns "15/7/2018 9:47:13 p. m." (US/Spanish locale)
   *   dt.toLocaleString("es-US");  // returns "15/07/2018 à 21:47:13" (US/French locale)
   *   dt.toLocaleString("es");  // returns "15/7/2018 21:47:13" (Spanish locale)
   *   dt.toLocaleString("fr");  // returns "15/07/2018 à 21:47:13" (French locale)
   *   dt.toLocaleString("fr-CA");  // returns "2018-07-15 21 h 47 min 13 s" (French-Canadian locale)
   *   dt.toLocaleString("ru");  // returns "15.07.2018, 21:47:13" (Russian locale)
   *   dt.toLocaleString("zh");  // returns "2018/7/15 下午9:47:13" (Chinese locale)
   * </pre>
   *
   * @param locale A string with a BCP 47 language tag (refer to the MDN docs for possible values)
   * @return A string representing the given date according to language-specific conventions.
   */
  public final native String toLocaleString(String locale) /*-{
    return this.toLocaleString(locale);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleString">{@code
   * toLocaleString()}</a> method, which returns a string with a language sensitive representation of this date. The new
   * locales and options arguments let applications specify the language whose formatting conventions should be used and
   * customize the behavior of the function. In older implementations, which ignore the locales and options arguments,
   * the locale used and the form of the string returned are entirely implementation dependent.
   *
   * @param locales an array of BCP 47 language tag strings (refer to the MDN docs for possible values)
   * @return A string representing the given date according to language-specific conventions.
   * @see #toLocaleString(String)
   */
  public final native String toLocaleString(JsStringArray locales) /*-{
    return this.toLocaleString(locales);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleString">{@code
   * toLocaleString()}</a> method, which returns a string with a language sensitive representation of this date. The new
   * locales and options arguments let applications specify the language whose formatting conventions should be used and
   * customize the behavior of the function. In older implementations, which ignore the locales and options arguments,
   * the locale used and the form of the string returned are entirely implementation dependent.
   *
   * @param locales an array of BCP 47 language tag strings (refer to the MDN docs for possible values)
   * @param options optional object containing additional options (refer to the MDN docs for possible values)
   * @return A string representing the given date according to language-specific conventions.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleString">MDN
   *     Reference</a>
   */
  public final native String toLocaleString(JsStringArray locales, JsObject options) /*-{
    return this.toLocaleString(locales, options);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleTimeString">{@code
   * toLocaleTimeString()}</a> method, which returns a string with a language sensitive representation of the time
   * portion of this date. The new locales and options arguments let applications specify the language whose formatting
   * conventions should be used and allow to customize the behavior of the function. In older implementations, which
   * ignore the locales and options arguments, the locale used and the form of the string returned are entirely
   * implementation dependent.
   * <p>
   * This version of the method uses the specified locale
   * <p>
   * <b>Example</b>:
   * <pre>
   *   // NOTE: this example was tested using Chrome 67.0.3396.99 (on Windows); results might differ in other browsers
   *   var dt = new Date("2018-07-16T01:47:13.842Z");
   *   dt.toLocaleTimeString("us");  // returns "9:47:13 PM" (US locale)
   *   dt.toLocaleTimeString("en-US");  // returns "9:47:13 PM" (US/English locale)
   *   dt.toLocaleTimeString("es-US");  // returns "9:47:13 p. m." (US/Spanish locale)
   *   dt.toLocaleTimeString("es-US");  // returns "21:47:13" (US/French locale)
   *   dt.toLocaleTimeString("es");  // returns "21:47:13" (Spanish locale)
   *   dt.toLocaleTimeString("fr");  // returns "21:47:13" (French locale)
   *   dt.toLocaleTimeString("fr-CA");  // returns "21 h 47 min 13 s" (French-Canadian locale)
   *   dt.toLocaleTimeString("ru");  // returns "21:47:13" (Russian locale)
   *   dt.toLocaleTimeString("zh");  // returns "下午9:47:13" (Chinese locale)
   * </pre>
   *
   * @param locale A string with a BCP 47 language tag (refer to the MDN docs for possible values)
   * @return A string representing the given date according to language-specific conventions.
   */
  public final native String toLocaleTimeString(String locale) /*-{
    return this.toLocaleTimeString(locale);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleTimeString">{@code
   * toLocaleTimeString()}</a> method, which returns a string with a language sensitive representation of the time
   * portion of this date. The new locales and options arguments let applications specify the language whose formatting
   * conventions should be used and allow to customize the behavior of the function. In older implementations, which
   * ignore the locales and options arguments, the locale used and the form of the string returned are entirely
   * implementation dependent.
   *
   * @param locales an array of BCP 47 language tag strings (refer to the MDN docs for possible values)
   * @return A string representing the given date according to language-specific conventions.
   * @see #toLocaleTimeString(String)
   */
  public final native String toLocaleTimeString(JsStringArray locales) /*-{
    return this.toLocaleTimeString(locales);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleTimeString">{@code
   * toLocaleTimeString()}</a> method, which returns a string with a language sensitive representation of the time
   * portion of this date. The new locales and options arguments let applications specify the language whose formatting
   * conventions should be used and allow to customize the behavior of the function. In older implementations, which
   * ignore the locales and options arguments, the locale used and the form of the string returned are entirely
   * implementation dependent.
   *
   * @param locales an array of BCP 47 language tag strings (refer to the MDN docs for possible values)
   * @param options optional object containing additional options (refer to the MDN docs for possible values)
   * @return A string representing the given date according to language-specific conventions.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleTimeString">MDN
   *     Reference</a>
   */
  public final native String toLocaleTimeString(JsStringArray locales, JsObject options) /*-{
    return this.toLocaleTimeString(locales, options);
  }-*/;

  /**
   * Invokes the <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toString">{@code
   * toString()}</a> method, which returns a string representing the specified Date object.
   * <p>
   * NOTE: we provide this method because {@link JavaScriptObject#toString()} is {@code final}
   * <p>
   *   <b>Example</b>:
   * <pre>
   *   // NOTE: this example was tested using Chrome 67.0.3396.99 (on Windows); results might differ in other browsers
   *   var dt = new Date("2018-07-16T01:47:13.842Z");
   *   dt.toString();  // returns "Sun Jul 15 2018 21:47:13 GMT-0400 (Eastern Daylight Time)"
   * </pre>
   *
   * @return A string representing the given date (always in American English).
   */
  public final native String toStringNative() /*-{
    return this.toString();
  }-*/;


  /**
   * @return a {@link java.util.Date java.util.Date} representing this JavaScript {@code Date}
   */
  public final Date toDate() {
    return new Date((long)getTime());
  }

  /**
   * @return a new {@link JsDate} instance representing the same date/time value.
   */
  public final JsDate copy() {
    return create(getTime());
  }

  /**
   * Adds (or subtracts) the given quantity of the given time unit to/from this date, returning a new instance
   * of {@link JsDate} for the result (this is a non-mutating operation).
   *
   * @param unit the time unit for the given amount (will be used to select the appropriate setter method);
   *   must be at least {@link TimeUnit#MILLISECONDS MILLISECONDS} and at most {@link TimeUnit#YEARS YEARS}
   * @param amount the quantity to add (pass a negative value to subtract)
   * @return A new instance representing the result of adding the given quantity to this date
   * @throws IllegalArgumentException if the given unit is less than {@link TimeUnit#MILLISECONDS MILLISECONDS}
   * @see java.util.Calendar#add(int, int)
   */
  public final JsDate add(TimeUnit unit, int amount) {
    JsDate date = copy();
    switch (unit) {
      case MILLISECONDS:
        date.setMilliseconds(date.getMilliseconds() + amount);
        return date;
      case SECONDS:
        date.setSeconds(date.getSeconds() + amount);
        return date;
      case MINUTES:
        date.setMinutes(date.getMinutes() + amount);
        return date;
      case HOURS:
        date.setHours(date.getHours() + amount);
        return date;
      case DAYS:
        date.setDate(date.getDate() + amount);
        return date;
      case WEEKS:
        // JsDate doesn't have a setter for "weeks", so we'll just use the one for days multiplied by 7
        date.setDate(date.getDate() + amount * 7);
        return date;
      case MONTHS:
        date.setMonth(date.getMonth() + amount);
        return date;
      case YEARS:
        date.setFullYear(date.getFullYear() + amount);
        return date;
      default:
        throw new IllegalArgumentException("JS Date doesn't support " + unit.name().toLowerCase());
    }
  }

  /**
   * Adds native {@code equals} and {@code hashCode} functions to {@code Date.prototype} for comparing {@link JsDate}
   * instances using the Java {@link #equals(Object)} and {@link #hashCode()} methods.
   * <p>
   * Due to the <a href="https://github.com/gwtproject/old_google_code_wiki/blob/master/OverlayTypes.wiki.md#restrictions-on-overlay-types">
   * restrictions on overlay types</a>, inheritance is not allowed, so we can't override these methods
   * in Java code, but we can use this hack to compensate for it.
   * <blockquote style="font-style: italic;">
   * Note: this hack works because {@link JavaScriptObject#equals(Object)} and {@link JavaScriptObject#hashCode()}
   * check the underlying JS object for presence of native methods with the same name, and delegate to them if possible.
   * That seems to be a hook created by the GWT devs to enable this exact hack, because there's not a single native
   * object defined in standard JavaScript API that provides these methods.
   * </blockquote>
   * On the other hand, there seems to be no way to implement {@link Comparable} (GWT Compiler will throw exception),
   * so the only options for comparing instances are to use {@link #equals(Object)} (using this hack)
   * or an external comparator (see {@link #comparator()}).
   */
  private static native void addEqualsAndHashCodeToPrototype() /*-{
    if (!Date.prototype.equals) {
      Date.prototype.equals = function(other) {
        return (other instanceof Date) && (this.getTime() === other.getTime());
      }
    }
    if (!Date.prototype.hashCode) {
      Date.prototype.hashCode = function() {
        return this.getTime() % @java.lang.Integer::MAX_VALUE;
      }
    }
  }-*/;

  /**
   * @return comparator based on {@link #getTime()}
   */
  public static Comparator<JsDate> comparator() {
    return Comparator.comparingDouble(com.google.gwt.core.client.JsDate::getTime);
  }

}
