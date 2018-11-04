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

import com.google.common.base.Predicate;

import java.util.Iterator;

/**
 * Implements {@link FilteringIterator#filter(Object)} by invoking the given predicate.
 *
 * @author Alex, 1/9/14
 */
public class PredicatedIterator<T> extends FilteringIterator<T> {

  /** The {@link #next} method will return only those elements from the {@link #delegate} iterator that satisfy this predicate */
  private final Predicate<T> predicate;

  public PredicatedIterator(Iterator<T> delegate, Predicate<T> predicate) {
    super(delegate);
    this.predicate = predicate;
  }

  public PredicatedIterator(boolean includeNullElements, Iterator<T> delegate, Predicate<T> predicate) {
    super(includeNullElements, delegate);
    this.predicate = predicate;
  }

  @Override
  protected boolean filter(T elt) {
    return predicate.apply(elt);
  }

}
