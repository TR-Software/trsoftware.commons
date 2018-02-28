package solutions.trsoftware.commons.server.util.function;

import java.util.function.BiConsumer;

/**
 * Same as {@link BiConsumer}, but allows the function to throw a declared exception.
 *
 * @param <A> the type of the first argument to the operation
 * @param <B> the type of the second argument to the operation
 *
 * @author Alex
 * @since 2/27/2018
 */
@FunctionalInterface
public interface ThrowingBiConsumer<A, B, E extends Exception>  {

  void accept(A a, B b) throws E;
}
