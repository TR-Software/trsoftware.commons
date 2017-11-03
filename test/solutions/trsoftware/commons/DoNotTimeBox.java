package solutions.trsoftware.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test methods that should not be surrounded with TimeBoxDecorator
 * should be marked with this annotation.  This is useful for tests
 *
 * @author Alex
 */
@Retention(RetentionPolicy.RUNTIME)  // the JVM won't have access usages of @Slow without this
@Target(ElementType.METHOD)  // only methods may be annotated with @Slow
public @interface DoNotTimeBox {
}