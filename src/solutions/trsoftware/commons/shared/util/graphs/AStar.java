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

package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nonnull;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Uses the A* algorithm to find the shortest path between a starting node and the nearest goal node in a weighted graph.
 * <p>
 * An instance of this class requires a {@link GraphSpec} that implements an admissible
 * {@linkplain GraphSpec#heuristic(Object, Object) heuristic function},
 * and can be safely shared by multiple threads (as long as the {@link GraphSpec} is thread-safe).
 *
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm (Wikipedia)</a>
 * @see AStarMultiPath
 *
 * @param <T> the graph node type
 *
 * @author Alex
 * @since 10/8/2023
 */
public class AStar<T> extends AStarBase<T, AStarSearchResult<T>> {

  /**
   * @param graphSpec should implement {@link GraphSpec#heuristic(Object, Object)}
   */
  public AStar(@Nonnull GraphSpec<T> graphSpec) {
    super(graphSpec);
  }

  /**
   * Finds all shortest paths from given starting node to the first-encountered node in the given set of goal nodes.
   * @return a result object that can be used to obtain the paths discovered by the search
   */
  @Override
  @Nonnull
  public AStarSearchResult<T> search(@Nonnull T start, @Nonnull Set<T> goals) {
    return new SearchResult<>(
        new SinglePathSearcher<>(graphSpec, start, requireNonNull(goals, "goals"))
    );
  }

}
