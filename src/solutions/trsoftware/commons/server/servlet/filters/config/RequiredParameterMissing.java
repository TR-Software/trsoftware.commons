package solutions.trsoftware.commons.server.servlet.filters.config;

import javax.servlet.FilterConfig;

/**
 * @author Alex
 * @since 1/3/2018
 */
class RequiredParameterMissing extends ParseException {
  RequiredParameterMissing(String paramName, Class<?> expectedType, FilterConfig filterConfig) {
    super("Missing value for required init-param", paramName, null, expectedType, filterConfig);
  }
}
