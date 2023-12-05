package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Search result returned by {@link Dijkstra}
 * 
 * @param <T> the graph node type
 * @author Alex
 * @see DijkstraMultiPathResult
 */
public interface DijkstraSearchResult<T> extends PathSearchResult<T> {

  /**
   * @return the total cost of the shortest path between the {@linkplain #getStart() starting node} and {@code target},
   *   or {@link Double#POSITIVE_INFINITY} if there is no finite-cost path between the two nodes.
   * @see GraphSpec#cost(Object, Object)
   */
  double getShortestPathCost(T target);


  /**
   * Returns the shortest path discovered by the search between the {@linkplain #getStart() starting node}
   * and the given {@code target} node.
   *
   * @return immutable list containing the {@linkplain #getStart() starting node} as the first element
   *     and {@code target} as the last element (singleton list if {@code target} is the starting node),
   *     or {@code null} if the search didn't find any paths to the target.
   * @see DijkstraMultiPathResult#getShortestPaths(Object)
   */
  @Nullable
  List<T> getShortestPath(T target);


  /**
   * Returns the set of all graph nodes reachable from the {@linkplain #getStart() starting node}.
   * In other words, this is the set of all nodes for which {@link #getShortestPathCost(Object)} is finite
   * and {@link #getShortestPath(Object)} is non-null.
   *
   * @return a (possibly empty) set of all reachable nodes (excluding the {@linkplain #getStart() starting node} itself)
   */
  @Nonnull
  Set<T> getReachableNodes();

}
