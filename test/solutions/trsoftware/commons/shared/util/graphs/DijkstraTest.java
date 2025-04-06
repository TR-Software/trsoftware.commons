package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.collect.ImmutableSet;
import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.lenientFormat;
import static java.util.Arrays.asList;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertContains;
import static solutions.trsoftware.commons.shared.util.StringUtils.methodCallToString;
import static solutions.trsoftware.commons.shared.util.graphs.Location.loc;

/**
 * @author Alex
 * @since 10/6/2023
 */
public class DijkstraTest extends PathSearchTestCase {

  public void testSearch() throws Exception {
    Grid grid = generateGrid(new String[]{
        "#...",
        "#X#.",
        "###.",
        ".a#.",
        "....",
        "....",
        "..X.",
    });
    graphSpec = new GraphSpec(grid);
    Location start = loc(1, 3);
    Map<Location, Set<List<Location>>> expectedPaths = MapUtils.linkedHashMap(  // can't use ImmutableMap b/c that doesn't permit null values
        // 'X' locations
        loc(2, 6), ImmutableSet.of(
            asList(start, loc(1, 4), loc(1, 5), loc(1, 6), loc(2, 6)),
            asList(start, loc(1, 4), loc(1, 5), loc(2, 5), loc(2, 6)),
            asList(start, loc(1, 4), loc(2, 4), loc(2, 5), loc(2, 6))),
        loc(1, 1), ImmutableSet.of(
            asList(start, loc(1, 4), loc(1, 5), loc(1, 6), loc(1, 0), loc(1, 1))),
        // adjacent node (cost = 1)
        loc(1, 4), ImmutableSet.of(
            asList(start, loc(1, 4))),
        // start node itself (cost = 0, singleton list as path)
        start, ImmutableSet.of(
            Collections.singletonList(start)),
        // 2 nodes away
        loc(0, 4), ImmutableSet.of(
            asList(start, loc(0, 3), loc(0, 4)),
            asList(start, loc(1, 4), loc(0, 4))),
        // a target without any valid paths to it ('#')
        loc(0, 0), null
    );

    // search and verify results of both single-path and multi-path search implementations
    searchAndVerifyResult(new Dijkstra<>(graphSpec), start, r ->
        verifySearchResult(r, expectedPaths));

    searchAndVerifyResult(new DijkstraMultiPath<>(graphSpec), start, r ->
        verifySearchResult(r, expectedPaths));
  }

  /**
   * Search with single goal.
   *
   * @param goal the single goal node to search for
   * @param start the starting node for the search
   * @param verifier will be invoked to verify the search result before returning it
   * @return the search result
   */
  private <R extends DijkstraSearchResult<Location>> R searchAndVerifyResult(DijkstraBase<Location, R> searcher, Location start,
                                                                             Consumer<R> verifier) {
    System.out.println("Results of " + methodCallToString(searcher.getClass(), "search", start) + ":");
    R result = searcher.search(start);
    printSearchResult(result);
    verifier.accept(result);
    System.out.println("--------------------------------------------------------------------------------");
    return result;
  }


  private void verifySearchResult(DijkstraSearchResult<Location> result,
                                  Map<Location, Set<List<Location>>> expectedPathsByTarget) {
    // TODO: maybe refactor dup code from other overloads of this method
    for (Location target : expectedPathsByTarget.keySet()) {
      Set<List<Location>> expectedPaths = expectedPathsByTarget.get(target);
      List<Location> shortestPath = result.getShortestPath(target);
      double shortestPathCost = result.getShortestPathCost(target);
      if (expectedPaths == null) {
        assertNull(shortestPath);
        assertEquals(Double.POSITIVE_INFINITY, shortestPathCost);
      }
      else {
        assertNotNull(shortestPath);
        assertContains(expectedPaths, shortestPath);
        assertEquals(graphSpec.getPathCost(shortestPath), shortestPathCost);
        assertEquals(target, ListUtils.last(shortestPath));  // path should terminate at the target node
      }
    }
  }

  private void verifySearchResult(DijkstraMultiPathResult<Location> result,
                                  Map<Location, Set<List<Location>>> expectedPathsByTarget) {
    // TODO: maybe refactor dup code from other overloads of this method
    for (Location target : expectedPathsByTarget.keySet()) {
      Set<List<Location>> expectedPaths = expectedPathsByTarget.get(target);
      Set<List<Location>> shortestPaths = result.getShortestPaths(target);
      List<Location> shortestPath = result.getShortestPath(target);
      double shortestPathCost = result.getShortestPathCost(target);
      if (expectedPaths == null) {
        assertNull(shortestPaths);
        assertNull(shortestPath);
        assertEquals(Double.POSITIVE_INFINITY, shortestPathCost);
      }
      else {
        assertNotNull(shortestPaths);
        assertEquals(expectedPaths, shortestPaths);
        assertNotNull(shortestPath);
        assertContains(expectedPaths, shortestPath);
        for (List<Location> path : shortestPaths) {
          assertEquals(graphSpec.getPathCost(path), shortestPathCost);
          assertEquals(target, ListUtils.last(path));  // each path should terminate at the target node
        }
      }
    }
  }

  public static <T> void printSearchResult(DijkstraSearchResult<T> result) {
    System.out.println("\tstart: " + result.getStart());
    System.out.println("\tnodesExamined: " + result.getNumNodesExamined());
    Set<T> reachableNodes = result.getReachableNodes();
    System.out.println("\treachableNodes: " + reachableNodes);
    System.out.println("\tshortestPaths:");
    for (T node : reachableNodes) {
      Set<List<T>> shortestPaths;
      if (result instanceof DijkstraMultiPathResult) {
        DijkstraMultiPathResult<T> multiPathResult = (DijkstraMultiPathResult<T>)result;
        shortestPaths = multiPathResult.getShortestPaths(node);
        assertNotNull(shortestPaths);  // shouldn't be null because the node is reachable
      }
      else {
        List<T> shortestPath = result.getShortestPath(node);
        assertNotNull(shortestPath);  // shouldn't be null because the node is reachable
        shortestPaths = Collections.singleton(shortestPath);
      }
      System.out.println(lenientFormat("\t\t%s: cost=%s; paths:", node, result.getShortestPathCost(node)));
      printPaths(shortestPaths, "\t\t\t\t\t ");
    }
  }

  public static <T> void printAbridgedSearchResult(DijkstraSearchResult<T> result) {
    System.out.println("\tstart: " + result.getStart());
    System.out.println("\tnodesExamined: " + result.getNumNodesExamined());
    Set<T> reachableNodes = result.getReachableNodes();
    Map<T, Double> pathCosts = reachableNodes.stream().collect(
        MapUtils.toMap(Function.identity(), result::getShortestPathCost, LinkedHashMap::new));
    System.out.println("\treachableNodes: " + reachableNodes.size());
    System.out.println("\tshortestPathCost: " + pathCosts);

  }

  public static <T> void printAbridgedSearchResult(DijkstraSearchResult<T> result, int maxPaths) {
    System.out.println("\tstart: " + result.getStart());
    System.out.println("\tnodesExamined: " + result.getNumNodesExamined());
    Set<T> reachableNodes = result.getReachableNodes();
    Map<T, Double> pathCosts = reachableNodes.stream().collect(Collectors.toMap(Function.identity(), result::getShortestPathCost));
    System.out.println("\treachableNodes: " + reachableNodes.size());
    System.out.println("\tshortestPaths:");
    for (T node : reachableNodes) {
      Set<List<T>> paths;
      if (result instanceof DijkstraMultiPathResult) {
        DijkstraMultiPathResult<T> multiPathResult = (DijkstraMultiPathResult<T>)result;
        paths = multiPathResult.getShortestPaths(node);
        assertNotNull(paths);  // shouldn't be null because the node is reachable
      }
      else {
        List<T> shortestPath = result.getShortestPath(node);
        assertNotNull(shortestPath);  // shouldn't be null because the node is reachable
        paths = Collections.singleton(shortestPath);
      }
      StringBuilder out = new StringBuilder().append("\t\t").append(node)
          .append(": cost=").append(result.getShortestPathCost(node));
      if (maxPaths > 0) {
        out.append("; paths (size=").append(paths.size()).append("):");
      }
      if (maxPaths == 0) {
        System.out.println(lenientFormat("\t\t%s: cost=%s", node, result.getShortestPathCost(node)));
      }
      else {
        System.out.println(lenientFormat("\t\t%s: cost=%s; paths (size=%s):",
            node, result.getShortestPathCost(node), paths.size()));
        String indent = "\t\t\t\t\t ";
        paths.stream().limit(maxPaths)
            .sorted(CollectionUtils.lexicographicOrder(Comparator.comparing(T::toString)))
            .map(path -> indent + path)
            .forEach(System.out::println);
        int remainder = paths.size() - maxPaths;
        if (remainder > 0) {
          System.out.println(indent + "+" + remainder + " more..");
        }

      }
    }
  }


}