package solutions.trsoftware.commons.server.servlet.config;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface that defines the {@code init-param} configuration of a <i>Java Servlet API</i> object
 * such as {@link Servlet}, {@link Filter}, or {@link ServletContext}.
 *
 * <p>
 * {@link WebConfigParser} uses this interface to bind {@code init-param} values defined in a webapp's deployment descriptor
 * to the fields declared by a class implementing this interface.
 * </p>
 *
 * @author Alex
 * @since 1/2/2018
 */
public interface InitParameters {

  interface ParameterParser<T> {
    T parse(String value);
  }

  /**
   * Fields of an {@link InitParameters} subclass may be marked with this optional annotation, which
   * allows customizing how they're mapped from the {@code init-param} values in a webapp configuration
   * by a {@link WebConfigParser}.
   */
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Param {
    /**
     * @return {@code true} if a {@link RequiredInitParameterMissing} exception should be thrown when the
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
