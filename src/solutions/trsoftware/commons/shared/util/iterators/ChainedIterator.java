/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.util.iterators;

import com.google.common.collect.Iterators;

import java.util.Collection;
import java.util.Iterator;

/**
 * Combines multiple iterators into a single "chain", similar to
 * <a href="https://docs.python.org/2/library/itertools.html#itertools.chain">itertools.chain</a> in Python.
 * <p>
 * This class has the same functionality as {@link Iterators#concat(Iterator)} in Guava, but provides constructors
 * supporting a wider range of sources for the chain.
 *
 * @see Iterators#concat(Iterator)
 * @author Alex
 * @since 4/30/2018
 */
public class ChainedIterator<T> implements Iterator<T> {

  /** Provides the next {@link Iterator} in the chain */
  private Iterator<Iterator<T>> chain;
  /**
   * The current iterator in the chain
   */
  private Iterator<T> it;

  public ChainedIterator(Iterator<Iterator<T>> chain) {
    this.chain = chain;
    it = getNextIterator();
  }

  public ChainedIterator(Collection<Iterator<T>> iterators) {
    this(iterators.iterator());
  }

  public static <T> ChainedIterator<T> fromIterables(Collection<? extends Iterable<T>> iterables) {
    return fromIterables(iterables.iterator());
  }

  @SuppressWarnings("unchecked")
  public static <T> ChainedIterator<T> fromIterables(Iterator<? extends Iterable<T>> iterables) {
    return new ChainedIterator<T>(new TransformingIterator<Iterable<T>, Iterator<T>>((Iterator<Iterable<T>>)iterables) {
      @Override
      protected Iterator<T> transform(Iterable<T> input) {
        return input.iterator();
      }
    });
  }

  /**
   * @return the next non-empty {@link Iterator} in the chain, or {@code null} if the chain is finished.
   */
  private Iterator<T> getNextIterator() {
    while (chain.hasNext()) {
      Iterator<T> nextIt = chain.next();
      if (nextIt.hasNext())
        return nextIt;
      // skip empty iterators
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return it != null && it.hasNext();
  }

  @Override
  public T next() {
    T result = it.next();
    if (!it.hasNext())
      it = getNextIterator();
    return result;
  }

  @Override
  public void remove() {
    it.remove();
  }
}
