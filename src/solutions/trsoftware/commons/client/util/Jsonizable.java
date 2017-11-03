package solutions.trsoftware.commons.client.util;

/**
   * Supports objects that want to write themselves to the stream (that is,
 * objects that aren't a string/number/boolean or map/list thereof).
 */
public interface Jsonizable {
  /** Write self to the buffer in JSON notation */
  void dumpJson(StringBuilder sb);
}
