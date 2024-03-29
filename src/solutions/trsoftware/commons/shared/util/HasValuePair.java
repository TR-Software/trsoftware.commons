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

package solutions.trsoftware.commons.shared.util;

/**
 * @author Alex, 3/30/2016
 */
public class HasValuePair<L, R> implements OrderedPair<L, R> {

  private final HasValue<L> left;
  private final HasValue<R> right;

  public HasValuePair(HasValue<L> left, HasValue<R> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public L getLeft() {
    return left.getValue();
  }

  @Override
  public R getRight() {
    return right.getValue();
  }
}
