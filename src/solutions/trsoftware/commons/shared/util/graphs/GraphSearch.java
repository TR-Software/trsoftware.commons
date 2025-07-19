package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.Graph;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.ValueGraph;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.compare.LexicographicComparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Search algorithms for working with {@linkplain com.google.common.graph Guava's graph package}.
 * 
 * @author Alex
 * @since 4/17/2025
 * @param <N> Node parameter type
 */
public class GraphSearch<N> {
  @Nonnull
  private final SuccessorsFunction<N> graph;
  /**
   * Comparator for sorting the search results, like {@link ElementOrder}
   */
  @Nullable
  private Comparator<N> elementComparator;
  /**
   * True if graph is {@linkplain Graph#isDirected() directed}, false if unknown
   */
  private boolean directed;


  public GraphSearch(@Nonnull SuccessorsFunction<N> graph) {
    this(graph, null);
  }

  public GraphSearch(@Nonnull SuccessorsFunction<N> graph, @Nullable Comparator<N> elementComparator) {
    this.graph = requireNonNull(graph, "graph");
    this.elementComparator = elementComparator;
  }

  public GraphSearch(@Nonnull Graph<N> graph) {
    this.graph = requireNonNull(graph, "graph");
    ElementOrder<N> nodeOrder = graph.nodeOrder();
    if (nodeOrder.type() == ElementOrder.Type.SORTED)
      this.elementComparator = nodeOrder.comparator();
    directed = graph.isDirected();
  }

  public GraphSearch(@Nonnull ValueGraph<N, ?> graph) {
    this.graph = requireNonNull(graph, "graph");
    ElementOrder<N> nodeOrder = graph.nodeOrder();
    if (nodeOrder.type() == ElementOrder.Type.SORTED)
      this.elementComparator = nodeOrder.comparator();
    directed = graph.isDirected();
  }

  // TODO: unit test & doc

  public List<List<N>> detectCycles(Iterable<N> rootNodes) {
    Set<N> nodesExamined = new LinkedHashSet<>();
    List<List<N>> cycles = new ArrayList<>();
    for (N node : rootNodes) {
      if (nodesExamined.contains(node))
        continue;  // non-root node (already visited by a DFS)
      // DFS from this node
      List<N> path = new ArrayList<>();
      Multimap<N, N> visitedEdges = LinkedHashMultimap.create();
      boolean cycleDetected = detectCycles(node, path, visitedEdges, cycles);
      nodesExamined.addAll(visitedEdges.keySet());  // exclude nodes visited on this iteration from roots
    }
    // remove duplicates
    removeDuplicates(cycles);
    // sort the cycles using the specified comparator, if any
    if (elementComparator != null)
      cycles.sort(new LexicographicComparator<>(elementComparator));
    return cycles;
  }

  private boolean detectCycles(N node, List<N> path, Multimap<N, N> visitedEdges, List<List<N>> cycles) {
    // DFS traversal (pre-order)
    int lastIndexOfNode = path.lastIndexOf(node);
    boolean cycleDetected = lastIndexOfNode != -1;
    if (cycleDetected) {
      // base case: cycle detected
      List<N> cycle = ListUtils.copyOfRange(path, lastIndexOfNode, path.size());
      cycle.add(node);  // cycle must contain same start and end node (by definition)
      cycles.add(cycle);
    }
    // recursive case:
    Iterable<? extends N> successors = graph.successors(node);
    boolean visitedSuccessors = false;
    for (N next : successors) {
      if (!visitedEdges.containsEntry(node, next)) {
        visitedEdges.put(node, next);
        if (!visitedSuccessors) {
          path.add(node);  // push current node onto stack
          visitedSuccessors = true;
        }
        // recursive call:
        cycleDetected |= detectCycles(next, path, visitedEdges, cycles);
      }
    }
    if (visitedSuccessors) {
      path.remove(path.size() - 1);  // pop current node off the stack
    }
    return cycleDetected;
  }

  private static <E, L extends List<E>> void removeDuplicates(List<L> lists) {
    // TODO: maybe extract to util class
    Set<Set<E>> uniques = new LinkedHashSet<>();
    lists.removeIf(list -> !uniques.add(ImmutableSet.copyOf(list)));
  }
}
