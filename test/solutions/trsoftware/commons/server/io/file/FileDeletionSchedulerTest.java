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

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.testutil.TestUtils;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.util.SetUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static solutions.trsoftware.commons.server.testutil.TestUtils.printCollection;

/**
 * @author Alex
 * @since 5/16/2018
 */
public class FileDeletionSchedulerTest extends TestCase {

  public void testRun() throws Exception {
    TreeSet<Path> tempFiles = new TreeSet<>();
    // create some test data
    // a) some file trees in the temp dir
    for (int i = 0; i < 3; i++) {
      Path tempDir = Files.createTempDirectory(
          String.format("%s_dir%d_", TestUtils.qualifiedTestName(this, true), i));
      FileUtilsTest.createDummyFileTree(tempDir, i, i+2);
      // add all the files in this tree
      try (Stream<Path> recursiveListing = Files.walk(tempDir)) {
        recursiveListing.forEach(tempFiles::add);
      }
    }
    // b) some files directly in the temp dir (not under a subdir)
    for (int i = 0; i < 3; i++) {
      Path tempFile = Files.createTempFile(
          String.format("%s_file%d_", TestUtils.qualifiedTestName(this, true), i),
          null
      );
      tempFiles.add(tempFile);
    }
    // add some files that are not under the temp dir (these should not be scheduled for deletion)
    TreeSet<Path> notToBeDeleted = new TreeSet<>(Arrays.asList(
        ReflectionUtils.getClassFile(getClass()).toFile().toPath()
    ));
    TreeSet<Path> allFiles = new TreeSet<>(tempFiles);
    allFiles.addAll(notToBeDeleted);
    printCollection("tempFiles", tempFiles);
    System.out.println("notToBeDeleted = " + notToBeDeleted);

    MockFileDeletionScheduler scheduler = new MockFileDeletionScheduler(allFiles);
    scheduler.run();
    List<Path> scheduled = scheduler.scheduled;
    printCollection("scheduled", scheduled);
    // make sure all files visited
    assertTrue(scheduler.getVisited().containsAll(allFiles));
    // make sure that only temp files are actually scheduled
    LinkedHashSet<Path> scheduledSet = new LinkedHashSet<>(scheduled);
    assertTrue(scheduledSet.containsAll(tempFiles));
    assertTrue(SetUtils.intersection(scheduledSet, notToBeDeleted).isEmpty());
    // make sure that the files are scheduled in the correct order (parents before children)
    for (int i = 0; i < scheduled.size(); i++) {
      Path path = scheduled.get(i);
      Path parent = path.getParent();
      if (scheduledSet.contains(parent)) {
        int iChild = scheduled.indexOf(path);
        int iParent = scheduled.indexOf(parent);
        assertTrue(
            String.format("Parent not scheduled *before* child (child <%s> at pos %d; parent <%s> at pos %d)",
                path, iChild, parent, iParent),
            iParent < iChild);
      }
    }
  }


  private class MockFileDeletionScheduler extends FileDeletionScheduler {
    private List<Path> scheduled = new ArrayList<>();

    MockFileDeletionScheduler(NavigableSet<Path> paths) {
      super(paths);
    }

    @Override
    protected void scheduleDeletion(Path path) {
      super.scheduleDeletion(path);
      scheduled.add(path);
    }

    Set<Path> getVisited() {
      return visited;
    }
  }
}