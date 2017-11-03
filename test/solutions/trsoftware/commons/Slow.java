package solutions.trsoftware.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test methods that take more than 5 seconds to execute should be marked
 * with this annotation.
 *
 * @author Alex
 */
@Retention(RetentionPolicy.RUNTIME)  // the JVM won't have access usages of @Slow without this
@Target(ElementType.METHOD)  // only methods may be annotated with @Slow
public @interface Slow {
}
