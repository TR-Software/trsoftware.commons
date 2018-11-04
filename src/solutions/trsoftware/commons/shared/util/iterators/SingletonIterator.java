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

import com.google.common.collect.UnmodifiableIterator;

import java.util.NoSuchElementException;

/**
 * An iterator that contains exactly one element.
 *
 * @author Alex, 4/17/2015
 */
public class SingletonIterator<T> extends UnmodifiableIterator<T> {

  private T next;

  public SingletonIterator(T elt) {
    next = elt;
  }

  // TODO: the current implementation doesn't support null values; should we support them?

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public T next() {
    if (next == null)
      throw new NoSuchElementException();
    T ret = next;
    next = null;
    return ret;
  }
}
