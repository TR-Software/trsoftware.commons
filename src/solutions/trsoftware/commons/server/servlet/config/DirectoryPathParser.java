package solutions.trsoftware.commons.server.servlet.config;

import solutions.trsoftware.commons.server.io.file.FileUtils;

import java.nio.file.Path;

/**
 * Extends {@link PathParser#parse(String)} to assert that the path refers to an existing directory.
 *
 * @see FileUtils#assertIsDirectory(Path)
 * @author Alex
 * @since 7/29/2018
 */
public class DirectoryPathParser extends PathParser {

  @Override
  public Path parse(String pathStr) throws Exception {
    Path path = super.parse(pathStr);
    FileUtils.assertIsDirectory(path);
    return path;
  }
}
