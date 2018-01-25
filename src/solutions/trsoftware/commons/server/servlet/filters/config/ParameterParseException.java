package solutions.trsoftware.commons.server.servlet.filters.config;

import javax.servlet.FilterConfig;

/**
 * @author Alex
 * @since 1/3/2018
 */
class ParameterParseException extends ParseException {
  ParameterParseException(String paramName, String paramValue, Class<?> expectedType, Throwable cause, FilterConfig filterConfig) {
    super("Unable to parse init-param", cause, paramName, paramValue, expectedType, filterConfig);
  }
}
