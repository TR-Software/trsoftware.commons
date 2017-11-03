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

package solutions.trsoftware.commons.server.memquery.util;

import java.util.Comparator;
import java.util.List;

/**
 * Encapsulates a list of internal Comparators to which it delegates, using each subsequent Comparator to as a tie-breaker
 * for its predecessors.
 *
 * @author Alex, 1/9/14
 */
public class CompositeComparator<T> implements Comparator<T> {

  private final List<Comparator<T>> comparators;

  public CompositeComparator(List<Comparator<T>> delegates) {
    this.comparators = delegates;
  }

  @Override
  public int compare(T o1, T o2) {
    for (Comparator<T> next : comparators) {
      int result = next.compare(o1, o2);
      if (result != 0)
        return result;
    }
    return 0;
  }
}
