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

package solutions.trsoftware.commons.server.memquery;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alex, 4/17/2015
 */
public class SortedArrayListRelation extends ArrayListRelation implements SortedRelation {

  /**
   * The sort orders for this sorted relation
   */
  private final List<SortOrder> sortOrders;

  /**
   * The {@link Comparator} used to sort this relation (according to {@link #sortOrders})
   */
  private final Comparator<Row> comparator;

  public SortedArrayListRelation(RelationSchema schema, Iterator<Row> rowIter, List<SortOrder> sortOrders) {
    super(schema, rowIter);
    comparator = MemQuery.makeComparator(sortOrders, schema);
    rows.sort(comparator);
    this.sortOrders = ImmutableList.copyOf(sortOrders);
  }

  SortedArrayListRelation(Relation relation, List<SortOrder> sortOrders) {
    this(relation.getSchema(), relation.iterator(), sortOrders);
  }

  /**
   * @return the {@link Comparator} used to sort this relation
   */
  @Override
  public Comparator<Row> getComparator() {
    return comparator;
  }

  /**
   * @return the sort orders for this sorted relation
   */
  @Override
  public List<SortOrder> getSortOrders() {
    return sortOrders;
  }

  /**
   * Iterates all the rows matching the given row according to the comparator.
   *
   * @param query a row containing values of the columns used by our comparator
   * @see #getComparator()
   * @see #getSortOrders()
   */
  @Override
  public Iterator<Row> iterMatches(Row query) {
    int foundIdx = Collections.binarySearch(rows, query, comparator);
    if (foundIdx < 0)
      return Collections.emptyIterator();
    else {
      // Collections.binarySearch returns an arbitrary matching element;
      // we do a linear scan on the list in both directions to find the actual endpoints
      // NOTE: if the number of expected matches is expected to be large, we could do a recursive binary search instead (see see com.google.common.collect.SortedLists.KeyPresentBehavior.FIRST_PRESENT)
      int lower = foundIdx;
      int upper = foundIdx;
      for (int i = foundIdx - 1; i >= 0 && comparator.compare(rows.get(i), query) == 0; i--) {
        lower = i;
      }
      for (int i = foundIdx + 1; i < rows.size() && comparator.compare(rows.get(i), query) == 0; i++) {
        upper = i;
      }
      return rows.subList(lower, upper+1).iterator();
    }
  }

}
