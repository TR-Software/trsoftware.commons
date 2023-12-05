package solutions.trsoftware.commons.shared.util.graphs;

/**
 * Search result of A* or Dijkstra's.
 *
 * @param <T> the graph node type
 * @author Alex
 */
public interface PathSearchResult<T> {
  /**
   * @return the starting node of the path search
   */
  T getStart();

  /**
   * @return the number of graph nodes analyzed by the search
   */
  int getNumNodesExamined();
}
