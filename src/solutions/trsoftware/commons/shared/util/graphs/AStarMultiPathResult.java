package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Search result returned by {@link AStarMultiPath}
 *
 * @param <T> the graph node type
 * @author Alex
 */
public interface AStarMultiPathResult<T> extends AStarSearchResult<T> {

  /**
   * Returns the set of goal nodes discovered by the search, if there are multiple goals having the same
   * {@linkplain #getShortestPathCost() distance} from the {@linkplain #getStart() starting node}.
   *
   * @return the set of goal nodes discovered by the search, or
   *   {@code null} if the search failed to discover a valid (i.e. finite-cost) path to a goal
   */
  @Nullable
  default Set<T> getReachedGoals() {
    T reachedGoal = getReachedGoal();
    return reachedGoal == null ? null : Collections.singleton(reachedGoal);
  }

  /**
   * Returns the set of all possible shortest paths (of equal {@linkplain #getShortestPathCost() cost})
   * discovered by the search between the {@linkplain #getStart() starting node}
   * and the {@linkplain #getReachedGoals() reached goals}.
   *
   * @return immutable set of all shortest paths from the {@linkplain #getStart() starting node} to every
   *   {@linkplain #getReachedGoals() reached goal}, or {@code null} if none of the goals are reachable
   * @see #getShortestPath()
   */
  @Nullable
  default Set<List<T>> getShortestPaths() {  // protected b/c should only be exposed by Dijkstra
    List<T> path = getShortestPath();
    return path == null ? null : Collections.singleton(path);
  }
}
