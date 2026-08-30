/*
 * Copyright (c) 2005 Brian Goetz
 * Released under the Creative Commons Attribution License
 *   (http://creativecommons.org/licenses/by/2.5)
 * Official home: http://www.jcip.net
 */
package solutions.trsoftware.commons.shared.annotations;

import java.lang.annotation.*;

/**
 * The class to which this annotation is applied is not thread-safe. This
 * annotation primarily exists for clarifying the non-thread-safety of a class
 * that might otherwise be assumed to be thread-safe, despite the fact that it
 * is a bad idea to assume a class is thread-safe without good reason.
 *
 * <p style="font-style: italic;">
 *   Note: this annotation is equivalent to {@link javax.annotation.concurrent.NotThreadSafe},
 *   except it is {@linkplain RetentionPolicy#RUNTIME retained at runtime}.
 * </p>
 *
 * @see ThreadSafe
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotThreadSafe {
}
