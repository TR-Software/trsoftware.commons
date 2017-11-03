package solutions.trsoftware.commons.client.util.template;

import java.util.Map;

/** A part of the final output - either a string or a substituted variable */
public interface TemplatePart {
  /** Appends itself to the given string buffer, optionally using a substitution */
  public StringBuilder write(StringBuilder buffer, Map<String, String> substitutions);
}
