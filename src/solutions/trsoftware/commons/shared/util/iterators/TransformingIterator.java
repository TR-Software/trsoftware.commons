/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
 * Returns an output element after applying a transformation on the corresponding input element.
 * @author Alex, 1/12/14
 */
public abstract class TransformingIterator<I, O> implements Iterator<O> {

  protected Iterator<I> delegate;

  public TransformingIterator(Iterator<I> delegate) {
    this.delegate = delegate;
  }

  /** Transforms an input element into the corresponding output element */
  protected abstract O transform(I input);

  public boolean hasNext() {
    return delegate.hasNext();
  }

  public O next() {
    return transform(delegate.next());
  }

  @Override
  public void remove() {
    delegate.remove();
  }
}
