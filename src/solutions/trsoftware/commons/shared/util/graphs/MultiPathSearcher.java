package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.collect.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Underlying implementation of {@link AStarMultiPath} and {@link DijkstraMultiPath}.
 * <p>
 * The search is invoked automatically by the constructor, therefore an instance of this class represents the result
 * of a completed search, which cannot be further modified.
 * <p>
 * Unlike {@link SinglePathSearcher}, which terminates the {@linkplain #runSearch() search loop} as soon as it encounters
 * a goal node, {@link MultiPathSearcher} keeps going until every node having the same priority as the goal node in the
 * {@linkplain #frontier priority queue} has been examined, in order to discover all alternate paths of the same cost
 * as the optimal path.  To keep track of multiple path alternatives, it stores the {@linkplain #cameFrom back-references}
 * in a Multimap instead of a Map.
 *
 * @author Alex
 * @since 11/13/2023
 */
public class MultiPathSearcher<T> extends PathSearcher<T> implements DijkstraMultiPathResult<T>, AStarMultiPathResult<T> {

  /**
   * For node {@code n}, {@link #cameFrom cameFrom.get(n)} is the set of nodes immediately preceding it on the
   * currently-known cheapest paths to it from the start node.
   */
  protected final SetMultimap<T, T> cameFrom;

  /**
   * If multiple equidistant, they
   */
  private Set<T> reachedGoals;

  protected MultiPathSearcher(GraphSpec<T> graphSpec, @Nonnull T start, @Nullable Set<T> goals) {
    super(graphSpec, start, goals);
    // TODO: maybe use regular Map<T, Set<T>> with computeIfAbsent for better perf than Multimap?
    cameFrom = MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
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
    // Note: this partly duplicates SinglePathSearcher.runSearch
    Double bestCost = null;
    HashSet<T> alreadyExpanded = new HashSet<>();  // keep track to avoid expanding same node more than once
    while (!frontier.isEmpty()) {
      PQEntry<T> currentEntry = frontier.poll();
      assert currentEntry != null;
      if (bestCost != null && bestCost < currentEntry.getPriority())
        break;  // the remainder of the queue is worse than this
      T current = currentEntry.getElement();
      if (isGoal(current)) {
        addReachedGoal(current);
        // don't stop just yet: finish expanding all PQ entries with the same priority, to find all possible shortest paths
        bestCost = distance(current);
        continue;
      }
      for (T next : neighbors(current)) {
        numNodesExamined++;
        double cost = cost(current, next);
        if (Double.isFinite(cost)) {
          double altCost = distance(current) + cost;  // "alt"
          double dNext = distance(next);  // current best estimate
          if (altCost <= dNext) {
            /*
             NOTE: this if stmt adds the node for further examination iff:
               1) It's reachable, as checked by Double.isFinite(altCost)
               2) wasn't already evaluated or has a lower cost than what we've seen before
             */
            if (altCost < dNext) {
              // found a better alternative (path via next is better than what we had before)
              costSoFar.put(next, altCost);
              // remove any previous backrefs (since the new path is better)
              if (cameFrom.containsKey(next))  // Note: checking containsKey to avoid allocation of intermediate view collection
                cameFrom.removeAll(next);  // TODO: is this stmt ever reached in practice?
            }
            cameFrom.put(next, current);
            if (alreadyExpanded.add(next)) {  // haven't expanded this node yet
              double priority = altCost + computeHeuristic(next);  // priority is the f(x) value for this node
              frontier.offer(next, priority);
            }
          }
        }
      }
    }
    frontier = null;  // clear PQ to free up memory  TODO: maybe make it a local var?
  }

  private void addReachedGoal(T goal) {
    if (reachedGoal == null) {
      reachedGoal = goal;
    }
    else {
      // expand to a set only if multiple goals were discovered
      if (reachedGoals == null) {
        reachedGoals = new LinkedHashSet<>();
        reachedGoals.add(reachedGoal);
      }
      reachedGoals.add(goal);
    }
  }

  /**
   * @return the subset of {@link #goals} encountered by the search, or
   *   {@code null} if no goals were specified or the search did not discover a path to any goal
   */
  @Nullable
  public Set<T> getReachedGoals() {
    if (reachedGoals != null) {
      // multiple goals discovered
      return ImmutableSet.copyOf(reachedGoals);  // defensive copy
    }
    else if (reachedGoal != null) {
      // only 1 goal discovered
      return Collections.singleton(reachedGoal);
    }
    else {
      // no goals discovered
      return null;
    }
  }

  @Nullable
  @Override
  protected T getAncestor(T node) {
    return Iterables.getFirst(cameFrom.get(node), null);
  }

  @Nonnull
  @Override
  public Set<T> getReachableNodes() {
    return ImmutableSet.copyOf(cameFrom.keySet());
  }

  /**
   * Constructs a set of all possible shortest paths between {@link #start} and {@code target}.
   * <p>
   * Note: if only 1 path is needed, the more-lightweight {@link #getShortestPath} method can offer better performance.
   *
   * @return immutable set of all shortest paths from the {@linkplain #getStart() starting node} to {@code target},
   *     or {@code null} if didn't find any finite-cost paths.
   *     <p>Note: if {@code target} is the starting node, will return a singleton set containing a singleton list
   *     (with the starting/target node as the only element).
   * @see #getShortestPath(Object)
   */
  @Nullable
  public Set<List<T>> getShortestPaths(T target) {
    if (!cameFrom.containsKey(target)) {
      if (start.equals(target)) {
        // special case: path from start to itself
        return Collections.singleton(Collections.singletonList(start));
      }
      // if cameFrom doesn't contain goal, then we didn't find a path
      return null;
    }
    else {
      // run a DFS on the sub-graph represented by cameFrom
      ImmutableSet.Builder<List<T>> paths = ImmutableSet.builder();
      Deque<PathNode<T>> stack = new LinkedList<>();
      stack.push(new PathNode<>(target, null));
      while (!stack.isEmpty()) {
        PathNode<T> current = stack.pop();
        Set<T> ancestors = cameFrom.get(current.node);
        if (ancestors.isEmpty()) {
          // reached the starting node
          paths.add(current.getPath());
        }
        else {
          for (T next : ancestors) {
            stack.push(new PathNode<>(next, current));
          }
        }
      }
      return paths.build();
    }
  }

  @Nullable
  @Override
  public Set<List<T>> getShortestPaths() {  // AStar
    if (reachedGoal == null)
      // no goals found
      return null;
    else if (reachedGoals != null) {
      // multiple goals found
      ImmutableSet.Builder<List<T>> union = ImmutableSet.builder();
      reachedGoals.stream().map(this::getShortestPaths)
          .filter(Objects::nonNull)  // probably can assume that not null, since each of these goals was found
          .forEach(union::addAll);
      return union.build();
      /*
        Note: could improve perf of this branch by extracting helper method from getShortestPaths(T)
        that takes an ImmutableSet.Builder and adds each List to the builder rather than returning a Set.
        However, it's probably rare to find multiple equidistant goals, so probably not worth the effort to optimize this
       */
    }
    else {
      // found exactly 1 goal
      return getShortestPaths(reachedGoal);
    }
  }

  /**
   * Helper class for {@link #getShortestPaths}: a stack frame in the DFS search over the graph of shortest
   * path represented by {@link #cameFrom}.
   */
  private static class PathNode<T> {
    private final T node;
    private final PathNode<T> parent;

    /**
     * Extends the given path with the given node.
     */
    PathNode(T node, PathNode<T> parent) {
      this.node = node;
      this.parent = parent;
    }

    /**
     * @return reconstructs the path from this node to the root of the tree
     * @see #getShortestPaths
     */
    List<T> getPath() {
      ImmutableList.Builder<T> pathBuilder = ImmutableList.builder();
      for (PathNode<T> next = this; next != null; next = next.parent) {
        pathBuilder.add(next.node);
      }
      return pathBuilder.build();
    }
  }
}
