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

import java.util.Collections;
import java.util.NoSuchElementException;

/**
 * An iterator that doesn't return any elements.
 *
 * @deprecated Java 1.7+ added {@link Collections#emptyIterator()}, which does the same thing.
 *
 * @author Alex, 4/17/2015
 * @see Collections#emptyIterator()
 */
public class NullIterator<T> extends UnmodifiableIterator<T> {

  private static NullIterator instance;

  @SuppressWarnings("unchecked")
  public static <T> NullIterator<T> getInstance() {
    if (instance == null)
      instance = new NullIterator();
    return instance;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public T next() {
    throw new NoSuchElementException();
  }
}
