package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Search result returned by {@link DijkstraMultiPath}
 *
 * @param <T> the graph node type
 * @author Alex
 * @since 11/13/2023
 */
public interface DijkstraMultiPathResult<T> extends DijkstraSearchResult<T> {

  /**
   * Returns the set of all possible shortest paths (of equal {@linkplain #getShortestPathCost(Object) cost})
   * discovered by the search between the {@linkplain #getStart() starting node} and {@code target}.
   *
   * @return immutable set of all shortest paths from the {@linkplain #getStart() starting node} to {@code target},
   *     or {@code null} if didn't find any finite-cost paths.
   *     <p>Note: if {@code target} is the starting node, will return a singleton set containing a singleton list
   *     (with the starting/target node as the only element).
   * @see #getShortestPath(Object)
   */
  @Nullable
  Set<List<T>> getShortestPaths(T target);

}
