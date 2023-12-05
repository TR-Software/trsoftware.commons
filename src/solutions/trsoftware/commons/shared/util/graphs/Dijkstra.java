package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Uses Dijkstra's algorithm to find the shortest path from a starting node to all other nodes in a weighted graph.
 * <p>
 * For faster results, the search can be limited to a subset of target nodes by using {@link AStar#search(Object, Set)},
 * taking advantage of the fact that Dijkstra's algorithm can be expressed as a special case of A* with
 * {@linkplain GraphSpec#heuristic(Object, Object) heuristic} ℎ(N) = 0 for every node.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Dijkstra's algorithm (Wikipedia)</a>
 * @see DijkstraMultiPath
 *
 * @param <T> the graph node type
 * @author Alex
 * @since 11/19/2023
 */
public class Dijkstra<T> extends DijkstraBase<T, DijkstraSearchResult<T>> {

  public Dijkstra(@Nonnull GraphSpec<T> graphSpec) {
    super(graphSpec);
  }

  @Nonnull
  @Override
  public DijkstraSearchResult<T> search(@Nonnull T start) {
    return new SearchResult<>(
        new SinglePathSearcher<T>(graphSpec, start, null)
    );
  }

}