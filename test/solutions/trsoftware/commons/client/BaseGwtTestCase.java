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

package solutions.trsoftware.commons.client;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.google.gwt.junit.client.GWTTestCase;
import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.gwt.GwtArgs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * @see GwtArgs
 * @author Alex
 */
public abstract class BaseGwtTestCase extends GWTTestCase {

  private static boolean gwtArgsProcessed;

  public static boolean modifyRunStyle = false;

  @Override
  protected void runTest() throws Throwable {
    maybeModifyGwtArgs();
    super.runTest();
  }

  /**
   * Modifies the gwt.args property to allow web mode and hosted mode unit tests to run simultaneously
   * (ensures that their -war and -gen directories are different; see 2015_03_31_GWT_unit_test_useragent_property_bug.txt
   * and https://github.com/gwtproject/gwt/issues/9102).  This only needs to be done once
   * per lifetime of the JVM, hence the "maybe".  Doing it automatically like this lets us not worry with setting up
   * separate run configurations in our IDE for web mode and hosted mode GWT tests.
   *
   * We also modify the gwt.persistentunitcachedir setting to a globally-shared dir, because different runs are able
   * to reuse most of each-other's compiler unit cache output (see doc/gwt/experiments/PersistentUnitCacheUsageExperiment.txt)
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
        if (modifyRunStyle && !gwtArgs.containsKey(GwtArgs.RUN_STYLE) && !gwtArgs.containsKey(GwtArgs.USER_AGENTS)) {
          // if no -runStyle specified, we'll use our own setting, so that we can restrict the -userAgent property to
          // only include permutations for the browsers that will be testing on (otherwise,
          // JUnitShell will build permutations for all user.agent values, which takes longer to compile)
          // see http://www.gwtproject.org/doc/latest/DevGuideTestingHtmlUnit.html
          String runStyle;
          String userAgents;
          if (gwtArgs.containsKey(GwtArgs.DRAFT_COMPILE)) {
            // we'll take this to mean that the user wants a very fast test run, so we restrict the perms to only 1 browser
            // (FF38 is the default in GWT 2.8.x with HtmlUnit 2.19)
            runStyle = "HtmlUnit:FF38";
            userAgents = "gecko1_8"; // we only need the permutation for the browser specified in -runStyle
          }
          else{
            // since the user is not passing -draftCompile, we're assuming that he's willing to wait longer to get a more thorough test run
            // htmlunit-2.15 supports FF17,FF24,IE8,IE9,IE11,Chrome, of which we choose only FF24,IE8,IE11,Chrome,
            // because FF17 seems redundant, and IE9 didn't seem to work when we tried it
            runStyle = "HtmlUnit:Chrome,FF38,IE8,IE11";
            userAgents = "safari,gecko1_8,ie8"; // we only need the permutation for the browsers specified in -runStyle
            // NOTE: IE11 uses the "gecko1_8" permutation in RunStyleHtmlUnit
          }
          gwtArgs.put(GwtArgs.RUN_STYLE, runStyle);
          gwtArgs.put(GwtArgs.USER_AGENTS, userAgents);
        }
      }

      // warn if attempting to use com.gargoylesoftware.htmlunit.BrowserVersion.EDGE (tends to cause lots of test failures)
      if (gwtArgs.getRunStyleHtmlUnitArgs().contains("Edge")) {
        System.err.printf("WARNING: using BrowserVersion.EDGE could cause inexplicable test failures (remove %s from -%s %s)%n",
            BrowserVersion.EDGE.getNickname(), GwtArgs.RUN_STYLE, gwtArgs.get(GwtArgs.RUN_STYLE));
      }

      // we need to use separate dirs depending on the configuration of the running tests to avoid a race condition
      // arising from tests using different builds using the same -war dir (see the doc for this method for more details)
      String userDir = System.getProperty("user.dir");
      String targetSubdirName = gwtArgs.isWebMode() ? "gwtWebModeTests" : "gwtHostedModeTests";

      if (gwtArgs.containsKey(GwtArgs.USER_AGENTS))
        targetSubdirName += "_" + gwtArgs.get(GwtArgs.USER_AGENTS);
      Path stagingDir = getStagingDir();
      Path targetSubdir = FileUtils.maybeCreateDirectory(stagingDir.resolve(targetSubdirName));
      Path genDir = FileUtils.maybeCreateDirectory(targetSubdir.resolve(GwtArgs.GEN));
      Path warDir = FileUtils.maybeCreateDirectory(targetSubdir.resolve(GwtArgs.WAR));
      Path workDir = FileUtils.maybeCreateDirectory(targetSubdir.resolve(GwtArgs.WORK_DIR));

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

  private static Path getStagingDir() throws IOException {
    // check if we have a custom property that specifies where the GWT compiler output for these tests should go
    Path stagingDirPath;
    String stagingDirValue = System.getProperty("BaseGwtTestCase.stagingDir");
    if (stagingDirValue != null) {
      stagingDirPath = Paths.get(stagingDirValue);
    }
    else  {
      // default to a subdirectory under the program's "working directory"
      stagingDirPath = Paths.get(System.getProperty("user.dir")).resolve("gwtTestsStaging");
    }
    return FileUtils.maybeCreateDirectory(stagingDirPath);
  }


}
