package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Search result returned by {@link AStar}
 *
 * @param <T> the graph node type
 * @author Alex
 * @see AStarMultiPathResult
 */
public interface AStarSearchResult<T> extends PathSearchResult<T> {
  /**
   * @return the total cost of the shortest path between the {@linkplain #getStart() starting node} and and the nearest goal
   *   or {@link Double#POSITIVE_INFINITY} if didn't find any paths to a goal.
   * @see GraphSpec#cost(Object, Object)
   */
  double getShortestPathCost();

  /**
   * Returns the first (i.e. nearest) goal node discovered by the search.
   *
   * @return the nearest goal encountered by the search, or
   *   {@code null} if the search failed to discover a valid (i.e. finite-cost) path to a goal
   */
  @Nullable
  T getReachedGoal();

  /**
   * Returns the first (of possibly several) shortest paths discovered by the search between the
   * {@linkplain #getStart() starting node} and the nearest goal node.
   *
   * @return immutable list containing the {@linkplain #getStart() starting node} as the first element
   *     and the goal node as the last element (singleton list if the starting node is also a goal node),
   *     or {@code null} if the search didn't find any paths to a goal.
   * @see AStarMultiPathResult#getShortestPaths()
   */
  @Nullable
  List<T> getShortestPath();
}
