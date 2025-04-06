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
import com.google.gwt.core.client.JsArrayMixed;

/**
 * A JSNI overlay type for the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Console">window.console</a> object.
 *
 * Supports a subset of the methods provided by the various browser implementations of {@code window.console}
 * (e.g. {@link #logVarArgs}, {@link #time}, {@link #timeEnd}, etc.)
 * <p style="font-style:italic;">
 * NOTE: {@link elemental.js.html.JsConsole} is a more full-featured implementation of this concept and is part of GWT's experimental new "Elemental" package.
 * However, that class doesn't compensate for lack of functionality of certain methods, and also Elemental only
 * works with SuperDevMode (<a href="http://stackoverflow.com/questions/17428265/adding-elemental-to-gwt">GWT compiler
 * error in normal DevMode</a>)
 * </p>
 *
 * @see <a href="http://getfirebug.com/wiki/index.php/Console_API">Firebug Console Reference</a>
 * @see <a href="https://developers.google.com/chrome-developer-tools/docs/console-api">Chrome Dev Tools Console Reference</a>
 * 
 * @since Mar 26, 2013
 * @author Alex
 */
public class JsConsole extends JavaScriptObject {

  /**
   * The available verbosity levels, can be used to select the output method to be invoked.
   * @see #log(Level, String)
   * @see #log(Level, JsArrayMixed)
   */
  public enum Level {
    DEBUG, INFO, WARN, ERROR;

    public String getJsMethodName() {
      return name().toLowerCase();
    }
  }

  // Overlay types always have protected, zero-arg constructors, because the object must have been instantiated in javascript
  protected JsConsole() { }

  /**
   * @return the {@code window.console} object if the browser supports it, or an empty object if not.  Guaranteed
   * to never return {@code null}.
   */
  public static native JsConsole get() /*-{
    return $wnd.console || {};
  }-*/;

  /**
   * Alias for <a href="https://developer.mozilla.org/en-US/docs/Web/API/Console/assert">{@code console.assert}</a>:
   * Writes an error message to the console if the assertion is false. If the assertion is true, nothing happens.
   * NOTE: we can't name this method {@code assert} (to match its JS name) because that's a reserved keyword in Java.
   */
  public final native void assertion(Object condition, Object message) /*-{
    this.assert && this.assert(condition, message);
  }-*/;

  /**
   * Clear the console.
   */
  public final native void clear() /*-{
    this.clear && this.clear();
  }-*/;

  /** Log the number of times this line has been called with the given label. */
  public final native void count(Object arg) /*-{
    this.count && this.count(arg);
  }-*/;

  public final native boolean implementsCount() /*-{
    return !!this.count;
  }-*/;

  /**
   * Logs an object or a string with {@code console.log}
   *
   * @see #logVarArgs(JavaScriptObject)
   * @see #logVarArgs(Level, JavaScriptObject)
   * @see #log(Level, String)
   */
  public final native void log(Object arg) /*-{
    this.log && this.log(arg);
  }-*/;

  /**
   * Invokes {@code console.log} with multiple arguments using {@code console.log.apply(argsArray)}.
   * <p>
   * Can use {@link JsArrayMixed} to easily construct the args array.  For example:
   * <pre>
   *   {@link #logVarArgs}(JsMixedArray.create().add("Event object: ").add(event))
   * </pre>
   *
   * @param argsArray must be an Array or an "array-like" object (otherwise {@code Function.apply}
   *   could throw a {@code TypeError}) an pass a {@link JsArrayMixed} to take advantage of {@code console.log}'s vararg capability. Example:

   * @see #logVarArgs(Level, JavaScriptObject)
   */
  public final native <A extends JavaScriptObject> void logVarArgs(A argsArray) /*-{
    this.log && this.log.apply(this, argsArray);
  }-*/;

  public final void log(Level level, String arg) {
    logVarArgs(level, JsMixedArray.create().add(arg));
  }

  /**
   * Selects the output method to call based on the given {@link Level}, and calls it with the given args.
   * This method takes advantage of the console's vararg capability, unlike {@link #log(Level, String)}.
   * <p>
   * Example:
   * <pre>
   *   {@link #logVarArgs(Level, JavaScriptObject) log}(JsMixedArray.create().add("Event object: ").add(event))
   * </pre>
   * @param level the verbosity level
   * @param args must be an Array (can pass a {@link JsMixedArray} to construct the args array using method chaining)
   */
  public final <A extends JavaScriptObject> void logVarArgs(Level level, A args) {
    logVarArgs(level.getJsMethodName(), args);
  }

  /**
   * @return {@code true} iff this browser's console implementation supports the logging method corresponding the given verbosity level.
   */
  public final boolean supports(Level level) {
    return supports(level.getJsMethodName());
  }

  /**
   * @return {@code true} iff this browser's console implementation supports the logging method corresponding the given verbosity level.
   */
  private native boolean supports(String level) /*-{
    return !!this[level];
  }-*/;

  private native <A extends JavaScriptObject> void logVarArgs(String level, A args) /*-{
    var method = this[level] || this.log; // fall back on the log method
    method && method.apply(this, args);  // NOTE: we use method.apply because in Chromium 35 can't invoke console methods through a variable (see https://gist.github.com/euank/7523581)
  }-*/;

  /**
   * Invokes a {@code console} method that accepts a variable number of args, by executing the equivalent of
   * {@code console[name].apply(console, args)}.
   * <p>
   * If the invocation throws an error (e.g. if {@code argsArray} is not an array or an "array-like" object,
   * the error will be printed using {@code console.warn}, or if the given method isn't available,
   * will simply return {@code false}.
   *
   * @param name the name of the {@code console} method to invoke (e.g. "log", "debug", "trace", etc.)
   * @param argsArray must be an array or an "array-like" object such as {@code NodeList}, otherwise {@code Function.apply}
   *   throws a {@code TypeError}
   * @return {@code true} iff the invocation was successful
   */
  // TODO(10/9/2024): use this to add var-args support for all the methods in this class
  public final native boolean invokeVarArgsMethod(String name, JavaScriptObject argsArray) /*-{
    var method = this[name];
    if (method) {
      try {
        method.apply(this, argsArray);  // NOTE: another reason to use method.apply: in Chromium 35 can't invoke console methods through a variable (see https://gist.github.com/euank/7523581)
        return true;
      }
      catch (e) {
        // most likely because args is invalid for Function.apply (e.g. not array or array-like)
        if (this.warn)
          this.warn("Error invoking console." + name + "(", argsArray, "):", e);
      }
    }
    return false;
  }-*/;

  /**
   * Writes a message to the console with the visual "error" icon and color coding and a hyperlink to the line where it was called.
   * <p>Equivalent to calling {@link #log(Level, String)} with {@link Level#ERROR}.
   * To pass multiple arguments, call {@link #log(Level, JsArrayMixed)}
   * @deprecated Use {@link #log(Level, String)} or {@link #log(Level, JsArrayMixed)} instead.
   */
  public final native void error(Object arg) /*-{
    this.error && this.error(arg);
  }-*/;

  public final native boolean implementsError() /*-{
    return !!this.error;
  }-*/;
  
  /**
   * Informative logging information.  There's no real difference between this method and {@link #logVarArgs} in most browsers.
   * <p>Equivalent to calling {@link #log(Level, String)} with {@link Level#INFO}.
   * To pass multiple arguments, call {@link #log(Level, JsArrayMixed)}
   * @deprecated Use {@link #log(Level, String)} or {@link #log(Level, JsArrayMixed)} instead.
   */
  public final native void info(Object arg) /*-{
    this.info && this.info(arg);
  }-*/;

  public final native boolean implementsInfo() /*-{
    return !!this.info;
  }-*/;

  
  /**
   * Prints debugging info.  This is the lowest logging output level, and will likely not be displayed by default.
   * In Chrome, you can make these messages visible by selecting the "Verbose" filter level.
   * <p>Equivalent to calling {@link #log(Level, String)} with {@link Level#DEBUG}.
   * To pass multiple arguments, call {@link #log(Level, JsArrayMixed)}
   * @deprecated Use {@link #log(Level, String)} or {@link #log(Level, JsArrayMixed)} instead.
   */
  public final native void debug(Object arg) /*-{
    this.debug && this.debug(arg);
  }-*/;

  public final native boolean implementsDebug() /*-{
    return !!this.debug;
  }-*/;

  /**
   * Writes a message to the console with the visual "warning" icon and color coding and a hyperlink to the line where it was called.
   * <p>Equivalent to calling {@link #log(Level, String)} with {@link Level#WARN}.
   * To pass multiple arguments, call {@link #log(Level, JsArrayMixed)}
   * @deprecated Use {@link #log(Level, String)} or {@link #log(Level, JsArrayMixed)} instead.
   */
  public final native void warn(Object arg) /*-{
    this.warn && this.warn(arg);
  }-*/;

  public final native boolean implementsWarn() /*-{
    return !!this.warn;
  }-*/;

  /** "Writes a message to the console and opens a nested block to indent all future messages sent to the console. Call console.groupEnd() to close the block." */
  public final native void group(Object arg) /*-{
    this.group && this.group(arg);
  }-*/;

  public final native boolean implementsGroup() /*-{
    return !!this.group;
  }-*/;

  /** "Like console.group(), but the block is initially collapsed." */
  public final native void groupCollapsed(Object arg) /*-{
    this.groupCollapsed && this.groupCollapsed(arg);
  }-*/;

  public final native boolean implementsGroupCollapsed() /*-{
    return !!this.groupCollapsed;
  }-*/;

  /**
   * "Closes the most recently opened block created by a call to console.group()
   * or console.groupCollapsed()"
   */
  public final native void groupEnd() /*-{
    this.groupEnd();
  }-*/;

  public final native boolean implementsGroupEnd() /*-{
    return !!this.groupEnd;
  }-*/;

  /**
   * Some {@code window.console} implementations (like WebKit) support a markTimeline
   * method, by which an app can add an annotation to the Timeline section
   * of the browser's developer tools.  This is particularly useful for the
   * Speed Tracer chrome extension (see: https://developers.google.com/web-toolkit/speedtracer/logging-api )
   *
   * @deprecated superceded by {@link #timeStamp(Object)} (which is also non-standard)
   */
  public final native void markTimeline(Object arg) /*-{
    this.markTimeline && this.markTimeline(arg);
  }-*/;

  public final native boolean implementsMarkTimeline() /*-{
    return !!this.markTimeline;
  }-*/;

  /** "Turns on the JavaScript profiler. The optional argument title would contain the text to be printed in the header of the profile report." */
  public final native void profile(String title) /*-{
    this.profile && this.profile(title);
  }-*/;

  public final native boolean implementsProfile() /*-{
    return !!this.profile;
  }-*/;

  /** "Turns off the JavaScript profiler and prints its report." */
  public final native void profileEnd(String title) /*-{
    this.profileEnd && this.profileEnd(title);
  }-*/;

  public final native boolean implementsProfileEnd() /*-{
    return !!this.profileEnd;
  }-*/;

  /** "Creates a new timer under the given name. Call console.timeEnd(name) with the same name to stop the timer and print the time elapsed." */
  public final native void time(String title) /*-{
    this.time && this.time(title);
  }-*/;

  public final native boolean implementsTime() /*-{
    return !!this.time;
  }-*/;

  /** "Stops a timer created by a call to console.time(name) and writes the time elapsed." */
  public final native void timeEnd(String title) /*-{
    this.timeEnd && this.timeEnd(title);
  }-*/;

  public final native boolean implementsTimeEnd() /*-{
    return !!this.timeEnd;
  }-*/;

  public final native void timeStamp(Object arg) /*-{
    this.timeStamp && this.timeStamp(arg);
  }-*/;

  public final native boolean implementsTimeStamp() /*-{
    return !!this.timeStamp;
  }-*/;

  /** "Prints an interactive stack trace of JavaScript execution at the point where it is called." */
  public final native void trace(Object arg) /*-{
    this.trace && this.trace(arg);
  }-*/;

  public final native boolean implementsTrace() /*-{
    return !!this.trace;
  }-*/;


  // TODO(12/21/2024): experimental builder class:

  public static MessageBuilder newMessage() {
    return new MessageBuilder();
  }

  public static MessageBuilder newMessage(Level level) {
    return new MessageBuilder(level);
  }
  
  public static class MessageBuilder {

    private Level level;
    private JsMixedArray args = JsMixedArray.create();

    public MessageBuilder() {
      this(Level.INFO);
    }

    public MessageBuilder(Level level) {
      this.level = level;
    }

    public MessageBuilder setLevel(Level level) {
      this.level = level;
      return this;
    }

    /**
     * Appends an argument for the logging method.
     */
    public MessageBuilder append(String value) {
      args.add(value);
      return this;
    }

    /**
     * Appends an argument for the logging method
     */
    public MessageBuilder append(boolean value) {
      args.add(value);
      return this;
    }

    /**
     * Appends an argument for the logging method.
     */
    public MessageBuilder append(double value) {
      args.add(value);
      return this;
    }

    /**
     * Appends an argument for the logging method.
     * @return this array, for method chaining.
     * @param value
     */
    public MessageBuilder append(JavaScriptObject value) {
      args.add(value);
      return this;
    }

    /**
     * Appends several arguments for the logging method.
     * @return this array, for method chaining.
     * @param values
     */
    public MessageBuilder append(JavaScriptObject... values) {
      args.add(values);
      return this;
    }

    public void log() {
      JsConsole.get().logVarArgs(level, args);
    }

  }

}
