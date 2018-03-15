package solutions.trsoftware.commons.server.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Provides utilities for working with files using the new Java 7 {@link java.nio.file} API.
 *
 * @author Alex
 * @since 2/25/2018
 */
public class FileUtils {

  /** Path of the system temp directory, given by the {@code java.io.tmpdir} system property */
  public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

  /**
   * Copies the file tree rooted at the given path to a new temporary directory with the given name prefix.
   *
   * @see #createTempCopyOfDirectory(Path, String)
   */
  public static File createTempCopyOfDirectory(String srcDirPath, String tmpDirNamePrefix) throws IOException {
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
  public static File createTempCopyOfDirectory(Path srcDirPath, String tmpDirNamePrefix) throws IOException {
    assertIsDirectory(srcDirPath);
    Path tmpDir = Files.createTempDirectory(tmpDirNamePrefix);
    File file = copyFileTree(srcDirPath, tmpDir).toFile();
    file.deleteOnExit();
    return file;
  }

  /**
   * Same as {@link #createTempCopyOfDirectory(String, String)}, but catches any {@link IOException}
   * and re-throws it as a {@link RuntimeException}.
   *
   * @throws RuntimeException if the call to {@link #createTempCopyOfDirectory(String, String)} threw
   * and {@link IOException}
   */
  public static File createTempCopyOfDirectoryUnchecked(String srcDirPath, String tmpDirNamePrefix) throws RuntimeException {
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
  public static File createTempCopyOfDirectoryUnchecked(Path srcDirPath, String tmpDirNamePrefix) throws RuntimeException {
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
  public static Path copyFileTree(Path sourcePath, Path targetPath) throws IOException {
    // this code is copied verbatim from the FileVisitor javadoc
    Files.walkFileTree(sourcePath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
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
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
            return CONTINUE;
          }
        });
    return targetPath;
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
   * Provides the {@link File#deleteOnExit()} functionality for a {@link Path} object.
   * @param file the file to be deleted on exit
   * @return the same file that was passed in
   */
  public static Path deleteOnExit(Path file) {
    file.toFile().deleteOnExit();
    return file;
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
    protected abstract FileVisitResult visitMatchedFile(Path file, BasicFileAttributes attrs, Matcher match);
  }
}
