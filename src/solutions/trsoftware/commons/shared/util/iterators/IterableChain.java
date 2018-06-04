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

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Alex
 * @since 4/30/2018
 */
public class IterableChain<T, A extends Iterable<T>> implements Iterator<T> {

  private final Collection<A> iterables;
  private Iterator<A> cursor;
  private Iterator<T> it;

  public IterableChain(Collection<A> iterables) {
    this.iterables = iterables;
    cursor = iterables.iterator();
    it = getNextIterator();
  }

  private Iterator<T> getNextIterator() {
    while (cursor.hasNext()) {
      Iterator<T> nextIt = cursor.next().iterator();
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
