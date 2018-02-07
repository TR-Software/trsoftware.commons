package solutions.trsoftware.commons.server.gwt;

import solutions.trsoftware.commons.shared.util.LazyReference;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Indexes a filesystem directory that contains the GWT Compiler output for a module.
 *
 * @author Alex
 * @since 1/27/2018
 */
public class GwtModuleFiles {

  // TODO: test this class

  public static final LazyReference<Pattern> cacheDotHtmlFilenamePattern = new LazyReference<Pattern>() {
    @Override
    protected Pattern create() {
      return Pattern.compile(".*([0-9A-F]{32})\\.cache\\.html");
    }
  };

  /** The directory containing the GWT compiler output */
  private File baseDir;

  private Set<File> cacheHtmlFiles = new LinkedHashSet<>();
  private Set<String> permutationStrongNames = new LinkedHashSet<>();

  public GwtModuleFiles(File baseDir) {
    this.baseDir = baseDir;
    File[] files = baseDir.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        Matcher matcher = cacheDotHtmlFilenamePattern.get().matcher(file.getName());
        if (matcher.matches()) {
          cacheHtmlFiles.add(file);
          permutationStrongNames.add(matcher.group(1));
        }
      }
    }
  }

  public Set<String> getPermutationStrongNames() {
    return Collections.unmodifiableSet(permutationStrongNames);
  }
}
