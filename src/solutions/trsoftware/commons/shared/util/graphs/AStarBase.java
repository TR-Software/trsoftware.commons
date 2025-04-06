package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Base class for {@link AStar} and {@link AStarMultiPath}.
 *
 * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm">A* search algorithm (Wikipedia)</a>
 *
 * @param <T> the graph node type
 * @param <R> the search result type
 *
 * @author Alex
 * @since 10/8/2023
 */
abstract class AStarBase<T, R extends AStarSearchResult<T>> {

  // TODO: maybe extract a PathSearch superclass to share with DijkstraBase (although it would contain only the graphSpec; can't share the search methods)

  @Nonnull
  protected final GraphSpec<T> graphSpec;

  AStarBase(@Nonnull GraphSpec<T> graphSpec) {
    this.graphSpec = requireNonNull(graphSpec, "graphSpec");
  }

  /**
   * Finds the shortest path from given starting node to the first-encountered node among the given set of goal nodes.
   * @return a result object that can be used to obtain the paths discovered by the search
   */
  @Nonnull
  public abstract R search(@Nonnull T start, @Nonnull Set<T> goals);

  /**
   * Finds the shortest path from given starting node to the given goal node.
   * @return a result object that can be used to obtain the paths discovered by the search
   */
  @Nonnull
  public R search(@Nonnull T start, @Nonnull T goal) {
    return search(start, Collections.singleton(requireNonNull(goal, "goal")));
  }


  /**
   * Adapts a {@link PathSearcher} instance to the appropriate result type.
   * We're using this intermediate object because {@link PathSearcher} implements both {@link AStarSearchResult}
   * and {@link DijkstraSearchResult}, but we want to return an object that implements only {@link AStarSearchResult}
   * (or {@link AStarMultiPathResult}) in order for any {@code instanceof} checks to work as expected
   * (i.e. if we returned the {@link PathSearcher} instance without wrapping it in this intermediate container,
   * then {@code result instanceof AStarSearchResult} and {@code result instanceof DijkstraSearchResult}
   * would both be {@code true}, which is undesirable).
   *
   * @param <T> the graph node type
   * @param <R> the search result type
   */
  protected static class SearchResult<T, R extends PathSearcher<T>> implements AStarSearchResult<T> {
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
    public double getShortestPathCost() {
      return delegate.getShortestPathCost();
    }

    @Override
    @Nullable
    public T getReachedGoal() {
      return delegate.getReachedGoal();
    }

    @Override
    @Nullable
    public List<T> getShortestPath() {
      return delegate.getShortestPath();
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("start", getStart())
          .add("reachedGoal", getReachedGoal())
          .add("shortestPathCost", getShortestPathCost())
          .add("numNodesExamined", getNumNodesExamined())
          .add("shortestPath", getShortestPath())
          .toString();
    }
  }

}
