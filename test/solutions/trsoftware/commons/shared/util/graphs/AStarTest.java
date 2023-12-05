package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.text.CharRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertContains;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.StringUtils.methodCallToString;
import static solutions.trsoftware.commons.shared.util.graphs.Location.loc;

/**
 * @author Alex
 * @since 10/6/2023
 */
public class AStarTest extends PathSearchTestCase {

  @SuppressWarnings("ConstantConditions")
  public void testBadArgs() throws Exception {
    graphSpec = new GraphSpec(generateGrid(new String[]{
        "#...",
        "#X#.",
        "###.",
        ".a#.",
        "....",
        "....",
        "..X.",
    }));

    {
      // 1) constructor
      assertThrows(NullPointerException.class, () -> new AStar<Location>(null));
      // 2) search methods
      AStar<Location> searcher = new AStar<>(graphSpec);
      // a) null start/goals
      for (Location start : new Location[]{null, loc(1, 3)}) {
        assertThrows(NullPointerException.class, (Runnable)() -> searcher.search(start, (Location)null));
        assertThrows(NullPointerException.class, (Runnable)() -> searcher.search(start, (Set<Location>)null));
      }
      // b) goals is empty set or contains null
      Location start = loc(1, 3);
      assertThrows(IllegalArgumentException.class, (Runnable)() -> searcher.search(start, Collections.emptySet()));
      assertThrows(IllegalArgumentException.class, (Runnable)() -> searcher.search(start, SetUtils.newSet(loc(2, 6), loc(1, 1), null)));
    }
    // TODO: maybe extract code duped between these 2 blocks
    {
      // 1) constructor
      assertThrows(NullPointerException.class, () -> new AStarMultiPath<Location>(null));
      // 2) search methods
      AStarMultiPath<Location> searcher = new AStarMultiPath<>(graphSpec);
      // a) null start/goals
      for (Location start : new Location[]{null, loc(1, 3)}) {
        assertThrows(NullPointerException.class, (Runnable)() -> searcher.search(start, (Location)null));
        assertThrows(NullPointerException.class, (Runnable)() -> searcher.search(start, (Set<Location>)null));
      }
      // b) goals is empty set or contains null
      Location start = loc(1, 3);
      assertThrows(IllegalArgumentException.class, (Runnable)() -> searcher.search(start, Collections.emptySet()));
      assertThrows(IllegalArgumentException.class, (Runnable)() -> searcher.search(start, SetUtils.newSet(loc(2, 6), loc(1, 1), null)));
    }
    
  }

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
    Map<Location, Set<List<Location>>> expectedPaths = ImmutableMap.of(
        loc(2, 6), ImmutableSet.of(
            asList(loc(1, 3), loc(1, 4), loc(1, 5), loc(1, 6), loc(2, 6)),
            asList(loc(1, 3), loc(1, 4), loc(1, 5), loc(2, 5), loc(2, 6)),
            asList(loc(1, 3), loc(1, 4), loc(2, 4), loc(2, 5), loc(2, 6))),
        loc(1, 1), ImmutableSet.of(
            asList(loc(1, 3), loc(1, 4), loc(1, 5), loc(1, 6), loc(1, 0), loc(1, 1))
        )
    );
    graphSpec = new GraphSpec(grid);
    Set<Location> goals = grid.streamCells().filter(cell -> cell.getValue() == 'X')
        .map(Grid.Cell::getLocation).collect(Collectors.toSet());
    assertEquals(ImmutableSet.of(loc(2, 6), loc(1, 1)), goals);
    Set<Location> startLocations = grid.streamCells().filter(cell -> cell.getValue() == 'a')
        .map(Grid.Cell::getLocation).collect(Collectors.toSet());
    Location start = loc(1, 3);
    assertEquals(Collections.singleton(start), startLocations);  // ensure only 1 start location specified

    // 1) search with all goals
    searchAndVerifyResults(start, goals, loc(2, 6), expectedPaths.get(loc(2, 6)));
    // 2) search with each goal individually
    for (Location goal : goals) {
      searchAndVerifyResults(start, goal, goal, expectedPaths.get(goal));
    }
  }

  public void testSearchWithMultiplePaths() throws Exception {
    Grid grid = generateGrid(new String[]{
        "X...",
        "#a#.",
        "###.",
        ".X#.",
        "....",
        "....",
        "..b.",
    });
    graphSpec = new GraphSpec(grid);
    Location g0 = loc(0, 0);
    Location g1 = loc(1, 3);
    Location a = loc(1, 1);
    Location b = loc(2, 6);

    Set<Location> goals = grid.streamCells().filter(cell -> cell.getValue() == 'X')
        .map(Grid.Cell::getLocation).collect(Collectors.toSet());
    assertEquals(ImmutableSet.of(g0, g1), goals);

    // 1) search from 'a' (1, 1), with all goals:
    searchAndVerifyResults(a, goals, g0,
        // only 1 shortest path in this case
        singleton(asList(a, loc(1, 0), g0)));

    // 2) search from 'b' (2, 6), with all goals:
    //    this should find multiple shortest paths from b to g0, but none to g1 (since it's farther away)
    searchAndVerifyResults(b, goals, g0,
        // shortest path from b to g0 could be any one of the following paths:
        ImmutableSet.of(
            // start by going left
            asList(loc(2, 6), loc(1, 6), loc(0, 6), loc(0, 0)),
            asList(loc(2, 6), loc(1, 6), loc(1, 0), loc(0, 0)),
            // start by going right
            asList(loc(2, 6), loc(3, 6), loc(0, 6), loc(0, 0)),
            asList(loc(2, 6), loc(3, 6), loc(3, 0), loc(0, 0)),
            // start by going down
            asList(loc(2, 6), loc(2, 0), loc(1, 0), loc(0, 0)),
            asList(loc(2, 6), loc(2, 0), loc(3, 0), loc(0, 0))
        )
    );

  }

  public void testSearchWithEquidistantGoals() throws Exception {
    /* Test what happens if there are multiple shortest paths to each of several equidistant goals
         - should it return all paths to all such goals (maybe replace result.reachedGoal with a Set instead of single value?)
       Note: this is only applicable to multi-path search
     */
    Grid grid = generateGrid(new String[]{
        "#..a",
        "#X#.",
        "###.",
        "..#Y",
        "....",
        ".Z..",
        "....",
    });
    // here we have 2 goals (X and Y) at at the same distance (3 moves) from 'a', and another goal ('Z') farther away
    graphSpec = new GraphSpec(grid);
    Set<Location> goals = ImmutableSet.of(loc(1, 1), loc(3, 3), loc(1, 5));
    Set<Location> nearestGoals = ImmutableSet.of(loc(1, 1), loc(3, 3));
    Location start = loc(3, 0);
    // sanity check:
    goals.forEach(goal -> assertTrue(new CharRange('X', 'Z').contains(grid.getCell(goal).getValue())));
    assertEquals('a', grid.getCell(start).getValue());

    searchAndVerifyResults(start, goals, nearestGoals,
        ImmutableSet.of(
            // path from 'a' to 'X'
            asList(loc(3, 0), loc(2, 0), loc(1, 0), loc(1, 1)),
            // path from 'a' to 'Y'
            asList(loc(3, 0), loc(3, 1), loc(3, 2), loc(3, 3))
        )
    );
  }

  public void testSearchStartingAtGoal() throws Exception {
    Grid grid = generateGrid(new String[]{
        "#..a",
        "#X#.",
        "###.",
        "..#Y",
        "....",
        ".Z..",
        "....",
    });
    graphSpec = new GraphSpec(grid);

    // if the set of goals includes the starting node, shortest path cost should be 0, and the path should be a
    // singleton list containing just the starting node
    Location start = loc(3, 0);
    Set<Location> goals = ImmutableSet.of(start, loc(1, 1), loc(3, 3), loc(1, 5));

    searchAndVerifyResults(start, goals, start,
        ImmutableSet.of(
            // path from 'a' to 'a'
            Collections.singletonList(start)
        )
    );
  }

  public void testSearchWithoutValidPaths() throws Exception {
    Grid grid = generateGrid(new String[]{
        "###.",
        "#X#.",
        "###.",
        ".a#.",
        "....",
        "..#.",
        ".#X#",
    });
    graphSpec = new GraphSpec(grid);
    Set<Location> goals = ImmutableSet.of(loc(2, 6), loc(1, 1));
    Location start = loc(1, 3);

    // 1) search with all goals
    searchAndVerifyResults(start, goals, (Location)null, null);
    // 2) search with each goal individually
    for (Location goal : goals) {
      searchAndVerifyResults(start, goal, null, null);
    }
    // 3) with an obstacle cells ('#') as goal
    searchAndVerifyResults(start, loc(2, 3), null, null);
  }

  /**
   * Search with multiple goals; expect single goal reached.
   *
   * @param start the starting node for the search
   * @param goal the single goal node to search for
   * @param expectedGoal the expected goal node discovered by the search, or {@code null} if no goals are reachable
   * @param expectedPaths all possible shortest paths between the starting node and the goal expected to be reached
   */
  private void searchAndVerifyResults(Location start, Set<Location> goals, Location expectedGoal,
                                      Set<List<Location>> expectedPaths) {
    searchAndVerifyResults(start, goals,
        expectedGoal != null ? singleton(expectedGoal) : null,
        expectedPaths);
  }

  /**
   * Search with multiple goals; expect multiple (equidistant) goals reached.
   *
   * @param start the starting node for the search
   * @param goal the single goal node to search for
   * @param expectedGoals the expected goal nodes discovered by the search (if there are multiple equidistant goals);
   *   singleton if only 1 nearest goal, or {@code null} if no goals are reachable
   * @param expectedPaths all possible shortest paths between the starting node and the goal expected to be reached
   */
  private void searchAndVerifyResults(Location start, Set<Location> goals, Set<Location> expectedGoals,
                                      Set<List<Location>> expectedPaths) {
    // test both single-path and multi-path search
    search(new AStar<>(graphSpec), start, goals,
        result -> verifySearchResult(result, expectedGoals, expectedPaths));
    search(new AStarMultiPath<>(graphSpec), start, goals,
        result -> verifySearchResult(result, expectedGoals, expectedPaths));
  }

  /**
   * Search with single goal.
   *
   * @param start the starting node for the search
   * @param goal the single goal node to search for
   * @param expectedGoal the expected goal node discovered by the search, or {@code null} if no goals are reachable
   * @param expectedPaths all possible shortest paths between the starting node and the goal expected to be reached
   */
  private void searchAndVerifyResults(Location start, Location goal, @Nullable Location expectedGoal,
                                      Set<List<Location>> expectedPaths) {
    // test both single-path and multi-path search
    search(new AStar<>(graphSpec), start, goal,
        result -> verifySearchResult(result, expectedGoal, expectedPaths));
    search(new AStarMultiPath<>(graphSpec), start, goal,
        result -> verifySearchResult(result, expectedGoal, expectedPaths));
  }

  /**
   * Search with single goal.
   * @param start the starting node for the search
   * @param goal the single goal node to search for
   * @param verifier will be invoked to verify the search result before returning it
   * @return the search result
   */
  private <R extends AStarSearchResult<Location>> R search(AStarBase<Location, R> searcher, Location start, Location goal,
                                                           Consumer<R> verifier) {
    System.out.println("Results of " + methodCallToString(searcher.getClass(), "search", start, goal) + ":");
    R result = searcher.search(start, goal);
    printSearchResult(result);
    verifier.accept(result);
    System.out.println("--------------------------------------------------------------------------------");
    return result;
  }

  /**
   * Search with single goal.
   * @param start the starting node for the search
   * @param goal the single goal node to search for
   * @param verifier will be invoked to verify the search result before returning it
   * @return the search result
   */
  private <R extends AStarSearchResult<Location>> R search(AStarBase<Location, R> searcher, Location start, Set<Location> goals,
                                                           Consumer<R> verifier) {
    System.out.println("Results of " + methodCallToString(searcher.getClass(), "search", start, goals) + ":");
    R result = searcher.search(start, goals);
    printSearchResult(result);
    verifier.accept(result);
    System.out.println("--------------------------------------------------------------------------------");
    return result;
  }


  private void verifySearchResult(@Nonnull AStarSearchResult<Location> result,
                                  @Nullable Location expectedGoal,
                                  @Nullable Set<List<Location>> expectedPaths) {
    verifySearchResult(result,
        expectedGoal != null ? singleton(expectedGoal) : null,
        expectedPaths);
  }

  private void verifySearchResult(@Nonnull AStarSearchResult<Location> result,
                                  @Nullable Set<Location> expectedGoals,
                                  @Nullable Set<List<Location>> expectedPaths) {
    Location reachedGoal = result.getReachedGoal();
    List<Location> shortestPath = result.getShortestPath();
    double shortestPathCost = result.getShortestPathCost();
    if (expectedPaths == null) {
      assertNull(shortestPath);
      assertEquals(Double.POSITIVE_INFINITY, shortestPathCost);
      assertNull(reachedGoal);
      assertNull(expectedGoals);
    }
    else {
      assertNotNull(shortestPath);
      assertContains(expectedPaths, shortestPath);
      assertEquals(graphSpec.getPathCost(shortestPath), shortestPathCost);
      assertNotNull(reachedGoal);
      assertContains(expectedGoals, reachedGoal);
    }
  }

  private void verifySearchResult(AStarMultiPathResult<Location> result,
                                  Location expectedGoal,
                                  Set<List<Location>> expectedPaths) {
    verifySearchResult(result,
        expectedGoal != null ? singleton(expectedGoal) : null,
        expectedPaths);
  }

  private void verifySearchResult(AStarMultiPathResult<Location> result,
                                  Set<Location> expectedGoals,
                                  Set<List<Location>> expectedPaths) {
    verifySearchResult((AStarSearchResult<Location>)result, expectedGoals, expectedPaths);
    Set<List<Location>> shortestPaths = result.getShortestPaths();
    Set<Location> reachedGoals = result.getReachedGoals();
    double shortestPathCost = result.getShortestPathCost();
    if (expectedPaths == null) {
      assertNull(reachedGoals);
      assertNull(shortestPaths);
    }
    else {
      assertNotNull(reachedGoals);
      assertEquals(expectedGoals, reachedGoals);
      assertNotNull(shortestPaths);
      assertEquals(expectedPaths, shortestPaths);
      // make sure that the cost of each shortest path is equal to the shortestPathCost
      // and that there is at least 1 path to each of the reached goals
      HashSet<Location> pathEndpoints = new HashSet<>();
      for (List<Location> path : shortestPaths) {
        assertEquals(graphSpec.getPathCost(path), shortestPathCost);
        pathEndpoints.add(ListUtils.last(path));
      }
      assertEquals(expectedGoals, pathEndpoints);
    }
  }

  public static void printSearchResult(AStarSearchResult<?> result) {
    System.out.println("\tstart: " + result.getStart());
    System.out.println("\tnodesExamined: " + result.getNumNodesExamined());
    System.out.println("\treachedGoal: " + result.getReachedGoal());
    System.out.println("\tshortestPathCost: " + result.getShortestPathCost());
    System.out.println("\tshortestPath:\n\t\t" + result.getShortestPath());
    if (result instanceof AStarMultiPathResult) {
      AStarMultiPathResult<?> multiPathResult = (AStarMultiPathResult<?>)result;
      System.out.println("\treachedGoals: " + multiPathResult.getReachedGoals());
      System.out.println("\tshortestPaths:");
      printPaths(multiPathResult.getShortestPaths(), "\t\t");
    }
  }

}