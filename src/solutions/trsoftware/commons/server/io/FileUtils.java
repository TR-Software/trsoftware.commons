package solutions.trsoftware.commons.server.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
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
   * Recursively copies the file tree rooted at {@code sourcePath} to {@code targetPath}.
   * @return {@code targetPath}
   * @throws IOException
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


}
