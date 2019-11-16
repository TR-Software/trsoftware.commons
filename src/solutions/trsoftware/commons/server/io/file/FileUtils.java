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

import solutions.trsoftware.commons.server.util.ServerStringUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Provides utilities for working with files using the new Java 7 {@link java.nio.file} API.
 *
 * @author Alex
 * @since 2/25/2018
 */
public class FileUtils {

  /**
   * The JVM property that gives the path to the system temporary-file directory.
   * @see #TEMP_DIR_PATH
   */
  public static final String TEMP_DIR_PROP = "java.io.tmpdir";

  /** Path of the system temp directory, given by the {@value #TEMP_DIR_PROP} system property */
  public static final String TEMP_DIR_PATH = System.getProperty(TEMP_DIR_PROP);

  /**
   * Copies the file tree rooted at the given path to a new temporary directory with the given name prefix.
   *
   * @see #createTempCopyOfDirectory(Path, String)
   */
  public static Path createTempCopyOfDirectory(String srcDirPath, String tmpDirNamePrefix) throws IOException {
    return createTempCopyOfDirectory(Paths.get(srcDirPath), tmpDirNamePrefix);
  }

  /**
   * Copies the file tree rooted at the given path to a new temporary directory with the given name prefix.
   *
   * @param srcDirPath should be a valid filesystem path of an existing directory.
   * @param tmpDirNamePrefix the argument to {@link Files#createTempDirectory(String, FileAttribute[])}
   * @return the temp directory that was created
   * @throws IOException
   * @throws IllegalArgumentException if {@code srcDir} does not exist or not a directory
   */
  public static Path createTempCopyOfDirectory(Path srcDirPath, String tmpDirNamePrefix) throws IOException {
    assertIsDirectory(srcDirPath);
    Path tmpDir = Files.createTempDirectory(tmpDirNamePrefix);
    copyFileTree(srcDirPath, tmpDir);
    deleteOnExit(tmpDir);
    return tmpDir;
  }

  /**
   * Same as {@link #createTempCopyOfDirectory(String, String)}, but catches any {@link IOException}
   * and re-throws it as a {@link RuntimeException}.
   *
   * @throws RuntimeException if the call to {@link #createTempCopyOfDirectory(String, String)} threw
   * and {@link IOException}
   */
  public static Path createTempCopyOfDirectoryUnchecked(String srcDirPath, String tmpDirNamePrefix) throws RuntimeException {
    try {
      return createTempCopyOfDirectory(srcDirPath, tmpDirNamePrefix);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Same as {@link #createTempCopyOfDirectory(Path, String)}, but catches any {@link IOException}
   * and re-throws it as a {@link RuntimeException}.
   *
   * @throws RuntimeException if the call to {@link #createTempCopyOfDirectory(Path, String)} threw
   * and {@link IOException}
   */
  public static Path createTempCopyOfDirectoryUnchecked(Path srcDirPath, String tmpDirNamePrefix) throws RuntimeException {
    try {
      return createTempCopyOfDirectory(srcDirPath, tmpDirNamePrefix);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Recursively copies files.
   * @param sourcePath the root directory of the file tree to be copied
   * @param targetPath the destination directory
   * @return {@code targetPath}
   *
   * @see Files#walkFileTree(Path, Set, int, FileVisitor)
   * @see Files#copy(Path, Path, CopyOption...)
   */
  // TODO: unit test this
  public static Path copyFileTree(Path sourcePath, Path targetPath) throws IOException {
    // this code is copied verbatim from the FileVisitor javadoc
    Files.walkFileTree(sourcePath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetDir = targetPath.resolve(sourcePath.relativize(dir));
            try {
              Files.copy(dir, targetDir);
            }
            catch (FileAlreadyExistsException e) {
              if (!Files.isDirectory(targetDir))
                throw e;
            }
            return CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
            return CONTINUE;
          }
        });
    return targetPath;
  }

  /**
   * Recursively deletes files.
   * @param root the root directory of the file tree to be deleted
   *
   * @see #copyFileTree(Path, Path) 
   * @see Files#walkFileTree(Path, Set, int, FileVisitor)
   * @see Files#delete(Path)
   */
  // TODO: unit test this
  public static void deleteFileTree(Path root) throws IOException {
    // this code is copied verbatim from the FileVisitor javadoc
    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
        if (e == null) {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
        else {
          // directory iteration failed
          throw e;
        }
      }
    });
  }

  /**
   * @param path the path to test
   * @throws IllegalArgumentException if the given path does not represent an existing directory on the filesystem
   */
  public static void assertIsDirectory(Path path) throws IllegalArgumentException{
    if (!Files.exists(path) || !Files.isDirectory(path))
      throw new IllegalArgumentException(path.toString() + " does not exist or not a directory");
  }

  /**
   * Creates a directory identified by the given path (including all intermediate directories) if it doesn't already
   * exist. If the path already exists, asserts that it's a directory.
   *
   * @param path directory to be created if it doesn't already exist
   * @param attrs an optional list of file attributes to set atomically when creating the directory
   * @return the given path
   * @throws NotDirectoryException if the given path already exists, but is not a directory
   * @see Files#createDirectories(Path, FileAttribute[])
   */
  public static Path maybeCreateDirectory(Path path, FileAttribute<?>... attrs) throws IOException {
    try {
      return Files.createDirectories(path);
    }
    catch (FileAlreadyExistsException e) {
      if (!Files.isDirectory(path)) {
        throw new NotDirectoryException(path.toString());
      }
    }
    return path; // should never reach this statement (Files.createDirectories either succeeds or throws an exception)
  }

  /**
   * Adds the given path to the global {@link TempFileRegistry}, which ensures that a proper  sequence of
   * {@link File#deleteOnExit()} calls will be made during shutdown (thereby allowing the cleanup of non-empty directories).
   *
   * @param path the file or directory to be deleted on exit
   * @return the same object that was passed in
   * @see TempFileRegistry
   */
  public static Path deleteOnExit(Path path) {
    TempFileRegistry.getInstance().add(path);
    return path;
  }

  /**
   * Creates a new file object by inserting the given suffix string before the
   * extension of the original filename.
   * @param originalFile (e.g. {@code "/home/foo/bar.txt"})
   * @param suffix (e.g. {@code "_bak"})
   * @return a file like {@code "/home/foo/bar_bak.txt"} (based on the above example)
   */
  public static File fileWithSuffix(File originalFile, String suffix) {
    return new File(originalFile.getParent(), filenameWithSuffix(originalFile.getName(), suffix));
  }

  /**
   * Creates a new filename by inserting the given suffix string before the extension
   * of the original file.
   * @param originalName (e.g. {@code "bar.txt"})
   * @param suffix (e.g. {@code "_bak"})
   * @return a filename like {@code "bar_bak.txt"} (based on the above example)
   */
  public static String filenameWithSuffix(String originalName, String suffix) {
    StringBuilder name = new StringBuilder(originalName);
    int extensionStartIndex = name.lastIndexOf(".");
    if (extensionStartIndex >= 0) {
      name.insert(extensionStartIndex, suffix);
    }
    else {
      name.append(suffix);
    }
    return name.toString();
  }

  /**
   * Returns the portion of the given filename preceding any dot characters.
   * @param filename (e.g. {@code "bar.txt"})
   * @return the prefix (e.g. {@code "bar"})
   */
  public static String filenamePrefix(String filename) {
    // TODO(10/11/2019): should we use lastIndexOf here? because extension is technically after the last dot
    // TODO: extract this code to StringUtils (can call it substringBefore)
    int extensionStartIndex = filename.indexOf(".");
    if (extensionStartIndex < 0)
      return filename;  // the name doesn't have an extension
    return filename.substring(0, extensionStartIndex);
  }

  /**
   * Returns the portion of the given filename that follows the last {@code .} (dot) char.
   * @param filename (e.g. {@code "bar.txt"})
   * @return the filename extension (e.g. {@code "txt"}), or an empty string if the filename doesn't have an extension.
   */
  public static String filenameExtension(String filename) {
    // TODO: extract this code to StringUtils (can call it substringAfter)
    int extensionStartIndex = filename.lastIndexOf(".");
    if (extensionStartIndex < 0)
      return "";  // the name doesn't have an extension
    return filename.substring(extensionStartIndex + 1);
  }

  /** Given "a","b","c", returns "a/b/c", where '/' is the File.separator */
  public static String joinPath(String... pathElements) {
    return StringUtils.join(File.separator, pathElements);
  }

  /** Given "a","b","c", returns a File representing "a/b/c", where '/' is the File.separator */
  public static File joinPathAsFile(String... pathElements) {
    return new File(joinPath(pathElements));
  }

  /**
   * URL-decodes the result of {@link URL#getFile()}, so that it can be used to construct a {@link File} or
   * {@link Path} object.
   *
   * This decoding step is necessary because the file path might contain spaces, which {@link URL#getFile()} would return as
   * {@code %20}, and the file system won't recognize them.
   *
   * @return The URL-decoded result of {@link URL#getFile()}
   * @throws NullPointerException if the argument is {@code null}
   */
  public static String urlToFilepath(URL fileUrl) {
    // there's a bug in the JVM that fails on file paths with spaces (b/c spaces are url-encoded in a URL)
    return ServerStringUtils.urlDecode(fileUrl.getFile());
  }

  /**
   * @return A {@link File} created from the URL-decoded result of {@link URL#getFile()}
   * @see #urlToFilepath(URL)
   */
  public static File urlToFile(URL fileUrl) {
    // there's a bug in the JVM that breaks on file paths with spaces b/c spaces are url-encoded
    return new File(urlToFilepath(fileUrl));
  }


  /**
   * Checks whether a directory is empty
   * @param dir the directory to check for presence of files
   * @return {@code true} iff the directory does not contain an files (or subdirectories)
   */
  public static boolean isEmpty(Path dir) throws IOException {
    return !Files.list(dir).findAny().isPresent();
  }

  /**
   * Lists the entries in the given directory, by materializing the stream returned by {@link Files#list(Path)}.
   *
   * @param dir the directory to list
   * @return a list of the entries in the given directory
   * @see Files#list(Path)
   */
  public static List<Path> listFiles(Path dir) throws IOException {
    return Files.list(dir).collect(Collectors.toList());
  }

  /**
   * Base class for a {@link FileVisitor} that matches filenames against a regular expression.
   * Intended to be used with {@link Files#walkFileTree}.
   * <p>
   * Implementations just have to provide the {@link #visitMatchedFile(Path, BasicFileAttributes, Matcher)} method,
   * which will be invoked for every file matching the pattern.
   * <p>
   * <em>NOTE: {@link FileSystem#getPathMatcher(String)} (Path, String)} offers similar (but more limited) functionality
   * using <a href="https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob">"glob" expressions</a></em>,
   * which might be preferable (when full regex matching is not required), because glob matching is implemented natively
   * by many filesystems and allows using the convenient "{@code /**}" syntax to "cross directory boundaries". This can
   * be used with {@link Files#newDirectoryStream(Path, String)} (non-recursive) or {@link Files#walk(Path,
   * FileVisitOption...)} (recursive)
   * <p>
   * Either way, using a regex ({@link Pattern}) allows for more precise matching than a glob and can be used to extract
   * useful information from each match (via capturing groups).
   *
   * @see Files#walkFileTree
   * @see Files#walk
   * @see Files#newDirectoryStream(Path, String)
   * @see <a href="https://stackoverflow.com/q/37383668">StackOverflow question illustrating PathMatcher usage</a>
   * @see Pattern
   */
  public static abstract class FilenamePatternVisitor extends SimpleFileVisitor<Path> {
    private final Pattern filenamePattern;

    /**
     * @param filenamePattern regex to be used for matching filenames.
     */
    public FilenamePatternVisitor(Pattern filenamePattern) {
      this.filenamePattern = filenamePattern;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Matcher matcher = filenamePattern.matcher(file.getFileName().toString());
      if (matcher.matches()) {
        return visitMatchedFile(file, attrs, matcher);
      }
      return CONTINUE;
    }

    /**
     * Do something with a matched file.  Will be invoked for every file that matches the regular expression.
     *
     * @param file the {@code file} argument received by {@link #visitFile(Path, BasicFileAttributes)}
     * @param attrs the {@code attrs} argument received by {@link #visitFile(Path, BasicFileAttributes)}
     * @param match the {@link Matcher} that was used to match the filename. Can be used to extract info via
     *     capturing groups
     * @return how to proceed from this point
     */
    protected abstract FileVisitResult visitMatchedFile(Path file, BasicFileAttributes attrs, Matcher match) throws IOException;
  }

  public static class FileTreePrintVisitor extends SimpleFileVisitor<Path> {
    protected int level = 0;
    protected PrintStream out;
    protected boolean rootDirPrinted;

    public FileTreePrintVisitor(PrintStream out) {
      this.out = out;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      printDir(dir);
      level++;
      return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      printFile(file);
      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      level--;
      return CONTINUE;
    }

    protected void printDir(Path entry) {
      out.printf("%s|-+ %s%n", StringUtils.indent(level*2), getDirName(entry));
    }

    protected String getDirName(Path dir) {
      Path name = rootDirPrinted ? dir.getFileName() : dir;
      rootDirPrinted = true;
      return name.toString();
    }

    protected void printFile(Path entry) {
      out.printf("%s|- %s%n", StringUtils.indent(level*2), entry.getFileName());
    }
  }
}
