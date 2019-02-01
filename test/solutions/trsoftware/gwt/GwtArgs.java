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

package solutions.trsoftware.gwt;

import com.google.gwt.dev.About;
import com.google.gwt.dev.GwtVersion;
import com.google.gwt.junit.JUnitShell;
import com.google.gwt.junit.client.GWTTestCase;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.compare.ComparisonOperator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the value of the {@value #SYS_PROP_GWT_ARGS} system property (which is consumed by JUnitShell) into a map,
 * and allows modifying it dynamically prior to running the unit tests.
 * <p>
 * <b>Reference</b>:
 * <br>
 * <i>output from running an instance of {@link GWTTestCase} with {@code -Dgwt.args="-help"}:</i>
 * <ul>
 *   <li>GWT 2.5.0
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
 * </li>
 *
 * <li>GWT 2.8
 *   <pre>
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
 *   </pre>
 * </li>
 * </ul>
 *
 * @see JUnitShell
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTesting.html#passingTestArguments">
 *   "Passing Arguments to the Test Infrastructure" (GWT Testing Guide)</a>
 */
public class GwtArgs extends LinkedHashMap<String, String> {

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

  private static GwtArgs instance;

  public synchronized static GwtArgs get() {
    if (instance == null)
      instance = new GwtArgs(System.getProperty(SYS_PROP_GWT_ARGS));
    return instance;
  }

  public GwtArgs(String argsString) {
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

  /**
   * Sets key=newValue if the existing value for the given key does not start with the given prefix.
   */
  public void putIfValueNotStartsWith(String key, String valuePrefix, String newValue) {
    String value = get(key);
    if (value == null || !value.startsWith(valuePrefix)) {
      put(key, newValue);
    }
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
   * @return the args formatted as a string that can be used as the value for the system property.
   */
  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder();
    for (Map.Entry<String, String> entry : entrySet()) {
      if (ret.length() > 0)
        ret.append(" ");
      ret.append("-").append(entry.getKey());
      String value = entry.getValue();
      if (value != null) {
        ret.append(" ");
        if (!value.matches("\\w*"))
          // quote the value if it has any non-word chars
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

  /**
   * Parsed value of the {@code -runStyle} arg.
   * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTesting.html">GWT Testing Guide</a>
   * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTestingHtmlUnit.html">GWT Testing HTML Unit Guide</a>
   */
  public static class RunStyleValue {
    private String name;
    private Set<String> args;

    /**
     * @param value the {@code -runStyle} arg
     */
    public RunStyleValue(String value) {
      name = value;
      int colon = value.indexOf(':');
      if (colon >= 0) {
        name = value.substring(0, colon);
        String args = value.substring(colon + 1);
        this.args = SetUtils.newSet(args.split(","));
      } else {
        this.args = Collections.emptySet();
      }
    }

    public String getName() {
      return name;
    }

    public Set<String> getArgs() {
      return args;
    }
  }

}
