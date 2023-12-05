package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Uses Dijkstra's algorithm to find all shortest paths from a starting node to all other nodes
 * in a weighted graph.
 * <p>
 * This class is the {@linkplain DijkstraMultiPathResult multi-path} version of {@link Dijkstra}.  It keeps searching
 * until it discovers {@linkplain DijkstraMultiPathResult#getShortestPaths(Object) all possible shortest paths}
 * to every node, and is therefore  slower than the simple version of the algorithm.
 * <p>
 * For quicker results, the search can be limited to a subset of target nodes by using {@link AStarMultiPath#search(Object, Set)},
 * taking advantage of the fact that Dijkstra's algorithm can be expressed as a special case of A* with
 * {@linkplain GraphSpec#heuristic(Object, Object) heuristic} ℎ(N) = 0 for every node.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Dijkstra's algorithm (Wikipedia)</a>
 * @see DijkstraMultiPathResult#getShortestPaths(Object)
 *
 * @param <T> the graph node type
 * @author Alex
 * @since 11/19/2023
 */
public class DijkstraMultiPath<T> extends DijkstraBase<T, DijkstraMultiPathResult<T>> {

  public DijkstraMultiPath(@Nonnull GraphSpec<T> graphSpec) {
    super(graphSpec);
  }

  @Nonnull
  @Override
  public DijkstraMultiPathResult<T> search(@Nonnull T start) {
    return new SearchResult<>(
        new MultiPathSearcher<>(graphSpec, start, null)
    );
  }


  protected static class SearchResult<T> extends DijkstraBase.SearchResult<T, MultiPathSearcher<T>>
      implements DijkstraMultiPathResult<T> {

    protected SearchResult(MultiPathSearcher<T> delegate) {
      super(delegate);
    }

    @Nullable
    public Set<List<T>> getShortestPaths(T target) {
      return delegate.getShortestPaths(target);
    }
  }
}
