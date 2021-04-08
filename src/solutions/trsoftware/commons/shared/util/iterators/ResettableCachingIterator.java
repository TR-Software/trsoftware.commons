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

package solutions.trsoftware.commons.shared.util.iterators;

import com.google.common.collect.UnmodifiableIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Alex, 10/15/2016
 */
public class ResettableCachingIterator<T> extends UnmodifiableIterator<T> implements ResettableIterator<T> {

  /** The underlying data stream */
  protected final Iterator<T> delegate;

  /** Stores elements that have already been returned from {@link #delegate} by the {@link #next()} method */
  private ArrayList<T> elementCache = new ArrayList<T>();

  /**
   * The next index in {@link #elementCache} to be returned by the {@link #next()} ()} method.
   * The value of -1 denotes that the {@link #next()} method should be returning elements
   * directly from {@link #delegate} and caching them.
   */
  protected int i = -1;

  public ResettableCachingIterator(Iterator<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void reset() {
    i = 0;  // start returning elements from the cache
  }

  @Override
  public boolean hasNext() {
    return (i >= 0 && i < elementCache.size()) || delegate.hasNext();
  }

  @Override
  public T next() {
    if (i < 0) {
      // the iterator hasn't been reset yet, so we're returning elements directly from the delegate
      if (delegate.hasNext()) {
        T next = delegate.next();
        elementCache.add(next);
        return next;
      }
      else
        throw new NoSuchElementException();
    }
    else if (i < elementCache.size()) {
      // the iterator has been reset at least once, so we're returning elements from the cache
      return elementCache.get(i++);
    }
    else {
      // we've exhausted the cache, so reset i to revert to returning elements from the delegate
      i = -1;
      return next();
    }
  }

}
