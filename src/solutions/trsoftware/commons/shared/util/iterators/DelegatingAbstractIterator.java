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

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

/**
 * @author Alex, 10/15/2016
 */
public abstract class DelegatingAbstractIterator<T> extends AbstractIterator<T> {

  protected final Iterator<T> delegate;

  public DelegatingAbstractIterator(Iterator<T> delegate) {
    this.delegate = delegate;
  }
}
