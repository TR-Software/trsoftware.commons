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

package solutions.trsoftware.commons.server.testutil;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import solutions.trsoftware.commons.client.util.callables.Function1_t;
import solutions.trsoftware.commons.server.util.ThreadUtils;

import java.io.File;
import java.io.IOException;

/**
 * This base test case creates one or more temporary directories in the system's base temp directory (specified by
 * the system property {@code java.io.tmpdir}) during {@link #setUp()} and deletes them during {@link #tearDown()}.
 * These directories directories will be named <code>${{@link #getName()}}_TEMPDIR_${{@link System#nanoTime()}}</code>
 * @since Nov 4, 2009
 *
 * @author Alex
 */
public abstract class TempDirTestCase extends TestCase {
  protected File[] tempDirs;

  /** Subclasses must implement this method to specify the number of temp dirs to be created */
  protected abstract int getNumDirs();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tempDirs = createTempDirs(getNumDirs(), getName());
  }

  @Override
  protected void tearDown() throws Exception {
    rmdir(tempDirs);
    super.tearDown();
  }

  /**
   * Creates a number of directories inside the system-default temporary directory (which is specified by the JVM property
   * {@code java.io.tmpdir})
   * @param n the number of directories to create
   * @param namePrefix the directories will be named <code>${namePrefix}_TEMPDIR_${{@link System#nanoTime()}}</code>
   * @return the created directories
   */
  public static File[] createTempDirs(int n, String namePrefix) throws IOException {
    File[] tempDirs = new File[n];
    for (int i = 0; i < tempDirs.length; i++) {
      File d = createTempDir(namePrefix);
      tempDirs[i] = d;
    }
    return tempDirs;
  }

  public static File createTempDir(String namePrefix) throws IOException {
    File d = new File(System.getProperty("java.io.tmpdir"), namePrefix + "_TEMPDIR_" + System.nanoTime());
    assertFalse(d.exists());
    mkdir(d);
    d.deleteOnExit();
    return d;
  }

  public static void rmdir(File[] dirs) throws IOException {
    for (File d : dirs)
      rmdir(d);
  }

  public static void rmdir(File dir) throws IOException {
    if (dir.exists())
      FileUtils.deleteDirectory(dir);
    assertFalse(dir.exists());
  }

  /**
   * Creates a new directory specified by the given path name.
   * @return {@code true} if a new directory was created or {@code false} if it couldn't be created
   * (e.g. already existed or user lacks permissions)
   */
  public static boolean mkdir(File dir) throws IOException {
    if (dir.exists())
      return false;
    else if (!dir.mkdir())
      return false;
    else {
      // make sure the directory actually exists before returning (there have been issues with this in the past)
      if (!dir.exists()) {
        // wait up to 50ms for the filesystem to catch up before reporting an error
        for (int i = 0; !dir.exists() && i < 50; i++)
          ThreadUtils.sleepUnchecked(1);
      }
      assertTrue(dir.exists());
      return true;
    }
  }

  /**
   * Runs the given closure after creating the specified number of temp dirs, and cleans up the temp dirs after
   * the closure has been run.  This is an "Automatic Resource Management Block" implementation.
   *
   * @param numDirs the number of temp directories to create before invoking the closure
   * @param namePrefix the directories will be named [namePrefix]_TEMPDIR_0 .. [namePrefix]_TEMPDIR_n
   * @param closure The code under test.  It will be passed an array of the created temp dirs.
   * @see <a href="http://mail.openjdk.java.net/pipermail/coin-dev/2009-February/000011.html">Josh Bloch's ARM block proposal</a>
   */
  public static void withTempDirs(final int numDirs, final String namePrefix, Function1_t<File[], Exception> closure) throws Exception {
    File[] tempDirs = createTempDirs(numDirs, namePrefix);
    try {
      closure.call(tempDirs);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      rmdir(tempDirs);
    }
  }

  /**
   * Runs the given closure after creating a temp dir and cleans up after the closure has been run.
   * This is an "Automatic Resource Management Block" implementation.
   *
   * @param namePrefix the directory will be named [namePrefix]_TEMPDIR_0
   * @param closure The code under test.  It will be passed an array of the created temp dirs.
   * @see <a href="http://mail.openjdk.java.net/pipermail/coin-dev/2009-February/000011.html">Josh Bloch's ARM block proposal</a>
   */
  public static void withTempDir(final String namePrefix, final Function1_t<File, Exception> closure) throws Exception {
    withTempDirs(1, namePrefix, arg -> closure.call(arg[0]));
  }

}
