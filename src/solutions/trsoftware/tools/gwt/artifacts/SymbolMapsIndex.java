package solutions.trsoftware.tools.gwt.artifacts;

import solutions.trsoftware.commons.server.io.FileUtils;
import solutions.trsoftware.commons.server.io.FileUtils.FilenamePatternVisitor;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Indexes the symbol maps emitted by the GWT compiler.
 */
public class SymbolMapsIndex {

  /**
   * The symbol map files indexed by permutation strong name.
   */
  private Map<String, Path> symbolMaps = new LinkedHashMap<>();

  /**
   * @param deployDir The root directory that contains the "deployable but not servable" files emitted by
   * the GWT compiler (the {@code -deploy} arg to the GWT compiler). This directory should contain the symbol maps.
   * @param filenamePattern Regex that matches the filenames of symbol maps emitted by the compiler into the
   * {@code -deploy} directory. The first group of this regex must capture the permutation strong name.
   */
  public SymbolMapsIndex(Path deployDir, Pattern filenamePattern) throws IOException {
    FileUtils.assertIsDirectory(deployDir);
    Files.walkFileTree(deployDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 5,
        new FilenamePatternVisitor(filenamePattern) {
          @Override
          protected FileVisitResult visitMatchedFile(Path file, BasicFileAttributes attrs, Matcher match) {
            String strongName = match.group(1);
            symbolMaps.put(strongName, file);
            return FileVisitResult.CONTINUE;
          }
        });
    if (symbolMaps.isEmpty())
      throw new IllegalArgumentException(String.format("No symbol map files matching /%s/ found in %s",
          filenamePattern.pattern(), deployDir));
  }

  public Map<String, Path> getSymbolMaps() {
    return symbolMaps;
  }

}
