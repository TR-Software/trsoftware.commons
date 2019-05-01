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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.NavigableSet;
import java.util.Objects;

/**
 * Helper for {@link TempFileRegistry}.
 * <p>
 * Schedules a set of files for deletion by calling {@link File#deleteOnExit()} in a sequence that would ensure that
 * children of a directory are deleted <em>before</em> the directory itself, leaving the directory empty and thereby
 * allowing the {@link File#delete()} call issued by {@link java.io.DeleteOnExitHook} to succeed.
 * <p>
 * <i>Exposed with package-private visibility for unit testing.</i>
 */
class FileDeletionScheduler implements Runnable {
  private final NavigableSet<Path> paths;
  protected final LinkedHashSet<Path> visited = new LinkedHashSet<>();  // exposed for unit testing

  FileDeletionScheduler(NavigableSet<Path> paths) {
    this.paths = Objects.requireNonNull(paths);
  }

  @Override
  public void run() {
    for (Path path : paths) {
      if (visited.contains(path))
        continue;
      if (Files.exists(path)) {
        if (Files.isRegularFile(path)) {
          maybeScheduleDeletion(path);
        }
        else {
          assert Files.isDirectory(path);
          visitDirectory(path);
        }
      }
      visited.add(path);
    }
  }

  /**
   * Calls {@link File#deleteOnExit()} on the argument, but only if it exists and its
   * {@link Path#toRealPath <em>real path</em>} is a descendant of {@link TempFileRegistry#getRealSystemTempDir()}.
   * @return {@code true} iff {@link File#deleteOnExit()} was actually called on the argument
   */
  private boolean maybeScheduleDeletion(Path path) {
    boolean wasScheduled = false;
    try {
      path = TempFileRegistry.requireChildOfSystemTempDirForReal(path);
      scheduleDeletion(path);
      wasScheduled = true;
    }
    catch (IOException | IllegalArgumentException e) {
      // suppress the exception, to allow the hook to keep running
    }
    visited.add(path);
    return wasScheduled;
  }

  /**
   * Calls {@link File#deleteOnExit()} on the argument.
   * Exposed with {@code protected} visibility for unit testing.
   */
  protected void scheduleDeletion(Path path) {
    path.toFile().deleteOnExit();
  }

  private void visitDirectory(Path path) {
    try {
      Files.walkFileTree(path, new DeleteOnExitVisitor());  // depth-first traversal
    }
    catch (Throwable e) {
      // trap all exceptions (to allow the shutdown hook to continue)
      e.printStackTrace();
    }
  }

  /**
   * Calls {@link #maybeScheduleDeletion(Path)} such that children of a directory are scheduled <em>after</em>
   * the directory itself (because {@link java.io.DeleteOnExitHook} will delete the files in the reverse order of
   * {@link File#deleteOnExit()})
   * <p>
   * <strong>NOTE</strong>: intended for a <em>depth-first traversal only</em>
   * @see Files#walkFileTree(Path, FileVisitor)
   */
  private class DeleteOnExitVisitor extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      try {
        TempFileRegistry.requireChildOfSystemTempDirForReal(dir);
      }
      catch (IOException | IllegalArgumentException e) {
        return FileVisitResult.SKIP_SUBTREE;  // this directory is not eligible for deletion
      }
      maybeScheduleDeletion(dir);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      maybeScheduleDeletion(file);
      return FileVisitResult.CONTINUE;
    }
  }

}
