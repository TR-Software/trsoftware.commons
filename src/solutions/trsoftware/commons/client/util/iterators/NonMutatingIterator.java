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

package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;

/**
 * Base class for iterators that don't support the {@link #remove()} operation.
 * 
 * Jan 15, 2010
 * @author Alex
 */
public abstract class NonMutatingIterator<T> implements Iterator<T> {

  @Override
  public final void remove() {
    throwRemoveNotSupported(getClass());
  }

  public static void throwRemoveNotSupported(Class cls) {
    throw new UnsupportedOperationException(cls.getName() + " does not support Iterator.remove");
  }
}
