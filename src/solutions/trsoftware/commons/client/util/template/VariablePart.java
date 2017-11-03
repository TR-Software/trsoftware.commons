package solutions.trsoftware.commons.client.util.template;

import java.util.Map;

/**
 * Replaces its variable with the given substitution, or if not there,
 * writes nothing to the buffer.
 */
public class VariablePart implements TemplatePart {
  private final String varName;

  public VariablePart(String varName) {
    this.varName = varName;
  }

  public StringBuilder write(StringBuilder buffer, Map<String, String> substitutions) {
    if (substitutions.containsKey(varName))
      return buffer.append(String.valueOf(substitutions.get(varName)));
    return buffer;
  }

  public String getVarName() {
    return varName;
  }

  @Override
  public String toString() {
    return new StringBuilder("${").append(varName).append('}').toString();
  }
}
