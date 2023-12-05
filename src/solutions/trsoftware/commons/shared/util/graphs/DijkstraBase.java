package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Base class for {@link Dijkstra} and {@link DijkstraMultiPath}.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Dijkstra's algorithm (Wikipedia)</a>
 *
 * @param <T> the graph node type
 * @param <R> the search result type
 *
 * @author Alex
 * @since 10/8/2023
 */
abstract class DijkstraBase<T, R extends DijkstraSearchResult<T>> {

  // TODO: maybe extract a PathSearch superclass to share with AStarBase (although it would contain only the graphSpec; can't share the search methods)

  @Nonnull
  protected final GraphSpec<T> graphSpec;

  DijkstraBase(@Nonnull GraphSpec<T> graphSpec) {
    this.graphSpec = requireNonNull(graphSpec, "graphSpec");
  }

  /**
   * Finds the shortest paths from given starting node to every other reachable node in the graph.
   * @return a result object that can be used to obtain the paths and distances discovered by the search
   */
  @Nonnull
  public abstract R search(@Nonnull T start);


  /**
   * Adapts a {@link PathSearcher} instance to the appropriate result type.
   * We're using this intermediate object because {@link PathSearcher} implements both {@link AStarSearchResult}
   * and {@link DijkstraSearchResult}, but we want to return an object that implements only {@link DijkstraSearchResult}
   * (or {@link DijkstraMultiPathResult}) in order for any {@code instanceof} checks to work as expected
   * (i.e. if we returned the {@link PathSearcher} instance without wrapping it in this intermediate container,
   * then {@code result instanceof AStarSearchResult} and {@code result instanceof DijkstraSearchResult}
   * would both be {@code true}, which is undesirable).
   *
   * @param <T> the graph node type
   * @param <R> the search result type
   */
  protected static class SearchResult<T, R extends PathSearcher<T>> implements DijkstraSearchResult<T> {
    protected final R delegate;

    protected SearchResult(R delegate) {
      this.delegate = delegate;
    }

    @Override
    public T getStart() {
      return delegate.getStart();
    }

    @Override
    public int getNumNodesExamined() {
      return delegate.getNumNodesExamined();
    }

    @Override
    public double getShortestPathCost(T target) {
      return delegate.getShortestPathCost(target);
    }

    @Override
    @Nullable
    public List<T> getShortestPath(T target) {
      return delegate.getShortestPath(target);
    }

    @Override
    @Nonnull
    public Set<T> getReachableNodes() {
      return delegate.getReachableNodes();
    }
  }

}
