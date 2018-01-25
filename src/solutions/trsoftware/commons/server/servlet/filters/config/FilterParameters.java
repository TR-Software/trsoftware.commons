package solutions.trsoftware.commons.server.servlet.filters.config;

import solutions.trsoftware.commons.server.servlet.filters.AbstractFilter;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for a class that defines a {@link Filter}'s {@code init-param} configuration.
 *
 * <p>
 * Our {@link FilterConfigParser} class binds the context's {@code init-param} values for a {@link Filter}
 * to the fields declared by a class implementing this interface.
 * </p>
 *
 * @see AbstractFilter#parse(FilterParameters)
 *
 * @author Alex
 * @since 1/2/2018
 */
public interface FilterParameters {

  interface ParameterParser<T> {
    T parse(String value);
  }

  /**
   * Fields of a {@link FilterParameters} subclass may be marked with this optional annotation, which
   * allows customizing how they're mapped from the {@code init-param} values in a {@link FilterConfig}
   * by a {@link FilterConfigParser}.
   */
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Param {
    /**
     * @return {@code true} if a {@link RequiredParameterMissing} exception should be thrown when the
     * {@link FilterConfig} does not contain a value for this init parameter.
     */
    boolean required() default false;
    /**
     * @return the name of the init parameter (arg for invoking {@link FilterConfig#getInitParameter(String)})
     * corresponding to the param for this field. Will default to the declared name of the field.
     */
    String name() default "";
    /**
     * Required if the field type doesn't have a {@code static valueOf(String)} method
     * that takes the param value and returns an instance of the field type (all primitive wrapper types have this method).
     * @return a class implementing {@link ParameterParser} to be used for parsing the {@link FilterConfig}
     * init parameter value for this field.
     */
    Class<? extends ParameterParser> parser() default ParameterParser.class;
  }
}
