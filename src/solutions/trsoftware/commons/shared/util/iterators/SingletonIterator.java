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

import java.util.NoSuchElementException;

/**
 * An iterator that contains exactly one element.
 * <p>
 * Same implementation as {@link java.util.Collections#singletonIterator(Object)}
 *
 * @author Alex, 4/17/2015
 */
public class SingletonIterator<T> extends UnmodifiableIterator<T> {

  private T elt;
  private boolean hasNext = true;

  public SingletonIterator(T elt) {
    this.elt = elt;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public T next() {
    if (hasNext) {
      hasNext = false;
      return elt;
    }
    throw new NoSuchElementException();
  }
}
