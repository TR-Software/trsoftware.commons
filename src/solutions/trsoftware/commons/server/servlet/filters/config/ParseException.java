package solutions.trsoftware.commons.server.servlet.filters.config;

import javax.servlet.FilterConfig;

import static solutions.trsoftware.commons.shared.util.StringUtils.valueToString;

/**
 * @author Alex
 * @since 1/3/2018
 */
public abstract class ParseException extends Exception {
  private String paramName;
  private String paramValue;
  private Class<?> expectedType;
  private FilterConfig filterConfig;

  ParseException(String message, Throwable cause, String paramName, String paramValue, Class<?> expectedType, FilterConfig filterConfig) {
    super(message + " " + paramInfoToString(paramName, paramValue, expectedType), cause);
    this.paramName = paramName;
    this.paramValue = paramValue;
    this.expectedType = expectedType;
  }

  ParseException(String message, String paramName, String paramValue, Class<?> expectedType, FilterConfig filterConfig) {
    this(message, null, paramName, paramValue, expectedType, filterConfig);
  }

  public String getParamName() {
    return paramName;
  }

  public String getParamValue() {
    return paramValue;
  }

  public Class<?> getExpectedType() {
    return expectedType;
  }

  public FilterConfig getFilterConfig() {
    return filterConfig;
  }

  private static String paramInfoToString(String paramName, String paramValue, Class<?> expectedType) {
    final StringBuilder sb = new StringBuilder("{");
    sb.append("param-name: ").append(valueToString(paramName));
    sb.append(", param-value: ").append(valueToString(paramValue));
    sb.append(", expected type: ").append(expectedType.getSimpleName());
    sb.append('}');
    return sb.toString();
  }

}
