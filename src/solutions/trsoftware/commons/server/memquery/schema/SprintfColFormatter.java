package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Formatter;

/**
 * @author Alex, 1/5/14
 */
public class SprintfColFormatter implements Formatter {
  private final String formatSpec;

  public SprintfColFormatter(String formatSpec) {
    this.formatSpec = formatSpec;
  }

  @Override
  public String format(Object value) {
    // TODO: temp try/catch
    try {
      return String.format(formatSpec, value);
    } catch (RuntimeException ex) {
      ex.printStackTrace();
      throw ex;
    }
  }
}
