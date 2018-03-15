package solutions.trsoftware.tools.gwt.artifacts;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.linker.SymbolMapsLinker;
import solutions.trsoftware.commons.shared.util.MapUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the comment lines that contain the permutation id and the selection property values
 * at the head of a symbol map file.
 *
 * Example:
 * <pre>
 * # { 2 }
 * # { 'chromeWithColNumbers' : 'false' , 'debug' : 'off' , 'user.agent' : 'ie10' }
 * # { 'chromeWithColNumbers' : 'false' , 'debug' : 'on' , 'user.agent' : 'ie10' }
 * </pre>
 * @see SymbolMapsLinker#doWriteSymbolMap
 */
public class SymbolMapHeaderParser {

  private static final Pattern PERM_ID_PATTERN = Pattern.compile("# \\{ (\\d+) }");

  /**
   * @see CompilationResult#getPermutationId()
   */
  private int permutationId;

  private List<String> selectionPropertyLines = new ArrayList<>();

  private Multimap<String, String> selectionProperties = LinkedHashMultimap.create();

  public SymbolMapHeaderParser(Path symbolMap) throws IOException {
    try (BufferedReader br = Files.newBufferedReader(symbolMap)) {
      String line = br.readLine();
      // the first line contains the permutation ID
      Matcher permIdMatcher = PERM_ID_PATTERN.matcher(line);
      if (permIdMatcher.matches())
        permutationId = Integer.parseInt(permIdMatcher.group(1));
      else
        throw new IllegalArgumentException("First line of symbol map must contain the permutation ID, matching '" + PERM_ID_PATTERN + "'");
      // the next lines contain the selection properties
      Gson gson = new Gson();
      for (line = br.readLine(); line != null && line.startsWith("# {"); line = br.readLine()) {
        String propDefs = line.substring(2);
        selectionPropertyLines.add(propDefs);
        MapUtils.putAllToMultimap(selectionProperties,
            gson.fromJson(propDefs, new TypeToken<Map<String, String>>(){}.getType()));
      }
    }
  }

  public int getPermutationId() {
    return permutationId;
  }

  public List<String> getSelectionPropertyLines() {
    return selectionPropertyLines;
  }

  public Multimap<String, String> getSelectionProperties() {
    return selectionProperties;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("SymbolMapHeaderParser{");
    sb.append("permutationId=").append(permutationId);
    sb.append(", selectionProperties=").append(selectionProperties);
    sb.append('}');
    return sb.toString();
  }
}
