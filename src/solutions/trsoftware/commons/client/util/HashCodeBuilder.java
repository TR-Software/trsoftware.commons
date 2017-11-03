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

package solutions.trsoftware.commons.client.util;

/**
 * Constructs a hash code from a composite of hash codes of a collection of objects.
 *
 * Example: new HashCodeBuilder().update(a, b, c).hashCode();
 *
 * Oct 17, 2012
 *
 * @author Alex
 */
public class HashCodeBuilder {

  private int result = 0;

  public HashCodeBuilder update(Object a) {
    // this algorithm is based on IntelliJ's auto-generated hash codes
    result = 31 * result + (a != null ? a.hashCode() : 0);
    return this;
  }

  public HashCodeBuilder update(Object... inputs) {
    for (Object x : inputs) {
      update(x);
    }
    return this;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HashCodeBuilder that = (HashCodeBuilder)o;

    if (result != that.result) return false;

    return true;
  }

  public int hashCode() {
    return result;
  }
}
