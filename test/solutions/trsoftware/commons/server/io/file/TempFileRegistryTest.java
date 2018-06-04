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
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.testutil.TestUtils;
import solutions.trsoftware.commons.server.util.RuntimeUtils;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import static solutions.trsoftware.commons.server.io.file.FileUtils.isEmpty;
import static solutions.trsoftware.commons.server.io.file.TempFileRegistry.requireChildOfSystemTempDir;
import static solutions.trsoftware.commons.server.io.file.TempFileRegistry.requireChildOfSystemTempDirForReal;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 5/16/2018
 */
public class TempFileRegistryTest extends TestCase {

  private static final Path systemTempDir = Paths.get(FileUtils.TEMP_DIR_PATH);

  private TempFileRegistry registry;

  public void setUp() throws Exception {
    super.setUp();
    registry = new TempFileRegistry();
  }

  public void tearDown() throws Exception {
    registry = null;
    super.tearDown();
  }

  public void testRequireChildOfSystemTempDir() throws Exception {
    requireChildOfSystemTempDir(systemTempDir.resolve("foo"));
    requireChildOfSystemTempDir(systemTempDir.resolve("foo").resolve("bar"));
    assertThrows(IllegalArgumentException.class, new Runnable() {
      @Override
      public void run() {
        requireChildOfSystemTempDir(ReflectionUtils.getCompilerOutputPath(getClass()));
      }
    });
    assertThrows(IllegalArgumentException.class, new Runnable() {
      @Override
      public void run() {
        requireChildOfSystemTempDir(ReflectionUtils.getClassFile(getClass()).toPath());
      }
    });
    // the sys temp dir itself shouldn't be allowed
    assertThrows(IllegalArgumentException.class, new Runnable() {
      @Override
      public void run() {
        requireChildOfSystemTempDir(systemTempDir);
      }
    });
  }

  public void testRequireChildOfSystemTempDirForReal() throws Exception {
    Path foo = systemTempDir.resolve("foo");
    // this file doesn't exist yet, so will throw IOException
    assertThrows(IOException.class, new Function0_t<Throwable>() {
      @Override
      public void call() throws Throwable {
        requireChildOfSystemTempDirForReal(foo);
      }
    });
    Path tempFile = Files.createTempFile(TestUtils.qualifiedTestName(this), null);
    // this file exists, so should succeed
    requireChildOfSystemTempDirForReal(tempFile);
    Files.delete(tempFile);
    Path tempDirectory = Files.createTempDirectory(TestUtils.qualifiedTestName(this));
    // this directory exists, so should succeed
    requireChildOfSystemTempDirForReal(tempDirectory);
    Files.delete(tempDirectory);
    // the following should throw IllegalArgumentException because they're not under the sys temp dir
    assertThrows(IllegalArgumentException.class, new Function0_t<Throwable>() {
      @Override
      public void call() throws Throwable {
        requireChildOfSystemTempDirForReal(ReflectionUtils.getCompilerOutputPath(getClass()));
      }
    });
    assertThrows(IllegalArgumentException.class, new Function0_t<Throwable>() {
      @Override
      public void call() throws Throwable {
        requireChildOfSystemTempDirForReal(ReflectionUtils.getClassFile(getClass()).toPath());
      }
    });
    // the sys temp dir itself shouldn't be allowed
    assertThrows(IllegalArgumentException.class, new Function0_t<Throwable>() {
      @Override
      public void call() throws Throwable {
        requireChildOfSystemTempDirForReal(systemTempDir);
      }
    });
  }

  /**
   * Tests {@link TempFileRegistry#add(Path)}, {@link TempFileRegistry#contains(Path)},
   * and {@link TempFileRegistry#remove(Path)}
   */
  public void testRegistration() throws Exception {
    LinkedHashSet<Path> validPaths = SetUtils.newSet(
        systemTempDir.resolve("foo"),
        systemTempDir.resolve("foo").resolve("bar")
    );
    for (Path path : validPaths) {
      assertTrue(registry.add(path));
      assertTrue(registry.contains(path));
    }
    assertEquals(validPaths, registry.getRegisteredPaths());
    LinkedHashSet<Path> invalidPaths = SetUtils.newSet(
        systemTempDir,
        ReflectionUtils.getCompilerOutputPath(getClass()),
        ReflectionUtils.getClassFile(getClass()).toPath()
    );
    for (Path path : invalidPaths) {
      assertThrows(IllegalArgumentException.class, new Runnable() {
        @Override
        public void run() {
          registry.add(path);
        }
      });
      assertFalse(registry.contains(path));
    }
    // the set of registered path should not have changed
    Set<Path> registeredPaths = registry.getRegisteredPaths();
    assertEquals(validPaths, registeredPaths);
    // lastly, test the remove method
    for (Path path : registeredPaths) {
      assertTrue(registry.contains(path));
      registry.remove(path);
      assertFalse(registry.contains(path));
    }
    assertTrue(registry.getRegisteredPaths().isEmpty());
  }

  @Slow
  public void testShutdownHook() throws Exception {
    // create a temp directory where the subprocess will create its own temp directory
    Path dir = Files.createTempDirectory(TestUtils.qualifiedTestName(this));
    TempFileRegistry.getInstance().add(dir);
    ProcessBuilder processBuilder = RuntimeUtils.buildNewJavaProcess();
    processBuilder.command().add(TempFileTester.class.getName());
    processBuilder.command().add(dir.toString());
    System.out.println("Starting " + String.join(" ", processBuilder.command()));
    Process subprocess = processBuilder.start();
    ServerIOUtils.pipeStreams(subprocess, TempFileTester.class.getSimpleName());
    int exitStatus = subprocess.waitFor();
    assertEquals(0, exitStatus);
    // make sure the directory is empty after the subprocess has terminated
    assertTrue(isEmpty(dir));
  }

  private static class TempFileTester {
    public static void main(String[] args) throws Exception {
      // the first arg is the path to the parent directory where to create the temp files
      Path parentDir = Paths.get(args[0]);
      Path subDir = Files.createDirectory(parentDir.resolve(TempFileTester.class.getSimpleName()));
      // create some dummy files in this directory
      TempFileRegistry.getInstance().add(subDir); // this is the operation being tested
      FileUtilsTest.createDummyFileTree(subDir, 3, 5);
      // print a listing of this directory
      Files.walkFileTree(parentDir, new FileUtils.FileTreePrintVisitor(System.out));
    }
  }
}