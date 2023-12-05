package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.containsNull;

/**
 * Base class for best-first search algorithms for finding shortest paths between nodes in a weighted graph,
 * such as A* and Dijkstra's (which can be formulated as a special case of A* with heuristic ℎ(N) = 0)
 * <p>
 * The search is invoked automatically by the constructor, therefore an instance of this class represents the result
 * of a completed search, which cannot be further modified.
 *
 * @param <T> the graph node type
 * @see AStar
 * @see Dijkstra
 *
 * @author Alex
 * @since 11/13/2023
 */
abstract class PathSearcher<T> implements DijkstraSearchResult<T>, AStarSearchResult<T> {

  // search parameter fields:
  @Nonnull
  protected final GraphSpec<T> graphSpec;
  @Nonnull
  protected final T start;
  @Nullable
  protected final Set<T> goals;

  // search state fields:
  protected int numNodesExamined;
  /**
   * The first goal in {@link #goals} that was reached by the search, or {@code null} if no goals
   * were specified or didn't find a path to any goal.
   */
  @Nullable
  protected T reachedGoal;
  /**
   * Priority queue of the nodes to be examined next.
   * <p>
   * In the context of Dijkstra's algorithm, the priority of a node is the currently-known best distance to it
   * from the start node.
   * <p>
   * In the context of A*, the priority is the {@code f(n) = g(n) + h(n)} value, where {@code g(n)} is the cost of the
   * path from the start node to {@code n}, and {@code h(n)} is the {@linkplain #heuristic heuristic}
   * function that estimates the cost of the cheapest path from n to the goal.
   */
  protected UniquePQ<T> frontier = new UniquePQ<>();
  /**
   * The current best distance (in terms of cost) from the start node to {@code n},
   * for every node {@code n} that has been examined so far.
   * <p>
   * In the context of A*, this is typically referred to as {@code g(n)}.
   */
  protected final HashMap<T, Double> costSoFar = new HashMap<>();

  protected PathSearcher(GraphSpec<T> graphSpec, @Nonnull T start, @Nullable Set<T> goals) {
    this.graphSpec = requireNonNull(graphSpec, "graphSpec");
    this.start = requireNonNull(start, "start");
    Preconditions.checkArgument(goals == null || (!goals.isEmpty() && !containsNull(goals)),
        "goals must be either null or a non-empty Set without any null elements (actual value: %s)", goals);
    this.goals = goals;
    frontier.offer(start, 0);  // TODO: maybe move to runSearch?
    costSoFar.put(start, 0d);  // TODO: maybe move to runSearch?
    numNodesExamined = 1;
  }

  /**
   * Runs the search with the parameters passed to constructor.
   * This method should be invoked automatically by the subclass constructors, and should never be invoked again.
   */
  protected abstract void runSearch();

  /**
   * Returns the current best distance (in terms of cost) from the start node to the given {@code node}.
   * <p>
   * In the context of A*, this is typically referred to as {@code g(n)}.
   */
  protected double distance(T node) {
    return costSoFar.getOrDefault(node, Double.POSITIVE_INFINITY);
  }

  /**
   * This method, used by {@link #getShortestPath(Object)}, should
   * return the predecessor of the given node on the shortest path discovered by the search.
   * <p>
   * Note: if searching for all possible shortest paths, this method should return the first such ancestor, if any.
   *
   * @return the predecessor of the given node on the shortest path discovered by the search,
   *      or {@code null} if the given node hasn't been examined.
   */
  @Nullable
  protected abstract T getAncestor(T node);

  // PathSearchResult methods:

  @Override
  public T getStart() {
    return start;
  }

  @Override
  public int getNumNodesExamined() {
    return numNodesExamined;
  }

  /**
   * @return the nearest goal encountered by the search, or
   *   {@code null} if no goals were specified or the search did not discover a path to any goal
   */
  @Override
  @Nullable
  public T getReachedGoal() {
    return reachedGoal;
  }

  /**
   * @return the cumulative cost of the shortest path between {@link #start} and {@code target},
   *     or {@link Double#POSITIVE_INFINITY} if no such path exists
   */
  @Override
  public double getShortestPathCost(T target) {
    // TODO: maybe inline distance method, since it's redundant to have both
    return distance(target);
  }

  @Override
  public double getShortestPathCost() {  // AStar
    // Note: this assumes our costSoFar Map allows null keys, which it does
    return distance(reachedGoal);
  }

  /**
   * Returns the first (of possibly several) shortest paths discovered by the search between the
   * {@linkplain #getStart() starting node} and the given {@code target} node.
   *
   * @return immutable list containing the {@linkplain #getStart() starting node} as the first element
   *   and {@code target} as the last element (singleton list if {@code target} is the starting node),
   *   or {@code null} if the search didn't find any paths to the target.
   * @see DijkstraMultiPathResult#getShortestPaths(Object)
   */
  @Nullable
  public List<T> getShortestPath(T target) {  // Dijkstra
    T next = getAncestor(target);
    if (next == null) {
      if (start.equals(target)) {
        // special case: path from start to itself; return singleton list instead of null
        return Collections.singletonList(start);
      }
      // if cameFrom doesn't contain target, then we didn't find a path (this method will return null)
      return null;
    }
    else {
      // reconstruct the path, using cameFrom
      ImmutableList.Builder<T> pathBuilder = ImmutableList.<T>builder();
      pathBuilder.add(target);
      do {
        pathBuilder.add(next);
        next = getAncestor(next);
      } while (next != null);
      return pathBuilder.build().reverse();
    }
  }

  @Nullable
  @Override
  public List<T> getShortestPath() {  // AStar
    return getShortestPath(reachedGoal);
  }

  /**
   * Returns the minimum of the heuristic values from the given node to each goal node.
   * <p>
   * In other words, this method computes
   * <code>Min{{@link #heuristic}(node, g<sub>1</sub>) ... {@link #heuristic}(node, g<sub>N</sub>)}</code>
   * &forall; g<sub>i</sub> &isin; {@link #goals}
   *
   * @return the {@code ℎ(N)} value for the given node.
   */
  protected double computeHeuristic(T node) {
    if (goals != null) {
      // Note: the constructor ensures that goals is either null or a non-empty set without any null elements
      Iterator<T> goalIt = goals.iterator();
      double min = heuristic(node, goalIt.next());
      while (goalIt.hasNext()) {
        min = Math.min(min, heuristic(node, goalIt.next()));
      }
      return min;
      // Note: original implementation (below) used a stream, but was replaced with above loop for better perf
      /*return goals.stream().filter(Objects::nonNull).mapToDouble(goal -> heuristic(node, goal)).min()
          .orElse(0);*/
    }
    // If no goals are specified (e.g. when running Dijkstra's), a heuristic value 0 is both admissible and consistent.
    return 0;
  }

  /**
   * @param node a node in the graph being searched
   * @return true if the node is a goal node of the search
   */
  protected boolean isGoal(T node) {
    if (goals != null)
      return goals.contains(node);
    return false;  // goals are not required for Dijkstra's algorithm
  }

  /**
   * @return the nodes adjacent to the given node in the graph.
   * @see GraphSpec#neighbors(Object)
   */
  protected Iterable<T> neighbors(T node) {
    return graphSpec.neighbors(node);
  }

  /**
   * Computes the true cost of going from node {@code a} to its direct neighbor {@code b}.
   * In other words, this is the weight of edge {@code (a, b)}.
   *
   * @param a a node in the graph
   * @param b a neighbor of {@code a}
   * @return the cost of traversing the edge {@code (a, b)}
   * @see GraphSpec#cost(Object, Object)
   */
  protected double cost(T a, T b) {
    return graphSpec.cost(a, b);
  }

  /**
   * Estimates the cost of going from {@code a} to {@code b} (where {@code b} is typically the goal node).
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
   *
   * @param a a node in the graph
   * @param b any other node (typically the goal node) in the same graph
   * @return estimate of the cost of going from a to b (assuming this is a weighted graph)
   * @see <a href="https://en.wikipedia.org/wiki/A*_search_algorithm#Properties">Properties of A* Search</a>
   * @see <a href="https://en.wikipedia.org/wiki/Admissible_heuristic">Admissible heuristic</a>
   * @see <a href="https://en.wikipedia.org/wiki/Consistent_heuristic">Consistent heuristic</a>
   * @see GraphSpec#heuristic(Object, Object)
   */
  protected double heuristic(T a, T b) {
    return graphSpec.heuristic(a, b);
  }

  static class PQEntry<T> implements Comparable<PQEntry<T>> {
    private final T element;
    private final double priority;

    public PQEntry(@Nonnull T element, double priority) {
      this.element = requireNonNull(element, "element");
      this.priority = priority;
    }

    public T getElement() {
      return element;
    }

    public double getPriority() {
      return priority;
    }

    @Override
    public int compareTo(PQEntry o) {
      return Double.compare(this.priority, o.priority);
    }

    @Override
    public String toString() {
      return element + ": " + priority;
    }
  }

  /**
   * Wraps a {@link PriorityQueue} containing {@link PQEntry} elements and ensures that the same
   * {@linkplain PQEntry#element node} cannot be added more than once (e.g. with a worse priority).
   *
   * @param <T> the node type
   */
  static class UniquePQ<T> {
    // TODO: maybe unit test this class?
    private final PriorityQueue<PQEntry<T>> queue = new PriorityQueue<>();
    private final Map<T, PQEntry<T>> entries = new HashMap<>();  // TODO: maybe use Multimap to account for dup entries?


    public boolean offer(@Nonnull T element, double priority) {
      PQEntry<T> existingEntry = entries.get(element);
      if (existingEntry == null || priority < existingEntry.priority) {
        /*
         Either the queue doesn't contain the element or contains it with a worse priority.
         In the latter case, it would be ideal to just update the priority of the existing entry for this element,
         but we can't do that efficiently with Java's PriorityQueue implementation
         (PQ.remove + PQ.offer is O(n*log(n)); see https://stackoverflow.com/q/1871253),
         so instead, we insert a duplicated entry for this element (with the better priority),
         and update our entries map to point to this newer entry.
         */
        PQEntry<T> newEntry = new PQEntry<>(element, priority);
        entries.put(element, newEntry);
        return queue.offer(newEntry);  // PriorityQueue.offer always returns true
      }
      return false;  // already have this element in the queue with a better or equal priority
    }

    @Nullable
    public PQEntry<T> poll() {
      PQEntry<T> entry = queue.poll();
      if (entry != null) {
        entries.remove(entry.element);
        return entry;
      }
      return null;
    }

    public int size() {
      return queue.size();
    }

    public boolean isEmpty() {
      return queue.isEmpty();
    }

    @Override
    public String toString() {
      return queue.toString();
    }
  }

}
