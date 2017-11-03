package solutions.trsoftware.commons.server.tools;

import solutions.trsoftware.commons.client.util.MemoryUnit;
import solutions.trsoftware.commons.client.util.stats.MaxComparable;
import solutions.trsoftware.commons.client.util.stats.NumberSample;
import solutions.trsoftware.commons.server.io.SplitterOutputStream;

import java.io.*;
import java.util.*;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.joinPath;
import static solutions.trsoftware.commons.server.io.ServerIOUtils.joinPathAsFile;

/**
 * Prints a report about the sizes of the compiled GWT modules
 * (all the .cache.html files in the output war directory).
 * Also saves a copy of the report in the war file's /WEB_INF/gwtExtras dir.
 *
 * NOTE: this class doesn't rezlly need to be shipped with the WAR distribution,
 * but we're just doing that for convenience, to keep the Ant build script simple. 
 * 
 * @author Alex
 * @since Apr 1, 2013
 */
public class GwtCompilationStatsReport {

  private static final String CACHE_HTML_FILE_SUFFIX = ".cache.html";
  private static final FilenameFilter cacheHtmlFilenameFilter = newSuffixFileFilter(CACHE_HTML_FILE_SUFFIX);

  /** Creates a filter that matches filenames ending with the given suffix */
  private static FilenameFilter newSuffixFileFilter(final String fileNameSuffix) {
    return new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(fileNameSuffix);
      }
    };
  }

  private static final Comparator<File> fileSizeComparator = new Comparator<File>() {
    public int compare(File o1, File o2) {
      return ((Long)o1.length()).compareTo(o2.length());
    }
  };

  public static void main(String[] args) throws IOException {
    // the first arg is the path to the war directory
    String warDirPath = args[0];
    String gzDirPath = joinPath(warDirPath, "gz");
    File warDir = getExistingDir(warDirPath);

    // 1) generate a report summarizing the module file sizes
    Map<String, File> dirMap = new HashMap<String, File>();
    dirMap.put("plain", warDir);
    dirMap.put("gzipped", getExistingDir(gzDirPath));

    String extrasDirPathSuffix = joinPath("WEB-INF", "gwtExtras");
    String reportSavePathSuffix = joinPath(extrasDirPathSuffix, GwtCompilationStatsReport.class.getSimpleName() + ".txt");
    PrintStream out = new PrintStream(new SplitterOutputStream(
        System.out,
        new FileOutputStream(joinPath(warDirPath, reportSavePathSuffix))
    ));

    out.println("This report will appear in " + reportSavePathSuffix);
    out.println("(generated on " + new Date() + " using " + GwtCompilationStatsReport.class.getName() + ")");
    out.println();
    out.println("GWT Compiler Options: " + args[1]);
    out.println();
    out.println("GWT module .cache.html file size stats (in KB):");
    out.println("-----------------------------------------------");
    out.printf("%11s,%5s,%5s,%5s%n", "Output Type", "Min", "Max", "Avg");
    for (Map.Entry<String, File> entry : dirMap.entrySet()) {
      NumberSample<Long> sizeStats = computeFileSizeStats(entry.getValue());
      out.printf("%11s,%5.0f,%5.0f,%5.0f%n", entry.getKey(),
          MemoryUnit.KILOBYTES.fromBytes(sizeStats.min()),
          MemoryUnit.KILOBYTES.fromBytes(sizeStats.max()),
          MemoryUnit.KILOBYTES.fromBytes(Math.round(sizeStats.mean())));
    }
    // 2) generate a detailed report on each permutation (using the info available in symbol maps)
    // the GWT compiler initially outputs the symbol maps to <deployDir>/<moduleName>/symbolMaps,
    // but our Ant build moves them all to the same dir in the WAR file
    // therefore we want to read the original dirs so we can correlate the module names with the strong names
    String originalExtrasDirPath = joinPath(warDirPath, "..", "gwt", "gwtExtras");
    File originalExtrasDir = getExistingDir(originalExtrasDirPath);
    File[] moduleDirs = originalExtrasDir.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });
    Map<String, File[]> symbolMapsByModuleName = new HashMap<String, File[]>();
    String symbolMapFilenameSuffix = ".symbolMap";
    MaxComparable<Integer> maxModuleNameLength = new MaxComparable<Integer>();
    for (File dir : moduleDirs) {
      String moduleName = dir.getName();
      maxModuleNameLength.update(moduleName.length());
      File symbolMapsDir = getExistingDir(joinPath(originalExtrasDirPath, moduleName, "symbolMaps"));
      symbolMapsByModuleName.put(moduleName,
          symbolMapsDir.listFiles(newSuffixFileFilter(symbolMapFilenameSuffix)));
    }


//    File symbolMapsDir = joinPathAsFile(warDirPath, extrasDirPathSuffix, "symbolMaps");
    out.println();
    out.println("GWT module .cache.html file permutation details:");
    out.println("------------------------------------------------");

    // print what: filename, plain size, gzip size, permutation id (which corresponds to the compile report), deferred binding properties, module name
    ArrayList<PermutationInfo> perms = new ArrayList<PermutationInfo>();
    for (String moduleName : symbolMapsByModuleName.keySet()) {
      for (File symbolMapFile : symbolMapsByModuleName.get(moduleName)) {
        String strongName = symbolMapFile.getName().substring(0, symbolMapFile.getName().length() - symbolMapFilenameSuffix.length());
        File moduleFile = joinPathAsFile(warDirPath, strongName + CACHE_HTML_FILE_SUFFIX);
        // read the module name from the module file
        BufferedReader symbolMapReader = new BufferedReader(new FileReader(symbolMapFile));
        perms.add(new PermutationInfo(strongName,
            MemoryUnit.KILOBYTES.fromBytes(moduleFile.length()),
            MemoryUnit.KILOBYTES.fromBytes(joinPathAsFile(gzDirPath, strongName + CACHE_HTML_FILE_SUFFIX).length()),
            moduleName,
            // the permutation # and deferred binding properties are contained in the first two lines of the symbol maps file
            symbolMapReader.readLine().substring(2), // strip the "# " chars at the beginning of the line
            symbolMapReader.readLine().substring(2))); // strip the "# " chars at the beginning of the line
        symbolMapReader.close();
      }
    }
    // sort the files by perm#
    Collections.sort(perms);
    // now print the info about each permutation
    out.printf("%32s, %12s, %" + maxModuleNameLength.get() + "s, %6s, %s%n", "strongName", "Size (KB)", "moduleName", "Perm#", "Properties");
    out.printf("%32s, %5s, %5s,%n", "", "plain", "gzip");
    for (PermutationInfo permInfo : perms) {
      out.printf("%32s, %5.0f, %5.0f, %" + maxModuleNameLength.get() + "s, %6s, %s%n", permInfo.strongName,
          permInfo.sizePlainKb, permInfo.sizeGzipKb, permInfo.moduleName, permInfo.permId, permInfo.props);
    }
  }

  private static class PermutationInfo implements Comparable<PermutationInfo> {
    private String strongName;
    private double sizePlainKb, sizeGzipKb;
    private String moduleName, permId, props;

    PermutationInfo(String strongName, double sizePlainKb, double sizeGzipKb, String moduleName, String permId, String props) {
      this.strongName = strongName;
      this.sizePlainKb = sizePlainKb;
      this.sizeGzipKb = sizeGzipKb;
      this.moduleName = moduleName;
      this.permId = permId;
      this.props = props;
    }

    public int compareTo(PermutationInfo o) {
      // sort first by module name then by permId
      int modNameCmp = moduleName.compareTo(o.moduleName);
      if (modNameCmp != 0)
        return modNameCmp;
      else
        return permId.compareTo(o.permId);
    }
  }


  private static NumberSample<Long> computeFileSizeStats(File dir) {
    NumberSample<Long> fileSizeStats = new NumberSample<Long>();
    for (File file : dir.listFiles(cacheHtmlFilenameFilter)) {
      fileSizeStats.update(file.length());
    }
    return fileSizeStats;
  }

  /** Creates a file from the given path and asserts that it already exists */
  private static File getExistingDir(String path) {
    File file = new File(path);
    if (!file.exists())
      throw new IllegalArgumentException("Directory does not exist: " + path);
    if (!file.isDirectory())
      throw new IllegalArgumentException("Not a directory: " + path);
    return file;
  }

}
