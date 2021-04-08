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
 * An iterator that counts the number of elements generated from the delegate, and optionally limits that number.
 * @author Alex, 4/20/2015
 */
public class CountingIterator<T> extends DelegatedNonMutatingIterator<T> {

  /** The number of successful invocations of {@link #next()} */
  private int count;
  private int limit = Integer.MAX_VALUE;

  public CountingIterator(Iterator<T> delegate) {
    super(delegate);
  }

  public CountingIterator(Iterator<T> delegate, int limit) {
    super(delegate);
    this.limit = limit;
  }

  @Override
  public boolean hasNext() {
    return super.hasNext() && count < limit;
  }

  @Override
  public T next() {
    T ret = super.next();
    count++;
    return ret;
  }

  public int getCount() {
    return count;
  }
}
