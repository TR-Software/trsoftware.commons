/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.shared.util.function;

import solutions.trsoftware.commons.shared.util.callables.FunctionN;

import java.util.function.*;
import java.util.stream.Stream;

/**
 * Utility methods for working with Java 8 {@link java.util.function functions}
 *
 * @author Alex
 * @since 5/3/2018
 */
public class FunctionalUtils {

  /**
   * Applies a sequence of given predicates to a <i>fixed argument</i>
   * (unlike {@link Stream#allMatch(Predicate)}, which applies a <i>fixed predicate</i> to a sequence of arguments).
   * <p>
   * This is a short-circuiting operation equivalent to:
   * <pre>
   *   predicate1.test(arg) && predicate2.test(arg) && ... && predicateN.test(arg)
   * </pre>
   * @return {@code true} iff all the predicates match the given arg.
   * @see Stream#allMatch(Predicate)
   */
  public static <T> boolean testAll(Iterable<? extends Predicate<T>> predicates, T arg) {
    for (Predicate<T> predicate : predicates) {
      if (!predicate.test(arg))
        return false;
    }
    return true;
  }

  // TODO: create a symmetrical "testAny" method, similar to "testAll"

  /**
   * @param <I> the input type
   * @param <R> the output type
   * @return a function that casts its argument of type {@link I} to the type {@link R}.
   * NOTE: the function might throw a {@link ClassCastException} when applied to an incompatible argument
   */
  @SuppressWarnings("unchecked")
  public static <I, R> Function<I, R> cast() {
    return i -> (R) i;
  }

  /**
   * Creates a <a href="https://en.wikipedia.org/wiki/Partial_application">partial application</a> of the given function
   * by fixing its first parameter to the given value, thus producing a function of smaller arity.
   *
   * @param fun the function to which the given arg will be fixed
   * @param t fixed value for the first arg of the given function
   * @return a new function that evaluates {@code (u) -> fun.apply(t, u)}
   * @see #partial2(BiFunction, Object)
   * @see #partial(BiFunction, Object, Object)
   * @see <a href="https://en.wikipedia.org/wiki/Partial_application">Partial application</a>
   * @see <a href="https://en.wikipedia.org/wiki/Currying#Contrast_with_partial_function_application">Partial application vs. Currying</a>
   * @see <a href="https://www.pgrs.net/2015/04/23/partial-function-application-in-java-8/">blog post that inspired this method</a>
   * @see <a href="https://en.wikipedia.org/wiki/Higher-order_function">Higher-order function</a>
   */
  public static <T, U, R> Function<U, R> partial1(BiFunction<T, U, R> fun, T t) {
    return (u) -> fun.apply(t, u);
  }

  /**
   * Creates a <a href="https://en.wikipedia.org/wiki/Partial_application">partial application</a> of the given function
   * by fixing its 2nd parameter to the given value, thus producing a function of smaller arity.
   *
   * @param fun the function to which the given arg will be fixed
   * @param u fixed value for the first arg of the given function
   * @return a new function that evaluates {@code (t) -> fun.apply(t, u)}
   * @see #partial1(BiFunction, Object)
   * @see #partial(BiFunction, Object, Object)
   * @see <a href="https://en.wikipedia.org/wiki/Partial_application">Partial application</a>
   * @see <a href="https://en.wikipedia.org/wiki/Currying#Contrast_with_partial_function_application">Partial application vs. Currying</a>
   * @see <a href="https://en.wikipedia.org/wiki/Higher-order_function">Higher-order function</a>
   * @see <a href="https://www.pgrs.net/2015/04/23/partial-function-application-in-java-8/">blog post that inspired this method</a>
   */
  public static <T, U, R> Function<T, R> partial2(BiFunction<T, U, R> fun, U u) {
    return (t) -> fun.apply(t, u);
  }

  /**
   * Creates a <a href="https://en.wikipedia.org/wiki/Partial_application">partial application</a> of the given function
   * by fixing both of its parameters to the given values, thus producing a function of smaller arity.
   *
   * @param fun the function to which the given args will be fixed
   * @param t fixed value for the first arg of the given function
   * @param u fixed value for the second arg of the given function
   * @return a new function that evaluates {@code () -> fun.apply(t, u)}
   * @see #partial1(BiFunction, Object)
   * @see #partial2(BiFunction, Object)
   * @see <a href="https://en.wikipedia.org/wiki/Partial_application">Partial application</a>
   * @see <a href="https://en.wikipedia.org/wiki/Currying#Contrast_with_partial_function_application">Partial application vs. Currying</a>
   * @see <a href="https://en.wikipedia.org/wiki/Higher-order_function">Higher-order function</a>
   * @see <a href="https://www.pgrs.net/2015/04/23/partial-function-application-in-java-8/">blog post that inspired this method</a>
   */
  public static <T, U, R> Supplier<R> partial(BiFunction<T, U, R> fun, T t, U u) {
    return () -> fun.apply(t, u);
  }

  /**
   * Creates a <a href="https://en.wikipedia.org/wiki/Partial_application">partial application</a> of the given function
   * by fixing all of its parameters to the given values, thus producing a function of smaller arity.
   *
   * @param fun the function to which the given args will be fixed
   * @param args fixed values for the args of the given function
   * @return a new function that evaluates {@code () -> fun.apply(t, args)}
   * @see #partial1(BiFunction, Object)
   * @see #partial2(BiFunction, Object)
   * @see <a href="https://en.wikipedia.org/wiki/Partial_application">Partial application</a>
   * @see <a href="https://en.wikipedia.org/wiki/Currying#Contrast_with_partial_function_application">Partial application vs. Currying</a>
   * @see <a href="https://en.wikipedia.org/wiki/Higher-order_function">Higher-order function</a>
   * @see <a href="https://www.pgrs.net/2015/04/23/partial-function-application-in-java-8/">blog post that inspired this method</a>
   */
  public static <R> Supplier<R> partial(FunctionN<R> fun, Object... args) {
    return () -> fun.apply(args);
  }

  /**
   * Creates a <a href="https://en.wikipedia.org/wiki/Partial_application">partial application</a> of the given function
   * by fixing its parameter to the given value, thus producing a function of smaller arity.
   *
   * @param fun the function to which the given arg will be fixed
   * @param t fixed value for the first arg of the given function
   * @return a new function that evaluates {@code () -> fun.apply(t)}
   * @see <a href="https://en.wikipedia.org/wiki/Partial_application">Partial application</a>
   * @see <a href="https://en.wikipedia.org/wiki/Currying#Contrast_with_partial_function_application">Partial application vs. Currying</a>
   * @see <a href="https://en.wikipedia.org/wiki/Higher-order_function">Higher-order function</a>
   * @see <a href="https://www.pgrs.net/2015/04/23/partial-function-application-in-java-8/">blog post that inspired this method</a>
   */
  public static <T, R> Supplier<R> partial(Function<T, R> fun, T t) {
    return () -> fun.apply(t);
  }

  /**
   * Creates a <a href="https://en.wikipedia.org/wiki/Partial_application">partial application</a> of the given function
   * by fixing its parameter to the given value, thus producing a function of smaller arity.
   *
   * @param fun the function to which the given arg will be fixed
   * @param t fixed value for the first arg of the given function
   * @return a new function that evaluates {@code () -> fun.accept(t)}
   * @see <a href="https://en.wikipedia.org/wiki/Partial_application">Partial application</a>
   * @see <a href="https://en.wikipedia.org/wiki/Currying#Contrast_with_partial_function_application">Partial application vs. Currying</a>
   * @see <a href="https://en.wikipedia.org/wiki/Higher-order_function">Higher-order function</a>
   * @see <a href="https://www.pgrs.net/2015/04/23/partial-function-application-in-java-8/">blog post that inspired this method</a>
   */
  public static <T> Runnable partial(Consumer<T> fun, T t) {
    return () -> fun.accept(t);
  }

  // TODO: unit test this class

}
