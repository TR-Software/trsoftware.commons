package solutions.trsoftware.commons.client.util;

/**
 * Mar 24, 2010
 *
 * @author Alex
 */
public class MessageFormatter {
  public static String exceptionTypeAndMessageToString(Throwable ex) {
    return StringUtils.template("$1 ($2)", ex.getMessage(), exceptionTypeToString(ex));
  }

  /** Convenience method for getting the name of the class of the given Exception */
  public static String exceptionTypeToString(Throwable ex) {
    String type = "Unknown";
    // these null checks are probably not necessary, but just in case...
    if (ex.getClass() != null) {
      String className = ex.getClass().getName();
      if (StringUtils.notBlank(className))
        type = className;
    }
    return type;
  }
}
