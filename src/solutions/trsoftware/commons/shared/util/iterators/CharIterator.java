/*
 * Copyright 2023 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.iterators;

import solutions.trsoftware.commons.shared.util.function.CharConsumer;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

/**
 * An {@link Iterator} specialized for primitive {@code char} values.
 * Does not support {@linkplain #remove() removal}.
 *
 * @see PrimitiveIterator.OfInt
 * @see java.text.CharacterIterator
 *
 * @author Alex
 * @since 4/13/2023
 */
public interface CharIterator extends PrimitiveIterator<Character, CharConsumer> {

  /**
   * Returns the next {@code char} element in the iteration.
   *
   * @return the next {@code char} element in the iteration
   * @throws NoSuchElementException if the iteration has no more elements
   */
  char nextChar();

  /**
   * {@inheritDoc}
   *
   * @implSpec The default implementation boxes the result of calling
   *     {@link #nextChar()}, and returns that boxed result.
   */
  @Override
  default Character next() {
    return nextChar();
  }

  /**
   * Performs the given action for each remaining element until all elements
   * have been processed or the action throws an exception.  Actions are
   * performed in the order of iteration, if that order is specified.
   * Exceptions thrown by the action are relayed to the caller.
   *
   * @param action The action to be performed for each element
   * @throws NullPointerException if the specified action is null
   * @implSpec <p>The default implementation behaves as if:
   *     <pre>{@code
   *         while (hasNext())
   *             action.accept(nextChar());
   *     }</pre>
   */
  default void forEachRemaining(CharConsumer action) {
    requireNonNull(action);
    while (hasNext())
      action.accept(nextChar());
  }

  /**
   * {@inheritDoc}
   *
   * @implSpec If the action is an instance of {@code CharConsumer} then it is cast
   *     to {@code CharConsumer} and passed to {@link #forEachRemaining};
   *     otherwise the action is adapted to an instance of
   *     {@code CharConsumer}, by boxing the argument of {@code CharConsumer},
   *     and then passed to {@link #forEachRemaining}.
   */
  @Override
  default void forEachRemaining(Consumer<? super Character> action) {
    if (action instanceof CharConsumer) {
      /*
       * Note: although CharConsumer doesn't extend Consumer<? super Character>, it's possible that the action
       * is an instance of a class that extends both interfaces.
       * This code was borrowed from PrimitiveIterator.OfInt.forEachRemaining(Consumer<? super java.lang.Integer>),
       * and since they chose to do it this way there, we just follow that example there.
       */
      forEachRemaining((CharConsumer)action);
    }
    else {
      // The method reference action::accept is never null
      requireNonNull(action);
      forEachRemaining((CharConsumer)action::accept);
    }
  }

  /**
   * Adapts a primitive {@code int} iterator to the {@link CharIterator} protocol, by casting each value returned
   * by {@link OfInt#nextInt()} to {@code char}.
   *
   * @param intIterator primitive {@code int} iterator to wrap
   */
  static CharIterator fromIntIterator(OfInt intIterator) {
    return new IntAdapter(intIterator);
  }

  /**
   * Constructs a primitive {@code char} iterator that returns the elements from the given {@link IntStream} as
   * {@code char}s.
   */
  static CharIterator fromIntStream(IntStream intStream) {
    return fromIntIterator(intStream.iterator());
  }

  /**
   * Adapts a primitive {@code int} iterator to the {@link CharIterator} protocol.
   */
  class IntAdapter implements CharIterator {
    private final PrimitiveIterator.OfInt delegate;

    public IntAdapter(@Nonnull OfInt delegate) {
      this.delegate = requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public char nextChar() {
      return (char)delegate.nextInt();
    }
  }

}

