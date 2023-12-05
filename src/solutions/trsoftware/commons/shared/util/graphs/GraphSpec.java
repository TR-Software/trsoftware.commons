package solutions.trsoftware.commons.shared.util.graphs;

import java.util.Iterator;
import java.util.List;

/**
 * Defines a weighted graph that can be searched using {@link AStar} or {@link Dijkstra}, with the following parameters:
 * <ol>
 *   <li>
 *     The weighted graph structure to be searched (required):
 *     <ul>
 *       <li>{@link #neighbors(Object)}: specifies the graph edges</li>
 *       <li>{@link #cost(Object, Object)}: specifies the edge weights</li>
 *     </ul>
 *   </li>
 *   <li>
 *     A {@linkplain #heuristic} function for searching the graph
 *     (required for {@linkplain AStar A*}, but optional for {@linkplain Dijkstra Dijkstra's})
 *   </li>
 * </ol>
 *
 * @param <T> the graph node type
 */
public interface GraphSpec<T> {
  /**
   * @return the nodes adjacent to the given node in the graph.
   */
  Iterable<T> neighbors(T node);

  /**
   * Returns the cost of going from node {@code a} to its direct neighbor {@code b}.
   * In other words, this is the length (or weight) of the edge {@code (a, b)}.
   *
   * @param a a node in the graph
   * @param b a neighbor of {@code a}
   * @return the cost of traversing the edge {@code (a, b)}
   */
  double cost(T a, T b);

  /**
   * Estimates the cost of going from {@code a} to {@code b} (where {@code b} is typically the goal node).
   * Note: this value is only relevant for the A* algorithm; for Dijkstra's algorithm the heuristic is always 0.
   * <p>
   * The heuristic function should be <em>admissible</em>, meaning that it never overestimates the actual cost
   * to reach the goal.  This property guarantees that the search will find an optimal (least-cost) path from
   * start to goal, but it does not guarantee finding that path in the least number of steps.
   * <p>
   * Ideally, the heuristic should also be <em>consistent</em> (i.e. "monotone") meaning that its estimate is always
   * less than or equal to the estimated distance from any neighboring vertex to the goal, plus the cost of reaching
   * that neighbor.
   * Formally, for every node N and each successor P of N, the estimated cost of reaching the goal from N is
   * no greater than the step cost of getting to P plus the estimated cost of reaching the goal from P. That is:
   * <ol>
   *   <li>ℎ(N) &le; c(N,P) + ℎ(P) and</li>
   *   <li>ℎ(G) = 0</li>
   * </ol>
   * where
   * <ul>
   *   <li>ℎ is the consistent heuristic function</li>
   *   <li>N is any node in the graph</li>
   *   <li>P is any descendant of N</li>
   *   <li>G is any goal node</li>
   *   <li>c(N,P) is the cost of reaching node P from N</li>
   * </ul>
   * With a <em>consistent</em> heuristic, A* is guaranteed to find an optimal path without processing any node
   * more than once.
   * When the heuristic is admissible but not consistent, it is possible for a node to be expanded by A*
   * many times, an exponential number of times in the worst case.
   * <p>
   * Note: the default implementation of this method returns 0 (which is both admissible and consistent).
   * This is the typical value for Dijkstra's algorithm, but for A* search, this method should be overridden with
   * a more reasonable heuristic.
   * </p>
   *
   * @param a a node in the graph
   * @param b any other node (typically the goal node) in the same graph
   * @return estimate of the cost of going from a to b (assuming this is a weighted graph)
   * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm#Properties">Properties of A* Search</a>
   * @see <a href="https://en.wikipedia.org/wiki/Admissible_heuristic">Admissible heuristic</a>
   * @see <a href="https://en.wikipedia.org/wiki/Consistent_heuristic">Consistent heuristic</a>
   */
  default double heuristic(T a, T b) {
    return 0;
  }

  /**
   * Computes the total cost of the given path using the {@link #cost} function.
   * @return the total cost of the given path, or 0 if the given list contains less than 2 elements.
   */
  default double getPathCost(List<T> path) {
    double cost = 0;
    if (path.size() > 1) {
      Iterator<T> it = path.iterator();
      T prev = it.next();
      while (it.hasNext()) {
        T next = it.next();
        cost += cost(prev, next);
        prev = next;
      }
    }
    return cost;
  }

}
