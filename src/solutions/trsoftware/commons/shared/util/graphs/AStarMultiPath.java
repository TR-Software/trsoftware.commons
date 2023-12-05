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
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Uses the A* algorithm to find all possible shortest paths between a starting node and the nearest goal node
 * in a weighted graph.
 * <p>
 * This class is the {@linkplain AStarMultiPathResult multi-path} version of {@link AStar}.  It keeps searching
 * until it discovers {@linkplain AStarMultiPathResult#getShortestPaths() all possible shortest paths}
 * to the nearest goal node (of which there could also be more than one), and is therefore
 * slower than the simple version of the algorithm because it requires more nodes to be examined
 * (and uses more memory to keep track of all the different paths).
 *
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm (Wikipedia)</a>
 * @see AStarMultiPathResult
 * @see AStar
 *
 * @param <T> the graph node type
 *
 * @author Alex
 * @since 10/8/2023
 */
public class AStarMultiPath<T> extends AStarBase<T, AStarMultiPathResult<T>> {

  /**
   * @param graphSpec should implement {@link GraphSpec#heuristic(Object, Object)}
   */
  public AStarMultiPath(@Nonnull GraphSpec<T> graphSpec) {
    super(graphSpec);
  }

  /**
   * Finds all shortest paths from given starting node to the first-encountered node in the given set of goal nodes.
   * @return a result object that can be used to obtain the paths discovered by the search
   */
  @Nonnull
  public AStarMultiPathResult<T> search(@Nonnull T start, @Nonnull Set<T> goals) {
    return new SearchResult<>(
        new MultiPathSearcher<>(graphSpec, start, requireNonNull(goals, "goals"))
    );
  }

  protected static class SearchResult<T> extends AStarBase.SearchResult<T, MultiPathSearcher<T>>
      implements AStarMultiPathResult<T> {

    protected SearchResult(MultiPathSearcher<T> delegate) {
      super(delegate);
    }

    @Nullable
    public Set<T> getReachedGoals() {
      return delegate.getReachedGoals();
    }

    @Nullable
    public Set<List<T>> getShortestPaths() {
      return delegate.getShortestPaths();
    }
  }

}
