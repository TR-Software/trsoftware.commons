package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Underlying implementation of {@link AStar} and {@link Dijkstra}.
 * <p>
 * The search is invoked automatically by the constructor, therefore an instance of this class represents the result
 * of a completed search, which cannot be further modified.
 *
 * @author Alex
 * @since 11/13/2023
 */
public class SinglePathSearcher<T> extends PathSearcher<T> {

  /**
   * For node {@code n}, {@link #cameFrom cameFrom.get(n)} is the predecessor of {@code n} on the
   * currently-known cheapest path to it from the start node.
   */
  private final Map<T, T> cameFrom;

  protected SinglePathSearcher(GraphSpec<T> graphSpec, @Nonnull T start, @Nullable Set<T> goals) {
    super(graphSpec, start, goals);
    cameFrom = new LinkedHashMap<>();
    // run the search automatically, to ensure this object is in a consistent state when the search result interface methods are invoked
    runSearch();
  }

  /**
   * Runs the search with the parameters passed to constructor.
   * This method can only be invoked once for any instance of this class.
   *
   * @throws IllegalStateException if the search is already running of finished
   */
  @Override
  protected final void runSearch() {
    // Note: this partly duplicates MultiPathSearcher.runSearch
    while (!frontier.isEmpty()) {
      PQEntry<T> currentEntry = frontier.poll();
      assert currentEntry != null;
      T current = currentEntry.getElement();
      if (isGoal(current)) {
        // stop at the first encountered goal
        reachedGoal = current;
        break;
      }
      for (T next : neighbors(current)) {
        numNodesExamined++;
        double cost = cost(current, next);
        if (Double.isFinite(cost)) {
          double altCost = distance(current) + cost;  // "alt"
          double dNext = distance(next);  // current best estimate
          if (altCost < dNext) {
            /*
             NOTE: this if stmt adds the node for further examination iff:
               1) It's reachable, as checked by Double.isFinite(altCost)
               2) wasn't already evaluated or has a lower cost than what we've seen before
             */
            // found a better alternative (path via next is better than what we had before)
            costSoFar.put(next, altCost);
            // replace any previous backrefs (since the new path is better)
            cameFrom.put(next, current);
            double priority = altCost + computeHeuristic(next);  // priority is the f(x) value for this node
            frontier.offer(next, priority);
          }
        }
      }
    }
    frontier = null;  // clear PQ to free up memory  TODO: maybe make it a local var?
  }

  @Nullable
  @Override
  protected T getAncestor(T node) {
    return cameFrom.get(node);
  }


  @Nonnull
  @Override
  public Set<T> getReachableNodes() {
    return ImmutableSet.copyOf(cameFrom.keySet());
  }
}
