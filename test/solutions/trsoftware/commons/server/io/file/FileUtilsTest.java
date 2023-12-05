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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static solutions.trsoftware.commons.server.io.file.FileUtils.*;

/**
 * @author Alex
 * @since 3/21/2018
 */
public class FileUtilsTest extends TestCase {

  public void testFileWithSuffix() throws Exception {
    assertEquals(System.getProperty("user.dir") + File.separator + "bar_bak.txt",
        fileWithSuffix(new File(System.getProperty("user.dir") + File.separator + "bar.txt"), "_bak").getAbsolutePath());
  }

  public void testFilenameWithSuffix() throws Exception {
    assertEquals("bar_bak.txt", filenameWithSuffix("bar.txt", "_bak"));
  }

  public void testFilenamePrefix() throws Exception {
    assertEquals("foo", filenamePrefix("foo.txt"));
    assertEquals("foo", filenamePrefix("foo.txt.gif"));
  }

  public void testFilenameExtension() throws Exception {
    assertEquals("txt", filenameExtension("foo.txt"));
    assertEquals("gif", filenameExtension("foo.txt.gif"));
    assertEquals("", filenameExtension("foo"));
  }

  public void testUrlToFilepath() throws Exception {
    // check that URL-decoding is performed
    String path = "/C:/foo bar/x y/z";
    System.out.println("path = " + path);
    // the URI constructor will properly encode the path (i.e. replace spaces with %20)
    URI uri = new URI("file", null, path, null);
    System.out.println("URI = " + uri);
    URL url = uri.toURL();
    System.out.println("URL = " + url);
    assertEquals(path, urlToFilepath(url));
  }

  public void testFileTreePrintVisitor() throws Exception {
    Path dir = ReflectionUtils.getClassFile(getClass()).toPath().getParent();
    Files.walkFileTree(dir, new FileTreePrintVisitor(System.out));
  }

  public void testIsEmpty() throws Exception {
    Path tempDir = createTempDir();
    System.out.println("tempDir = " + tempDir);
    deleteOnExit(tempDir);
    assertTrue(isEmpty(tempDir));
    createSomeDummyFiles(tempDir, 1);
    assertFalse(isEmpty(tempDir));
  }

  /**
   * <ol>
   *   <li>
   *     Spawns another Java process that creates a non-empty temp directory
   *     and calls {@link FileUtils#deleteOnExit(Path)} on it.
   *   </li>
   *   <li>
   *     Asserts that none of those files still exist after that process has terminated.
   *   </li>
   * </ol>
   */
  @Slow
  public void testDeleteOnExit() throws Exception {
    // create a temp directory where the subprocess will create its own temp directory
    Path dir = createTempDir();
    deleteOnExit(dir);
    ProcessBuilder processBuilder = RuntimeUtils.buildNewJavaProcess();
    processBuilder.command().add(TempDirTester.class.getName());
    processBuilder.command().add(dir.toString());
    System.out.println("Starting " + String.join(" ", processBuilder.command()));
    Process subprocess = processBuilder.start();
    ServerIOUtils.pipeStreams(subprocess, TempDirTester.class.getSimpleName());
    int exitStatus = subprocess.waitFor();
    assertEquals(0, exitStatus);
    // make sure the directory is empty after the subprocess has terminated
    assertTrue(isEmpty(dir));
  }

  private Path createTempDir() throws IOException {
    return Files.createTempDirectory(TestUtils.qualifiedTestName(this));
  }


  private static class TempDirTester {
    public static void main(String[] args) throws Exception {
      // the first arg is the path to the parent directory where to create the temp files
      Path parentDir = Paths.get(args[0]);
      // create a temp directory
      Path tempDir = Files.createTempDirectory(parentDir, FileUtilsTest.class.getSimpleName() + "." + TempDirTester.class.getSimpleName());
      FileUtils.deleteOnExit(tempDir); // this is the operation being tested
      createDummyFileTree(tempDir, 3, 5);
      // print a listing of this directory
      Files.walkFileTree(parentDir, new FileTreePrintVisitor(System.out));
    }
  }

  /**
   * Creates a file tree of the given depth, with each level containing {@code n} empty files and
   * {@code n} subdirectories.
   *
   * @param root the root directory of the file tree to be created; must exist
   * @param height the height of the resultant file tree
   * @param n the desired number of files and subdirectories at each level (the degree of each internal node will
   * be {@code n*2})
   * @throws IOException
   */
  static void createDummyFileTree(Path root, int height, int n) throws IOException {
    if (height < 0)
      throw new IllegalArgumentException("height < 0");
    // create some files in this directory
    createSomeDummyFiles(root, n);
    if (height == 0)
      return;
    // create some subdirectories with files
    for (int i = 0; i < n; i++) {
      Path subdir = root.resolve("subdir" + i);
      Files.createDirectory(subdir);
      createDummyFileTree(subdir, height-1, n);
    }
  }

  /**
   * Creates a number of empty files in the given directory.
   *
   * @param dir directory in which the files are to be created
   * @param n number of files to create
   * @return the created files
   */
  static List<Path> createSomeDummyFiles(Path dir, int n) throws IOException {
    ArrayList<Path> ret = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      ret.add(Files.createFile(dir.resolve("file" + i)));
    }
    return ret;
  }
}