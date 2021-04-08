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

import java.util.Iterator;

/**
 * An iterator that delegates all operations to the underlying iterator.  Useful for intercepting and overriding
 * methods of another iterator.
 *
 * @author Alex, Jan 15, 2010
 * @see com.google.common.collect.ForwardingIterator
 */
public class DelegatedIterator<T> implements Iterator<T> {
  protected Iterator<T> delegate;

  public DelegatedIterator(Iterator<T> delegate) {
    this.delegate = delegate;
  }

  public boolean hasNext() {
    return delegate.hasNext();
  }

  public T next() {
    return delegate.next();
  }

  @Override
  public void remove() {
    delegate.remove();
  }
}
