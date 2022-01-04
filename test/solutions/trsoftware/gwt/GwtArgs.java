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

package solutions.trsoftware.gwt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import com.google.gwt.dev.About;
import com.google.gwt.dev.GwtVersion;
import com.google.gwt.dev.util.arg.ArgHandlerFilterJsInteropExports;
import com.google.gwt.dev.util.arg.ArgHandlerSetProperties;
import com.google.gwt.junit.JUnitShell;
import com.google.gwt.junit.client.GWTTestCase;
import solutions.trsoftware.commons.client.testutil.RunStyleValue;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.compare.ComparisonOperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the value of the {@value #SYS_PROP_GWT_ARGS} system property (which is consumed by JUnitShell) into a multimap,
 * and allows modifying it dynamically prior to running the unit tests.
 * <p>
 * <b>Reference</b> (output from running an instance of {@link GWTTestCase} with {@code -Dgwt.args="-help"}):
 * <p>
 * <pre>
 Google Web Toolkit 2.5.0
 JUnitShell [-port port-number | "auto"] [-whitelist whitelist-string] [-blacklist blacklist-string] [-logdir directory] [-logLevel level] [-gen dir] [-codeServerPort port-number | "auto"] [-war dir] [-deploy dir] [-extra dir] [-workDir dir] [-style style] [-ea] [-XdisableClassMetadata] [-XdisableCastChecking] [-draftCompile] [-localWorkers count] [-prod] [-testMethodTimeout minutes] [-testBeginTimeout minutes] [-runStyle runstyle[:args]] [-notHeadless] [-standardsMode] [-quirksMode] [-Xtries 1] [-userAgents userAgents]
 where
 -port                   Specifies the TCP port for the embedded web server (defaults to 8888)
 -whitelist              Allows the user to browse URLs that match the specified regexes (comma or space separated)
 -blacklist              Prevents the user browsing URLs that match the specified regexes (comma or space separated)
 -logdir                 Logs to a file in the given directory, as well as graphically
 -logLevel               The level of logging detail: ERROR, WARN, INFO, TRACE, DEBUG, SPAM, or ALL
 -gen                    Debugging: causes normally-transient generated types to be saved in the specified directory
 -codeServerPort         Specifies the TCP port for the code server (defaults to 9997)
 -war                    The directory into which deployable output files will be written (defaults to 'war')
 -deploy                 The directory into which deployable but not servable output files will be written (defaults to 'WEB-INF/deploy' under the -war directory/jar, and may be the same as the -extra directory/jar)
 -extra                  The directory into which extra files, not intended for deployment, will be written
 -workDir                The compiler's working directory for internal use (must be writeable; defaults to a system temp dir)
 -style                  Script output style: OBF[USCATED], PRETTY, or DETAILED (defaults to OBF)
 -ea                     Debugging: causes the compiled output to check assert statements
 -XdisableClassMetadata  EXPERIMENTAL: Disables some java.lang.Class methods (e.g. getName())
 -XdisableCastChecking   EXPERIMENTAL: Disables run-time checking of cast operations
 -draftCompile           Enable faster, but less-optimized, compilations
 -localWorkers           The number of local workers to use when compiling permutations
 -prod                   Causes your test to run in production (compiled) mode (defaults to development mode)
 -testMethodTimeout      Set the test method timeout, in minutes
 -testBeginTimeout       Set the test begin timeout (time for clients to contact server), in minutes
 -runStyle               Selects the runstyle to use for this test.  The name is a suffix of com.google.gwt.junit.RunStyle or is a fully qualified class name, and may be followed with a colon and an argument for this runstyle.  The specified class mustextend RunStyle.
 -notHeadless            Causes the log window and browser windows to be displayed; useful for debugging
 -standardsMode          Run each test using an HTML document in standards mode (rather than quirks mode)
 -quirksMode             Run each test using an HTML document in quirks mode (rather than standards mode)
 -Xtries                 EXPERIMENTAL: Sets the maximum number of attempts for running each test method
 -userAgents             Specify the user agents to reduce the number of permutations for remote browser tests; e.g. ie6,ie8,safari,gecko1_8,opera
 * </pre>
 * <p>
 * <pre>
 Google Web Toolkit 2.8.2
 JUnitShell [-port port-number | "auto"] [-logdir directory] [-logLevel (ERROR|WARN|INFO|TRACE|DEBUG|SPAM|ALL)] [-gen dir] [-codeServerPort port-number | "auto"] [-war dir] [-deploy dir] [-extra dir] [-workDir dir] [-sourceLevel [auto, 1.8]] [-style (DETAILED|OBFUSCATED|PRETTY)] [-[no]checkAssertions] [-X[no]checkCasts] [-X[no]classMetadata] [-[no]draftCompile] [-localWorkers count] [-Xnamespace (NONE|PACKAGE)] [-optimize level] [-[no]incremental] [-[no]generateJsInteropExports] [-includeJsInteropExports/excludeJsInteropExports regex] [-setProperty name=value,value...] [-X[no]closureFormattedOutput] [-[no]devMode] [-testMethodTimeout minutes] [-testBeginTimeout minutes] [-runStyle runstyle[:args]] [-[no]showUi] [-Xtries 1] [-userAgents userAgents]
 where
 -port                                             Specifies the TCP port for the embedded web server (defaults to 8888)
 -logdir                                           Logs to a file in the given directory, as well as graphically
 -logLevel                                         The level of logging detail: ERROR, WARN, INFO, TRACE, DEBUG, SPAM or ALL (defaults to WARN)
 -gen                                              Debugging: causes normally-transient generated types to be saved in the specified directory
 -codeServerPort                                   Specifies the TCP port for the code server (defaults to 9997 for classic Dev Mode or 9876 for Super Dev Mode)
 -war                                              The directory into which deployable output files will be written (defaults to 'war')
 -deploy                                           The directory into which deployable but not servable output files will be written (defaults to 'WEB-INF/deploy' under the -war directory/jar, and may be the same as the -extra directory/jar)
 -extra                                            The directory into which extra files, not intended for deployment, will be written
 -workDir                                          The compiler's working directory for internal use (must be writeable; defaults to a system temp dir)
 -sourceLevel                                      Specifies Java source level (defaults to 1.8)
 -style                                            Script output style: DETAILED, OBFUSCATED or PRETTY (defaults to OBFUSCATED)
 -[no]checkAssertions                              Include assert statements in compiled output. (defaults to OFF)
 -X[no]checkCasts                                  EXPERIMENTAL: DEPRECATED: use jre.checks.checkLevel instead. (defaults to OFF)
 -X[no]classMetadata                               EXPERIMENTAL: Include metadata for some java.lang.Class methods (e.g. getName()). (defaults to ON)
 -[no]draftCompile                                 Compile quickly with minimal optimizations. (defaults to OFF)
 -localWorkers                                     The number of local workers to use when compiling permutations
 -Xnamespace                                       Puts most JavaScript globals into namespaces. Default: PACKAGE for -draftCompile, otherwise NONE
 -optimize                                         Sets the optimization level used by the compiler.  0=none 9=maximum.
 -[no]incremental                                  Compiles faster by reusing data from the previous compile. (defaults to OFF)
 -[no]generateJsInteropExports                     Generate exports for JsInterop purposes. If no -includeJsInteropExport/-excludeJsInteropExport provided, generates all exports. (defaults to OFF)
 -includeJsInteropExports/excludeJsInteropExports  Include/exclude members and classes while generating JsInterop exports. Flag could be set multiple times to expand the pattern. (The flag has only effect if exporting is enabled via -generateJsInteropExports)
 -setProperty                                      Set the values of a property in the form of propertyName=value1[,value2...].
 -X[no]closureFormattedOutput                      EXPERIMENTAL: Enables Javascript output suitable for post-compilation by Closure Compiler (defaults to OFF)
 -[no]devMode                                      Runs tests in Development Mode, using the Java virtual machine. (defaults to OFF)
 -testMethodTimeout                                Set the test method timeout, in minutes
 -testBeginTimeout                                 Set the test begin timeout (time for clients to contact server), in minutes
 -runStyle                                         Selects the runstyle to use for this test.  The name is a suffix of com.google.gwt.junit.RunStyle or is a fully qualified class name, and may be followed with a colon and an argument for this runstyle.  The specified class mustextend RunStyle.
 -[no]showUi                                       Causes the log window and browser windows to be displayed; useful for debugging. (defaults to OFF)
 -Xtries                                           EXPERIMENTAL: Sets the maximum number of attempts for running each test method
 -userAgents                                       Specify the user agents to reduce the number of permutations for remote browser tests; e.g. ie8,safari,gecko1_8
 * </pre>
 *
 * @see JUnitShell
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTesting.html#passingTestArguments">
 *   "Passing Arguments to the Test Infrastructure" (GWT Testing Guide)</a>
 */
public class GwtArgs /*extends LinkedHashMap<String, String>*/ {

  public static final String SYS_PROP_GWT_ARGS = "gwt.args";

  public static final String LOCAL_WORKERS = "localWorkers";
  public static final String DRAFT_COMPILE = "draftCompile";
  public static final String USER_AGENTS = "userAgents";
  public static final String RUN_STYLE = "runStyle";
  public static final String GEN = "gen";
  public static final String WAR = "war";
  public static final String WORK_DIR = "workDir";
  public static final String PROD_MODE = "prod";
  public static final String WEB_MODE = "web";
  /**
   * Property added in GWT 2.8.x: "Runs tests in Development Mode, using the Java virtual machine. (defaults to OFF)"
   */
  public static final String DEV_MODE = "devMode";
  /**
   * @see ArgHandlerSetProperties
   */
  public static final String SET_PROPERTY = "setProperty";

  /**
   * Args that may appear more than once.
   * @see ArgHandlerSetProperties
   * @see ArgHandlerFilterJsInteropExports
   */
  @VisibleForTesting
  static final Set<String> multiArgs = ImmutableSet.of(SET_PROPERTY, "includeJsInteropExports", "excludeJsInteropExports");

  /**
   * Splits the value of a {@value SET_PROPERTY} argument into a name-value pair.
   * Uses the same splitter settings as {@link ArgHandlerSetProperties#setString(String)}.
   */
  private static final Splitter nameValueSplitter = Splitter.on("=").trimResults().omitEmptyStrings();

  private static GwtArgs instance;

  public synchronized static GwtArgs get() {
    if (instance == null)
      instance = new GwtArgs(System.getProperty(SYS_PROP_GWT_ARGS));
    return instance;
  }

  private final ListMultimap<String, String> argsMultimap;

  public GwtArgs(String argsString) {
    argsMultimap = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    // parse gwt.args into a map
    String[] args = synthesizeArgs(argsString);
    String prevArg = null;
    for (String x : args) {
      // is this a new argument or the value of the previous argument?
      if (x.startsWith("-")) {
        // this is a new argument
        maybeAdd(prevArg, null);
        prevArg = x;
      }
      else {
        // this is the value of the previous argument
        assert prevArg != null;
        maybeAdd(prevArg, x);
        prevArg = null;
      }
    }
    maybeAdd(prevArg, null);
  }

  private void maybeAdd(String key, String value) {
    if (key != null) {
      // the previous argument didn't have a value
      put(key.substring(1), value);  // we strip off the leading "-" from the key
    }
  }

  /**
   * Synthesize command line arguments from a system property.
   * NOTE: this code is borrowed (copied almost verbatim) from {@link JUnitShell#synthesizeArgs()}
   */
  private static String[] synthesizeArgs(String args) {
    ArrayList<String> argList = new ArrayList<String>();
    if (args != null) {
      // Match either a non-whitespace, non start of quoted string, or a
      // quoted string that can have embedded, escaped quoting characters
      Pattern pattern = Pattern.compile("[^\\s\"]+|\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"");
      Matcher matcher = pattern.matcher(args);
      Pattern quotedArgsPattern = Pattern.compile("^([\"'])(.*)([\"'])$");

      while (matcher.find()) {
        // Strip leading and trailing quotes from the arg
        String arg = matcher.group();
        Matcher qmatcher = quotedArgsPattern.matcher(arg);
        if (qmatcher.matches()) {
          argList.add(qmatcher.group(2));
        }
        else {
          argList.add(arg);
        }
      }
    }
    return argList.toArray(new String[argList.size()]);
  }

  // java.util.Map-like interface methods for compatibility with the old Map-based implementation:

  /**
   * Returns the current value for the given argument.
   * <p><strong>Note:</strong>
   * A {@code null} return value could mean one of 2 things: either the argument doesn't exist
   * or it exists but doesn't have a value. Use {@link #containsKey(String)} to disambiguate.
   *
   * @param key the arg name
   * @return current value for the given argument or {@code null} if either it doesn't exist or doesn't have a value
   */
  @Nullable
  public String get(String key) {
    List<String> values = argsMultimap.get(key);
    if (values.size() > 1) {
      assert multiArgs.contains(key);
      // TODO: rather than throwing, maybe just print a warning and return the last value?
      throw new IllegalArgumentException("Multiple values present for the -" + key + " arg; use getAll(\"" + key + "\")");  // TODO: impl getAll
    }
    return Iterables.getLast(values, null);  // TODO: maybe Iterables.getFirst instead?
  }

  /**
   * @return all the values for the given arg as an unmodifiable list; if the arg is missing this will be an empty
   * list rather than {@code null}.
   */
  @Nonnull
  public List<String> getAll(String key) {
    // returning an unmodifiable list to force all changes to the underlying multimap to be made via put or setProperty
    return Collections.unmodifiableList(argsMultimap.get(key));
  }

  public boolean containsKey(String key) {
    return argsMultimap.containsKey(key);
  }

  public void put(String key, String value) {
    // TODO: check for null value?  especially if it's a multi-valued arg
    if (SET_PROPERTY.equals(key)) {
      Map.Entry<String, String> nvPair = splitProperty(value);
      setProperty(nvPair.getKey(), nvPair.getValue());
    }
    else if (multiArgs.contains(key)) {
      argsMultimap.put(key, value);
    }
    else {
      // this arg cannot be repeated
      argsMultimap.replaceValues(key, Collections.singletonList(value));
    }
  }

  /**
   * Removes all occurrences of the given arg.
   *
   * @param key the arg name
   * @return {@code true} if anything was removed by this operation
   */
  public boolean remove(String key) {
    return !argsMultimap.removeAll(key).isEmpty();
  }

  /**
   * Removes a specific occurrence of the given arg.
   *
   * @param key the arg name
   * @param value the arg value
   * @return {@code true} if anything was removed by this operation
   */
  public boolean remove(String key, String value) {
    return argsMultimap.remove(key, value);
  }

  /**
   * @return all args as key-value pairs (NOTE: changes to the returned collection or its entries will change
   * the internal state of this class)
   * @see Multimap#entries()
   */
  public Collection<Map.Entry<String, String>> getEntries() {
    return argsMultimap.entries();
  }

  // TODO: provide special accessors for -setProperty? (i.e. to get current value / set a new value of a specific property)

  /**
   * Adds or modifies a {@value SET_PROPERTY} argument for the given property name.
   * @param name property name
   * @param value a comma-separated string of values for this property
   * @return {@code true} if replaced an existing property setting or {@code false} if added a new one.
   */
  public boolean setProperty(String name, String value) {
    Objects.requireNonNull(name, "property name");
    Objects.requireNonNull(value, "property value");
    List<String> nameValuePairs = argsMultimap.get(SET_PROPERTY);
    // search existing properties in reverse order
    ListIterator<String> it = nameValuePairs.listIterator(nameValuePairs.size());
    if (findProperty(name, it) != null) {
      // property was already defined: replace its value
      it.set(joinProperty(name, value));
      return true;
    }
    else {
      // property was't already defined
      nameValuePairs.add(joinProperty(name, value));
      return false;
    }
  }

  /**
   * Searches through the existing {@value SET_PROPERTY} arguments for the given property name.
   *
   * @param name property name
   * @return the current setting for the given property (as a comma-separated string of values),
   * or {@code null} if not found.
   */
  @Nullable
  public String getProperty(String name) {
    Objects.requireNonNull(name, "property name");
    // TODO: maybe extract code duplicated in our setProperty method?
    List<String> nameValuePairs = argsMultimap.get(SET_PROPERTY);
    // search existing properties in reverse order
    ListIterator<String> it = nameValuePairs.listIterator(nameValuePairs.size());
    return findProperty(name, it);
  }

  /**
   * Removes the {@value SET_PROPERTY} argument corresponding to the given property name.
   * @param name property name
   * @return {@code true} if the property was found and removed or {@code false} if not found
   */
  public boolean removeProperty(String name) {
    Objects.requireNonNull(name, "property name");
    List<String> nameValuePairs = argsMultimap.get(SET_PROPERTY);
    // search existing properties in reverse order
    ListIterator<String> it = nameValuePairs.listIterator(nameValuePairs.size());
    if (findProperty(name, it) != null) {
      // property was already defined: replace its value
      it.remove();
      return true;
    }
    return false;
  }

  /**
   * Searches through the existing {@value SET_PROPERTY} arguments for the given property name
   * using an iterator obtained from the list returned by {@code argsMultimap.get(SET_PROPERTY)}.
   * <p>
   * If the property is found, returns its current value and leaves the iterator in a state
   * where {@link ListIterator#set(Object)} and {@link ListIterator#remove()} can be used to modify or remove
   * the corresponding {@value SET_PROPERTY} argument.
   *
   * @param name property name
   * @param it an iterator obtained from the list returned by {@code argsMultimap.get(SET_PROPERTY)},
   * initialized to start iteration from the back of the list
   * @return the current value for the existing property or {@code null} if not found.
   */
  @Nullable
  private String findProperty(String name, ListIterator<String> it) {
    while (it.hasPrevious()) {
      Map.Entry<String, String> nvPair = splitProperty(it.previous());
      if (nvPair.getKey().equals(name)) {
        return nvPair.getValue();
      }
    }
    return null;
  }

  /**
   * Splits the value of a {@value SET_PROPERTY} argument into a {@code name=values} pair.
   * @param propertyString e.g. {@code "foo=a,b,c"}
   * @return an entry with property name as the key
   * @throws IllegalArgumentException if the given string is not a valid {@value SET_PROPERTY} argument.
   */
  private static Map.Entry<String, String> splitProperty(String propertyString) {
    List<String> nvPair = nameValueSplitter.splitToList(propertyString);
    if (nvPair.size() != 2)
      throw new IllegalArgumentException("Invalid value for -" + SET_PROPERTY + ": " + StringUtils.quote(propertyString));
    return new AbstractMap.SimpleEntry<>(nvPair.get(0), nvPair.get(1));
  }

  /**
   * @return "name=value"
   */
  private static String joinProperty(String name, String value) {
    return name + '=' + value;
  }

  /**
   * {@value SYS_PROP_GWT_ARGS} also indicate whether JUnitShell should run tests in <i>production mode</i> (as JavaScript),
   * or in <i>dev mode</i> (in a JVM).
   * <ul>
   *   <li>Prior to GWT 2.8.x, <i>dev mode</i> was the default unless the args contained the {@code "-prod"} flag (or its deprecated alias {@code "-web"})</li>
   *   <li>GWT 2.8.x got rid of those flags, and <i>production mode</i> is the new default unless the args contain a {@code "-devMode"} flag (added in 2.8)</li>
   * </ul>
   *
   * @return true iff the args indicate that the test should be run in <i>production mode</i> (i.e. compiled to JS)
   */
  public boolean isWebMode() {
    GwtVersion gwtVersion = getGwtVersion();
    if (ComparisonOperator.GT.compare(gwtVersion, new GwtVersion(null))
        && ComparisonOperator.LT.compare(gwtVersion, new GwtVersion("2.8.0-rc1"))) {
      // we have a GWT version older than 2.8.0-rc1
      return containsKey(PROD_MODE) || containsKey(WEB_MODE);
    }
    // we have a newer GWT version (or the version number is not available)
    return !containsKey(DEV_MODE);
  }

  protected GwtVersion getGwtVersion() {
    return About.getGwtVersionObject();
  }

  /**
   * @return the args formatted as a string that can be used as a new value for the {@value SYS_PROP_GWT_ARGS} system property.
   */
  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder();
    for (Map.Entry<String, String> entry : getEntries()) {
      if (ret.length() > 0)
        ret.append(" ");
      ret.append("-").append(entry.getKey());
      String value = entry.getValue();
      // TODO: check for empty string?
      if (value != null) {
        ret.append(" ");
        if (!value.matches("[\\w,=]+"))
          // quote the value if it has any non-word chars ('=' and ',' also okay)
          ret.append("\"").append(value).append("\"");
        else
          ret.append(value);
      }
    }
    return ret.toString();
  }

  /**
   * @return the parsed value of the {@code -runStyle} arg, or {@code null} if this arg not specified
   */
  public RunStyleValue getRunStyle() {
    String value = get(RUN_STYLE);
    if (StringUtils.notBlank(value))
      return new RunStyleValue(value);
    return null;
  }

  /**
   * @return the set of browser names parsed from a {@code -runStyle HtmlUnit:...} arg, or an empty set if
   * the {@code -runStyle} arg is not specified or doesn't start with the {@code "HtmlUnit"} prefix.
   */
  public Set<String> getRunStyleHtmlUnitArgs() {
    RunStyleValue runStyle = getRunStyle();
    if (runStyle != null && "HtmlUnit".equals(runStyle.getName())) {
      return runStyle.getArgs();
    }
    return Collections.emptySet();
  }

}
