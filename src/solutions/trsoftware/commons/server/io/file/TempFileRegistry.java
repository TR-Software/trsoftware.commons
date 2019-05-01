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

package solutions.trsoftware.commons.server.io.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import static solutions.trsoftware.commons.server.io.file.FileUtils.TEMP_DIR_PATH;

/**
 * This class attempts to overcome a shortcoming of {@link File#deleteOnExit()} in regard to its inability to
 * delete non-empty directories.
 * <p>
 * To remedy that situation, this class creates a custom registry for temporary files (to be used <em>instead of</em>
 * {@link File#deleteOnExit()}), and uses a normal {@link Runtime#addShutdownHook(Thread) <i>shutdown hook</i>}
 * to arrange calls to {@link File#deleteOnExit()} into a sequence that would ensure that children of a directory
 * are deleted <em>before</em> the directory itself, leaving the directory empty and thereby allowing the
 * {@link File#delete()} call issued by {@link java.io.DeleteOnExitHook} to succeed.
 *
 * <h3><i>Motivation</i></h3>
 *
 * The normal way to clean up temporary files created by an application is to call {@link File#deleteOnExit()}.
 * That mechanism relies upon {@link java.io.DeleteOnExitHook}, which is a special step in the
 * {@link Shutdown JVM shutdown sequence} that is guaranteed to run <em>after</em> all the normal
 * {@link Runtime#addShutdownHook(Thread) <i>application hooks</i>} have finished running.
 * It calls {@link File#delete()} for each entry added by {@link File#deleteOnExit()} (in reverse order).
 * <p>
 * The problem is that {@link File#delete()} requires that a directory be empty, so to guarantee that all temp files
 * are cleaned up before the program terminates, one must be sure to call {@link File#deleteOnExit()} for every single
 * temp file individually.  This may not be convenient (or even possible) in certain situations, like for example, when
 * unit testing a class that creates persistent data in some directory (without intending for its files to be deleted).
 * Often it would be more convenient to be able to call {@link File#deleteOnExit()} only for a directory.
 *
 * Furthermore, the order in which calls to {@link File#deleteOnExit()} are made carries significant implications:
 * unless special care is used to ensure that it's called on all children of a directory <em>after</em> the directory
 * itself, some of those temporary files may persist after the JVM has shut down, because {@link File#delete()}
 * cannot delete non-empty directories.
 *
 * <h3><i>Background Reading</i></h3>
 *
 * The JVM shutdown sequence is described in the source code of the special class {@link Shutdown}:
 * <blockquote>
 *   The system shutdown hooks are registered with a predefined slot.
     The list of shutdown hooks is as follows:
 *   <ol start=0>
 *     <li><i>"Console restore hook"</i> (no explanation given in the code)</li>
 *     <li>
 *       <i>"Application hooks"</i> (which are registered with {@link Runtime#addShutdownHook(Thread)}
 *       and executed by {@link ApplicationShutdownHooks}).  These run concurrently as separate threads,
 *       and therefore, the application has no control of their execution order (unless their execution is somehow
 *       managed separately by the application itself, as suggested by
 *       <a href="http://mail.openjdk.java.net/pipermail/core-libs-dev/2013-June/018460.html">
 *         this message in the OpenJDK mailing list archives</a>)
 *   </li>
 *   <li>
 *     <i>"DeleteOnExit hook"</i> ({@link java.io.DeleteOnExitHook}).
 *     This hook is executed <em>after</em> all the <i>application hook</i> threads have finished running.
 *   </li>
 * </ol>
 * </blockquote>
 * The above information comes from <cite>the source code of {@link Shutdown} in JDK 1.8</cite>.
 *
 * <h3><i>Justification</i></h3>
 *
 * Other approaches that we considered (but rejected) included:
 * <ol type=a>
 *   <li>
 *     Using a normal <i>application hook</i> to delete the directories recursively with {@link FileUtils#deleteFileTree(Path)}.
 *     A reasonable approach, but the problem is that since we can't control the execution order of application hooks
 *     (see explanation above), it's possible that certain files (used by the other application hooks) will not be
 *     eligible for deletion until after those other hooks have finished.
 *   </li>
 *    <li>
 *      Tampering with the {@link Shutdown} sequence by forcing the directory hook (as described above) into
 *      {@link Shutdown#hooks slot 3}, thereby making it run only <em>after</em> all the application hooks
 *      (and the {@link java.io.DeleteOnExitHook}).
 *      The downside to this hack is that it may not be compatible with future versions of the JDK.
 *   </li>
 * </ol>
 *
 * Ultimately, it was decided that {@link java.io.DeleteOnExitHook DeleteOnExitHook} is still the best place to delete
 * temporary files, since it's guaranteed to run <em>after</em> all the <i>application hooks</i>, which allows
 * <i>application hooks</i> to finish working on their files (and even call {@link File#deleteOnExit()} during shutdown).
 * To overcome its deficiency regarding non-empty directories, it should be sufficient to simply re-arrange the calls
 * to {@link File#deleteOnExit()} so that directories will be empty when it's their turn to be deleted.
 *
 * @author Alex
 * @since 5/14/2018
 */
public final class TempFileRegistry {

  // TODO: perhaps make this a static class instead of singleton?
  private static final TempFileRegistry instance = new TempFileRegistry();

  /**
   * The {@link Path#isAbsolute() <em>absolute</em>} path of the system temp directory
   * (derived from the value of the {@value FileUtils#TEMP_DIR_PROP} system property)
   */
  public static final Path SYSTEM_TEMP_DIR = Paths.get(FileUtils.TEMP_DIR_PATH).toAbsolutePath();

  /**
   * Caches the return value of {@link #getRealSystemTempDir()}
   */
  private static Path realSystemTempDir;

  /**
   * The {@link Path#isAbsolute() <em>absolute</em>} paths of the registered files and directories
   */
  private NavigableSet<Path> paths = new TreeSet<>();

  private boolean shutdownHookAdded;

  private final Object lock = this;

  private static final Object staticLock = TempFileRegistry.class;

  /**
   * @return the {@link Path#toRealPath <em>real</em>} and {@link Path#isAbsolute() <em>absolute</em>} path of the
   * system temp directory (derived from the value of the {@value FileUtils#TEMP_DIR_PROP} system property)
   * @throws RuntimeException if an I/O exception occurs from calling {@link Path#toRealPath} on the system temp dir
   */
  public static Path getRealSystemTempDir() {
    if (realSystemTempDir == null) {
      synchronized (staticLock) {
        // double-checked locking
        if (realSystemTempDir == null) {
          try {
            realSystemTempDir = Paths.get(TEMP_DIR_PATH).toRealPath();
          }
          catch (IOException e) {
            throw new RuntimeException(String.format("Unable to resolve the system temp directory: %s", TEMP_DIR_PATH), e);
          }
        }
      }
    }
    return realSystemTempDir;
  }

  /**
   * Constructor exposed for unit testing only.  Normal application code should use {@link #getInstance()}.
   */
  TempFileRegistry() {

  }

  /**
   * Lazily adds the shutdown hook (if it hasn't already been added).  Assumes that it's called within a
   * {@code synchronized} block.
   * <p>
   * NOTE: if called from inside a webapp, could cause an exception while the app server (e.g. Tomcat) is shutting down,
   * because the webapp that added this hook would have already been unloaded at that point:
   * <pre>
   *   java.lang.IllegalStateException: Illegal access: this web application instance has been stopped already. Could not load [solutions.trsoftware.commons.server.io.file.FileDeletionScheduler]
   * </pre>
   */
  private void maybeAddShutdownHook() {
    if (!shutdownHookAdded) {
      Runtime.getRuntime().addShutdownHook(new Thread(this::runShutdownHook, getClass().getName() + " shutdown hook"));
      shutdownHookAdded = true;
    }
  }

  public static TempFileRegistry getInstance() {
    return instance;
  }

  /**
   * Adds the given path to this registry, so that correct calls to {@link File#deleteOnExit()} will be made during
   * the JVM {@link Runtime#addShutdownHook(Thread) <i>shutdown sequence</i>}.
   *
   * @param path a file or directory; does not have to actually exist yet
   * @return {@code true} if this path was not already registered
   * @throws IllegalArgumentException if the given path is not under the system temp directory (this is a safety
   * precaution to avoid inadvertently deleting important files)
   * @throws IllegalStateException if the shutdown hook is already running
   */
  public boolean add(Path path) {
    // safety precaution: make sure this path is contained within the system temp directory
    path = requireChildOfSystemTempDir(path);
    // we only need locking after the path has been checked
    synchronized (lock) {
      maybeAddShutdownHook();
      return getPaths().add(path);
    }
  }

  /**
   * Converts the given path to an {@link Path#isAbsolute() <em>absolute</em>}, path and asserts that it's
   * a descendant of {@link #SYSTEM_TEMP_DIR}.
   * <p>
   * NOTE: This method is a <em>weaker</em> version of {@link #requireChildOfSystemTempDirForReal(Path)},
   * and checks only whether the given path {@link Path#startsWith(Path) <em>starts with</em>} {@link #SYSTEM_TEMP_DIR}.
   * The given path does not have to actually exist.
   *
   * @param path a file or directory; does not have to actually exist
   * @return the result of {@link Path#toAbsolutePath()}
   * @throws IllegalArgumentException if the given path is not under the system temp directory (this is a safety
   * precaution to avoid inadvertently deleting important files)
   */
  public static Path requireChildOfSystemTempDir(Path path) throws IllegalArgumentException {
    return requireChildOfSystemTempDir(path.toAbsolutePath(), SYSTEM_TEMP_DIR);
  }

  /**
   * Helper for {@link #requireChildOfSystemTempDir(Path)} and {@link #requireChildOfSystemTempDirForReal(Path)}
   */
  private static Path requireChildOfSystemTempDir(Path path, Path systemTempDir) {
    if (path.equals(systemTempDir) || !path.startsWith(systemTempDir))
      throw new IllegalArgumentException(
          String.format("Path not under system temp file directory (given: <%s>; temp dir: <%s>)", path, systemTempDir));
    return path;
  }

  /**
   * Resolves the given path to a {@link Path#toRealPath <em>real</em>} and {@link Path#isAbsolute() <em>absolute</em>}
   * path, and asserts that it's a descendant of {@link #getRealSystemTempDir()}.
   * <p>
   * NOTE: this method is a <em>stronger</em> version of {@link #requireChildOfSystemTempDir(Path)},
   * and requires that the given path actually exists.
   *
   * @param path a file or directory; must exist.
   * @return the result of {@link Path#toAbsolutePath()}
   *
   * @throws IOException propagated from the call to {@link Path#toRealPath} on the given path or the system temp dir
   * @throws IllegalArgumentException if the given path is not under the system temp directory (this is a safety
   * precaution to avoid inadvertently deleting important files)
   * @see #getRealSystemTempDir()
   * @see Path#toRealPath(LinkOption...)
   */
  public static Path requireChildOfSystemTempDirForReal(Path path) throws IllegalArgumentException, IOException {
    return requireChildOfSystemTempDir(path.toRealPath(), getRealSystemTempDir());
  }

  /**
   * Calls {@link #add(Path)} with the given file.
   */
  public boolean add(File file) {
    return add(file.toPath());
  }

  /**
   * @return {@link #paths} if it's not {@code null} (otherwise throws {@link IllegalStateException})
   * @throws IllegalStateException if the shutdown hook is already running
   */
  private NavigableSet<Path> getPaths() {
    checkState();
    return paths;
  }

  /**
   * @throws IllegalStateException if the shutdown hook is already running
   */
  private void checkState() {
    if (paths == null)
      throw new IllegalStateException("Shutdown hook already running");
  }

  /**
   * @return the number of registered files
   */
  public int size() {
    synchronized (lock) {
      return getPaths().size();
    }
  }

  /**
   * @param path a file or directory
   * @return {@code true} if this path was already registered
   * @throws IllegalStateException if the shutdown hook is already running
   */
  public boolean contains(Path path) {
    path = path.toAbsolutePath();
    synchronized (lock) {
      return getPaths().contains(path);
    }
  }

  /**
   * Unregisters the given path. <em>WARNING</em>: this means that it will not be deleted during the
   * JVM {@link Runtime#addShutdownHook(Thread) <i>shutdown sequence</i>}.
   * @param path a file or directory
   * @return {@code true} if this path was removed (i.e. has been previously registered)
   * @throws IllegalStateException if the shutdown hook is already running
   */
  public boolean remove(Path path) {
    path = path.toAbsolutePath();
    synchronized (lock) {
      return getPaths().remove(path);
    }
  }

  /**
   * Method provided for unit testing
   * @return a copy of the paths added with the {@link #add(Path)} method (operations on the returned set have
   * no effect on the internal state of this {@link TempFileRegistry})
   */
  Set<Path> getRegisteredPaths() {
    synchronized (lock) {
      return new LinkedHashSet<>(getPaths());  // defensive copy
    }
  }

  private void runShutdownHook() {
    NavigableSet<Path> files;
    synchronized (lock) {
      checkState();
      files = this.paths;
      // clear the field to avoid further synchronization while the hook is running (and also prevent new files from being added)
      this.paths = null;
    }
    new FileDeletionScheduler(files).run();
  }

}
