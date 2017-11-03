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

import solutions.trsoftware.commons.client.util.callables.Function1;

import java.util.Iterator;

/**
 * Uses the encapsulated function to do the transform.
 * @author Alex, 1/12/14
 */
public class FunctionalTransformingIterator<I, O> extends TransformingIterator<I, O> {

  private final Function1<I, O> transformer;

  public FunctionalTransformingIterator(Iterator<I> delegate, Function1<I, O> transformer) {
    super(delegate);
    this.transformer = transformer;
  }

  @Override
  public O transform(I input) {
    return transformer.call(input);
  }
}
