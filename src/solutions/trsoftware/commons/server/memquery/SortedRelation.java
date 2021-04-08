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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A materialized sorted relation.
 *
 * @author Alex
 * @since 1/16/2019
 */
public interface SortedRelation extends Relation {
  /**
   * @return the {@link Comparator} used to sort this relation
   */
  Comparator<Row> getComparator();

  /**
   * @return the sort orders for this sorted relation
   */
  List<SortOrder> getSortOrders();

  /**
   * Iterates all the rows matching the given query according to the comparator.
   *
   * @param query a {@link Row} instance that would match the desired results (according to the comparator)
   *
   * @see #getComparator()
   * @see #getSortOrders()
   */
  Iterator<Row> iterMatches(Row query);
}
