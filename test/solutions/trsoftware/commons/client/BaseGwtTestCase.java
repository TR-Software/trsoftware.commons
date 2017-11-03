/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client;

import com.google.gwt.junit.JUnitShell;
import com.google.gwt.junit.client.GWTTestCase;
import solutions.trsoftware.commons.client.util.MapUtils;
import solutions.trsoftware.commons.server.testutil.TempDirTestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This should be used as the base class for all GWT unit tests instead of {@link GWTTestCase}.  It overrides
 * {@link #runTest()} to set a few system properties in order to fix a bug and implement some speedups
 * (see {@link #maybeModifyGwtArgs()}).
 *
 * <p>
 * There are two versions of this class. This version is the binary version that is able to override {@link #runTest()},
 * but it's not compilable with GWT.  The other version is a translatable class that is used within the browser.
 * See the <code>translatable</code> subpackage for the translatable implementation.
 * </p>
 *
 * <b>JUnitShell Args</b> (Output from running an instance of {@link GWTTestCase} with {@code -Dgwt.args="-help"}):
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
 * @author Alex
 */
public abstract class BaseGwtTestCase extends GWTTestCase {


  private static boolean gwtArgsProcessed;

  @Override
  protected void runTest() throws Throwable {
    maybeModifyGwtArgs();
    super.runTest();
  }

  /**
   * Modifies the gwt.args property to allow web mode and hosted mode unit tests to run simultaneously
   * (ensures that their -war and -gen directories are different; see 2015_03_31_GWT_unit_test_useragent_property_bug.txt
   * and https://code.google.com/p/google-web-toolkit/issues/detail?id=9168).  This only needs to be done once
   * per lifetime of the JVM, hence the "maybe".  Doing it automatically like this lets us not worry with setting up
   * separate run configurations in our IDE for web mode and hosted mode GWT tests.
   *
   * We also modify the gwt.persistentunitcachedir setting to a globally-shared dir, because different runs are able
   * to reuse most of each-other's compiler unit cache output (see doc/gwt/experiments/PersistentUnitCacheUsageExperiment.txt)
   *
   */
  private static void maybeModifyGwtArgs() throws IOException {
    if (!gwtArgsProcessed) {
      gwtArgsProcessed = true;
      GwtArgs gwtArgs = GwtArgs.get();
      String ourClassName = BaseGwtTestCase.class.getSimpleName();
      {
        /*
         We tried the following optimization (adding -localWorkers to speed up the web mode compile):
           if (gwtArgs.isWebMode())
             gwtArgs.put("localWorkers", String.valueOf(Runtime.getRuntime().availableProcessors()));
         But encountered two problems with that, because com.google.gwt.dev.ExternalPermutationWorkerFactory.launchExternalWorker
         uses ManagementFactory.getRuntimeMXBean().getInputArguments() when setting up the worker java processes, which means that
         1) the worker processes will ignore the "gwt.args" settings that we're overriding here (they will use the original ones from the command line)
         2) the worker processes won't start on JDK1.6 because of https://bugs.openjdk.java.net/browse/JDK-7020384?page=com.atlassian.jira.plugin.system.issuetabpanels:all-tabpanel
            (JDK1.6 will incorrectly parse the -Dgwt.args from the command line when it contains spaces
         So instead of adding -localWorkers, we have to make sure that they are not being used at all:
        */
        if (gwtArgs.remove(GwtArgs.LOCAL_WORKERS) != null) {
          System.err.printf("%s is removing %s from %s to avoid errors (see inline comment for explanation)%n", ourClassName, GwtArgs.LOCAL_WORKERS, GwtArgs.SYS_PROP_GWT_ARGS);
        }
      }

      if (gwtArgs.isWebMode()) {
        if (!gwtArgs.containsKey(GwtArgs.RUN_STYLE) && !gwtArgs.containsKey(GwtArgs.USER_AGENTS)) {
          // if no -runStyle specified, we'll use our own setting, so that we can restrict the -userAgent property to
          // only include permutations for the browsers that will be testing on (otherwise,
          // JUnitShell will build permutations for all user.agent values, which takes longer to compile)
          // see http://www.gwtproject.org/doc/latest/DevGuideTestingHtmlUnit.html
          String runStyle;
          String userAgents;
          if (gwtArgs.containsKey(GwtArgs.DRAFT_COMPILE)) {
            // we'll take this to mean that the user wants a very fast test run, so we restrict the perms to only 1 browser
            // we choose FF because FF3 was the default setting in GWT2.5.0 with htmlunit-2.9, but with htmlunit-2.15 we only have FF17 and FF24 as options
            runStyle = "HtmlUnit:FF24";
            userAgents = "gecko1_8"; // we only need the permutation for the browser specified in -runStyle
          }
          else{
            // since the user is not passing -draftCompile, we're assuming that he's willing to wait longer to get a more thorough test run
            // htmlunit-2.15 supports FF17,FF24,IE8,IE9,IE11,Chrome, of which we choose only FF24,IE8,IE11,Chrome,
            // because FF17 seems redundant, and IE9 didn't seem to work when we tried it
            runStyle = "HtmlUnit:FF24,IE8,IE11,Chrome";
            userAgents = "gecko1_8,ie8,ie10,safari"; // we only need the permutation for the browser specified in -runStyle
            // we're using the user.agent property "ie10" for IE11, because currently user.agent="ie10" really means "IE10+"
          }
          gwtArgs.put(GwtArgs.RUN_STYLE, runStyle);
          gwtArgs.put(GwtArgs.USER_AGENTS, userAgents);
        }
      }

      // we need to use separate dirs depending on the configuration of the running tests to avoid a race condition
      // arising from tests using different builds using the same -war dir (see the doc for this method for more details)
      String userDir = System.getProperty("user.dir");
      String targetSubdirName = gwtArgs.isWebMode() ? "gwtWebModeTests" : "gwtHostedModeTests";

      if (gwtArgs.containsKey(GwtArgs.USER_AGENTS))
        targetSubdirName += "_" + gwtArgs.get(GwtArgs.USER_AGENTS);
      /*
      TODO: cont here: instead of placing targetSubdir in user.dir, we should place it in the project compile output dir
      (which is excluded from the project and won't mess with our version control); use ReflectionUtils.getCompilerOutputDir()
       */
      File tempDir = TempDirTestCase.createTempDir(BaseGwtTestCase.class.getName());
      File targetSubdir = new File(tempDir, targetSubdirName);
      assertTrue(targetSubdir.mkdir());
      File genDir = new File(targetSubdir, GwtArgs.GEN);
      assertTrue(genDir.mkdir());
      File warDir = new File(targetSubdir, GwtArgs.WAR);
      assertTrue(warDir.mkdir());
      File workDir = new File(targetSubdir, GwtArgs.WORK_DIR);
      assertTrue(workDir.mkdir());

      gwtArgs.put(GwtArgs.GEN, genDir.toString());
      gwtArgs.put(GwtArgs.WAR, warDir.toString());
      gwtArgs.put(GwtArgs.WORK_DIR, workDir.toString());

//      gwtArgs.putIfValueNotStartsWith(GwtArgs.GEN, userDir, ServerIOUtils.joinPath(userDir, targetSubdir, GwtArgs.GEN));
//      gwtArgs.putIfValueNotStartsWith(GwtArgs.WAR, userDir, ServerIOUtils.joinPath(userDir, targetSubdir, GwtArgs.WAR));
//      gwtArgs.putIfValueNotStartsWith(GwtArgs.WORK_DIR, userDir, ServerIOUtils.joinPath(userDir, targetSubdir, "temp"));

      LinkedHashMap<String, String> newSysProps = MapUtils.linkedHashMap(
          GwtArgs.SYS_PROP_GWT_ARGS, gwtArgs.toString()/*,
          "gwt.persistentunitcachedir", "R:\\gwt-unitCache"*/
      );
      for (Map.Entry<String, String> prop : newSysProps.entrySet()) {
        String name = prop.getKey();
        String val = prop.getValue();
        System.out.printf("%s is setting system property %s=%s%n", ourClassName, name, val);
        System.setProperty(name, val);
      }
    }
  }


  /**
   * Parses the value of the "gwt.args" system property into a map, and allows modifying it dynamically prior to running
   * the unit tests.
   */
  static class GwtArgs extends LinkedHashMap<String, String> {

    public static final String SYS_PROP_GWT_ARGS = "gwt.args";

    public static final String PROD = "prod";
    public static final String LOCAL_WORKERS = "localWorkers";
    public static final String DRAFT_COMPILE = "draftCompile";
    public static final String USER_AGENTS = "userAgents";
    public static final String RUN_STYLE = "runStyle";
    public static final String WEB = "web";
    public static final String GEN = "gen";
    public static final String WAR = "war";
    public static final String WORK_DIR = "workDir";

    private static GwtArgs instance;

    static GwtArgs get() {
      if (instance == null)
        instance = new GwtArgs(System.getProperty(SYS_PROP_GWT_ARGS));
      return instance;
    }

    GwtArgs(String argsString) {
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
     * @return true iff the args contain "-prod" or its deprecated alias "-web".
     */
    public boolean isWebMode() {
      return containsKey(PROD) || containsKey(WEB);
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
  }
}
