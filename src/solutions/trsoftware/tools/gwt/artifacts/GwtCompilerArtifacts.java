package solutions.trsoftware.tools.gwt.artifacts;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Indexes a filesystem directory that contains the GWT Compiler output for a webapp.
 *
 * @author Alex
 * @since 1/27/2018
 */
public class GwtCompilerArtifacts {

  // TODO: test this class

  public static final String MODULE_STRONGNAME_RE = "([0-9A-F]{32})";
  /**
   * The filenames of modules emitted by the GWT compiler generally have the pattern
   * {@code {moduleStrongName}.cache.html} or {@code {moduleStrongName}.cache.js}.  The file type depends
   * on the <a href="http://www.gwtproject.org/doc/latest/DevGuideLinkers.html">linker</a> configured in the module XML.
   *
   * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideLinkers.html">GWT Linkers guide</a>
   */
  public static final Pattern PERMUTATION_FILENAME_PATTERN = Pattern.compile(MODULE_STRONGNAME_RE + "\\.cache\\.(?:js|html)");
  /**
   * The symbol maps emitted by the GWT compiler have the pattern {@code {moduleStrongName}.symbolMap}
   */
  public static final Pattern SYMBOLMAP_FILENAME_PATTERN = Pattern.compile(MODULE_STRONGNAME_RE + "\\.symbolMap");

  /** The root directory containing the GWT compiler output (i.e. the {@code -war} to the compiler) */
  private Path warDir;

  /**
   * The root directory into which "deployable but not servable" artifacts have been emitted (can be relative to {@link
   * #warDir}). This directory would typically include files such as symbol maps, source maps, and policy manifests.
   */
  private Path deployDir;

  private Set<File> permutationFiles = new LinkedHashSet<>();
  private Set<String> permutationStrongNames = new LinkedHashSet<>();

  /**
   * @param warDir root directory containing the GWT compiler output (i.e. the {@code -war} to the compiler)
   * @param deployDir root directory into which "deployable but not servable" artifacts have been emitted (can be
   *     relative to {@link #warDir}); this directory would typically include files such as symbol maps, source maps,
   *     and RPC policy manifests
   */
  public GwtCompilerArtifacts(Path warDir, Path deployDir) {
    this.warDir = warDir;
    // resolve the symbol maps directory
    if (!deployDir.isAbsolute())
      deployDir = warDir.resolve(deployDir);
    this.deployDir = deployDir;
/*    File[] files = warDir.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        Matcher matcher = PERMUTATION_FILENAME_PATTERN.matcher(file.getName());
        if (matcher.matches()) {
          permutationFiles.add(file);
          permutationStrongNames.add(matcher.group(1));
        }
      }
    }*/
  }

  public Set<String> getPermutationStrongNames() {
    return Collections.unmodifiableSet(permutationStrongNames);
  }
}
