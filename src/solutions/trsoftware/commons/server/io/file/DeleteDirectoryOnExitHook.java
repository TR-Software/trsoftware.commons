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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * This is a modified version of {@link java.io.DeleteOnExitHook}, adapted to support non-empty directories.
 *
 * This class holds a set of directories to be deleted on VM exit through a shutdown hook.
 * A set is used both to prevent double-insertion of the same file as well as offer
 * quick removal.
 *
 * @deprecated This class uses a potentially unsafe operation: placing its hook in {@link Shutdown#hooks slot 3} of the
 * {@link Shutdown} sequence (using {@link sun.misc.SharedSecrets}, which (in conjunction with
 * {@link sun.misc.JavaLangAccess#registerShutdownHook(int, boolean, Runnable)}) allows circumventing the internal
 * Java access controls for {@link Shutdown#add(int, boolean, Runnable)}.
 *
 * <p>
 * The original intention was to ensure that our {@link DeleteDirectoryOnExitHook} runs after all the <i>application hooks</i>
 * (registered with {@link Runtime#addShutdownHook(Thread)}) have finished executing, and clean up any non-empty
 * directories left behind by {@link java.io.DeleteOnExitHook}.
 *
 * However, since the JVM expects only 3 shutdown hook slots to be used ({@link Shutdown#hooks hooks[0..2]}),
 * this approach may not be compatible with future JDK releases, which may want to use {@link Shutdown#hooks hooks[3]}
 * for some other purpose, or even reduce the number of available {@link Shutdown#MAX_SYSTEM_HOOKS hook slots}
 * </p>
 * <p>
 *   <strong>Use {@link TempFileRegistry} instead of this class</strong>
 * </p>
 *
 * @see TempFileRegistry
 * @see <a href="http://mail.openjdk.java.net/pipermail/core-libs-dev/2013-June/018460.html">
 *        OpenJDK mailing list archives thread about JVM shutdown hooks</a>
 * @see <a href="http://www.docjar.com/docs/api/sun/misc/SharedSecrets.html">sun.misc.SharedSecrets</a>
 * @see <a href="http://www.docjar.com/docs/api/sun/misc/JavaLangAccess.html">sun.misc.JavaLangAccess</a>
 */
class DeleteDirectoryOnExitHook {
  private static LinkedHashSet<Path> directories = new LinkedHashSet<>();

  static {
    /*
    TODO: probably should stick with using an "application hook" (Runtime.getRuntime().addShutdownHook)
    because trying to stick something in a different Shutdown slot could be incompatible with future JDK versions.

    However, that behavior would have to be documented.  Perhaps better to use a different approach altogether:
      - have all calls to File.deleteOnExit() go through another intermediate class that uses a regular (application)
        shutdown hook to re-order the calls to File.deleteOnExit() such that deeper files will be deleted before
        their parent directory (ensuring that the parent directory will be empty after its children have been deleted)
      - in other words, this application hook, when executed, would examine all files registered with it,
        and for any directories, would use a recursive traversal (walkFileTree) to issue the correct sequence of
        calls to File.deleteOnExit()
    */

    // Runtime.getRuntime().addShutdownHook(new Thread(DeleteDirectoryOnExitHook::runHooks));
    sun.misc.SharedSecrets.getJavaLangAccess()
        .registerShutdownHook(3 /* Shutdown hook invocation order */,
            true /* register even if shutdown in progress */,
            new Runnable() {
                public void run() {
                   runHooks();
                }
            }
    );
  }

  private DeleteDirectoryOnExitHook() {
  }

  static synchronized void add(Path dir) {
    if (directories == null) {
      // the hook is running. Too late to add another item
      throw new IllegalStateException("Shutdown in progress");
    }

    directories.add(dir);
  }

  private static void runHooks() {
    LinkedHashSet<Path> theDirectories;

    synchronized (DeleteDirectoryOnExitHook.class) {
      theDirectories = directories;
      directories = null; // setting to null to indicate that the hook is running
    }

    ArrayList<Path> toBeDeleted = new ArrayList<>(theDirectories);

    // reverse the list to maintain previous jdk deletion order.
    // Last in first deleted.
    Collections.reverse(toBeDeleted);
    for (Path dir : toBeDeleted) {
      try {
        FileUtils.deleteFileTree(dir);
      }
      catch (IOException e) {
        // trap the exception so that the process may continue for subsequent items
        e.printStackTrace();
      }
    }
  }
}
