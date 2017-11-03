package solutions.trsoftware.commons.server.memquery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Methods marked with this annotation will be treated as projected fields with the specified name, and their
 * names can be passed to {@link MemQuery#createFor} and {@link MemQuery#addColumn} in order to
 * add automatically-generated ColSpecs to the query.
 *
 * @author Alex, 1/6/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProjectedField {
  /** If not specified, the name of the method will be used */
  String name() default "";
}
