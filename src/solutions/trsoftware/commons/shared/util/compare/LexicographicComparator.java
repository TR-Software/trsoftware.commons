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

package solutions.trsoftware.commons.shared.util.compare;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;

/**
 * Performs a pairwise comparison on iterable elements based on their order of significance
 * (the first element is considered most-significant), equivalent to a lexicographical string comparison.
 * <p>
 * <b>Example</b>: (using {@code new LexicographicComparator<Integer>(Comparator.naturalOrder())}):
 * <ul>
 *   <li>{@code [1, 2, 3] == [1, 2, 3]}</li>
 *   <li>{@code [1, 2, 3] < [1, 2, 3, 4]}</li>
 *   <li>{@code [1, 2, 3] < [1, 2, 4]}</li>
 *   <li>{@code [1, 2, 3] < [2]}</li>
 *   <li>{@code [1, 2, 3] > [1, 2]}</li>
 *   <li>{@code [1, 2, 3] > []}</li>
 *   <li>{@code [1, 2, 3] > null}</li>
 * </ul>
 * This comparator does not allow {@code null} arguments, but can be wrapped with
 * {@link Comparator#nullsFirst} or {@link Comparator#nullsLast} if needed.
 *
 * @param <E> the element type of the sequences being compared
 * @see CompositeComparator
 * @see Comparator#nullsFirst(Comparator)
 * @see Comparator#nullsFirst(Comparator)
 *
 * @author Alex
 * @since 8/9/2018
 */
public class LexicographicComparator<E> implements Comparator<Iterable<E>> {

  private final Comparator<E> elementComparator;

  public LexicographicComparator(@Nonnull Comparator<E> elementComparator) {
    this.elementComparator = requireNonNull(elementComparator, "elementComparator");
  }


  @Override
  public int compare(Iterable<E> seq1, Iterable<E> seq2) {
    // 1) check for null/empty lists (callers can wrap with Comparator.nullsFirst/nullsLast to allow null args)
    requireNonNull(seq1, "seq1");
    requireNonNull(seq2, "seq2");

    // 2) compare element-by-element in order of significance
    Iterator<E> iter1 = seq1.iterator();
    Iterator<E> iter2 = seq2.iterator();
    boolean hasNext1, hasNext2;
    int cmp;
    do {
      hasNext1 = iter1.hasNext();
      hasNext2 = iter2.hasNext();

      if (!hasNext1 && !hasNext2)
        return 0;
      else if (!hasNext1)
        return -1;
      else if (!hasNext2)
        return 1;

      E e1 = iter1.next();
      E e2 = iter2.next();
      cmp = elementComparator.compare(e1, e2);
    }
    while (cmp == 0);
    return cmp;
  }

}
