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

import java.util.Iterator;

/**
 * Filters a delegated iterator by returning only those elements for which the {@link #filter(Object)} method is true.
 *
 * @author Alex, 1/9/14
 */
public abstract class FilteringIterator<T> extends DelegatingAbstractIterator<T> {

  private boolean includeNullElements;

  public FilteringIterator(boolean includeNullElements, Iterator<T> delegate) {
    super(delegate);
    this.includeNullElements = includeNullElements;
  }

  public FilteringIterator(Iterator<T> delegate) {
    this(false, delegate);
  }

  @Override
  protected T computeNext() {
    while (delegate.hasNext()) {
      T elt = delegate.next();
      if (elt == null && !includeNullElements)
        continue;
      if (filter(elt)) {
        return elt;
      }
    }
    return endOfData();
  }

  /**
   * @return true iff the given element should be returned.
   */
  protected abstract boolean filter(T elt);

}
