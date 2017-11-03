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

package solutions.trsoftware.commons.shared.util;

/**
 * An immutable {@link OrderedPair}.
 *
 * @author Alex, 3/30/2016
 */
public class SimplePair<L, R> implements OrderedPair<L, R> {

  private final L left;
  private final R right;

  public SimplePair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public L getLeft() {
    return left;
  }

  @Override
  public R getRight() {
    return right;
  }

  @Override
  public String toString() {
    return new StringBuilder(128).append('(').append(left).append(", ").append(right).append(')').toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SimplePair that = (SimplePair)o;

    if (left != null ? !left.equals(that.left) : that.left != null) return false;
    if (right != null ? !right.equals(that.right) : that.right != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (right != null ? right.hashCode() : 0);
    return result;
  }
}
